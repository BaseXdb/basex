package org.basex.query.xpath.locpath;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.NodeBuilder;

/**
 * Self Step.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class StepSelf extends Step {
  @Override
  protected void eval(final Data data, final int p, final NodeBuilder t) {
    test.eval(data, p, data.kind(p), t);
  }

  @Override
  protected void pos(final XPContext ctx, final Data data, final int p) {
    if(posPred == 1) eval(data, p, result);
  }
  
  @Override
  protected void early(final XPContext ctx, final Data data, final int p)
    throws QueryException {

    final int[] pos = new int[preds.size()];
    if(test.eval(data, p, data.kind(p))) {
      if(!preds.earlyEval(ctx, result, p, pos)) return;
    }
  }
}
