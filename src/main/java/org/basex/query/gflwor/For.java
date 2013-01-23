package org.basex.query.gflwor;

import java.util.List;
import static org.basex.query.QueryText.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.gflwor.GFLWOR.Eval;
import org.basex.query.iter.Iter;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * FLWOR {@code for} clause, iterating over a sequence.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public class For extends GFLWOR.Clause {
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
          } else if(empty && iter != null && p == 0) {
            // expression yields no items, bind the empty sequence instead
            ctx.set(var, Empty.SEQ, info);
            if(pos != null) ctx.set(pos, Int.get(p), info);
            if(score != null) ctx.set(score, Dbl.get(0), info);
            iter = null;
            return true;
          } else if(!sub.next(ctx)) {
            // no more iterations from above, we're done here
            return false;
          }

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
      final FElem e2 = new FElem(Token.token(QueryText.AT));
      pos.plan(e2);
      e.add(e2);
    }

    if(score != null) {
      final FElem e2 = new FElem(Token.token(QueryText.SCORE));
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
  public boolean uses(final Use u) {
    return u == Use.VAR || expr.uses(u);
  }

  @Override
  public For compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    expr = expr.compile(ctx, scp);
    final SeqType tp = expr.type();
    final boolean emp = empty && tp.mayBeZero();
    type = SeqType.get(tp.type, emp ? Occ.ZERO_ONE : Occ.ONE);
    var.refineType(type, info);
    size = emp ? -1 : 1;
    return this;
  }

  @Override
  public boolean removable(final Var v) {
    return expr.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    expr = expr.remove(v);
    return this;
  }

  @Override
  public boolean visitVars(final VarVisitor visitor) {
    return expr.visitVars(visitor) && visitor.declared(var)
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
    expr.checkUp();
  }

  @Override
  public boolean databases(final StringList db) {
    return expr.databases(db);
  }

  /**
   * Tries to convert this for loop into a let binding.
   * @param clauses FLWOR clauses
   * @param p position
   * @return {@code true} if the clause was converted, {@code false} otherwise
   */
  boolean asLet(final List<GFLWOR.Clause> clauses, final int p) {
    if(expr.size() != 1 && !expr.type().one()) return false;
    clauses.set(p, Let.fromFor(this));
    if(score != null) clauses.add(p + 1, Let.fromForScore(this));
    if(pos != null) clauses.add(p + 1, new Let(pos, Int.get(1), false, info));
    return true;
  }

  /**
   * Tries to add the given expression as an attribute to this loop's sequence.
   * @param e expression to add
   * @return success
   */
  boolean toPred(final Expr e) {
    if(pos != null || score != null || !e.removable(var)) return false;
    e.remove(var);

    // attach predicates to axis path or filter, or create a new filter
    final Expr a = e.type().mayBeNumber() ? Function.BOOLEAN.get(info, e) : e;

    // add to clause expression
    if(expr instanceof AxisPath) {
      expr = ((AxisPath) expr).addPreds(a);
    } else if(expr instanceof Filter) {
      expr = ((Filter) expr).addPred(a);
    } else {
      expr = new Filter(info, expr, a);
    }
    return true;
  }

  @Override
  long calcSize(final long count) {
    final long sz = expr.size();
    return sz < 0 ? -1 : sz > 0 ? sz * count : empty ? 1 : 0;
  }
}
