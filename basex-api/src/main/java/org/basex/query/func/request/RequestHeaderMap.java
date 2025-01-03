package org.basex.query.func.request;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestHeaderMap extends ApiFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final RequestContext requestCtx = requestContext(qc);
    return requestCtx.headers();
  }
}
