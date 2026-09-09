package org.basex.query.value.node;

import static org.basex.query.QueryText.*;

import java.io.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Namespace node.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FNSpace extends FNode {
  /** Namespace name. */
  private final byte[] name;
  /** Namespace value. */
  private final byte[] value;

  /**
   * Default constructor.
   * @param name name
   * @param value value
   */
  public FNSpace(final byte[] name, final byte[] value) {
    this(name, value, null);
  }

  /**
   * Constructor with a parent element.
   * @param name name
   * @param value value
   * @param parent parent element (can be {@code null})
   */
  FNSpace(final byte[] name, final byte[] value, final XNode parent) {
    super(NodeType.NAMESPACE, parent);
    this.name = name;
    this.value = value;
  }

  @Override
  public boolean is(final GNode node) {
    if(this == node) return true;
    // namespace nodes are generated on demand: compare parent element and prefix
    final XNode par = parent();
    return par != null && node instanceof final FNSpace nspace && Token.eq(name, nspace.name) &&
        par.is(nspace.parent());
  }

  @Override
  public int compare(final GNode node) {
    return parent() != null && node instanceof final XNode xnode ?
      compareNSpace(this, xnode) : super.compare(node);
  }

  @Override
  public byte[] id() {
    final XNode par = parent();
    return par == null ? super.id() : Token.concat(par.id(), 'n', name);
  }

  @Override
  public QNm qname() {
    return new QNm(name);
  }

  @Override
  public byte[] name() {
    return name;
  }

  @Override
  public byte[] string() {
    return value;
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    // namespace nodes cannot be parsed from their serialization
    out.writeToken(name);
    out.writeToken(value);
  }

  @Override
  public FNSpace materialize(final Predicate<Data> test, final boolean funcs, final InputInfo ii,
      final QueryContext qc) {
    final XNode par = parent();
    return (par != null ? par : this).materialized(test, funcs, ii) ? this :
      new FNSpace(name, value);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name, VALUEE, value));
  }

  @Override
  public void toString(final QueryString qs) {
    final byte[] attr = name.length != 0 ? Token.concat(Token.XMLNS_COLON, name) : Token.XMLNS;
    qs.token(QueryString.toAttribute(attr, value));
  }
}
