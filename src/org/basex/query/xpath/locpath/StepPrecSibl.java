package org.basex.query.xpath.locpath;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.NodeBuilder;

/**
 * Preceding-sibling Step.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class StepPrecSibl extends Step {
  @Override
  protected void eval(final Data data, final int p, final NodeBuilder t) {
    int pre = p;
    final int parn = data.parent(pre, data.kind(pre));

    while(--pre > 0) {
      final int kind = data.kind(pre);
      final int par = data.parent(pre, kind);
      if(par < parn) return;
      
      if(kind == Data.ATTR || par != parn) continue;
      test.eval(data, pre, kind, t);
    };
  }

  @Override
  protected void pos(final XPContext ctx, final Data data, final int p)
      throws QueryException {
    
    final int parn = data.parent(p, data.kind(p));
    int pre = p;
    int pos = 0;

    while(--pre > 0) {
      final int kind = data.kind(pre);
      final int par = data.parent(pre, kind);
      if(par < parn) return;
      
      if(kind == Data.ATTR || par != parn) continue;
      if(test.eval(data, pre, kind) && ++pos == posPred) {
        preds.posEval(ctx, pre, result);
        return;
      }
    };
  }
  
  @Override
  protected void early(final XPContext ctx, final Data data, final int p)
      throws QueryException {

    final int[] pos = new int[preds.size()];
    final int parn = data.parent(p, data.kind(p));
    int pre = p;

    while(--pre > 0) {
      final int kind = data.kind(pre);
      final int par = data.parent(pre, kind);
      if(par < parn) return;
      
      if(kind == Data.ATTR || par != parn) continue;
      if(test.eval(data, pre, kind)) {
        if(!preds.earlyEval(ctx, result, pre, pos)) return;
      }
    };
  }
}
