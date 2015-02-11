package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * Iterative step expression with last() predicates.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class IterLastStep extends Step {
  /**
   * Constructor.
   * @param step step reference
   */
  IterLastStep(final Step step) {
    super(step.info, step.axis, step.test, step.preds);
  }

  @Override
  public NodeIter iter(final QueryContext qc) {
    return new NodeIter() {
      boolean stop;

      @Override
      public ANode next() throws QueryException {
        if(stop) return null;
        stop = true;

        // return last items
        final AxisIter iter = axis.iter(checkNode(qc));
        ANode litem = null;
        final Test tst = test;
        for(ANode item; (item = iter.next()) != null;) {
          qc.checkStop();
          if(tst.eq(item)) litem = item.finish();
        }
        return litem == null ? null : litem;
      }
    };
  }

  @Override
  public IterLastStep copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new IterLastStep(
        new CachedStep(info, axis, test.copy(), Arr.copyAll(qc, scp, vs, preds))));
  }
}
