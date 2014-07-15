package org.basex.query.value.node;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Namespace node.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNames extends FNode {
  /** Namespace name. */
  private final byte[] name;

  /**
   * Default constructor.
   * @param n name
   * @param v value
   */
  public FNames(final byte[] n, final byte[] v) {
    super(NodeType.NSP);
    name = n;
    value = v;
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
  public FNode copy() {
    return new FNames(name, value);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, name, VAL, value));
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder().add(' ').add(XMLNS);
    if(name.length != 0) tb.add(':').add(name);
    return tb.add("=\"").add(Token.string(value).replaceAll("\"", "\"\"")).
        add('"').toString();
  }
}
