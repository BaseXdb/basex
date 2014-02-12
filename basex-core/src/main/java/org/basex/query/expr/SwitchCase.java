package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Single case of a switch expression.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class SwitchCase extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param e return expression (placed first) and cases
   */
  public SwitchCase(final InputInfo ii, final Expr... e) {
    super(ii, e);
  }

  @Override
  public void checkUp() throws QueryException {
    final int es = expr.length;
    for(int e = 1; e < es; ++e) checkNoUp(expr[e]);
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) {
    // compile and simplify branches
    final int es = expr.length;
    for(int e = 0; e < es; e++) {
      try {
        expr[e] = expr[e].compile(ctx, scp);
      } catch(final QueryException ex) {
        // replace original expression with error
        expr[e] = FNInfo.error(ex, expr[e].type());
      }
    }
    return this;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new SwitchCase(info, copyAll(ctx, scp, vs, expr));
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp, final Var v,
      final Expr e) throws QueryException {
    boolean change = false;
    final int es = expr.length;
    for(int i = 0; i < es; i++) {
      Expr nw;
      try {
        nw = expr[i].inline(ctx, scp, v, e);
      } catch(final QueryException qe) {
        nw = FNInfo.error(qe, expr[i].type());
      }
      if(nw != null) {
        expr[i] = nw;
        change = true;
      }
    }
    return change ? optimize(ctx, scp) : null;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final int es = expr.length;
    for(int e = 1; e < es; ++e) sb.append(' ' + CASE + ' ' + expr[e]);
    if(es == 1) sb.append(' ' + DEFAULT);
    sb.append(' ' + RETURN + ' ' + expr[0]);
    return sb.toString();
  }

  /**
   * {@inheritDoc}
   * This method counts only the occurrences in the return expression.
   */
  @Override
  public VarUsage count(final Var v) {
    return expr[0].count(v);
  }

  /**
   * Checks how often a variable is used in this expression.
   * This method counts only the occurrences in the case expressions.
   * @param v variable to look for
   * @return number of occurrences
   */
  VarUsage countCases(final Var v) {
    VarUsage all = VarUsage.NEVER;
    final int es = expr.length;
    for(int i = 1; i < es; i++)
      if((all = all.plus(expr[i].count(v))) == VarUsage.MORE_THAN_ONCE) break;
    return all;
  }

  @Override
  public int exprSize() {
    int sz = 0;
    for(final Expr e : expr) sz += e.exprSize();
    return sz;
  }
}
