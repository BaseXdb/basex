package org.basex.query.expr.path;

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
 * @author BaseX Team 2005-15, BSD License
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
    final boolean scoring = qc.scoring;
    for(final Expr p : preds) {
      qc.size = nc.size();
      qc.pos = 1;
      int c = 0;
      final long nl = nc.size();
      for(int n = 0; n < nl; ++n) {
        qc.value = nc.get(n);
        final Item i = p.test(qc, info);
        if(i != null) {
          // assign score value
          final ANode node = nc.get(n);
          if(scoring) node.score(i.score());
          nc.nodes[c++] = node;
        }
        qc.pos++;
      }
      nc.size(c);
    }
    return nc;
  }

  @Override
  public Step copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final int pl = preds.length;
    final Expr[] pred = new Expr[pl];
    for(int p = 0; p < pl; p++) pred[p] = preds[p].copy(qc, scp, vs);
    return copy(new AxisStep(info, axis, test.copy(), pred));
  }
}
