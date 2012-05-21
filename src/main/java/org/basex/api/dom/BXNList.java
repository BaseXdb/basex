package org.basex.api.dom;

import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.w3c.dom.*;

/**
 * DOM - Node list implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
class BXNList implements NodeList {
  /** XQuery node set. */
  final NodeSeqBuilder nc;

  /**
   * Constructor.
   * @param nb nodes
   */
  BXNList(final NodeSeqBuilder nb) {
    nc = nb;
    nc.sort();
  }

  @Override
  public BXNode item(final int i) {
    ANode n = null;
    if(i < nc.size()) n = nc.get(i);
    return n != null ? n.toJava() : null;
  }

  @Override
  public int getLength() {
    return (int) nc.size();
  }
}
