package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.List;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
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
 * @author BaseX Team 2005-19, BSD License
 * @author Leo Woerteler
 */
public final class For extends ForLet {
  /** Position variable (can be {@code null}). */
  Var pos;
  /** Score variable (can be {@code null}). */
  Var score;
  /** {@code allowing empty} flag. */
  final boolean empty;

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
            qc.set(var, Empty.SEQ);
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
    // assign type to clause and variable
    final SeqType type = expr.seqType();
    exprType.assign(type.type, empty && type.mayBeEmpty() ? Occ.ZERO_ONE : Occ.ONE);
    var.refineType(seqType(), size(), cc);
    var.data = expr.data();
    if(pos != null) pos.refineType(SeqType.ITR_O, 1, cc);
    if(score != null) score.refineType(SeqType.DBL_O, 1, cc);
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
   * @return {@code true} if the clause was converted, {@code false} otherwise
   */
  boolean asLet(final List<Clause> clauses, final int p) {
    if(!expr.seqType().one()) return false;
    clauses.set(p, Let.fromFor(this));
    if(score != null) clauses.add(p + 1, Let.fromForScore(this));
    if(pos != null) clauses.add(p + 1, new Let(pos, Int.ONE, false));
    return true;
  }

  /**
   * Adds a predicate to the looped expression.
   * @param ex expression to add as predicate
   */
  void addPredicate(final Expr ex) {
    if(expr instanceof AxisPath && !ex.has(Flag.POS)) {
      // add to axis path, provided that predicate is not positional
      expr = ((AxisPath) expr).addPreds(ex);
    } else if(expr instanceof Filter) {
      // add to existing filter expression
      expr = ((Filter) expr).addPred(ex);
    } else {
      // create new filter expression
      expr = Filter.get(info, expr, ex);
    }
  }

  /**
   * Tries to add the given expression as a predicate.
   * Replaces variable references with a context expression
   * @param cc compilation context
   * @param ex expression to add as predicate
   * @return success flag
   * @throws QueryException query exception
   */
  public boolean toPredicate(final CompileContext cc, final Expr ex) throws QueryException {
    if(empty || !(vars().length == 1 && ex.uses(var) && ex.inlineable(var))) return false;

    // reset context value (will not be accessible in predicate)
    Expr pred = ex;
    cc.pushFocus(expr);
    try {
      // assign type of iterated items to context expression
      final Expr r = ex.inline(var, new ContextValue(info).optimize(cc), cc);
      if(r != null) pred = r;
    } finally {
      cc.removeFocus();
    }

    // attach predicates to axis path or filter, or create a new filter
    if(pred.seqType().mayBeNumber()) pred = cc.function(Function.BOOLEAN, info, pred);

    addPredicate(pred);
    return true;
  }

  @Override
  void calcSize(final long[] minMax) {
    final long size = expr.size(), factor = size > 0 ? size : empty ? 1 : 0;
    minMax[0] *= factor;
    final long max = minMax[1];
    if(max > 0) minMax[1] = size < 0 ? -1 : max * factor;
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof For)) return false;
    final For f = (For) obj;
    return Objects.equals(pos, f.pos) && Objects.equals(score, f.score) && empty == f.empty &&
        super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem elem = planElem();
    if(empty) elem.add(planAttr(EMPTYY, true));
    var.plan(elem);
    if(pos != null) {
      final FElem el = new FElem(AT);
      pos.plan(el);
      elem.add(el);
    }
    if(score != null) {
      final FElem el = new FElem(SCORE);
      score.plan(el);
      elem.add(el);
    }
    expr.plan(elem);
    plan.add(elem);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(FOR).append(' ').append(var);
    if(empty) sb.append(' ').append(ALLOWING).append(' ').append(EMPTYY);
    if(pos != null) sb.append(' ').append(AT).append(' ').append(pos);
    if(score != null) sb.append(' ').append(SCORE).append(' ').append(score);
    return sb.append(' ').append(IN).append(' ').append(expr).toString();
  }
}
