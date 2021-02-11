package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Step expression, caching all results.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CachedStep extends Step {
  /**
   * Constructor.
   * @param info input info
   * @param axis axis
   * @param test node test
   * @param preds predicates
   */
  public CachedStep(final InputInfo info, final Axis axis, final Test test, final Expr... preds) {
    super(info, axis, test, preds);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // evaluate step
    final ANodeList list = new ANodeList();
    for(final ANode node : axis.iter(checkNode(qc))) {
      if(test.matches(node)) list.add(node.finish());
    }

    // evaluate predicates
    final QueryFocus focus = qc.focus, qf = new QueryFocus();
    qc.focus = qf;
    try {
      for(final Expr expr : exprs) {
        final long nl = list.size();
        qf.size = nl;
        int c = 0;
        for(int n = 0; n < nl; n++) {
          final ANode node = list.get(n);
          qf.value = node;
          qf.pos = n + 1;
          if(expr.test(qc, info) != null) list.set(c++, node);
        }
        list.size(c);
      }
    } finally {
      qc.focus = focus;
    }
    return list.clean().iter();
  }

  @Override
  public Step copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final int pl = exprs.length;
    final Expr[] pred = new Expr[pl];
    for(int p = 0; p < pl; p++) pred[p] = exprs[p].copy(cc, vm);
    return copyType(new CachedStep(info, axis, test.copy(), pred));
  }
}
