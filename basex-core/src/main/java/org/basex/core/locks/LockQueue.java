package org.basex.core.locks;

/**
 * Lock queue.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class LockQueue {
  /** Maximum number of parallel jobs. */
  protected final int parallel;
  /** Number of currently running jobs. */
  protected int jobs;

  /**
   * Constructor.
   * @param parallel parallel jobs
   */
  LockQueue(final int parallel) {
    this.parallel = parallel;
  }

  /**
   * Queues the job until it can be started.
   * @param id job id
   * @param read read flag
   * @param write write flag
   * @throws InterruptedException interrupted exception
   */
  abstract void acquire(Long id, boolean read, boolean write) throws InterruptedException;

  /**
   * Notifies other jobs that a job has been completed.
   */
  synchronized void release() {
    notifyAll();
    jobs--;
  }
}
