package org.basex.query.value.seq.tree;

import java.util.*;

import org.basex.query.util.fingertree.*;
import org.basex.query.value.item.*;

/**
 * A partial leaf node containing fewer elements than required in a node.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
final class PartialLeafNode extends PartialNode<Item, Item> {
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
  protected NodeLike<Item, Item>[] concat(final NodeLike<Item, Item> other) {
    @SuppressWarnings("unchecked")
    final NodeLike<Item, Item>[] out = new NodeLike[2];
    if(other instanceof LeafNode) {
      final Item[] ls = elems, rs = ((LeafNode) other).values;
      final int l = ls.length, r = rs.length, n = l + r;
      if(n <= TreeSeq.MAX_LEAF) {
        // merge into one node
        final Item[] vals = new Item[n];
        System.arraycopy(ls, 0, vals, 0, l);
        System.arraycopy(rs, 0, vals, l, r);
        out[0] = new LeafNode(vals);
      } else {
        // split into two
        final int ll = n / 2, rl = n - ll, move = r - rl;
        final Item[] newLeft = new Item[ll], newRight = new Item[rl];
        System.arraycopy(ls, 0, newLeft, 0, l);
        System.arraycopy(rs, 0, newLeft, l, move);
        System.arraycopy(rs, move, newRight, 0, rl);
        out[0] = new LeafNode(newLeft);
        out[1] = new LeafNode(newRight);
      }
    } else {
      final Item[] elems2 = ((PartialLeafNode) other).elems;
      final int l = elems.length, r = elems2.length, n = l + r;
      final Item[] vals = new Item[n];
      System.arraycopy(elems, 0, vals, 0, l);
      System.arraycopy(elems2, 0, vals, l, r);
      out[0] = n < TreeSeq.MIN_LEAF ? new PartialLeafNode(vals) : new LeafNode(vals);
    }
    return out;
  }

  @Override
  protected int append(final NodeLike<Item, Item>[] nodes, final int pos) {
    if(pos == 0) {
      nodes[0] = this;
      return 1;
    }

    final NodeLike<Item, Item> left = nodes[pos - 1];
    if(left instanceof PartialLeafNode) {
      final Item[] ls = ((PartialLeafNode) left).elems, rs = elems;
      final int l = ls.length, r = rs.length, n = l + r;
      final Item[] vals = new Item[n];
      System.arraycopy(ls, 0, vals, 0, l);
      System.arraycopy(rs, 0, vals, l, r);
      nodes[pos - 1] = n < TreeSeq.MIN_LEAF ? new PartialLeafNode(vals) : new LeafNode(vals);
      return pos;
    }

    final Item[] ls = ((LeafNode) left).values, rs = elems;
    final int l = ls.length, r = rs.length, n = l + r;
    if(n <= TreeSeq.MAX_LEAF) {
      final Item[] vals = new Item[n];
      System.arraycopy(ls, 0, vals, 0, l);
      System.arraycopy(rs, 0, vals, l, r);
      nodes[pos - 1] = new LeafNode(vals);
      return pos;
    }

    final int ll = n / 2, rl = n - ll, move = l - ll;
    final Item[] newLeft = new Item[ll], newRight = new Item[rl];
    System.arraycopy(ls, 0, newLeft, 0, ll);
    System.arraycopy(ls, ll, newRight, 0, move);
    System.arraycopy(rs, 0, newRight, move, r);
    nodes[pos - 1] = new LeafNode(newLeft);
    nodes[pos] = new LeafNode(newRight);
    return pos + 1;
  }

  @Override
  protected void toString(final StringBuilder sb, final int indent) {
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append(getClass().getSimpleName()).append(Arrays.toString(elems));
  }
}
