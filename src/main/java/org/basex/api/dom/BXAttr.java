package org.basex.api.dom;

import org.basex.core.Main;
import org.basex.query.item.FTxt;
import org.basex.query.item.Nod;
import org.basex.query.iter.NodIter;
import org.basex.util.Token;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * DOM - Attribute implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
    return Token.string(node.str());
  }

  public String getName() {
    return getNodeName();
  }

  @Override
  public Node getNextSibling() {
    return null;
  }

  @Override
  public Node getPreviousSibling() {
    return null;
  }

  @Override
  public Node getParentNode() {
    return null;
  }

  public Element getOwnerElement() {
    return (Element) getParentNode();
  }

  public String getValue() {
    return getNodeValue();
  }

  public boolean isId() {
    return false;
  }

  public boolean getSpecified() {
    return false;
  }

  public TypeInfo getSchemaTypeInfo() {
    Main.notimplemented();
    return null;
  }

  public void setValue(final String value) {
    error();
  }

  @Override
  public Node getFirstChild() {
    return new BXText(new FTxt(node.str(), node));
  }

  @Override
  public NodeList getChildNodes() {
    final NodIter nb = new NodIter();
    nb.add(new FTxt(node.str(), node));
    return new BXNList(nb);
  }
}
