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
public final class ArrayAppend extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    final int as = array.arraySize();
    final ValueList vl = new ValueList(as + 1);
    for(final Value v : array.members()) vl.add(v);
    return vl.add(qc.value(exprs[1])).array();
  }
}
