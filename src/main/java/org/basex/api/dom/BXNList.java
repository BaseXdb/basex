package org.basex.api.dom;

import org.basex.data.Nodes;
import org.basex.query.item.DBNode;
import org.basex.query.item.ANode;
import org.basex.query.iter.NodeCache;
import org.w3c.dom.NodeList;

/**
 * DOM - Node list implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class BXNList implements NodeList {
  /** XQuery node set. */
  NodeCache xquery;
  /** XQuery node set. */
  Nodes nodes;

  /**
   * Constructor.
   * @param nb nodes
   */
  BXNList(final NodeCache nb) {
    xquery = nb;
    xquery.sort();
  }

  /**
   * Constructor.
   * @param n nodes
   */
  public BXNList(final Nodes n) {
    nodes = n;
  }

  @Override
  public BXNode item(final int i) {
    ANode n = null;
    if(xquery != null) {
      if(i < xquery.size()) n = xquery.get(i);
    } else {
      if(i < nodes.size()) n = new DBNode(nodes.data, nodes.list[i]);
    }
    return n != null ? n.toJava() : null;
  }

  @Override
  public int getLength() {
    return (int) (xquery != null ? xquery.size() : nodes.size());
  }
}
