package org.basex.core;

import org.basex.util.Util;

/**
 * Management of executing read/write processes. Multiple readers, single
 * writers (readers/writer lock).
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Andreas Weiler
 */
public final class LockCG {
  /** Synchronization object. */
  private Object mutex = new Object();
  /** Number of active readers. */
  private int readers;
  /** Writer flag. */
  private boolean writer;

  /**
   * Modifications before executing a command.
   * @param w writing flag
   */
  public void register(final boolean w) {
    synchronized(mutex) {
      try {
        while(true) {
          if(!writer) {
            if(w) {
              if(readers == 0) {
                writer = true;
                break;
              }
            } else {
              readers++;
              break;
            }
          }
          mutex.wait();
        }
      } catch(final InterruptedException ex) {
        Util.stack(ex);
      }
    }
  }

  /**
   * Modifications after executing a command.
   * @param w writing flag
   */
  public synchronized void unregister(final boolean w) {
    synchronized(mutex) {
      if(w) {
        writer = false;
        mutex.notifyAll();
      } else {
        if(--readers == 0) mutex.notifyAll();
      }
    }
  }
}
