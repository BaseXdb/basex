package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * If expression.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class If extends Arr {
  /** If expression. */
  private Expr cond;

  /**
   * Constructor.
   * @param ii input info
   * @param c condition
   * @param t then clause
   * @param e else clause
   */
  public If(final InputInfo ii, final Expr c, final Expr t, final Expr e) {
    super(ii, t, e);
    cond = c;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(cond);
    checkAllUp(expr);
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    cond = cond.compile(ctx, scp).compEbv(ctx);
    // static condition: return branch in question
    if(cond.isValue()) return optPre(eval(ctx).compile(ctx, scp), ctx);

    // compile and simplify branches
    final int es = expr.length;
    for(int e = 0; e < es; e++) {
      try {
        expr[e] = expr[e].compile(ctx, scp);
      } catch(final QueryException ex) {
        // replace original expression with error
        expr[e] = FNInfo.error(ex, type);
      }
    }

    return optimize(ctx, scp);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    // static condition: return branch in question
    if(cond.isValue()) return optPre(eval(ctx), ctx);

    // if A then B else B -> B (errors in A will be ignored)
    if(expr[0].sameAs(expr[1])) return optPre(expr[0], ctx);

    // if not(A) then B else C -> if A then C else B
    if(cond.isFunction(Function.NOT)) {
      ctx.compInfo(OPTWRITE, this);
      cond = ((Arr) cond).expr[0];
      final Expr tmp = expr[0];
      expr[0] = expr[1];
      expr[1] = tmp;
    }

    // if A then true() else false() -> boolean(A)
    if(expr[0] == Bln.TRUE && expr[1] == Bln.FALSE) {
      ctx.compInfo(OPTWRITE, this);
      return compBln(cond, info);
    }

    // if A then false() else true() -> not(A)
    // if A then B else true() -> not(A) or B
    if(expr[0].type().eq(SeqType.BLN) && expr[1] == Bln.TRUE) {
      ctx.compInfo(OPTWRITE, this);
      final Expr e = Function.NOT.get(null, info, cond);
      return expr[0] == Bln.FALSE ? e : new Or(info, e, expr[0]);
    }

    type = expr[0].type().union(expr[1].type());
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return ctx.iter(eval(ctx));
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    return ctx.value(eval(ctx));
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return eval(ctx).item(ctx, info);
  }

  /**
   * Evaluates the condition and returns the matching expression.
   * @param ctx query context
   * @return resulting expression
   * @throws QueryException query exception
   */
  private Expr eval(final QueryContext ctx) throws QueryException {
    return expr[cond.ebv(ctx, info).bool(info) ? 0 : 1];
  }

  @Override
  public boolean has(final Flag flag) {
    return cond.has(flag) || super.has(flag);
  }

  @Override
  public boolean removable(final Var v) {
    return cond.removable(v) && super.removable(v);
  }

  @Override
  public VarUsage count(final Var v) {
    return cond.count(v).plus(VarUsage.maximum(v, expr));
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    final Expr sub = cond.inline(ctx, scp, v, e);
    if(sub != null) cond = sub;
    boolean te = false;
    final int es = expr.length;
    for(int i = 0; i < es; i++) {
      Expr nw;
      try {
        nw = expr[i].inline(ctx, scp, v, e);
      } catch(final QueryException qe) {
        nw = FNInfo.error(qe, type);
      }
      if(nw != null) {
        expr[i] = nw;
        te = true;
      }
    }
    return te || sub != null ? optimize(ctx, scp) : null;
  }

  @Override
  public If copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new If(info, cond.copy(ctx, scp, vs),
        expr[0].copy(ctx, scp, vs), expr[1].copy(ctx, scp, vs)));
  }

  @Override
  public Expr indexEquivalent(final IndexCosts ic) throws QueryException {
    final int es = expr.length;
    for(int e = 0; e < es; ++e) expr[e] = expr[e].indexEquivalent(ic);
    return this;
  }

  @Override
  public boolean isVacuous() {
    return expr[0].isVacuous() && expr[1].isVacuous();
  }

  @Override
  public void markTailCalls(final QueryContext ctx) {
    expr[0].markTailCalls(ctx);
    expr[1].markTailCalls(ctx);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), cond, expr);
  }

  @Override
  public String toString() {
    return IF + '(' + cond + ") " + THEN + ' ' + expr[0] + ' ' + ELSE + ' ' + expr[1];
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return cond.accept(visitor) && super.accept(visitor);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr e : expr) sz += e.exprSize();
    return sz + cond.exprSize();
  }

  @Override
  public Expr typeCheck(final TypeCheck tc, final QueryContext ctx, final VarScope scp)
      throws QueryException {
    for(int i = 0; i < expr.length; i++) expr[i] = tc.check(expr[i], ctx, scp);
    return optimize(ctx, scp);
  }
}
