package org.basex.core.locks;

import org.basex.core.*;
import org.basex.data.*;

/**
 * Read and write locks of a single job.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Locks {
  /** Read locks. */
  public final LockList reads = new LockList();
  /** Write locks. */
  public final LockList writes = new LockList();
  /** Locks of the nearest blocked caller that holds locks (can be {@code null}). */
  private Locks caller;
  /** Indicates that the job occupies a run slot in the lock queue. */
  boolean slot;

  /**
   * Finalizes locks. Replaces context references with current database, sorts entries,
   * removes duplicates, assigns global read lock if global write lock exists.
   * @param ctx database context
   * @return self reference
   */
  public Locks finish(final Context ctx) {
    // global write lock: no read locks required
    if(writes.global()) reads.reset();

    // resolve context references, sort, remove duplicates
    final Data data = ctx.data();
    final String name = data == null ? null : data.meta.name;
    writes.finish(name);
    reads.finish(name);

    // remove read locks that are also defined as write locks
    reads.remove(writes);
    return this;
  }

  /**
   * Indicates if any read or write lock exists.
   * @return result of check
   */
  public boolean locking() {
    return reads.locking() || writes.locking();
  }

  /**
   * Assigns the locks of a blocked caller that waits for the job.
   * @param locks locks of the caller (can be {@code null})
   */
  public void caller(final Locks locks) {
    // a caller with locking callers only reads what they read: its own locks add nothing
    if(locks != null) caller = locks.inherited() ? locks.caller : locks.locking() ? locks : null;
  }

  /**
   * Indicates if a blocked caller of the job holds locks.
   * @return result of check
   */
  public boolean inherited() {
    return caller != null;
  }

  /**
   * Checks if the specified locks are not covered by the read locks of a blocked caller.
   * @param locks locks to check
   * @return result of check
   */
  public boolean deadlocks(final Locks locks) {
    // the job must never wait for a lock: it may only read what its blocked caller reads,
    // but nothing that the caller writes
    if(!inherited() || !locks.locking()) return false;
    if(locks.writes.locking()) return true;
    final LockList held = caller.reads, written = caller.writes;
    if(locks.reads.global()) return !held.global() || written.locking();
    for(final String lock : locks.reads) {
      if(!held.global() && !held.contains(lock) || written.contains(lock)) return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return "Reads: " + reads + ", Writes: " + writes;
  }
}
