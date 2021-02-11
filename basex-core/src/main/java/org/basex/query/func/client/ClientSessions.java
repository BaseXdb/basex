package org.basex.query.func.client;

import java.io.*;

import org.basex.api.client.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Opened database client sessions.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ClientSessions implements QueryResource {
  /** Last inserted id. */
  private int lastId = -1;
  /** Map with all open sessions and their ids. */
  private final TokenObjMap<ClientSession> conns = new TokenObjMap<>();

  /**
   * Adds a session.
   * @param cs client session
   * @return session id
   */
  synchronized Uri add(final ClientSession cs) {
    final byte[] uri = Token.token(cs + "/" + ++lastId);
    conns.put(uri, cs);
    return Uri.uri(uri);
  }

  /**
   * Returns a session.
   * @param id session id
   * @return session
   */
  synchronized ClientSession get(final Uri id) {
    return conns.get(id.string());
  }

  /**
   * Removes a session.
   * @param id session id
   */
  synchronized void remove(final Uri id) {
    conns.remove(id.string());
  }

  @Override
  public synchronized void close() {
    for(final ClientSession cs : conns.values()) {
      try {
        if(cs != null) cs.close();
      } catch(final IOException ex) {
        Util.debug(ex);
      }
    }
    conns.clear();
  }
}
