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
  /** Catches. */
  private final Catch[] ctch;

  /**
   * Constructor.
   * @param ii input info
   * @param t try expression
   * @param c catch expressions
   */
  public Try(final InputInfo ii, final Expr t, final Catch[] c) {
    super(ii, t);
    ctch = c;
  }

  @Override
  public void checkUp() throws QueryException {
    // check if no or all try/catch expressions are updating
    final Expr[] tmp = new Expr[ctch.length + 1];
    tmp[0] = expr;
    for(int c = 0; c < ctch.length; ++c) tmp[c + 1] = ctch[c].expr;
    checkAllUp(tmp);
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    try {
      super.compile(ctx, scp);
      if(expr.isValue()) return optPre(expr, ctx);
    } catch(final QueryException ex) {
      if(!ex.isCatchable()) throw ex;
      for(final Catch c : ctch) {
        if(c.matches(ex)) {
          // found a matching clause, compile and inline error message
          return optPre(c.compile(ctx, scp).asExpr(ex, ctx, scp), ctx);
        }
      }
      throw ex;
    }

    for(final Catch c : ctch) c.compile(ctx, scp);
    type = expr.type();
    for(final Catch c : ctch)
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
      for(final Catch c : ctch) if(c.matches(ex)) return c.value(ctx, ex);
      throw ex;
    }
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.maximum(v, ctch).plus(expr.count(v));
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
      for(final Catch c : ctch) {
        if(c.matches(qe)) {
          // found a matching clause, inline variable and error message
          return optPre(c.inline(ctx, scp, v, e).asExpr(qe, ctx, scp), ctx);
        }
      }
      throw qe;
    }

    for(final Catch c : ctch) change |= c.inline(ctx, scp, v, e) != null;
    return change ? this : null;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new Try(info, expr.copy(ctx, scp, vs), Arr.copyAll(ctx, scp, vs, ctch));
  }

  @Override
  public boolean has(final Flag flag) {
    for(final Catch c : ctch) if(c.has(flag)) return true;
    return super.has(flag);
  }

  @Override
  public boolean removable(final Var v) {
    for(final Catch c : ctch) if(!c.removable(v)) return false;
    return super.removable(v);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), expr, ctch);
  }

  @Override
  public void markTailCalls(final QueryContext ctx) {
    for(final Catch c : ctch) c.markTailCalls(ctx);
    expr.markTailCalls(ctx);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("try { " + expr + " }");
    for(final Catch c : ctch) sb.append(' ').append(c);
    return sb.toString();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && visitAll(visitor, ctch);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr e : ctch) sz += e.exprSize();
    return sz;
  }
}
