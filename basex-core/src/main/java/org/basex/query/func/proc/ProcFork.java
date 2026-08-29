package org.basex.query.func.proc;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProcFork extends ProcFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    exec(qc, true);
    return Empty.VALUE;
  }
}
