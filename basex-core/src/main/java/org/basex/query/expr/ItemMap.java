package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple map expression: item-based evaluation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
final class ItemMap extends SimpleMap {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  ItemMap(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    Item result = exprs[0].item(qc, info);
    final QueryFocus qf = qc.focus, focus = new QueryFocus();
    qc.focus = focus;
    try {
      final int el = exprs.length;
      for(int e = 1; e < el && result != Empty.VALUE; e++) {
        focus.value = result;
        result = exprs[e].item(qc, info);
      }
      return result;
    } finally {
      qc.focus = qf;
    }
  }

  @Override
  public ItemMap copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new ItemMap(info, Arr.copyAll(cc, vm, exprs)));
  }
}
