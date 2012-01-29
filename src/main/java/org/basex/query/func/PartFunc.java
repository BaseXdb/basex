package org.basex.query.func;

import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.QNm;
import org.basex.query.util.TypedFunc;
import org.basex.query.util.Var;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Partial function application.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class PartFunc extends UserFunc {
  /**
   * Function constructor for static calls.
   * @param ii input info
   * @param fun typed function expression
   * @param arg arguments
   */
  public PartFunc(final InputInfo ii, final TypedFunc fun, final Var[] arg) {
    super(ii, new QNm(), nn(fun.type.type(arg)), fun.ret(), true);
    expr = fun.fun;
  }

  /**
   * Function constructor for dynamic calls.
   * @param ii input info
   * @param func function expression
   * @param arg arguments
   */
  public PartFunc(final InputInfo ii, final Expr func, final Var[] arg) {
    // [LW] XQuery/HOF: dynamic type propagation
    super(ii, new QNm(), nn(arg), func.type(), true);
    expr = func;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(int i = 0; i < args.length; ++i) {
      ser.attribute(Token.token(ARG + i), args[i].name.string());
    }
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    comp(ctx, false);
    // defer creation of function item because of closure
    return new InlineFunc(input, ret, args, expr).comp(ctx);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(FUNCTION).append('(');
    for(final Var v : args)
      sb.append(v).append(v == args[args.length - 1] ? "" : ", ");
    return sb.append(") { ").append(expr).append(" }").toString();
  }

  /**
   * Collects all non-{@code null} variables from the array.
   * @param vars array of variables, can contain {@code null}s
   * @return all non-{@code null} variables
   */
  private static Var[] nn(final Var[] vars) {
    Var[] out = {};
    for(final Var v : vars) if(v != null) out = Array.add(out, v);
    return out;
  }

  @Override
  boolean tco() {
    return false;
  }
}
