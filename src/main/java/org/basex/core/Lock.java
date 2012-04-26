package org.basex.core;

import java.util.*;

import org.basex.util.*;

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
   * Modifications before executing a process.
   * @param pr process
   */
  void lock(final Progress pr) {
    final Object o = new Object();

    synchronized(mutex) {
      // add object to queue
      queue.add(o);

      // maximum number of readers
      final int maxReaders = Math.max(ctx.mprop.num(MainProp.PARALLEL), 1);

      while(true) {
        if(!writer && o == queue.get(0)) {
          if(pr.updating) {
            // check updating process
            if(readers == 0) {
              // start writing process
              writer = true;
              break;
            }
          } else if(readers < maxReaders) {
            // increase number of readers
            ++readers;
            break;
          }
        }
        // check if process has already been stopped
        pr.checkStop();
        // wait for next process to be finalized
        try {
          mutex.wait();
        } catch(final InterruptedException ex) {
          Util.stack(ex);
        }
      }
      // start process, remove from queue
      queue.remove(0);
    }
  }

  /**
   * Modifications after executing a command.
   * @param pr process
   */
  void unlock(final Progress pr) {
    synchronized(mutex) {
      if(pr.updating) {
        writer = false;
      } else {
        --readers;
      }
      mutex.notifyAll();
    }
  }
}
