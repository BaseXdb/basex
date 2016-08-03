package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Step expression, caching all results.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class CachedStep extends Step {
  /**
   * Constructor.
   * @param info input info
   * @param axis axis
   * @param test node test
   * @param preds predicates
   */
  CachedStep(final InputInfo info, final Axis axis, final Test test, final Expr[] preds) {
    super(info, axis, test, preds);
  }

  @Override
  public NodeIter iter(final QueryContext qc) throws QueryException {
    // evaluate step
    final ANodeList list = new ANodeList();
    for(final ANode n : axis.iter(checkNode(qc))) {
      if(test.eq(n)) list.add(n.finish());
    }

    // evaluate predicates
    final QueryFocus qf = qc.focus;
    final boolean scoring = qc.scoring;
    for(final Expr pred : preds) {
      final long nl = list.size();
      qf.size = nl;
      qf.pos = 1;
      int c = 0;
      for(int n = 0; n < nl; ++n) {
        final ANode node = list.get(n);
        qf.value = node;
        final Item tst = pred.test(qc, info);
        if(tst != null) {
          // assign score value
          if(scoring) node.score(tst.score());
          list.set(c++, node);
        }
        qf.pos++;
      }
      list.size(c);
    }
    return list.iter();
  }

  @Override
  public Step copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final int pl = preds.length;
    final Expr[] pred = new Expr[pl];
    for(int p = 0; p < pl; p++) pred[p] = preds[p].copy(cc, vm);
    return copyType(new CachedStep(info, axis, test.copy(), pred));
  }
}
