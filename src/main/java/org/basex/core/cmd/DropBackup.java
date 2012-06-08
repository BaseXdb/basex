package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'drop backup' command and deletes backups of a database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DropBackup extends Command {
  /**
   * Default constructor.
   * @param name name of database
   */
  public DropBackup(final String name) {
    super(Perm.CREATE, name);
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    if(!MetaData.validName(name, true)) return error(NAME_INVALID_X, name);

    // retrieve all databases
    final StringList dbs = context.databases().listDBs(name);
    // loop through all databases and drop backups
    for(final String db : dbs) drop(db.contains("-") ? db : db + '-', context);

    // if the given argument is not a database name, it could be the name
    // of a backup file
    if(dbs.size() == 0) drop(name, context);

    return info(BACKUP_DROPPED_X, name + '*' + IO.ZIPSUFFIX);
  }

  /**
   * Drops one or more backups of the specified database.
   * @param db database
   * @param ctx database context
   * @return number of dropped backups
   */
  private static int drop(final String db, final Context ctx) {
    final IOFile dir = ctx.mprop.dbpath();
    int c = 0;
    for(final IOFile f : dir.children()) {
      final String n = f.name();
      if(n.startsWith(db) && n.endsWith(IO.ZIPSUFFIX) && f.delete()) {
        c++;
        final int dl = db.length() - 1;
        ctx.databases().delete(db.charAt(dl) == '-' ? db.substring(0, dl) : db, true);
      }
    }
    return c;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.BACKUP).args();
  }
}
