package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Castable expression.
 *
 * @author BaseX Team 2005-13, BSD License
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
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);
    return expr.isValue() ? preEval(ctx) : this;
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii) {
    try {
      seq.cast(expr.item(ctx, ii), ctx, ii, this);
      return Bln.TRUE;
    } catch(final QueryException ex) {
      return Bln.FALSE;
    }
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new Castable(info, expr.copy(ctx, scp, vs), seq);
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
