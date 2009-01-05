package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Dec;
import org.basex.query.xquery.item.Flt;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Itr;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;

/**
 * Unary Expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);
    if(expr.e()) {
      ctx.compInfo(OPTSIMPLE, this, expr);
      return expr;
    }
    return expr.i() && ((Item) expr).n() ? eval((Item) expr) : this;
  }
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Item it = atomic(ctx, expr, true);
    if(it == null) return Iter.EMPTY;
    if(!it.u() && !it.n()) Err.num(info(), it);
    final double d = it.dbl();
    return (it.u() ? Dbl.get(minus ? -d : d) : eval(it)).iter();
  }

  /**
   * Returns the result of the unary expression.
   * @param it input item
   * @return resulting item
   * @throws XQException xquery exception
   */
  private Item eval(final Item it) throws XQException {
    if(!minus) return it;
    
    switch(it.type) {
      case DBL: return Dbl.get(-it.dbl());
      case FLT: return Flt.get(-it.flt());
      case DEC: return Dec.get(it.dec().negate());
      default:  return Itr.get(-it.itr());
    }
  }

  @Override
  public Type returned() {
    return Type.DBL;
  }
  
  @Override
  public String toString() {
    return "-" + expr;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NEGATE, Token.token(minus));
    expr.plan(ser);
    ser.closeElement();
  }
}
