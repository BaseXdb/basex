package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.AtomType;
import org.basex.query.item.Dbl;
import org.basex.query.item.Dec;
import org.basex.query.item.Flt;
import org.basex.query.item.Item;
import org.basex.query.item.Int;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Unary expression.
 *
 * @author BaseX Team 2005-12, BSD License
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
    if(!type.type.isNumber()) {
      // expression will always yield a number, empty sequence or error
      type = type.mayBeZero() ? SeqType.ITR_ZO : SeqType.ITR;
    }
    return expr.isValue() ? preEval(ctx) : this;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Item it = expr.item(ctx, info);
    if(it == null) return null;
    final Type ip = it.type;

    if(!ip.isUntyped() && !ip.isNumber()) Err.number(this, it);
    final double d = it.dbl(info);
    if(ip.isUntyped()) return Dbl.get(minus ? -d : d);

    if(!minus) return it;
    switch((AtomType) ip) {
      case DBL: return Dbl.get(-d);
      case FLT: return Flt.get(-it.flt(info));
      case DEC: return Dec.get(it.dec(info).negate());
      default:  return Int.get(-it.itr(info));
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
