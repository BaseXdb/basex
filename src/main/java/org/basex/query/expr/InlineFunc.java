package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FunItem;
import org.basex.query.item.FunType;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Inline function.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public class InlineFunc extends Func {

  /**
   * Constructor.
   * @param ii input info
   * @param r return type
   * @param argv arguments
   * @param body function body
   */
  public InlineFunc(final InputInfo ii, final SeqType r, final Var[] argv,
      final Expr body) {
    super(ii, null, argv, r, true);
    expr = body;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    return this;
  }

  @Override
  public FunItem item(final QueryContext ctx, final InputInfo ii) {
    final FunType ft = FunType.get(this);
    final boolean c = ft.ret != null && !expr.type().instance(ft.ret);
    return new FunItem(args, expr, ft, ctx.vars.local(), c);
  }

  @Override
  public Value value(final QueryContext ctx) {
    return item(ctx, input);
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return value(ctx).iter();
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
    if(ret != null) tb.append("as ").append(ret.toString()).append(' ');
    return tb.append("{ ").append(expr).append(" }").toString();
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 || super.uses(u);
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
