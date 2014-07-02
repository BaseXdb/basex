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
    final Expr[] tmp = new Expr[catches.length + 1];
    tmp[0] = expr;
    for(int c = 0; c < catches.length; ++c) tmp[c + 1] = catches[c].expr;
    checkAllUp(tmp);
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    try {
      super.compile(ctx, scp);
      if(expr.isValue()) return optPre(expr, ctx);
    } catch(final QueryException ex) {
      if(!ex.isCatchable()) throw ex;
      for(final Catch c : catches) {
        if(c.matches(ex)) {
          // found a matching clause, compile and inline error message
          return optPre(c.compile(ctx, scp).asExpr(ex, ctx, scp), ctx);
        }
      }
      throw ex;
    }

    for(final Catch c : catches) c.compile(ctx, scp);
    type = expr.type();
    for(final Catch c : catches)
      if(!c.expr.isFunction(Function.ERROR)) type = type.union(c.type());
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    // don't catch errors from error handlers
    try {
      return ctx.value(expr);
    } catch(final QueryException ex) {
      if(!ex.isCatchable()) throw ex;
      for(final Catch c : catches) if(c.matches(ex)) return c.value(ctx, ex);
      throw ex;
    }
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.maximum(v, catches).plus(expr.count(v));
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp, final Var v, final Expr e)
      throws QueryException {

    boolean change = false;
    try {
      final Expr sub = expr.inline(ctx, scp, v, e);
      if(sub != null) {
        if(sub.isValue()) return optPre(sub, ctx);
        expr = sub;
        change = true;
      }
    } catch(final QueryException qe) {
      if(!qe.isCatchable()) throw qe;
      for(final Catch c : catches) {
        if(c.matches(qe)) {
          // found a matching clause, inline variable and error message
          final Catch nw = c.inline(ctx, scp, v, e);
          return optPre((nw == null ? c : nw).asExpr(qe, ctx, scp), ctx);
        }
      }
      throw qe;
    }

    for(final Catch c : catches) change |= c.inline(ctx, scp, v, e) != null;
    return change ? this : null;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new Try(info, expr.copy(ctx, scp, vs), Arr.copyAll(ctx, scp, vs, catches));
  }

  @Override
  public boolean has(final Flag flag) {
    for(final Catch c : catches) if(c.has(flag)) return true;
    return super.has(flag);
  }

  @Override
  public boolean removable(final Var v) {
    for(final Catch c : catches) if(!c.removable(v)) return false;
    return super.removable(v);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), expr, catches);
  }

  @Override
  public void markTailCalls(final QueryContext ctx) {
    for(final Catch c : catches) c.markTailCalls(ctx);
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
