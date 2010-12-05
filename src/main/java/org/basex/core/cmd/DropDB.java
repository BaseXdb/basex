package org.basex.core.cmd;

import static org.basex.core.Commands.*;
import static org.basex.core.Text.*;
import java.io.File;
import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;

/**
 * Evaluates the 'drop database' command and deletes a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
    // name of database
    final String db = args[0];
    if(!validName(db)) return error(NAMEINVALID, db);

    // database does not exist; return true
    if(!prop.dbexists(db)) return info(DBNOTDROPPED);

    // close database if it's currently opened and not opened by others
    close(db);

    // check if database is still pinned
    if(context.pinned(db)) return error(DBLOCKED, db);
    // try to drop database
    return drop(db, prop) ? info(DBDROPPED, db) : error(DBDROPERROR);
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

    final File path = pr.dbpath(db);
    final File[] files = path.listFiles();
    // path not found/no permissions...
    if(!path.exists() || files == null) return false;

    for(final File sub : files) {
      if(pat == null || sub.getName().matches(pat))
        if(!sub.delete()) return false;
    }
    return pat != null || path.delete();
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.DB).args();
  }
}
