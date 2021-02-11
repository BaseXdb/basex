package org.basex.server;

import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class delays blocked clients.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ClientBlocker {
  /** Temporarily blocked clients. */
  private final TokenIntMap blocked = new TokenIntMap();

  /**
   * Registers the client and delays the process.
   * @param client client address
   */
  public synchronized void delay(final byte[] client) {
    // do not delay more than 10 seconds
    int delay = Math.min(blocked.get(client) + 1, 20);
    blocked.put(client, delay);
    while(--delay > 0) Performance.sleep(500);
  }

  /**
   * Resets the login delay after successful login.
   * @param client client address
   */
  public synchronized void remove(final byte[] client) {
    blocked.remove(client);
  }
}
