package org.basex.core;

import org.basex.util.list.*;

/**
 * Lock interface; will get obsolete in a later version.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public interface ILocking {

  /**
   * Puts read locks on an array of objects.
   *
   * Store and return the {@code token} for unlocking these objects again.
   * @param pr progress
   * @param read names of databases to put read locks on
   * @param write names of databases to put write locks on
   */
  void acquire(final Progress pr, final StringList read, final StringList write);

  /**
   * Unlock all objects a transaction locked.
   * @param pr progress
   */
  void release(final Progress pr);
}
