package org.basex.query.gflwor;

import static org.basex.query.QueryText.*;

import java.util.List;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.gflwor.GFLWOR.Clause;
import org.basex.query.gflwor.GFLWOR.Eval;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * FLWOR {@code for} clause, iterating over a sequence.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class For extends Clause {
  /** Item variable. */
  final Var var;
  /** Position variable. */
  final Var pos;
  /** Score variable. */
  final Var score;
  /** Bound expression. */
  Expr expr;
  /** {@code allowing empty} flag. */
  final boolean empty;

  /**
   * Constructor.
   * @param v item variable
   * @param p position variable or {@code null}
   * @param s score variable or {@code null}
   * @param e bound expression
   * @param emp {@code allowing empty} flag
   * @param ii input info
   */
  public For(final Var v, final Var p, final Var s, final Expr e, final boolean emp,
      final InputInfo ii) {
    super(ii, vars(v, p, s));
    var = v;
    pos = p;
    score = s;
    expr = e;
    empty = emp;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      /** Expression iterator. */
      private Iter iter;
      /** Current position. */
      private long p;
      @Override
      public boolean next(final QueryContext ctx) throws QueryException {
        while(true) {
          final Item it = iter == null ? null : iter.next();
          if(it != null) {
            // there's another item to serve
            ++p;
            ctx.set(var, it, info);
            if(pos != null) ctx.set(pos, Int.get(p), info);
            if(score != null) ctx.set(score, Dbl.get(it.score()), info);
            return true;
          }
          if(empty && iter != null && p == 0) {
            // expression yields no items, bind the empty sequence instead
            ctx.set(var, Empty.SEQ, info);
            if(pos != null) ctx.set(pos, Int.get(p), info);
            if(score != null) ctx.set(score, Dbl.get(0), info);
            iter = null;
            return true;
          }
          // no more iterations from above, we're done here
          if(!sub.next(ctx)) return false;

          // next iteration, reset iterator and counter
          iter = expr.iter(ctx);
          p = 0;
        }
      }
    };
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    if(empty) e.add(planAttr(Token.token(EMPTYORD), Token.TRUE));
    var.plan(e);
    if(pos != null) {
      final FElem e2 = new FElem(AT);
      pos.plan(e2);
      e.add(e2);
    }

    if(score != null) {
      final FElem e2 = new FElem(SCORE);
      score.plan(e2);
      e.add(e2);
    }

    expr.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(FOR).append(' ').append(var);
    if(empty) sb.append(' ').append(ALLOWING).append(' ').append(EMPTYORD);
    if(pos != null) sb.append(' ').append(AT).append(' ').append(pos);
    if(score != null) sb.append(' ').append(SCORE).append(' ').append(score);
    return sb.append(' ').append(IN).append(' ').append(expr).toString();
  }

  @Override
  public boolean has(final Flag flag) {
    return expr.has(flag);
  }

  @Override
  public For compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    expr = expr.compile(ctx, scp);
    return optimize(ctx, scp);
  }

  @Override
  public For optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    final SeqType tp = expr.type();
    final boolean emp = empty && tp.mayBeZero();
    type = SeqType.get(tp.type, emp ? Occ.ZERO_ONE : Occ.ONE);
    var.refineType(type, ctx, info);
    if(pos != null) pos.refineType(SeqType.ITR, ctx, info);
    if(score != null) score.refineType(SeqType.DBL, ctx, info);
    size = emp ? -1 : 1;
    return this;
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
  public Clause inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    final Expr sub = expr.inline(ctx, scp, v, e);
    if(sub == null) return null;
    expr = sub;
    return optimize(ctx, scp);
  }

  @Override
  public For copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    final Var v = scp.newCopyOf(ctx, var);
    vs.put(var.id, v);
    final Var p = pos == null ? null : scp.newCopyOf(ctx, pos);
    if(p != null) vs.put(pos.id, p);
    final Var s = score == null ? null : scp.newCopyOf(ctx, score);
    if(s != null) vs.put(score.id, s);
    return new For(v, p, s, expr.copy(ctx, scp, vs), empty, info);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor) && visitor.declared(var)
        && (pos == null || visitor.declared(pos))
        && (score == null || visitor.declared(score));
  }

  /**
   * Gathers all non-{@code null} variables.
   * @param v var
   * @param p pos
   * @param s scope
   * @return non-{@code null} variables
   */
  private static Var[] vars(final Var v, final Var p, final Var s) {
    return p == null ? s == null ? new Var[] { v } : new Var[] { v, s } :
      s == null ? new Var[] { v, p } : new Var[] { v, p, s };
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  /**
   * Tries to convert this for loop into a let binding.
   * @param clauses FLWOR clauses
   * @param p position
   * @return {@code true} if the clause was converted, {@code false} otherwise
   */
  boolean asLet(final List<Clause> clauses, final int p) {
    if(expr.size() != 1 && !expr.type().one()) return false;
    clauses.set(p, Let.fromFor(this));
    if(score != null) clauses.add(p + 1, Let.fromForScore(this));
    if(pos != null) clauses.add(p + 1, new Let(pos, Int.get(1), false, info));
    return true;
  }

  /**
   * Tries to add the given expression as an attribute to this loop's sequence.
   * @param ctx query context
   * @param scp variable scope
   * @param p expression to add
   * @return success
   * @throws QueryException query exception
   */
  boolean toPred(final QueryContext ctx, final VarScope scp, final Expr p)
      throws QueryException {
    if(empty || !(vars.length == 1 && p.uses(var) && p.removable(var))) return false;
    final Expr r = p.inline(ctx, scp, var, new Context(info)), e = r == null ? p : r;

    // attach predicates to axis path or filter, or create a new filter
    final Expr a = e.type().mayBeNumber() ? Function.BOOLEAN.get(null, info, e) : e;

    // add to clause expression
    if(expr instanceof AxisPath) {
      expr = ((Path) expr).addPreds(ctx, scp, a);
    } else if(expr instanceof Filter) {
      expr = ((Filter) expr).addPred(ctx, scp, a);
    } else {
      expr = Filter.get(info, expr, a).optimize(ctx, scp);
    }

    return true;
  }

  @Override
  long calcSize(final long count) {
    final long sz = expr.size();
    return sz < 0 ? -1 : sz > 0 ? sz * count : empty ? 1 : 0;
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }
}
