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
public final class ArrayForEach extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    final FItem fun = checkArity(exprs[1], 1, qc);
    final ArrayBuilder builder = new ArrayBuilder();
    final Iterator<Value> iter = array.members();
    while(iter.hasNext()) builder.append(fun.invokeValue(qc, info, iter.next()));
    return builder.freeze();
  }
}
