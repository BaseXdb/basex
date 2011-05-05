package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.QNm;
import org.basex.query.util.TypedFunc;
import org.basex.query.util.Var;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Partial function application.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public final class PartFunApp extends Func {

  /**
   * Function constructor for static calls.
   * @param ii input info
   * @param fun typed function expression
   * @param arg arguments
   */
  public PartFunApp(final InputInfo ii, final TypedFunc fun, final Var[] arg) {
    super(ii, new QNm(), nn(fun.type.type(arg)), fun.ret(), true);
    expr = fun.fun;
  }

  /**
   * Function constructor for dynamic calls.
   * @param ii input info
   * @param func function expression
   * @param arg arguments
   */
  public PartFunApp(final InputInfo ii, final Expr func, final Var[] arg) {
    // [LW] XQuery/HOF: dynamic type propagation
    super(ii, new QNm(), nn(arg), func.type(), true);
    expr = func;
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
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    // defer creation of function item because of closure
    // [LW] can we skip this if closure is empty?
    return new InlineFunc(input, ret, args, expr);
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

}
