package org.basex.query.xquery.path;

import static org.basex.query.xquery.XQText.*;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.util.Err;

/**
 * Path expression. Mustn't be called with more than one predicate.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class IterStep extends Step {
  /** Flag is set to true if predicate has last function. */
  final boolean last;
  /** Flag is set to true if predicate has a numeric value. */
  final boolean num;

  /**
   * Constructor.
   * @param a axis
   * @param t node test
   * @param p predicates
   * @param l lastFlag is true if predicate has a last function
   * @param n numberFlag is true if predicate has a numeric value
   */
  public IterStep(final Axis a, final Test t, final Expr[] p, 
      final boolean l, final boolean n) {
    super(a, t, p);
    last = l;
    num = n;
  }

  @Override
  public NodeIter iter(final XQContext ctx) {
    return new NodeIter() {
      final Item ci = ctx.item;
      final int cp = ctx.pos;
      boolean more = true;
      NodeIter ir;
      Iter iter;
      
      @Override
      public Nod next() throws XQException {
        if(more) {
          if(iter == null) {
            iter = checkCtx(ctx);
            ctx.pos = 1;
          }
          
          Nod temp = null;
          while(true) {
            if(ir == null) {
              final Item i = iter.next();
              if(i == null) {
                ctx.item = ci;
                ctx.pos = cp;
                if(temp != null) {
                  more = false;
                  return temp;
                }
                return null;
              }
              if(!i.node()) Err.or(NODESPATH, IterStep.this, i.type);
              ir = axis.init((Nod) i);
            }
            
            final Nod nod = ir.next();
            if(nod != null) {
              if(test.e(nod)) {
                // evaluates predicates
                ctx.item = nod;
                final Item i = ctx.iter(pred[0]).ebv();
  
                final boolean found = i.n() ? i.dbl() == ctx.pos : i.bool();
                ctx.pos++;
                
                if(found) {
                  // assign score value
                  nod.score(i.score());
                  if(num) more = false;
                  ctx.item = ci;
                  return nod.finish();
                }
                // remember last node
                if(last) temp = nod.finish();
              }
            } else {
              ir = null;
            }
          }
        }
        return null;
      } 
    };
  }
}
