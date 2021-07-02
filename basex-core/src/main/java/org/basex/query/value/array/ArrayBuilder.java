package org.basex.query.value.array;

import org.basex.query.expr.*;
import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A builder for creating an {@link XQArray} by prepending and appending members.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class ArrayBuilder {
  /** Capacity of the root. */
  private static final int CAP = 2 * XQArray.MAX_DIGIT;
  /** Size of inner nodes. */
  private static final int NODE_SIZE = (XQArray.MIN_LEAF + XQArray.MAX_LEAF + 1) / 2;

  /** Ring buffer containing the root-level members. */
  private final Value[] members = new Value[CAP];

  /** Number of members in left digit. */
  private int inLeft;
  /** Middle between left and right digit in the buffer. */
  private int mid = CAP / 2;
  /** Number of members in right digit. */
  private int inRight;
  /** Builder for the middle tree. */
  private final FingerTreeBuilder<Value> tree = new FingerTreeBuilder<>();

  /**
   * Adds a member to the start of the array.
   * @param member member to add
   */
  public void prepend(final Value member) {
    if(inLeft < XQArray.MAX_DIGIT) {
      // just insert the member
      members[(mid - inLeft + CAP - 1) % CAP] = member;
      inLeft++;
    } else if(tree.isEmpty() && inRight < XQArray.MAX_DIGIT) {
      // move the middle to the left
      mid = (mid + CAP - 1) % CAP;
      members[(mid - inLeft + CAP) % CAP] = member;
      inRight++;
    } else {
      // push leaf node into the tree
      final Value[] leaf = new Value[NODE_SIZE];
      final int start = (mid - NODE_SIZE + CAP) % CAP;
      for(int i = 0; i < NODE_SIZE; i++) leaf[i] = members[(start + i) % CAP];
      tree.prepend(new LeafNode(leaf));

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
  }

  /**
   * Adds a member to the end of the array.
   * @param member member to add
   */
  public void append(final Value member) {
    if(inRight < XQArray.MAX_DIGIT) {
      // just insert the member
      members[(mid + inRight) % CAP] = member;
      inRight++;
    } else if(tree.isEmpty() && inLeft < XQArray.MAX_DIGIT) {
      // move the middle to the right
      mid = (mid + 1) % CAP;
      members[(mid + inRight + CAP - 1) % CAP] = member;
      inLeft++;
    } else {
      // push leaf node into the tree
      final Value[] leaf = new Value[NODE_SIZE];
      final int start = mid;
      for(int i = 0; i < NODE_SIZE; i++) leaf[i] = members[(start + i) % CAP];
      tree.append(new LeafNode(leaf));

      // move rest of the nodes to the right
      final int rest = inRight - NODE_SIZE;
      for(int i = 0; i < rest; i++) {
        final int to = (mid + i) % CAP, from = (to + NODE_SIZE) % CAP;
        members[to] = members[from];
      }

      // insert the member
      members[(mid + rest) % CAP] = member;
      inRight = rest + 1;
    }
  }

  /**
   * Appends the given array to this builder.
   * @param array array to append
   */
  public void append(final XQArray array) {
    if(!(array instanceof BigArray)) {
      for(final Value value : array.members()) append(value);
      return;
    }

    final BigArray big = (BigArray) array;
    final Value[] ls = big.left, rs = big.right;
    final FingerTree<Value, Value> midTree = big.middle;
    if(midTree.isEmpty()) {
      for(final Value l : big.left) append(l);
      for(final Value r : big.right) append(r);
      return;
    }

    // merge middle digits
    if(tree.isEmpty()) {
      final int k = inLeft + inRight;
      final Value[] temp = new Value[k];
      final int l = (mid - inLeft + CAP) % CAP, m = CAP - l;
      if(k <= m) {
        Array.copyToStart(members, l, k, temp);
      } else {
        Array.copyToStart(members, l, m, temp);
        Array.copyFromStart(members, k - m, temp, m);
      }

      inLeft = inRight = 0;
      tree.append(midTree);
      for(int i = ls.length; --i >= 0;) prepend(ls[i]);
      for(int i = k; --i >= 0;) prepend(temp[i]);
      for(final Value r : rs) append(r);
      return;
    }

    final int inMiddle = inRight + big.left.length,
        leaves = (inMiddle + XQArray.MAX_LEAF - 1) / XQArray.MAX_LEAF,
        leafSize = (inMiddle + leaves - 1) / leaves;
    for(int i = 0, l = 0; l < leaves; l++) {
      final int inLeaf = Math.min(leafSize, inMiddle - i);
      final Value[] leaf = new Value[inLeaf];
      for(int p = 0; p < inLeaf; p++) {
        leaf[p] = i < inRight ? members[(mid + i) % CAP] : big.left[i - inRight];
        i++;
      }
      tree.append(new LeafNode(leaf));
    }

    tree.append(big.middle);
    inRight = 0;
    for(final Value r : big.right) append(r);
  }

  /**
   * Creates an {@link XQArray} containing the members of this builder.
   * @return resulting array
   */
  public XQArray array() {
    return array(SeqType.ARRAY);
  }

  /**
   * Creates an {@link XQArray} containing the members of this builder.
   * @param type array type
   * @return resulting array
   */
  public XQArray array(final Type type) {
    final int n = inLeft + inRight;
    if(n == 0) return XQArray.empty();

    final int start = (mid - inLeft + CAP) % CAP;
    if(n <= XQArray.MAX_SMALL) {
      // small int array, fill directly
      final Value[] small = new Value[n];
      for(int i = 0; i < n; i++) small[i] = members[(start + i) % CAP];
      return new SmallArray(small, type);
    }

    // deep array
    final int a = tree.isEmpty() ? n / 2 : inLeft, b = n - a;
    final Value[] ls = new Value[a], rs = new Value[b];
    for(int i = 0; i < a; i++) ls[i] = members[(start + i) % CAP];
    for(int i = a; i < n; i++) rs[i - a] = members[(start + i) % CAP];
    return new BigArray(ls, tree.freeze(), rs, type);
  }

  /**
   * Creates an {@link XQArray} containing the members of this builder.
   * @param expr expression that created the array (can be {@code null})
   * @return resulting array
   */
  public XQArray array(final Expr expr) {
    return expr != null ? array(expr.seqType().type) : array();
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
