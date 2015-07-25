package org.basex.query.value.array;

import java.util.*;

import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;

/**
 * A partial leaf node containing fewer elements than required in a node.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
final class PartialLeafNode implements NodeLike<Value, Value> {
  /** The single element. */
  final Value[] elems;

  /**
   * Constructor.
   * @param elems the elements
   */
  PartialLeafNode(final Value[] elems) {
    this.elems = elems;
  }

  @Override
  public int append(final NodeLike<Value, Value>[] nodes, final int pos) {
    if(pos == 0) {
      nodes[0] = this;
      return 1;
    }

    final NodeLike<Value, Value> left = nodes[pos - 1];
    if(left instanceof PartialLeafNode) {
      final Value[] ls = ((PartialLeafNode) left).elems, rs = elems;
      final int l = ls.length, r = rs.length, n = l + r;
      final Value[] vals = new Value[n];
      System.arraycopy(ls, 0, vals, 0, l);
      System.arraycopy(rs, 0, vals, l, r);
      nodes[pos - 1] = n < Array.MIN_LEAF ? new PartialLeafNode(vals) : new LeafNode(vals);
      return pos;
    }

    final Value[] ls = ((LeafNode) left).values, rs = elems;
    final int l = ls.length, r = rs.length, n = l + r;
    if(n <= Array.MAX_LEAF) {
      final Value[] vals = new Value[n];
      System.arraycopy(ls, 0, vals, 0, l);
      System.arraycopy(rs, 0, vals, l, r);
      nodes[pos - 1] = new LeafNode(vals);
      return pos;
    }

    final int ll = n / 2, rl = n - ll, move = l - ll;
    final Value[] newLeft = new Value[ll], newRight = new Value[rl];
    System.arraycopy(ls, 0, newLeft, 0, ll);
    System.arraycopy(ls, ll, newRight, 0, move);
    System.arraycopy(rs, 0, newRight, move, r);
    nodes[pos - 1] = new LeafNode(newLeft);
    nodes[pos] = new LeafNode(newRight);
    return pos + 1;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + elems.length + ")" + Arrays.toString(elems);
  }
}
