package org.basex.core;

import java.util.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Management of executing read/write processes.
 * Supports multiple readers, limited by {@link GlobalOptions#PARALLEL},
 * and a single writer (readers/writer lock).
 *
 * Since Version 7.6, this locking class has been replaced by {@link DBLocking}.
 * It can still be activated by setting {@link GlobalOptions#GLOBALLOCK} to {@code true}.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class ProcLocking implements Locking {
  /** Queue for all waiting processes. */
  private final LinkedList<Object> queue = new LinkedList<>();
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
   * @param ctx context
   */
  ProcLocking(final Context ctx) {
    this.ctx = ctx;
  }

  @Override
  public void acquire(final Proc pr, final StringList read, final StringList write) {
    final Object o = new Object();

    synchronized(mutex) {
      // add object to queue
      queue.add(o);

      // maximum number of readers
      final int maxReaders = Math.max(ctx.globalopts.get(GlobalOptions.PARALLEL), 1);

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

  @Override
  public void release(final Proc pr) {
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
