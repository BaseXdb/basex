package org.basex.query.value.array;

import org.basex.core.jobs.*;
import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A builder for creating an {@link XQArray} by appending members.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
final class TreeArrayBuilder extends ArrBuilder {
  /** Capacity of the root. */
  private static final int CAP = 2 * TreeArray.MAX_DIGIT;
  /** Size of inner nodes. */
  private static final int NODE_SIZE = (TreeArray.MIN_LEAF + TreeArray.MAX_LEAF + 1) / 2;

  /** Ring buffer containing the root-level members. */
  private Value[] members = new Value[CAP];
  /** Builder for the middle tree. */
  private FingerTreeBuilder<Value> tree = new FingerTreeBuilder<>();

  /** Number of members in left digit. */
  private int inLeft;
  /** Middle between left and right digit in the buffer. */
  private int mid = CAP / 2;
  /** Number of members in right digit. */
  private int inRight;

  /**
   * Adds a member to the start of the array.
   * @param member member to add
   * @return reference to this builder for convenience
   */
  TreeArrayBuilder prepend(final Value member) {
    if(inLeft < TreeArray.MAX_DIGIT) {
      // just insert the member
      members[(mid - inLeft + CAP - 1) % CAP] = member;
      inLeft++;
    } else if(tree.isEmpty() && inRight < TreeArray.MAX_DIGIT) {
      // move the middle to the left
      mid = (mid + CAP - 1) % CAP;
      members[(mid - inLeft + CAP) % CAP] = member;
      inRight++;
    } else {
      // push leaf node into the tree
      tree.prepend(new LeafNode(members(mid - NODE_SIZE, NODE_SIZE)));

      // move rest of the nodes to the right
      final int rest = inLeft - NODE_SIZE;
      final int p0 = (mid - inLeft + CAP) % CAP;
      for(int i = 0; i < rest; i++) {
        final int from = (p0 + i) % CAP, to = (from + NODE_SIZE) % CAP;
        members[to] = members[from];
      }

      // insert the member
      members[(mid - rest + CAP - 1) % CAP] = member;
      inLeft = rest + 1;
    }
    return this;
  }

  /**
   * Appends the members of an array, reusing its middle tree.
   * @param big array
   * @param job interruptible job
   * @return reference to this builder for convenience
   */
  TreeArrayBuilder add(final BigArray big, final Job job) {
    final Value[] ls = big.left, rs = big.right;
    final FingerTree<Value, Value> midTree = big.middle;
    if(midTree.isEmpty()) {
      for(final Value l : ls) {
        job.checkStop();
        add(l);
      }
      for(final Value r : rs) {
        job.checkStop();
        add(r);
      }
      return this;
    }

    // merge middle digits
    if(tree.isEmpty()) {
      final int k = inLeft + inRight;
      final Value[] temp = members(mid - inLeft, k);

      inLeft = inRight = 0;
      tree.append(midTree);
      for(int i = ls.length; --i >= 0;) {
        job.checkStop();
        prepend(ls[i]);
      }
      for(int i = k; --i >= 0;) {
        job.checkStop();
        prepend(temp[i]);
      }
      for(final Value r : rs) {
        job.checkStop();
        add(r);
      }
      return this;
    }

    final int inMiddle = inRight + ls.length,
        leaves = (inMiddle + TreeArray.MAX_LEAF - 1) / TreeArray.MAX_LEAF,
        leafSize = (inMiddle + leaves - 1) / leaves;
    for(int i = 0, l = 0; l < leaves; l++) {
      final int inLeaf = Math.min(leafSize, inMiddle - i);
      final Value[] leaf = new Value[inLeaf];
      for(int p = 0; p < inLeaf; p++) {
        leaf[p] = i < inRight ? members[(mid + i) % CAP] : ls[i - inRight];
        i++;
      }
      tree.append(new LeafNode(leaf));
    }

    tree.append(midTree);
    inRight = 0;
    for(final Value r : rs) {
      job.checkStop();
      add(r);
    }
    return this;
  }

  /**
   * Returns an array containing the given number of members starting at the given position in
   * this builder’s ring buffer.
   * @param from position of the first member
   * @param n number of members
   * @return array containing the members
   */
  private Value[] members(final int from, final int n) {
    final Value[] arr = new Value[n];
    final int p = (from % CAP + CAP) % CAP, m = CAP - p;
    if(n <= m) {
      Array.copyToStart(members, p, n, arr);
    } else {
      Array.copyToStart(members, p, m, arr);
      Array.copyFromStart(members, n - m, arr, m);
    }
    return arr;
  }

  @Override
  public TreeArrayBuilder add(final Value member) {
    if(inRight < TreeArray.MAX_DIGIT) {
      // just insert the member
      members[(mid + inRight) % CAP] = member;
      inRight++;
    } else if(tree.isEmpty() && inLeft < TreeArray.MAX_DIGIT) {
      // move the middle to the right
      mid = (mid + 1) % CAP;
      members[(mid + inRight + CAP - 1) % CAP] = member;
      inLeft++;
    } else {
      // push leaf node into the tree
      tree.append(new LeafNode(members(mid, NODE_SIZE)));

      // move rest of the nodes to the left
      final int rest = inRight - NODE_SIZE;
      for(int i = 0; i < rest; i++) {
        final int to = (mid + i) % CAP, from = (to + NODE_SIZE) % CAP;
        members[to] = members[from];
      }

      // insert the member
      members[(mid + rest) % CAP] = member;
      inRight = rest + 1;
    }
    return this;
  }

  @Override
  public XQArray array(final ArrayType type) {
    final int n = inLeft + inRight, start = (mid - inLeft + CAP) % CAP;
    final XQArray array;
    if(n == 0) {
      array = XQArray.empty();
    } else if(n == 1) {
      array = XQArray.get(members[start]);
    } else if(n <= TreeArray.MAX_SMALL) {
      // small array, fill directly
      array = new SmallArray(members(start, n), type);
    } else {
      // deep array
      final int a = tree.isEmpty() ? n / 2 : inLeft;
      array = new BigArray(members(start, a), tree.freeze(), members(start + a, n - a), type);
    }
    // invalidate data structures
    members = null;
    tree = null;
    return array;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Util.className(this)).append('[');
    if(tree.isEmpty()) {
      final int n = inLeft + inRight, first = (mid - inLeft + CAP) % CAP;
      if(n > 0) {
        sb.append(members[first]);
        for(int i = 1; i < n; i++) sb.append(", ").append(members[(first + i) % CAP]);
      }
    } else {
      final int first = (mid - inLeft + CAP) % CAP;
      sb.append(members[first]);
      for(int i = 1; i < inLeft; i++) sb.append(", ").append(members[(first + i) % CAP]);
      for(final Value value : tree) sb.append(", ").append(value);
      for(int i = 0; i < inRight; i++) sb.append(", ").append(members[(mid + i) % CAP]);
    }
    return sb.append(']').toString();
  }
}
