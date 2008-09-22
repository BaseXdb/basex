package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Var;

/**
 * Project specific try/catch expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Try extends Arr {
  /** Variable. */
  private final Var var1;
  /** Variable. */
  private final Var var2;

  /**
   * Constructor.
   * @param tr try expression
   * @param ct catch expression
   * @param v1 first variable
   * @param v2 second variable
   */
  public Try(final Expr tr, final Expr ct, final Var v1, final Var v2) {
    super(tr, ct);
    var1 = v1;
    var2 = v2;
  }

  @Override
  public Expr comp(final XQContext ctx) {
    return this;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    try {
      return ctx.iter(expr[0]);
    } catch(final XQException e) {
      final int s = ctx.vars.size();
      if(var1 != null) ctx.vars.add(var1.item(Str.get(e.msg())));
      if(var2 != null) ctx.vars.add(var2.item(Str.get(e.code())));
      final Iter iter = ctx.iter(expr[1]);
      ctx.vars.reset(s);
      return iter;
    } catch(final Exception e) {
      return Str.ZERO.iter();
    }
  }

  @Override
  public String toString() {
    return "try { " + expr[0] + " } catch { " + expr[1] + "}";
  }
}
