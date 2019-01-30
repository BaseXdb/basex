package org.basex.query.func.session;

import java.util.*;

import javax.servlet.http.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This module contains functions for processing global sessions.
 *
 * @author BaseX Team 2005-19, BSD License
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
   * @param value default value
   * @return session attribute or {@code null}
   */
  public Value get(final byte[] key, final Value value) {
    final Object object = session.getAttribute(Token.string(key));
    if(object == null) return value;
    if(object instanceof Value) return (Value) object;
    return null;
  }

  /**
   * Updates a session attribute.
   * @param key key of the attribute
   * @param value value to be stored
   * @param qc query context
   * @return item that cannot be stored, or {@code null} if everything is alright
   */
  public Item set(final byte[] key, final Value value, final QueryContext qc) {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Item item : value) {
      final Item it = item.materialize(qc, item.persistent());
      if(it == null) return item;
      vb.add(it);
    }
    session.setAttribute(Token.string(key), vb.value());
    return null;
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
