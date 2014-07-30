package org.basex.api.dom;

import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * DOM - Element implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BXElem extends BXNode implements Element {
  /**
   * Constructor.
   * @param node node reference
   */
  public BXElem(final ANode node) {
    super(node);
  }

  @Override
  public String getNodeName() {
    return Token.string(nd.name());
  }

  @Override
  public String getLocalName() {
    return Token.string(Token.local(nd.name()));
  }

  @Override
  public BXNNode getAttributes() {
    return new BXNNode(finish(nd.attributes()));
  }

  @Override
  public String getAttribute(final String name) {
    final ANode n = attribute(name);
    return n != null ? Token.string(n.string()) : "";
  }

  @Override
  public String getNamespaceURI() {
    final byte[] uri = nd.qname().uri();
    return uri.length == 0 ? null : Token.string(uri);
  }

  @Override
  public String getAttributeNS(final String uri, final String name) {
    throw notImplemented();
  }

  @Override
  public BXAttr getAttributeNode(final String name) {
    return (BXAttr) get(attribute(name));
  }

  @Override
  public BXAttr getAttributeNodeNS(final String uri, final String name) {
    throw notImplemented();
  }

  @Override
  public BXNList getElementsByTagName(final String name) {
    return getElements(name);
  }

  @Override
  public BXNList getElementsByTagNameNS(final String uri, final String name) {
    throw notImplemented();
  }

  @Override
  public TypeInfo getSchemaTypeInfo() {
    throw notImplemented();
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
  public boolean hasAttributeNS(final String uri, final String name) {
    throw notImplemented();
  }

  @Override
  public void removeAttribute(final String name) {
    throw readOnly();
  }

  @Override
  public void removeAttributeNS(final String uri, final String name) {
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
  public void setAttributeNS(final String uri, final String name, final String value) {
    throw readOnly();
  }

  @Override
  public BXAttr setAttributeNode(final Attr node) {
    throw readOnly();
  }

  @Override
  public BXAttr setAttributeNodeNS(final Attr node) {
    throw readOnly();
  }

  @Override
  public void setIdAttribute(final String name, final boolean id) {
    throw readOnly();
  }

  @Override
  public void setIdAttributeNS(final String uri, final String name, final boolean id) {
    throw readOnly();
  }

  @Override
  public void setIdAttributeNode(final Attr node, final boolean id) {
    throw readOnly();
  }

  /**
   * Returns the specified attribute.
   * @param name attribute name
   * @return node, or {@code null}
   */
  private ANode attribute(final String name) {
    final AxisIter ai = nd.attributes();
    final byte[] nm = Token.token(name);
    for(ANode n; (n = ai.next()) != null;) if(Token.eq(nm, n.name())) return n.finish();
    return null;
  }
}
