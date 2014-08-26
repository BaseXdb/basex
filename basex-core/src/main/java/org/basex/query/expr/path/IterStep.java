package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative step expression without numeric predicates.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class IterStep extends Step {
  /**
   * Constructor.
   * @param info input info
   * @param axis axis
   * @param test node test
   * @param preds predicates
   */
  IterStep(final InputInfo info, final Axis axis, final Test test, final Expr[] preds) {
    super(info, axis, test, preds);
  }

  @Override
  public NodeIter iter(final QueryContext qc) {
    return new NodeIter() {
      AxisIter ai;

      @Override
      public ANode next() throws QueryException {
        if(ai == null) ai = axis.iter(checkNode(qc));
        while(true) {
          qc.checkStop();
          final ANode node = ai.next();
          if(node == null) return null;
          // evaluate node test and predicates
          if(test.eq(node) && preds(node, qc)) return node.finish();
        }
      }
    };
  }

  @Override
  public IterStep copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Expr[] pred = new Expr[preds.length];
    for(int i = 0; i < pred.length; i++) pred[i] = preds[i].copy(qc, scp, vs);
    return copy(new IterStep(info, axis, test.copy(), pred));
  }
}
