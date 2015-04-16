package org.basex.query.func.array;

import java.util.*;

import org.basex.query.*;
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
public final class ArrayForEachPair extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Array array1 = toArray(exprs[0], qc), array2 = toArray(exprs[1], qc);
    final FItem fun = checkArity(exprs[2], 2, qc);
    final ArrayBuilder builder = new ArrayBuilder();
    final Iterator<Value> as = array1.iterator(0), bs = array2.iterator(0);
    while(as.hasNext() && bs.hasNext())
      builder.append(fun.invokeValue(qc, info, as.next(), bs.next()));
    return builder.freeze();
  }
}
