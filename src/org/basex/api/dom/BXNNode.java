package org.basex.api.dom;

import org.basex.BaseX;
import org.basex.query.iter.NodIter;
import org.basex.util.Token;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * DOM - NamedNodeMap Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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

  public Node getNamedItem(final String name) {
    final byte[] nm = Token.token(name);
    final int s = getLength();
    for(int i = 0; i < s; i++) {
      final byte[] n = xquery != null ? xquery.list[i].nname() :
        nodes.data.tag(nodes.nodes[i]);
      if(Token.eq(n, nm)) return item(i);
    }
    return null;
  }

  public Node getNamedItemNS(final String uri, final String ln) {
    BaseX.notimplemented();
    return null;
  }

  public Node setNamedItem(final Node arg) {
    BaseX.notimplemented();
    return null;
  }

  public Node removeNamedItem(final String name) {
    BaseX.notimplemented();
    return null;
  }

  public Node setNamedItemNS(final Node arg) {
    BaseX.notimplemented();
    return null;
  }

  public Node removeNamedItemNS(final String uri, final String ln) {
    BaseX.notimplemented();
    return null;
  }
}
