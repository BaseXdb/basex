package org.basex.query.func.bit;

import static org.basex.query.func.Function.*;

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
public final class BitRotate extends BitFn {
  @Override
  Value value(final long value, final QueryContext qc) throws QueryException {
    final long by = toLong(arg(1), qc);
    final int size = toSize(arg(2), Long.SIZE, qc);
    if(size == 0) return Itr.ZERO;

    final int r = Math.floorMod(by, size);
    final long v = unsigned(value, size);
    return Itr.get(unsigned(v << r | v >>> size - r, size));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = optFirst();
    if(expr != this) return expr;

    // bit:rotate($i, 0, $size) → bit:unsigned($i, $size)
    final long size = size(arg(2));
    return literal(arg(1), 0) && size != -1 && size <= Long.SIZE ?
      cc.function(_BIT_UNSIGNED, info, arg(0), arg(2)) : this;
  }
}
