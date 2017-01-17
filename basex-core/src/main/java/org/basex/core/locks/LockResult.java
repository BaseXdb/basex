package org.basex.core.locks;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.util.*;

/**
 * Read and write locks.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Jens Erat
 */
public class LockResult {
  /** Read locks. */
  public final LockList reads = new LockList();
  /** Write locks. */
  public final LockList writes = new LockList();

  /**
   * Sequentially adds lock results to the given instance.
   * @param lr lock instance
   */
  public void add(final LockResult lr) {
    // if command writes to currently opened database, it may affect any database that has been
    // opened before. hence, assign write locks to all opened databases
    if(lr.writes.contains(Locking.CONTEXT)) writes.add(reads);

    // merge local locks with global lock lists
    reads.add(lr.reads);
    writes.add(lr.writes);
  }

  /**
   * Finalizes locks. Replaces context references with current database, sorts entries,
   * removes duplicates, assigns global read lock if global write lock exists.
   * @param ctx database context
   */
  public void finish(final Context ctx) {
    // global write lock leads to global read locks
    if(writes.global()) reads.addGlobal();
    final Data data = ctx.data();
    writes.finish(data);
    reads.finish(data);
  }

  @Override
  public String toString() {
    return new StringBuilder(Util.className(getClass())).append(": Read ").
        append(reads).append(", Write ").append(writes).append(']').toString();
  }
}
