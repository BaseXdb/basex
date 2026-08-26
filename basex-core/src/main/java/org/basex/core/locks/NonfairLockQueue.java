package org.basex.core.locks;

import java.util.*;

/**
 * Lock queue for non-fair locking.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class NonfairLockQueue extends LockQueue {
  /** Queued readers. */
  private final Queue<Long> readers = new ArrayDeque<>();
  /** Queued writers. */
  private final Queue<Long> writers = new ArrayDeque<>();

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
      // add job ID to queue and wait
      final Queue<Long> queue = write ? writers : readers;
      queue.add(id);

      // loop until job is placed first (prefer readers); an interrupt must not leave the ID behind
      try {
        do {
          wait();
        } while(jobs >= parallel || write && !readers.isEmpty() || !id.equals(queue.peek()));
      } finally {
        queue.remove(id);
      }
    }
    jobs++;
  }

  @Override
  public synchronized String toString() {
    return "Jobs: " + jobs + ", queued readers: " + readers + ", queued writers: " + writers;
  }
}
