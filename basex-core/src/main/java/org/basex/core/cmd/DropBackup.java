package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'drop backup' command and deletes backups of a database.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class DropBackup extends ABackup {
  /**
   * Default constructor.
   * @param pattern database pattern with optional date  ({@code null} for general data)
   */
  public DropBackup(final String pattern) {
    super(pattern != null ? pattern : "");
  }

  @Override
  protected boolean run() {
    final String pattern = args[0];
    final boolean general = pattern.isEmpty();
    if(!(general || Databases.validPattern(pattern))) return error(NAME_INVALID_X, pattern);

    // loop through all databases and collect databases to be dropped
    final StringList names = general ? new StringList("") : context.listDBs(pattern);
    // if the given argument is not a database name, it could be the name of a backup file
    if(names.isEmpty() && context.perm(Perm.READ, pattern)) names.add(pattern);

    // drop all backups
    for(final String name : names) {
      for(final String backup : context.databases.backups(name)) {
        drop(backup, soptions);
      }
    }
    return info(BACKUP_DROPPED_X, pattern);
  }

  /**
   * Drops a backup with the specified name.
   * @param backup name of backup file
   * @param sopts static options
   * @return success flag
   */
  public static boolean drop(final String backup, final StaticOptions sopts) {
    return sopts.dbPath(backup + IO.ZIPSUFFIX).delete();
  }

  @Override
  public void addLocks() {
    addLocks(jc().locks.writes, 0);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.BACKUP).args();
  }
}
