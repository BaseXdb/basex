package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Pragma extension.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class Extension extends Single {
  /** Pragmas of the ExtensionExpression. */
  private final Pragma[] pragmas;

  /**
   * Constructor.
   * @param info input info
   * @param pragmas pragmas
   * @param expr enclosed expression
   */
  public Extension(final InputInfo info, final Pragma[] pragmas, final Expr expr) {
    super(info, expr);
    this.pragmas = pragmas;
  }

  @Override
  public void checkUp() throws QueryException {
    expr.checkUp();
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    try {
      for(final Pragma p : pragmas) p.init(qc, info);
      expr = expr.compile(qc, scp);
      type = expr.type();
      size = expr.size();
    } finally {
      for(final Pragma p : pragmas) p.finish(qc);
    }
    return this;
  }

  @Override
  public ValueIter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    try {
      for(final Pragma p : pragmas) p.init(qc, info);
      return qc.value(expr);
    } finally {
      for(final Pragma p : pragmas) p.finish(qc);
    }
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Pragma[] prag = pragmas.clone();
    for(int i = 0; i < prag.length; i++) prag[i] = prag[i].copy();
    return copyType(new Extension(info, prag, expr.copy(qc, scp, vs)));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), pragmas, expr);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Pragma p : pragmas) sb.append(p).append(' ');
    return sb.append(BRACE1 + ' ').append(expr).append(' ').append(BRACE2).toString();
  }
}
