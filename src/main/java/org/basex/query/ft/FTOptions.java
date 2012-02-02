package org.basex.query.ft;

import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNode;
import org.basex.query.iter.FTIter;
import org.basex.util.InputInfo;
import org.basex.util.Util;
import org.basex.util.ft.FTOpt;

/**
 * FTOptions expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTOptions extends FTExpr {
  /** FTOptions. */
  private final FTOpt opt;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param o ft options
   */
  public FTOptions(final InputInfo ii, final FTExpr e, final FTOpt o) {
    super(ii, e);
    opt = o;
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    final FTOpt tmp = ctx.ftopt;
    ctx.ftopt = opt.copy(tmp);
    if(opt.sw != null && ctx.value != null && ctx.value.data() != null)
      opt.sw.comp(ctx.value.data());
    expr[0] = expr[0].comp(ctx);
    ctx.ftopt = tmp;
    return expr[0];
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

  @Override
  public FTNode item(final QueryContext ctx, final InputInfo ii) {
    // shouldn't be called, as compile returns argument
    throw Util.notexpected();
  }

  @Override
  public FTIter iter(final QueryContext ctx) {
    // shouldn't be called, as compile returns argument
    throw Util.notexpected();
  }
}
