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
   * Returns the array size of the specified expression.
   * @param expr expression
   * @return array size
   */
  final long arraySize(final Expr expr) {
    return arraySize(expr, 0);
  }

  /**
   * Returns the array size of the specified expression, plus offset.
   * @param expr expression
   * @param offset offset to add (can be negative)
   * @return resulting array size
   */
  final long arraySize(final Expr expr, final int offset) {
    long size = -1;
    if(expr.seqType().instanceOf(Types.ARRAY_O)) size = expr.structSize();
    return size == -1 ? -1 : size + offset;
  }
}
