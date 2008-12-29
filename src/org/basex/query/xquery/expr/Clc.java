package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Token;

/**
 * Calculation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Clc extends Arr {
  /** Calculation operator. */
  private final Calc calc;

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression
   * @param c calculation operator
   */
  public Clc(final Expr e1, final Expr e2, final Calc c) {
    super(e1, e2);
    calc = c;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);

    final Expr e1 = expr[0];
    final Expr e2 = expr[1];
    
    Expr e = this;
    if(e1.i() && e2.i()) e = calc.ev((Item) e1, (Item) e2);
    if(e1.e() || e2.e()) e = Seq.EMPTY;
    if(e != this) ctx.compInfo(OPTSIMPLE, this, e);
    return e;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Item a = ctx.atomic(expr[0], this, true);
    if(a == null) return Iter.EMPTY;
    final Item b = ctx.atomic(expr[1], this, true);
    if(b == null) return Iter.EMPTY;
    return calc.ev(a, b).iter();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(calc.name));
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return toString(" " + calc.name + " ");
  }

  @Override
  public String info() {
    return "'" + calc.name + "' expression";
  }
}
