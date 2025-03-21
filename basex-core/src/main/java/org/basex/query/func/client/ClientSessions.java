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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ClientSessions implements QueryResource {
  /** Last inserted ID. */
  private int lastId = -1;
  /** Map with all open sessions and their IDs. */
  private final TokenObjectMap<ClientSession> conns = new TokenObjectMap<>();

  /**
   * Adds a session.
   * @param cs client session
   * @return session ID
   */
  synchronized Uri add(final ClientSession cs) {
    final byte[] uri = Token.token(cs + "/" + ++lastId);
    conns.put(uri, cs);
    return Uri.get(uri);
  }

  /**
   * Returns a session.
   * @param id session ID
   * @return session
   */
  synchronized ClientSession get(final Uri id) {
    return conns.get(id.string());
  }

  /**
   * Removes a session.
   * @param id session ID
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
