package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function call for user-defined functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BaseFuncCall extends UserFuncCall {
  /**
   * Function constructor.
   * @param ii input info
   * @param nm function name
   * @param arg arguments
   */
  public BaseFuncCall(final InputInfo ii, final QNm nm, final Expr[] arg) {
    super(ii, nm, arg);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    UserFunc fun = func;
    Value[] args = args(ctx);

    final int calls = ctx.tailCalls;
    try {
      do {
        // cache arguments, evaluate function and reset variable scope
        final int fp = addArgs(ctx, ii, fun.scope, fun.args, args);
        ctx.tailCalls = 0;
        try {
          return fun.item(ctx, ii);
        } catch(final Continuation c) {
          fun = c.getFunc();
          args = c.getArgs();
        } finally {
          fun.scope.exit(ctx, fp);
        }
      } while(true);
    } catch(final QueryException ex) {
      ex.add(info);
      throw ex;
    } finally {
      ctx.tailCalls = calls;
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    UserFunc fun = func;
    Value[] args = args(ctx);

    final int calls = ctx.tailCalls;
    try {
      do {
        // cache arguments, evaluate function and reset variable scope
        final int fp = addArgs(ctx, info, fun.scope, fun.args, args);
        ctx.tailCalls = 0;
        try {
          return ctx.value(fun);
        } catch(final Continuation c) {
          fun = c.getFunc();
          args = c.getArgs();
        } finally {
          fun.scope.exit(ctx, fp);
        }
      } while(true);
    } catch(final QueryException ex) {
      ex.add(info);
      throw ex;
    } finally {
      ctx.tailCalls = calls;
    }
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public Expr markTailCalls() {
    return new TailFuncCall(info, name, func, expr);
  }
}
