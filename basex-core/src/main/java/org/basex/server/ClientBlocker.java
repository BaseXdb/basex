package org.basex.server;

import static org.basex.util.Token.*;

import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class delays blocked clients.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ClientBlocker {
  /** Delay per failed login, in milliseconds. */
  private static final long DELAY = 500;
  /** Maximum delay, in milliseconds. */
  private static final long MAX = 10_000;
  /** Time of inactivity after which all clients are forgotten, in milliseconds. */
  private static final long EXPIRE = 60_000;

  /** Failed logins, indexed by client address and username. */
  private final TokenIntMap blocked = new TokenIntMap();
  /** Time of the last failed login. */
  private long last;

  /**
   * Registers a failed login and delays the process.
   * @param client client address
   * @param name username (can be {@code null})
   */
  public void delay(final byte[] client, final String name) {
    final int count;
    synchronized(this) {
      final long time = System.currentTimeMillis();
      if(time - last > EXPIRE) blocked.clear();
      last = time;
      // the address counter throttles guesses across accounts, the username counter across clients
      count = Math.max(register(client), register(key(name)));
    }
    Performance.sleep(Math.min((count - 1) * DELAY, MAX));
  }

  /**
   * Resets the login delay after a successful login.
   * @param client client address
   * @param name username (can be {@code null})
   */
  public synchronized void remove(final byte[] client, final String name) {
    blocked.remove(client);
    final byte[] key = key(name);
    if(key != null) blocked.remove(key);
  }

  /**
   * Increases and returns the number of failed logins for a key.
   * @param key key (can be {@code null})
   * @return number of failed logins
   */
  private int register(final byte[] key) {
    if(key == null) return 0;
    final int count = Math.max(blocked.get(key), 0) + 1;
    blocked.put(key, count);
    return count;
  }

  /**
   * Returns the map key for a username. The prefix keeps it apart from client addresses.
   * @param name username (can be {@code null})
   * @return key, or {@code null} if no username was supplied
   */
  private static byte[] key(final String name) {
    return name == null || name.isEmpty() ? null : token('@' + name);
  }
}
