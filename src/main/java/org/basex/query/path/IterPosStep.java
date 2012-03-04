package org.basex.query.path;

import static org.basex.query.util.Err.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.Value;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.NodeIter;

/**
 * Iterative step expression with numeric predicates.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class IterPosStep extends AxisStep {
  /**
   * Constructor.
   * @param s step reference
   */
  IterPosStep(final AxisStep s) {
    super(s.input, s.axis, s.test, s.preds);
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

        if(ai == null) {
          final Value v = checkCtx(ctx);
          if(!v.type.isNode()) NODESPATH.thrw(input, IterPosStep.this, v.type);
          ai = axis.iter((ANode) v);
        }

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
}
