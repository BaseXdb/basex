package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
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
import org.basex.util.Token;

/**
 * Unary expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Unary extends Single {
  /** Minus flag. */
  private final boolean minus;

  /**
   * Constructor.
   * @param e expression
   * @param min minus flag
   */
  public Unary(final Expr e, final boolean min) {
    super(e);
    minus = min;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    if(expr.e()) {
      ctx.compInfo(OPTPRE, this);
      return expr;
    }
    return expr.i() ? atomic(ctx) : this;
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    final Item it = expr.atomic(ctx);
    if(it == null) return null;
    if(!it.u() && !it.n()) Err.num(info(), it);
    final double d = it.dbl();
    if(it.u()) return Dbl.get(minus ? -d : d);

    if(!minus) return it;
    switch(it.type) {
      case DBL: return Dbl.get(-d);
      case FLT: return Flt.get(-it.flt());
      case DEC: return Dec.get(it.dec().negate());
      default:  return Itr.get(-it.itr());
    }
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return SeqType.ITR;
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
