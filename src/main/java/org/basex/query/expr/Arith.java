package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Arithmetic expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Arith extends Arr {
  /** Calculation operator. */
  private final Calc calc;

  /**
   * Constructor.
   * @param ii input info
   * @param e1 first expression
   * @param e2 second expression
   * @param c calculation operator
   */
  public Arith(final InputInfo ii, final Expr e1, final Expr e2, final Calc c) {
    super(ii, e1, e2);
    calc = c;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);

    final SeqType s0 = expr[0].type();
    final SeqType s1 = expr[1].type();
    type = SeqType.ITEM_ZO;
    if(s0.type.isNumber() && s1.type.isNumber()) {
      type = s0.one() && s1.one() ? SeqType.ITR : SeqType.ITR_ZO;
    } else if(s0.one() && s1.one()) {
      type = SeqType.ITEM;
    }

    return optPre(oneIsEmpty() ? null : allAreValues() ?
        item(ctx, info) : this, ctx);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Item a = expr[0].item(ctx, info);
    if(a == null) return null;
    final Item b = expr[1].item(ctx, info);
    if(b == null) return null;
    return calc.ev(info, a, b);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, OP, Token.token(calc.name));
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String description() {
    return '\'' + calc.name + "' expression";
  }

  @Override
  public String toString() {
    return toString(' ' + calc.name + ' ');
  }
}
