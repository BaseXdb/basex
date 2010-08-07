package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Cast expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Cast extends Single {
  /** Type. */
  private final SeqType seq;

  /**
   * Function constructor.
   * @param ii input info
   * @param e expression
   * @param t data type
   */
  public Cast(final InputInfo ii, final Expr e, final SeqType t) {
    super(ii, e);
    seq = t;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    return checkUp(expr, ctx).item() ? preEval(ctx) : this;
  }

  @Override
  public Item atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return seq.cast(expr.atomic(ctx, input), this, ctx, input);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(seq.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return seq;
  }

  @Override
  public String desc() {
    return seq.type + " " + CAST;
  }

  @Override
  public String toString() {
    return Main.info("% cast as %", expr, seq);
  }
}
