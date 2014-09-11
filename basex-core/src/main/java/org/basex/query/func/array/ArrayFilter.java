package org.basex.query.func.array;

import org.basex.query.*;
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
public final class ArrayFilter extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    final FItem fun = checkArity(exprs[1], 1, qc);
    final ValueList vl = new ValueList();
    for(final Value v : array.members()) {
      if(toBoolean(fun.invokeItem(qc, info, v))) vl.add(v);
    }
    return vl.array();
  }
}
