package org.basex.modules;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * This module contains functions for processing server-side session data.
 *
 * @author BaseX Team 2005-16, BSD License
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
    return session().get(key);
  }

  /**
   * Returns a session attribute.
   * @param key key to be requested
   * @param def default value
   * @return session attribute
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Value get(final Str key, final Value def) throws QueryException {
    return session().get(key, def);
  }

  /**
   * Updates a session attribute.
   * @param key key of the attribute
   * @param value value to be stored
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public void set(final Str key, final Value value) throws QueryException {
    session().set(key, value);
  }

  /**
   * Removes a session attribute.
   * @param key key of the attribute
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public void delete(final Str key) throws QueryException {
    session().delete(key);
  }

  /**
   * Registers the current query and interrupts a running instance with the same key.
   * @param key key
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public void registerQuery(final Str key) throws QueryException {
    session().registerQuery(key);
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
    if(queryContext.http == null) throw SessionErrors.noContext();
    return new ASession(((HTTPContext) queryContext.http).req.getSession(), queryContext);
  }
}
