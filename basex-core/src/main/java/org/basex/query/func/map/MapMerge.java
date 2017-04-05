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
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class MapMerge extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter maps = qc.iter(exprs[0]);
    final MergeOptions opts = new MergeOptions();
    if(exprs.length > 1) new FuncOptions(info).acceptUnknown().assign(toMap(exprs[1], qc), opts);

    final MergeDuplicates merge = opts.get(MergeOptions.DUPLICATES);
    Map map = Map.EMPTY;
    for(Item it; (it = maps.next()) != null;) {
      qc.checkStop();
      map = map.addAll(toMap(it), merge, info);
    }
    return map;
  }
}
