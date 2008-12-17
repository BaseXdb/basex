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
  boolean lastFlag;
  /** Flag is set to true if predicate has a numeric value. */
  boolean numFlag;

  /**
   * Constructor.
   * @param a axis
   * @param t node test
   * @param p predicates
   * @param last lastFlag is true if predicate has a last function
   * @param num numberFlag is true if predicate has a numeric value
   */
  public IterStep(final Axis a, final Test t, final Expr[] p, 
      final boolean last, final boolean num) {
    super(a, t, p);
    lastFlag = last;
    numFlag = num;
  }

  @Override
  public NodeIter iter(final XQContext ctx) throws XQException {
    final Item ci = ctx.item;
    final int cp = ctx.pos;
    final Iter iter = checkCtx(ctx);

    // no special predicate treatment?
    return new NodeIter() {
      /** Temporary iterator. */
      NodeIter ir;
      boolean first = true;
      boolean finished = false;
      Nod nod;
      Nod temp;
      
      @Override
      public Nod next() throws XQException {
        if(finished) return null;
        
        if(first) {
          first = false;
          ctx.pos = 1;
        }
        
        while(true) {
          if(ir == null) {
            final Item it = iter.next();
            if(it == null) {
              ctx.item = ci;
              ctx.pos = cp;
              if(lastFlag && temp != null) {
                finished = true;
                return temp;
              }
              return null;
            }
            if(!it.node()) Err.or(NODESPATH, IterStep.this, it.type);
            ir = axis.init((Nod) it);
          }
          nod = ir.next();
          if(nod != null) {
            if(test.e(nod)) {
              // evaluates predicates
              boolean add = true;

              ctx.item = nod;
              final Item i = ctx.iter(pred[0]).ebv();
              if(i.n() ? i.dbl() == ctx.pos : i.bool()) {
                // assign score value
                nod.score(i.score());
              } else {
                add = false;
              }
              temp = lastFlag ? nod.finish() : null;
              ctx.pos++;

              if(add) {
                ctx.item = ci;
                if(numFlag) {
                  finished = true;
                  return nod.finish();
                }
                return nod.finish();
              }
            }
          } else {
            ir = null;
          }
        }
      } 
    };
  }
}
