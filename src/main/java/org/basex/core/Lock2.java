package org.basex.core;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Management of executing read/write processes. Multiple readers, single
 * writers (readers/writer lock).
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Andreas Weiler
 */
public final class Lock2 {
  /** Flag for skipping all locking tests. */
  private static final boolean SKIP = false;
  /** List of waiting processes for writers or reading groups. */
  /** Read/Write lock. */
  final ReadWriteLock lock = new ReentrantReadWriteLock(true);

  /**
   * Default constructor.
   */
  public Lock2() {
  }

  /**
   * Modifications before executing a command.
   * @param w writing flag
   */
  public void register(final boolean w) {
    if(SKIP) return;
    if(w) {
      lock.writeLock().lock();
      return;
    }
    lock.readLock().lock();
  }

  /**
   * Modifications after executing a command.
   * @param w writing flag
   */
  public void unregister(final boolean w) {
    if(SKIP) return;
    if(w) {
      lock.writeLock().unlock();
      return;
    }
    lock.readLock().unlock();
  }
}
