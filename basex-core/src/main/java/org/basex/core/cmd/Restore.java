package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.zip.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'restore' command and restores a backup of a database.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class Restore extends ABackup {
  /** Total files in a zip operation. */
  private int total;
  /** Current file in a zip operation. */
  private int curr;
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
      restore(db, backup, soptions, this);
      return !closed || new Open(db).run(context) ? info(DB_RESTORED_X, backup, jc().performance) :
        error(DB_NOT_RESTORED_X, db);
    } catch(final IOException ex) {
      Util.debug(ex);
      return error(DB_NOT_RESTORED_X, db);
    }
  }

  @Override
  public void addLocks() {
    super.addLocks();
    // Not sure whether database or name of backup file is provided: lock both
    final String backup = args[0];
    jc().locks.writes.add(backup).add(Databases.name(backup));
  }

  /**
   * Restores the specified database.
   * @param db name of database
   * @param name name of backup
   * @param sopts static options
   * @param cmd calling command instance (can be {@code null})
   * @throws IOException I/O exception
   */
  public static void restore(final String db, final String name, final StaticOptions sopts,
      final Restore cmd) throws IOException {

    // drop target database
    DropDB.drop(db, sopts);

    final IOFile dbPath = sopts.dbPath();
    final IOFile backup = new IOFile(dbPath, name + IO.ZIPSUFFIX);
    if(cmd != null) {
      try(ZipFile zip = new ZipFile(backup.file())) {
        cmd.total = zip.size();
      }
    }
    try(InputStream is = backup.inputStream(); ZipInputStream in = new ZipInputStream(is)) {
      for(ZipEntry ze; (ze = in.getNextEntry()) != null;) {
        final IOFile trg = new IOFile(dbPath, ze.getName());
        if(ze.isDirectory()) {
          trg.md();
        } else {
          trg.parent().md();
          trg.write(in);
        }
        if(cmd != null) cmd.curr++;
      }
    }
  }

  @Override
  public String shortInfo() {
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

  @Override
  public double progressInfo() {
    return (double) curr / total;
  }
}
