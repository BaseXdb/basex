package org.basex.query.func.bit;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BitShift extends BitFn {
  @Override
  Value value(final long value, final QueryContext qc) throws QueryException {
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
    return literal(arg(1), 0) && integer(value) ? value : this;
  }
}
