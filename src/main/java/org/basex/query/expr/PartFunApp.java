package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FunItem;
import org.basex.query.item.FunType;
import org.basex.query.item.QNm;
import org.basex.query.util.TypedFunc;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Function call.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class PartFunApp extends Func {

  /**
   * Function constructor for static calls.
   * @param ii input info
   * @param func typed function expression
   * @param arg arguments
   */
  public PartFunApp(final InputInfo ii, final TypedFunc func, final Var[] arg) {
    super(ii, new Var(ii, new QNm(), func.ret()), func.type.type(arg), true);
    expr = func.fun;
  }

  /**
   * Function constructor for dynamic calls.
   * @param ii input info
   * @param func function expression
   * @param arg arguments
   */
  public PartFunApp(final InputInfo ii, final Expr func, final Var[] arg) {
    // [LW] dynamic type propagation
    super(ii, new Var(ii, new QNm(), func.type()), arg, true);
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

    return new FunItem(args, expr, FunType.get(this), null);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(FUNCTION).append('(');
    for(final Var v : args)
      sb.append(v).append(v == args[args.length - 1] ? "" : ", ");
    return sb.append(") { ").append(expr).append(" }").toString();
  }

}
