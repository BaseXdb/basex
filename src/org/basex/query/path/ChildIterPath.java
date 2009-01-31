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
      final Item c = ctx.item;
      final long cs = ctx.size;
      final long cp = ctx.pos;
      private Iter[] iter;
      private Iter ir;
      private Item item;
      private int p;

      @Override
      public Item next() throws QueryException {

        // first call of method
        if(iter == null) {
          item = root != null ? ctx.iter(root).finish() : ctx.item;
          ctx.item = item;
          iter = new Iter[step.length];
          iter[0] = ctx.iter(step[0]);
          //for(int f = 0; f < step.length; f++) iter[f] = ctx.iter(step[f]);
          ctx.size = item.size(ctx);
          ctx.pos = 1;
        }

        while(true) {
          if(ir != null) {
            final Item i = ir.next();
            if(i != null) {
              if(!i.node()) Err.or(NODESPATH, this, i.type);
              ctx.item = i;
              ctx.pos++;
              return i;
            }
            ir = null;
          } else {
            while((item = iter[p].next()) != null) {
              if(p + 1 < step.length) {
                p++;
                ctx.pos = 1;
                ctx.item = item;
                iter[p] = ctx.iter(step[p]);
              } else {
                ir = iter[p];
                break;
              }
            }
            if(ir == null && p-- == 0) {
              ctx.item = c;
              ctx.size = cs;
              ctx.pos = cp;
              return null;
            }
          }
        }
      }
    };
  }
}