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
 * Value map expression.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ValueMap extends Mapping {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs expressions
   */
  public ValueMap(final InputInfo info, final Expr... exprs) {
    super(info, exprs[exprs.length - 1].seqType(), exprs);
  }

  /**
   * Creates a new, optimized map expression.
   * @param cc compilation context
   * @param info input info (can be {@code null})
   * @param exprs expressions
   * @return list, single expression or empty sequence
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final InputInfo info, final Expr... exprs)
      throws QueryException {
    final int el = exprs.length;
    return el > 1 ? new ValueMap(info, exprs).optimize(cc) : exprs[0];
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
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      QueryFocus qf;
      Iter iter;

      @Override
      public Item next() throws QueryException {
        final QueryFocus focus = qc.focus;
        try {
          if(iter == null) {
            qf = map(qc);
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
      map(qc);
      return exprs[exprs.length - 1].value(qc);
    } finally {
      qc.focus = focus;
    }
  }

  /**
   * Performs value mapping for all operands except for the last.
   * @param qc query context
   * @return new query focus
   * @throws QueryException query exception
   */
  private QueryFocus map(final QueryContext qc) throws QueryException {
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
  boolean items() {
    return false;
  }

  @Override
  Expr merge(final Expr expr, final Expr next, final CompileContext cc) throws QueryException {
    return expr.has(Flag.NDT) ? null : next.has(Flag.CTX) ? inline(expr, next, cc) : next;
  }

  @Override
  public ValueMap copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new ValueMap(info, Arr.copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof ValueMap && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.tokens(exprs, " ~ ", true);
  }
}
