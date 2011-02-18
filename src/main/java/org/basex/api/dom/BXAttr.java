package org.basex.api.dom;

import org.basex.query.item.FTxt;
import org.basex.query.item.Nod;
import org.basex.query.iter.NodIter;
import org.basex.util.Token;
import org.basex.util.Util;
import org.w3c.dom.Attr;
import org.w3c.dom.TypeInfo;

/**
 * DOM - Attribute implementation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class BXAttr extends BXNode implements Attr {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXAttr(final Nod n) {
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
  public String getNodeValue() {
    return Token.string(node.atom());
  }

  @Override
  public String getName() {
    return getNodeName();
  }

  @Override
  public String getNamespaceURI() {
    final byte[] uri = node.qname().uri().atom();
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
  public BXElem getParentNode() {
    return null;
  }

  @Override
  public BXElem getOwnerElement() {
    return getParentNode();
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
    error();
  }

  @Override
  public BXText getFirstChild() {
    return new BXText(new FTxt(node.atom(), node));
  }

  @Override
  public BXNList getChildNodes() {
    final NodIter nb = new NodIter();
    nb.add(new FTxt(node.atom(), node));
    return new BXNList(nb);
  }
}
