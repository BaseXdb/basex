package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * Iterative step expression with one or more simple numeric predicates.
 *
 * @author BaseX Team 2005-19, BSD License
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
      final ItrPos[] posExpr = new ItrPos[exprs.length];
      final long[] cPos = new long[exprs.length];
      BasicNodeIter iter;
      boolean skip;

      @Override
      public ANode next() throws QueryException {
        if(skip) return null;
        if(iter == null) {
          iter = axis.iter(checkNode(qc));
          final int el = exprs.length;
          for(int e = 0; e < el; e++) {
            final Expr pred = exprs[e];
            if(pred instanceof ItrPos) {
              posExpr[e] = (ItrPos) pred;
            } else if(numeric(pred)) {
              // pre-evaluate numeric position
              final Item item = pred.atomItem(qc, info);
              if(item == null) return null;
              final double dbl = toDouble(item);
              final long lng = (long) dbl;
              if(dbl != lng) return null;
              final Expr expr = ItrPos.get(lng, info);
              if(expr instanceof ItrPos) posExpr[e] = (ItrPos) expr;
              else return null;
            }
          }
        }

        for(final ANode node : iter) {
          qc.checkStop();
          if(test.eq(node) && preds(node)) return node.finish();
        }
        return null;
      }

      private boolean preds(final ANode node) throws QueryException {
        final QueryFocus qf = qc.focus;
        final Value cv = qf.value;
        qf.value = node;
        try {
          double s = qc.scoring ? 0 : -1;
          final int pl = exprs.length;
          for(int p = 0; p < pl; p++) {
            final Expr pred = exprs[p];
            final ItrPos pos = posExpr[p];
            if(pos == null) {
              final Item tst = pred.test(qc, info);
              if(tst == null) return false;
              if(s != -1) s += tst.score();
            } else {
              final long ps = ++cPos[p];
              if(!pos.matches(ps)) return false;
              if(pos.skip(ps)) skip = true;
            }
          }
          if(s > 0) node.score(Scoring.avg(s, exprs.length));
        } finally {
          qf.value = cv;
        }
        return true;
      }
    };
  }

  @Override
  public Step copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new IterPosStep(info, axis, test.copy(), Arr.copyAll(cc, vm, exprs)));
  }
}
