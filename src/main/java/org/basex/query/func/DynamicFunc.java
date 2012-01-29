package org.basex.query.func;

import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Arr;
import org.basex.query.expr.Expr;
import org.basex.query.item.FItem;
import org.basex.query.item.FuncType;
import org.basex.query.item.Item;
import org.basex.query.item.Type;
import org.basex.query.item.Value;
import org.basex.query.item.map.Map;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import static org.basex.query.util.Err.*;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Dynamic function call.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class DynamicFunc extends Arr {
  /**
   * Function constructor.
   * @param ii input info
   * @param fun function expression
   * @param arg arguments
   */
  public DynamicFunc(final InputInfo ii, final Expr fun, final Expr[] arg) {
    super(ii, Array.add(arg, fun));
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    final int ar = expr.length - 1;
    final Expr f = expr[ar];
    final Type t = f.type().type;
    if(t instanceof FuncType) {
      final FuncType ft = (FuncType) t;
      if(ft.args != null && ft.args.length != ar)
        throw INVARITY.thrw(input, f, ar);
      if(ft.ret != null) type = ft.ret;
    }

    // maps can only contain fully evaluated Values, so this is safe
    return allAreValues() && f instanceof Map ? optPre(value(ctx), ctx) : this;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return getFun(ctx).invItem(ctx, ii, argv(ctx));
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    return getFun(ctx).invValue(ctx, input, argv(ctx));
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return getFun(ctx).invIter(ctx, input, argv(ctx));
  }

  /**
   * Evaluates all arguments.
   * @param ctx query context
   * @return array of argument values
   * @throws QueryException query exception
   */
  private Value[] argv(final QueryContext ctx) throws QueryException {
    final Value[] argv = new Value[expr.length - 1];
    for(int i = argv.length; --i >= 0;) argv[i] = ctx.value(expr[i]);
    return argv;
  }

  /**
   * Evaluates and checks the function item.
   * @param ctx query context
   * @return function item
   * @throws QueryException query exception
   */
  private FItem getFun(final QueryContext ctx) throws QueryException {
    final int ar = expr.length - 1;
    final Item it = checkItem(expr[ar], ctx);
    if(!it.type.isFunction()) throw Err.type(this, FuncType.arity(ar), it);
    final FItem fit = (FItem) it;
    if(fit.arity() != ar) throw INVARITY.thrw(input, fit, ar);
    return fit;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr[expr.length - 1].plan(ser);
    for(int i = 0; i < expr.length - 1; i++) expr[i].plan(ser);
    ser.closeElement();
  }

  @Override
  public String description() {
    return expr[expr.length - 1].description() + "(...)";
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(expr[expr.length - 1].toString());
    tb.add('(');
    for(int i = 0; i < expr.length - 1; i++) {
      tb.add(expr[i].toString());
      if(i < expr.length - 2) tb.add(", ");
    }
    return tb.add(')').toString();
  }
}
