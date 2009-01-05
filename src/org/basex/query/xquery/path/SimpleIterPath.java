package org.basex.query.xquery.path;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;

/**
 * Iterative path expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Dennis Stratmann
 */
public final class SimpleIterPath extends AxisPath {
  /**
   * Constructor.
   * @param r root expression
   * @param s location steps
   */
  public SimpleIterPath(final Expr r, final Step... s) {
    super(r, s);
  }

  @Override
  public Iter iter(final XQContext ctx) {
    return new Iter() {
      final Item tmp = ctx.item;
      Iter s;
      
      @Override
      public Item next() throws XQException  {
        if(s == null) {
          if(root != null) ctx.item = ctx.iter(root).finish();
          s = ctx.iter(step[0]);
        }
        final Item it = s.next();
        ctx.item = tmp;
        return it;
      }
    };
  }
}
