package org.basex.query.xquery.expr;

import org.basex.BaseX;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Var;
import org.basex.util.Token;

/**
 * Catch clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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

  @Override
  public Iter iter(final XQContext ctx) {
    BaseX.notexpected();
    return null;
  }

  /**
   * Catch iterator.
   * @param ctx query context
   * @param e thrown exception
   * @return resulting item
   * @throws XQException evaluation exception
   */
  public Iter iter(final XQContext ctx, final XQException e)
      throws XQException {

    final byte[] code = Token.token(e.code());
    if(!find(code)) return null;
    
    final int s = ctx.vars.size();
    if(var1 != null) ctx.vars.add(var1.bind(new QNm(code), ctx));
    if(var2 != null) ctx.vars.add(var2.bind(Str.get(e.simple()), ctx));
    if(var3 != null) ctx.vars.add(var3.bind(e.item, ctx));
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
  public String toString() {
    return "catch { " + expr + "}";
  }
}
