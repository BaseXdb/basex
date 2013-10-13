package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdCreate;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'backup' command and creates a backup of a database.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class CreateBackup extends ABackup {

  /**
   * Default constructor.
   * @param arg optional argument
   */
  public CreateBackup(final String arg) {
    super(arg);
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    if(!Databases.validName(name, true)) return error(NAME_INVALID_X, name);

    // retrieve all databases
    final StringList dbs = context.databases.listDBs(name);
    if(dbs.isEmpty()) return error(DB_NOT_FOUND_X, name);

    // loop through all databases
    boolean ok = true;
    for(final String db : dbs) {
      if(!goptions.dbpath(db).isDir()) continue;
      if(backup(db)) {
        // backup was successful
        info(DB_BACKUP_X, db, perf);
      } else {
        info(DB_NOT_BACKUP_X, db);
        ok = false;
      }
    }
    return ok;
  }

  /**
   * Backups the specified database.
   * @param db name of the database
   * @return success flag
   */
  private boolean backup(final String db) {
    final String backup = db + '-' + DateTime.format(new Date(), DateTime.DATETIME) +
        IO.ZIPSUFFIX;
    final IOFile zf = goptions.dbpath(backup);
    final Zip zip = proc(new Zip(zf));

    try {
      final IOFile path = goptions.dbpath(db);
      zip.zip(path, path.descendants());
      return true;
    } catch(final IOException ex) {
      Util.debug(ex);
      return false;
    }
  }

  @Override
  public void databases(final LockResult lr) {
    super.databases(lr);
    databases(lr.read, 0);
  }

  @Override
  protected String tit() {
    return BACKUP;
  }

  @Override
  public boolean supportsProg() {
    return true;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.BACKUP).args();
  }
}
