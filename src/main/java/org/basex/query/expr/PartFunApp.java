package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FunItem;
import org.basex.query.item.FunType;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
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
   * Function constructor for dynamic calls.
   * @param ii input info
   * @param call function expression
   * @param arg arguments
   */
  public PartFunApp(final InputInfo ii, final Expr call, final Var[] arg) {
    super(ii, new Var(ii, new QNm(), call.type()), arg, true);
    expr = call;
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

    final SeqType[] at = new SeqType[args.length];
    for(int i = 0; i < at.length; i++)
      at[i] = args[i].type == null ? SeqType.ITEM_ZM : args[i].type;

    return new FunItem(args, expr, FunType.get(at, var.type()));
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(FUNCTION).append('(');
    for(final Var v : args)
      sb.append(v).append(v == args[args.length - 1] ? "" : ", ");
    return sb.append(") { ").append(expr).append(" }").toString();
  }

}
