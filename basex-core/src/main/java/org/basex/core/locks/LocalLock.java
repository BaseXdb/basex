package org.basex.core.locks;

import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

/**
 * Local locks.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class LocalLock extends ReentrantReadWriteLock {
  /** Pins. */
  private final AtomicInteger pins = new AtomicInteger(0);

  /**
   * Constructor.
   * @param fair fair locking
   */
  LocalLock(final boolean fair) {
    super(fair);
  }

  /**
   * Pins a lock.
   * @return pin count
   */
  int pin() {
    return pins.incrementAndGet();
  }

  /**
   * Unpins a lock.
   * @return pin count
   */
  int unpin() {
    return pins.decrementAndGet();
  }
}
