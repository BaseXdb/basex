package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ArrayRemove extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    final int p = checkPos(array, toLong(exprs[1], qc)), as = array.arraySize() - 1;
    if(p == 0 || p == as) return Array.get(array, p == 0 ? 1 : 0, as);
    final ValueList vl = new ValueList(as);
    for(int a = 0; a <= as; a++) if(a != p) vl.add(array.get(a));
    return vl.array();
  }
}
