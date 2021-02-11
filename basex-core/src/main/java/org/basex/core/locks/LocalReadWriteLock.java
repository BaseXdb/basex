package org.basex.core.locks;

import java.util.concurrent.locks.*;

/**
 * Local read/write locks.
 *
 * @author BaseX Team 2005-21, BSD License
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
   */
  void pin() {
    ++pins;
  }

  /**
   * Unpins a lock.
   * @return if no pins are left
   */
  boolean unpin() {
    return --pins == 0;
  }
}
