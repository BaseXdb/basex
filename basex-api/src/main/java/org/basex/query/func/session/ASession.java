package org.basex.query.func.session;

import java.util.*;

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
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ASession {
  /** Session. */
  private final HttpSession session;

  /**
   * Constructor.
   * @param session HTTP session
   */
  public ASession(final HttpSession session) {
    this.session = session;
  }

  /**
   * Returns the session ID.
   * @return session ID
   */
  public Str id() {
    return Str.get(session.getId());
  }

  /**
   * Returns the creation time.
   * @return creation time
   */
  public Dtm created() {
    return Dtm.get(session.getCreationTime());
  }

  /**
   * Returns the last access time.
   * @return creation time
   */
  public Dtm accessed() {
    return Dtm.get(session.getLastAccessedTime());
  }

  /**
   * Returns all session attributes.
   * @return session attributes
   */
  public Value names() {
    final TokenList tl = new TokenList();
    for(final String name : Collections.list(session.getAttributeNames())) {
      final byte[] token = XMLToken.token(name, true);
      if(token != null) tl.add(token);
    }
    return StrSeq.get(tl);
  }

  /**
   * Returns the value of a session attribute.
   * @param key key to be requested
   * @param qc query context
   * @param info input info
   * @return value, or {@code null} if attribute does not exist
   * @throws QueryException query exception
   */
  public Value get(final String key, final QueryContext qc, final InputInfo info)
      throws QueryException {
    final Object value = HTTPConnection.getAttribute(session, key);
    return value != null ? JavaCall.toValue(value, qc, info) : null;
  }

  /**
   * Updates a session attribute.
   * @param name name of the attribute
   * @param value value to be stored
   */
  public void set(final String name, final Value value) {
    session.setAttribute(name, value);
  }

  /**
   * Removes a session attribute.
   * @param key key of the attribute
   */
  public void delete(final String key) {
    session.removeAttribute(key);
  }

  /**
   * Closes a session.
   */
  public void close() {
    session.invalidate();
  }
}
