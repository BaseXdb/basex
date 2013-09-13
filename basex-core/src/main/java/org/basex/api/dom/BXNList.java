package org.basex.api.dom;

import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.w3c.dom.*;

/**
 * DOM - Node list implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
class BXNList implements NodeList {
  /** XQuery node set. */
  final ANodeList nl;

  /**
   * Constructor.
   * @param nb nodes
   */
  BXNList(final ANodeList nb) {
    nl = nb;
  }

  @Override
  public BXNode item(final int i) {
    ANode n = null;
    if(i < nl.size()) n = nl.get(i);
    return n != null ? BXNode.get(n) : null;
  }

  @Override
  public int getLength() {
    return nl.size();
  }
}
