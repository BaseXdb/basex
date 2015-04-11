package org.basex.query.value.seq.tree;

import java.util.*;

import org.basex.query.util.fingertree.*;
import org.basex.query.value.item.*;

/**
 * A leaf node containing {@link Item}s.
 *
 * @author BaseX Team 2005-15, BSD License
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
    System.arraycopy(values, 0, vals, 0, p);
    vals[p] = val;
    System.arraycopy(values, p, vals, p + 1, n - p);

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
      System.arraycopy(lvals, 0, newLeft, 0, l);
      System.arraycopy(vals, 0, newLeft, l, move);
      System.arraycopy(vals, move, newRight, 0, newRight.length);
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
      System.arraycopy(vals, 0, newLeft, 0, l);
      System.arraycopy(vals, l, newRight, 0, move);
      System.arraycopy(rvals, 0, newRight, move, r);
      siblings[1] = new LeafNode(newLeft);
      siblings[2] = new LeafNode(newRight);
      return false;
    }

    // split the node
    final int l = vals.length / 2, r = vals.length - l;
    final Item[] newLeft = new Item[l], newRight = new Item[r];
    System.arraycopy(vals, 0, newLeft, 0, l);
    System.arraycopy(vals, l, newRight, 0, r);
    siblings[3] = siblings[2];
    siblings[1] = new LeafNode(newLeft);
    siblings[2] = new LeafNode(newRight);
    return true;
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
      System.arraycopy(values, 0, vals, 0, p);
      System.arraycopy(values, p + 1, vals, p, n - 1 - p);
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

      System.arraycopy(lvals, 0, newLeft, 0, ll);
      System.arraycopy(lvals, ll, newRight, 0, move);
      System.arraycopy(values, 0, newRight, move, p);
      System.arraycopy(values, p + 1, newRight, move + p, n - 1 - p);
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

      System.arraycopy(values, 0, newLeft, 0, p);
      System.arraycopy(values, p + 1, newLeft, p, n - 1 - p);
      System.arraycopy(rvals, 0, newLeft, n - 1, move);
      System.arraycopy(rvals, move, newRight, 0, rl);
      out[1] = new LeafNode(newLeft);
      out[2] = new LeafNode(newRight);
      return out;
    }

    if(left != null) {
      // merge with left neighbor
      final Item[] lvals = ((LeafNode) left).values;
      final int l = lvals.length, r = values.length;
      final Item[] vals = new Item[l + r - 1];
      System.arraycopy(lvals, 0, vals, 0, l);
      System.arraycopy(values, 0, vals, l, p);
      System.arraycopy(values, p + 1, vals, l + p, r - 1 - p);
      out[0] = new LeafNode(vals);
      out[1] = null;
      return out;
    }

    if(right != null) {
      // merge with right neighbor
      final Item[] rvals = ((LeafNode) right).values;
      final int l = values.length, r = rvals.length;
      final Item[] vals = new Item[l - 1 + r];
      System.arraycopy(values, 0, vals, 0, p);
      System.arraycopy(values, p + 1, vals, p, l - 1 - p);
      System.arraycopy(rvals, 0, vals, l - 1, r);
      out[1] = null;
      out[2] = new LeafNode(vals);
      return out;
    }

    // underflow
    final Item[] vals = new Item[n - 1];
    System.arraycopy(values, 0, vals, 0, p);
    System.arraycopy(values, p + 1, vals, p, n - 1 - p);
    out[1] = new PartialLeafNode(vals);
    return out;
  }

  @Override
  public int append(final NodeLike<Item, Item>[] nodes, final int pos) {
    if(pos == 0) {
      nodes[pos] = this;
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
      System.arraycopy(ls, 0, vals, 0, l);
      System.arraycopy(rs, 0, vals, l, r);
      nodes[pos - 1] = new LeafNode(vals);
      return pos;
    }

    // split into two
    final int ll = n / 2, rl = n - ll, move = r - rl;
    final Item[] newLeft = new Item[ll], newRight = new Item[rl];
    System.arraycopy(ls, 0, newLeft, 0, l);
    System.arraycopy(rs, 0, newLeft, l, move);
    System.arraycopy(rs, move, newRight, 0, rl);
    nodes[pos - 1] = new LeafNode(newLeft);
    nodes[pos] = new LeafNode(newRight);
    return pos + 1;
  }

  @Override
  public NodeLike<Item, Item> slice(final long off, final long size) {
    final int p = (int) off, n = (int) size;
    final Item[] out = new Item[n];
    System.arraycopy(values, p, out, 0, n);
    return n < TreeSeq.MIN_LEAF ? new PartialLeafNode(out) : new LeafNode(out);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + size() + ")" + Arrays.toString(values);
  }

  @Override
  public long checkInvariants() {
    if(values.length < TreeSeq.MIN_LEAF || values.length > TreeSeq.MAX_LEAF)
      throw new AssertionError("Wrong " + getClass().getSimpleName() + " size: " + values.length);
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
}
