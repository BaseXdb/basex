package org.basex.query.expr.constr;

import static org.basex.util.Token.*;

import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Node builder.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FBuilder {
  /** Parent node (can be {@code null}). */
  public FNode root;
  /** Namespaces (can be {@code null}). */
  public Atts namespaces;
 /** Attributes (can be {@code null}). */
  public ANodeList attributes;
  /** Children (can be {@code null}). */
  public ANodeList children;
  /** Finished flag. */
  public boolean finished;

  /**
   * Constructor.
   */
  public FBuilder() {
  }

  /**
   * Constructor.
   * @param root root node
   */
  public FBuilder(final FNode root) {
    this.root = root;
  }

  /**
   * Finishes the node construction.
   * @return constructed node
   */
  public FNode finish() {
    if(!finished) {
      finished = true;
      if(root instanceof FElem) return ((FElem) root).finish(this);
      if(root instanceof FDoc) return ((FDoc) root).finish(this);
    }
    throw Util.notExpected("Node building has already been finished.");
  }

  /**
   * Adds a node.
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
    if(finished) throw Util.notExpected("Node building has already been finished.");

    final boolean attr = node.type == NodeType.ATTRIBUTE;
    ANodeList nodes = attr ? attributes : children;
    if(nodes == null) {
      nodes = new ANodeList();
      if(attr) attributes = nodes;
      else children = nodes;
    }
    nodes.add(node);
    return this;
  }

  /**
   * Creates and adds a text node if the specified value is not empty.
   * @param text value of text node
   * @return self reference
   */
  public FBuilder add(final byte[] text) {
    if(text.length != 0) add(new FTxt(text));
    return this;
  }
  /**
   * Creates and adds a text node if the specified value is not empty.
   * @param text value of text node
   * @return self reference
   */
  public FBuilder add(final String text) {
    return add(token(text));
  }

  /**
   * Adds an attribute and updates its parent reference.
   * @param nm attribute name
   * @param val attribute value
   * @return self reference
   */
  public FBuilder add(final String nm, final String val) {
    return add(token(nm), token(val));
  }

  /**
   * Adds an attribute and updates its parent reference.
   * @param nm attribute name
   * @param val attribute value
   * @return self reference
   */
  public FBuilder add(final byte[] nm, final String val) {
    return add(nm, token(val));
  }

  /**
   * Adds an attribute and updates its parent reference.
   * @param nm attribute name
   * @param val attribute value
   * @return self reference
   */
  public FBuilder add(final String nm, final byte[] val) {
    return add(token(nm), val);
  }

  /**
   * Adds an attribute and updates its parent reference.
   * @param nm attribute name
   * @param val attribute value
   * @return self reference
   */
  public FBuilder add(final byte[] nm, final byte[] val) {
    return add(new FAttr(nm, val));
  }

  /**
   * Adds an attribute and updates its parent reference.
   * @param nm attribute name
   * @param val attribute value
   * @return self reference
   */
  public FBuilder add(final QNm nm, final String val) {
    return add(nm, token(val));
  }

  /**
   * Adds an attribute and updates its parent reference.
   * @param nm attribute name
   * @param val attribute value
   * @return self reference
   */
  public FBuilder add(final QNm nm, final byte[] val) {
    return add(new FAttr(nm, val));
  }

  /**
   * Adds a namespace declaration.
   * @param name name
   * @param uri uri
   * @return self reference
   */
  public FBuilder addNS(final byte[] name, final byte[] uri) {
    if(namespaces == null) namespaces = new Atts();
    namespaces.add(name, uri);
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
