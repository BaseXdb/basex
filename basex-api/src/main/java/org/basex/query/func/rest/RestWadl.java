package org.basex.query.func.rest;

import org.basex.http.web.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestWadl extends ApiFunc {
  @Override
  public FNode value(final QueryContext qc) throws QueryException {
    return WebModules.get(qc.context).wadl(request(qc), qc.context);
  }
}
