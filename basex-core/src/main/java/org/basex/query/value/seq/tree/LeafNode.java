package org.basex.query.value.seq.tree;

import java.util.*;

import org.basex.query.util.fingertree.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * A leaf node containing {@link Item}s.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
final class LeafNode implements Node<Item, Item> {
  /** Elements stored in this leaf node. */
  final Item[] values;

  /**
   * Constructor.
   * @param values the values
   */
  LeafNode(final Item[] values) {
    this.values = values;
    assert values.length >= TreeSeq.MIN_LEAF && values.length <= TreeSeq.MAX_LEAF;
  }

  @Override
  public long size() {
    return values.length;
  }

  @Override
  public LeafNode reverse() {
    final int n = values.length;
    final Item[] out = new Item[n];
    for(int i = 0; i < n; i++) out[i] = values[n - 1 - i];
    return new LeafNode(out);
  }

  @Override
  public boolean insert(final Node<Item, Item>[] siblings, final long pos, final Item val) {
    final int p = (int) pos, n = values.length;
    final Item[] vals = new Item[n + 1];
    Array.copy(values, p, vals);
    vals[p] = val;
    Array.copy(values, p, n - p, vals, p + 1);

    if(n < TreeSeq.MAX_LEAF) {
      // there is capacity
      siblings[1] = new LeafNode(vals);
      return false;
    }

    final LeafNode left = (LeafNode) siblings[0];
    if(left != null && left.values.length < TreeSeq.MAX_LEAF) {
      // push elements to the left sibling
      final Item[] lvals = left.values;
      final int l = lvals.length, diff = TreeSeq.MAX_LEAF - l, move = (diff + 1) / 2;
      final Item[] newLeft = new Item[l + move], newRight = new Item[n + 1 - move];
      Array.copy(lvals, l, newLeft);
      Array.copyFromStart(vals, move, newLeft, l);
      Array.copyToStart(vals, move, newRight.length, newRight);
      siblings[0] = new LeafNode(newLeft);
      siblings[1] = new LeafNode(newRight);
      return false;
    }

    final LeafNode right = (LeafNode) siblings[2];
    if(right != null && right.values.length < TreeSeq.MAX_LEAF) {
      // push elements to the right sibling
      final Item[] rvals = right.values;
      final int r = rvals.length, diff = TreeSeq.MAX_LEAF - r, move = (diff + 1) / 2,
          l = n + 1 - move;
      final Item[] newLeft = new Item[l], newRight = new Item[r + move];
      Array.copy(vals, l, newLeft);
      Array.copyToStart(vals, l, move, newRight);
      Array.copyFromStart(rvals, r, newRight, move);
      siblings[1] = new LeafNode(newLeft);
      siblings[2] = new LeafNode(newRight);
      return false;
    }

    // split the node
    final int l = vals.length / 2, r = vals.length - l;
    final Item[] newLeft = new Item[l], newRight = new Item[r];
    Array.copy(vals, l, newLeft);
    Array.copyToStart(vals, l, r, newRight);
    siblings[3] = siblings[2];
    siblings[1] = new LeafNode(newLeft);
    siblings[2] = new LeafNode(newRight);
    return true;
  }

  @Override
  public LeafNode set(final long pos, final Item val) {
    final Item[] vals = values.clone();
    vals[(int) pos] = val;
    return new LeafNode(vals);
  }

