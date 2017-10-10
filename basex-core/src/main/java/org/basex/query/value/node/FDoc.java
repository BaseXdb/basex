package org.basex.query.value.node;

import static org.basex.query.QueryText.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.Type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.w3c.dom.*;

/**
 * Document node fragment.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FDoc extends FNode {
  /** Child nodes. */
  private final ANodeList children;
  /** Base URI. */
  private final byte[] uri;

  /**
   * Constructor.
   */
  public FDoc() {
    this(Token.EMPTY);
  }

  /**
   * Constructor.
   * @param uri base uri
   */
  public FDoc(final String uri) {
    this(Token.token(uri));
  }

  /**
   * Constructor.
   * @param uri base uri
   */
  public FDoc(final byte[] uri) {
    this(new ANodeList(), uri);
  }

  /**
   * Constructor.
   * @param children children
   * @param uri base uri
   */
  public FDoc(final ANodeList children, final byte[] uri) {
    super(NodeType.DOC);
    this.children = children;
    this.uri = uri;
    // update parent references
    for(final ANode n : children) n.parent(this);
  }

  @Override
  public FDoc optimize() {
    // update parent references
    for(final ANode n : children) n.parent(this);
    return this;
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
   * @param bu base uri
   */
  public FDoc(final DocumentFragment doc, final byte[] bu) {
    this(bu);
    final Node elem = doc.getFirstChild();
    if(elem instanceof Element) children.add(new FElem((Element) elem, this, new TokenMap()));
  }

  @Override
  public byte[] string() {
    return string(children);
  }

  @Override
  public BasicNodeIter children() {
    return iter(children);
  }

  @Override
  public boolean hasChildren() {
    return !children.isEmpty();
  }

  @Override
  public byte[] baseURI() {
    return uri;
  }

  @Override
  public FNode deepCopy(final MainOptions options) {
    return new FDoc(children, uri).optimize();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof FDoc)) return false;
    final FDoc f = (FDoc) obj;
    return children.equals(f.children) && Token.eq(uri, f.uri) && super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(QueryText.BASE, uri, TYP, seqType()));
  }

  @Override
  public byte[] xdmInfo() {
    return new ByteList().add(typeId().asByte()).add(uri).add(0).finish();
  }

  @Override
  public ID typeId() {
    // check if a document has a single element as child
    return (children.size() == 1 && children.get(0).type == NodeType.ELM
        ? NodeType.DEL : type).id();
  }

  @Override
  public String toString() {
    return new TokenBuilder(QueryText.DOCUMENT).add(" { ").add(uri).add(" }").toString();
  }
}
