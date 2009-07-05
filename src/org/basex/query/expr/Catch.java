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
  private final Var[] var;
  /** Supported codes. */
  private final QNm[] codes;

  /**
   * Constructor.
   * @param ct catch expression
   * @param c supported error codes
   * @param v variables
   */
  public Catch(final Expr ct, final QNm[] c, final Var... v) {
    super(ct);
    codes = c;
    var = v;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    final int s = ctx.vars.size();
    for(final Var v : var) ctx.vars.add(v);
    super.comp(ctx);
    ctx.vars.reset(s);
    return this;
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
    if(var.length > 0) {
      ctx.vars.add(var[0].bind(new QNm(code), ctx).clone());
    }
    if(var.length > 1) {
      ctx.vars.add(var[1].bind(Str.get(e.simple()), ctx).clone());
    }
    if(var.length > 2) {
      ctx.vars.add(var[2].bind(e.iter.finish(), ctx).clone());
    }
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
  public boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.VAR || super.uses(u, ctx);
  }

  @Override
  public Expr remove(final Var v) {
    for(final Var vr : var) if(!v.visible(vr)) return this;
    return super.remove(v);
  }

  @Override
  public String toString() {
    return "catch { " + expr + "}";
  }
}
