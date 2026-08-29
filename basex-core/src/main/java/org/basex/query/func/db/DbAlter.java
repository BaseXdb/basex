package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbAlter extends DbCopy {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    copy(qc, false);
    return Empty.VALUE;
  }

  @Override
  protected boolean writeLock() {
    // the source database is renamed
    return true;
  }
}
