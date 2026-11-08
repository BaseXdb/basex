package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'drop database' command and deletes a database.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DropDB extends ACreate {
  /**
   * Default constructor.
   * @param pattern database pattern
   */
  public DropDB(final String pattern) {
    super(pattern);
  }

  @Override
  protected boolean run() {
    final String pattern = args[0];
    if(!Databases.validPattern(pattern)) return error(NAME_INVALID_X, pattern);

    // close an opened main-memory database, which has no representation on disk
    final Data data = context.data();
    if(data != null && data.inMemory() && Databases.regex(pattern).matcher(data.meta.name).matches()
        && Close.close(context)) {
      return info(DB_DROPPED_X, data.meta.name);
    }

    // retrieve all databases; return true if no database is found (no error)
    final StringList dbs = context.databases.list(context.user(), Perm.ADMIN, pattern);
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
    data.close();
    return data.inMemory() || drop(data.meta.name, sopts);
  }

  /**
   * Deletes the specified database or general data.
   * @param db name of the database (empty string for general data)
   * @param sopts static options
   * @return success flag
   */
  public static synchronized boolean drop(final String db, final StaticOptions sopts) {
    if(db.isEmpty()) {
      final IOFile dbpath = sopts.dbPath(db);
      boolean ok = true;
      for(final String path : sopts.dbFiles(db)) ok &= new IOFile(dbpath, path).delete();
      return ok;
    }
    // move directory aside first: fails as a whole if a file is locked
    try {
      final IOFile tmp = moveAside(db, sopts);
      return tmp == null || tmp.delete();
    } catch(final IOException ex) {
      Util.debug(ex);
      return false;
    }
  }

  /**
   * Moves a database directory to a new temporary database directory.
   * @param db name of the database
   * @param sopts static options
   * @return temporary directory, or {@code null} if the database does not exist
   * @throws IOException I/O exception
   */
  static IOFile moveAside(final String db, final StaticOptions sopts) throws IOException {
    final IOFile dir = sopts.dbPath(db);
    if(!dir.exists()) return null;
    final IOFile tmp = sopts.dbPath(sopts.createTempDb(db));
    try {
      dir.moveTo(tmp, true);
      return tmp;
    } catch(final IOException ex) {
      tmp.delete();
      throw ex;
    }
  }

  /**
   * Drops files in a database directory with the specified pattern.
   * @param path database path
   * @param pattern file pattern
   * @return success of operation
   */
  public static synchronized boolean drop(final IOFile path, final String pattern) {
    boolean ok = true;
    for(final IOFile file : path.children()) {
      ok &= !file.name().matches(pattern) || file.delete();
    }
    return ok;
  }

  @Override
  public boolean newData(final Context ctx) {
    return close(ctx, args[0]);
  }

  @Override
  public void addLocks() {
    addLocks(jc().locks.writes, 0);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.DB).args();
  }
}
