package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.regex.*;

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
    String db = args[0];
    if(!Databases.validName(db)) return error(NAME_INVALID_X, db);

    // find backup file with or without date suffix
    IOFile file = backupFile(db, context);
    // db is already the name of a backup -> extract db name
    if(file != null)
      db = dbName(db);
    else
      return error(BACKUP_NOT_FOUND_X, db);

    // close database if it's currently opened and not opened by others
    if(!closed) closed = close(context, db);
    // check if database is still pinned
    if(context.pinned(db)) return error(DB_PINNED_X, db);

    // try to restore database
    try {
      restore(file, this, goptions);
      return !closed || new Open(db).run(context) ?
        info(DB_RESTORED_X, file.name(), perf) : error(DB_NOT_RESTORED_X, db);
    } catch(final IOException ex) {
      return error(DB_NOT_RESTORED_X, db);
    }
  }

  /**
   * Extracts the name of a database from the name of a backup file. E.g. for the backup name
   * backup-2014-03-04-09-02-33, the method returns 'backup' as the database name.
   * @param backupName Name of the backup file.
   *                   Valid formats:
   *                    <dbname>-yyyy-mm-dd-hh-mm-ss
   *                    <dbname>
   * @return name of the database (<dbname>)
   */
  public static String dbName(final String backupName) {
    return Pattern.compile(DateTime.PATTERN + '$').split(backupName)[0];
  }

  /**
   * Finds either a specific backup file if the complete name is given or the latest backup file
   * otherwise.
   * @param name name of backup file or database name
   * @param ctx context
   * @return IOFile backup file, or {@code null} if there is no backup for the given database
   */
  public static IOFile backupFile(final String name, final Context ctx) {
    String n = name;

    // find backup file with date suffix
    IOFile file = ctx.globalopts.dbpath(n + IO.ZIPSUFFIX);
    if(file.exists())
      return file;

    // in case only the database name is given, find the latest backup
    final StringList list = Databases.backupPaths(n, ctx).sort(Prop.CASE, false);
    return !list.isEmpty() ? new IOFile(list.get(0)) : null;
  }

  @Override
  public void databases(final LockResult lr) {
    super.databases(lr);
    final String name = args[0];
    // Not sure whether database or backup name is provided, lock both
    lr.write.add(name).add(dbName(name));
  }

  /**
   * Restores the specified database.
   * @param file          file
   * @param cmd calling   command instance
   * @param glblOptions   global options
   * @throws IOException  I/O exception
   */
  public static void restore(final IOFile file, final Restore cmd, final GlobalOptions glblOptions)
      throws IOException {
    final Zip zip = new Zip(file);
    if(cmd != null) cmd.proc(zip);
    zip.unzip(glblOptions.dbpath());
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
