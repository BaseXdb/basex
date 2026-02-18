package org.basex.query.util.list;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for nodes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class GNodeList extends ObjectList<XNode, GNodeList> {
  /**
   * Constructor.
   */
  public GNodeList() {
    this(1);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity
   */
  public GNodeList(final long capacity) {
    super(new XNode[Array.initialCapacity(capacity)]);
  }

  /**
   * Returns an iterator over the items in this list.
   * The list must not be modified after the iterator has been requested.
   * @return the iterator
   */
  public BasicNodeIter iter() {
    return size == 0 ? BasicNodeIter.EMPTY : new BasicNodeIter() {
      int pos;

      @Override
      public long size() {
        return size;
      }
      @Override
      public XNode next() {
        return pos < size ? list[pos++] : null;
      }
      @Override
      public XNode get(final long i) {
        return list[(int) i];
      }
      @Override
      public Value value(final QueryContext qc, final Expr expr) {
        return ItemSeq.get(list, size, NodeType.NODE.refine(expr));
      }
    };
  }

  /**
   * Invalidates all entries that are not referenced in the list.
   * @return the iterator
   */
  public GNodeList clean() {
    Arrays.fill(list, size, list.length, null);
    return this;
  }

  @Override
  public boolean equals(final XNode node1, final XNode node2) {
    return node1.is(node2);
  }

  @Override
  protected XNode[] newArray(final int s) {
    return new XNode[s];
  }

  @Override
  public boolean equals(final Object obj) {
    return obj == this || obj instanceof GNodeList && super.equals(obj);
  }

  /**
   * Returns a node iterator.
   * @param nodes nodes
   * @return the iterator
   */
  public static BasicNodeIter iter(final XNode[] nodes) {
    final int nl = nodes.length;
    return nl == 0 ? BasicNodeIter.EMPTY : new BasicNodeIter() {
      int pos;

      @Override
      public long size() {
        return nl;
      }
      @Override
      public XNode next() {
        return pos < nl ? nodes[pos++] : null;
      }
      @Override
      public XNode get(final long i) {
        return nodes[(int) i];
      }
      @Override
      public Value value(final QueryContext qc, final Expr expr) {
        return ItemSeq.get(nodes, nl, NodeType.NODE.refine(expr));
      }
    };
  }
}
