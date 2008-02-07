package org.basex.query.xpath.expr;

import static org.basex.query.xpath.XPText.*;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.Calc;
import org.basex.query.xpath.values.Num;
import org.basex.query.xpath.values.Item;

/**
 * Calculation Expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Calculation extends DualExpr {
  /** Expression Type. */
  public Calc type;

  /**
   * Constructor.
   * @param e1 first expression (factor or dividend)
   * @param e2 second expression (factor or divisor)
   * @param t MultiplicativeExpr.MULTIPLICATION / DIVISION / MODULO
   */
  public Calculation(final Expr e1, final Expr e2, final Calc t) {
    super(e1, e2);
    type = t;
  }

  @Override
  public Num eval(final XPContext ctx) throws QueryException {
    return new Num(type.eval(ctx.eval(expr1).num(), ctx.eval(expr2).num()));
  }

  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    expr1 = expr1.compile(ctx);
    expr2 = expr2.compile(ctx);
    if(expr1 instanceof Item && expr2 instanceof Item) {
      ctx.compInfo(OPTCALC);
      final double d1 = ((Item) expr1).num();
      final double d2 = ((Item) expr2).num();
      return new Num(type.eval(d1, d2));
    }
    return this;
  }

  @Override
  public String toString() {
    return expr1 + " " + type + " " + expr2;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this, TYPE, type.name);
    expr1.plan(ser);
    expr2.plan(ser);
    ser.closeElement(this);
  }
}
