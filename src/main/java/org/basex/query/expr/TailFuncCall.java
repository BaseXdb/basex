package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * A tail-recursive function call.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public final class TailFuncCall extends UserFuncCall {
  /**
   * Constructor.
   * @param ii input info
   * @param nm name of the function to call
   * @param f function
   * @param arg arguments
   */
  public TailFuncCall(final InputInfo ii, final QNm nm, final UserFunc f,
      final Expr[] arg) {
    super(ii, nm, arg);
    func = f;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    checkHeight(ctx);

    // cache arguments, evaluate function and reset variable scope
    final int s = addArgs(ctx, args(ctx));
    final Item it = func.item(ctx, ii);
    ctx.vars.size(s);
    return it;
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    checkHeight(ctx);

    // cache arguments, evaluate function and reset variable scope
    final int s = addArgs(ctx, args(ctx));
    final Value v = func.value(ctx);
    ctx.vars.size(s);
    return v;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkHeight(ctx);

    // [LW] make result streamable
    return value(ctx).iter();
  }

  /**
   * Checks is the maximum number of successive tail calls is reached, and
   * triggers a continuation exception if this happens.
   * @param ctx query context
   * @throws QueryException query exception
   */
  private void checkHeight(final QueryContext ctx) throws QueryException {
    if(ctx.maxCalls >= 0 && ctx.tailCalls++ > ctx.maxCalls)
      throw new Continuation(args(ctx));
  }

}
