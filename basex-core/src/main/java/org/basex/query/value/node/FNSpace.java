package org.basex.query.value.node;

import static org.basex.query.QueryText.*;

import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Namespace node.
 *
 * @author BaseX Team 2005-24, BSD License
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
    super(NodeType.NAMESPACE_NODE);
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
  public FNSpace materialize(final Predicate<Data> test, final InputInfo ii,
      final QueryContext qc) {
    return materialized(test, ii) ? this : new FNSpace(name, value);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof FNSpace)) return false;
    final FNSpace f = (FNSpace) obj;
    return Token.eq(name, ((FNSpace) obj).name) && Token.eq(value, f.value) && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name, VALUEE, value));
  }

  @Override
  public void toString(final QueryString qs) {
    final TokenBuilder tb = new TokenBuilder().add(Token.XMLNS);
    if(name.length != 0) tb.add(':').add(name);
    tb.add('=').add(QueryString.toQuoted(value));
    qs.token(tb.finish());
  }
}
