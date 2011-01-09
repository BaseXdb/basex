package org.basex.query.path;

import static org.basex.query.util.Err.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Nod;
import org.basex.query.item.Value;
import org.basex.query.iter.NodeIter;
import org.basex.util.InputInfo;

/**
 * Iterative step expression without positional predicates.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
final class IterStep extends AxisStep {
  /**
   * Constructor.
   * @param ii input info
   * @param a axis
   * @param t node test
   * @param p predicates
   */
  IterStep(final InputInfo ii, final Axis a, final Test t, final Expr[] p) {
    super(ii, a, t, p);
  }

  @Override
  public NodeIter iter(final QueryContext ctx) {
    return new NodeIter() {
      NodeIter ir;

      @Override
      public Nod next() throws QueryException {
        if(ir == null) {
          final Value v = checkCtx(ctx);
          if(!v.node()) NODESPATH.thrw(input, IterStep.this, v.type);
          ir = axis.iter((Nod) v);
        }

        while(true) {
          ctx.checkStop();
          final Nod nod = ir.next();
          if(nod == null) return null;
          // evaluate node test and predicates
          if(test.eval(nod) && preds(nod, ctx)) return nod.finish();
        }
      }

      @Override
      public boolean reset() {
        ir = null;
        return true;
      }
    };
  }
}
