package org.basex.query.xpath.path;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.NodeBuilder;

/**
 * Parent Step.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class StepParent extends Step {
  @Override
  protected void eval(final Data data, final int p, final NodeBuilder t) {
    final int k = data.kind(p);
    if(k == Data.DOC) return;
    final int pre = data.parent(p, k);
    test.eval(data, pre, data.kind(pre), t);
  }

  @Override
  protected void pos(final XPContext ctx, final Data data, final int p) {
    if(posPred == 1) eval(data, p, result);
  }

  @Override
  protected void early(final XPContext ctx, final Data data, final int p)
    throws QueryException {

    final int pre = data.parent(p, data.kind(p));
    final int[] pos = new int[preds.size()];
    if(test.eval(data, pre, data.kind(pre))) {
      if(!preds.earlyEval(ctx, result, pre, pos)) return;
    }
  }
}
