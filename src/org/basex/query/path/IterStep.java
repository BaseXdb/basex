package org.basex.query.path;

import static org.basex.query.QueryText.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Pos;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeIter;
import org.basex.query.util.Err;

/**
 * Path expression. Mustn't be called with more than one predicate.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class IterStep extends Step {
  /** Flag is set to true if predicate has last function. */
  final boolean last;
  /** Optional position predicate. */
  final Pos pos;

  /**
   * Constructor.
   * @param a axis
   * @param t node test
   * @param p predicates
   * @param ps position predicate
   * @param l lastFlag is true if predicate has a last function
   */
  public IterStep(final Axis a, final Test t, final Expr[] p, 
      final Pos ps, final boolean l) {
    super(a, t, p);
    last = l;
    pos = ps;
  }

  @Override
  public NodeIter iter(final QueryContext ctx) {
    return new NodeIter() {
      final Item ci = ctx.item;
      final long cp = ctx.pos;
      boolean finish;
      NodeIter ir;
      Iter iter;

      @Override
      public Nod next() throws QueryException {
        if(finish) return null;

        if(iter == null) {
          iter = checkCtx(ctx).iter();
          ctx.pos = 0;
        }
        
        Nod temp = null;
        while(true) {
          if(ir == null) {
            final Item i = iter.next();
            if(i == null) break;
            if(!i.node()) Err.or(NODESPATH, IterStep.this, i.type);
            ir = axis.init((Nod) i);
          }
          
          final Nod nod = ir.next();
          if(nod != null) {
            if(test.eval(nod)) {
              // evaluates predicates
              ctx.item = nod;
              ctx.pos++;
              final Item i = pred[0].test(ctx);

              if(i != null) {
                // assign score value
                nod.score(i.score());
                // check if no more results are to be expected
                if(pos != null && pos.last(ctx)) ir = null;
                return nod.finish();
              }
              // remember last node
              if(last) temp = nod.finish();
            }
          } else {
            ir = null;
          }
        }

        ctx.item = ci;
        ctx.pos = cp;
        finish = last;
        return temp;
      } 
    };
  }
}