  @Override
  public NodeLike<Item, Item>[] remove(final Node<Item, Item> left,
      final Node<Item, Item> right, final long pos) {
    final int p = (int) pos, n = values.length;
    @SuppressWarnings("unchecked")
    final NodeLike<Item, Item>[] out = new NodeLike[] { left, null, right };
    if(n > TreeSeq.MIN_LEAF) {
      // we do not have to split
      final Item[] vals = new Item[n - 1];
      Array.copy(values, p, vals);
      Array.copy(values, p + 1, n - 1 - p, vals, p);
      out[1] = new LeafNode(vals);
      return out;
    }

    final LeafNode leftLeaf = (LeafNode) left;
    if(leftLeaf != null && leftLeaf.arity() > TreeSeq.MIN_LEAF) {
      // steal from the left neighbor
      final Item[] lvals = leftLeaf.values;
      final int l = lvals.length, diff = l - TreeSeq.MIN_LEAF, move = (diff + 1) / 2;
      final int ll = l - move, rl = n - 1 + move;
      final Item[] newLeft = new Item[ll], newRight = new Item[rl];

      Array.copy(lvals, ll, newLeft);
      Array.copyToStart(lvals, ll, move, newRight);
      Array.copyFromStart(values, p, newRight, move);
      Array.copy(values, p + 1, n - 1 - p, newRight, move + p);
      out[0] = new LeafNode(newLeft);
      out[1] = new LeafNode(newRight);
      return out;
    }

    final LeafNode rightLeaf = (LeafNode) right;
    if(rightLeaf != null && rightLeaf.arity() > TreeSeq.MIN_LEAF) {
      // steal from the right neighbor
      final Item[] rvals = rightLeaf.values;
      final int r = rvals.length, diff = r - TreeSeq.MIN_LEAF, move = (diff + 1) / 2;
      final int ll = n - 1 + move, rl = r - move;
      final Item[] newLeft = new Item[ll], newRight = new Item[rl];

      Array.copy(values, p, newLeft);
      Array.copy(values, p + 1, n - 1 - p, newLeft, p);
      Array.copyFromStart(rvals, move, newLeft, n - 1);
      Array.copyToStart(rvals, move, rl, newRight);
      out[1] = new LeafNode(newLeft);
      out[2] = new LeafNode(newRight);
      return out;
    }

    if(left != null) {
      // merge with left neighbor
      final Item[] lvals = ((LeafNode) left).values;
      final int l = lvals.length, r = values.length;
      final Item[] vals = new Item[l + r - 1];
      Array.copy(lvals, l, vals);
      Array.copyFromStart(values, p, vals, l);
      Array.copy(values, p + 1, r - 1 - p, vals, l + p);
      out[0] = new LeafNode(vals);
      return out;
    }

    if(right != null) {
      // merge with right neighbor
      final Item[] rvals = ((LeafNode) right).values;
      final int l = values.length, r = rvals.length;
      final Item[] vals = new Item[l - 1 + r];
      Array.copy(values, p, vals);
      Array.copy(values, p + 1, l - 1 - p, vals, p);
      Array.copyFromStart(rvals, r, vals, l - 1);
      out[2] = new LeafNode(vals);
      return out;
    }

    // underflow
    final Item[] vals = new Item[n - 1];
    Array.copy(values, p, vals);
    Array.copy(values, p + 1, n - 1 - p, vals, p);
    out[1] = new PartialLeafNode(vals);
    return out;
  }

  @Override
  public int append(final NodeLike<Item, Item>[] nodes, final int pos) {
    if(pos == 0) {
      nodes[0] = this;
      return 1;
    }

    final NodeLike<Item, Item> left = nodes[pos - 1];
    if(!(left instanceof PartialLeafNode)) {
      nodes[pos] = this;
      return pos + 1;
    }

    final Item[] ls = ((PartialLeafNode) left).elems, rs = values;
    final int l = ls.length, r = rs.length, n = l + r;
    if(n <= TreeSeq.MAX_LEAF) {
      // merge into one node
      final Item[] vals = new Item[n];
      Array.copy(ls, l, vals);
      Array.copyFromStart(rs, r, vals, l);
      nodes[pos - 1] = new LeafNode(vals);
      return pos;
    }

    // split into two
    final int ll = n / 2, rl = n - ll, move = r - rl;
    final Item[] newLeft = new Item[ll], newRight = new Item[rl];
    Array.copy(ls, l, newLeft);
    Array.copyFromStart(rs, move, newLeft, l);
    Array.copyToStart(rs, move, rl, newRight);
    nodes[pos - 1] = new LeafNode(newLeft);
    nodes[pos] = new LeafNode(newRight);
    return pos + 1;
  }

  @Override
  public NodeLike<Item, Item> slice(final long off, final long size) {
    final int p = (int) off, n = (int) size;
    final Item[] out = new Item[n];
    Array.copyToStart(values, p, n, out);
    return n < TreeSeq.MIN_LEAF ? new PartialLeafNode(out) : new LeafNode(out);
  }

  @Override
  public long checkInvariants() {
    if(values.length < TreeSeq.MIN_LEAF || values.length > TreeSeq.MAX_LEAF)
      throw new AssertionError("Wrong " + Util.className(this) + " size: " + values.length);
    return values.length;
  }

  @Override
  public int arity() {
    return values.length;
  }

  @Override
  public Item getSub(final int index) {
    return values[index];
  }

  @Override
  public String toString() {
    return Util.className(this) + '(' + size() + ')' + Arrays.toString(values);
  }
}
