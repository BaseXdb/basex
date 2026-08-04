package org.basex.query.func.inspect;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class InspectContext extends StandardFunc {
  @Override
  protected FNode item(final QueryContext qc) throws QueryException {
    return new PlainDoc(qc, info).context();
  }
}
