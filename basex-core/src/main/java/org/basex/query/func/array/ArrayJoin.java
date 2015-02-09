package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ArrayJoin extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ValueList vl = new ValueList();
    final Iter ir = qc.iter(exprs[0]);
    for(Item it; (it = ir.next()) != null;) {
      for(final Value v : toArray(it).members()) vl.add(v);
    }
    return vl.array();
  }
}
