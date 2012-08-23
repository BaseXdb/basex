package org.basex.query.value.node;

import static org.basex.query.QueryText.*;

import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.w3c.dom.*;

/**
 * Document node fragment.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FDoc extends FNode {
  /** Child nodes. */
  private final ANodeList children;
  /** Base URI. */
  private final byte[] base;

  /**
   * Constructor.
   */
  public FDoc() {
    this(Token.EMPTY);
  }

  /**
   * Constructor.
   * @param b base uri
   */
  public FDoc(final byte[] b) {
    this(new ANodeList(), b);
  }

  /**
   * Constructor.
   * @param ch children
   * @param b base uri
   */
  public FDoc(final ANodeList ch, final byte[] b) {
    super(NodeType.DOC);
    children = ch;
    base = b;
    // update parent references
    final long ns = ch.size();
    for(int n = 0; n < ns; ++n) ch.get(n).parent(this);
  }

  /**
   * Adds a node and updates its parent reference.
   * @param node node to be added
   * @return self reference
   */
  public FDoc add(final ANode node) {
    children.add(node);
    node.parent(this);
    return this;
  }

  /**
   * Constructor for DOM nodes.
   * Originally provided by Erdal Karaca.
   * @param doc DOM node
   * @param b base uri
   */
  public FDoc(final DocumentFragment doc, final byte[] b) {
    this(b);
    final Node elem = doc.getFirstChild();
    if(elem != null && elem instanceof Element)
      children.add(new FElem((Element) elem, this, new TokenMap()));
    // [LW] DOM: DocumentFragment != Document, possibly multiple roots
  }

  @Override
  public byte[] string() {
    return string(children);
  }

  @Override
  public AxisMoreIter children() {
    return iter(children);
  }

  @Override
  public boolean hasChildren() {
    return children.size() != 0;
  }

  @Override
  public byte[] baseURI() {
    return base;
  }

  @Override
  public FDoc copy() {
    return new FDoc(children, base);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(BASE, base));
  }

  @Override
  public byte[] xdmInfo() {
    return new ByteList().add(typeId()).add(base).add(0).toArray();
  }

  @Override
  public byte typeId() {
    // check if a document has a single element as child
    return children.size() == 1 && children.get(0).type == NodeType.ELM ?
      NodeType.DEL.id() : type.id();
  }

  @Override
  public String toString() {
    return Util.info("%(%)", type, base);
  }
}
