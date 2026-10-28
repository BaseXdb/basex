package org.basex.api.dom;

import org.basex.query.util.list.*;
import org.w3c.dom.*;

/**
 * DOM - Node list implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class BXNList implements NodeList {
  /** XQuery node set. */
  final GNodeList nodes;

  /**
   * Constructor.
   * @param nodes nodes
   */
  BXNList(final GNodeList nodes) {
    this.nodes = nodes;
  }

  @Override
  public BXNode item(final int index) {
    return index >= 0 && index < nodes.size() ? BXNode.get(nodes.get(index)) : null;
  }

  @Override
  public int getLength() {
    return nodes.size();
  }
}
