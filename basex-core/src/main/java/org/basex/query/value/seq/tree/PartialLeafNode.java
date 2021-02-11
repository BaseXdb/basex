package org.basex.query.value.seq.tree;

import java.util.*;

import org.basex.query.util.fingertree.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * A partial leaf node containing fewer elements than required in a node.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
final class PartialLeafNode implements NodeLike<Item, Item> {
  /** The single element. */
  final Item[] elems;

  /**
   * Constructor.
   * @param elems the elements
   */
  PartialLeafNode(final Item[] elems) {
    this.elems = elems;
  }

  @Override
  public int append(final NodeLike<Item, Item>[] nodes, final int pos) {
    if(pos == 0) {
      nodes[0] = this;
      return 1;
    }

    final NodeLike<Item, Item> left = nodes[pos - 1];
    if(left instanceof PartialLeafNode) {
      final Item[] ls = ((PartialLeafNode) left).elems, rs = elems;
      final int l = ls.length, r = rs.length, n = l + r;
      final Item[] vals = new Item[n];
      Array.copy(ls, l, vals);
      Array.copyFromStart(rs, r, vals, l);
      nodes[pos - 1] = n < TreeSeq.MIN_LEAF ? new PartialLeafNode(vals) : new LeafNode(vals);
      return pos;
    }

    final Item[] ls = ((LeafNode) left).values, rs = elems;
    final int l = ls.length, r = rs.length, n = l + r;
    if(n <= TreeSeq.MAX_LEAF) {
      final Item[] vals = new Item[n];
      Array.copy(ls, l, vals);
      Array.copyFromStart(rs, r, vals, l);
      nodes[pos - 1] = new LeafNode(vals);
      return pos;
    }

    final int ll = n / 2, rl = n - ll, move = l - ll;
    final Item[] newLeft = new Item[ll], newRight = new Item[rl];
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
