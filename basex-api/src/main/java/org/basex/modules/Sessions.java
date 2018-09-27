package org.basex.modules;

import java.util.*;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * This module contains functions for processing global sessions.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class Sessions extends QueryModule {
  /**
   * Returns the ids of all registered sessions.
   * @return session ids
   */
  public static Value ids() {
    final HashMap<String, HttpSession> http = SessionListener.sessions();
    final TokenList tl = new TokenList(http.size());
    for(final String s : http.keySet()) tl.add(s);
    return StrSeq.get(tl);
  }

  /**
   * Returns the creation time of the session.
   * @param id session id
   * @return creation time
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Dtm created(final Str id) throws QueryException {
    return session(id).created();
  }

  /**
   * Returns the last access time of the session.
   * @param id session id
   * @return creation time
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Dtm accessed(final Str id) throws QueryException {
    return session(id).accessed();
  }

  /**
   * Returns all attributes of a session.
   * @param id session id
   * @return session attributes
   * @throws QueryException query exception
   */
  public Value names(final Str id) throws QueryException {
    return session(id).names();
  }

  /**
   * Returns the value of a session attribute.
   * @param id session id
   * @param name name of the attribute
   * @return attribute value
   * @throws QueryException query exception
   */
  public Value get(final Str id, final Str name) throws QueryException {
    return get(id, name, null);
  }

  /**
   * Returns the value of a session attribute.
   * @param id session id
   * @param name name of the attribute
   * @param def default value
   * @return attribute value
   * @throws QueryException query exception
   */
  public Value get(final Str id, final Str name, final Value def) throws QueryException {
    return session(id).get(name, def);
  }

  /**
   * Assigns an attribute to the session.
   * @param id session id
   * @param name name of the attribute
   * @param value item to be stored
   * @throws QueryException query exception
   */
  public void set(final Str id, final Str name, final Value value) throws QueryException {
    session(id).set(name, value);
  }

  /**
   * Removes a session attribute.
   * @param id session id
   * @param name name of the attribute
   * @throws QueryException query exception
   */
  public void delete(final Str id, final Str name) throws QueryException {
    session(id).delete(name);
  }

  /**
   * Closes a session.
   * @param id session id
   * @throws QueryException query exception
   */
  public void close(final Str id) throws QueryException {
    session(id).close();
  }

  /**
   * Returns a session instance.
   * @param id session id
   * @return request
   * @throws QueryException query exception
   */
  private ASession session(final Str id) throws QueryException {
    return new ASession(queryContext, id);
  }
}
