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
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class MapMerge extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // legacy code (obsolete, as only required by map:new)...
    if(exprs.length == 0) return Map.EMPTY;

    Map map = null;
    final Iter maps = exprs[0].iter(qc);
    for(Item it; (it = maps.next()) != null;) {
      final Map m = toMap(it);
      map = map == null ? m : map.addAll(m, ii);
    }
    return map == null ? Map.EMPTY : map;
  }
}
