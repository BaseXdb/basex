package org.basex.query.value.node;

import static org.basex.util.Token.*;

import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Node builder.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FBuilder {
  /** Empty namespace array. */
  private static final Atts NO_NAMESPACES = new Atts(0);
  /** Empty node array. */
  private static final FNode[] NO_NODES = {};

  /** Parent node (can be {@code null}). */
  public FNode root;
  /** Namespaces (can be {@code null}). */
  public Atts namespaces;
 /** Attributes (can be {@code null}). */
  public ANodeList attributes;
  /** Children (can be {@code null}). */
  public ANodeList children;

  /**
   * Constructor.
   */
  public FBuilder() { }

  /**
   * Constructor.
   * @param root root node
   */
  FBuilder(final FNode root) {
    this.root = root;
  }

  /**
   * Finishes the node construction.
   * @return constructed node
   */
  public FNode finish() {
    final Atts ns = namespaces != null ? namespaces.optimize() : NO_NAMESPACES;
    final ANode[] at = attributes != null ? attributes.finish() : NO_NODES;
    final ANode[] ch = children != null ? children.finish() : NO_NODES;
    return root instanceof FElem ? ((FElem) root).finish(ns, at, ch) : ((FDoc) root).finish(ch);
  }

  /**
   * Finalizes and adds a node.
   * @param builder builder
   * @return self reference
   */
  public FBuilder add(final FBuilder builder) {
    return add(builder.finish());
  }

  /**
   * Adds a node.
   * @param node node to be added
   * @return self reference
   */
  public FBuilder add(final ANode node) {
    final boolean attr = node.type == NodeType.ATTRIBUTE;
    ANodeList nodes = attr ? attributes : children;
    if(nodes == null) {
      nodes = new ANodeList();
      if(attr) attributes = nodes;
      else children = nodes;
    }
    nodes.add(node);
    node.parent(root);
    return this;
  }

  /**
   * Creates and adds a text node if the specified value is not {@code null} and non-empty.
   * @param value value of text node (can be {@code null})
   * @return self reference
   */
  public FBuilder add(final byte[] value) {
    return value != null && value.length != 0 ? add(new FTxt(value)) : this;
  }

  /**
   * Creates and adds a text node if the specified value is not {@code null} and non-empty.
   * @param value value of text node (can be {@code null})
   * @return self reference
   */
  public FBuilder add(final Object value) {
    return value != null ? add(token(value.toString())) : this;
  }

  /**
   * Creates and adds an attribute if the specified value is not {@code null}.
   * @param name attribute name
   * @param value attribute value (can be {@code null})
   * @return self reference
   */
  public FBuilder add(final QNm name, final byte[] value) {
    return value != null ? add(new FAttr(name, value)) : this;
  }

  /**
   * Creates and adds an attribute if the specified value is not {@code null}.
   * @param name attribute name
   * @param value attribute value (can be {@code null})
   * @return self reference
   */
  public FBuilder add(final QNm name, final Object value) {
    return value != null ? add(name, token(value.toString())) : this;
  }

  /**
   * Adds a namespace declaration.
   * @param prefix prefix
   * @param uri URI
   * @return self reference
   */
  public FBuilder addNS(final byte[] prefix, final byte[] uri) {
    if(namespaces == null) namespaces = new Atts();
    namespaces.add(prefix, uri);
    return this;
  }

  /**
   * Adds a namespace declaration for the QName of this element.
   * @return self reference
   */
  public FBuilder declareNS() {
    final QNm name = root.qname();
    return addNS(name.prefix(), name.uri());
  }

  /**
   * Indicates if the node lists are empty.
   * @return result of check
   */
  public boolean isEmpty() {
    return attributes == null && children == null;
  }
}
