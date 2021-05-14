package org.basex.query.func;

import static org.basex.query.QueryError.*;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;

/**
 * Request function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class ApiFunc extends StandardFunc {
  /**
   * Returns the current HTTP servlet request.
   * @param qc query context
   * @return HTTP request
   * @throws QueryException query exception
   */
  public final RequestContext requestContext(final QueryContext qc) throws QueryException {
    final RequestContext rc = (RequestContext) qc.context.getExternal(RequestContext.class);
    if(rc == null) throw BASEX_HTTP.get(info);
    return rc;
  }

  /**
   * Returns the current HTTP servlet request.
   * @param qc query context
   * @return HTTP request
   * @throws QueryException query exception
   */
  public final HttpServletRequest request(final QueryContext qc) throws QueryException {
    return requestContext(qc).request;
  }
}
