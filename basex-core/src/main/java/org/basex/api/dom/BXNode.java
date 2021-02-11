package org.basex.api.dom;

import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.w3c.dom.*;

/**
 * DOM - Node implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class BXNode implements Node {
  /** Node type mapping (see {@link Data} interface). */
  private static final short[] TYPES = {
    Node.DOCUMENT_NODE, Node.ELEMENT_NODE, Node.TEXT_NODE, Node.ATTRIBUTE_NODE,
    Node.COMMENT_NODE, Node.PROCESSING_INSTRUCTION_NODE, Node.CDATA_SECTION_NODE,
    Node.DOCUMENT_FRAGMENT_NODE
  };
  /** Node name mapping (see {@link Data} interface). */
  private static final String[] NAMES = {
    "#document", null, "#text", null, "#comment", null, "#cdata-section", "#document-fragment"
  };
  /** Node reference. */
  final ANode nd;

  /**
   * Constructor.
   * @param nd node reference
   */
  BXNode(final ANode nd) {
    this.nd = nd;
  }

  /**
   * Creates a new DOM node instance for the input node.
   * @param node input node
   * @return DOM node, or {@code null} if input is {@code null} as well
   */
  public static BXNode get(final ANode node) {
    if(node == null) return null;
    switch(node.nodeType()) {
      case DOCUMENT_NODE: return new BXDoc(node);
      case ELEMENT: return new BXElem(node);
      case TEXT: return new BXText(node);
      case COMMENT: return new BXComm(node);
      case PROCESSING_INSTRUCTION : return new BXPI(node);
      // drop parent reference (see Xerces’ NodeImpl#getParentNode)
      case ATTRIBUTE: return new BXAttr(new FAttr(node.qname(), node.string()));
      default : return null;
    }
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
    return nd.kind();
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
    return nd.toJava();
  }

  @Override
  public final short compareDocumentPosition(final Node node) {
    return (short) Integer.compare(nd.diff(((BXNode) node).nd), 0);
  }

  @Override
  public BXNNode getAttributes() {
    return null;
  }

  @Override
  public final String getBaseURI() {
    return IO.get(string(nd.baseURI())).url();
  }

  @Override
  public BXNList getChildNodes() {
    return new BXNList(finish(nd.childIter()));
  }

  @Override
  public BXNode getFirstChild() {
    return get(nd.childIter().next());
  }

  @Override
  public final BXNode getLastChild() {
    ANode node = null;
    for(final ANode n : nd.childIter()) node = n;
    return node != null ? get(node) : null;
  }

  @Override
  public String getNamespaceURI() {
    return null;
  }

  @Override
  public BXNode getNextSibling() {
    return get(nd.followingSiblingIter().next());
  }

  @Override
  public BXNode getPreviousSibling() {
    return get(nd.precedingSiblingIter().next());
  }

  @Override
  public final BXNode getParentNode() {
    return get(nd.parent());
  }

  @Override
  public final boolean hasChildNodes() {
    return getFirstChild() != null;
  }

  @Override
  public final boolean isSameNode(final Node node) {
    return node instanceof BXNode && ((BXNode) node).nd.is(nd);
  }

  @Override
  public BXDoc getOwnerDocument() {
    ANode n = nd;
    for(ANode p; (p = n.parent()) != null;) n = p;
    return n.type == NodeType.DOCUMENT_NODE ? (BXDoc) get(n) : null;
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
    return string(nd.string());
  }

  @Override
  public final BXNode appendChild(final Node node) {
    throw readOnly();
  }

  @Override
  public final Object getUserData(final String name) {
    return null;
  }

  @Override
  public final boolean isSupported(final String name, final String version) {
    return false;
  }

  @Override
  public final BXNode insertBefore(final Node node, final Node ref) {
    throw readOnly();
  }

  @Override
  public final boolean isDefaultNamespace(final String uri) {
    throw notImplemented();
  }

  @Override
  public final boolean isEqualNode(final Node cmp) {
    throw notImplemented();
  }

  @Override
  public final String lookupNamespaceURI(final String prefix) {
    throw notImplemented();
  }

  @Override
  public final String lookupPrefix(final String uri) {
    throw notImplemented();
  }

  @Override
  public final void normalize() {
    throw readOnly();
  }

  @Override
  public final BXNode removeChild(final Node node) {
    throw readOnly();
  }

  @Override
  public final BXNode replaceChild(final Node node, final Node old) {
    throw readOnly();
  }

  @Override
  public final void setNodeValue(final String value) {
    throw readOnly();
  }

  @Override
  public final void setPrefix(final String prefix) {
    throw readOnly();
  }

  @Override
  public final void setTextContent(final String value) {
    throw readOnly();
  }

  @Override
  public final Object setUserData(final String name, final Object value,
      final UserDataHandler handler) {
    throw readOnly();
  }

  @Override
  public final String toString() {
    return '[' + getNodeName() + ": " + getNodeValue() + ']';
  }

  /**
   * Returns all nodes with the given element name.
   * @param name element name
   * @return nodes
   */
  final BXNList getElements(final String name) {
    final ANodeList nb = new ANodeList();
    final byte[] nm = "*".equals(name) ? null : token(name);
    for(final ANode n : nd.descendantIter()) {
      if(n.type == NodeType.ELEMENT && (nm == null || eq(nm, n.name()))) nb.add(n.finish());
    }
    return new BXNList(nb);
  }

  /**
   * Returns a node cache with the specified nodes.
   * @param iter axis iterator
   * @return node cache
   */
  static ANodeList finish(final BasicNodeIter iter) {
    final ANodeList nl = new ANodeList();
    for(final ANode n : iter) nl.add(n.finish());
    return nl;
  }

  /**
   * Returns the internal node representation.
   * @return xquery node
   */
  public final ANode getNode() {
    return nd;
  }

  /**
   * Throws a DOM modification exception.
   * @return DOM exception
   */
  static DOMException readOnly() {
    return new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
        "DOM implementation is read-only.");
  }

  /**
   * Throws a runtime exception for an unimplemented method.
   * @return runtime exception
   */
  static UnsupportedOperationException notImplemented() {
    return new UnsupportedOperationException();
  }
}
