package org.basex.api.dom;

import org.basex.query.xpath.item.Nod;
import org.basex.query.xquery.item.DBNode;
import org.basex.query.xquery.util.NodeBuilder;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * DOM - NodeList Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class BXNList implements NodeList {
  /** XQuery node set. */
  protected NodeBuilder xquery;
  /** XPath node set. */
  protected Nod xpath;
  
  /**
   * Constructor.
   * @param nb nodes
   */
  public BXNList(final NodeBuilder nb) {
    xquery = nb;
  }
  
  /**
   * Constructor.
   * @param ns node set
   */
  public BXNList(final Nod ns) {
    xpath = ns;
  }
  
  public final Node item(final int i) {
    org.basex.query.xquery.item.Nod n = null;
    if(xquery != null) {
      if(i < xquery.size) n = xquery.list[i];
    } else {
      if(i < xpath.size) n = new DBNode(xpath.data, xpath.nodes[i]);
    }
    return n != null ? n.java() : null;
  }

  public final int getLength() {
    return xquery != null ? xquery.size : xpath.size;
  }
}
