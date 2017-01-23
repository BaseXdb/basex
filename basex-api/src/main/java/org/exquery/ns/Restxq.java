package org.exquery.ns;

import org.basex.http.*;
import org.basex.http.restxq.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * This module contains standard RESTXQ functions.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class Restxq extends QueryModule {
  /**
   * Returns an {Code application.wadl} description including all RESTXQ services.
   * @return wadl description
   * @throws QueryException query exception
   */
  public FElem wadl() throws QueryException {
    return RestXqModules.get(queryContext.context).wadl(connection());
  }

  /**
   * Returns the base URI of the resource function.
   * @return base uri
   * @throws QueryException query exception
   */
  public Uri baseUri() throws QueryException {
    final HTTPConnection conn = connection();
    return Uri.uri(conn.req.getRequestURI().replace(conn.req.getPathInfo(), ""));
  }

  /**
   * Returns the base URI of the resource function.
   * @return base uri
   * @throws QueryException query exception
   */
  public Uri uri() throws QueryException {
    return Uri.uri(connection().req.getRequestURI());
  }

  /**
   * Initializes the RESTXQ module cache.
   */
  public void init() {
    RestXqModules.get(queryContext.context).init();
  }

  /**
   * Returns the current HTTP connection.
   * @return HTTP connection
   * @throws QueryException query exception
   */
  private HTTPConnection connection() throws QueryException {
    if(queryContext.http != null) return (HTTPConnection) queryContext.http;
    throw new QueryException("HTTP connection required.");
  }
}
