package org.basex.query.expr;

import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Project specific try/catch expression.
 *
 * @author BaseX Team 2005-11, BSD License
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
    for(int c = 0; c < ctch.length; ++c) {
      ctch[c] = ((Catch) checkUp(ctch[c], ctx)).comp(ctx);
    }
    checkUp(expr, ctx);

    // compile expression
    try {
      super.comp(ctx);
      // return value, which will never throw an error
      if(expr.value()) return expr;
    } catch(final QueryException ex) {
      // catch exception for evaluation if expression fails at compile time
      qe = ex;
    }

    // evaluate result type
    type = expr.type();
    for(final Catch c : ctch) type = type.intersect(c.type());
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    final int s = ctx.vars.size();
    try {
      // don't catch errors from error handlers
      if(qe != null) return err(ctx, qe);
      try {
        return expr.value(ctx);
      } catch(final QueryException ex) {
        return err(ctx, ex);
      }
    } finally {
      // always reset the scope
      ctx.vars.reset(s);
    }
  }

  /**
   * Handles an exception.
   * @param ctx query context
   * @param ex query exception
   * @return result
   * @throws QueryException query exception
   */
  private Value err(final QueryContext ctx, final QueryException ex)
      throws QueryException {

    for(final Catch c : ctch) {
      final Value val = c.value(ctx, ex);
      if(val != null) return val;
    }
    throw ex;
  }

  @Override
  public int count(final Var v) {
    int c = super.count(v);
    for(final Catch ct : ctch) c += ct.count(v);
    return c;
  }

  @Override
  public boolean uses(final Use u) {
    for(final Catch c : ctch) if(c.uses(u)) return true;
    return super.uses(u);
  }

  @Override
  public boolean removable(final Var v) {
    for(final Catch c : ctch) if(!c.removable(v)) return false;
    return super.removable(v);
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
    final StringBuilder sb = new StringBuilder("try { " + expr + " }");
    for(final Catch c : ctch) sb.append(" " + c);
    return sb.toString();
  }
}
