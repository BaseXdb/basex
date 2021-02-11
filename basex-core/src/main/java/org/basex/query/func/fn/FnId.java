package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnId extends Ids {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return ids(qc, false);
  }
}
