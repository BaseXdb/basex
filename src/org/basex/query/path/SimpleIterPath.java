package org.basex.query.path;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.Array;

/**
 * Iterative path expression for location paths which return sorted and
 * duplicate-free results.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Dennis Stratmann
 * @author Christian Gruen
 */
public final class SimpleIterPath extends AxisPath {
  /**
   * Constructor.
   * @param r root expression
   * @param s location steps
   */
  public SimpleIterPath(final Expr r, final Step[] s) {
    super(r, s);
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      final Item c = ctx.item;
      Expr[] expr;
      Iter[] iter;
      int p;

      @Override
      public Item next() throws QueryException {
        if(iter == null) {
          final int o = root != null ? 1 : 0;
          // copy expressions to be iterated
          expr = new Expr[step.length + o];
          expr[0] = root;
          Array.copy(step, expr, o);
          // create iterator array
          iter = new Iter[expr.length];
          iter[0] = ctx.iter(expr[0]);
        }

        while(p != -1) {
          final Item i = iter[p].next();
          if(i == null) {
            p--;
          } else {
            if(p == iter.length - 1) {
              if(!i.node()) Err.or(NODESPATH, this, i.type);
              return i;
            }
            p++;
            ctx.item = i;
            iter[p] = ctx.iter(expr[p]);
          }
        }
        ctx.item = c;
        return null;
      }
    };
  }

  @Override
  public boolean duplicates(final QueryContext ctx) {
    return false;
  }
}