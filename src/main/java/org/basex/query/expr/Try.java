package org.basex.query.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Project specific try/catch expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Try extends Single {
  /** Query exception. */
  QueryException qe;
  /** Catches. */
  private final Catch[] ctch;

  /**
   * Constructor.
   * @param ii input info
   * @param t try expression
   * @param c catch expressions
   */
  public Try(final InputInfo ii, final Expr t, final Catch[] c) {
    super(ii, t);
    ctch = c;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(int c = 0; c < ctch.length; c++) {
      ctch[c] = ((Catch) checkUp(ctch[c], ctx)).comp(ctx);
    }
    checkUp(expr, ctx);
    try {
      return super.comp(ctx);
    } catch(final QueryException ex) {
      qe = ex;
      return this;
    }
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      final int s = ctx.vars.size();
      Iter it;

      @Override
      public Item next() throws QueryException {
        if(it == null && qe != null) it = err(ctx, qe);
        try {
          if(it == null) it = ctx.iter(expr);
          return it.next();
        } catch(final QueryException ex) {
          ctx.vars.reset(s);
          it = err(ctx, ex);
        }
        return it.next();
      }
    };
  }

  /**
   * Handles an exception iterator.
   * @param ctx query context
   * @param ex query exception
   * @return result iterator
   * @throws QueryException query exception
   */
  Iter err(final QueryContext ctx, final QueryException ex)
    throws QueryException {
    for(final Catch c : ctch) {
      final Iter it = c.iter(ctx, ex);
      if(it != null) return it;
    }
    throw ex;
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
