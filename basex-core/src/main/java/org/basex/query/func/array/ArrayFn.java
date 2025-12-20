package org.basex.query.func.array;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.array.*;
import org.basex.query.value.type.*;

/**
 * Functions on arrays.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class ArrayFn extends StandardFunc {
  /**
   * Checks if a position is within the range of an array.
   * @param array array
   * @param pos position
   * @param incl include last entry
   * @return specified position -1
   * @throws QueryException query exception
   */
  final long toPos(final XQArray array, final long pos, final boolean incl) throws QueryException {
    final long size = array.structSize() + (incl ? 1 : 0);
    if(pos > 0 && pos <= size) return pos - 1;
    throw (size == 0 ? ARRAYEMPTY : ARRAYBOUNDS_X_X).get(info, pos, size);
  }

  /**
   * Returns the size of the array passed via the specified argument.
   * @param a index of argument
   * @return array size
   */
  final long structSize(final int a) {
    final Expr expr1 = arg(a);
    return expr1.seqType().instanceOf(Types.ARRAY_O) ? expr1.structSize() : -1;
  }
}
