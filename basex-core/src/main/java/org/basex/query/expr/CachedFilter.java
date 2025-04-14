package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
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
    final QueryFocus focus = qc.focus, qf = new QueryFocus();
    qc.focus = qf;
    try {
      for(final Expr expr : exprs) {
        final ValueBuilder vb = new ValueBuilder(qc);
        final long vs = value.size();
        qf.size = vs;
        for(int v = 0; v < vs; ++v) {
          qc.checkStop();
          final Item item = value.itemAt(v);
          qf.value = item;
          qf.pos = v + 1;
          if(expr.test(qc, info, v + 1)) vb.add(item);
        }
        value = vb.value();
      }
      return value;
    } finally {
      qc.focus = focus;
    }
  }

  @Override
  public Filter copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new CachedFilter(info, root.copy(cc, vm), Arr.copyAll(cc, vm, exprs)));
  }
}
