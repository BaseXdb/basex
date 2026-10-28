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
    super(NodeType.NAMESPACE);
    this.name = name;
    this.value = value;
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
    return materialized(test, funcs, ii) ? this : new FNSpace(name, value);
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
