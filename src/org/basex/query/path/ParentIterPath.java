package org.basex.query.path;

import static org.basex.query.QueryText.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;

/**
 * Iterative path expression for parent steps onley.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class ParentIterPath extends AxisPath {
  /**
   * Constructor.
   * @param r root expression
   * @param s location steps
   */
  public ParentIterPath(final Expr r, final Step[] s) {
    super(r, s);
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      
      @Override
      public Item next() throws QueryException {
        final Item c = ctx.item;
        final long cs = ctx.size;
        final long cp = ctx.pos;
        
        if (ctx.item != null) {
          ctx.pos = 0;
          ctx.size = 1;
          int j = 0;
          for (; j < step.length; j++) {
            final Item par = ctx.iter(step[j]).next();
            if (par == null) break;
            if(!par.node()) Err.or(NODESPATH, this, par.type);
            ctx.item = par;
          }
          if (j == step.length) {
            ctx.pos = 0;
            ctx.size = 1;
            return ctx.item;
          }
        }
        
        ctx.item = c;
        ctx.size = cs;
        ctx.pos = cp;
        return null;
      }
      @Override
      public boolean ordered() {
        // results will always be ordered..
        return true;
      }
    }; 
  }
}