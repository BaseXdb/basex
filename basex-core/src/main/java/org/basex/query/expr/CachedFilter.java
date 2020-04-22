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
 * @author BaseX Team 2005-20, BSD License
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

    final QueryFocus qf = qc.focus, focus = new QueryFocus();
    qc.focus = focus;
    try {
      // evaluate first predicate, based on incoming value
      Expr pred = exprs[0];
      long vs = value.size();
      focus.size = vs;
      focus.pos = 1;

      final boolean scoring = qc.scoring;
      for(int s = 0; s < vs; s++) {
        qc.checkStop();
        final Item item = value.itemAt(s);
        focus.value = item;
        final Item test = pred.test(qc, info);
        if(test != null) {
          if(scoring) item.score(test.score());
          items.add(item);
        }
        focus.pos++;
      }
      // save memory
      value = null;

      // evaluate remaining predicates, based on value builder
      final int pl = exprs.length;
      for(int i = 1; i < pl; i++) {
        vs = items.size();
        pred = exprs[i];
        focus.size = vs;
        focus.pos = 1;
        int c = 0;
        for(int s = 0; s < vs; ++s) {
          qc.checkStop();
          final Item item = items.get(s);
          focus.value = item;
          final Item test = pred.test(qc, info);
          if(test != null) {
            if(scoring) item.score(test.score());
            items.set(c++, item);
          }
          focus.pos++;
        }
        items.size(c);
      }
    } finally {
      qc.focus = qf;
    }

    return items.value(this);
  }

  @Override
  public Filter copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CachedFilter(info, root.copy(cc, vm), Arr.copyAll(cc, vm, exprs)));
  }
}
