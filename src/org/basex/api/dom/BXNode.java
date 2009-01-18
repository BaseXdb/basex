package org.basex.api.dom;

import static org.basex.util.Token.*;
import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.util.NodeBuilder;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

/**
 * DOM - Node Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class BXNode implements Node {
  /** Node type mapping (see {@link Data} interface). */
  static final short[] TYPES = {
    Node.DOCUMENT_NODE, Node.ELEMENT_NODE,
    Node.TEXT_NODE, Node.ATTRIBUTE_NODE,
    Node.COMMENT_NODE, Node.PROCESSING_INSTRUCTION_NODE,
    Node.CDATA_SECTION_NODE, Node.DOCUMENT_FRAGMENT_NODE
  };
  /** Node name mapping (see {@link Data} interface). */
  static final String[] NAMES = {
    "#document", null, "#text", null, "#comment", null, "#cdata-section",
    "#document-fragment"
  };
  /** Data reference. */
  protected final Nod node;

  /**
   * Constructor.
   * @param n node reference
   */
  protected BXNode(final Nod n) {
    node = n;
  }

  public String getNodeName() {
    return NAMES[kind()];
  }

  public final short getNodeType() {
    return TYPES[kind()];
  }

  /**
   * Returns a numeric value for the node kind.
   * @return node kind
   */
  protected int kind() {
    return Nod.kind(node.type);
  }

  public String getNodeValue() {
    return null;
  }

  public String getLocalName() {
    return null;
  }

  public final Node cloneNode(final boolean deep) {
    return node.copy().java();
  }

  public final short compareDocumentPosition(final Node other) {
    final int d = node.diff((Nod) other);
    return (short) (d < 0 ? -1 : d > 0 ? 1 : 0);
  }

  public NamedNodeMap getAttributes() {
    return null;
  }

  public final String getBaseURI() {
    return IO.url(string(node.base()));
  }

  public NodeList getChildNodes() {
    return new BXNList(finish(node.child()));
  }

  public Node getFirstChild() {
    try {
      return finish(node.child().next());
    } catch(final XQException ex) {
      BaseX.notexpected();
      return null;
    }
  }

  public final Node getLastChild() {
    Nod n = null;
    try {
      final NodeIter it = node.child();
      Nod t = null;
      while((t = it.next()) != null) n = t;
    } catch(final XQException ex) {
      BaseX.notexpected();
    }
    return finish(n);
  }

  public final String getNamespaceURI() {
    return null;
  }

  public Node getNextSibling() {
    try {
      return finish(node.follSibl().next());
    } catch(final XQException ex) {
      BaseX.notexpected();
      return null;
    }
  }

  public Node getPreviousSibling() {
    try {
      return finish(node.precSibl().next());
    } catch(final XQException ex) {
      BaseX.notexpected();
      return null;
    }
  }

  public Node getParentNode() {
    return finish(node.parent());
  }

  /**
   * Returns a Java node for the specified argument or null.
   * @param n node instance
   * @return resulting node
   */
  protected Node finish(final Nod n) {
    return n != null ? n.java() : null;
  }

  public final boolean hasChildNodes() {
    return getFirstChild() != null;
  }

  public final boolean isSameNode(final Node other) {
    return this == other;
  }

  public Document getOwnerDocument() {
    Nod n = node;
    Nod p = n;
    while((p = n.parent()) != null) n = p;
    return n.type == Type.DOC ? (Document) n.java() : null;
  }

  public final boolean hasAttributes() {
    return getAttributes().getLength() != 0;
  }

  public final Object getFeature(final String feature, final String version) {
    return null;
  }

  public final String getPrefix() {
    return null;
  }

  public final String getTextContent() {
    return string(node.str());
  }

  public final Node appendChild(final Node newChild) {
    error();
    return null;
  }

  public final Object getUserData(final String key) {
    return null;
  }

  public final boolean isSupported(final String feature, final String version) {
    return false;
  }

  public final Node insertBefore(final Node newChild, final Node refChild) {
    error();
    return null;
  }

  public final boolean isDefaultNamespace(final String namespaceURI) {
    BaseX.notimplemented();
    return false;
  }

  public final boolean isEqualNode(final Node arg) {
    BaseX.notimplemented();
    return false;
  }

  public final String lookupNamespaceURI(final String prefix) {
    BaseX.notimplemented();
    return null;
  }

  public final String lookupPrefix(final String namespaceURI) {
    BaseX.notimplemented();
    return null;
  }

  public final void normalize() {
    error();
  }

  public final Node removeChild(final Node oldChild) {
    error();
    return null;
  }

  public final Node replaceChild(final Node newChild, final Node oldChild) {
    error();
    return null;
  }

  public final void setNodeValue(final String nodeValue) {
    error();
  }

  public final void setPrefix(final String prefix) {
    error();
  }

  public final void setTextContent(final String textContent) {
    error();
  }

  public final Object setUserData(final String key, final Object dat,
      final UserDataHandler handler) {
    error();
    return null;
  }

  @Override
  public final String toString() {
    return "[" + getNodeName() + ": " + getNodeValue() + "]";
  }

  /**
   * Returns all nodes with the given tag name.
   * @param tag tag name
   * @return nodes
   */
  protected final NodeList getElements(final String tag) {
    final NodeBuilder nb = new NodeBuilder(true);
    final NodeIter iter = node.desc();
    final byte[] nm = tag.equals("*") ? null : token(tag);
    try {
      Nod n = null;
      while((n = iter.next()) != null) {
        if(n.type == Type.ELM && (nm == null || eq(nm, n.nname())))
          nb.add(n.copy());
      }
    } catch(final XQException ex) {
      BaseX.notexpected();
    }
    return new BXNList(nb);
  }

  /**
   * Returns a node builder with the specified nodes. 
   * @param it node iterator
   * @return node builder
   */
  protected static final NodeBuilder finish(final NodeIter it) {
    final NodeBuilder nb = new NodeBuilder(true);
    try {
      Nod n = null;
      while((n = it.next()) != null) nb.add(n.copy());
    } catch(final XQException ex) {
      BaseX.notexpected();
    }
    return nb;
  }

  /**
   * Returns the XQuery node.
   * @return xquery node
   */
  public final Nod getNod() {
    return node;
  }

  /**
   * Throws a DOM modification exception.
   */
  public final void error() {
    throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
        "DOM implementation is read-only.");
  }
}
