package org.basex.query.util;

import java.io.*;

import org.basex.core.*;
import org.basex.query.value.item.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Opened database client sessions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ClientSessions {
  /** Last inserted id. */
  private int lastId = -1;
  /** Map with all open sessions and their ids. */
  private final TokenObjMap<ClientSession> conns = new TokenObjMap<ClientSession>();

  /**
   * Adds a session.
   * @param cs client session
   * @return session id
   */
  public Uri add(final ClientSession cs) {
    final byte[] uri = Token.token(Text.NAMELC + "://" + cs + '/' + ++lastId);
    conns.add(uri, cs);
    return Uri.uri(uri);
  }

  /**
   * Returns a session.
   * @param id session id
   * @return session
   */
  public ClientSession get(final Uri id) {
    return conns.get(id.string());
  }

  /**
   * Removes a session.
   * @param id session id
   */
  public void remove(final Uri id) {
    conns.delete(id.string());
  }

  /**
   * Closes all opened sessions.
   */
  public void close() {
    for(final byte[] c : conns) {
      final ClientSession cs = conns.get(c);
      if(cs == null) continue;
      try {
        cs.close();
      } catch(final IOException ex) {
        Util.debug(ex);
      }
    }
  }
}
