package org.basex.api.dom;

import org.basex.query.item.ANode;
import org.basex.query.iter.AxisIter;
import org.basex.util.Token;
import org.basex.util.Util;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.TypeInfo;

/**
 * DOM - Element implementation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class BXElem extends BXNode implements Element {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXElem(final ANode n) {
    super(n);
  }

  @Override
  public String getNodeName() {
    return Token.string(node.name());
  }

  @Override
  public String getLocalName() {
    return Token.string(Token.local(node.name()));
  }

  @Override
  public BXNNode getAttributes() {
    return new BXNNode(finish(node.attributes()));
  }

  @Override
  public String getAttribute(final String name) {
    final ANode n = attribute(name);
    return n != null ? Token.string(n.string()) : "";
  }

  @Override
  public String getNamespaceURI() {
    final byte[] uri = node.qname().uri();
    return uri.length == 0 ? null : Token.string(uri);
  }

  @Override
  public String getAttributeNS(final String uri, final String ln) {
    Util.notimplemented();
    return null;
  }

  @Override
  public BXAttr getAttributeNode(final String name) {
    final ANode n = attribute(name);
    return n != null ? (BXAttr) n.toJava() : null;
  }

  @Override
  public BXAttr getAttributeNodeNS(final String uri, final String ln) {
    Util.notimplemented();
    return null;
  }

  @Override
  public BXNList getElementsByTagName(final String name) {
    return getElements(name);
  }

  @Override
  public BXNList getElementsByTagNameNS(final String uri, final String ln) {
    Util.notimplemented();
    return null;
  }

  @Override
  public TypeInfo getSchemaTypeInfo() {
    Util.notimplemented();
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
    Util.notimplemented();
    return false;
  }

  @Override
  public void removeAttribute(final String name) {
    readOnly();
  }

  @Override
  public void removeAttributeNS(final String uri, final String ln) {
    readOnly();
  }

  @Override
  public BXAttr removeAttributeNode(final Attr oldAttr) {
    readOnly();
    return null;
  }

  @Override
  public void setAttribute(final String name, final String value) {
    readOnly();
  }

  @Override
  public void setAttributeNS(final String uri, final String qn,
      final String value) {
    readOnly();
  }

  @Override
  public BXAttr setAttributeNode(final Attr at) {
    readOnly();
    return null;
  }

  @Override
  public BXAttr setAttributeNodeNS(final Attr at) {
    readOnly();
    return null;
  }

  @Override
  public void setIdAttribute(final String name, final boolean isId) {
    readOnly();
  }

  @Override
  public void setIdAttributeNS(final String uri, final String ln,
      final boolean isId) {
    readOnly();
  }

  @Override
  public void setIdAttributeNode(final Attr at, final boolean isId) {
    readOnly();
  }

  /**
   * Returns the specified attribute.
   * @param name attribute name
   * @return node, or {@code null}
   */
  private ANode attribute(final String name) {
    final AxisIter ai = node.attributes();
    final byte[] nm = Token.token(name);
    for(ANode n; (n = ai.next()) != null;) {
      if(Token.eq(nm, n.name())) return n;
    }
    return null;
  }
}
