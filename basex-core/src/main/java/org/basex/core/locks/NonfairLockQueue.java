package org.basex.core.locks;

import java.util.*;

/**
 * Lock queue for non-fair locking.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public class NonfairLockQueue extends LockQueue {
  /** Queued readers. */
  private final Queue<Long> readers = new LinkedList<>();
  /** Queued writers. */
  private final Queue<Long> writers = new LinkedList<>();

  @Override
  public void wait(final Long id, final boolean read, final boolean write)
      throws InterruptedException {

    // no locks: don't wait
    if(!read && !write) return;

    // add job id to queue and wait
    final Queue<Long> queue = write ? writers : readers;
    queue.add(id);

    // loop until job is placed first (prefer readers)
    do wait(); while(write && !readers.isEmpty() || id != queue.peek());

    // remove job from queue
    queue.remove(id);
  }

  @Override
  public synchronized String toString() {
    return "Queued readers: " + readers + ", queued writers: " + writers;
  }
}
