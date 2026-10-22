package org.basex.server;

import static org.basex.util.Token.*;

import java.net.*;
import java.util.*;
import java.util.function.*;

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
  /** Client addresses and usernames whose failed login is currently being delayed. */
  private final HashSet<String> delayed = new HashSet<>();
  /** Client addresses and usernames whose password is currently being checked. */
  private final HashSet<String> checked = new HashSet<>();
  /** Time of the last failed login. */
  private long last;

  /**
   * Checks a login, and resets or delays the client depending on the result.
   * @param address client address
   * @param name username (can be {@code null})
   * @param check password check
   * @return success flag
   */
  public boolean login(final InetAddress address, final String name, final BooleanSupplier check) {
    // the textual form is shared by all protocols
    final String host = address.getHostAddress();
    final byte[] client = token(host), user = key(name);
    final String attempt = user == null ? host : host + '@' + name;
    // parallel attempts of a client and user wait for running checks and fail during delays
    synchronized(this) {
      try {
        while(checked.contains(attempt)) wait();
      } catch(final InterruptedException ex) {
        Thread.currentThread().interrupt();
        return false;
      }
      if(delayed.contains(attempt)) return false;
      checked.add(attempt);
    }
    boolean success = false;
    try {
      success = check.getAsBoolean();
    } finally {
      synchronized(this) {
        checked.remove(attempt);
        if(success) {
          // reset the login delay
          blocked.remove(client);
          if(user != null) blocked.remove(user);
        } else {
          delayed.add(attempt);
        }
        notifyAll();
      }
      if(!success) delay(client, user, attempt);
    }
    return success;
  }

  /**
   * Registers a failed login and delays the process.
   * @param client client key
   * @param user user key (can be {@code null})
   * @param attempt key of the client and username
   */
  private void delay(final byte[] client, final byte[] user, final String attempt) {
    final int count;
    synchronized(this) {
      final long time = System.currentTimeMillis();
      if(time - last > EXPIRE) blocked.clear();
      last = time;
      // the address counter throttles guesses across accounts, the username counter across clients
      count = Math.max(register(client), register(user));
    }
    try {
      Performance.sleep(Math.min((count - 1) * DELAY, MAX));
    } finally {
      synchronized(this) {
        delayed.remove(attempt);
      }
    }
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
