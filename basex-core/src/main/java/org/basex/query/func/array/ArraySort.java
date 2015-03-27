package org.basex.query.func.array;

import java.util.*;

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
    final Array value = toArray(exprs[0], qc);

    final int sz = (int) value.arraySize();
    final ValueList vl = new ValueList(sz);
    final Iterator<Value> iter = value.members();
    if(exprs.length > 1) {
      final FItem key = checkArity(exprs[1], 1, qc);
      while(iter.hasNext()) vl.add(key.invokeValue(qc, info, iter.next()));
    } else {
      while(iter.hasNext()) vl.add(iter.next());
    }

    final Integer[] order = FnSort.sort(vl, this);
    final ArrayBuilder builder = new ArrayBuilder();
    if(exprs.length > 1) {
      for(int r = 0; r < sz; r++) builder.append(value.get(order[r]));
    } else {
      for(int r = 0; r < sz; r++) builder.append(vl.get(order[r]));
    }
    return builder.freeze();
  }
}
