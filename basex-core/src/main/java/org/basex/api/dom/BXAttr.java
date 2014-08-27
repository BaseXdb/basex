package org.basex.api.dom;

import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * DOM - Attribute implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BXAttr extends BXNode implements Attr {
  /**
   * Constructor.
   * @param node node reference
   */
  public BXAttr(final ANode node) {
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
  public String getNodeValue() {
    return Token.string(nd.string());
  }

  @Override
  public String getName() {
    return getNodeName();
  }

  @Override
  public String getNamespaceURI() {
    final byte[] uri = nd.qname().uri();
    return uri.length == 0 ? null : Token.string(uri);
  }

  @Override
  public BXNode getNextSibling() {
    return null;
  }

  @Override
  public BXNode getPreviousSibling() {
    return null;
  }

  @Override
  public BXElem getOwnerElement() {
    return (BXElem) getParentNode();
  }

  @Override
  public String getValue() {
    return getNodeValue();
  }

  @Override
  public boolean isId() {
    return false;
  }

  @Override
  public boolean getSpecified() {
    return false;
  }

  @Override
  public TypeInfo getSchemaTypeInfo() {
    throw notImplemented();
  }

  @Override
  public void setValue(final String value) {
    throw readOnly();
  }

  @Override
  public BXText getFirstChild() {
    return new BXText(text());
  }

  @Override
  public BXNList getChildNodes() {
    return new BXNList(new ANodeList(text()));
  }

  /**
   * Returns the attribute value as text node.
   * @return text node
   */
  private FNode text() {
    return new FTxt(nd.string()).parent(nd);
  }
}
