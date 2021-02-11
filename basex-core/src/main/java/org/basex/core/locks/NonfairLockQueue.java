package org.basex.core.locks;

import java.util.*;

/**
 * Lock queue for non-fair locking.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class NonfairLockQueue extends LockQueue {
  /** Queued readers. */
  private final Queue<Long> readers = new LinkedList<>();
  /** Queued writers. */
  private final Queue<Long> writers = new LinkedList<>();

  /**
   * Constructor.
   * @param parallel parallel jobs
   */
  NonfairLockQueue(final int parallel) {
    super(parallel);
  }

  @Override
  public synchronized void acquire(final Long id, final boolean read, final boolean write)
      throws InterruptedException {

    // only wait if job is locking
    if(jobs >= parallel && (read || write)) {
      // add job id to queue and wait
      final Queue<Long> queue = write ? writers : readers;
      queue.add(id);

      // loop until job is placed first (prefer readers)
      do {
        wait();
      } while(jobs >= parallel || write && !readers.isEmpty() || !id.equals(queue.peek()));

      // remove job from queue
      queue.remove(id);
    }
    jobs++;
  }

  @Override
  public synchronized String toString() {
    return "Jobs: " + jobs + ", queued readers: " + readers + ", queued writers: " + writers;
  }
}
