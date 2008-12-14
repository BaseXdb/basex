package org.basex.query.xpath.expr;

import static org.basex.query.xpath.XPText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Calc;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Dbl;
import org.basex.util.Token;

/**
 * Calculation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Clc extends Arr {
  /** Calculation operator. */
  public final Calc calc;

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression
   * @param c calculation type
   */
  public Clc(final Expr e1, final Expr e2, final Calc c) {
    super(e1, e2);
    calc = c;
  }

  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    super.comp(ctx);

    if(expr[0] instanceof Item && expr[1] instanceof Item) {
      ctx.compInfo(OPTFUNC, calc);
      final double d1 = ((Item) expr[0]).num();
      final double d2 = ((Item) expr[1]).num();
      return new Dbl(calc.eval(d1, d2));
    }
    return this;
  }

  @Override
  public Dbl eval(final XPContext ctx) throws QueryException {
    return new Dbl(calc.eval(ctx.eval(expr[0]).num(), ctx.eval(expr[1]).num()));
  }

  @Override
  public String toString() {
    return toString(calc);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(calc.name));
    expr[0].plan(ser);
    expr[1].plan(ser);
    ser.closeElement();
  }
}
