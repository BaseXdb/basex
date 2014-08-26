package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * FTExtensionSelection expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class FTExtensionSelection extends FTExpr {
  /** Pragmas. */
  private final Pragma[] pragmas;

  /**
   * Constructor.
   * @param info input info
   * @param pragmas pragmas
   * @param expr enclosed FTSelection
   */
  public FTExtensionSelection(final InputInfo info, final Pragma[] pragmas, final FTExpr expr) {
    super(info, expr);
    this.pragmas = pragmas;
  }

  @Override
  public FTNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return exprs[0].item(qc, info);
  }

  @Override
  public FTIter iter(final QueryContext qc) throws QueryException {
    return exprs[0].iter(qc);
  }

  @Override
  public FTExpr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Pragma[] prag = pragmas.clone();
    for(int i = 0; i < prag.length; i++) prag[i] = prag[i].copy();
    return copyType(new FTExtensionSelection(info, prag, exprs[0].copy(qc, scp, vs)));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), pragmas, exprs);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Pragma p : pragmas) sb.append(p).append(' ');
    return sb.append(CURLY1 + ' ').append(exprs[0]).append(' ').append(CURLY2).toString();
  }
}
