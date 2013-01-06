package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * A tail-recursive function call.
 *
 * @author BaseX Team 2005-12, BSD License
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
  TailFuncCall(final InputInfo ii, final QNm nm, final UserFunc f, final Expr[] arg) {
    super(ii, nm, arg);
    func = f;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    checkHeight(ctx);

    // cache arguments, evaluate function and reset variable scope
    final Value[] cs = addArgs(ctx, ii, func.scope, func.args, args(ctx));
    try {
      return func.item(ctx, ii);
    } catch(final QueryException ex) {
      ex.add(info);
      throw ex;
    } finally {
      func.scope.exit(ctx, cs);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    checkHeight(ctx);

    // cache arguments, evaluate function and reset variable scope
    final Value[] cs = addArgs(ctx, info, func.scope, func.args, args(ctx));
    try {
      return ctx.value(func);
    } catch(final QueryException ex) {
      ex.add(info);
      throw ex;
    } finally {
      func.scope.exit(ctx, cs);
    }
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  /**
   * Checks is the maximum number of successive tail calls is reached, and
   * triggers a continuation exception if this happens.
   * @param ctx query context
   * @throws QueryException query exception
   */
  private void checkHeight(final QueryContext ctx) throws QueryException {
    final int max = ctx.maxCalls;
    if(max >= 0 && ctx.tailCalls++ > max) throw new Continuation(args(ctx));
  }
}
