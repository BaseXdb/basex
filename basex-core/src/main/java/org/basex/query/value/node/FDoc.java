package org.basex.query.value.node;

import static org.basex.query.QueryText.*;

import java.util.function.*;

import org.basex.data.*;
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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FDoc extends FNode {
  /** Base URI. */
  private final byte[] uri;
  /** Children. */
  private ANode[] children;

  /**
   * Constructor.
   * @param uri base URI
   */
  private FDoc(final byte[] uri) {
    super(NodeType.DOCUMENT_NODE);
    this.uri = uri;
  }

  /**
   * Creates a document builder.
   * @param uri base URI
   * @return document builder
   */
  public static FBuilder build(final byte[] uri) {
    return new FBuilder(new FDoc(uri));
  }

  /**
   * Creates a document builder.
   * @return document builder
   */
  public static FBuilder build() {
    return build(Token.EMPTY);
  }

  /**
   * Creates a document builder for DOM nodes.
   * Originally provided by Erdal Karaca.
   * @param uri base uri
   * @param doc DOM node
   * @return document builder
   */
  public static FBuilder build(final String uri, final DocumentFragment doc) {
    final FBuilder builder = build(Token.token(uri));
    children(doc, builder, new TokenMap());
    return builder;
  }

  /**
   * Finalizes the node.
   * @param ch children
   * @return self reference
   */
  FDoc finish(final ANode[] ch) {
    children = ch;
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

    final FBuilder doc = build(uri);
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
