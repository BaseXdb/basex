package org.basex.api.dom;

import static org.basex.util.Token.*;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.AxisIter;
import org.basex.util.Util;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;

/**
 * DOM - Node implementation.
 *
 * @author BaseX Team 2005-12, BSD License
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
  final ANode node;

  /**
   * Constructor.
   * @param n node reference
   */
  BXNode(final ANode n) {
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
  int kind() {
    return node.kind();
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
    return IO.get(string(node.baseURI())).url();
  }

  @Override
  public BXNList getChildNodes() {
    return new BXNList(finish(node.children()));
  }

  @Override
  public BXNode getFirstChild() {
    return toJava(node.children().next());
  }

  @Override
  public final BXNode getLastChild() {
    ANode n = null;
    final AxisIter ai = node.children();
    for(ANode t; (t = ai.next()) != null;) n = t;
    return toJava(n);
  }

  @Override
  public String getNamespaceURI() {
    return null;
  }

  @Override
  public BXNode getNextSibling() {
    return toJava(node.followingSibling().next());
  }

  @Override
  public BXNode getPreviousSibling() {
    return toJava(node.precedingSibling().next());
  }

  @Override
  public final BXNode getParentNode() {
    return toJava(node.parent());
  }

  /**
   * Returns a Java node for the specified argument or {@code null}.
   * @param n node instance
   * @return resulting node
   */
  private BXNode toJava(final ANode n) {
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
    ANode n = node;
    for(ANode p; (p = n.parent()) != null;) n = p;
    return n.type == NodeType.DOC ? (BXDoc) n.toJava() : null;
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
    return string(node.string());
  }

  @Override
  public final BXNode appendChild(final Node newChild) {
    readOnly();
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
    readOnly();
    return null;
  }

  @Override
  public final boolean isDefaultNamespace(final String namespaceURI) {
    Util.notimplemented();
    return false;
  }

  @Override
  public final boolean isEqualNode(final Node cmp) {
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
    readOnly();
  }

  @Override
  public final BXNode removeChild(final Node oldChild) {
    readOnly();
    return null;
  }

  @Override
  public final BXNode replaceChild(final Node newChild, final Node oldChild) {
    readOnly();
    return null;
  }

  @Override
  public final void setNodeValue(final String nodeValue) {
    readOnly();
  }

  @Override
  public final void setPrefix(final String prefix) {
    readOnly();
  }

  @Override
  public final void setTextContent(final String textContent) {
    readOnly();
  }

  @Override
  public final Object setUserData(final String key, final Object dat,
      final UserDataHandler handler) {
    readOnly();
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
  final BXNList getElements(final String tag) {
    final NodeCache nb = new NodeCache();
    final AxisIter ai = node.descendant();
    final byte[] nm = tag.equals("*") ? null : token(tag);
    for(ANode n; (n = ai.next()) != null;) {
      if(n.type == NodeType.ELM && (nm == null || eq(nm, n.name())))
        nb.add(n.copy());
    }
    return new BXNList(nb);
  }

  /**
   * Returns a node cache with the specified nodes.
   * @param ai axis iterator
   * @return node cache
   */
  static NodeCache finish(final AxisIter ai) {
    final NodeCache nc = new NodeCache();
    for(ANode n; (n = ai.next()) != null;) nc.add(n.finish());
    return nc;
  }

  /**
   * Returns the XQuery node.
   * @return xquery node
   */
  public final ANode getNod() {
    return node;
  }

  /**
   * Throws a DOM modification exception.
   */
  final void readOnly() {
    throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
        "DOM implementation is read-only.");
  }
}
