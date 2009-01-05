package org.basex.query.xquery.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.FTOpt;
import org.basex.query.xquery.IndexContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.iter.Iter;

/**
 * FTOptions expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTOptions extends FTExpr {
  /** FTOptions. */
  public FTOpt opt;

  /**
   * Constructor.
   * @param e expression
   * @param o ft options
   */
  public FTOptions(final FTExpr e, final FTOpt o) {
    super(e);
    opt = o;
  }

  @Override
  public FTExpr comp(final XQContext ctx) throws XQException {
    final FTOpt tmp = ctx.ftopt;
    opt.compile(tmp);
    ctx.ftopt = opt;
    expr[0] = expr[0].comp(ctx);
    ctx.ftopt = tmp;
    return this;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final FTOpt tmp = ctx.ftopt;
    ctx.ftopt = opt;
    final Iter it = ctx.iter(expr[0]);
    ctx.ftopt = tmp;
    return it;
  }

  @Override
  public void indexAccessible(final XQContext ctx, final IndexContext ic) 
      throws XQException {
    
    ic.io &= opt.indexAccessible(ic.data.meta);
    
    final FTOpt tmp = ctx.ftopt;
    ctx.ftopt = opt;
    expr[0].indexAccessible(ctx, ic);
    ctx.ftopt = tmp;
  }

  @Override
  public Expr indexEquivalent(final XQContext ctx, final IndexContext ic)
      throws XQException {

    return new FTOptions((FTExpr) expr[0].indexEquivalent(ctx, ic), opt);
  }
  
  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    opt.plan(ser);
    expr[0].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return expr[0].toString() + opt;
  }
}
