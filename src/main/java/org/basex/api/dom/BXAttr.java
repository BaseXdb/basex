package org.basex.api.dom;

import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * DOM - Attribute implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BXAttr extends BXNode implements Attr {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXAttr(final ANode n) {
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
  public String getNodeValue() {
    return Token.string(node.string());
  }

  @Override
  public String getName() {
    return getNodeName();
  }

  @Override
  public String getNamespaceURI() {
    final byte[] uri = node.qname().uri();
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
    throw Util.notimplemented();
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
    final NodeSeqBuilder nb = new NodeSeqBuilder();
    nb.add(text());
    return new BXNList(nb);
  }

  /**
   * Returns the attribute value as text node.
   * @return text node
   */
  private FTxt text() {
    final FTxt n = new FTxt(node.string());
    n.parent(node);
    return n;
  }
}
