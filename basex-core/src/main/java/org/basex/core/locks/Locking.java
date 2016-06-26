package org.basex.core.locks;

import org.basex.core.jobs.*;
import org.basex.util.list.*;

/**
 * Lock interface; will get obsolete after database locking has been finalized.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public interface Locking {
  /**
   * Puts read and write locks on the specified databases.
   * @param job job to be queued
   * @param read names of databases to put read locks on.
   * Global locking is performed if the passed on reference is {@code null}
   * @param write names of databases to put write locks on.
   * Global locking is performed if the passed on reference is {@code null}
   */
  void acquire(final Job job, final StringList read, final StringList write);

  /**
   * Unlock all string locked by a transaction.
   * @param job job to be unlocked
   */
  void release(final Job job);
}
