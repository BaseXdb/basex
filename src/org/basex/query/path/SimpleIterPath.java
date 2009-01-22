package org.basex.query.path;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

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
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      final Item tmp = ctx.item;
      Iter s;
      
      @Override
      public Item next() throws QueryException {
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
