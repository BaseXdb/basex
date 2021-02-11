package org.basex.query.value.node;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Namespace node.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FNSpace extends FNode {
  /** Namespace name. */
  private final byte[] name;

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
  public FNSpace materialize(final QueryContext qc, final boolean copy) {
    return copy ? new FNSpace(name, value) : this;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof FNSpace && Token.eq(name, ((FNSpace) obj).name) &&
        super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name, VALUEE, value));
  }

  @Override
  public void plan(final QueryString qs) {
    final TokenBuilder tb = new TokenBuilder().add(Token.XMLNS);
    if(name.length != 0) tb.add(':').add(name);
    tb.add('=').add(QueryString.toQuoted(value));
    qs.token(tb.finish());
  }
}
