package org.basex.query.path;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;

/**
 * Iterative path expression for only child steps.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Dennis Stratmann
 */
public final class ChildIterPath extends AxisPath {
  /**
   * Constructor.
   * @param r root expression
   * @param s location steps
   */
  public ChildIterPath(final Expr r, final Step[] s) {
    super(r, s);
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      Iter[] iter = null;
      boolean init = true;
      int p = 0;
      Iter ri = null;

      final Item c = ctx.item;
      final long cp = ctx.pos;

      @Override
      public Item next() throws QueryException {
        if (init) {
          init = false;
          if(root != null) {
            if(ri == null) ri = ctx.iter(root);
            ctx.item = ri.next();
          }
          iter = new Iter[step.length];
          ctx.pos = 1;
        }
        
        while(p > -1) {
          if(iter[p] == null) iter[p] = ctx.iter(step[p]);

          final Item i = iter[p].next();
          if(i == null) {
            p--;
          } else {
            if (p == step.length - 1) {
              if(!i.node()) Err.or(NODESPATH, this, i.type);
              return i;
            }
            p++;
            ctx.item = i;
            iter[p] = ctx.iter(step[p]);
          }
        }
        ctx.item = c;
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