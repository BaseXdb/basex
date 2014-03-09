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
    String name = args[0];
    final String db = dbName(name);
    if(!Databases.validName(db)) return error(NAME_INVALID_X, name);

    // find backup file with or without date suffix
    final IOFile file = backupFile(name, context);
    if(file == null) return error(BACKUP_NOT_FOUND_X, name);

    // close database if it's currently opened and not opened by others
    if(!closed) closed = close(context, db);
    // check if database is still pinned
    if(context.pinned(db)) return error(DB_PINNED_X, db);

    // try to restore database
    try {
      restore(db, file, this, context);
      return !closed || new Open(db).run(context) ?
        info(DB_RESTORED_X, file.name(), perf) : error(DB_NOT_RESTORED_X, db);
    } catch(final IOException ex) {
      return error(DB_NOT_RESTORED_X, db);
    }
  }

  /**
   * Extracts the name of a database from the name of a backup file. E.g. for the backup name
   * backup-2014-03-04-09-02-33, the method returns 'backup' as the database name.
   * @param backupName Name of the backup file. Valid formats:
   *                   {@code [dbname]-yyyy-mm-dd-hh-mm-ss},
   *                   {@code [dbname]}
   * @return name of the database ({@code [dbname]})
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
    final String n = name;

    // find backup file with date suffix
    final IOFile file = ctx.globalopts.dbpath(n + IO.ZIPSUFFIX);
    if(file.exists()) return file;

    // if only the database name is given, return the most recent backup
    final StringList list = Databases.backupPaths(n, ctx).sort(Prop.CASE, false);
    return !list.isEmpty() ? new IOFile(list.get(0)) : null;
  }

  @Override
  public void databases(final LockResult lr) {
    super.databases(lr);
    // Not sure whether database or name of backup file is provided: lock both
    final String name = args[0];
    lr.write.add(name).add(dbName(name));
  }

  /**
   * Restores the specified database.
   * @param db name of database
   * @param file file
   * @param cmd calling command instance
   * @param context database context
   * @throws IOException  I/O exception
   */
  public static void restore(final String db, final IOFile file, final Restore cmd,
      final Context context) throws IOException {

    // drop target database
    DropDB.drop(db, context);

    final Zip zip = new Zip(file);
    if(cmd != null) cmd.proc(zip);
    zip.unzip(context.globalopts.dbpath());
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
