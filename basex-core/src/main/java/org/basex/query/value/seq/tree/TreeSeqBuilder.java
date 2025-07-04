package org.basex.query.value.seq.tree;

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
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class TreeSeqBuilder implements SeqBuilder {
  /** Capacity of the root. */
  private static final int CAP = 2 * TreeSeq.MAX_DIGIT;
  /** Size of inner nodes. */
  private static final int NODE_SIZE = (TreeSeq.MIN_LEAF + TreeSeq.MAX_LEAF + 1) / 2;

  /** Ring buffer containing the root-level items. */
  private final Item[] items = new Item[CAP];

  /** Number of items in left digit. */
  private int inLeft;
  /** Middle between left and right digit in the buffer. */
  private int mid = CAP / 2;
  /** Number of items in right digit. */
  private int inRight;
  /** Builder for the middle tree. */
  private final FingerTreeBuilder<Item> tree = new FingerTreeBuilder<>();

  /**
   * Adds an item to the start of the sequence.
   * @param item item to add
   * @return reference to this builder for convenience
   */
  public TreeSeqBuilder prepend(final Item item) {
    if(inLeft < TreeSeq.MAX_DIGIT) {
      // just insert the item
      items[(mid - inLeft + CAP - 1) % CAP] = item;
      inLeft++;
    } else if(tree.isEmpty() && inRight < TreeSeq.MAX_DIGIT) {
      // move the middle to the left
      mid = (mid + CAP - 1) % CAP;
      items[(mid - inLeft + CAP) % CAP] = item;
      inRight++;
    } else {
      // push leaf node into the tree
      tree.prepend(new LeafNode(items(mid - NODE_SIZE, NODE_SIZE)));

      // move rest of the nodes to the right
      final int rest = inLeft - NODE_SIZE;
      final int p0 = (mid - inLeft + CAP) % CAP;
      for(int i = 0; i < rest; i++) {
        final int from = (p0 + i) % CAP, to = (from + NODE_SIZE) % CAP;
        items[to] = items[from];
      }

      // insert the item
      items[(mid - rest + CAP - 1) % CAP] = item;
      inLeft = rest + 1;
    }
    return this;
  }

  @Override
  public TreeSeqBuilder add(final Item item) {
    if(inRight < TreeSeq.MAX_DIGIT) {
      // just insert the item
      items[(mid + inRight) % CAP] = item;
      inRight++;
    } else if(tree.isEmpty() && inLeft < TreeSeq.MAX_DIGIT) {
      // move the middle to the right
      mid = (mid + 1) % CAP;
      items[(mid + inRight + CAP - 1) % CAP] = item;
      inLeft++;
    } else {
      // push leaf node into the tree
      tree.append(new LeafNode(items(mid, NODE_SIZE)));
      // move rest of the nodes to the right
      final int rest = inRight - NODE_SIZE;
      for(int i = 0; i < rest; i++) {
        final int to = (mid + i) % CAP, from = (to + NODE_SIZE) % CAP;
        items[to] = items[from];
      }
      // insert the item
      items[(mid + rest) % CAP] = item;
      inRight = rest + 1;
    }
    return this;
  }

  @Override
  public TreeSeqBuilder add(final Value value, final QueryContext qc) {
    // shortcut for adding single items
    if(value.size() == 1) return add((Item) value);

    if(!(value instanceof final BigSeq big)) {
      final BasicIter<?> iter = value.iter();
      for(Item item; (item = iter.next()) != null;) {
        qc.checkStop();
        add(item);
      }
      return this;
    }

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
        Array.copyToStart(items, l, k, temp);
      } else {
        Array.copyToStart(items, l, m, temp);
        Array.copyFromStart(items, k - m, temp, m);
      }

      inLeft = inRight = 0;
      tree.append(midTree);
      for(int i = ls.length; --i >= 0;) {
        qc.checkStop();
        prepend(ls[i]);
      }
      for(int i = k; --i >= 0;) {
        qc.checkStop();
        prepend(temp[i]);
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
        leaf[p] = i < inRight ? items[(mid + i) % CAP] : ls[i - inRight];
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

  @Override
  public Seq value(final Type type) {
    // small int sequence, fill directly
    final int n = inLeft + inRight, start = (mid - inLeft + CAP) % CAP;
    if(n <= TreeSeq.MAX_SMALL) return new SmallSeq(items(start, n), type);

    // deep sequence
    final int ll = tree.isEmpty() ? n / 2 : inLeft;
    return new BigSeq(items(start, ll), tree.freeze(), items(start + ll, n - ll), type);
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
    final int p = (from % CAP + CAP) % CAP, m = CAP - p;
    if(n <= m) {
      Array.copyToStart(items, p, n, arr);
    } else {
      Array.copyToStart(items, p, m, arr);
      Array.copyFromStart(items, n - m, arr, m);
    }
    return arr;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Util.className(this)).append('[');
    if(tree.isEmpty()) {
      final int n = inLeft + inRight, first = (mid - inLeft + CAP) % CAP;
      if(n > 0) {
        sb.append(items[first]);
        for(int i = 1; i < n; i++) sb.append(", ").append(items[(first + i) % CAP]);
      }
    } else {
      final int first = (mid - inLeft + CAP) % CAP;
      sb.append(items[first]);
      for(int i = 1; i < inLeft; i++) sb.append(", ").append(items[(first + i) % CAP]);
      for(final Item item : tree) sb.append(", ").append(item);
      for(int i = 0; i < inRight; i++) sb.append(", ").append(items[(mid + i) % CAP]);
    }
    return sb.append(']').toString();
  }
}
