package org.basex.query.func.util;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class UtilRange extends FnSubsequence {
  @Override
  public long start(final double first) {
    return (long) Math.ceil(first);
  }

  @Override
  public long end(final long first, final double second) {
    return (long) Math.floor(second);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    if(exprs[1] instanceof Int && ((Int) exprs[1]).itr() < 1) {
      final Expr[] args = exprs.clone();
      args[1] = Int.ONE;
      return cc.function(_UTIL_RANGE, info, args);
    }
    return super.opt(cc);
  }
}
