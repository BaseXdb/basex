
package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Step expression: iterative evaluation (no positional access).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IterStep extends Step {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param axis axis
   * @param test node test
   * @param preds predicates
   */
  IterStep(final InputInfo info, final Axis axis, final Test test, final Expr... preds) {
    super(info, axis, test, preds);
  }

  @Override
  public NodeIter iter(final QueryContext qc) throws QueryException {
    final BasicNodeIter iter = axis.iter(checkNode(qc));
    return new NodeIter() {
      @Override
      public ANode next() throws QueryException {
        for(final ANode node : iter) {
          qc.checkStop();
          if(test.matches(node) && test(node, qc)) return node.finish();
        }
        return null;
      }
    };
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    for(final ANode node : axis.iter(checkNode(qc))) {
      qc.checkStop();
      if(test.matches(node) && test(node, qc)) return true;
    }
    return false;
  }

  @Override
  public IterStep copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new IterStep(info, axis, test.copy(), Arr.copyAll(cc, vm, exprs)));
  }
}
