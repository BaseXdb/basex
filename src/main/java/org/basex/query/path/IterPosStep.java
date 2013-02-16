package org.basex.query.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * Iterative step expression with numeric predicates.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class IterPosStep extends Step {
  /**
   * Constructor.
   * @param s step reference
   */
  IterPosStep(final Step s) {
    super(s.info, s.axis, s.test, s.preds);
    last = s.last;
    pos = s.pos;
  }

  @Override
  public NodeIter iter(final QueryContext ctx) {
    return new NodeIter() {
      boolean skip;
      AxisIter ai;
      long cpos;

      @Override
      public ANode next() throws QueryException {
        if(skip) return null;
        if(ai == null) ai = axis.iter(checkNode(ctx));

        ANode lnode = null, node;
        while(true) {
          ctx.checkStop();
          node = ai.next();
          if(node == null) {
            skip = last;
            return lnode;
          }

          // evaluate node test
          if(!test.eq(node)) continue;

          // evaluate predicates
          final long cp = ctx.pos;
          final long cs = ctx.size;
          ctx.size = 0;
          ctx.pos = ++cpos;
          final boolean p = preds(node, ctx);
          ctx.pos = cp;
          ctx.size = cs;
          if(p) {
            // check if more results can be expected
            skip = pos != null && pos.skip(ctx);
            return node.finish();
          }
          // remember last node
          if(last) lnode = node.finish();
        }
      }

      @Override
      public boolean reset() {
        ai = null;
        skip = false;
        cpos = 0;
        return true;
      }
    };
  }

  @Override
  public Step copy(final QueryContext ctx, final VarScope scp,
      final IntMap<Var> vs) {
    final Expr[] pred = new Expr[preds.length];
    for(int i = 0; i < pred.length; i++) pred[i] = preds[i].copy(ctx, scp, vs);
    return copy(new IterPosStep(new AxisStep(info, axis, test.copy(), pred)));
  }
}
