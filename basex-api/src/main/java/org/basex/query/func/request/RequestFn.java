package org.basex.query.func.request;

import static org.basex.query.QueryError.*;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.func.*;

/**
 * Request function.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
abstract class RequestFn extends StandardFunc {
  /**
   * Returns the current HTTP servlet request.
   * @param qc query context
   * @return HTTP request
   * @throws QueryException query exception
   */
  final HttpServletRequest request(final QueryContext qc) throws QueryException {
    final Object req = qc.getProperty(HTTPText.REQUEST);
    if(req == null) throw BASEX_HTTP.get(info);
    return (HttpServletRequest) req;
  }
}
