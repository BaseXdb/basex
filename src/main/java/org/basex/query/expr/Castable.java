package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.util.*;

/**
 * Castable expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Castable extends Single {
  /** Instance. */
  private final SeqType seq;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param s sequence type
   */
  public Castable(final InputInfo ii, final Expr e, final SeqType s) {
    super(ii, e);
    seq = s;
    type = SeqType.BLN;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    return checkUp(expr, ctx).isValue() ? preEval(ctx) : this;
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii) {
    try {
      seq.cast(expr.item(ctx, ii), true, ctx, ii, this);
      return Bln.TRUE;
    } catch(final QueryException ex) {
      return Bln.FALSE;
    }
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(TYP, seq), expr);
  }

  @Override
  public String toString() {
    return expr + " " + CASTABLE + ' ' + AS + ' ' + seq;
  }
}
