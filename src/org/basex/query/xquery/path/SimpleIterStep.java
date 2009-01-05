package org.basex.query.xquery.path;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
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
          final Nod n = ir.next();
          if(n == null) ir = null;
          else if(test.eval(n)) return n.finish();
        }
      }
    };
  }
}
