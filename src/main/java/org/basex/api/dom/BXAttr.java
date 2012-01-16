package org.basex.api.dom;

import org.basex.query.item.FTxt;
import org.basex.query.item.ANode;
import org.basex.query.iter.NodeCache;
import org.basex.util.Token;
import org.basex.util.Util;
import org.w3c.dom.Attr;
import org.w3c.dom.TypeInfo;

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
    Util.notimplemented();
    return null;
  }

  @Override
  public void setValue(final String value) {
    readOnly();
  }

  @Override
  public BXText getFirstChild() {
    return new BXText(text());
  }

  @Override
  public BXNList getChildNodes() {
    final NodeCache nb = new NodeCache();
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
