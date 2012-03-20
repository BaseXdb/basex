package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdDrop;
import org.basex.core.Context;
import org.basex.data.*;
import org.basex.util.list.StringList;
import org.basex.io.IOFile;

/**
 * Evaluates the 'drop database' command and deletes a database.
 *
 * @author BaseX Team 2005-12, BSD License
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
    if(!MetaData.validName(args[0], true)) return error(NAME_INVALID_X, args[0]);

    // retrieve all databases; return true if no database is found (no error)
    final StringList dbs = context.databases().listDBs(args[0]);
    if(dbs.size() == 0) return info(NO_DB_DROPPED, args[0]);

    // loop through all databases
    boolean ok = true;
    for(final String db : dbs) {
      // close database if it's currently opened
      close(context, db);
      // check if database is still pinned
      if(context.pinned(db) || pinned(context, db)) {
        info(DB_PINNED_X, db);
        ok = false;
      } else if(!drop(db, context)) {
        // dropping was not successful
        info(DB_NOT_DROPPED_X, db);
        ok = false;
      } else {
        info(DB_DROPPED_X, db);
      }
    }
    return ok;
  }

  @Override
  public String pinned(final Context ctx) {
    return null;
  }

  /**
   * Deletes the specified database.
   * @param db name of the database
   * @param ctx database context
   * @return success flag
   */
  public static synchronized boolean drop(final String db, final Context ctx) {
    final IOFile dbpath = ctx.mprop.dbpath(db);
    return dbpath.exists() && drop(dbpath) && ctx.databases().delete(db);
  }

  /**
   * Drops a database directory.
   * @param path database path
   * @return success of operation
   */
  public static synchronized boolean drop(final IOFile path) {
    return path.exists() && path.delete();
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
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.DB).args();
  }
}
