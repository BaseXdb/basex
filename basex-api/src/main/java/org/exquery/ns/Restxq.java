package org.exquery.ns;

import static org.basex.query.QueryError.*;

import org.basex.http.*;
import org.basex.http.restxq.*;
import org.basex.http.web.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * This module contains standard RESTXQ functions.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class Restxq extends QueryModule {
  /**
   * Returns an {@code application.wadl} description with all RESTXQ endpoints.
   * @return WADL description
   * @throws QueryException query exception
   */
  public FElem wadl() throws QueryException {
    return WebModules.get(queryContext.context).wadl(connection());
  }

  /**
   * Returns the base URI of the request.
   * @return base URI
   * @throws QueryException query exception
   */
  public Uri baseUri() throws QueryException {
    final HTTPConnection conn = connection();
    final String uri = conn.req.getRequestURI(), path = conn.req.getPathInfo();
    return Uri.uri(path != null ? uri.substring(0, uri.length() - path.length()) : uri);
  }

  /**
   * Returns the full URI of the request.
   * @return base URI
   * @throws QueryException query exception
   */
  public Uri uri() throws QueryException {
    return Uri.uri(connection().req.getRequestURI());
  }

  /**
   * Initializes the RESTXQ module cache.
   */
  public void init() {
    WebModules.get(queryContext.context).init();
  }

  /**
   * Returns the current HTTP connection.
   * @return HTTP connection
   * @throws QueryException query exception
   */
  private HTTPConnection connection() throws QueryException {
    final Object http = queryContext.getProperty(HTTPText.HTTP);
    if(http == null) throw BASEX_HTTP.get(null);
    return (HTTPConnection) http;
  }
}
