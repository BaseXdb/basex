package org.basex.core;

import org.basex.util.list.*;

/**
 * Result object for databases function.
 * @see Proc#databases(LockResult)
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Jens Erat
 */
public class LockResult {
  /** List of databases to read lock. */
  public final StringList read = new StringList(1);
  /** List of databases to write lock. */
  public final StringList write = new StringList(1);
  /** Flag if global read lock is required. */
  public boolean readAll;
  /** Flag if global write lock is required. */
  public boolean writeAll;

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("LockResult: ");
    sb.append("Read ");
    if(read == null) sb.append(" all? ").append(readAll);
    else sb.append(read);
    sb.append(", Write ");
    if(write == null) sb.append("all? ").append(writeAll);
    else sb.append(write);
    return sb.toString();
  }
}
