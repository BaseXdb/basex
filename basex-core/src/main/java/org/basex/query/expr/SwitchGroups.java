package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Switch case group (case ... case ... return ...).
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class SwitchGroups extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param exprs return expression (placed first) and cases (default branch has 0 cases)
   */
  public SwitchGroups(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public void checkUp() throws QueryException {
    final int es = exprs.length;
    for(int e = 1; e < es; ++e) checkNoUp(exprs[e]);
  }

  @Override
  public Expr compile(final CompileContext cc) {
    // compile and simplify branches
    final int es = exprs.length;
    for(int e = 0; e < es; e++) {
      try {
        exprs[e] = exprs[e].compile(cc);
      } catch(final QueryException ex) {
        // replace original expression with error
        exprs[e] = cc.error(ex, exprs[e]);
      }
    }
    return this;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new SwitchGroups(info, copyAll(cc, vm, exprs));
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    boolean change = false;
    final int es = exprs.length;
    for(int i = 0; i < es; i++) {
      Expr nw;
      try {
        nw = exprs[i].inline(var, ex, cc);
      } catch(final QueryException qe) {
        nw = cc.error(qe, exprs[i]);
      }
      if(nw != null) {
        exprs[i] = nw;
        change = true;
      }
    }
    return change ? optimize(cc) : null;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final int es = exprs.length;
    for(int e = 1; e < es; ++e) sb.append(' ').append(CASE).append(' ').append(exprs[e]);
    if(es == 1) sb.append(' ').append(DEFAULT);
    return sb.append(' ').append(RETURN).append(' ').append(exprs[0]).toString();
  }

  /**
   * {@inheritDoc}
   * This method counts only the occurrences in the return expression.
   */
  @Override
  public VarUsage count(final Var var) {
    return exprs[0].count(var);
  }

  /**
   * Checks how often a variable is used in this expression.
   * This method counts only the occurrences in the case expressions.
   * @param var variable to look for
   * @return number of occurrences
   */
  VarUsage countCases(final Var var) {
    VarUsage all = VarUsage.NEVER;
    final int es = exprs.length;
    for(int i = 1; i < es; i++)
      if((all = all.plus(exprs[i].count(var))) == VarUsage.MORE_THAN_ONCE) break;
    return all;
  }

  @Override
  public int exprSize() {
    int sz = 0;
    for(final Expr expr : exprs) sz += expr.exprSize();
    return sz;
  }
}
