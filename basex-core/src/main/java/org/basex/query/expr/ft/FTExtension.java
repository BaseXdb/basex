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
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class FTExtension extends FTExpr {
  /** Pragmas. */
  private final Pragma[] pragmas;

  /**
   * Constructor.
   * @param info input info
   * @param pragmas pragmas
   * @param expr enclosed FTSelection
   */
  public FTExtension(final InputInfo info, final Pragma[] pragmas, final FTExpr expr) {
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
  public FTExpr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Pragma[] prag = pragmas.clone();
    final int pl = prag.length;
    for(int i = 0; i < pl; i++) prag[i] = prag[i].copy();
    return copyType(new FTExtension(info, prag, exprs[0].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof FTExtension &&
        Array.equals(pragmas, ((FTExtension) obj).pragmas) && super.equals(obj);
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
