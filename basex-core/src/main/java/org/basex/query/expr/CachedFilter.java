package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Filter expression, caching all results.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class CachedFilter extends Filter {
  /**
   * Constructor.
   * @param info input info
   * @param root root expression
   * @param preds predicates
   */
  CachedFilter(final InputInfo info, final Expr root, final Expr... preds) {
    super(info, root, preds);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    Value val = root.value(qc);
    final Value cv = qc.value;
    final long cs = qc.size;
    final long cp = qc.pos;

    try {
      // evaluate first predicate, based on incoming value
      final ValueBuilder vb = new ValueBuilder();
      Expr pred = preds[0];
      long vs = val.size();
      qc.size = vs;
      qc.pos = 1;

      final boolean scoring = qc.scoring;
      for(int s = 0; s < vs; s++) {
        final Item item = val.itemAt(s);
        qc.value = item;
        final Item test = pred.test(qc, info);
        if(test != null) {
          if(scoring) item.score(test.score());
          vb.add(item);
        }
        qc.pos++;
      }
      // save memory
      val = null;

      // evaluate remaining predicates, based on value builder
      final int pl = preds.length;
      for(int i = 1; i < pl; i++) {
        vs = vb.size();
        pred = preds[i];
        qc.size = vs;
        qc.pos = 1;
        int c = 0;
        for(int s = 0; s < vs; ++s) {
          final Item item = vb.get(s);
          qc.value = item;
          final Item test = pred.test(qc, info);
          if(test != null) {
            if(scoring) item.score(test.score());
            vb.set(c++, item);
          }
          qc.pos++;
        }
        vb.size(c);
      }

      // return resulting values
      return vb;
    } finally {
      qc.value = cv;
      qc.size = cs;
      qc.pos = cp;
    }
  }

  @Override
  public Filter copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new CachedFilter(info, root.copy(qc, scp, vs),
        Arr.copyAll(qc, scp, vs, preds)));
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem();
    addPlan(plan, el, root);
    super.plan(el);
  }
}
