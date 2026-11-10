package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Pipeline expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Pipeline extends Mapping {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs expressions
   */
  public Pipeline(final InputInfo info, final Expr... exprs) {
    super(info, exprs[exprs.length - 1].seqType(), exprs);
  }

  /**
   * Creates a new, optimized pipeline expression.
   * @param cc compilation context
   * @param info input info (can be {@code null})
   * @param exprs expressions
   * @return list, single expression or empty sequence
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final InputInfo info, final Expr... exprs)
      throws QueryException {
    final int el = exprs.length;
    return el > 1 ? new Pipeline(info, exprs).optimize(cc) : exprs[0];
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    flatten(cc);

    final Expr[] merged = merge(cc);
    if(merged != null) return get(cc, info, merged);

    final int el = exprs.length;
    exprType.assign(exprs[el - 1]);
    return this;
  }

  @Override
  public Expr unfold(final Unfolding uf, final CompileContext cc) throws QueryException {
    final int last = exprs.length - 1;
    exprs[last] = exprs[last].unfold(uf, cc);
    final Expr expr = optimize(cc);
    if(expr instanceof final Pipeline pipeline &&
        pipeline.exprs[pipeline.exprs.length - 1] instanceof final Recur recur) {
      final Expr absorbed = recur.absorb(ex -> ex.has(Flag.CTX),
          ex -> pipeline.with(ex).optimize(cc), cc);
      if(absorbed != null) return absorbed;
    }
    return expr;
  }

  /**
   * Returns the last operand as conditional step, with the pipeline moved into its branches.
   * @param cc compilation context
   * @return conditional expression (can be {@code null})
   * @throws QueryException query exception
   */
  If branches(final CompileContext cc) throws QueryException {
    return Unfold.distribute(exprs[exprs.length - 1], ex -> ex.has(Flag.CTX), this::with, cc);
  }

  /**
   * Returns a new pipeline with a replaced last operand.
   * @param last last operand
   * @return pipeline
   */
  private Pipeline with(final Expr last) {
    final Expr[] ops = exprs.clone();
    ops[ops.length - 1] = last;
    return new Pipeline(info, ops);
  }

  @Override
  public Iter iter(final QueryContext qc) {
    return new Iter() {
      QueryFocus qf;
      Iter iter;

      @Override
      public Item next() throws QueryException {
        final QueryFocus focus = qc.focus;
        try {
          if(iter == null) {
            qf = eval(qc);
            iter = exprs[exprs.length - 1].iter(qc);
          }
          qc.focus = qf;
          return iter.next();
        } finally {
          qc.focus = focus;
        }
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final QueryFocus focus = qc.focus;
    try {
      eval(qc);
      return exprs[exprs.length - 1].value(qc);
    } finally {
      qc.focus = focus;
    }
  }

  /**
   * Evaluates all operands except for the last.
   * @param qc query context
   * @return new query focus
   * @throws QueryException query exception
   */
  private QueryFocus eval(final QueryContext qc) throws QueryException {
    final QueryFocus qf = new QueryFocus();
    qf.value = exprs[0].value(qc);
    qc.focus = qf;
    final int last = exprs.length - 1;
    for(int e = 1; e < last; e++) {
      qf.value = exprs[e].value(qc);
    }
    return qf;
  }

  @Override
  public Expr inlineTypeCheck(final TypeCheck tc, final CompileContext cc) throws QueryException {
    // (A -> B) coerce as T → A -> (B coerce as T)
    final int last = exprs.length - 1;
    final Expr ex = tc.check(exprs[last], cc);
    if(ex == null) return this;
    exprs[last] = ex;
    return optimize(cc);
  }

  @Override
  boolean items() {
    return false;
  }

  @Override
  Expr merge(final Expr expr, final Expr next, final CompileContext cc) {
    // $a -> 1 → 1, $a -> (. + 1) → $a + 1
    return expr.has(Flag.NDT) ? null : next.has(Flag.CTX) ? inline(expr, next, cc) : next;
  }

  @Override
  public Pipeline copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new Pipeline(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Pipeline && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.operands(exprs, " -> ");
  }
}
