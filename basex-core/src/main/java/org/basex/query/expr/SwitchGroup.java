package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Group of switch cases (case ... case ... return ...).
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class SwitchGroup extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param exprs return expression (placed first) and cases (default branch has 0 cases)
   */
  public SwitchGroup(final InputInfo info, final Expr... exprs) {
    super(info, SeqType.ITEM_ZM, exprs);
  }

  @Override
  public void checkUp() throws QueryException {
    final int el = exprs.length;
    for(int e = 1; e < el; ++e) checkNoUp(exprs[e]);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    // compile and simplify branches
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      try {
        exprs[e] = exprs[e].compile(cc);
      } catch(final QueryException ex) {
        // replace original expression with error
        exprs[e] = cc.error(ex, exprs[e]);
      }
    }
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final int el = exprs.length;
    for(int e = 1; e < el; e++) exprs[e] = exprs[e].simplifyFor(AtomType.ATM, cc);
    return this;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new SwitchGroup(info, copyAll(cc, vm, exprs));
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    boolean changed = false;
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      Expr nw;
      try {
        nw = exprs[e].inline(var, ex, cc);
      } catch(final QueryException qe) {
        nw = cc.error(qe, exprs[e]);
      }
      if(nw != null) {
        exprs[e] = nw;
        changed = true;
      }
    }
    return changed ? optimize(cc) : null;
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
    final int el = exprs.length;
    for(int e = 1; e < el; e++) {
      if((all = all.plus(exprs[e].count(var))) == VarUsage.MORE_THAN_ONCE) break;
    }
    return all;
  }

  @Override
  public int exprSize() {
    int size = 0;
    for(final Expr expr : exprs) size += expr.exprSize();
    return size;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof SwitchGroup && super.equals(obj);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final int el = exprs.length;
    for(int e = 1; e < el; ++e) sb.append(' ').append(CASE).append(' ').append(exprs[e]);
    if(el == 1) sb.append(' ').append(DEFAULT);
    return sb.append(' ').append(RETURN).append(' ').append(exprs[0]).toString();
  }
}
