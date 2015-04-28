
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
 * @author BaseX Team 2005-15, BSD License
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
      BasicNodeIter iter;

      @Override
      public ANode next() throws QueryException {
        if(iter == null) iter = axis.iter(checkNode(qc));
        for(ANode node; (node = iter.next()) != null;) {
          qc.checkStop();
          if(test.eq(node) && preds(node, qc)) return node.finish();
        }
        return null;
      }
    };
  }

  @Override
  public IterStep copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new IterStep(info, axis, test.copy(), Arr.copyAll(qc, scp, vs, preds)));
  }
}
