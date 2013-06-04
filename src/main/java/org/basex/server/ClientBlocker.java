package org.basex.server;

import org.basex.util.hash.*;

/**
 * This class delays blocked clients.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ClientBlocker {
  /** Temporarily blocked clients. */
  private final TokenIntMap blocked = new TokenIntMap();

  /**
   * Registers the client and returns a delay time.
   * @param client client address
   * @return number of seconds to wait
   */
  public synchronized int delay(final byte[] client) {
    int delay = blocked.value(client);
    delay = delay == -1 ? 1 : Math.min(delay, 1024) * 2;
    blocked.put(client, delay);
    return delay;
  }

  /**
   * Resets the login delay after successful login.
   * @param client client address
   */
  public synchronized void remove(final byte[] client) {
    blocked.delete(client);
  }
}
