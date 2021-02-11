package org.basex.query.func.array;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.array.*;

/**
 * Functions on arrays.
 *
 * @author BaseX Team 2005-21, BSD License
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
  final long checkPos(final XQArray array, final long pos, final boolean incl)
      throws QueryException {

    final long as = array.arraySize() + (incl ? 1 : 0);
    if(pos < 1 || pos > as) throw (as == 0 ? ARRAYEMPTY : ARRAYBOUNDS_X_X).get(info, pos, as);
    return pos - 1;
  }
}
