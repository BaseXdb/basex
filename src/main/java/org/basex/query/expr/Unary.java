package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Dbl;
import org.basex.query.item.Dec;
import org.basex.query.item.Flt;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.SeqType;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Unary expression.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class Unary extends Single {
  /** Minus flag. */
  private final boolean minus;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param min minus flag
   */
  public Unary(final InputInfo ii, final Expr e, final boolean min) {
    super(ii, e);
    minus = min;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    type = expr.type();
    if(!type.num()) type = SeqType.ITR;
    return expr.value() ? preEval(ctx) : this;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Item it = expr.item(ctx, input);
    if(it == null) return null;

    if(!it.unt() && !it.num()) Err.number(this, it);
    final double d = it.dbl(input);
    if(it.unt()) return Dbl.get(minus ? -d : d);

    if(!minus) return it;
    switch(it.type) {
      case DBL: return Dbl.get(-d);
      case FLT: return Flt.get(-it.flt(input));
      case DEC: return Dec.get(it.dec(input).negate());
      default:  return Itr.get(-it.itr(input));
    }
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, VAL, Token.token(minus));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return (minus ? "-" : "") + expr;
  }
}
