package org.basex.modules;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * This module contains functions for processing server-side session data.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class Session extends QueryModule {
  /**
   * Returns the session ID.
   * @return session id
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Str id() throws QueryException {
    return session().id();
  }

  /**
   * Returns the creation time of the session.
   * @return creation time
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Dtm created() throws QueryException {
    return session().created();
  }

  /**
   * Returns the last access time of the session.
   * @return creation time
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Dtm accessed() throws QueryException {
    return session().accessed();
  }

  /**
   * Returns all session attributes.
   * @return session attributes
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Value names() throws QueryException {
    return session().names();
  }

  /**
   * Returns a session attribute.
   * @param key key to be requested
   * @return session attribute
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Value get(final Str key) throws QueryException {
    return get(key, null);
  }

  /**
   * Returns the value of a session attribute.
   * @param name name of attribute
   * @param def default value
   * @return session attribute
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Value get(final Str name, final Value def) throws QueryException {
    return session().get(name, def);
  }

  /**
   * Assigns an attribute to the session.
   * @param name name of attribute
   * @param value value to be stored
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public void set(final Str name, final Value value) throws QueryException {
    session().set(name, value);
  }

  /**
   * Removes a session attribute.
   * @param name name of attribute
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public void delete(final Str name) throws QueryException {
    session().delete(name);
  }

  /**
   * Closes a session.
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public void close() throws QueryException {
    session().close();
  }

  /**
   * Returns a session instance.
   * @return request
   * @throws QueryException query exception
   */
  private ASession session() throws QueryException {
    return new ASession(queryContext, null);
  }
}
