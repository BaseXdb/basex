package org.basex.query.func.prof;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ProfMemory extends ProfTime {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // measure initial memory consumption
    Performance.gc(4);
    final long min = Performance.memory();
    return value(qc, () -> {
      Performance.gc(2);
      return token(Performance.format(Math.max(0, Performance.memory() - min)));
    });
  }
}
