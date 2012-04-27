package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.util.*;

/**
 * Instance test.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Instance extends Single {
  /** Sequence type. */
  private final SeqType seq;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param s sequence type
   */
  public Instance(final InputInfo ii, final Expr e, final SeqType s) {
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
  public Bln item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return Bln.get(seq.instance(ctx.value(expr)));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(TYP, seq), expr);
  }

  @Override
  public String toString() {
    return Util.info("% instance of %", expr, seq);
  }
}
