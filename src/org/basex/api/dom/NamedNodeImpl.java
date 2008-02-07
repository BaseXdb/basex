package org.basex.api.dom;

import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.util.Token;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * DOM - NamedNodeMap Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class NamedNodeImpl extends NodeListImpl implements NamedNodeMap {
  /**
   * Constructor.
   * @param d data reference
   * @param n node array
   * @param s size
   */
  public NamedNodeImpl(final Data d, final int[] n, final int s) {
    super(d, n, s);
  }
  
  public Node getNamedItem(final String name) {
    final int att = data.attNameID(Token.token(name));
    if(att == 0) return null;
    
    for(int i = 0; i < size; i++) {
      if(data.attNameID(nodes[i]) == att) return NodeImpl.get(data, nodes[i]);
    }
    return null;
  }

  public Node getNamedItemNS(final String uri, final String ln) {
    BaseX.notimplemented();
    return null;
  }

  public Node setNamedItem(final Node arg) {
    BaseX.noupdates();
    return null;
  }

  public Node removeNamedItem(final String name) {
    BaseX.noupdates();
    return null;
  }

  public Node setNamedItemNS(final Node arg) {
    BaseX.noupdates();
    return null;
  }

  public Node removeNamedItemNS(final String uri, final String ln) {
    BaseX.noupdates();
    return null;
  }
}
