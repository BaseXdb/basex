package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Filter expression, caching all results.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class CachedFilter extends Filter {
  /**
   * Constructor.
   * @param info input info
   * @param root root expression
   * @param preds predicate expressions
   */
  public CachedFilter(final InputInfo info, final Expr root, final Expr... preds) {
    super(info, root, preds);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ItemList items = new ItemList();
    Value value = root.value(qc);

    final QueryFocus focus = qc.focus, qf = new QueryFocus();
    qc.focus = qf;
    try {
      // evaluate first predicate, based on incoming value
      Expr expr = exprs[0];
      long vs = value.size();
      qf.size = vs;

      for(int v = 0; v < vs; v++) {
        qc.checkStop();
        final Item item = value.itemAt(v);
        qf.value = item;
        qf.pos = v + 1;
        final Item test = expr.test(qc, info);
        if(test != null) {
          items.add(item);
        }
      }
      // save memory
      value = null;

      // evaluate remaining predicates, based on value builder
      final int el = exprs.length;
      for(int e = 1; e < el; e++) {
        vs = items.size();
        qf.size = vs;
        expr = exprs[e];
        int c = 0;
        for(int i = 0; i < vs; ++i) {
          qc.checkStop();
          final Item item = items.get(i);
          qf.value = item;
          qf.pos = i + 1;
          if(expr.test(qc, info) != null) items.set(c++, item);
        }
        items.size(c);
      }
    } finally {
      qc.focus = focus;
    }
    return items.value(this);
  }

  @Override
  public Filter copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CachedFilter(info, root.copy(cc, vm), Arr.copyAll(cc, vm, exprs)));
  }
}
