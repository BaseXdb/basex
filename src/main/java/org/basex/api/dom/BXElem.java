package org.basex.api.dom;

import org.basex.core.Main;
import org.basex.query.QueryException;
import org.basex.query.item.Nod;
import org.basex.query.iter.NodeIter;
import org.basex.util.Token;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.TypeInfo;

/**
 * DOM - Element implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class BXElem extends BXNode implements Element {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXElem(final Nod n) {
    super(n);
  }

  @Override
  public String getNodeName() {
    return Token.string(node.nname());
  }

  @Override
  public String getLocalName() {
    return getNodeName();
  }

  @Override
  public BXNNode getAttributes() {
    return new BXNNode(finish(node.attr()));
  }

  @Override
  public String getAttribute(final String name) {
    final Nod n = attribute(name);
    return n != null ? Token.string(n.atom()) : "";
  }

  @Override
  public String getNamespaceURI() {
    final byte[] uri = node.qname().uri.atom();
    return uri.length == 0 ? null : Token.string(uri);
  }

  @Override
  public String getAttributeNS(final String uri, final String ln) {
    Main.notimplemented();
    return null;
  }

  @Override
  public BXAttr getAttributeNode(final String name) {
    final Nod n = attribute(name);
    return n != null ? (BXAttr) n.toJava() : null;
  }

  @Override
  public BXAttr getAttributeNodeNS(final String uri, final String ln) {
    Main.notimplemented();
    return null;
  }

  @Override
  public BXNList getElementsByTagName(final String name) {
    return getElements(name);
  }

  @Override
  public BXNList getElementsByTagNameNS(final String uri, final String ln) {
    Main.notimplemented();
    return null;
  }

  @Override
  public TypeInfo getSchemaTypeInfo() {
    Main.notimplemented();
    return null;
  }

  @Override
  public String getTagName() {
    return getNodeName();
  }

  @Override
  public boolean hasAttribute(final String name) {
    return attribute(name) != null;
  }

  @Override
  public boolean hasAttributeNS(final String uri, final String ln) {
    Main.notimplemented();
    return false;
  }

  @Override
  public void removeAttribute(final String name) {
    error();
  }

  @Override
  public void removeAttributeNS(final String uri, final String ln) {
    error();
  }

  @Override
  public BXAttr removeAttributeNode(final Attr oldAttr) {
    error();
    return null;
  }

  @Override
  public void setAttribute(final String name, final String value) {
    error();
  }

  @Override
  public void setAttributeNS(final String uri, final String qn,
      final String value) {
    error();
  }

  @Override
  public BXAttr setAttributeNode(final Attr at) {
    error();
    return null;
  }

  @Override
  public BXAttr setAttributeNodeNS(final Attr at) {
    error();
    return null;
  }

  @Override
  public void setIdAttribute(final String name, final boolean isId) {
    error();
  }

  @Override
  public void setIdAttributeNS(final String uri, final String ln,
      final boolean isId) {
    error();
  }

  @Override
  public void setIdAttributeNode(final Attr at, final boolean isId) {
    error();
  }

  /**
   * Returns the specified attribute.
   * @param name attribute name
   * @return nod instance
   */
  private Nod attribute(final String name) {
    try {
      Nod n = null;
      final NodeIter iter = node.attr();
      final byte[] nm = Token.token(name);
      while((n = iter.next()) != null) {
        if(Token.eq(nm, n.nname())) return n;
      }
    } catch(final QueryException ex) {
      Main.notexpected();
    }
    return null;
  }
}
