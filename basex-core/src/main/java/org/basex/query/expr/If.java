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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class If extends Arr {
  /** If expression. */
  private Expr cond;

  /**
   * Constructor.
   * @param info input info
   * @param cond condition
   * @param branch1 then branch
   * @param branch2 else branch
   */
  public If(final InputInfo info, final Expr cond, final Expr branch1, final Expr branch2) {
    super(info, branch1, branch2);
    this.cond = cond;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(cond);
    checkAllUp(exprs);
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    cond = cond.compile(ctx, scp).compEbv(ctx);
    // static condition: return branch in question
    if(cond.isValue()) return optPre(eval(ctx).compile(ctx, scp), ctx);

    // compile and simplify branches
    final int es = exprs.length;
    for(int e = 0; e < es; e++) {
      try {
        exprs[e] = exprs[e].compile(ctx, scp);
      } catch(final QueryException ex) {
        // replace original expression with error
        exprs[e] = FNInfo.error(ex, type);
      }
    }
    return optimize(ctx, scp);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    // static condition: return branch in question
    if(cond.isValue()) return optPre(eval(ctx), ctx);

    // if A then B else B -> B (errors in A will be ignored)
    if(exprs[0].sameAs(exprs[1])) return optPre(exprs[0], ctx);

    // if not(A) then B else C -> if A then C else B
    if(cond.isFunction(Function.NOT)) {
      ctx.compInfo(OPTWRITE, this);
      cond = ((Arr) cond).exprs[0];
      final Expr tmp = exprs[0];
      exprs[0] = exprs[1];
      exprs[1] = tmp;
    }

    // rewritings for constant booleans
    if(exprs[0].type().eq(SeqType.BLN) && exprs[1].type().eq(SeqType.BLN)) {
      final Expr a = cond, b = exprs[0], c = exprs[1];
      if(b == Bln.TRUE) {
        if(c == Bln.FALSE) {
          // if(A) then true() else false() -> xs:boolean(A)
          ctx.compInfo(OPTPRE, this);
          return compBln(a, info);
        }
        // if(A) then true() else C -> A or C
        ctx.compInfo(OPTWRITE, this);
        return new Or(info, a, c).optimize(ctx, scp);
      }

      if(c == Bln.TRUE) {
        if(b == Bln.FALSE) {
          // if(A) then false() else true() -> not(A)
          ctx.compInfo(OPTPRE, this);
          return Function.NOT.get(null, a).optimize(ctx, scp);
        }
        // if(A) then B else true() -> not(A) or B
        ctx.compInfo(OPTWRITE, this);
        final Expr notA = Function.NOT.get(null, a).optimize(ctx, scp);
        return new Or(info, notA, b).optimize(ctx, scp);
      }

      if(b == Bln.FALSE) {
        // if(A) then false() else C -> not(A) and C
        ctx.compInfo(OPTWRITE, this);
        final Expr notA = Function.NOT.get(null, a).optimize(ctx, scp);
        return new And(info, notA, c).optimize(ctx, scp);
      }

      if(c == Bln.FALSE) {
        // if(A) then B else false() -> A and B
        ctx.compInfo(OPTWRITE, this);
        return new And(info, a, b).optimize(ctx, scp);
      }
    }

    type = exprs[0].type().union(exprs[1].type());
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
    return exprs[cond.ebv(ctx, info).bool(info) ? 0 : 1];
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
    return cond.count(v).plus(VarUsage.maximum(v, exprs));
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp, final Var v, final Expr e)
      throws QueryException {

    final Expr sub = cond.inline(ctx, scp, v, e);
    if(sub != null) cond = sub;
    boolean te = false;
    final int es = exprs.length;
    for(int i = 0; i < es; i++) {
      Expr nw;
      try {
        nw = exprs[i].inline(ctx, scp, v, e);
      } catch(final QueryException qe) {
        nw = FNInfo.error(qe, type);
      }
      if(nw != null) {
        exprs[i] = nw;
        te = true;
      }
    }
    return te || sub != null ? optimize(ctx, scp) : null;
  }

  @Override
  public If copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new If(info, cond.copy(ctx, scp, vs),
        exprs[0].copy(ctx, scp, vs), exprs[1].copy(ctx, scp, vs)));
  }

  @Override
  public boolean isVacuous() {
    return exprs[0].isVacuous() && exprs[1].isVacuous();
  }

  @Override
  public void markTailCalls(final QueryContext ctx) {
    exprs[0].markTailCalls(ctx);
    exprs[1].markTailCalls(ctx);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), cond, exprs);
  }

  @Override
  public String toString() {
    return IF + '(' + cond + ") " + THEN + ' ' + exprs[0] + ' ' + ELSE + ' ' + exprs[1];
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return cond.accept(visitor) && super.accept(visitor);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr e : exprs) sz += e.exprSize();
    return sz + cond.exprSize();
  }

  @Override
  public Expr typeCheck(final TypeCheck tc, final QueryContext ctx, final VarScope scp)
      throws QueryException {
    for(int i = 0; i < exprs.length; i++) {
      final SeqType tp = exprs[i].type();
      try {
        exprs[i] = tc.check(exprs[i], ctx, scp);
      } catch(final QueryException ex) {
        exprs[i] = FNInfo.error(ex, tp);
      }
    }
    return optimize(ctx, scp);
  }
}
