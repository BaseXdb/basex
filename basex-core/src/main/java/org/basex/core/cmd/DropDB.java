package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'drop database' command and deletes a database.
 *
 * @author BaseX Team 2005-21, BSD License
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
    final String pattern = args[0];
    if(!Databases.validPattern(pattern)) return error(NAME_INVALID_X, pattern);

    // retrieve all databases; return true if no database is found (no error)
    final StringList dbs = context.listDBs(pattern);
    if(dbs.isEmpty()) return info(NO_DB_DROPPED);

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
   * Deletes the specified database. Calls for main-memory database instances are ignored.
   * @param data data reference
   * @param sopts static options
   * @return success flag
   */
  public static synchronized boolean drop(final Data data, final StaticOptions sopts) {
    if(data.inMemory()) return true;
    data.close();
    return drop(data.meta.name, sopts);
  }

  /**
   * Deletes the specified database.
   * @param db name of the database
   * @param sopts static options
   * @return success flag
   */
  public static synchronized boolean drop(final String db, final StaticOptions sopts) {
    final IOFile dbpath = sopts.dbPath(db);
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
  public void addLocks() {
    final Locks locks = jc().locks;
    if(!addLocks(locks.writes, 0)) locks.writes.addGlobal();
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.DB).args();
  }
}
