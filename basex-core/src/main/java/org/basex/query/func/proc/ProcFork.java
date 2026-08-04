package org.basex.query.func.proc;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProcFork extends ProcFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    exec(qc, true);
    return Empty.VALUE;
  }
}
