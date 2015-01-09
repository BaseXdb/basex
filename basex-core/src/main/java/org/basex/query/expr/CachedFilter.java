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
 * Filter expression.
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
      Expr p = preds[0];
      long is = val.size();
      qc.size = is;
      qc.pos = 1;
      for(int s = 0; s < is; ++s) {
        final Item it = val.itemAt(s);
        qc.value = it;
        final Item i = p.test(qc, info);
        if(i != null) vb.add(it);
        qc.pos++;
      }
      // save memory
      val = null;

      // evaluate remaining predicates, based on value builder
      final int pl = preds.length;
      for(int i = 1; i < pl; i++) {
        is = vb.size();
        p = preds[i];
        qc.size = is;
        qc.pos = 1;
        int c = 0;
        for(int s = 0; s < is; ++s) {
          final Item it = vb.get(s);
          qc.value = it;
          if(p.test(qc, info) != null) vb.set(c++, it);
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
    return copy(new CachedFilter(info, root.copy(qc, scp, vs), Arr.copyAll(qc, scp, vs, preds)));
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem();
    addPlan(plan, el, root);
    super.plan(el);
  }
}
