package org.basex.query.func.update;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class UpdateCache extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final boolean reset = exprs.length > 0 && toBoolean(exprs[0], qc);
    return qc.updates().output(reset);
  }
}
