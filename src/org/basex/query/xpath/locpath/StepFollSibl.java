package org.basex.query.xpath.locpath;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.NodeBuilder;

/**
 * Following-sibling Step.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class StepFollSibl extends Step {
  @Override
  protected void eval(final Data data, final int p, final NodeBuilder t) {
    int kind = data.kind(p);
    if(kind == Data.ATTR) return;

    final int par = data.parent(p, kind);
    final int size = par + data.size(par, data.kind(par));
    int pre = p + data.size(p, kind);

    while(pre != size) {
      kind = data.kind(pre);
      test.eval(data, pre, kind, t);
      pre += data.size(pre, kind);
    }
  }

  @Override
  protected void pos(final XPContext ctx, final Data data, final int p)
      throws QueryException {

    int kind = data.kind(p);
    if(kind == Data.ATTR) return;

    final int par = data.parent(p, kind);
    final int size = par + data.size(par, data.kind(par));
    int pre = p + data.size(p, kind);
    int pos = 0;
    
    while(pre != size) {
      kind = data.kind(pre);
      if(test.eval(data, pre, kind) && ++pos == posPred) {
        preds.posEval(ctx, pre, result);
        return;
      }
      pre += data.size(pre, kind);
    }
  }
  
  @Override
  protected void early(final XPContext ctx, final Data data, final int p)
      throws QueryException {

    int kind = data.kind(p);
    if(kind == Data.ATTR) return;

    final int[] pos = new int[preds.size()];
    final int par = data.parent(p, kind);
    final int size = par + data.size(par, data.kind(par));
    int pre = p + data.size(p, kind);
    
    while(pre != size) {
      kind = data.kind(pre);
      if(test.eval(data, pre, kind)) {
        if(!preds.earlyEval(ctx, result, pre, pos)) return;
      }
      pre += data.size(pre, kind);
    }
  }
}
