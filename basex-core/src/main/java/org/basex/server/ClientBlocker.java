package org.basex.server;

import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class delays blocked clients.
 *
 * @author BaseX Team 2005-15, BSD License
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
    int delay = blocked.get(client);
    delay = delay == -1 ? 1 : Math.min(delay, 1024) << 1;
    blocked.put(client, delay);
    for(int d = delay; d > 0; d--) Performance.sleep(100);
  }

  /**
   * Resets the login delay after successful login.
   * @param client client address
   */
  public synchronized void remove(final byte[] client) {
    blocked.delete(client);
  }
}
