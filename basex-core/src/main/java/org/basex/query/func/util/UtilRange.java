package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
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
  protected boolean range() {
    return true;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    if(arg(1) instanceof final Itr itr) {
      // util:range(EXPR, -5, END)  ->  util:range(EXPR, 1, END)
      if(itr.itr() < 1) arg(1, arg -> Itr.ONE);
    }
    return super.opt(cc);
  }
}
