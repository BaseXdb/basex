package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Project specific try/catch expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Try extends Single {
  /** Catch clauses. */
  private final Catch[] catches;

  /**
   * Constructor.
   * @param info input info
   * @param expr try expression
   * @param catches catch expressions
   */
  public Try(final InputInfo info, final Expr expr, final Catch[] catches) {
    super(info, expr);
    this.catches = catches;
  }

  @Override
  public void checkUp() throws QueryException {
    // check if no or all try/catch expressions are updating
    final int cl = catches.length;
    final Expr[] tmp = new Expr[cl + 1];
    tmp[0] = expr;
    for(int c = 0; c < cl; ++c) tmp[c + 1] = catches[c].expr;
    checkAllUp(tmp);
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    try {
      super.compile(qc, scp);
      if(expr.isValue()) return optPre(expr, qc);
    } catch(final QueryException ex) {
      if(!ex.isCatchable()) throw ex;
      for(final Catch c : catches) {
        if(c.matches(ex)) {
          // found a matching clause, compile and inline error message
          return optPre(c.compile(qc, scp).asExpr(ex, qc, scp), qc);
        }
      }
      throw ex;
    }

    for(final Catch c : catches) c.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) {
    seqType = expr.seqType();
    for(final Catch c : catches) {
      if(!c.expr.isFunction(Function.ERROR)) seqType = seqType.union(c.seqType());
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // don't catch errors from error handlers
    try {
      return qc.value(expr);
    } catch(final QueryException ex) {
      if(!ex.isCatchable()) throw ex;
      for(final Catch c : catches) if(c.matches(ex)) return c.value(qc, ex);
      throw ex;
    }
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.maximum(var, catches).plus(expr.count(var));
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {

    boolean change = false;
    try {
      final Expr sub = expr.inline(qc, scp, var, ex);
      if(sub != null) {
        if(sub.isValue()) return optPre(sub, qc);
        expr = sub;
        change = true;
      }
    } catch(final QueryException qe) {
      if(!qe.isCatchable()) throw qe;
      for(final Catch c : catches) {
        if(c.matches(qe)) {
          // found a matching clause, inline variable and error message
          final Catch nw = c.inline(qc, scp, var, ex);
          return optPre((nw == null ? c : nw).asExpr(qe, qc, scp), qc);
        }
      }
      throw qe;
    }

    for(final Catch c : catches) change |= c.inline(qc, scp, var, ex) != null;
    return change ? this : null;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Try(info, expr.copy(qc, scp, vs), Arr.copyAll(qc, scp, vs, catches));
  }

  @Override
  public boolean has(final Flag flag) {
    for(final Catch c : catches) if(c.has(flag)) return true;
    return super.has(flag);
  }

  @Override
  public boolean removable(final Var var) {
    for(final Catch c : catches) if(!c.removable(var)) return false;
    return super.removable(var);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), expr, catches);
  }

  @Override
  public void markTailCalls(final QueryContext qc) {
    for(final Catch c : catches) c.markTailCalls(qc);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("try { " + expr + " }");
    for(final Catch c : catches) sb.append(' ').append(c);
    return sb.toString();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && visitAll(visitor, catches);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr e : catches) sz += e.exprSize();
    return sz;
  }
}
