package org.basex.api.dom;

import org.basex.data.Data;
import org.basex.query.xpath.values.NodeSet;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * DOM - NodeList Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class NodeListImpl implements NodeList {
  /** Data reference. */
  final Data data;
  /** Node array. */
  final int[] nodes;
  /** Size. */
  final int size;
  
  /**
   * Constructor.
   * @param it result
   */
  public NodeListImpl(final NodeSet it) {
    this(it.data, it.nodes, it.size());
  }
  
  /**
   * Constructor.
   * @param d data reference
   * @param n node array
   * @param s size
   */
  public NodeListImpl(final Data d, final int[] n, final int s) {
    data = d;
    nodes = n;
    size = s;
  }
  
  public Node item(final int i) {
    if(i < size) return NodeImpl.get(data, nodes[i]);
    throw new IndexOutOfBoundsException("Out of Range: " + i + " >= " + size);
  }

  public int getLength() {
    return size;
  }
}
