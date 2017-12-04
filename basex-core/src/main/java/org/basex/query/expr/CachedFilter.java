package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Filter expression, caching all results.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public class CachedFilter extends Filter {
  /**
   * Constructor.
   * @param info input info
   * @param root root expression
   * @param preds predicates
   */
  public CachedFilter(final InputInfo info, final Expr root, final Expr... preds) {
    super(info, root, preds);
  }

  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    Value val = root.value(qc);
    final QueryFocus qf = qc.focus, focus = new QueryFocus();
    qc.focus = focus;
    try {
      // evaluate first predicate, based on incoming value
      final ItemList buffer = new ItemList();
      Expr pred = exprs[0];
      long vs = val.size();
      focus.size = vs;
      focus.pos = 1;

      final boolean scoring = qc.scoring;
      for(int s = 0; s < vs; s++) {
        qc.checkStop();
        final Item it = val.itemAt(s);
        focus.value = it;
        final Item test = pred.test(qc, info);
        if(test != null) {
          if(scoring) it.score(test.score());
          buffer.add(it);
        }
        focus.pos++;
      }
      // save memory
      val = null;

      // evaluate remaining predicates, based on value builder
      final int pl = exprs.length;
      for(int i = 1; i < pl; i++) {
        vs = buffer.size();
        pred = exprs[i];
        focus.size = vs;
        focus.pos = 1;
        int c = 0;
        for(int s = 0; s < vs; ++s) {
          qc.checkStop();
          final Item it = buffer.get(s);
          focus.value = it;
          final Item test = pred.test(qc, info);
          if(test != null) {
            if(scoring) it.score(test.score());
            buffer.set(c++, it);
          }
          focus.pos++;
        }
        buffer.size(c);
      }

      // return resulting values
      return buffer.value();
    } finally {
      qc.focus = qf;
    }
  }

  @Override
  public Filter copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CachedFilter(info, root.copy(cc, vm), Arr.copyAll(cc, vm, exprs)));
  }
}
