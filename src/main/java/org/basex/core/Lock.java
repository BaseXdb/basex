package org.basex.core;

import java.util.LinkedList;
import org.basex.util.Util;

/**
 * Management of executing read/write processes.
 * Supports multiple readers, limited by {@link MainProp#PARALLEL},
 * and a single writer (readers/writer lock).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class Lock {
  /** Queue for all waiting processes. */
  private final LinkedList<Object> queue = new LinkedList<Object>();
  /** Mutex object. */
  private final Object mutex = new Object();
  /** Database context. */
  private final Context ctx;

  /** Number of active readers. */
  private int readers;
  /** Writer flag. */
  private boolean writer;

  /**
   * Default constructor.
   * @param c context
   */
  Lock(final Context c) {
    ctx = c;
  }

  /**
   * Modifications before executing a command.
   * @param w writing flag
   */
  void lock(final boolean w) {
    final Object o = new Object();

    synchronized(mutex) {
      queue.add(o);

      while(true) {
        if(!writer && o == queue.get(0)) {
          if(w) {
            if(readers == 0) {
              writer = true;
              break;
            }
          } else if(readers < Math.max(ctx.mprop.num(MainProp.PARALLEL), 1)) {
            ++readers;
            break;
          }
        }
        try {
          mutex.wait();
        } catch(final InterruptedException ex) {
          Util.stack(ex);
        }
      }

      queue.remove(0);
    }
  }

  /**
   * Modifications after executing a command.
   * @param w writing flag
   */
  void unlock(final boolean w) {
    synchronized(mutex) {
      if(w) {
        writer = false;
      } else {
        --readers;
      }
      mutex.notifyAll();
    }
  }
}
