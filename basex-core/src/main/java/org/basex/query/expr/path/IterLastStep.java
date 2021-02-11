package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative step expression with a single last() predicate.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class IterLastStep extends Step {
  /**
   * Constructor.
   * @param info input info
   * @param axis axis
   * @param test node test
   * @param preds predicates
   */
  IterLastStep(final InputInfo info, final Axis axis, final Test test, final Expr... preds) {
    super(info, axis, test, preds);
  }

  @Override
  public NodeIter iter(final QueryContext qc) {
    return new NodeIter() {
      boolean stop;

      @Override
      public ANode next() throws QueryException {
        if(stop) return null;
        stop = true;

        // return last item
        ANode last = null;
        for(final ANode node : axis.iter(checkNode(qc))) {
          qc.checkStop();
          if(test.matches(node)) last = node.finish();
        }
        return last;
      }
    };
  }

  @Override
  public IterLastStep copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new IterLastStep(info, axis, test.copy(), Arr.copyAll(cc, vm, exprs)));
  }
}
