package org.basex.modules;

import java.util.*;

import javax.servlet.http.*;

import org.basex.data.*;
import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This module contains functions for processing server-side session data.
 *
 * @author BaseX Team 2005-15, BSD License
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
  public Value get(final Str key) throws QueryException {
    return get(key, null);
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
    final Object o = session().getAttribute(key.toJava());
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
  @Requires(Permission.NONE)
  public void set(final Str key, final Value value) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(Math.max(1, (int) value.size()));
    for(final Item item : value) {
      if(item instanceof FItem) throw SessionErrors.functionItem();
      final Data d = item.data();
      if(d != null && !d.inMemory()) {
        // convert database node to main memory data instance
        vb.add(((ANode) item).dbCopy(queryContext.context.options));
      } else {
        vb.add(item);
      }
    }
    session().setAttribute(key.toJava(), vb.value());
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
    if(queryContext.http == null) throw SessionErrors.noContext();
    return ((HTTPContext) queryContext.http).req.getSession();
  }
}
