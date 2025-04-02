package org.basex.query.func.prof;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProfMemory extends ProfTime {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // measure initial memory consumption
    Performance.gc(4);
    final long min = Performance.memory();
    return evaluate(qc, false, () -> {
      Performance.gc(2);
      return Math.max(0, Performance.memory() - min);
    });
  }
}
