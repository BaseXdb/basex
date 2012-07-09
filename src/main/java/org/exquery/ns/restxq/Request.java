package org.exquery.ns.restxq;

import javax.servlet.http.*;
import org.basex.query.*;
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
   * @param request servlet request
   * @return session id
   */
  @Deterministic
  @Requires(Permission.NONE)
  public String sessionId(final HttpServletRequest request) {
    return request.getSession().getId();
  }

  /**
   * Returns a session attribute.
   * @param request servlet request
   * @param key key to be requested
   * @return session attribute
   */
  @ContextDependent
  @Requires(Permission.NONE)
  public Str getAttribute(final HttpServletRequest request, final String key) {
    final Object o = request.getSession().getAttribute(key);
    return o == null ? null : Str.get(o.toString());
  }

  /**
   * Sets a session attribute.
   * @param request servlet request
   * @param key key of the attribute
   * @param value value to be set
   */
  @ContextDependent
  @Requires(Permission.NONE)
  public void setAttribute(final HttpServletRequest request, final String key,
      final String value) {
    request.getSession().setAttribute(key, value);
  }
}
