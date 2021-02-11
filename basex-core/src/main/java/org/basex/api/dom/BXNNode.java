package org.basex.api.dom;

import org.basex.query.util.list.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * DOM - Named node map implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class BXNNode extends BXNList implements NamedNodeMap {
  /**
   * Constructor.
   * @param nodes nodes
   */
  BXNNode(final ANodeList nodes) {
    super(nodes);
  }

  @Override
  public BXNode getNamedItem(final String name) {
    final byte[] nm = Token.token(name);
    final int s = getLength();
    for(int i = 0; i < s; ++i) {
      if(Token.eq(nodes.get(i).name(), nm)) return item(i);
    }
    return null;
  }

  @Override
  public BXNode getNamedItemNS(final String uri, final String name) {
    throw BXNode.notImplemented();
  }

  @Override
  public BXNode setNamedItem(final Node node) {
    throw BXNode.notImplemented();
  }

  @Override
  public BXNode removeNamedItem(final String name) {
    throw BXNode.notImplemented();
  }

  @Override
  public BXNode setNamedItemNS(final Node node) {
    throw BXNode.notImplemented();
  }

  @Override
  public BXNode removeNamedItemNS(final String uri, final String name) {
    throw BXNode.notImplemented();
  }
}
