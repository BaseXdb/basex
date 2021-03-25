package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.List;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * FLWOR {@code for} clause, iterating over a sequence.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class For extends ForLet {
  /** Position variable (can be {@code null}). */
  Var pos;
  /** Score variable (can be {@code null}). */
  Var score;
  /** {@code allowing empty} flag. */
  boolean empty;

  /**
   * Constructor.
   * @param var item variable
   * @param expr bound expression
   */
  public For(final Var var, final Expr expr) {
    this(var, null, null, expr, false);
  }

  /**
   * Constructor.
   * @param var item variable
   * @param pos position variable or {@code null}
   * @param score score variable or {@code null}
   * @param expr bound expression
   * @param empty {@code allowing empty} flag
   */
  public For(final Var var, final Var pos, final Var score, final Expr expr, final boolean empty) {
    super(var.info, SeqType.ITEM_ZO, var, expr, score != null, vars(var, pos, score));
    this.pos = pos;
    this.score = score;
    this.empty = empty;
  }

  /**
   * Creates a for expression from a let binding.
   * @param lt let binding
   * @param cc compilation context
   * @return for expression
   * @throws QueryException query exception
   */
  static For fromLet(final Let lt, final CompileContext cc) throws QueryException {
    final Expr expr = lt.scoring ? cc.function(Function._FT_SCORE, lt.info, lt.expr) : lt.expr;
    return new For(lt.var, expr).optimize(cc);
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      /** Expression iterator. */
      private Iter iter;
      /** Current position. */
      private long p;

      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        while(true) {
          // next iteration, reset iterator and counter
          Item item = null;
          if(iter != null) {
            if(scoring) {
              final boolean s = qc.scoring;
              try {
                qc.scoring = true;
                item = qc.next(iter);
              } finally {
                qc.scoring = s;
              }
            } else {
              item = qc.next(iter);
            }
          }
          if(item != null) {
            // there's another item to serve
            ++p;
            qc.set(var, item);
            if(pos != null) qc.set(pos, Int.get(p));
            if(score != null) qc.set(score, Dbl.get(item.score()));
            return true;
          }
          if(empty && iter != null && p == 0) {
            // expression yields no items, bind the empty sequence instead
            qc.set(var, Empty.VALUE);
            if(pos != null) qc.set(pos, Int.get(p));
            if(score != null) qc.set(score, Dbl.ZERO);
            iter = null;
            return true;
          }
          // no more iterations from above, we're done here
          if(!sub.next(qc)) return false;

          // next iteration, reset iterator and counter
          if(scoring) {
            final boolean s = qc.scoring;
            try {
              qc.scoring = true;
              iter = expr.iter(qc);
            } finally {
              qc.scoring = s;
            }
          } else {
            iter = expr.iter(qc);
          }
          p = 0;
        }
      }
    };
  }

  @Override
  public For optimize(final CompileContext cc) throws QueryException {
    // assign type to clause and variable; remove empty flag if expression always yields items
    final SeqType st = expr.seqType();
    if(st.oneOrMore()) empty = false;
    exprType.assign(st.with(empty ? st.zero() ? Occ.ZERO : Occ.ZERO_OR_ONE : Occ.EXACTLY_ONE));

    var.refineType(seqType(), size(), cc);
    var.expr(expr);
    if(pos != null) {
      pos.refineType(SeqType.INTEGER_O, 1, cc);
      pos.expr(Int.ZERO);
    }
    if(score != null) {
      score.refineType(SeqType.DOUBLE_O, 1, cc);
      score.expr(Dbl.ZERO);
    }
    return this;
  }

  @Override
  public For copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new For(cc.copy(var, vm), cc.copy(pos, vm), cc.copy(score, vm),
        expr.copy(cc, vm), empty));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor) && visitor.declared(var)
        && (pos == null || visitor.declared(pos))
        && (score == null || visitor.declared(score));
  }

  /**
   * Gathers all non-{@code null} variables.
   * @param var var
   * @param pos pos
   * @param scope scope
   * @return non-{@code null} variables
   */
  private static Var[] vars(final Var var, final Var pos, final Var scope) {
    return pos == null ? scope == null ? new Var[] { var } : new Var[] { var, scope } :
      scope == null ? new Var[] { var, pos } : new Var[] { var, pos, scope };
  }

  /**
   * Tries to convert this for loop into a let binding.
   * @param clauses FLWOR clauses
   * @param p position
   * @param cc compilation context
   * @return {@code true} if the clause was converted, {@code false} otherwise
   * @throws QueryException query exception
   */
  boolean asLet(final List<Clause> clauses, final int p, final CompileContext cc)
      throws QueryException {

    if(!expr.seqType().one()) return false;
    clauses.set(p, Let.fromFor(this, cc));
    if(score != null) clauses.add(p + 1, Let.fromForScore(this, cc));
    if(pos != null) clauses.add(p + 1, new Let(pos, Int.ONE).optimize(cc));
    return true;
  }

  /**
   * Removes a variable reference.
   * @param cc compilation context
   * @param vr variable to be removed
   */
  void remove(final CompileContext cc, final Var vr) {
    cc.info(OPTVAR_X, vr);
    if(vr == score) {
      score = null;
      scoring = false;
    } else {
      pos = null;
    }
    vars = vars(var, pos, score);
  }

  @Override
  public void calcSize(final long[] minMax) {
    final long size = expr.size(), factor = size > 0 ? size : empty ? 1 : 0;
    minMax[0] *= factor;
    final long max = minMax[1];
    if(max > 0) minMax[1] = size >= 0 ? max * factor : -1;
  }

  @Override
  Expr inlineExpr(final CompileContext cc) {
    return empty || vars.length > 1 || var.checksType() ? null : expr;
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof For)) return false;
    final For f = (For) obj;
    return Objects.equals(pos, f.pos) && Objects.equals(score, f.score) && empty == f.empty &&
        super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    final FElem elem = plan.attachVariable(plan.create(this), var, false);
    if(empty) plan.addAttribute(elem, EMPTYY, true);
    if(pos != null) plan.addElement(elem, plan.create(AT, pos));
    if(score != null) plan.addElement(elem, plan.create(SCORE, score));
    plan.add(elem, expr);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(FOR).token(var);
    if(empty) qs.token(ALLOWING).token(EMPTYY);
    if(pos != null) qs.token(AT).token(pos);
    if(score != null) qs.token(SCORE).token(score);
    qs.token(IN).token(expr);
  }
}
