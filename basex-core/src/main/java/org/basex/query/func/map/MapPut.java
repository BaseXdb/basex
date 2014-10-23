package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public class MapPut extends StandardFunc {
  @Override
  public final Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return toMap(exprs[0], qc).put(toAtomItem(exprs[1], qc), qc.value(exprs[2]), ii);
  }
}
