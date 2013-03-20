package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * A tail-recursive function call.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class TailFuncCall extends StaticFuncCall {
  /**
   * Constructor.
   * @param ii input info
   * @param nm name of the function to call
   * @param f function
   * @param a arguments
   */
  TailFuncCall(final InputInfo ii, final QNm nm, final StaticUserFunc f, final Expr[] a) {
    super(ii, nm, a);
    func = f;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    checkHeight(ctx);
    final Value[] args = args(ctx);
    try {
      return func.invItem(ctx, ii, args);
    } catch(final QueryException ex) {
      ex.add(info);
      throw ex;
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    checkHeight(ctx);
    final Value[] args = args(ctx);
    try {
      return func.invValue(ctx, info, args);
    } catch(final QueryException ex) {
      ex.add(info);
      throw ex;
    }
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
