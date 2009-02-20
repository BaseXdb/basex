package org.basex.query.path;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Seq;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeIter;
import org.basex.query.util.Err;

/**
 * Iterative step expression without predicates.
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
  public Expr comp(final QueryContext ctx) throws QueryException {
    return !test.comp(ctx) ? Seq.EMPTY : this;
  }
  
  @Override
  public NodeIter iter(final QueryContext ctx) throws QueryException {
    final Iter iter = checkCtx(ctx).iter();

    return new NodeIter() {
      NodeIter ir;

      @Override
      public Nod next() throws QueryException {
        while(true) {
          if(ir == null) {
            final Item it = iter.next();
            if(it == null) return null;
            if(!it.node()) Err.or(NODESPATH, SimpleIterStep.this, it.type);
            ir = axis.init((Nod) it);
          }
          final Nod n = ir.next();
          if(n == null) {
            ir = null;
          } else if(test.eval(n)) {
            return n.finish();
          }
        }
      }
    };
  }
}
