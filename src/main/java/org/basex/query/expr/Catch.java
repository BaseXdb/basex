package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemCache;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Catch clause.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Catch extends Single {
  /** Variable. */
  private final Var[] var;
  /** Supported codes. */
  private final QNm[] codes;

  /**
   * Constructor.
   * @param ii input info
   * @param ct catch expression
   * @param c supported error codes
   * @param v variables
   */
  public Catch(final InputInfo ii, final Expr ct, final QNm[] c,
      final Var... v) {
    super(ii, ct);
    codes = c;
    var = v;
  }

  @Override
  public Catch comp(final QueryContext ctx) throws QueryException {
    final int s = ctx.vars.size();
    for(final Var v : var) ctx.vars.add(v);
    super.comp(ctx);
    ctx.vars.reset(s);
    return this;
  }

  /**
   * Catch iterator.
   * @param ctx query context
   * @param ex thrown exception
   * @return resulting item
   * @throws QueryException query exception
   */
  Iter iter(final QueryContext ctx, final QueryException ex)
      throws QueryException {

    final byte[] code = Token.token(ex.code());
    if(!find(code)) return null;

    final int s = ctx.vars.size();
    if(var.length > 0) {
      final QNm err = new QNm(code, QueryTokens.ERRORURI);
      ctx.vars.add(var[0].bind(err, ctx).copy());
    }
    if(var.length > 1) {
      ctx.vars.add(var[1].bind(Str.get(ex.getLocalizedMessage()), ctx).copy());
    }
    if(var.length > 2) {
      ctx.vars.add(var[2].bind(ex.value(), ctx).copy());
    }
    final Iter ir = ItemCache.get(ctx.iter(expr));
    ctx.vars.reset(s);
    return ir;
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
  public boolean uses(final Use u) {
    return u == Use.VAR || super.uses(u);
  }

  @Override
  public int count(final Var v) {
    for(final Var vr : var) if(vr.eq(v)) return 0;
    return super.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    for(final Var vr : var) if(vr.eq(v)) return true;
    return super.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    for(final Var vr : var) if(vr.eq(v)) return this;
    return super.remove(v);
  }

  @Override
  public String toString() {
    return "catch { " + expr + "}";
  }
}
