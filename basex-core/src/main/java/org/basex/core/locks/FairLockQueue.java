package org.basex.core.locks;

import java.util.*;

/**
 * Lock queue for fair locking.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FairLockQueue extends LockQueue {
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
  public synchronized void acquire(final Long id, final boolean read, final boolean write)
      throws InterruptedException {

    // add job id to queue and wait
    if(jobs >= parallel) {
      queue.add(id);

      // loop until job is placed first
      do {
        wait();
      } while(!id.equals(queue.peek()));

      // remove job from queue
      queue.remove(id);
    }
    jobs++;
  }

  @Override
  public synchronized String toString() {
    return "Jobs: " + jobs + ", queue: " + queue;
  }
}
