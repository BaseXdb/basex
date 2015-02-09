package org.exquery.ns;

import org.basex.http.*;
import org.basex.http.restxq.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * This module contains standard RESTXQ functions.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Restxq extends QueryModule {
  /**
   * Returns an {Code application.wadl} description including all RESTXQ services.
   * @return wadl description
   * @throws QueryException query exception
   */
  public FElem wadl() throws QueryException {
    return RestXqModules.get().wadl(http());
  }

  /**
   * Returns the base URI of the resource function.
   * @return base uri
   * @throws QueryException query exception
   */
  public Uri baseUri() throws QueryException {
    final HTTPContext http = http();
    return Uri.uri(http.req.getRequestURI().replace(http.req.getPathInfo(), ""));
  }

  /**
   * Returns the base URI of the resource function.
   * @return base uri
   * @throws QueryException query exception
   */
  public Uri uri() throws QueryException {
    return Uri.uri(http().req.getRequestURI());
  }

  /**
   * Returns the servlet request instance.
   * @return request
   * @throws QueryException query exception
   */
  private HTTPContext http() throws QueryException {
    if(queryContext.http != null) return (HTTPContext) queryContext.http;
    throw new QueryException("Servlet context required.");
  }
}
