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
 * This module contains functions for processing server-side session data.
 *
 * @author BaseX Team 2005-12, BSD License
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
    return Str.get(session().getId());
  }

  /**
   * Returns the creation time of the session.
   * @return creation time
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Dtm created() throws QueryException {
    return new Dtm(session().getCreationTime(), null);
  }

  /**
   * Returns the last access time of the session.
   * @return creation time
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Dtm accessed() throws QueryException {
    return new Dtm(session().getLastAccessedTime(), null);
  }

  /**
   * Returns all session attributes.
   * @return session attributes
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Value names() throws QueryException {
    final TokenList tl = new TokenList();
    final Enumeration<String> en = session().getAttributeNames();
    while(en.hasMoreElements()) tl.add(en.nextElement());
    return StrSeq.get(tl);
  }

  /**
   * Returns a session attribute.
   * @param key key to be requested
   * @return session attribute
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Item get(final Str key) throws QueryException {
    return get(key, null);
  }

  /**
   * Returns a session attribute.
   * @param key key to be requested
   * @param def default item
   * @return session attribute
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public Item get(final Str key, final Item def) throws QueryException {
    final Object o = session().getAttribute(key.toJava());
    if(o == null) return def;
    if(o instanceof Item) return (Item) o;
    throw SessionErrors.noAttribute(Util.className(o));
  }

  /**
   * Updates a session attribute.
   * @param key key of the attribute
   * @param item item to be stored
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public void set(final Str key, final Item item) throws QueryException {
    Item it = item;
    final Data d = it.data();
    if(d != null && !d.inMemory()) {
      // convert database node to main memory data instance
      it = ((ANode) it).dbCopy(context.context.options);
    } else if(it instanceof FItem) {
      throw SessionErrors.functionItem();
    }
    session().setAttribute(key.toJava(), it);
  }

  /**
   * Removes a session attribute.
   * @param key key of the attribute
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public void delete(final Str key) throws QueryException {
    session().removeAttribute(key.toJava());
  }

  /**
   * Invalidates a session.
   * @throws QueryException query exception
   */
  @Requires(Permission.NONE)
  public void close() throws QueryException {
    session().invalidate();
  }

  /**
   * Returns the session instance.
   * @return request
   * @throws QueryException query exception
   */
  private HttpSession session() throws QueryException {
    if(context.http == null) throw SessionErrors.noContext();
    return ((HTTPContext) context.http).req.getSession();
  }
}
