package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.item.FunItem;
import org.basex.query.item.FunType;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Inline function.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Leonard Woerteler
 */
public class InlineFunc extends Func {

  /**
   * Constructor.
   * @param ii input info
   * @param ret return type
   * @param argv arguments
   * @param body function body
   */
  public InlineFunc(final InputInfo ii, final SeqType ret, final Var[] argv,
      final Expr body) {
    super(ii, new Var(ii, null, ret), argv, true);
    expr = body;
  }

  @Override
  public FunItem item(final QueryContext ctx, final InputInfo ii) {
    return new FunItem(args, expr, FunType.get(this), ctx.vars.local());
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return item(ctx, input).iter();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(int i = 0; i < args.length; ++i) {
      ser.attribute(Token.token(ARG + i), args[i].name.atom());
    }
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder tb = new StringBuilder(FUNCTION).append(PAR1);
    for(int i = 0; i < args.length; i++) {
      if(i > 0) tb.append(", ");
      tb.append(args[i].toString());
    }
    tb.append(PAR2).append(' ');
    if(type != null) tb.append(type.toString()).append(' ');
    return tb.append("{ ").append(expr).append(" }").toString();
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 || super.uses(u);
  }

  /**
   * Checks if the given variable is shadowed by an argument.
   * @param v variable
   * @return result of check
   */
  private boolean shadowed(final Var v) {
    for(final Var a : args) if(a.eq(v)) return true;
    return false;
  }

  @Override
  public int count(final Var v) {
    return shadowed(v) ? 0 : expr.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    return false;
  }

  @Override
  public Expr remove(final Var v) {
    throw Util.notexpected(v);
  }
}
