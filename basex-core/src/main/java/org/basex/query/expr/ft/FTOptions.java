package org.basex.query.expr.ft;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTOptions expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FTOptions extends FTExpr {
  /** FTOptions. */
  private final FTOpt opt;

  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   * @param opt full-text options
   */
  public FTOptions(final InputInfo info, final FTExpr expr, final FTOpt opt) {
    super(info, expr);
    this.opt = opt;
  }

  @Override
  public FTExpr compile(final CompileContext cc) throws QueryException {
    final QueryContext qc = cc.qc;
    final FTOpt tmp = qc.ftOpt();
    qc.ftOpt(opt.assign(tmp));
    final Value value = qc.focus.value;
    try {
      if(opt.sw != null && value != null && value.data() != null) opt.sw.compile(value.data());
      return exprs[0].compile(cc);
    } finally {
      qc.ftOpt(tmp);
    }
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
  public FTExpr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new FTOptions(info, exprs[0].copy(cc, vm), new FTOpt().assign(opt)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof FTOptions && opt.equals(((FTOptions) obj).opt) &&
        super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), opt, exprs[0]);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(exprs[0]).token(opt);
  }
}
