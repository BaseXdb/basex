package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FunItem;
import org.basex.query.item.FunType;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.Token;

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
  public Item item(final QueryContext ctx, final InputInfo ii) {
    return null;
  }

}
