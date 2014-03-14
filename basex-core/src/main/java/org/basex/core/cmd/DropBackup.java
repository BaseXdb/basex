package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdDrop;
import org.basex.io.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'drop backup' command and deletes backups of a database.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class DropBackup extends ABackup {
  /**
   * Default constructor.
   * @param name name of database
   */
  public DropBackup(final String name) {
    super(name);
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    if(!Databases.validName(name, true)) return error(NAME_INVALID_X, name);

    // loop through all databases and collect databases to be dropped
    final StringList dbs = context.databases.listDBs(name);
    // if the given argument is not a database name, it could be the name of a backup file
    if(dbs.isEmpty()) dbs.add(name);

    // drop all backups
    for(final String db : dbs) {
      for(final String backup : context.databases.backups(db)) drop(backup, context);
    }

    return info(BACKUP_DROPPED_X, name + '*' + IO.ZIPSUFFIX);
  }

  /**
   * Drops a backup with the specified name.
   * @param name name of backup file
   * @param ctx database context
   * @return success flag
   */
  public static boolean drop(final String name, final Context ctx) {
    return new IOFile(ctx.globalopts.dbpath(), name + IO.ZIPSUFFIX).delete();
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.BACKUP).args();
  }
}
