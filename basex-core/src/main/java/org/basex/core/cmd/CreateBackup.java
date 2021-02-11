package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdCreate;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'backup' command and creates a backup of a database.
 *
 * @author BaseX Team 2005-21, BSD License
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
    final String pattern = args[0];
    if(!Databases.validPattern(pattern)) return error(NAME_INVALID_X, pattern);

    // retrieve all databases
    final StringList dbs = context.listDBs(pattern);
    if(dbs.isEmpty()) return error(DB_NOT_FOUND_X, pattern);

    // loop through all databases
    boolean ok = true;
    for(final String db : dbs) {
      // don't open databases marked as updating
      if(MetaData.file(soptions.dbPath(db), DATAUPD).exists()) {
        // reject backups of databases that are currently being updated (or corrupt)
        info(DB_UPDATED_X, db);
        ok = false;
      } else {
        try {
          backup(db, soptions, this);
          // backup was successful
          info(DB_BACKUP_X, db, jc().performance);
        } catch(final IOException ex) {
          Util.debug(ex);
          info(DB_NOT_BACKUP_X, db);
          ok = false;
        }
      }
    }
    return ok;
  }

  /**
   * Backups the specified database.
   * @param db name of the database
   * @param sopts static options
   * @param cmd calling command instance
   * @throws IOException I/O Exception
   */
  public static void backup(final String db, final StaticOptions sopts, final CreateBackup cmd)
      throws IOException {

    final String backup = db + '-' + DateTime.format(new Date(), DateTime.DATETIME) + IO.ZIPSUFFIX;
    final IOFile zf = sopts.dbPath(backup);
    final Zip zip = new Zip(zf);

    try {
      if(cmd != null) cmd.pushJob(zip);
      final IOFile dbpath = sopts.dbPath(db);
      final StringList files = dbpath.descendants();
      // ignore file indicating an update (this file is generated when using XQuery)
      files.removeAll(DATAUPD + IO.BASEXSUFFIX);
      zip.zip(dbpath, files);
    } finally {
      if(cmd != null) cmd.popJob();
    }
  }

  @Override
  public void addLocks() {
    super.addLocks();
    addLocks(jc().locks.reads, 0);
  }

  @Override
  public String shortInfo() {
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
