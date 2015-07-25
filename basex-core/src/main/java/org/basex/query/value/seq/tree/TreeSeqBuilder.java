package org.basex.query.value.seq.tree;

import java.util.*;

import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * A builder for creating a {@link Seq}uence (with at least 2 items) by prepending and appending
 * {@link Item}s.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class TreeSeqBuilder implements Iterable<Item> {
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
   * Returns a {@link Value} representation of the given items.
   * @param items array containing the items
   * @param n number of items (must be {@code 2} or more)
   * @param type item type of the resulting value (not checked), may be {@code null}
   * @return the value
   */
  public static Seq value(final Item[] items, final int n, final Type type) {
    if(n < 2) throw new AssertionError("At least 2 items expected");

    if(n <= TreeSeq.MAX_SMALL) {
      final Item[] small = new Item[n];
      System.arraycopy(items, 0, small, 0, n);
      return new SmallSeq(small, type);
    }

    final TreeSeqBuilder tsb = new TreeSeqBuilder();
    for(int i = 0; i < n; i++) tsb.add(items[i]);
    return tsb.seq(type);
  }

  /**
   * Adds an element to the start of the array.
   * @param elem element to add
   * @return reference to this builder for convenience
   */
  public TreeSeqBuilder addFront(final Item elem) {
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
      tree.prepend(new LeafNode(items(mid - NODE_SIZE, NODE_SIZE)));

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
  public TreeSeqBuilder add(final Item elem) {
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
      tree.append(new LeafNode(items(mid, NODE_SIZE)));

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
  public TreeSeqBuilder add(final Value val) {
    // shortcut for adding single items
    if(val instanceof Item) return add((Item) val);

    if(!(val instanceof BigSeq)) {
      for(final Item it : val) add(it);
      return this;
    }

    final BigSeq big = (BigSeq) val;
    final Item[] ls = big.left, rs = big.right;
    final FingerTree<Item, Item> midTree = big.middle;
    if(midTree.isEmpty()) {
      for(final Item l : ls) add(l);
      for(final Item r : rs) add(r);
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

    final int inMiddle = inRight + ls.length,
        leaves = (inMiddle + TreeSeq.MAX_LEAF - 1) / TreeSeq.MAX_LEAF,
        leafSize = (inMiddle + leaves - 1) / leaves;
    for(int i = 0, l = 0; l < leaves; l++) {
      final int inLeaf = Math.min(leafSize, inMiddle - i);
      final Item[] leaf = new Item[inLeaf];
      for(int p = 0; p < inLeaf; p++) {
        leaf[p] = i < inRight ? vals[(mid + i) % CAP] : ls[i - inRight];
        i++;
      }
      tree.append(new LeafNode(leaf));
    }

    tree.append(midTree);
    inRight = 0;
    for(final Item r : rs) add(r);
    return this;
  }

  /**
   * Creates a sequence containing the current elements of this builder.
   * @return resulting sequence
   */
  Seq seq() {
    return seq((Type) null);
  }

  /**
   * Creates a sequence containing the current elements of this builder.
   * @param ret type of all elements, may be {@code null}
   * @return resulting sequence
   */
  public Seq seq(final Type ret) {
    final int n = inLeft + inRight;
    final int start = (mid - inLeft + CAP) % CAP;
    if(n < 2) throw new AssertionError("At least 2 items expected");

    // small int array, fill directly
    if(n <= TreeSeq.MAX_SMALL) return new SmallSeq(items(start, n), ret);

    // deep array
    final int ll = tree.isEmpty() ? n / 2 : inLeft;
    return new BigSeq(items(start, ll), tree.freeze(), items(start + ll, n - ll), ret);
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
      for(final Item item : tree) sb.append(", ").append(item);
      for(int i = 0; i < inRight; i++) sb.append(", ").append(vals[(mid + i) % CAP]);
    }
    return sb.append(']').toString();
  }

  @Override
  public Iterator<Item> iterator() {

    return new Iterator<Item>() {
      private int pos = -inLeft;
      private Iterator<Item> sub;

      @Override
      public boolean hasNext() {
        return pos <= inRight;
      }

      @Override
      public Item next() {
        if(pos > inRight) throw new NoSuchElementException();

        if(pos < 0) {
          final int p = pos++;
          return vals[(mid + p + CAP) % CAP];
        }

        if(pos == 0) {
          if(tree != null) {
            if(sub == null) sub = tree.iterator();
            if(sub.hasNext()) return sub.next();
            sub = null;
          }
          pos++;
        }

        // pos > 0
        final int p = pos++;
        return vals[(mid + p - 1) % CAP];
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * Returns an array containing the given number of items starting at the given position in this
   * builder's ring buffer.
   * @param from position of the first item
   * @param n number of items
   * @return array containing the items
   */
  private Item[] items(final int from, final int n) {
    final Item[] arr = new Item[n];
    final int p = ((from % CAP) + CAP) % CAP;
    final int m = CAP - p;
    if(n <= m) {
      System.arraycopy(vals, p, arr, 0, n);
    } else {
      System.arraycopy(vals, p, arr, 0, m);
      System.arraycopy(vals, 0, arr, m, n - m);
    }
    return arr;
  }
}
