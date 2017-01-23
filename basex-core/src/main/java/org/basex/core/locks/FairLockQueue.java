package org.basex.core.locks;

import java.util.*;

/**
 * Lock queue for fair locking.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public class FairLockQueue extends LockQueue {
  /** Queue. */
  private final Queue<Long> queue = new LinkedList<>();

  @Override
  public void wait(final Long id, final boolean read, final boolean write)
      throws InterruptedException {

    // add job id to queue and wait
    queue.add(id);

    // loop until job is placed first
    do wait(); while(!id.equals(queue.peek()));

    // remove job from queue
    queue.remove(id);
  }

  @Override
  public String toString() {
    return "Queue: " + queue;
  }
}
