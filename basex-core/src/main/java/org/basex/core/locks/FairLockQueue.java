package org.basex.core.locks;

import java.util.*;

/**
 * Lock queue for fair locking.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class FairLockQueue extends LockQueue {
  /** Queue. */
  private final Queue<Long> queue = new LinkedList<>();

  /**
   * Constructor.
   * @param parallel parallel jobs
   */
  FairLockQueue(final int parallel) {
    super(parallel);
  }

  @Override
  synchronized void acquire(final Long id, final boolean read, final boolean write)
      throws InterruptedException {

    // add job ID to queue and wait
    if(jobs >= parallel) {
      queue.add(id);

      // loop until job is placed first; an interrupt must not leave the ID behind
      try {
        do {
          wait();
        } while(!id.equals(queue.peek()));
      } finally {
        queue.remove(id);
      }
    }
    jobs++;
  }

  @Override
  public synchronized String toString() {
    return "Jobs: " + jobs + ", queue: " + queue;
  }
}
