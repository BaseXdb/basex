package org.basex.query.func.validate;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ValidateXsdReport extends ValidateXsd {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return report(qc);
  }
}
