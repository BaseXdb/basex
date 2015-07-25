package org.basex.query.func.validate;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ValidateRng extends ValidateRngInfo {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return check(qc);
  }
}
