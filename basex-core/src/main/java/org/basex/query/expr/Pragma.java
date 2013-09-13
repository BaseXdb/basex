package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Abstract pragma expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public abstract class Pragma extends ExprInfo {
  /** QName. */
  protected final QNm name;
  /** Pragma value. */
  protected final byte[] value;

  /**
   * Constructor.
   * @param n name of pragma
   * @param v optional value
   */
  public Pragma(final QNm n, final byte[] v) {
    name = n;
    value = v;
  }

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(VAL, value), name);
  }

  /**
   * Initializes the pragma expression.
   * @param ctx query context
   * @param info input info
   * @throws QueryException query exception
   */
  abstract void init(final QueryContext ctx, final InputInfo info) throws QueryException;

  /**
   * Finalizes the pragma expression.
   * @param ctx query context
   */
  abstract void finish(final QueryContext ctx);

  @Override
  public final String toString() {
    final TokenBuilder tb = new TokenBuilder(PRAGMA + ' ' + name + ' ');
    if(value.length != 0) tb.add(value).add(' ');
    return tb.add(PRAGMA2).toString();
  }

  /**
   * Creates a copy of this pragma.
   * @return copy
   */
  public abstract Pragma copy();
}
