package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative step expression with one or more simple numeric predicates.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class IterPosStep extends Step {
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
      final CmpPos[] posExpr = new CmpPos[exprs.length];
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
            final Expr expr = exprs[e];
            if(expr instanceof CmpPos) {
              posExpr[e] = (CmpPos) expr;
            } else if(numeric(expr)) {
              // pre-evaluate numeric position
              final Item item = expr.item(qc, info);
              if(item == Empty.VALUE) return null;
              final Expr ex = ItrPos.get(toDouble(item), info);
              if(!(ex instanceof CmpPos)) return null;
              posExpr[e] = (CmpPos) ex;
            }
          }
        }

        for(final ANode node : iter) {
          qc.checkStop();
          if(test.matches(node) && preds(node)) return node.finish();
        }
        return null;
      }

      private boolean preds(final ANode node) throws QueryException {
        final QueryFocus qf = qc.focus;
        final Value cv = qf.value;
        qf.value = node;
        try {
          final int pl = exprs.length;
          for(int p = 0; p < pl; p++) {
            final Expr pred = exprs[p];
            final CmpPos pos = posExpr[p];
            if(pos == null) {
              final Item tst = pred.test(qc, info);
              if(tst == null) return false;
            } else {
              final long ps = ++cPos[p];
              final int t = pos.test(ps, qc);
              if(t == 0) return false;
              if(t == 2) skip = true;
            }
          }
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
