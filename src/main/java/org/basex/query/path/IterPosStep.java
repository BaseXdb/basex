package org.basex.query.path;

import static org.basex.query.util.Err.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Nod;
import org.basex.query.item.Value;
import org.basex.query.iter.NodeIter;

/**
 * Iterative step expression with position predicates.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class IterPosStep extends Step {
  /**
   * Constructor.
   * @param s step reference
   */
  IterPosStep(final Step s) {
    super(s.input, s.axis, s.test, s.pred);
    last = s.last;
    pos = s.pos;
  }

  @Override
  public NodeIter iter(final QueryContext ctx) {
    return new NodeIter() {
      boolean skip;
      NodeIter ir;
      long cpos;

      @Override
      public Nod next() throws QueryException {
        if(skip) return null;

        if(ir == null) {
          final Value v = checkCtx(ctx);
          if(!v.node()) NODESPATH.thrw(input, IterPosStep.this, v.type);
          ir = axis.iter((Nod) v);
        }
        
        Nod lnod = null;
        while(true) {
          ctx.checkStop();

          final Nod nod = ir.next();
          if(nod == null) {
            skip = last;
            return lnod;
          }

          // evaluate node test
          if(test.eval(nod)) {
            // set context item and position
            ctx.pos = ++cpos;

            // evaluate predicates
            if(preds(nod, ctx)) {
              // check if no more results are to be expected
              skip = pos != null && pos.skip(ctx);
              return nod.finish();
            }
            // remember last node
            if(last) lnod = nod.finish();
          }
        }
      }
      
      @Override
      public boolean reset() {
        ir = null;
        skip = false;
        cpos = 0;
        return true;
      }
    };
  }
}
