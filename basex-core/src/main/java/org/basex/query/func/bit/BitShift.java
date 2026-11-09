package org.basex.query.func.bit;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BitShift extends BitFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Long value = toLongOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;

    final long by = toLong(arg(1), qc);
    return Itr.get(by >= Long.SIZE ? 0 : by >= 0 ? value << by :
      value >> -Math.max(by, 1 - Long.SIZE));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = optFirst();
    if(expr != this) return expr;

    // bit:shift($i, 0) → $i
    final Expr value = arg(0);
    return arg(1) instanceof final Itr by && by.itr() == 0 && integer(value) ? value : this;
  }
}
