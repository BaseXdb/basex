package org.basex.modules;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * This module contains functions for handling servlet requests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Session extends QueryModule {
  /**
   * Returns the session ID.
   * @return session id
   * @throws QueryException query exception
   */
  @Deterministic
  @Requires(Permission.NONE)
  public String sessionId() throws QueryException {
    return session().getId();
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
    final Object o = session().getAttribute(key);
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
    session().setAttribute(key, value);
  }

  /**
   * Returns the session instance.
   * @return request
   * @throws QueryException query exception
   */
  private HttpSession session() throws QueryException {
    if(context.http == null) throw new QueryException("Servlet context required.");
    return ((HTTPContext) context.http).req.getSession();
  }
}
