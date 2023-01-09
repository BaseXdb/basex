package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.zip.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'restore' command and restores a database.
 *
 * @author BaseX Team 2005-23, BSD License
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
   * @param name name of backup with optional date ({@code null} for general data)
   */
  public Restore(final String name) {
    super(name != null ? name : "");
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    if(!(name.isEmpty() || Databases.validName(name))) return error(NAME_INVALID_X, name);

    // find backup with or without date suffix
    final StringList backups = context.databases.backups(name);
    if(backups.isEmpty()) return error(BACKUP_NOT_FOUND_X, name);

    final String backup = backups.get(0), db = Databases.name(backup);
    if(!db.isEmpty()) {
      // close database if it's currently opened and not opened by others
      if(!closed) closed = close(context, db);
      // check if database is still pinned
      if(context.pinned(db)) return error(DB_PINNED_X, db);
    }

    // try to restore database
    try {
      restore(db, backup, soptions, this);
      return db.isEmpty() || !closed || new Open(db).run(context) ?
        info(DB_RESTORED_X, backup, jc().performance) : error(DB_NOT_RESTORED_X, db);
    } catch(final IOException ex) {
      Util.debug(ex);
      return error(DB_NOT_RESTORED_X, db);
    }
  }

  /**
   * Restores the specified database .
   * @param db name of database (empty string for general data)
   * @param backup name of backup
   * @param sopts static options
   * @param cmd calling command instance (can be {@code null})
   * @throws IOException I/O exception
   */
  public static void restore(final String db, final String backup, final StaticOptions sopts,
      final Restore cmd) throws IOException {

    // drop existing files
    DropDB.drop(db, sopts);
    // unzip backup
    final IOFile dbPath = sopts.dbPath(), file = new IOFile(dbPath, backup + IO.ZIPSUFFIX);
    if(cmd != null) {
      try(ZipFile zip = new ZipFile(file.file())) {
        cmd.total = zip.size();
      }
    }
    try(InputStream is = file.inputStream(); ZipInputStream in = new ZipInputStream(is)) {
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
  public void addLocks() {
    final LockList list = jc().locks.writes;
    final String name = args[0], db = Databases.name(name);
    if(db.isEmpty()) {
      list.addGlobal();
    } else {
      // not sure whether database or name of backup file is provided: lock both
      list.add(name).add(db);
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
