package org.basex.server;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Client info.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public interface ClientInfo {
  /**
   * Returns the host and port of a client.
   * @return address of client
   */
  String clientAddress();

  /**
   * Returns the name of the current client.
   * @return name of client
   */
  String clientName();

  /**
   * Returns the name of a client, taken from the specified object or from the logged in user.
   * @param id object with user id (can be {@code null})
   * @param ctx database context
   * @return name of client or {@code null}
   */
  default String clientName(final Object id, final Context ctx) {
    // try to get string representation of supplied user id
    try {
      if(id instanceof Item) return Token.string(((Item) id).string(null));
    } catch(final QueryException ex) {
      Util.debug(ex);
    }

    // check for authenticated user
    final User user = ctx.user();
    if(user != null) return user.name();

    // user is unknown
    return null;
  }
}
