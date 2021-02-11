package org.basex.query.value.array;

import java.util.*;

import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * A leaf node containing {@link Value}s.
 *
 * @author BaseX Team 2005-21, BSD License
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
    assert values.length >= XQArray.MIN_LEAF && values.length <= XQArray.MAX_LEAF;
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
  public LeafNode set(final long pos, final Value value) {
    final Value[] vals = values.clone();
    vals[(int) pos] = value;
    return new LeafNode(vals);
  }

  @Override
  public boolean insert(final Node<Value, Value>[] siblings, final long pos, final Value value) {
    final int p = (int) pos, n = values.length;
    final Value[] vals = new Value[n + 1];
    Array.copy(values, p, vals);
    vals[p] = value;
    Array.copy(values, p, n - p, vals, p + 1);

    if(n < XQArray.MAX_LEAF) {
      // there is capacity
      siblings[1] = new LeafNode(vals);
      return false;
    }

    final LeafNode left = (LeafNode) siblings[0];
    if(left != null && left.values.length < XQArray.MAX_LEAF) {
      // push elements to the left sibling
      final Value[] lvals = left.values;
      final int l = lvals.length, diff = XQArray.MAX_LEAF - l, move = (diff + 1) / 2;
      final Value[] newLeft = new Value[l + move], newRight = new Value[n + 1 - move];
      Array.copy(lvals, l, newLeft);
      Array.copyFromStart(vals, move, newLeft, l);
      Array.copyToStart(vals, move, newRight.length, newRight);
      siblings[0] = new LeafNode(newLeft);
      siblings[1] = new LeafNode(newRight);
      return false;
    }

    final LeafNode right = (LeafNode) siblings[2];
    if(right != null && right.values.length < XQArray.MAX_LEAF) {
      // push elements to the right sibling
      final Value[] rvals = right.values;
      final int r = rvals.length, diff = XQArray.MAX_LEAF - r, move = (diff + 1) / 2,
          l = n + 1 - move;
      final Value[] newLeft = new Value[l], newRight = new Value[r + move];
      Array.copy(vals, l, newLeft);
      Array.copyToStart(vals, l, move, newRight);
      Array.copyFromStart(rvals, r, newRight, move);
      siblings[1] = new LeafNode(newLeft);
      siblings[2] = new LeafNode(newRight);
      return false;
    }

    // split the node
    final int l = vals.length / 2, r = vals.length - l;
    final Value[] newLeft = new Value[l], newRight = new Value[r];
    Array.copy(vals, l, newLeft);
    Array.copyToStart(vals, l, r, newRight);
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
    if(n > XQArray.MIN_LEAF) {
      // we do not have to split
      final Value[] vals = new Value[n - 1];
      Array.copy(values, p, vals);
      Array.copy(values, p + 1, n - 1 - p, vals, p);
      out[1] = new LeafNode(vals);
      return out;
    }

    final LeafNode leftLeaf = (LeafNode) left;
    if(leftLeaf != null && leftLeaf.arity() > XQArray.MIN_LEAF) {
      // steal from the left neighbor
      final Value[] lvals = leftLeaf.values;
      final int l = lvals.length, diff = l - XQArray.MIN_LEAF, move = (diff + 1) / 2;
      final int ll = l - move, rl = n - 1 + move;
      final Value[] newLeft = new Value[ll], newRight = new Value[rl];

      Array.copy(lvals, ll, newLeft);
      Array.copyToStart(lvals, ll, move, newRight);
      Array.copyFromStart(values, p, newRight, move);
      Array.copy(values, p + 1, n - 1 - p, newRight, move + p);
      out[0] = new LeafNode(newLeft);
      out[1] = new LeafNode(newRight);
      return out;
    }

    final LeafNode rightLeaf = (LeafNode) right;
    if(rightLeaf != null && rightLeaf.arity() > XQArray.MIN_LEAF) {
      // steal from the right neighbor
      final Value[] rvals = rightLeaf.values;
      final int r = rvals.length, diff = r - XQArray.MIN_LEAF, move = (diff + 1) / 2;
      final int ll = n - 1 + move, rl = r - move;
      final Value[] newLeft = new Value[ll], newRight = new Value[rl];

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
      final Value[] lvals = ((LeafNode) left).values;
      final int l = lvals.length, r = values.length;
      final Value[] vals = new Value[l + r - 1];
      Array.copy(lvals, l, vals);
      Array.copyFromStart(values, p, vals, l);
      Array.copy(values, p + 1, r - 1 - p, vals, l + p);
      out[0] = new LeafNode(vals);
      return out;
    }

    if(right != null) {
      // merge with right neighbor
      final Value[] rvals = ((LeafNode) right).values;
      final int l = values.length, r = rvals.length;
      final Value[] vals = new Value[l - 1 + r];
      Array.copy(values, p, vals);
      Array.copy(values, p + 1, l - 1 - p, vals, p);
      Array.copyFromStart(rvals, r, vals, l - 1);
      out[2] = new LeafNode(vals);
      return out;
    }

    // underflow
    final Value[] vals = new Value[n - 1];
    Array.copy(values, p, vals);
    Array.copy(values, p + 1, n - 1 - p, vals, p);
    out[1] = new PartialLeafNode(vals);
    return out;
  }

  @Override
  public int append(final NodeLike<Value, Value>[] nodes, final int pos) {
    if(pos == 0) {
      nodes[0] = this;
      return 1;
    }

    final NodeLike<Value, Value> left = nodes[pos - 1];
    if(!(left instanceof PartialLeafNode)) {
      nodes[pos] = this;
      return pos + 1;
    }

    final Value[] ls = ((PartialLeafNode) left).elems, rs = values;
    final int l = ls.length, r = rs.length, n = l + r;
    if(n <= XQArray.MAX_LEAF) {
      // merge into one node
      final Value[] vals = new Value[n];
      Array.copy(ls, l, vals);
      Array.copyFromStart(rs, r, vals, l);
      nodes[pos - 1] = new LeafNode(vals);
      return pos;
    }

    // split into two
    final int ll = n / 2, rl = n - ll, move = r - rl;
    final Value[] newLeft = new Value[ll], newRight = new Value[rl];
    Array.copy(ls, l, newLeft);
    Array.copyFromStart(rs, move, newLeft, l);
    Array.copyToStart(rs, move, rl, newRight);
    nodes[pos - 1] = new LeafNode(newLeft);
    nodes[pos] = new LeafNode(newRight);
    return pos + 1;
  }

  @Override
  public NodeLike<Value, Value> slice(final long off, final long size) {
    final int p = (int) off, n = (int) size;
    final Value[] out = new Value[n];
    Array.copyToStart(values, p, n, out);
    return n < XQArray.MIN_LEAF ? new PartialLeafNode(out) : new LeafNode(out);
  }

  @Override
  public long checkInvariants() {
    if(values.length < XQArray.MIN_LEAF || values.length > XQArray.MAX_LEAF)
      throw new AssertionError("Wrong " + Util.className(this) + " size: " + values.length);
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
    return Util.className(this) + '(' + size() + ')' + Arrays.toString(values);
  }
}
