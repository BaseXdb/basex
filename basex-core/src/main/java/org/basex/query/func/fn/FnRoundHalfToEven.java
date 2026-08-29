package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnRoundHalfToEven extends FnRound {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return round(qc, RoundMode.HALF_TO_EVEN);
  }
}
