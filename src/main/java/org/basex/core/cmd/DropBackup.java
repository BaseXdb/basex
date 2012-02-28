package org.basex.core.cmd;

import static org.basex.core.Commands.*;
import static org.basex.core.Text.*;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.Context;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.util.list.StringList;

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
    super(User.CREATE, name);
  }

  @Override
  protected boolean run() {
    if(!MetaData.validName(args[0], true))
      return error(NAME_INVALID_X, args[0]);

    final StringList dbs = context.getDatabases().listDBs(args[0]);
    // loop through all databases and drop backups
    for(final String db : dbs) {
      drop(db.contains("-") ? db : db + '-', context);
    }
    // if the given argument is not a database name, it could be the name
    // of a backup file
    if(dbs.size() == 0) drop(args[0], context);

    return info(BACKUP_DROPPED_X, args[0] + '*' + IO.ZIPSUFFIX);
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
      if(n.startsWith(db) && n.endsWith(IO.ZIPSUFFIX)) {
        if(f.delete()) {
          c++;
          ctx.getDatabases().delete(
              db.charAt(db.length() - 1) == '-' ? db.substring(0,
                  db.length() - 1) : db, true);
        }
      }
    }
    return c;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.BACKUP).args();
  }
}
