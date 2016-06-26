package org.basex.core.locks;

import java.util.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.util.*;

/**
 * Management of executing read/write jobs.
 * Supports multiple readers, limited by {@link StaticOptions#PARALLEL},
 * and a single writer (readers/writer lock).
 *
 * Since Version 7.6, this locking class has been replaced by {@link DBLocking}.
 * It can still be activated by setting {@link StaticOptions#GLOBALLOCK} to {@code true}.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class JobLocking implements Locking {
  /** Queue for all waiting jobs. */
  private final LinkedList<Object> queue = new LinkedList<>();
  /** Mutex object. */
  private final Object mutex = new Object();
  /** Database context. */
  private final StaticOptions sopts;

  /** Number of active readers. */
  private int readers;
  /** Writer flag. */
  private boolean writer;

  /**
   * Default constructor.
   * @param ctx database context
   */
  public JobLocking(final Context ctx) {
    this.sopts = ctx.soptions;
  }

  @Override
  public void acquire(final Job job) {
    final Object o = new Object();

    synchronized(mutex) {
      // add object to queue
      queue.add(o);

      final int parallel = Math.max(sopts.get(StaticOptions.PARALLEL), 1);
      while(true) {
        if(!writer && o == queue.get(0)) {
          if(job.updating) {
            // check updating job
            if(readers == 0) {
              // start writing job
              writer = true;
              break;
            }
          } else if(readers < parallel) {
            // increase number of readers
            ++readers;
            break;
          }
        }
        // check if job has already been stopped
        job.checkStop();
        // wait for next job to be finalized
        try {
          mutex.wait();
        } catch(final InterruptedException ex) {
          Util.stack(ex);
        }
      }
      // start job, remove from queue
      queue.remove(0);
    }
  }

  @Override
  public void release(final Job job) {
    synchronized(mutex) {
      if(job.updating) {
        writer = false;
      } else {
        --readers;
      }
      mutex.notifyAll();
    }
  }
}
