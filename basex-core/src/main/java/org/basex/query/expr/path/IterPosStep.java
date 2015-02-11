package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative step expression with one or more simple numeric predicates.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class IterPosStep extends Step {
  /**
   * Constructor.
   * @param info input info
   * @param axis axis
   * @param test node test
   * @param preds predicates
   */
  IterPosStep(final InputInfo info, final Axis axis, final Test test, final Expr... preds) {
    super(info, axis, test, preds);
  }

  @Override
  public NodeIter iter(final QueryContext qc) {
    return new NodeIter() {
      final Pos[] posExpr = new Pos[preds.length];
      final long[] cPos = new long[preds.length];
      boolean skip;
      AxisIter ai;

      @Override
      public ANode next() throws QueryException {
        if(skip) return null;
        if(ai == null) {
          ai = axis.iter(checkNode(qc));
          final int pl = preds.length;
          for(int p = 0; p < pl; p++) {
            final Expr pred = preds[p];
            if(pred instanceof Pos) {
              posExpr[p] = (Pos) pred;
            } else if(num(pred)) {
              // pre-evaluate numeric position
              final double dbl = toDouble(pred, qc);
              final long lng = (long) dbl;
              if(dbl != lng) return null;
              final Expr e = Pos.get(lng, info);
              if(e instanceof Pos) posExpr[p] = (Pos) e;
              else return null;
            }
          }
        }

        for(ANode node; (node = ai.next()) != null;) {
          qc.checkStop();
          if(test.eq(node) && preds(node)) return node.finish();
        }
        return null;
      }

      /**
       * Evaluates the predicates.
       * @param node input node
       * @return result of check
       * @throws QueryException query exception
       */
      private boolean preds(final ANode node) throws QueryException {
        final int pl = preds.length;
        for(int p = 0; p < pl; p++) {
          final Expr pred = preds[p];
          final Pos pos = posExpr[p];
          if(pos == null) {
            qc.value = node;
            if(pred.test(qc, info) == null) return false;
          } else {
            final long ps = ++cPos[p];
            if(!pos.matches(ps)) return false;
            if(pos.skip(ps)) skip = true;
          }
        }
        return true;
      }
    };
  }

  @Override
  public Step copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new IterPosStep(info, axis, test.copy(), Arr.copyAll(qc, scp, vs, preds)));
  }
}
