package org.basex.core.proc;

import static org.basex.core.Text.*;

import java.io.File;

import org.basex.core.Context;
import org.basex.core.Proc;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;

/**
 * Evaluates the 'alter database' command and alters the name of a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class AlterDB extends Proc {
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
    // DB is currently locked
    if(context.pinned(db)) return error(DBLOCKED, db);
    
    // try to alter database
    return !prop.dbexists(db) ? error(DBNOTFOUND, db) :
      alter(db, name) ? info(DBALTERED, db, name) : error(DBNOTALTERED, db);
  }
  
  /**
   * Alters the database name.
   * @param db database
   * @param name new name
   * @return success of operation
   */
  private boolean alter(final String db, final String name) {
    File f = prop.dbpath(db);
    if(!f.renameTo(new File(f.getParentFile(), name))) {
      return false;
    }
    return true;
  }
  
  @Override
  public boolean updating(final Context ctx) {
    return true;
  }

  @Override
  public String toString() {
    return Cmd.ALTER + " " + CmdCreate.DB + args();
  }
}
