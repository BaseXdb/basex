package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdDrop;
import org.basex.io.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'drop database' command and deletes a database.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DropDB extends ACreate {
  /**
   * Default constructor.
   * @param name name of database
   */
  public DropDB(final String name) {
    super(name);
  }

  @Override
  protected boolean run() {
    if(!Databases.validName(args[0], true)) return error(NAME_INVALID_X, args[0]);

    // retrieve all databases; return true if no database is found (no error)
    final StringList dbs = context.databases.listDBs(args[0]);
    if(dbs.isEmpty()) return info(NO_DB_DROPPED, args[0]);

    // loop through all databases
    boolean ok = true;
    for(final String db : dbs) {
      // close database if it's currently opened
      close(context, db);
      // check if database is still pinned
      if(context.pinned(db)) {
        info(DB_PINNED_X, db);
        ok = false;
      } else if(!drop(db, soptions)) {
        // dropping was not successful
        info(DB_NOT_DROPPED_X, db);
        ok = false;
      } else {
        info(DB_DROPPED_X, db);
      }
    }
    return ok;
  }

  /**
   * Deletes the specified database.
   * @param db name of the database
   * @param sopts static options
   * @return success flag
   */
  public static synchronized boolean drop(final String db, final StaticOptions sopts) {
    final IOFile dbpath = sopts.dbpath(db);
    return dbpath.exists() && dbpath.delete();
  }

  /**
   * Recursively drops files in database directory with the specified pattern.
   * @param path database path
   * @param pat file pattern
   * @return success of operation
   */
  public static synchronized boolean drop(final IOFile path, final String pat) {
    boolean ok = true;
    for(final IOFile f : path.children()) ok &= !f.name().matches(pat) || f.delete();
    return ok;
  }

  @Override
  public boolean newData(final Context ctx) {
    return close(ctx, args[0]);
  }

  @Override
  public void databases(final LockResult lr) {
    if(!databases(lr.write, 0)) lr.writeAll = true;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.DB).args();
  }
}
