package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Filter expression, caching all results.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class CachedFilter extends Filter {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param root root expression
   * @param preds predicate expressions
   */
  public CachedFilter(final InputInfo info, final Expr root, final Expr... preds) {
    super(info, root, preds);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    Value value = root.value(qc);
    if(value.isEmpty()) return Empty.VALUE;

    final QueryFocus focus = qc.focus, qf = new QueryFocus();
    qc.focus = qf;
    try {
      for(final Expr expr : exprs) {
        value = eval(value, expr, qc);
        if(value.isEmpty()) break;
      }
      return value;
    } finally {
      qc.focus = focus;
    }
  }

  /**
   * Filters a value by testing each of its items against a predicate.
   * @param value items to filter
   * @param pred predicate expression
   * @param qc query context
   * @return filtered value
   * @throws QueryException query exception
   */
  final Value eval(final Value value, final Expr pred, final QueryContext qc)
      throws QueryException {
    final QueryFocus qf = qc.focus;
    final long vs = value.size();
    qf.size = vs;
    final ValueBuilder vb = new ValueBuilder(qc);
    for(int v = 0; v < vs; ++v) {
      qc.checkStop();
      final Item item = value.itemAt(v);
      qf.value = item;
      qf.pos = v + 1;
      if(pred.test(qc, info, v + 1)) vb.add(item);
    }
    return vb.value();
  }

  @Override
  public Filter copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new CachedFilter(info, root.copy(cc, vm), copyAll(cc, vm, exprs)));
  }
}
