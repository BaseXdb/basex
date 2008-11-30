package org.basex.query.xpath.path;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.NodeBuilder;

/**
 * Descendant-or-self Step.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class StepDescOrSelf extends Step {
  @Override
  protected void eval(final Data data, final int p, final NodeBuilder t) {
    int kind = data.kind(p);
    final int size = p + data.size(p, kind);
    int pre = p;

    while(pre != size) {
      kind = data.kind(pre);
      test.eval(data, pre, kind, t);
      pre += data.attSize(pre, kind);
    }
  }

  @Override
  protected void pos(final XPContext ctx, final Data data,
      final int p) throws QueryException {

    int pos = 0;
    int kind = data.kind(p);
    final int size = p + data.size(p, kind);
    int pre = p;

    while(pre != size) {
      kind = data.kind(pre);
      if(test.eval(data, pre, kind) && ++pos == posPred) {
        preds.posEval(ctx, pre, result);
        return;
      }
      pre += data.attSize(pre, kind);
    }
  }
  
  @Override
  protected void early(final XPContext ctx, final Data data,
      final int p) throws QueryException {

    final int[] pos = new int[preds.size()];
    int kind = data.kind(p);
    final int size = p + data.size(p, kind);
    int pre = p;

    while(pre != size) {
      kind = data.kind(pre);
      if(test.eval(data, pre, kind)) {
        if(!preds.earlyEval(ctx, result, pre, pos)) return;
      }
      pre += data.attSize(pre, kind);
    }
  }
}
