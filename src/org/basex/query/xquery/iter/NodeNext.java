package org.basex.query.xquery.iter;

import org.basex.query.xquery.item.Node;

/**
 * Simple iterator.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class NodeNext extends NodeIter {
  /** Nodes to iterate. */
  private NodIter nodes;
  /** Child counter. */
  private int c = -1;
  
  /**
   * Constructor.
   * @param n nodes
   */
  public NodeNext(final NodIter n) {
    nodes = n;
  }

  @Override
  public Node next() {
    return ++c < nodes.size ? nodes.list[c] : null;
  }
}
