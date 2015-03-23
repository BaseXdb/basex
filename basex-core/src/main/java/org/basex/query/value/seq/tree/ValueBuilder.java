package org.basex.query.value.seq.tree;

import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * A builder for creating a {@link Value} by prepending and appending {@link Item}s.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class ValueBuilder {
  /** Capacity of the root. */
  private static final int CAP = 2 * TreeSeq.MAX_DIGIT;
  /** Size of inner nodes. */
  private static final int NODE_SIZE = (TreeSeq.MIN_LEAF + TreeSeq.MAX_LEAF + 1) / 2;

  /** Ring buffer containing the root-level elements. */
  private final Item[] vals = new Item[CAP];

  /** Number of elements in left digit. */
  private int inLeft;
  /** Middle between left and right digit in the buffer. */
  private int mid = CAP / 2;
  /** Number of elements in right digit. */
  private int inRight;
  /** Builder for the middle tree. */
  private final FingerTreeBuilder<Item> tree = new FingerTreeBuilder<>();

  /**
   * Adds an element to the start of the array.
   * @param elem element to add
   * @return reference to this builder for convenience
   */
  public ValueBuilder addFront(final Item elem) {
    if(inLeft < TreeSeq.MAX_DIGIT) {
      // just insert the element
      vals[(mid - inLeft + CAP - 1) % CAP] = elem;
      inLeft++;
    } else if(tree.isEmpty() && inRight < TreeSeq.MAX_DIGIT) {
      // move the middle to the left
      mid = (mid + CAP - 1) % CAP;
      vals[(mid - inLeft + CAP) % CAP] = elem;
      inRight++;
    } else {
      // push leaf node into the tree
      final Item[] leaf = new Item[NODE_SIZE];
      final int start = (mid - NODE_SIZE + CAP) % CAP;
      for(int i = 0; i < NODE_SIZE; i++) leaf[i] = vals[(start + i) % CAP];
      tree.prepend(new LeafNode(leaf));

      // move rest of the nodes to the right
      final int rest = inLeft - NODE_SIZE;
      final int p0 = (mid - inLeft + CAP) % CAP;
      for(int i = 0; i < rest; i++) {
        final int from = (p0 + i) % CAP, to = (from + NODE_SIZE) % CAP;
        vals[to] = vals[from];
      }

      // insert the element
      vals[(mid - rest + CAP - 1) % CAP] = elem;
      inLeft = rest + 1;
    }
    return this;
  }

  /**
   * Adds an element to the end of the array.
   * @param elem element to add
   * @return reference to this builder for convenience
   */
  public ValueBuilder add(final Item elem) {
    if(inRight < TreeSeq.MAX_DIGIT) {
      // just insert the element
      vals[(mid + inRight) % CAP] = elem;
      inRight++;
    } else if(tree.isEmpty() && inLeft < TreeSeq.MAX_DIGIT) {
      // move the middle to the right
      mid = (mid + 1) % CAP;
      vals[(mid + inRight + CAP - 1) % CAP] = elem;
      inLeft++;
    } else {
      // push leaf node into the tree
      final Item[] leaf = new Item[NODE_SIZE];
      final int start = mid;
      for(int i = 0; i < NODE_SIZE; i++) leaf[i] = vals[(start + i) % CAP];
      tree.append(new LeafNode(leaf));

      // move rest of the nodes to the right
      final int rest = inRight - NODE_SIZE;
      for(int i = 0; i < rest; i++) {
        final int to = (mid + i) % CAP, from = (to + NODE_SIZE) % CAP;
        vals[to] = vals[from];
      }

      // insert the element
      vals[(mid + rest) % CAP] = elem;
      inRight = rest + 1;
    }
    return this;
  }

  /**
   * Appends the items of the given value to this builder.
   * @param val value to append
   * @return this builder for convenience
   */
  public ValueBuilder add(final Value val) {
    if(!(val instanceof BigSeq)) {
      for(final Item it : val) add(it);
      return this;
    }

    final BigSeq big = (BigSeq) val;
    final Item[] ls = big.left, rs = big.right;
    final FingerTree<Item, Item> midTree = big.middle;
    if(midTree.isEmpty()) {
      for(final Item l : big.left) add(l);
      for(final Item r : big.right) add(r);
      return this;
    }

    // merge middle digits
    if(tree.isEmpty()) {
      final int k = inLeft + inRight;
      final Item[] temp = new Item[k];
      final int l = (mid - inLeft + CAP) % CAP, m = CAP - l;
      if(k <= m) {
        System.arraycopy(vals, l, temp, 0, k);
      } else {
        System.arraycopy(vals, l, temp, 0, m);
        System.arraycopy(vals, 0, temp, m, k - m);
      }

      inLeft = inRight = 0;
      tree.append(midTree);
      for(int i = ls.length; --i >= 0;) addFront(ls[i]);
      for(int i = k; --i >= 0;) addFront(temp[i]);
      for(int i = 0; i < rs.length; i++) add(rs[i]);
      return this;
    }

    final int inMiddle = inRight + big.left.length,
        leaves = (inMiddle + TreeSeq.MAX_LEAF - 1) / TreeSeq.MAX_LEAF,
        leafSize = (inMiddle + leaves - 1) / leaves;
    for(int i = 0, l = 0; l < leaves; l++) {
      final int inLeaf = Math.min(leafSize, inMiddle - i);
      final Item[] leaf = new Item[inLeaf];
      for(int p = 0; p < inLeaf; p++) {
        leaf[p] = i < inRight ? vals[(mid + i) % CAP] : big.left[i - inRight];
        i++;
      }
      tree.append(new LeafNode(leaf));
    }

    tree.append(big.middle);
    inRight = 0;
    for(final Item r : big.right) add(r);
    return this;
  }

  /**
   * Creates a sequence containing the current elements of this builder.
   * @return resulting sequence
   */
  public Value value() {
    return value(null);
  }

  /**
   * Creates a sequence containing the current elements of this builder.
   * @param ret type of all elements, may be {@code null}
   * @return resulting sequence
   */
  public Value value(final Type ret) {
    final int n = inLeft + inRight;
    if(n == 0) return Empty.SEQ;

    final int start = (mid - inLeft + CAP) % CAP;
    if(n == 1) return vals[start];

    if(n <= TreeSeq.MAX_SMALL) {
      // small int array, fill directly
      final Item[] small = new Item[n];
      for(int i = 0; i < n; i++) small[i] = vals[(start + i) % CAP];
      return new SmallSeq(small, ret);
    }

    // deep array
    final int a = tree.isEmpty() ? n / 2 : inLeft, b = n - a;
    final Item[] ls = new Item[a], rs = new Item[b];
    for(int i = 0; i < a; i++) ls[i] = vals[(start + i) % CAP];
    for(int i = a; i < n; i++) rs[i - a] = vals[(start + i) % CAP];
    return new BigSeq(ls, tree.freeze(), rs, ret);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('[');
    if(tree.isEmpty()) {
      final int n = inLeft + inRight, first = (mid - inLeft + CAP) % CAP;
      if(n > 0) {
        sb.append(vals[first]);
        for(int i = 1; i < n; i++) sb.append(", ").append(vals[(first + i) % CAP]);
      }
      return sb.append(']').toString();
    }

    if(inLeft > 0) {
      final int first = (mid - inLeft + CAP) % CAP;
      sb.append(vals[first]);
      for(int i = 1; i < inLeft; i++) sb.append(", ").append(vals[(first + i) % CAP]);
      sb.append(", ");
    }

    tree.toString(sb);
    for(int i = 0; i < inRight; i++) sb.append(", ").append(vals[(mid + i) % CAP]);
    return sb.append(']').toString();
  }
}
