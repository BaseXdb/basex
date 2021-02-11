package org.basex.api.dom;

import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.w3c.dom.*;

/**
 * DOM - Node list implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
class BXNList implements NodeList {
  /** XQuery node set. */
  final ANodeList nodes;

  /**
   * Constructor.
   * @param nodes nodes
   */
  BXNList(final ANodeList nodes) {
    this.nodes = nodes;
  }

  @Override
  public BXNode item(final int index) {
    ANode n = null;
    if(index < nodes.size()) n = nodes.get(index);
    return n != null ? BXNode.get(n) : null;
  }

  @Override
  public int getLength() {
    return nodes.size();
  }
}
