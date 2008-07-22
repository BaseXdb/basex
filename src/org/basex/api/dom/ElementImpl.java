package org.basex.api.dom;

import org.basex.BaseX;
import org.basex.data.Data;
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
public final class ElementImpl extends NodeImpl implements Element {
  /**
   * Constructor.
   * @param d data reference
   * @param p pre value
   */
  public ElementImpl(final Data d, final int p) {
    super(d, p, Data.ELEM);
  }
  
  @Override
  public String getNodeName() {
    return Token.string(data.tag(pre));
  }

  public String getAttribute(final String name) {
    final int att = data.atts.id(Token.token(name));
    if(att == 0) return null;
    final byte[] str = data.attValue(att, pre);
    return str == null ? null : Token.string(str);
  }

  public String getAttributeNS(final String uri, final String ln) {
    BaseX.notimplemented();
    return null;
  }

  public Attr getAttributeNode(final String name) {
    final int att = data.atts.id(Token.token(name));
    if(att == 0) return null;
    int s = pre + data.attSize(pre, kind);
    int p = pre;
    while(++p != s) if(data.attNameID(p) == att) return (Attr) get(data, p);
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
