package org.basex.api.dom;

import org.basex.BaseX;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.util.Token;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * DOM - Element Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BXElement extends BXNode implements Element {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXElement(final Nod n) {
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

  public String getAttribute(final String name) {
    final NodeIter iter = node.attr();
    final byte[] nm = Token.token(name);
    try {
      Nod n = null;
      while((n = iter.next()) != null) {
        if(Token.eq(nm, n.nname())) return Token.string(n.str());
      }
    } catch(final XQException ex) {
      BaseX.notexpected();
    }
    return "";
  }

  public String getAttributeNS(final String uri, final String ln) {
    BaseX.notimplemented();
    return null;
  }

  public Attr getAttributeNode(final String name) {
    final NodeIter iter = node.attr();
    final byte[] nm = Token.token(name);
    try {
      Nod n = null;
      while((n = iter.next()) != null) {
        if(Token.eq(nm, n.nname())) return (Attr) n.java();
      }
    } catch(final XQException ex) {
      BaseX.notexpected();
    }
    return null;
  }

  public Attr getAttributeNodeNS(final String uri, final String ln) {
    BaseX.notimplemented();
    return null;
  }

  public NodeList getElementsByTagName(final String name) {
    return getElements(name);
  }

  public NodeList getElementsByTagNameNS(final String uri, final String ln) {
    BaseX.notimplemented();
    return null;
  }

  public TypeInfo getSchemaTypeInfo() {
    BaseX.notimplemented();
    return null;
  }

  public String getTagName() {
    return getNodeName();
  }

  public boolean hasAttribute(final String name) {
    return getAttribute(name) != null;
  }

  public boolean hasAttributeNS(final String uri, final String ln) {
    BaseX.notimplemented();
    return false;
  }

  public void removeAttribute(final String name) {
    BaseX.notimplemented();
  }

  public void removeAttributeNS(final String uri, final String ln) {
    BaseX.notimplemented();
  }

  public Attr removeAttributeNode(final Attr oldAttr) {
    BaseX.notimplemented();
    return null;
  }

  public void setAttribute(final String name, final String value) {
    BaseX.notimplemented();
  }

  public void setAttributeNS(final String uri, final String qn,
      final String value) {
    BaseX.notimplemented();
  }

  public Attr setAttributeNode(final Attr newAttr) {
    BaseX.notimplemented();
    return null;
  }

  public Attr setAttributeNodeNS(final Attr newAttr) {
    BaseX.notimplemented();
    return null;
  }

  public void setIdAttribute(final String name, final boolean isId) {
    BaseX.notimplemented();
  }

  public void setIdAttributeNS(final String uri, final String ln,
      final boolean isId) {
    BaseX.notimplemented();
  }

  public void setIdAttributeNode(final Attr idAttr, final boolean isId) {
    BaseX.notimplemented();
  }
}
