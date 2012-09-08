package org.basex.modules;

import static org.basex.query.util.Err.*;

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
 * This module contains functions for handling servlet requests.
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
  @Deterministic
  @Requires(Permission.NONE)
  public Str id() throws QueryException {
    return Str.get(session().getId());
  }

  /**
   * Returns all session attributes.
   * @return session attribute
   * @throws QueryException query exception
   */
  @ContextDependent
  @Requires(Permission.NONE)
  public Value attributes() throws QueryException {
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
  @ContextDependent
  @Requires(Permission.NONE)
  public Item attribute(final Str key) throws QueryException {
    return attribute(key, null);
  }

  /**
   * Returns a session attribute.
   * @param key key to be requested
   * @param def default item
   * @return session attribute
   * @throws QueryException query exception
   */
  @ContextDependent
  @Requires(Permission.NONE)
  public Item attribute(final Str key, final Item def) throws QueryException {
    final Object o = session().getAttribute(key.toJava());
    if(o == null) return def;
    if(!(o instanceof Item)) BXSE_GET.thrw(null, Util.name(o));
    return (Item) o;
  }

  /**
   * Updates a session attribute.
   * @param key key of the attribute
   * @param item item to be stored
   * @throws QueryException query exception
   */
  @ContextDependent
  @Requires(Permission.NONE)
  public void updateAttribute(final Str key, final Item item) throws QueryException {
    Item it = item;
    final Data d = it.data();
    if(d != null && !d.inMemory()) {
      // convert database node to main memory data instance
      it = ((ANode) it).dbCopy(context.context.prop);
    } else if(it instanceof FItem) {
      BXSE_FITEM.thrw(null, it);
    }
    session().setAttribute(key.toJava(), it);
  }

  /**
   * Returns the session instance.
   * @return request
   * @throws QueryException query exception
   */
  private HttpSession session() throws QueryException {
    if(context.http == null) throw new QueryException("Servlet context required.");
    return ((HTTPContext) context.http).req.getSession();
  }
}
