package org.basex.modules;

import static org.basex.query.QueryError.*;

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
final class ASession {
  /** Query context. */
  private final QueryContext qc;
  /** Session id. */
  private final Str id;
  /** Session. */
  private HttpSession session;

  /**
   * Constructor.
   * @param qc query context
   * @param id session id (if {@code null}, local session will be retrieved)
   * @throws QueryException query exception
   */
  ASession(final QueryContext qc, final Str id) throws QueryException {
    this.qc = qc;
    this.id = id;

    final Object req = qc.getProperty(HTTPText.REQUEST);
    if(req == null) throw BASEX_HTTP.get(null);

    if(id == null) {
      session = ((HttpServletRequest) req).getSession();
    } else {
      session = SessionListener.sessions().get(id.toJava());
      if(session == null) throw SESSIONS_NOTFOUND_X.get(null, id);
    }
  }

  /**
   * Returns the session ID.
   * @return session id
   */
  Str id() {
    return Str.get(session.getId());
  }

  /**
   * Returns the creation time.
   * @return creation time
   */
  Dtm created() {
    return Dtm.get(session.getCreationTime());
  }

  /**
   * Returns the last access time.
   * @return creation time
   */
  Dtm accessed() {
    return Dtm.get(session.getLastAccessedTime());
  }

  /**
   * Returns all session attributes.
   * @return session attributes
   */
  Value names() {
    final TokenList tl = new TokenList();
    final Enumeration<String> en = session.getAttributeNames();
    while(en.hasMoreElements()) tl.add(en.nextElement());
    return StrSeq.get(tl);
  }

  /**
   * Returns a session attribute.
   * @param key key to be requested
   * @return session attribute
   * @throws QueryException query exception
   */
  Value get(final Str key) throws QueryException {
    return get(key, null);
  }

  /**
   * Returns a session attribute.
   * @param key key to be requested
   * @param def default value (can be {@code null})
   * @return session attribute
   * @throws QueryException query exception
   */
  Value get(final Str key, final Value def) throws QueryException {
    final Object object = session.getAttribute(key.toJava());
    if(object == null) return def;
    if(object instanceof Value) return (Value) object;
    throw (id == null ? SESSION_GET_X : SESSIONS_GET_X).get(null, QueryError.chop(object, null));
  }

  /**
   * Updates a session attribute.
   * @param key key of the attribute
   * @param value value to be stored
   * @throws QueryException query exception
   */
  void set(final Str key, final Value value) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Item item : value) {
      final Item it = item.materialize(qc, item.persistent());
      if(it == null) throw (id == null ? SESSION_SET_X : SESSIONS_SET_X).get(null, item);
      vb.add(it);
    }
    session.setAttribute(key.toJava(), vb.value());
  }

  /**
   * Removes a session attribute.
   * @param key key of the attribute
   */
  void delete(final Str key) {
    session.removeAttribute(key.toJava());
  }

  /**
   * Closes a session.
   */
  void close() {
    session.invalidate();
  }
}
