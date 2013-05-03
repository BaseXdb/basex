package org.basex.core;

import org.basex.util.list.*;

/**
 * Result object for databases function.
 * @see Proc#databases(LockResult)
 */
public class LockResult {
  /** Flag if global read lock is required. */
  public boolean readAll;
  /** Flag if global write lock is required. */
  public boolean writeAll;
  /** List of databases to read lock. */
  public StringList read = new StringList(1);
  /** List of databases to write lock. */
  public StringList write = new StringList(1);
}
