package org.basex.core;

import org.basex.util.list.*;

/**
 * Lock interface; will get obsolete after database locking has been finalized.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public interface Locking {
  /**
   * Puts read and write locks the specified databases.
   * Store and return the {@code token} for unlocking these objects again.
   *
   * @param pr process
   * @param read names of databases to put read locks on.
   *   Global locking is performed if the passed on reference is {@code null}
   * @param write names of databases to put write locks on.
   *   Global locking is performed if the passed on reference is {@code null}
   */
  void acquire(final Proc pr, final StringList read, final StringList write);

  /**
   * Unlock all string locked by a transaction.
   * @param write write locks to keep
   */
  void downgrade(final StringList write);

  /**
   * Unlock all string locked by a transaction.
   * @param pr process
   */
  void release(final Proc pr);
}
