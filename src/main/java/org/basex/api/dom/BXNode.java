package org.basex.api.dom;

import static org.basex.util.Token.*;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.util.Util;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;

/**
 * DOM - Node implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class BXNode implements Node {
  /** Node type mapping (see {@link Data} interface). */
  private static final short[] TYPES = {
    Node.DOCUMENT_NODE, Node.ELEMENT_NODE,
    Node.TEXT_NODE, Node.ATTRIBUTE_NODE,
    Node.COMMENT_NODE, Node.PROCESSING_INSTRUCTION_NODE,
    Node.CDATA_SECTION_NODE, Node.DOCUMENT_FRAGMENT_NODE
  };
  /** Node name mapping (see {@link Data} interface). */
  private static final String[] NAMES = {
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

  @Override
  public String getNodeName() {
    return NAMES[kind()];
  }

  @Override
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

  @Override
  public String getNodeValue() {
    return null;
  }

  @Override
  public String getLocalName() {
    return null;
  }

  @Override
  public final BXNode cloneNode(final boolean deep) {
    return node.copy().toJava();
  }

  @Override
  public final short compareDocumentPosition(final Node other) {
    final int d = node.diff(((BXNode) other).node);
    return (short) (d < 0 ? -1 : d > 0 ? 1 : 0);
  }

  @Override
  public BXNNode getAttributes() {
    return null;
  }

  @Override
  public final String getBaseURI() {
    return IO.get(string(node.base())).url();
  }

  @Override
  public BXNList getChildNodes() {
    return new BXNList(finish(node.child()));
  }

  @Override
  public BXNode getFirstChild() {
    try {
      return finish(node.child().next());
    } catch(final QueryException ex) {
      Util.notexpected();
      return null;
    }
  }

  @Override
  public final BXNode getLastChild() {
    Nod n = null;
    try {
      final NodeIter it = node.child();
      Nod t = null;
      while((t = it.next()) != null) n = t;
    } catch(final QueryException ex) {
      Util.notexpected();
    }
    return finish(n);
  }

  @Override
  public String getNamespaceURI() {
    return null;
  }

  @Override
  public BXNode getNextSibling() {
    try {
      return finish(node.follSibl().next());
    } catch(final QueryException ex) {
      Util.notexpected();
      return null;
    }
  }

  @Override
  public BXNode getPreviousSibling() {
    try {
      return finish(node.precSibl().next());
    } catch(final QueryException ex) {
      Util.notexpected();
      return null;
    }
  }

  @Override
  public BXNode getParentNode() {
    return finish(node.parent());
  }

  /**
   * Returns a Java node for the specified argument or null.
   * @param n node instance
   * @return resulting node
   */
  private BXNode finish(final Nod n) {
    return n != null ? n.toJava() : null;
  }

  @Override
  public final boolean hasChildNodes() {
    return getFirstChild() != null;
  }

  @Override
  public final boolean isSameNode(final Node other) {
    return this == other;
  }

  @Override
  public BXDoc getOwnerDocument() {
    Nod n = node;
    Nod p;
    while((p = n.parent()) != null) n = p;
    return n.type == Type.DOC ? (BXDoc) n.toJava() : null;
  }

  @Override
  public final boolean hasAttributes() {
    return getAttributes().getLength() != 0;
  }

  @Override
  public final Object getFeature(final String feature, final String version) {
    return null;
  }

  @Override
  public final String getPrefix() {
    return null;
  }

  @Override
  public final String getTextContent() {
    return string(node.atom());
  }

  @Override
  public final BXNode appendChild(final Node newChild) {
    error();
    return null;
  }

  @Override
  public final Object getUserData(final String key) {
    return null;
  }

  @Override
  public final boolean isSupported(final String feature, final String version) {
    return false;
  }

  @Override
  public final BXNode insertBefore(final Node newChild, final Node refChild) {
    error();
    return null;
  }

  @Override
  public final boolean isDefaultNamespace(final String namespaceURI) {
    Util.notimplemented();
    return false;
  }

  @Override
  public final boolean isEqualNode(final Node arg) {
    Util.notimplemented();
    return false;
  }

  @Override
  public final String lookupNamespaceURI(final String prefix) {
    Util.notimplemented();
    return null;
  }

  @Override
  public final String lookupPrefix(final String namespaceURI) {
    Util.notimplemented();
    return null;
  }

  @Override
  public final void normalize() {
    error();
  }

  @Override
  public final BXNode removeChild(final Node oldChild) {
    error();
    return null;
  }

  @Override
  public final BXNode replaceChild(final Node newChild, final Node oldChild) {
    error();
    return null;
  }

  @Override
  public final void setNodeValue(final String nodeValue) {
    error();
  }

  @Override
  public final void setPrefix(final String prefix) {
    error();
  }

  @Override
  public final void setTextContent(final String textContent) {
    error();
  }

  @Override
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
  protected final BXNList getElements(final String tag) {
    final NodIter nb = new NodIter();
    final NodeIter iter = node.descendant();
    final byte[] nm = tag.equals("*") ? null : token(tag);
    try {
      Nod n = null;
      while((n = iter.next()) != null) {
        if(n.type == Type.ELM && (nm == null || eq(nm, n.nname())))
          nb.add(n.copy());
      }
    } catch(final QueryException ex) {
      Util.notexpected();
    }
    return new BXNList(nb);
  }

  /**
   * Returns a node builder with the specified nodes.
   * @param it node iterator
   * @return node builder
   */
  protected static final NodIter finish(final NodeIter it) {
    final NodIter nb = new NodIter();
    try {
      Nod n = null;
      while((n = it.next()) != null) nb.add(n.copy());
    } catch(final QueryException ex) {
      Util.notexpected();
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
  protected final void error() {
    throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
        "DOM implementation is read-only.");
  }
}
