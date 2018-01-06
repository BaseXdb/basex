package org.basex.query.func.prof;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class ProfSleep extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final long ms = toLong(exprs[0], qc);
    final Performance perf = new Performance();
    for(int m = 0; m < ms; m++) {
      if((System.nanoTime() - perf.start()) / 1000000 >= ms) break;
      Performance.sleep(1);
      qc.checkStop();
    }
    return null;
  }
}
