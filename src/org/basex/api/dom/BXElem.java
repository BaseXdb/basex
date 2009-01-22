package org.basex.api.dom;

import org.basex.BaseX;
import org.basex.query.QueryException;
import org.basex.query.item.Nod;
import org.basex.query.iter.NodeIter;
import org.basex.util.Token;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * DOM - Element Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BXElem extends BXNode implements Element {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXElem(final Nod n) {
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
  public NamedNodeMap getAttributes() {
    return new BXNNode(finish(node.attr()));
  }

  public String getAttribute(final String name) {
    final Nod n = attribute(name);
    return n != null ? Token.string(n.str()) : "";
  }

  public String getAttributeNS(final String uri, final String ln) {
    BaseX.notimplemented();
    return null;
  }

  public Attr getAttributeNode(final String name) {
    final Nod n = attribute(name);
    return n != null ? (Attr) n.java() : null;
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
    return attribute(name) != null;
  }

  public boolean hasAttributeNS(final String uri, final String ln) {
    BaseX.notimplemented();
    return false;
  }

  public void removeAttribute(final String name) {
    error();
  }

  public void removeAttributeNS(final String uri, final String ln) {
    error();
  }

  public Attr removeAttributeNode(final Attr oldAttr) {
    error();
    return null;
  }

  public void setAttribute(final String name, final String value) {
    error();
  }

  public void setAttributeNS(final String uri, final String qn,
      final String value) {
    error();
  }

  public Attr setAttributeNode(final Attr at) {
    error();
    return null;
  }

  public Attr setAttributeNodeNS(final Attr at) {
    error();
    return null;
  }

  public void setIdAttribute(final String name, final boolean isId) {
    error();
  }

  public void setIdAttributeNS(final String uri, final String ln,
      final boolean isId) {
    error();
  }

  public void setIdAttributeNode(final Attr at, final boolean isId) {
    error();
  }

  /**
   * Returns the specified attribute.
   * @param name attribute name
   * @return nod instance
   */
  private Nod attribute(final String name) {
    try {
      Nod n = null;
      final NodeIter iter = node.attr();
      final byte[] nm = Token.token(name);
      while((n = iter.next()) != null) {
        if(Token.eq(nm, n.nname())) return n;
      }
    } catch(final QueryException ex) {
      BaseX.notexpected();
    }
    return null;
  }
}
