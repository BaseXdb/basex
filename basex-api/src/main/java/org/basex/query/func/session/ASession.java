package org.basex.query.func.session;

import static org.basex.query.QueryError.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.func.java.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

import jakarta.servlet.http.*;

/**
 * This module contains functions for processing global sessions.
 * Every access goes through {@link RequestState}, which reports a session that the container
 * has dropped; such a session is not available any more, and is reported as such.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ASession {
  /** Session. */
  private final HttpSession session;
  /** ID of the addressed session ({@code null}: session of the current request). */
  private final byte[] id;
  /** Input info (can be {@code null}). */
  private final InputInfo info;

  /**
   * Constructor.
   * @param session HTTP session
   * @param id ID of the addressed session ({@code null}: session of the current request)
   * @param info input info (can be {@code null})
   */
  public ASession(final HttpSession session, final byte[] id, final InputInfo info) {
    this.session = session;
    this.id = id;
    this.info = info;
  }

  /**
   * Returns the session ID.
   * @return session ID
   * @throws QueryException query exception
   */
  public Str id() throws QueryException {
    final String value = RequestState.id(session);
    if(value == null) throw notFound();
    return Str.get(value);
  }

  /**
   * Returns the creation time.
   * @return creation time
   * @throws QueryException query exception
   */
  public Dtm created() throws QueryException {
    return time(RequestState.created(session));
  }

  /**
   * Returns the last access time.
   * @return access time
   * @throws QueryException query exception
   */
  public Dtm accessed() throws QueryException {
    return time(RequestState.accessed(session));
  }

  /**
   * Returns all session attributes.
   * @return session attributes
   * @throws QueryException query exception
   */
  public Value names() throws QueryException {
    final String[] names = RequestState.attributeNames(session);
    if(names == null) throw notFound();
    final TokenList tl = new TokenList();
    for(final String name : names) {
      final byte[] token = XMLToken.check(name, true);
      if(token != null) tl.add(token);
    }
    return StrSeq.get(tl);
  }

  /**
   * Returns the value of a session attribute.
   * @param key attribute key
   * @param qc query context
   * @return value, or {@code null} if attribute does not exist
   * @throws QueryException query exception
   */
  public Value get(final String key, final QueryContext qc) throws QueryException {
    final Object value = RequestState.attribute(session, key);
    return value != null ? JavaCall.toValue(value, qc, info) : null;
  }

  /**
   * Updates a session attribute.
   * @param key attribute key
   * @param value value to be stored
   * @throws QueryException query exception
   */
  public void set(final String key, final Value value) throws QueryException {
    if(!RequestState.attribute(session, key, value)) throw notFound();
  }

  /**
   * Removes a session attribute.
   * @param key attribute key
   * @throws QueryException query exception
   */
  public void delete(final String key) throws QueryException {
    if(!RequestState.remove(session, key)) throw notFound();
  }

  /**
   * Closes a session.
   * @throws QueryException query exception
   */
  public void close() throws QueryException {
    if(!RequestState.invalidate(session)) throw notFound();
  }

  /**
   * Returns a time of the session.
   * @param ms time in milliseconds ({@code -1}: session is not available)
   * @return time
   * @throws QueryException query exception
   */
  private Dtm time(final long ms) throws QueryException {
    if(ms == -1) throw notFound();
    return Dtm.get(ms);
  }

  /**
   * Returns an error for a session that is not available.
   * @return query exception
   */
  private QueryException notFound() {
    return id != null ? SESSIONS_NOTFOUND_X.get(info, id) : SESSION_NOTFOUND.get(info);
  }
}
