package org.basex.query.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.FTOpt;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.iter.FTNodeIter;

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
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    final FTOpt tmp = ctx.ftopt;
    opt.compile(tmp);
    ctx.ftopt = opt;
    expr[0] = expr[0].comp(ctx);
    ctx.ftopt = tmp;
    return this;
  }

  @Override
  public FTNodeIter iter(final QueryContext ctx) throws QueryException {
    final FTOpt tmp = ctx.ftopt;
    ctx.ftopt = opt;
    final FTNodeIter ir = expr[0].iter(ctx);
    ctx.ftopt = tmp;
    return ir;
  }

  @Override
  public void indexAccessible(final QueryContext ctx, final IndexContext ic) 
      throws QueryException {
    
    ic.io &= opt.indexAccessible(ic.data.meta);
    final FTOpt tmp = ctx.ftopt;
    ctx.ftopt = opt;
    expr[0].indexAccessible(ctx, ic);
    ctx.ftopt = tmp;
  }

  @Override
  public FTExpr indexEquivalent(final QueryContext ctx, final IndexContext ic)
      throws QueryException {

    return new FTOptions(expr[0].indexEquivalent(ctx, ic), opt);
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
