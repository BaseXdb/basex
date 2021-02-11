package org.basex.query.func.session;

import java.util.*;

import javax.servlet.http.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This module contains functions for processing global sessions.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * @return session id
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
    final Enumeration<String> en = session.getAttributeNames();
    while(en.hasMoreElements()) tl.add(en.nextElement());
    return StrSeq.get(tl);
  }

  /**
   * Returns a session attribute.
   * @param key key to be requested
   * @return session attribute or {@code null}
   */
  public Object get(final byte[] key) {
    return session.getAttribute(Token.string(key));
  }

  /**
   * Updates a session attribute.
   * @param name name of the attribute
   * @param value value to be stored
   */
  public void set(final byte[] name, final Value value) {
    session.setAttribute(Token.string(name), value);
  }

  /**
   * Removes a session attribute.
   * @param key key of the attribute
   */
  public void delete(final byte[] key) {
    session.removeAttribute(Token.string(key));
  }

  /**
   * Closes a session.
   */
  public void close() {
    session.invalidate();
  }
}
