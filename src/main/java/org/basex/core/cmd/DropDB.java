package org.basex.core.cmd;

import static org.basex.core.Commands.*;
import static org.basex.core.Text.*;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.io.IOFile;

/**
 * Evaluates the 'drop database' command and deletes a database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DropDB extends Command {
  /**
   * Default constructor.
   * @param name name of database
   */
  public DropDB(final String name) {
    super(User.CREATE, name);
  }

  @Override
  protected boolean run() {
    if(!validName(args[0], true)) return error(NAMEINVALID, args[0]);

    // retrieve all databases; return true if no database is found (no error)
    final String[] dbs = databases(args[0]);
    if(dbs.length == 0) return info(DBNOTDROPPED, args[0]);

    // loop through all databases
    boolean ok = true;
    for(final String db : dbs) {
      // close database if it's currently opened
      close(context, db);
      // check if database is still pinned
      if(context.pinned(db)) {
        info(DBLOCKED, db);
        ok = false;
      } else if(!drop(db, prop)) {
        // dropping was not successful
        info(DBDROPERROR, db);
        ok = false;
      } else {
        info(DBDROPPED, db);
      }
    }
    return ok;
  }

  /**
   * Deletes the specified database.
   * @param db database name
   * @param pr database properties
   * @return success flag
   */
  public static synchronized boolean drop(final String db, final Prop pr) {
    return drop(db, null, pr);
  }

  /**
   * Drops a database directory.
   * @param db database to delete
   * @param pat file pattern
   * @param pr database properties
   * @return success of operation
   */
  public static synchronized boolean drop(final String db,
      final String pat, final Prop pr) {

    final IOFile path = new IOFile(pr.dbpath(db));
    boolean ok = path.exists();
    // try to delete all files
    for(final IOFile sub : path.children()) {
      if(pat == null || sub.name().matches(pat)) ok &= sub.delete();
    }
    // only delete directory if no pattern was specified
    return (pat != null || path.delete()) && ok;
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
