package org.basex.core.proc;

import static org.basex.Text.*;
import static org.basex.core.Commands.*;
import org.basex.core.Process;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.io.IO;

/**
 * Evaluates the 'drop database' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DropDB extends Process {
  /**
   * Constructor.
   * @param n name of database
   */
  public DropDB(final String n) {
    super(STANDARD, n);
  }

  @Override
  protected boolean exec() {
    final String db = args[0];
    final Data data = context.data();

    // close database if still open and not in main memory
    if(data != null && !(data instanceof MemData) && data.meta.name.equals(db))
      new Close().execute(context);

    // check if database is still pinned
    if(context.pinned(db)) return error(DBINUSE);

    // try to drop database
    return !IO.dbpath(db).exists() ? error(DBNOTFOUND, db) :
      drop(db) ? info(DBDROPPED, db) : error(DBNOTDROPPED);
  }

  /**
   * Deletes the specified database.
   * @param db database name
   * @return success flag
   */
  public static synchronized boolean drop(final String db) {
    return IO.dbdelete(db, null);
  }

  @Override
  public String toString() {
    return Cmd.DROP.name() + " " + CmdDrop.DB + args();
  }
}
