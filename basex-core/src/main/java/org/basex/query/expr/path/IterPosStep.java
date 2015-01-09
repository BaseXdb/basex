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
  /**
   * Constructor.
   * @param s step reference
   */
  IterPosStep(final Step s) {
    super(s.info, s.axis, s.test, s.preds);
    last = s.last;
    pos = s.pos;
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

        ANode lnode = null;
        while(true) {
          qc.checkStop();
          final ANode node = ai.next();
          if(node == null) {
            skip = last;
            return lnode;
          }

          // evaluate node test
          if(!test.eq(node)) continue;

          // evaluate predicates
          final long cp = qc.pos, cs = qc.size;
          qc.size = 0;
          qc.pos = ++cpos;
          try {
            if(preds(node, qc)) {
              // check if more results can be expected
              skip = pos != null && pos.skip(qc);
              return node.finish();
            }
            // remember last node
            if(last) lnode = node.finish();
          } finally {
            qc.pos = cp;
            qc.size = cs;
          }
        }
      }
    };
  }

  @Override
  public Step copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final AxisStep step = new AxisStep(info, axis, test.copy(), Arr.copyAll(qc, scp, vs, preds));
    return copy(new IterPosStep(step));
  }
}
