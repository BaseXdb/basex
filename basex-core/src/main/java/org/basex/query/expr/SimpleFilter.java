package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple filter expression with one deterministic predicate.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
final class SimpleFilter extends CachedFilter {
  /**
   * Constructor.
   * @param info input info
   * @param root root expression
   * @param preds predicates
   */
  SimpleFilter(final InputInfo info, final Expr root, final Expr... preds) {
    super(info, root, preds);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item pred = exprs[0].ebv(qc, info);
    if(pred != null) {
      final Value val = qc.value(root);
      if(pred instanceof ANum) {
        final double pos = pred.dbl(info);
        if(pos > 0 && pos <= val.size() && pos == (long) pos) return val.itemAt((long) pos - 1);
      } else {
        if(pred.bool(info)) return val;
      }
    }
    return Empty.SEQ;
  }

  @Override
  public Filter copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new SimpleFilter(info, root.copy(cc, vm),
        Arr.copyAll(cc, vm, exprs)));
  }
}
