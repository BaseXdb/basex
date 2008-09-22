package org.basex.api.dom;

import org.basex.BaseX;
import org.basex.query.xquery.item.Nod;
import org.basex.util.Token;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

/**
 * DOM - Attribute Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
    return true;
  }

  public TypeInfo getSchemaTypeInfo() {
    BaseX.notimplemented();
    return null;
  }

  public void setValue(final String value) {
    BaseX.notimplemented();
  }
}
