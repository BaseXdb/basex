package org.basex.core.locks;

/**
 * Lock queue.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public abstract class LockQueue {
  /**
   * Queues the job until it can be started.
   * @param id job id
   * @param read read flag
   * @param write write flag
   * @throws InterruptedException interrupted exception
   */
  public abstract void wait(Long id, boolean read, boolean write) throws InterruptedException;
}
