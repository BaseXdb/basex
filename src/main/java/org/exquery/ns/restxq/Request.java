package org.exquery.ns.restxq;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * This module contains functions for handling servlet requests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Request extends QueryModule {
  /**
   * Returns the session ID.
   * @return session id
   * @throws QueryException query exception
   */
  @Deterministic
  @Requires(Permission.NONE)
  public String sessionId() throws QueryException {
    return request().getSession().getId();
  }

  /**
   * Returns a session attribute.
   * @param key key to be requested
   * @return session attribute
   * @throws QueryException query exception
   */
  @ContextDependent
  @Requires(Permission.NONE)
  public Str attribute(final String key) throws QueryException {
    final Object o = request().getSession().getAttribute(key);
    return o == null ? null : Str.get(o.toString());
  }

  /**
   * Updates a session attribute.
   * @param key key of the attribute
   * @param value value to be set
   * @throws QueryException query exception
   */
  @ContextDependent
  @Requires(Permission.NONE)
  public void updateAttribute(final String key, final String value)
      throws QueryException {
    request().getSession().setAttribute(key, value);
  }

  /**
   * Returns the path of the request.
   * @return path
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public String path() throws QueryException {
    return request().getPathInfo();
  }

  /**
   * Returns the names of all query parameters.
   * @return parameter names
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Value parameterNames() throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(final String s : request().getParameterMap().keySet()) vb.add(Str.get(s));
    return vb.value();
  }

  /**
   * Returns the value of a specific query parameter.
   * @param key key to be requested
   * @return parameter value
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Value parameter(final String key) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    final String[] val = request().getParameterValues(key);
    if(val != null) {
      for(final String v : val) vb.add(Str.get(v));
    }
    return vb.value();
  }

  /**
   * Returns the servlet request instance.
   * @return request
   * @throws QueryException query exception
   */
  private HttpServletRequest request() throws QueryException {
    if(context.http == null) throw new QueryException("Servlet context required.");
    return ((HTTPContext) context.http).req;
  }
}
