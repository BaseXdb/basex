package org.basex.query.func.validate;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ValidateXsdInfo extends ValidateXsd {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return info(qc);
  }
}
