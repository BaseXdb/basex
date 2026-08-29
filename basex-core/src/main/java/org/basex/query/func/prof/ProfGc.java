package org.basex.query.func.prof;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProfGc extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Long count = toLongOrNull(arg(0), qc);
    Performance.gc((int) Math.min(Integer.MAX_VALUE, count != null ? count : 1));
    return Empty.VALUE;
  }
}
