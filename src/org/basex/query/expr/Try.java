package org.basex.query.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;

/**
 * Project specific try/catch expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Try extends Single {
  /** Catches. */
  final Catch[] ctch;

  /**
   * Constructor.
   * @param t try expression
   * @param c catch expressions
   */
  public Try(final Expr t, final Catch[] c) {
    super(t);
    ctch = c;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(int c = 0; c < ctch.length; c++) {
      ctch[c] = ((Catch) checkUp(ctch[c], ctx)).comp(ctx);
    }
    checkUp(expr, ctx);
    return super.comp(ctx);
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      final int s = ctx.vars.size();
      Iter it;

      @Override
      public Item next() throws QueryException {
        try {
          if(it == null) it = ctx.iter(expr);
          return it.next();
        } catch(final QueryException ex) {
          for(final Catch c : ctch) {
            it = c.iter(ctx, ex);
            if(it != null) return it.next();
          }
          ctx.vars.reset(s);
          throw ex;
        }
      }
    };
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    for(final Catch c : ctch) if(c.uses(u, ctx)) return true;
    return super.uses(u, ctx);
  }

  @Override
  public Expr remove(final Var v) {
    for(final Catch c : ctch) c.remove(v);
    return super.remove(v);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr.plan(ser);
    for(final Catch c : ctch) c.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("try { " + expr + "}");
    for(final Catch c : ctch) sb.append(" " + c);
    return sb.toString();
  }
}
