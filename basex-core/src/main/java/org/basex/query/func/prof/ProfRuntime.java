package org.basex.query.func.prof;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class ProfRuntime extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Runtime rt = Runtime.getRuntime();
    final long max = rt.maxMemory();
    final long total = rt.totalMemory();
    final long used = total - rt.freeMemory();
    final long procs = rt.availableProcessors();

    final MapBuilder mb = new MapBuilder();
    mb.put("max", Int.get(max));
    mb.put("total", Int.get(total));
    mb.put("used", Int.get(used));
    mb.put("processors", Int.get(procs));
    return mb.finish();
  }
}
