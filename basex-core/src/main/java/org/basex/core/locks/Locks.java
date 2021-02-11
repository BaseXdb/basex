package org.basex.core.locks;

import org.basex.core.*;
import org.basex.data.*;

/**
 * Read and write locks of a single job.
 *
 * @author BaseX Team 2005-21, BSD License
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
   */
  public void finish(final Context ctx) {
    // global write lock: no read locks required
    if(writes.global()) reads.reset();

    // resolve context references, sort, remove duplicates
    final Data data = ctx.data();
    final String name = data == null ? null : data.meta.name;
    writes.finish(name);
    reads.finish(name);

    // remove read locks that are also defined as write locks
    reads.remove(writes);
  }

  @Override
  public String toString() {
    return "Reads: " + reads + ", Writes: " + writes;
  }
}
