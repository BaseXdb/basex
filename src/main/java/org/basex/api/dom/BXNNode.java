package org.basex.api.dom;

import org.basex.core.Main;
import org.basex.data.Data;
import org.basex.query.iter.NodIter;
import org.basex.util.Token;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * DOM - Named node map implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class BXNNode extends BXNList implements NamedNodeMap {
  /**
   * Constructor.
   * @param nb nodes
   */
  public BXNNode(final NodIter nb) {
    super(nb);
  }

  public BXNode getNamedItem(final String name) {
    final byte[] nm = Token.token(name);
    final int s = getLength();
    for(int i = 0; i < s; i++) {
      final byte[] n = xquery != null ? xquery.get(i).nname() :
        nodes.data.name(nodes.nodes[i], Data.ELEM);
      if(Token.eq(n, nm)) return item(i);
    }
    return null;
  }

  public BXNode getNamedItemNS(final String uri, final String ln) {
    Main.notimplemented();
    return null;
  }

  public BXNode setNamedItem(final Node arg) {
    Main.notimplemented();
    return null;
  }

  public BXNode removeNamedItem(final String name) {
    Main.notimplemented();
    return null;
  }

  public BXNode setNamedItemNS(final Node arg) {
    Main.notimplemented();
    return null;
  }

  public BXNode removeNamedItemNS(final String uri, final String ln) {
    Main.notimplemented();
    return null;
  }
}
