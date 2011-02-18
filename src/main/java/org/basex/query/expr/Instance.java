package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.SeqType;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Instance test.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Instance extends Single {
  /** Instance. */
  private final SeqType seq;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param s sequence type
   */
  public Instance(final InputInfo ii, final Expr e, final SeqType s) {
    super(ii, e);
    seq = s;
    type = SeqType.BLN;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    return checkUp(expr, ctx).value() ? preEval(ctx) : this;
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return Bln.get(seq.instance(ctx.iter(expr)));
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(seq.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return Util.info("% instance of %", expr, seq);
  }
}
