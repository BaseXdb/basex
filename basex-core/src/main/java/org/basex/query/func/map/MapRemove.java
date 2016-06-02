package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Leo Woerteler
 */
public final class MapRemove extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    Map map = toMap(exprs[0], qc);
    final Iter keys = exprs[1].iter(qc);
    for(Item it; (it = keys.next()) != null;) {
      map = map.delete(toAtomItem(it, qc), info);
    }
    return map;
  }
}
