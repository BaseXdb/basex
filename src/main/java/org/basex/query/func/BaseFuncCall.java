package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function call for user-defined functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BaseFuncCall extends StaticFuncCall {
  /**
   * Function constructor.
   * @param nm function name
   * @param a arguments
   * @param sctx static context
   * @param ii input info
   */
  public BaseFuncCall(final QNm nm, final Expr[] a, final StaticContext sctx,
      final InputInfo ii) {
    super(nm, a, sctx, ii);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    StaticFunc fun = func;
    Value[] args = args(ctx);

    final int calls = ctx.tailCalls;
    try {
      do {
        ctx.tailCalls = 0;
        try {
          return fun.invItem(ctx, ii, args);
        } catch(final Continuation c) {
          fun = c.getFunc();
          args = c.getArgs();
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
    StaticFunc fun = func;
    Value[] args = args(ctx);

    final int calls = ctx.tailCalls;
    try {
      do {
        ctx.tailCalls = 0;
        try {
          return fun.invValue(ctx, info, args);
        } catch(final Continuation c) {
          fun = c.getFunc();
          args = c.getArgs();
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
  public Expr markTailCalls() {
    return new TailFuncCall(name, expr, func, sc, info);
  }
}
