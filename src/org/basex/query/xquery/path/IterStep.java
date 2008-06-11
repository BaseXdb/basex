package org.basex.query.xquery.path;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Node;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.util.Err;

/**
 * Path expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class IterStep extends Step {
  /**
   * Constructor.
   * @param a axis
   * @param t node test
   * @param p predicates
   */
  public IterStep(final Axis a, final Test t, final Expr[] p) {
    super(a, t, p);
  }
  
  @Override
  public NodeIter iter(final XQContext ctx) throws XQException {
    final Item item = ctx.item;

    if(item == null) Err.or(XPNOCTX, this);
    final Iter iter = item.iter();

    // no special predicate treatment?
    return new NodeIter() {
      /** Temporary iterator. */
      NodeIter ir;
      
      @Override
      public Node next() throws XQException {
        while(true) {
          if(ir == null) {
            final Item it = iter.next();
            if(it == null) {
              ctx.item = item;
              return null;
            }
            if(!it.node()) Err.or(NODESPATH, this, it);
            ir = axis.init((Node) it);
          }
          final Node nod = ir.next();
          if(nod != null) {
            if(test.e(nod, ctx)) {
              // evaluates predicates
              boolean add = true;
              for(final Expr e : expr) {
                ctx.item = nod;
                final Item i = ctx.iter(e).ebv();
                if(i.bool()) {
                  // assign score value
                  nod.score(i.score());
                } else {
                  add = false;
                  break;
                }
              }
              if(add) {
                ctx.item = item;
                return nod.finish();
              }
            }
          } else {
            ir = null;
          }
        }
      }

      @Override
      public String toString() {
        return IterStep.this.toString();
      }
    };
  }
}
