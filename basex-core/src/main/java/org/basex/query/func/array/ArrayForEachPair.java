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
public final class ArrayForEachPair extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Array array1 = toArray(exprs[0], qc), array2 = toArray(exprs[1], qc);
    final FItem fun = checkArity(exprs[2], 2, qc);
    final int as = Math.min(array1.arraySize(), array2.arraySize());
    final ValueList vl = new ValueList(as);
    for(int a = 0; a < as; a++) vl.add(fun.invokeValue(qc, info, array1.get(a), array2.get(a)));
    return vl.array();
  }
}
