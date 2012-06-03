package org.basex.api.dom;

import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * DOM - Element implementation.
 *
 * @author BaseX Team 2005-12, BSD License
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
    throw Util.notimplemented();
  }

  @Override
  public BXAttr getAttributeNode(final String name) {
    final ANode n = attribute(name);
    return n != null ? (BXAttr) n.toJava() : null;
  }

  @Override
  public BXAttr getAttributeNodeNS(final String uri, final String ln) {
    throw Util.notimplemented();
  }

  @Override
  public BXNList getElementsByTagName(final String name) {
    return getElements(name);
  }

  @Override
  public BXNList getElementsByTagNameNS(final String uri, final String ln) {
    throw Util.notimplemented();
  }

  @Override
  public TypeInfo getSchemaTypeInfo() {
    throw Util.notimplemented();
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
    throw Util.notimplemented();
  }

  @Override
  public void removeAttribute(final String name) {
    throw readOnly();
  }

  @Override
  public void removeAttributeNS(final String uri, final String ln) {
    throw readOnly();
  }

  @Override
  public BXAttr removeAttributeNode(final Attr oldAttr) {
    throw readOnly();
  }

  @Override
  public void setAttribute(final String name, final String value) {
    throw readOnly();
  }

  @Override
  public void setAttributeNS(final String uri, final String qn, final String value) {
    throw readOnly();
  }

  @Override
  public BXAttr setAttributeNode(final Attr at) {
    throw readOnly();
  }

  @Override
  public BXAttr setAttributeNodeNS(final Attr at) {
    throw readOnly();
  }

  @Override
  public void setIdAttribute(final String name, final boolean isId) {
    throw readOnly();
  }

  @Override
  public void setIdAttributeNS(final String uri, final String ln, final boolean isId) {
    throw readOnly();
  }

  @Override
  public void setIdAttributeNode(final Attr at, final boolean isId) {
    throw readOnly();
  }

  /**
   * Returns the specified attribute.
   * @param name attribute name
   * @return node, or {@code null}
   */
  private ANode attribute(final String name) {
    final AxisIter ai = node.attributes();
    final byte[] nm = Token.token(name);
    for(ANode n; (n = ai.next()) != null;) if(Token.eq(nm, n.name())) return n.finish();
    return null;
  }
}
