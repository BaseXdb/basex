package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.MainProp;
import org.basex.core.CommandBuilder;
import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdAlter;
import org.basex.data.MetaData;

/**
 * Evaluates the 'alter database' command and renames a database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class AlterDB extends Command {
  /** States if current database was closed. */
  private boolean closed;

  /**
   * Default constructor.
   * @param db database
   * @param name new name
   */
  public AlterDB(final String db, final String name) {
    super(User.CREATE, db, name);
  }

  @Override
  protected boolean run() {
    final String db = args[0];
    final String name = args[1];
    // check if names are valid
    if(!MetaData.validName(db, false)) return error(NAMEINVALID, db);
    if(!MetaData.validName(name, false)) return error(NAMEINVALID, name);

    // database does not exist
    if(!mprop.dbexists(db)) return error(DBNOTFOUND, db);
    // target database exists already
    if(mprop.dbexists(name)) return error(DBEXIST, name);

    // close database if it's currently opened and not opened by others
    if(!closed) closed = close(context, db);
    // check if database is still pinned
    if(context.pinned(db)) return error(DBLOCKED, db);

    // try to alter database
    return alter(db, name, mprop) && (!closed || new Open(name).run(context)) ?
      info(DBALTERED, db, name) : error(DBNOTALTERED, db);
  }

  /**
   * Renames the specified database.
   * @param db database name
   * @param dbnew new database name
   * @param pr database properties
   * @return success flag
   */
  public static synchronized boolean alter(final String db,
      final String dbnew, final MainProp pr) {
    return pr.dbpath(db).renameTo(pr.dbpath(dbnew));
  }

  @Override
  public boolean newData(final Context ctx) {
    closed = close(ctx, args[0]);
    return closed;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.ALTER + " " + CmdAlter.DB).args();
  }
}
