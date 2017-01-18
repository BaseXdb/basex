package org.basex.core.locks;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.util.*;

/**
 * Read and write locks of a single job.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Jens Erat
 */
public class Locks {
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
    writes.finish(data);
    reads.finish(data);

    // remove read locks that are also defined as write locks
    reads.remove(writes);
  }

  @Override
  public String toString() {
    return new StringBuilder(Util.className(getClass())).append(": Read ").
        append(reads).append(", Write ").append(writes).append(']').toString();
  }
}
