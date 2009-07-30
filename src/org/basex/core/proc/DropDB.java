package org.basex.core.proc;

import static org.basex.Text.*;
import static org.basex.core.Commands.*;
import java.io.File;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.data.Data;

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

    // close database if still open
    if(data != null && data.meta.name.equals(db)) exec(new Close());

    // check if database is still pinned
    if(context.pinned(db)) return error(DBINUSE);

    // try to drop database
    return !prop.dbpath(db).exists() ? error(DBNOTFOUND, db) :
      drop(db, prop) ? info(DBDROPPED, db) : error(DBNOTDROPPED);
  }

  /**
   * Deletes the specified database.
   * @param db database name
   * @param pr database properties
   * @return success flag
   */
  public static boolean drop(final String db, final Prop pr) {
    return delete(db, null, pr);
  }

  /**
   * Recursively deletes a database directory.
   * @param db database to delete
   * @param pat file pattern
   * @param pr database properties
   * @return success of operation
   */
  static synchronized boolean delete(final String db,
      final String pat, final Prop pr) {

    final File path = pr.dbpath(db);
    if(!path.exists()) return false;

    boolean ok = true;
    for(final File sub : path.listFiles()) {
      if(pat == null || sub.getName().matches(pat)) ok &= sub.delete();
    }
    if(pat == null) ok &= path.delete();
    return ok;
  }

  @Override
  public String toString() {
    return Cmd.DROP.name() + " " + CmdDrop.DB + args();
  }
}
