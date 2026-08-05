package org.basex.query.func.validate;

import org.basex.query.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ValidateRngReport extends ValidateRng {
  @Override
  public FNode value(final QueryContext qc) throws QueryException {
    return report(qc);
  }
}
