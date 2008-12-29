package org.basex.query.xpath.path;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.NodeBuilder;

/**
 * Following Step.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class StepFoll extends Step {
  @Override
  protected void eval(final Data data, final int p, final NodeBuilder t) {
    int kind = data.kind(p);
    if(kind == Data.ATTR) return;

    final int size = data.meta.size;
    int pre = p + data.size(p, kind);

    while(pre != size) {
      kind = data.kind(pre);
      test.eval(data, pre, kind, t);
      pre += data.attSize(pre, kind);
    }
  }

  @Override
  protected void pos(final XPContext ctx, final Data data, final int p)
      throws QueryException {

    int kind = data.kind(p);
    if(kind == Data.ATTR) return;
    
    int pos = 0;
    final int size = data.meta.size;
    int pre = p + data.size(p, kind);
    
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
  protected void early(final XPContext ctx, final Data data, final int p)
      throws QueryException {

    int kind = data.kind(p);
    if(kind == Data.ATTR) return;
    
    final int[] pos = new int[preds.size()];
    final int size = data.meta.size;
    int pre = p + data.size(p, kind);
    
    while(pre != size) {
      kind = data.kind(pre);
      if(test.eval(data, pre, kind)) {
        if(!preds.earlyEval(ctx, result, pre, pos)) return;
      }
      pre += data.attSize(pre, kind);
    }
  }
}
