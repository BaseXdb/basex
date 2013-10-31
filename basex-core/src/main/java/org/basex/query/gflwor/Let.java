package org.basex.query.gflwor;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.gflwor.GFLWOR.Eval;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FLWOR {@code let} clause, binding an expression to a variable.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class Let extends GFLWOR.Clause {
  /** Variable. */
  public final Var var;
  /** Bound expression. */
  public Expr expr;
  /** Score flag. */
  private final boolean score;

  /**
   * Constructor.
   * @param v variable
   * @param e expression
   * @param scr score flag
   * @param ii input info
   */
  public Let(final Var v, final Expr e, final boolean scr, final InputInfo ii) {
    super(ii, v);
    var = v;
    expr = e;
    score = scr;
  }

  /**
   * Creates a let expression from a for loop over a single item.
   * @param fr for loop
   * @return let binding
   */
  static Let fromFor(final For fr) {
    final Let lt = new Let(fr.var, fr.expr, false, fr.info);
    lt.type = fr.expr.type();
    return lt;
  }

  /**
   * Creates a let binding for the score variable of a for clause.
   * @param fr for clause
   * @return let binding for the score variable
   */
  static Let fromForScore(final For fr) {
    final Expr varRef = new VarRef(fr.info, fr.var);
    return new Let(fr.score, varRef, true, fr.info);
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      @Override
      public boolean next(final QueryContext ctx) throws QueryException {
        if(!sub.next(ctx)) return false;
        ctx.set(var, score ? score(expr.iter(ctx)) : ctx.value(expr), info);
        return true;
      }
    };
  }

  /**
   * Calculates the score of the given iterator.
   * @param iter iterator
   * @return score
   * @throws QueryException evaluation exception
   */
  private static Dbl score(final Iter iter) throws QueryException {
    double sum = 0;
    int sz = 0;
    for(Item it; (it = iter.next()) != null; sum += it.score(), sz++);
    return Dbl.get(Scoring.let(sum, sz));
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    if(score) e.add(planAttr(Token.token(SCORE), Token.TRUE));
    var.plan(e);
    expr.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    return LET + ' ' + (score ? SCORE + ' ' : "") + var + ' ' + ASSIGN + ' ' + expr;
  }

  @Override
  public boolean has(final Flag flag) {
    return expr.has(flag);
  }

  @Override
  public Let compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    var.refineType(score ? SeqType.DBL : expr.type(), ctx, info);
    expr = expr.compile(ctx, scp);
    return optimize(ctx, scp);
  }

  @Override
  public Let optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    if(!score && expr instanceof TypeCheck) {
      final TypeCheck tc = (TypeCheck) expr;
      if(tc.isRedundant(var) || var.adoptCheck(tc.type, tc.promote)) {
        ctx.compInfo(OPTCAST, tc.type);
        expr = tc.expr;
      }
    }

    type = score ? SeqType.DBL : expr.type();
    var.refineType(type, ctx, info);
    if(var.checksType() && expr.isValue()) {
      expr = var.checkType((Value) expr, ctx, info);
      var.refineType(expr.type(), ctx, info);
    }
    size = score ? 1 : expr.size();
    return this;
  }

  /**
   * Binds the the value of this let clause to the context if it is statically known.
   * @param ctx query context
   * @throws QueryException evaluation exception
   */
  void bindConst(final QueryContext ctx) throws QueryException {
    if(expr.isValue()) {
      ctx.compInfo(OPTBIND, var);
      ctx.set(var, score ? score(expr.iter(ctx)) : (Value) expr, info);
    }
  }

  @Override
  public boolean removable(final Var v) {
    return expr.removable(v);
  }

  @Override
  public VarUsage count(final Var v) {
    return expr.count(v);
  }

  @Override
  public GFLWOR.Clause inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    final Expr sub = expr.inline(ctx, scp, v, e);
    if(sub == null) return null;
    expr = sub;
    return optimize(ctx, scp);
  }

  @Override
  public Let copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    final Var v = scp.newCopyOf(ctx, var);
    vs.put(var.id, v);
    return new Let(v, expr.copy(ctx, scp, vs), score, info);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor) && visitor.declared(var);
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  long calcSize(final long cnt) {
    return cnt;
  }

  /**
   * Returns an expression that is appropriate for inlining.
   * @param ctx query context
   * @param scp variable scope
   * @return inlineable expression
   * @throws QueryException query exception
   */
  public Expr inlineExpr(final QueryContext ctx, final VarScope scp) throws QueryException {
    return score ? Function._FT_SCORE.get(null, expr).optimize(ctx, scp)
                 : var.checked(expr, ctx, scp, info);
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }
}
