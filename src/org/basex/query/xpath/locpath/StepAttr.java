package org.basex.query.xpath.locpath;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.NodeBuilder;

/**
 * Attribute Step.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class StepAttr extends Step {
  @Override
  protected void eval(final Data data, final int p, final NodeBuilder t) {
    final int size = p + data.attSize(p, data.kind(p));
    int pre = p;

    while(++pre != size) test.eval(data, pre, Data.ATTR, t);
  }

  @Override
  protected void pos(final XPContext ctx, final Data data, final int p)
      throws QueryException {

    int pos = 0;
    final int size = p + data.attSize(p, data.kind(p));
    int pre = p;

    while(++pre != size) {
      if(test.eval(data, pre, Data.ATTR) && ++pos == posPred) {
        preds.posEval(ctx, pre, result);
        return;
      }
    }
  }

  @Override
  protected void early(final XPContext ctx, final Data data, final int p)
      throws QueryException {

    final int[] pos = new int[preds.size()];
    final int size = p + data.attSize(p, data.kind(p));
    int pre = p;

    while(++pre != size) {
      if(test.eval(data, pre, Data.ATTR)) {
        if(!preds.earlyEval(ctx, result, pre, pos)) return;
      }
    }
  }
}
