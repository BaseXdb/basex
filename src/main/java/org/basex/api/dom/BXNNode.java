package org.basex.api.dom;

import org.basex.data.Data;
import org.basex.query.iter.NodeCache;
import org.basex.util.Token;
import org.basex.util.Util;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * DOM - Named node map implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BXNNode extends BXNList implements NamedNodeMap {
  /**
   * Constructor.
   * @param nb nodes
   */
  public BXNNode(final NodeCache nb) {
    super(nb);
  }

  @Override
  public BXNode getNamedItem(final String name) {
    final byte[] nm = Token.token(name);
    final int s = getLength();
    for(int i = 0; i < s; ++i) {
      final byte[] n = xquery != null ? xquery.get(i).name() :
        nodes.data.name(nodes.list[i], Data.ELEM);
      if(Token.eq(n, nm)) return item(i);
    }
    return null;
  }

  @Override
  public BXNode getNamedItemNS(final String uri, final String ln) {
    throw Util.notimplemented();
  }

  @Override
  public BXNode setNamedItem(final Node node) {
    throw Util.notimplemented();
  }

  @Override
  public BXNode removeNamedItem(final String name) {
    throw Util.notimplemented();
  }

  @Override
  public BXNode setNamedItemNS(final Node node) {
    throw Util.notimplemented();
  }

  @Override
  public BXNode removeNamedItemNS(final String uri, final String ln) {
    throw Util.notimplemented();
  }
}
