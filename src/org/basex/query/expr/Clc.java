package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
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
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);

    final Expr e1 = expr[0];
    final Expr e2 = expr[1];
    
    Expr e = this;
    if(e1.i() && e2.i()) e = atomic(ctx);
    if(e1.e() || e2.e()) e = Seq.EMPTY;
    if(e != this) ctx.compInfo(OPTPRE, this);
    return e;
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    final Item a = expr[0].atomic(ctx);
    if(a == null) return null;
    final Item b = expr[1].atomic(ctx);
    if(b == null) return null;
    return calc.ev(a, b);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(calc.name));
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public Return returned(final QueryContext ctx) {
    return Return.NUM;
  }

  @Override
  public String info() {
    return "'" + calc.name + "' expression";
  }

  @Override
  public String toString() {
    return toString(" " + calc.name + " ");
  }
}
