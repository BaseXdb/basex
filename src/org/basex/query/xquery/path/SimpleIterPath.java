package org.basex.query.xquery.path;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodeIter;

/**
 * Iterative path expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Dennis Stratmann
 */
public final class SimpleIterPath extends Path {
  /**
   * Constructor.
   * @param r root expression
   * @param p expression list
   */
  public SimpleIterPath(final Expr r, final Expr[] p) {
    super(r, p);
  }

  @Override
  public Iter iter(final XQContext ctx) {
    return new Iter() {
      // [DS] here's where the bug occurred:
      // "first" was globally defined in this class; this is why
      // it was always false after the first iter() call.
      //
      // Second - another small bug: ctx.item must be reset to
      // the initial value after the last value has been returned
      
      final Item tmp = ctx.item;
      boolean first = true;
      NodeIter step;
      Item item;
      
      @Override
      public Item next() throws XQException  {
        if(first) {
          item = ctx.iter(root).finish();
          ctx.item = item;
          // expr[0] is always a SimpleIterStep, so casting is OK
          step = (NodeIter) ctx.iter(expr[0]);
          first = false;
        }
        final Nod it = step.next();
        ctx.item = tmp;
        return it;
      }
    };
  }
}
