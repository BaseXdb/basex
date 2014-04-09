package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'restore' command and restores a backup of a database.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class Restore extends ABackup {
  /** States if current database was closed. */
  private boolean closed;

  /**
   * Default constructor.
   * @param arg optional argument
   */
  public Restore(final String arg) {
    super(arg);
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    if(!Databases.validName(name)) return error(NAME_INVALID_X, name);

    // find backup with or without date suffix
    final StringList backups = context.databases.backups(name);
    if(backups.isEmpty()) return error(BACKUP_NOT_FOUND_X, name);

    final String backup = backups.get(0);
    final String db = Databases.name(backup);

    // close database if it's currently opened and not opened by others
    if(!closed) closed = close(context, db);
    // check if database is still pinned
    if(context.pinned(db)) return error(DB_PINNED_X, db);

    // try to restore database
    try {
      restore(db, backup, this, context);
      return !closed || new Open(db).run(context) ?
        info(DB_RESTORED_X, backup, perf) : error(DB_NOT_RESTORED_X, db);
    } catch(final IOException ex) {
      Util.debug(ex);
      return error(DB_NOT_RESTORED_X, db);
    }
  }

  @Override
  public void databases(final LockResult lr) {
    super.databases(lr);
    // Not sure whether database or name of backup file is provided: lock both
    final String backup = args[0];
    lr.write.add(backup).add(Databases.name(backup));
  }

  /**
   * Restores the specified database.
   * @param db name of database
   * @param backup name of backup
   * @param cmd calling command instance
   * @param context database context
   * @throws IOException  I/O exception
   */
  public static void restore(final String db, final String backup, final Restore cmd,
      final Context context) throws IOException {

    // drop target database
    DropDB.drop(db, context);

    final IOFile dbpath = context.globalopts.dbpath();
    final Zip zip = new Zip(new IOFile(dbpath, backup + IO.ZIPSUFFIX));
    if(cmd != null) cmd.proc(zip);
    zip.unzip(dbpath);
  }

  @Override
  protected String tit() {
    return RESTORE;
  }

  @Override
  public boolean newData(final Context ctx) {
    closed = close(ctx, args[0]);
    return closed;
  }

  @Override
  public boolean supportsProg() {
    return true;
  }
}
