package org.basex.query.func.prof;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class ProfSleep extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final long ms = toLong(exprs[0], qc);
    for(int m = 0; m < ms; m++) {
      Performance.sleep(1);
      qc.checkStop();
    }
    return null;
  }
}
