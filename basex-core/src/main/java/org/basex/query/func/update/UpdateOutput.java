package org.basex.query.func.update;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UpdateOutput extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    qc.updates().addOutput(arg(0).value(qc), info, qc);
    return Empty.VALUE;
  }
}
