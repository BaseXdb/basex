package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.SeqType;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Instance test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    if(!checkUp(expr, ctx).item()) return this;
    ctx.compInfo(OPTPRE, this);
    return atomic(ctx, input);
  }

  @Override
  public Bln atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return Bln.get(seq.instance(expr.iter(ctx)));
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return SeqType.BLN;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(seq.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return Main.info("% instance of %", expr, seq);
  }
}
