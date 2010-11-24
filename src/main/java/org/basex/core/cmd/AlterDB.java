package org.basex.core.cmd;

import static org.basex.core.Text.*;
import org.basex.core.CommandBuilder;
import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdAlter;

/**
 * Evaluates the 'alter database' command and renames a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class AlterDB extends Command {
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
    if(!checkName(db)) return error(NAMEINVALID, db);
    if(!checkName(name)) return error(NAMEINVALID, name);

    // DB does not exist
    if(!prop.dbexists(db)) return error(DBNOTFOUND, db);
    // Target DB exists already
    if(prop.dbexists(name)) return error(DBEXISTS, name);
    
    // close database if it's currently opened
    final boolean close = context.data != null &&
      db.equals(context.data.meta.name);
    if(close) new Close().run(context);
    
    // DB is currently locked
    if(context.pinned(db)) return error(DBLOCKED, db);

    // try to alter database
    return alter(db, name, prop) && (!close || new Open(name).run(context))
    ? info(DBALTERED, db, name) : error(DBNOTALTERED, db);
  }

  /**
   * Renames the specified database.
   * @param db database name
   * @param dbnew new database name
   * @param pr database properties
   * @return success flag
   */
  public static synchronized boolean alter(final String db,
      final String dbnew, final Prop pr) {
    return pr.dbpath(db).renameTo(pr.dbpath(dbnew));
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.ALTER + " " + CmdAlter.DB).args();
  }
}
