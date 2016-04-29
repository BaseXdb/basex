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
public final class MapMerge extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    Map map = Map.EMPTY;
    final Iter maps = exprs[0].iter(qc);
    for(Item it; (it = maps.next()) != null;) {
      final Map m = toMap(it);
      map = map == Map.EMPTY ? m : map.addAll(m, info);
    }
    return map;
  }
}
