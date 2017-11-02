package org.basex.query.value.node;

import static org.basex.query.QueryText.*;

import org.basex.core.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Namespace node.
 *
 * @author BaseX Team 2005-17, BSD License
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
    super(NodeType.NSP);
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
  public FNode deepCopy(final MainOptions options) {
    return new FNSpace(name, value);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof FNSpace && Token.eq(name, ((FNSpace) obj).name) &&
        super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(NAME, name, VALUEE, value, TYPE, seqType()));
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(Token.XMLNS);
    if(name.length != 0) tb.add(':').add(name);
    return tb.add('=').add(toString(value)).toString();
  }
}
