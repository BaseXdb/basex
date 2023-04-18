package org.basex.query.value.node;

import static org.basex.query.QueryText.*;

import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.constr.*;
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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FDoc extends FNode {
  /** Child nodes. */
  private ANode[] children = EMPTY;
  /** Base URI. */
  private final byte[] uri;

  /**
   * Constructor.
   * @param uri base uri
   */
  public FDoc(final byte[] uri) {
    super(NodeType.DOCUMENT_NODE);
    this.uri = uri;
  }

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
   * Constructor for DOM nodes.
   * Originally provided by Erdal Karaca.
   * @param uri base uri
   * @param doc DOM node
   */
  public FDoc(final byte[] uri, final DocumentFragment doc) {
    this(uri);
    final FBuilder builder = new FBuilder();
    children(doc, builder, new TokenMap());
    finish(builder);
  }

  /**
   * Assigns nodes.
   * @param nodes nodes to be assigned
   * @return self reference
   */
  public FDoc finish(final FBuilder nodes) {
    children = nodes.children == null ? FNode.EMPTY : nodes.children.finish();
    for(final ANode child : children) child.parent(this);
    return this;
  }

  @Override
  public byte[] string() {
    return string(children);
  }

  @Override
  public BasicNodeIter childIter() {
    return ANodeList.iter(children);
  }

  @Override
  public boolean hasChildren() {
    return children.length != 0;
  }

  @Override
  public byte[] baseURI() {
    return uri;
  }

  @Override
  public FNode materialize(final Predicate<Data> test, final InputInfo ii, final QueryContext qc)
      throws QueryException {

    if(materialized(test, ii)) return this;

    final FBuilder doc = new FBuilder(new FDoc(uri));
    for(final ANode child : children) doc.add(child.materialize(test, ii, qc));
    return doc.finish();
  }

  @Override
  public byte[] xdmInfo() {
    return new ByteList().add(typeId().asByte()).add(uri).add(0).finish();
  }

  @Override
  public ID typeId() {
    // check if a document has a single element as child
    return (children.length == 1 && children[0].type == NodeType.ELEMENT ?
      NodeType.DOCUMENT_NODE_ELEMENT : type).id();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof FDoc)) return false;
    final FDoc f = (FDoc) obj;
    return children.equals(f.children) && Token.eq(uri, f.uri) && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, BASE, uri));
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(DOCUMENT).brace(uri.length == 0 ? DOTS : QueryString.toQuoted(uri));
  }
}
