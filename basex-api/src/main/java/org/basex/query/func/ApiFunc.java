package org.basex.query.func;

import static org.basex.query.QueryError.*;

import jakarta.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Request function.
 *
 * @author BaseX Team, BSD License
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

  /**
   * Returns a non-null string.
   * @param string string (can be {@code null})
   * @return string
   */
  public final Str toStr(final String string) {
    return Str.get(string != null ? string : "");
  }
}
