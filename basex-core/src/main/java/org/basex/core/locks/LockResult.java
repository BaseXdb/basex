package org.basex.core.locks;

import org.basex.core.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Result object for databases function.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Jens Erat
 * @see Proc#databases(LockResult)
 */
public class LockResult {
  /** Read locks. */
  public final StringList read = new StringList(1);
  /** Write locks. */
  public final StringList write = new StringList(1);
  /** Flag if global read lock is required. */
  public boolean readAll;
  /** Flag if global write lock is required. */
  public boolean writeAll;

  /**
   * Merge lock instances.
   * @param lr lock instance
   */
  public void union(final LockResult lr) {
    // if command writes to currently opened database, it may affect any database that has been
    // opened before. hence, assign write locks to all opened databases
    if(lr.write.contains(DBLocking.CONTEXT)) write.add(read);
    // merge local locks with global lock lists
    read.add(lr.read);
    write.add(lr.write);
    readAll |= lr.readAll;
    writeAll |= lr.writeAll;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Util.className(getClass())).append(": Read ");
    if(readAll) sb.append(" readall");
    sb.append(read).append(", Write ");
    if(writeAll) sb.append(" writeall");
    return sb.append(write).append(']').toString();
  }
}
