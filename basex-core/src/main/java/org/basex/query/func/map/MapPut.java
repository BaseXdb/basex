package org.basex.query.func.map;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class MapPut extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Map map = toMap(exprs[0], qc);
    final Item key = toAtomItem(exprs[1], qc);
    final Value val = qc.value(exprs[2]);
    if(!map.checkTz(key)) throw MAP_TZ.get(ii);
    return map.put(key, val, ii);
  }
}
