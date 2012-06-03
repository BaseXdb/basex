package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
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
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    Expr fun = func;
    Var[] args = args(ctx);
    do {
      // cache arguments, evaluate function and reset variable scope
      final VarStack cs = addArgs(ctx, args);
      ctx.tailCalls = 0;
      try {
        return fun.item(ctx, ii);
      } catch(final Continuation c) {
        fun = c.getFunc();
        args = c.getArgs();
      } finally {
        ctx.vars.reset(cs);
      }
    } while(true);
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    Expr fun = func;
    Var[] args = args(ctx);
    do {
      // cache arguments, evaluate function and reset variable scope
      final VarStack cs = addArgs(ctx, args);
      ctx.tailCalls = 0;
      try {
        return ctx.value(fun);
      } catch(final Continuation c) {
        fun = c.getFunc();
        args = c.getArgs();
      } finally {
        ctx.vars.reset(cs);
      }
    } while(true);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    // [LW] make result streamable
    return value(ctx).iter();
  }

  @Override
  public Expr markTailCalls() {
    return new TailFuncCall(info, name, func, expr);
  }
}
