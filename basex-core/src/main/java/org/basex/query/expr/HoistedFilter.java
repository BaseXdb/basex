package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Filter expression with loop-invariant predicates.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HoistedFilter extends CachedFilter {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param root root expression
   * @param preds single loop-invariant predicate
   */
  HoistedFilter(final InputInfo info, final Expr root, final Expr... preds) {
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
        final Value pred = expr.value(qc);
        if(pred.isEmpty()) return Empty.VALUE;

        if(pred instanceof final Item item) {
          // evaluate single-item predicate
          if(item instanceof final ANum num) {
            final double d = num.dbl(info) - 1;
            final long l = (long) d;
            value = d == l && l >= 0 && l < value.size() ? value.itemAt(l) : Empty.VALUE;
          } else if(!item.test(qc, info, 0)) {
            value = Empty.VALUE;
          }
        } else {
          // fallback evaluation for multiple items
          value = eval(value, pred, qc);
        }
        if(value.isEmpty()) break;
      }
      return value;
    } finally {
      qc.focus = focus;
    }
  }

  @Override
  public HoistedFilter copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new HoistedFilter(info, root.copy(cc, vm), copyAll(cc, vm, exprs)));
  }
}
