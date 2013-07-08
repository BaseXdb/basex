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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class AxisStep extends Step {

  /**
   * Constructor.
   * @param ii input info
   * @param a axis
   * @param t node test
   * @param p predicates
   */
  AxisStep(final InputInfo ii, final Axis a, final Test t, final Expr[] p) {
    super(ii, a, t, p);
  }

  @Override
  public NodeIter iter(final QueryContext ctx) throws QueryException {
    // evaluate step
    final AxisIter ai = axis.iter(checkNode(ctx));
    final NodeSeqBuilder nc = new NodeSeqBuilder();
    for(ANode n; (n = ai.next()) != null;) {
      if(test.eq(n)) nc.add(n.finish());
    }

    // evaluate predicates
    for(final Expr p : preds) {
      ctx.size = nc.size();
      ctx.pos = 1;
      int c = 0;
      for(int n = 0; n < nc.size(); ++n) {
        ctx.value = nc.get(n);
        final Item i = p.test(ctx, info);
        if(i != null) {
          // assign score value
          nc.get(n).score(i.score());
          nc.nodes[c++] = nc.get(n);
        }
        ctx.pos++;
      }
      nc.size(c);
    }
    return nc;
  }

  @Override
  public Step copy(final QueryContext ctx, final VarScope scp,
      final IntObjMap<Var> vs) {
    final Expr[] pred = new Expr[preds.length];
    for(int i = 0; i < pred.length; i++) pred[i] = preds[i].copy(ctx, scp, vs);
    return copy(new AxisStep(info, axis, test.copy(), pred));
  }
}
