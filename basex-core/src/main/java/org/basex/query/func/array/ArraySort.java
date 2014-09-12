package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ArraySort extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Array value = toArray(exprs[0], qc);

    final int sz = value.arraySize();
    final ValueList vl = new ValueList(sz);
    if(exprs.length > 1) {
      final FItem key = checkArity(exprs[1], 1, qc);
      for(final Value v : value.members()) vl.add(key.invokeValue(qc, info, v));
    } else {
      for(final Value v : value.members()) vl.add(v);
    }

    final Integer[] order = FnSort.sort(vl, this);
    final ValueList tmp = new ValueList(sz);
    for(int r = 0; r < sz; r++) tmp.add(value.get(order[r]));
    return tmp.array();
  }
}
