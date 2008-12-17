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
 * Iterative step expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class SimpleIterStep extends Step {
  /**
   * Constructor.
   * @param a axis
   * @param t node test
   */
  public SimpleIterStep(final Axis a, final Test t) {
    super(a, t);
  }
  
  @Override
  public NodeIter iter(final XQContext ctx) throws XQException {
    final Iter iter = checkCtx(ctx);

    return new NodeIter() {
      NodeIter ir;
      
      @Override
      public Nod next() throws XQException {
        while(true) {
          if(ir == null) {
            final Item it = iter.next();
            if(it == null) return null;
            if(!it.node()) Err.or(NODESPATH, SimpleIterStep.this, it.type);
            ir = axis.init((Nod) it);
          }
          final Nod nod = ir.next();
          if(nod == null) ir = null;
          
          /* [SG], [DS] originally, SimpleIterStep was supposed to contain no
           predicates, so this code is subject to further checks..
           alternatively, a Step/IterStep instance could be created
           for steps with predicates. */

          else if(test.e(nod)) {
            // check preds
            final Item tmp = ctx.item;
            ctx.item = nod;
            for(final Expr p : pred) {
              final Item i = ctx.iter(p).ebv();
              if(i.n() ? i.dbl() == ctx.pos : i.bool()) {
                // assign score value
                nod.score(i.score());
              } else {
                ir = null;
                break;
              }
            }
            ctx.item = tmp;
            if (ir != null) return nod.finish();
          }
        }
      }
    };
  }

  /**
   * Checks if there is anything to sum up.
   * @return boolean sum up
  public boolean sumUp() {
    return axis == Axis.CHILD && test instanceof NameTest && pred.length == 0;
  }
   */
}
