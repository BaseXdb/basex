package org.basex.query.func.request;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.map.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestHeaderMap extends ApiFunc {
  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    final RequestContext requestCtx = requestContext(qc);
    return requestCtx.headers();
  }
}
