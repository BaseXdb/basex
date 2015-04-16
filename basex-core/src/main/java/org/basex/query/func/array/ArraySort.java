package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ArraySort extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Array array = toArray(exprs[0], qc);

    final int sz = (int) array.arraySize();
    final ValueList vl = new ValueList(sz);
    if(exprs.length > 1) {
      final FItem key = checkArity(exprs[1], 1, qc);
      for(final Value val : array.members()) vl.add(key.invokeValue(qc, info, val));
    } else {
      for(final Value val : array.members()) vl.add(val);
    }

    final Integer[] order = FnSort.sort(vl, this);
    final ArrayBuilder builder = new ArrayBuilder();
    if(exprs.length > 1) {
      for(int r = 0; r < sz; r++) builder.append(array.get(order[r]));
    } else {
      for(int r = 0; r < sz; r++) builder.append(vl.get(order[r]));
    }
    return builder.freeze();
  }
}
