package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class DbOpenId extends DbOpenPre {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return open(qc, false);
  }
}
