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
    final long n = array.arraySize();
    final long from = toLong(exprs[1], qc) - 1;
    if(from < 0 || from > n) throw ARRAYBOUNDS_X_X.get(info, from + 1, n + 1);
    if(exprs.length == 2) return array.subArray(from, n - from);

    final long len = toLong(exprs[2], qc);
    if(len < 0) throw ARRAYNEG_X.get(info, len);
    if(from + len > n) throw ARRAYBOUNDS_X_X.get(info, from + 1 + len, n + 1);
    return array.subArray(from, len);
  }
}
