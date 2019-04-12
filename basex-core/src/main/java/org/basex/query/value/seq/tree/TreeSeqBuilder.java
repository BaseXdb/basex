package org.basex.query.value.seq.tree;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A builder for creating a {@link Seq}uence (with at least 2 items) by prepending and appending
 * {@link Item}s.
 *
 * @author BaseX Team 2005-19, BSD License
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
   * @param size number of items (must be {@code 2} or more)
   * @param type type (can be {@code null})
   * @return the value
   */
  public static Seq value(final Item[] items, final int size, final Type type) {
    if(size <= TreeSeq.MAX_SMALL) {
      final Item[] small = new Item[size];
      Array.copy(items, size, small);
      return new SmallSeq(small, type);
    }
    final TreeSeqBuilder tsb = new TreeSeqBuilder();
    for(int i = 0; i < size; i++) tsb.add(items[i]);
    return tsb.seq(type);
  }

  /**
   * Adds an item to the start of the array.
   * @param item item to add
   * @return reference to this builder for convenience
   */
  public TreeSeqBuilder addFront(final Item item) {
    if(inLeft < TreeSeq.MAX_DIGIT) {
      // just insert the element
      vals[(mid - inLeft + CAP - 1) % CAP] = item;
      inLeft++;
    } else if(tree.isEmpty() && inRight < TreeSeq.MAX_DIGIT) {
      // move the middle to the left
      mid = (mid + CAP - 1) % CAP;
      vals[(mid - inLeft + CAP) % CAP] = item;
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
      vals[(mid - rest + CAP - 1) % CAP] = item;
      inLeft = rest + 1;
    }
    return this;
  }

  /**
   * Adds an item to the end of the array.
   * @param item item to add
   * @return reference to this builder for convenience
   */
  public TreeSeqBuilder add(final Item item) {
    if(inRight < TreeSeq.MAX_DIGIT) {
      // just insert the element
      vals[(mid + inRight) % CAP] = item;
      inRight++;

    } else if(tree.isEmpty() && inLeft < TreeSeq.MAX_DIGIT) {
      // move the middle to the right
      mid = (mid + 1) % CAP;
      vals[(mid + inRight + CAP - 1) % CAP] = item;
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
      // insert the item
      vals[(mid + rest) % CAP] = item;
      inRight = rest + 1;
    }
    return this;
  }

  /**
   * Appends the items of the given value to this builder.
   * @param val value to append
   * @param qc query context
   * @return this builder for convenience
   */
  public TreeSeqBuilder add(final Value val, final QueryContext qc) {
    // shortcut for adding single items
    if(val.isItem()) return add((Item) val);

    if(!(val instanceof BigSeq)) {
      final BasicIter<?> iter = val.iter();
      for(Item item; (item = iter.next()) != null;) {
        qc.checkStop();
        add(item);
      }
      return this;
    }

    final BigSeq big = (BigSeq) val;
    final Item[] ls = big.left, rs = big.right;
    final FingerTree<Item, Item> midTree = big.middle;
    if(midTree.isEmpty()) {
      for(final Item l : ls) {
        qc.checkStop();
        add(l);
      }
      for(final Item r : rs) {
        qc.checkStop();
        add(r);
      }
      return this;
    }

    // merge middle digits
    if(tree.isEmpty()) {
      final int k = inLeft + inRight;
      final Item[] temp = new Item[k];
      final int l = (mid - inLeft + CAP) % CAP, m = CAP - l;
      if(k <= m) {
        Array.copyToStart(vals, l, k, temp);
      } else {
        Array.copyToStart(vals, l, m, temp);
        Array.copyFromStart(vals, k - m, temp, m);
      }

      inLeft = inRight = 0;
      tree.append(midTree);
      for(int i = ls.length; --i >= 0;) {
        qc.checkStop();
        addFront(ls[i]);
      }
      for(int i = k; --i >= 0;) {
        qc.checkStop();
        addFront(temp[i]);
      }
      for(final Item r : rs) {
        qc.checkStop();
        add(r);
      }
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
    for(final Item r : rs) {
      qc.checkStop();
      add(r);
    }
    return this;
  }

  /**
   * Creates a sequence containing the current elements of this builder.
   * @return resulting sequence
   */
  public Seq seq() {
    return seq(null);
  }

  /**
   * Creates a sequence containing the current elements of this builder.
   * @param type type of all elements, may be {@code null}
   * @return resulting sequence
   */
  public Seq seq(final Type type) {
    // small int array, fill directly
    final int n = inLeft + inRight, start = (mid - inLeft + CAP) % CAP;
    if(n <= TreeSeq.MAX_SMALL) return new SmallSeq(items(start, n), type);

    // deep array
    final int ll = tree.isEmpty() ? n / 2 : inLeft;
    return new BigSeq(items(start, ll), tree.freeze(), items(start + ll, n - ll), type);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Util.className(this)).append('[');
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
        if(pos < 0) {
          final int p = pos++;
          return vals[(mid + p + CAP) % CAP];
        }

        if(pos == 0) {
          if(sub == null) sub = tree.iterator();
          if(sub.hasNext()) return sub.next();
          sub = null;
          pos++;
        }

        // pos > 0
        final int p = pos++;
        return vals[(mid + p - 1) % CAP];
      }

      @Override
      public void remove() {
        throw Util.notExpected();
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
    final int p = ((from % CAP) + CAP) % CAP, m = CAP - p;
    if(n <= m) {
      Array.copyToStart(vals, p, n, arr);
    } else {
      Array.copyToStart(vals, p, m, arr);
      Array.copyFromStart(vals, n - m, arr, m);
    }
    return arr;
  }
}
