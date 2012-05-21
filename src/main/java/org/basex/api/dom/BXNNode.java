package org.basex.api.dom;

import org.basex.query.iter.*;
import org.basex.util.*;
import org.w3c.dom.*;

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
  public BXNNode(final NodeSeqBuilder nb) {
    super(nb);
  }

  @Override
  public BXNode getNamedItem(final String name) {
    final byte[] nm = Token.token(name);
    final int s = getLength();
    for(int i = 0; i < s; ++i) {
      if(Token.eq(nc.get(i).name(), nm)) return item(i);
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
