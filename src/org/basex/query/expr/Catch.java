package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.Token;

/**
 * Catch clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Catch extends Single {
  /** Variable. */
  private final Var var1;
  /** Variable. */
  private final Var var2;
  /** Variable. */
  private final Var var3;
  /** Supported codes. */
  private final QNm[] codes;

  /**
   * Constructor.
   * @param ct catch expression
   * @param c supported error codes
   * @param v1 first variable
   * @param v2 second variable
   * @param v3 third variable
   */
  public Catch(final Expr ct, final QNm[] c, final Var v1, final Var v2,
      final Var v3) {
    super(ct);
    codes = c;
    var1 = v1;
    var2 = v2;
    var3 = v3;
  }

  /**
   * Catch iterator.
   * @param ctx query context
   * @param e thrown exception
   * @return resulting item
   * @throws QueryException evaluation exception
   */
  public Iter iter(final QueryContext ctx, final QueryException e)
      throws QueryException {

    final byte[] code = e.code() == null ? Token.EMPTY : Token.token(e.code());
    if(!find(code)) return null;
    
    final int s = ctx.vars.size();
    if(var1 != null) ctx.vars.add(var1.bind(new QNm(code), ctx).clone());
    if(var2 != null) ctx.vars.add(var2.bind(Str.get(e.simple()), ctx).clone());
    if(var3 != null) ctx.vars.add(var3.bind(e.iter.finish(), ctx).clone());
    final Iter iter = ctx.iter(expr);
    ctx.vars.reset(s);
    return iter;
  }

  /**
   * Finds iterator.
   * @param err error code
   * @return result of check
   */
  private boolean find(final byte[] err) {
    for(final QNm c : codes) if(c == null || Token.eq(c.ln(), err)) return true;
    return false;
  }

  @Override
  public boolean uses(final Use use, final QueryContext ctx) {
    return use == Use.VAR || super.uses(use, ctx);
  }

  @Override
  public Expr remove(final Var v) {
    return v.visible(var1) && v.visible(var2) && v.visible(var3) ?
      super.remove(v) : this;
  }

  @Override
  public String toString() {
    return "catch { " + expr + "}";
  }
}
