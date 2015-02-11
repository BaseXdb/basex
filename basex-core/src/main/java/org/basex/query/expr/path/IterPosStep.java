package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * Iterative step expression with numeric predicates.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class IterPosStep extends Step {
  /** Positional predicate. */
  private final Pos pos;

  /**
   * Constructor.
   * @param step step reference
   * @param pos positional predicate
   */
  IterPosStep(final Step step, final Pos pos) {
    super(step.info, step.axis, step.test, step.preds);
    this.pos = pos;
  }

  @Override
  public NodeIter iter(final QueryContext qc) {
    return new NodeIter() {
      boolean skip;
      AxisIter ai;
      long cpos;

      @Override
      public ANode next() throws QueryException {
        if(skip) return null;
        if(ai == null) ai = axis.iter(checkNode(qc));

        for(ANode node; (node = ai.next()) != null;) {
          qc.checkStop();
          // evaluate node and predicate test
          if(test.eq(node) && pos.matches(++cpos)) {
            if(pos.skip(cpos)) skip = true;
            return node.finish();
          }
        }
        return null;
      }
    };
  }

  @Override
  public IterPosStep copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new IterPosStep(
        new CachedStep(info, axis, test.copy(), Arr.copyAll(qc, scp, vs, preds)), pos));
  }
}
