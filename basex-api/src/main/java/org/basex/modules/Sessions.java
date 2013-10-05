package org.basex.modules;

import java.util.*;

import javax.servlet.http.*;

import org.basex.data.*;
import org.basex.http.*;
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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Sessions extends QueryModule {
  /**
   * Returns the ids of all registered sessions.
   * @return session ids
   */
  public Value ids() {
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
    return new Dtm(session(id).getCreationTime(), null);
  }

  /**
   * Returns the last access time of the session.
   * @param id session id
   * @return creation time
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Dtm accessed(final Str id) throws QueryException {
    return new Dtm(session(id).getLastAccessedTime(), null);
  }

  /**
   * Returns all attributes of the specified session.
   * @param id session id
   * @return session attributes
   * @throws QueryException query exception
   */
  public Value names(final Str id) throws QueryException {
    final TokenList tl = new TokenList();
    final Enumeration<String> en = session(id).getAttributeNames();
    while(en.hasMoreElements()) tl.add(en.nextElement());
    return StrSeq.get(tl);
  }

  /**
   * Returns the specified session attribute of a session.
   * @param id session id
   * @param key key to be requested
   * @return session attribute
   * @throws QueryException query exception
   */
  public Item get(final Str id, final Str key) throws QueryException {
    return get(id, key, null);
  }

  /**
   * Returns the specified session attribute of a session.
   * @param id session id
   * @param key key to be requested
   * @param def default item
   * @return session attribute
   * @throws QueryException query exception
   */
  public Item get(final Str id, final Str key, final Item def) throws QueryException {
    final Object o = session(id).getAttribute(key.toJava());
    if(o == null) return def;
    if(o instanceof Item) return (Item) o;
    throw SessionErrors.noAttribute(Util.name(o));
  }

  /**
   * Updates a session attribute.
   * @param id session id
   * @param key key of the attribute
   * @param item item to be stored
   * @throws QueryException query exception
   */
  public void set(final Str id, final Str key, final Item item) throws QueryException {
    Item it = item;
    final Data d = it.data();
    if(d != null && !d.inMemory()) {
      // convert database node to main memory data instance
      it = ((ANode) it).dbCopy(context.context.options);
    } else if(it instanceof FItem) {
      throw SessionErrors.functionItem();
    }
    session(id).setAttribute(key.toJava(), it);
  }

  /**
   * Removes a session attribute.
   * @param id session id
   * @param key key of the attribute
   * @throws QueryException query exception
   */
  public void delete(final Str id, final Str key) throws QueryException {
    session(id).removeAttribute(key.toJava());
  }

  /**
   * Invalidates a session.
   * @param id session id
   * @throws QueryException query exception
   */
  public void close(final Str id) throws QueryException {
    session(id).invalidate();
  }

  /**
   * Returns the specified session.
   * @param id ids
   * @return request
   * @throws QueryException query exception
   */
  private HttpSession session(final Str id) throws QueryException {
    if(context.http == null) throw SessionErrors.noContext();
    final HashMap<String, HttpSession> http = SessionListener.sessions();
    final HttpSession session = http.get(id.toJava());
    if(session == null) throw SessionErrors.whichSession(id);
    return session;
  }
}
