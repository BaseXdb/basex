package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative step expression with one or more simple numeric predicates.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class IterPosStep extends Step {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
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
      final long[] cPos = new long[exprs.length];
      BasicNodeIter iter;
      boolean skip;

      @Override
      public ANode next() throws QueryException {
        if(skip) return null;
        if(iter == null) iter = axis.iter(checkNode(qc));

        for(final ANode node : iter) {
          qc.checkStop();
          if(test.matches(node) && preds(node)) return node.finish();
        }
        return null;
      }

      private boolean preds(final ANode node) throws QueryException {
        final QueryFocus qf = qc.focus;
        final Value qv = qf.value;
        qf.value = node;
        try {
          final int pl = exprs.length;
          for(int p = 0; p < pl; p++) {
            final Expr pred = exprs[p];
            if(pred instanceof CmpPos) {
              final int t = ((CmpPos) pred).test(++cPos[p], qc);
              if(t == 0) return false;
              if(t == 2) skip = true;
            } else if(!pred.test(qc, info, 0)) {
              return false;
            }
          }
        } finally {
          qf.value = qv;
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
