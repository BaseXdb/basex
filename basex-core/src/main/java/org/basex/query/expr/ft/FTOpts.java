package org.basex.query.expr.ft;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTOptions expression.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class FTOpts extends FTExpr {
  /** FTOptions. */
  private final FTOpt opt;

  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   * @param opt ft options
   */
  public FTOpts(final InputInfo info, final FTExpr expr, final FTOpt opt) {
    super(info, expr);
    this.opt = opt;
  }

  @Override
  public FTExpr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    final FTOpt tmp = qc.ftOpt();
    qc.ftOpt(opt.copy(tmp));
    if(opt.sw != null && qc.value != null && qc.value.data() != null) opt.sw.comp(qc.value.data());
    exprs[0] = exprs[0].compile(qc, scp);
    qc.ftOpt(tmp);
    return exprs[0];
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), opt, exprs[0]);
  }

  @Override
  public String toString() {
    return exprs[0].toString() + opt;
  }

  @Override
  public FTNode item(final QueryContext qc, final InputInfo ii) {
    // shouldn't be called, as compile returns argument
    throw Util.notExpected();
  }

  @Override
  public FTIter iter(final QueryContext qc) {
    // shouldn't be called, as compile returns argument
    throw Util.notExpected();
  }

  @Override
  public FTExpr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new FTOpts(info, exprs[0].copy(qc, scp, vs), new FTOpt().copy(opt));
  }
}
