package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Cached map expression.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class CachedMap extends SimpleMap {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public CachedMap(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    Value value = exprs[0].value(qc);

    final QueryFocus qf = qc.focus, focus = new QueryFocus();
    qc.focus = focus;
    try {
      final int el = exprs.length;
      for(int e = 1; e < el; e++) {
        final Expr expr = exprs[e];
        focus.pos = 0;
        focus.size = value.size();
        final ValueBuilder vb = new ValueBuilder(qc);
        final Iter iter = value.iter();
        for(Item item; (item = qc.next(iter)) != null;) {
          focus.pos++;
          focus.value = item;
          vb.add(expr.value(qc));
        }
        value = vb.value(expr);
      }
    } finally {
      qc.focus = qf;
    }

    return value;
  }

  @Override
  public SimpleMap copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CachedMap(info, Arr.copyAll(cc, vm, exprs)));
  }
}
