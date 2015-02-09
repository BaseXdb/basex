package org.basex.query.func.array;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ArraySubarray extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    final int p = checkPos(array, toLong(exprs[1], qc), true);
    final int l = exprs.length > 2 ? (int) toLong(exprs[2], qc) : array.arraySize() - p;
    if(l < 0) throw ARRAYNEG_X.get(info, l);
    checkPos(array, p + 1 + l, true);
    return Array.get(array, p, l);
  }
}
