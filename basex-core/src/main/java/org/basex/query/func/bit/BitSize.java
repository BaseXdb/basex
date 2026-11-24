package org.basex.query.func.bit;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Bit function that converts the lowest bits of an integer.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class BitSize extends BitFn {
  @Override
  final Value value(final long value, final QueryContext qc) throws QueryException {
    return Itr.get(convert(value, toSize(arg(1), Long.MAX_VALUE, qc)));
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = optFirst();
    if(expr != this) return expr;

    final Expr value = arg(0);
    final long size = size(arg(1));
    // bit:unsigned($i, 64) → $i
    if(size >= Long.SIZE && integer(value)) return value;
    final boolean unsigned = _BIT_UNSIGNED.is(value);
    if(size == -1 || !unsigned && !_BIT_SIGNED.is(value)) return this;
    final long inner = size(value.arg(1));
    if(inner == -1) return this;

    final boolean same = value.getClass() == getClass();
    // bit:unsigned(bit:signed($i, 16), 8) → bit:unsigned($i, 8)
    if(inner >= size) {
      return same && inner == size ? value :
        definition.get(info, value.arg(0), arg(1)).optimize(cc);
    }
    // bit:unsigned(bit:unsigned($i, 8), 16) → bit:unsigned($i, 8)
    // bit:signed(bit:unsigned($i, 8), 16) → bit:unsigned($i, 8)
    return same || unsigned ? value : this;
  }

  /**
   * Converts the lowest bits of a value.
   * @param value value
   * @param size number of bits (0-64)
   * @return converted value
   */
  abstract long convert(long value, int size);
}
