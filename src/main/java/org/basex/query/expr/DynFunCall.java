package org.basex.query.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FunItem;
import org.basex.query.item.FunType;
import org.basex.query.item.Item;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Function call.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class DynFunCall extends Arr {

  /**
   * Function constructor.
   * @param ii input info
   * @param fun function expression
   * @param arg arguments
   */
  public DynFunCall(final InputInfo ii, final Expr fun, final Expr[] arg) {
    super(ii, Array.add(arg, fun));
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    final int n = expr.length - 1;

    final Value[] argv = new Value[n];
    // evaluate arguments
    for(int a = 0; a < n; ++a) argv[a] = expr[a].value(ctx);

    return getFun(ctx).invItem(ctx, ii, argv);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
      final int n = expr.length - 1;

      final Value[] argv = new Value[n];
      // evaluate arguments
      for(int a = 0; a < n; ++a) argv[a] = expr[a].value(ctx);

    return getFun(ctx).invIter(ctx, input, argv);
  }

  /**
   * Evaluates and checks the function item.
   * @param ctx context
   * @return function item
   * @throws QueryException query exception
   */
  private FunItem getFun(final QueryContext ctx) throws QueryException {
    final Item it = expr[expr.length - 1].item(ctx, input);
    if(!it.func() || ((FunType) it.type).args.length != expr.length - 1)
      Err.type(this, FunType.arity(expr.length - 1), it);
    return (FunItem) it;
  }

  /**
   * The function expression.
   * @return function
   */
  public Expr fun() {
    return expr[expr.length - 1];
  }

  /**
   * Argument expressions.
   * @return arguments
   */
  public Expr[] args() {
    final Expr[] args = new Expr[expr.length - 1];
    System.arraycopy(expr, 0, args, 0, args.length);
    return args;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr[expr.length - 1].plan(ser);
    for(int i = 0; i < expr.length - 1; i++) expr[i].plan(ser);
    ser.closeElement();
  }

  @Override
  public String desc() {
    return expr[expr.length - 1].desc() + "(...)";
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
