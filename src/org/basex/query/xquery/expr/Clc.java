package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
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
    if(expr[0].e() || expr[1].e()) {
      ctx.compInfo(OPTPREEVAL, this);
      return Seq.EMPTY;
    }
    if(expr[0].i() && expr[1].i()) {
      final Item it1 = iter(expr[0]).atomic(this, true);
      final Item it2 = iter(expr[1]).atomic(this, true);
      ctx.compInfo(OPTPREEVAL, this);
      return calc.ev(it1, it2);
    }
    return this;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Item a = ctx.iter(expr[0]).atomic(this, true);
    if(a == null) return Iter.EMPTY;
    final Item b = ctx.iter(expr[1]).atomic(this, true);
    if(b == null) return Iter.EMPTY;
    return calc.ev(a, b).iter();
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this, TYPE, Token.token(calc.name));
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement(this);
  }

  @Override
  public String color() {
    return "FF9999";
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
