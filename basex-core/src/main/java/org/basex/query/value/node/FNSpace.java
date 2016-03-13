package org.basex.query.value.node;

import static org.basex.query.QueryText.*;

import org.basex.core.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Namespace node.
 *
 * @author BaseX Team 2005-16, BSD License
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
  public void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, name, VAL, value));
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder().add(' ').add(Token.XMLNS);
    if(name.length != 0) tb.add(':').add(name);
    return tb.add('=').add(Atm.toString(value)).toString();
  }
}
