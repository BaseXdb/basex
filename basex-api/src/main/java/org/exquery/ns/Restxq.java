package org.exquery.ns;

import static org.basex.query.QueryError.*;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * This module contains standard RESTXQ functions.
 * The class name is {@code Restxq} instead of {@code RESTXQ}.
 * Otherwise, it would be resolved to {@code r-e-s-t-x-q} in XQuery.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class Restxq extends QueryModule {
  /**
   * Returns an {@code application.wadl} description with all RESTXQ end-points.
   * @return WADL description
   * @throws QueryException query exception
   */
  public FElem wadl() throws QueryException {
    return WebModules.get(queryContext.context).wadl(request());
  }

  /**
   * Returns the base URI of the request.
   * @return base URI
   * @throws QueryException query exception
   */
  public Uri baseUri() throws QueryException {
    final HttpServletRequest req = request();
    final String uri = req.getRequestURI(), path = req.getPathInfo();
    return Uri.uri(path != null ? uri.substring(0, uri.length() - path.length()) : uri);
  }

  /**
   * Returns the full URI of the request.
   * @return base URI
   * @throws QueryException query exception
   */
  public Uri uri() throws QueryException {
    return Uri.uri(request().getRequestURI());
  }

  /**
   * Initializes the web module cache.
   */
  public void init() {
    WebModules.get(queryContext.context).init();
  }

  /**
   * Returns the current HTTP servlet request.
   * @return HTTP request
   * @throws QueryException query exception
   */
  private HttpServletRequest request() throws QueryException {
    final Object req = queryContext.getProperty(HTTPText.REQUEST);
    if(req == null) throw BASEX_HTTP.get(null);
    return (HttpServletRequest) req;
  }
}
