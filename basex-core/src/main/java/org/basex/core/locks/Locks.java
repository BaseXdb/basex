package org.basex.core.locks;

import java.util.*;

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
   * Checks if these locks and the specified locks cannot be held by two jobs at the same time.
   * Both lock sets must have been finished (see {@link #finish(Context)}).
   * @param locks locks to compare with
   * @return result of check
   */
  public boolean conflicts(final Locks locks) {
    // a global write lock is exclusive and clashes with any lock held by the other job
    if(writes.global() && locks.locking() || locks.writes.global() && locking()) return true;
    // a global read lock clashes with the other job's local write locks
    if(reads.global() && locks.writes.local() || locks.reads.global() && writes.local())
      return true;
    // a write lock clashes with any equally-named lock held by the other job
    return writes.intersects(locks.writes) || writes.intersects(locks.reads) ||
        locks.writes.intersects(reads);
  }

  /**
   * Returns a readable, comma-separated list of all lock strings.
   * @return lock strings, including {@code (global)} for a global lock
   */
  public String labels() {
    final ArrayList<String> list = new ArrayList<>();
    for(final String lock : writes) list.add(lock);
    for(final String lock : reads) list.add(lock);
    if(writes.global() || reads.global()) list.add("(global)");
    return String.join(", ", list);
  }

  @Override
  public String toString() {
    return "Reads: " + reads + ", Writes: " + writes;
  }
}
