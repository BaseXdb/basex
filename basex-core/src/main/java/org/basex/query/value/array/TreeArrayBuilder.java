package org.basex.query.value.array;

import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A builder for creating an {@link XQArray} by prepending and appending members.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
final class TreeArrayBuilder implements ArrBuilder {
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
   * @return self reference
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
    return this;
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
    return this;
  }

  /**
   * Creates an {@link XQArray} containing the members of this builder.
   * @return resulting array
   */
  XQArray array() {
    return array(ArrayType.ARRAY);
  }

  @Override
  public XQArray array(final ArrayType type) {
    // invalidate data structures
    final FingerTreeBuilder<Value> builder = tree;
    final Value[] values = members;
    members = null;
    tree = null;

    final int n = inLeft + inRight;
    if(n == 0) return XQArray.empty();
    final int start = (mid - inLeft + CAP) % CAP;
    if(n == 1) return XQArray.get(values[start]);

    if(n <= TreeArray.MAX_SMALL) {
      // small int array, fill directly
      final Value[] small = new Value[n];
      for(int i = 0; i < n; i++) small[i] = values[(start + i) % CAP];
      return new SmallArray(small, type);
    }

    // deep array
    final int a = builder.isEmpty() ? n / 2 : inLeft, b = n - a;
    final Value[] ls = new Value[a], rs = new Value[b];
    for(int i = 0; i < a; i++) ls[i] = values[(start + i) % CAP];
    for(int i = a; i < n; i++) rs[i - a] = values[(start + i) % CAP];
    return new BigArray(ls, builder.freeze(), rs, type);
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
