package org.basex.modules;

import java.util.*;

import javax.servlet.http.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This module contains functions for processing global sessions.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class ASession {
  /** Query context. */
  private final QueryContext qc;
  /** Session. */
  private final HttpSession session;

  /**
   * Constructor.
   * @param session session
   * @param qc query context
   */
  ASession(final HttpSession session, final QueryContext qc) {
    this.session = session;
    this.qc = qc;
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
   * @throws QueryException query exception
   */
  Dtm created() throws QueryException {
    return new Dtm(session.getCreationTime(), null);
  }

  /**
   * Returns the last access time.
   * @return creation time
   * @throws QueryException query exception
   */
  Dtm accessed() throws QueryException {
    return new Dtm(session.getLastAccessedTime(), null);
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
    final Object o = session.getAttribute(key.toJava());
    if(o == null) return def;
    if(o instanceof Value) return (Value) o;
    throw SessionErrors.noAttribute(Util.className(o));
  }

  /**
   * Updates a session attribute.
   * @param key key of the attribute
   * @param value value to be stored
   * @throws QueryException query exception
   */
  void set(final Str key, final Value value) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(final Item item : value) {
      if(item instanceof FItem) throw SessionErrors.functionItem();
      final Data d = item.data();
      vb.add(d == null || d.inMemory() ? item : ((ANode) item).deepCopy(qc.context.options));
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
