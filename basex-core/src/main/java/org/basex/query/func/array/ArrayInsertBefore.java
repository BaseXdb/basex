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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ArrayInsertBefore extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    final int p = checkPos(array, toLong(exprs[1], qc), true);
    final Value ins = qc.value(exprs[2]);
    final int as = array.arraySize();
    final ValueList vl = new ValueList(as + 1);
    for(int a = 0; a < as; a++) {
      if(a == p) vl.add(ins);
      vl.add(array.get(a));
    }
    if(p == as) vl.add(ins);
    return vl.array();
  }
}
