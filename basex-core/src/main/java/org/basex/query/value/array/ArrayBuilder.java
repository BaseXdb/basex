package org.basex.query.value.array;

import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;

/**
 * A builder for creating an {@link Array} by prepending and appending elements.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class ArrayBuilder {
  /** Capacity of the root. */
  private static final int CAP = 2 * Array.MAX_DIGIT;
  /** Size of inner nodes. */
  private static final int NODE_SIZE = (Array.MIN_LEAF + Array.MAX_LEAF + 1) / 2;

  /** Ring buffer containing the root-level elements. */
  private final Value[] vals = new Value[CAP];

  /** Number of elements in left digit. */
  private int inLeft;
  /** Middle between left and right digit in the buffer. */
  private int mid = CAP / 2;
  /** Number of elements in right digit. */
  private int inRight;
  /** Builder for the middle tree. */
  private final FingerTreeBuilder<Value> tree = new FingerTreeBuilder<>();

  /**
   * Adds an element to the start of the array.
   * @param elem element to add
   * @return self reference for convenience
   */
  public ArrayBuilder prepend(final Value elem) {
    if(inLeft < Array.MAX_DIGIT) {
      // just insert the element
      vals[(mid - inLeft + CAP - 1) % CAP] = elem;
      inLeft++;
    } else if(tree.isEmpty() && inRight < Array.MAX_DIGIT) {
      // move the middle to the left
      mid = (mid + CAP - 1) % CAP;
      vals[(mid - inLeft + CAP) % CAP] = elem;
      inRight++;
    } else {
      // push leaf node into the tree
      final Value[] leaf = new Value[NODE_SIZE];
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
   * @return self reference for convenience
   */
  public ArrayBuilder append(final Value elem) {
    if(inRight < Array.MAX_DIGIT) {
      // just insert the element
      vals[(mid + inRight) % CAP] = elem;
      inRight++;
    } else if(tree.isEmpty() && inLeft < Array.MAX_DIGIT) {
      // move the middle to the right
      mid = (mid + 1) % CAP;
      vals[(mid + inRight + CAP - 1) % CAP] = elem;
      inLeft++;
    } else {
      // push leaf node into the tree
      final Value[] leaf = new Value[NODE_SIZE];
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
   * Appends the given array to this builder.
   * @param arr array to append
   * @return self reference for convenience
   */
  public ArrayBuilder append(final Array arr) {
    if(!(arr instanceof BigArray)) {
      for(final Value val : arr.members()) append(val);
      return this;
    }

    final BigArray big = (BigArray) arr;
    final Value[] ls = big.left, rs = big.right;
    final FingerTree<Value, Value> midTree = big.middle;
    if(midTree.isEmpty()) {
      for(final Value l : big.left) append(l);
      for(final Value r : big.right) append(r);
      return this;
    }

    // merge middle digits
    if(tree.isEmpty()) {
      final int k = inLeft + inRight;
      final Value[] temp = new Value[k];
      final int l = (mid - inLeft + CAP) % CAP, m = CAP - l;
      if(k <= m) {
        System.arraycopy(vals, l, temp, 0, k);
      } else {
        System.arraycopy(vals, l, temp, 0, m);
        System.arraycopy(vals, 0, temp, m, k - m);
      }

      inLeft = inRight = 0;
      tree.append(midTree);
      for(int i = ls.length; --i >= 0;) prepend(ls[i]);
      for(int i = k; --i >= 0;) prepend(temp[i]);
      for(final Value r : rs) append(r);
      return this;
    }

    final int inMiddle = inRight + big.left.length,
        leaves = (inMiddle + Array.MAX_LEAF - 1) / Array.MAX_LEAF,
        leafSize = (inMiddle + leaves - 1) / leaves;
    for(int i = 0, l = 0; l < leaves; l++) {
      final int inLeaf = Math.min(leafSize, inMiddle - i);
      final Value[] leaf = new Value[inLeaf];
      for(int p = 0; p < inLeaf; p++) {
        leaf[p] = i < inRight ? vals[(mid + i) % CAP] : big.left[i - inRight];
        i++;
      }
      tree.append(new LeafNode(leaf));
    }

    tree.append(big.middle);
    inRight = 0;
    for(final Value r : big.right) append(r);
    return this;
  }

  /**
   * Creates an {@link Array} containing the elements of this builder.
   * @return resulting array
   */
  public Array freeze() {
    final int n = inLeft + inRight;
    if(n == 0) return Array.empty();

    final int start = (mid - inLeft + CAP) % CAP;
    if(n <= Array.MAX_SMALL) {
      // small int array, fill directly
      final Value[] small = new Value[n];
      for(int i = 0; i < n; i++) small[i] = vals[(start + i) % CAP];
      return new SmallArray(small);
    }

    // deep array
    final int a = tree.isEmpty() ? n / 2 : inLeft, b = n - a;
    final Value[] ls = new Value[a], rs = new Value[b];
    for(int i = 0; i < a; i++) ls[i] = vals[(start + i) % CAP];
    for(int i = a; i < n; i++) rs[i - a] = vals[(start + i) % CAP];
    return new BigArray(ls, tree.freeze(), rs);
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
    } else {
      final int first = (mid - inLeft + CAP) % CAP;
      sb.append(vals[first]);
      for(int i = 1; i < inLeft; i++) sb.append(", ").append(vals[(first + i) % CAP]);
      for(final Value val : tree) sb.append(", ").append(val);
      for(int i = 0; i < inRight; i++) sb.append(", ").append(vals[(mid + i) % CAP]);
    }
    return sb.append(']').toString();
  }
}
