package org.basex.query.value.array;

import java.util.*;

import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * A partial leaf node containing fewer elements than required in a node.
 *
 * @author BaseX Team 2005-21, BSD License
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
      Array.copy(ls, l, vals);
      Array.copyFromStart(rs, r, vals, l);
      nodes[pos - 1] = n < XQArray.MIN_LEAF ? new PartialLeafNode(vals) : new LeafNode(vals);
      return pos;
    }

    final Value[] ls = ((LeafNode) left).values, rs = elems;
    final int l = ls.length, r = rs.length, n = l + r;
    if(n <= XQArray.MAX_LEAF) {
      final Value[] vals = new Value[n];
      Array.copy(ls, l, vals);
      Array.copyFromStart(rs, r, vals, l);
      nodes[pos - 1] = new LeafNode(vals);
      return pos;
    }

    final int ll = n / 2, rl = n - ll, move = l - ll;
    final Value[] newLeft = new Value[ll], newRight = new Value[rl];
    Array.copy(ls, ll, newLeft);
    Array.copyToStart(ls, ll, move, newRight);
    Array.copyFromStart(rs, r, newRight, move);
    nodes[pos - 1] = new LeafNode(newLeft);
    nodes[pos] = new LeafNode(newRight);
    return pos + 1;
  }

  @Override
  public String toString() {
    return Util.className(this) + '(' + elems.length + ')' + Arrays.toString(elems);
  }
}
