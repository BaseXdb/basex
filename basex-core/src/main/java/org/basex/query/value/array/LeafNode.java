package org.basex.query.value.array;

import java.util.*;

import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;

/**
 * A leaf node containing {@link Value}s.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
final class LeafNode implements Node<Value, Value> {
  /** Elements stored in this leaf node. */
  final Value[] values;

  /**
   * Constructor.
   * @param values the values
   */
  LeafNode(final Value[] values) {
    this.values = values;
    assert values.length >= Array.MIN_LEAF && values.length <= Array.MAX_LEAF;
  }

  @Override
  public long size() {
    return values.length;
  }

  @Override
  public LeafNode reverse() {
    final int n = values.length;
    final Value[] out = new Value[n];
    for(int i = 0; i < n; i++) out[i] = values[n - 1 - i];
    return new LeafNode(out);
  }

  @Override
  public boolean insert(final Node<Value, Value>[] siblings,
      final long pos, final Value val) {
    final int p = (int) pos, n = values.length;
    final Value[] vals = new Value[n + 1];
    System.arraycopy(values, 0, vals, 0, p);
    vals[p] = val;
    System.arraycopy(values, p, vals, p + 1, n - p);

    if(n < Array.MAX_LEAF) {
      // there is capacity
      siblings[1] = new LeafNode(vals);
      return false;
    }

    final LeafNode left = (LeafNode) siblings[0];
    if(left != null && left.values.length < Array.MAX_LEAF) {
      // push elements to the left sibling
      final Value[] lvals = left.values;
      final int l = lvals.length, diff = Array.MAX_LEAF - l, move = (diff + 1) / 2;
      final Value[] newLeft = new Value[l + move], newRight = new Value[n + 1 - move];
      System.arraycopy(lvals, 0, newLeft, 0, l);
      System.arraycopy(vals, 0, newLeft, l, move);
      System.arraycopy(vals, move, newRight, 0, newRight.length);
      siblings[0] = new LeafNode(newLeft);
      siblings[1] = new LeafNode(newRight);
      return false;
    }

    final LeafNode right = (LeafNode) siblings[2];
    if(right != null && right.values.length < Array.MAX_LEAF) {
      // push elements to the right sibling
      final Value[] rvals = right.values;
      final int r = rvals.length, diff = Array.MAX_LEAF - r, move = (diff + 1) / 2,
          l = n + 1 - move;
      final Value[] newLeft = new Value[l], newRight = new Value[r + move];
      System.arraycopy(vals, 0, newLeft, 0, l);
      System.arraycopy(vals, l, newRight, 0, move);
      System.arraycopy(rvals, 0, newRight, move, r);
      siblings[1] = new LeafNode(newLeft);
      siblings[2] = new LeafNode(newRight);
      return false;
    }

    // split the node
    final int l = vals.length / 2, r = vals.length - l;
    final Value[] newLeft = new Value[l], newRight = new Value[r];
    System.arraycopy(vals, 0, newLeft, 0, l);
    System.arraycopy(vals, l, newRight, 0, r);
    siblings[3] = siblings[2];
    siblings[1] = new LeafNode(newLeft);
    siblings[2] = new LeafNode(newRight);
    return true;
  }

  @Override
  public NodeLike<Value, Value>[] remove(final Node<Value, Value> left,
      final Node<Value, Value> right, final long pos) {
    final int p = (int) pos, n = values.length;
    @SuppressWarnings("unchecked")
    final NodeLike<Value, Value>[] out = new NodeLike[] { left, null, right };
    if(n > Array.MIN_LEAF) {
      // we do not have to split
      final Value[] vals = new Value[n - 1];
      System.arraycopy(values, 0, vals, 0, p);
      System.arraycopy(values, p + 1, vals, p, n - 1 - p);
      out[1] = new LeafNode(vals);
      return out;
    }

    final LeafNode leftLeaf = (LeafNode) left;
    if(leftLeaf != null && leftLeaf.arity() > Array.MIN_LEAF) {
      // steal from the left neighbor
      final Value[] lvals = leftLeaf.values;
      final int l = lvals.length, diff = l - Array.MIN_LEAF, move = (diff + 1) / 2;
      final int ll = l - move, rl = n - 1 + move;
      final Value[] newLeft = new Value[ll], newRight = new Value[rl];

      System.arraycopy(lvals, 0, newLeft, 0, ll);
      System.arraycopy(lvals, ll, newRight, 0, move);
      System.arraycopy(values, 0, newRight, move, p);
      System.arraycopy(values, p + 1, newRight, move + p, n - 1 - p);
      out[0] = new LeafNode(newLeft);
      out[1] = new LeafNode(newRight);
      return out;
    }

    final LeafNode rightLeaf = (LeafNode) right;
    if(rightLeaf != null && rightLeaf.arity() > Array.MIN_LEAF) {
      // steal from the right neighbor
      final Value[] rvals = rightLeaf.values;
      final int r = rvals.length, diff = r - Array.MIN_LEAF, move = (diff + 1) / 2;
      final int ll = n - 1 + move, rl = r - move;
      final Value[] newLeft = new Value[ll], newRight = new Value[rl];

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
      final Value[] lvals = ((LeafNode) left).values;
      final int l = lvals.length, r = values.length;
      final Value[] vals = new Value[l + r - 1];
      System.arraycopy(lvals, 0, vals, 0, l);
      System.arraycopy(values, 0, vals, l, p);
      System.arraycopy(values, p + 1, vals, l + p, r - 1 - p);
      out[0] = new LeafNode(vals);
      out[1] = null;
      return out;
    }

    if(right != null) {
      // merge with right neighbor
      final Value[] rvals = ((LeafNode) right).values;
      final int l = values.length, r = rvals.length;
      final Value[] vals = new Value[l - 1 + r];
      System.arraycopy(values, 0, vals, 0, p);
      System.arraycopy(values, p + 1, vals, p, l - 1 - p);
      System.arraycopy(rvals, 0, vals, l - 1, r);
      out[1] = null;
      out[2] = new LeafNode(vals);
      return out;
    }

    // underflow
    final Value[] vals = new Value[n - 1];
    System.arraycopy(values, 0, vals, 0, p);
    System.arraycopy(values, p + 1, vals, p, n - 1 - p);
    out[1] = new PartialLeafNode(vals);
    return out;
  }

  @Override
  public int append(final NodeLike<Value, Value>[] nodes, final int pos) {
    if(pos == 0) {
      nodes[pos] = this;
      return 1;
    }

    final NodeLike<Value, Value> left = nodes[pos - 1];
    if(!(left instanceof PartialLeafNode)) {
      nodes[pos] = this;
      return pos + 1;
    }

    final Value[] ls = ((PartialLeafNode) left).elems, rs = values;
    final int l = ls.length, r = rs.length, n = l + r;
    if(n <= Array.MAX_LEAF) {
      // merge into one node
      final Value[] vals = new Value[n];
      System.arraycopy(ls, 0, vals, 0, l);
      System.arraycopy(rs, 0, vals, l, r);
      nodes[pos - 1] = new LeafNode(vals);
      return pos;
    }

    // split into two
    final int ll = n / 2, rl = n - ll, move = r - rl;
    final Value[] newLeft = new Value[ll], newRight = new Value[rl];
    System.arraycopy(ls, 0, newLeft, 0, l);
    System.arraycopy(rs, 0, newLeft, l, move);
    System.arraycopy(rs, move, newRight, 0, rl);
    nodes[pos - 1] = new LeafNode(newLeft);
    nodes[pos] = new LeafNode(newRight);
    return pos + 1;
  }

  @Override
  public NodeLike<Value, Value> slice(final long off, final long size) {
    final int p = (int) off, n = (int) size;
    final Value[] out = new Value[n];
    System.arraycopy(values, p, out, 0, n);
    return n < Array.MIN_LEAF ? new PartialLeafNode(out) : new LeafNode(out);
  }

  @Override
  public long checkInvariants() {
    if(values.length < Array.MIN_LEAF || values.length > Array.MAX_LEAF)
      throw new AssertionError("Wrong " + getClass().getSimpleName() + " size: " + values.length);
    return values.length;
  }

  @Override
  public int arity() {
    return values.length;
  }

  @Override
  public Value getSub(final int index) {
    return values[index];
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + size() + ")" + Arrays.toString(values);
  }
}
