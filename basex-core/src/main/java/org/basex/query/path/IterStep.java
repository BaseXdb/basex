package org.basex.query.path;

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
   * @param ii input info
   * @param a axis
   * @param t node test
   * @param p predicates
   */
  IterStep(final InputInfo ii, final Axis a, final Test t, final Expr[] p) {
    super(ii, a, t, p);
  }

  @Override
  public NodeIter iter(final QueryContext ctx) {
    return new NodeIter() {
      AxisIter ai;

      @Override
      public ANode next() throws QueryException {
        if(ai == null) ai = axis.iter(checkNode(ctx));
        while(true) {
          ctx.checkStop();
          final ANode node = ai.next();
          if(node == null) return null;
          // evaluate node test and predicates
          if(test.eq(node) && preds(node, ctx)) return node.finish();
        }
      }

      @Override
      public boolean reset() {
        ai = null;
        return true;
      }
    };
  }

  @Override
  public IterStep copy(final QueryContext ctx, final VarScope scp,
      final IntObjMap<Var> vs) {
    final Expr[] pred = new Expr[preds.length];
    for(int i = 0; i < pred.length; i++) pred[i] = preds[i].copy(ctx, scp, vs);
    return copy(new IterStep(info, axis, test.copy(), pred));
  }
}
