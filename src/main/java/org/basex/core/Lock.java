package org.basex.core;

import java.util.LinkedList;
import org.basex.util.Util;

/**
 * Management of executing read/write processes.
 * Supports multiple readers, limited by {@link Prop#PARALLEL},
 * and single writers (readers/writer lock).
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
final class Lock {
  /** Queue. */
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
  void register(final boolean w) {
    synchronized(mutex) {
      final Object o = new Object();
      queue.add(o);

      try {
        while(true) {
          if(o == queue.get(0) && !writer) {
            if(w) {
              if(readers == 0) {
                writer = true;
                break;
              }
            } else if(readers < Math.max(ctx.prop.num(Prop.PARALLEL), 1)) {
              ++readers;
              break;
            }
          }
          mutex.wait();
        }
      } catch(final InterruptedException ex) {
        Util.stack(ex);
      }

      queue.remove(0);
    }
  }

  /**
   * Modifications after executing a command.
   * @param w writing flag
   */
  synchronized void unregister(final boolean w) {
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
