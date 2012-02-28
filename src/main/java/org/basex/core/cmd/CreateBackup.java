package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.util.Date;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.core.User;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.io.Zip;
import org.basex.util.Util;
import org.basex.util.list.StringList;

/**
 * Evaluates the 'backup' command and creates a backup of a database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CreateBackup extends Command {

  /**
   * Default constructor.
   * @param arg optional argument
   */
  public CreateBackup(final String arg) {
    super(User.CREATE, arg);
  }

  @Override
  protected boolean run() {
    if(!MetaData.validName(args[0], true))
      return error(NAME_INVALID_X, args[0]);

    // retrieve all databases
    final StringList dbs = context.databases().listDBs(args[0]);
    if(dbs.size() == 0) return error(DB_NOT_FOUND_X, args[0]);

    // loop through all databases
    boolean ok = true;
    for(final String db : dbs) {
      if(!mprop.dbpath(db).isDir()) continue;
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
   * @param db database name
   * @return success flag
   */
  private boolean backup(final String db) {
    try {
      final IOFile path = mprop.dbpath(db);
      final IOFile file = mprop.dbpath(db + '-' +
      IO.DATE.format(new Date()) + IO.ZIPSUFFIX);

      final Zip zip = progress(new Zip(file));
      zip.zip(path);
      context.databases().add(db, true);
      return true;
    } catch(final IOException ex) {
      Util.debug(ex);
      return false;
    }
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
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.BACKUP).args();
  }
}
