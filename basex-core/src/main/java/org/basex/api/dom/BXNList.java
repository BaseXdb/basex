package org.basex.api.dom;

import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.w3c.dom.*;

/**
 * DOM - Node list implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
class BXNList implements NodeList {
  /** XQuery node set. */
  final ANodeList nl;

  /**
   * Constructor.
   * @param nodes nodes
   */
  BXNList(final ANodeList nodes) {
    nl = nodes;
  }

  @Override
  public BXNode item(final int index) {
    ANode n = null;
    if(index < nl.size()) n = nl.get(index);
    return n != null ? BXNode.get(n) : null;
  }

  @Override
  public int getLength() {
    return nl.size();
  }
}
