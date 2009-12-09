package org.basex.query.ft;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;

/**
 * FTOptions expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTOptions extends FTExpr {
  /** FTOptions. */
  final FTOpt opt;

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
    opt.init(tmp);
    opt.comp(ctx);
    ctx.ftopt = opt;
    expr[0] = expr[0].comp(ctx);
    ctx.ftopt = tmp;
    return this;
  }

  // called by sequential variant
  @Override
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    final FTOpt tmp = ctx.ftopt;
    ctx.ftopt = opt;
    final FTItem it = expr[0].atomic(ctx);
    ctx.ftopt = tmp;
    return it;
  }

  // called by index variant
  @Override
  public FTIter iter(final QueryContext ctx) {
    return new FTIter() {
      final FTOpt tmp = ctx.ftopt;
      FTIter ir;

      @Override
      public FTItem next() throws QueryException {
        ctx.ftopt = opt;
        if(ir == null) ir = expr[0].iter(ctx);
        final FTItem it = ir.next();
        ctx.ftopt = tmp;
        return it;
      }
    };
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    final FTOpt tmp = ic.ctx.ftopt;
    ic.ctx.ftopt = opt;
    final boolean ia = expr[0].indexAccessible(ic);
    ic.ctx.ftopt = tmp;
    return ia;
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
