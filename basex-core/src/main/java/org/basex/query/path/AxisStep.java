package org.basex.query.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Axis step expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class AxisStep extends Step {
  /**
   * Constructor.
   * @param info input info
   * @param axis axis
   * @param test node test
   * @param preds predicates
   */
  AxisStep(final InputInfo info, final Axis axis, final Test test, final Expr[] preds) {
    super(info, axis, test, preds);
  }

  @Override
  public NodeSeqBuilder iter(final QueryContext qc) throws QueryException {
    // evaluate step
    final AxisIter ai = axis.iter(checkNode(qc));
    final NodeSeqBuilder nc = new NodeSeqBuilder();
    for(ANode n; (n = ai.next()) != null;) {
      if(test.eq(n)) nc.add(n.finish());
    }

    // evaluate predicates
    for(final Expr p : preds) {
      qc.size = nc.size();
      qc.pos = 1;
      int c = 0;
      for(int n = 0; n < nc.size(); ++n) {
        qc.value = nc.get(n);
        final Item i = p.test(qc, info);
        if(i != null) {
          // assign score value
          nc.get(n).score(i.score());
          nc.nodes[c++] = nc.get(n);
        }
        qc.pos++;
      }
      nc.size(c);
    }
    return nc;
  }

  @Override
  public Step copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Expr[] pred = new Expr[preds.length];
    for(int i = 0; i < pred.length; i++) pred[i] = preds[i].copy(qc, scp, vs);
    return copy(new AxisStep(info, axis, test.copy(), pred));
  }
}
