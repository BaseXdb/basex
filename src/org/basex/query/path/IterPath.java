package org.basex.query.path;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;

/**
 * Iterative path expression for location paths which return sorted and
 * duplicate-free results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Dennis Stratmann
 * @author Christian Gruen
 */
public final class IterPath extends AxisPath {
  /**
   * Constructor.
   * @param r root expression
   * @param s location steps
   */
  public IterPath(final Expr r, final Step[] s) {
    super(r, s);
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      final Item c = ctx.item;
      Expr[] expr;
      Iter[] iter;
      Nod prev;
      int p;

      @Override
      public Nod next() throws QueryException {
        if(iter == null) {
          expr = step;
          if(root != null) {
            // copy expressions to be iterated
            expr = new Expr[step.length + 1];
            expr[0] = root;
            System.arraycopy(step, 0, expr, 1, step.length);
          }
          // create iterator array
          iter = new Iter[expr.length];
          iter[0] = ctx.iter(expr[0]);
          prev = null;
          p = 0;
        }

        while(p != -1) {
          final Item i = iter[p].next();
          if(i == null) {
            p--;
          } else {
            if(p == iter.length - 1) {
              if(!i.node()) Err.or(NODESPATH, this, i.type);
              final Nod n = (Nod) i;
              if(prev == null || !prev.is(n)) {
                prev = n;
                ctx.item = c;
                return n;
              }
            } else {
              p++;
              ctx.item = i;
              iter[p] = ctx.iter(expr[p]);
            }
          }
        }
        ctx.item = c;
        return null;
      }

      @Override
      public boolean reset() {
        ctx.item = c;
        iter = null;
        return true;
      }
    };
  }

  @Override
  public boolean duplicates(final QueryContext ctx) {
    return false;
  }
}