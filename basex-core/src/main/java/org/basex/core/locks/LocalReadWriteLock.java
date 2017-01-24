package org.basex.core.locks;

import java.util.concurrent.locks.*;

/**
 * Local read/write locks.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
final class LocalReadWriteLock extends ReentrantReadWriteLock {
  /** Pins. */
  private int pins;

  /**
   * Constructor.
   * @param fair fair locking
   */
  LocalReadWriteLock(final boolean fair) {
    super(fair);
  }

  /**
   * Pins a lock.
   * @return pin count
   */
  int pin() {
    return ++pins;
  }

  /**
   * Unpins a lock.
   * @return pin count
   */
  int unpin() {
    return --pins;
  }
}
