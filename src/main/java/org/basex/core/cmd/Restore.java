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
 * @author BaseX Team 2005-12, BSD License
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
    IOFile file = mprop.dbpath(db + IO.ZIPSUFFIX);
    if(!file.exists()) {
      final StringList list = Databases.backupPaths(db, context);
      if(!list.isEmpty()) file = new IOFile(list.get(0));
    } else {
      // db is already the name of a backup -> extract db name
      db = Pattern.compile(DateTime.PATTERN + '$').split(db)[0];
    }
    if(!file.exists()) return error(BACKUP_NOT_FOUND_X, db);

    // close database if it's currently opened and not opened by others
    if(!closed) closed = close(context, db);
    // check if database is still pinned
    if(context.pinned(db)) return error(DB_PINNED_X, db);

    // try to restore database
    return restore(file) && (!closed || new Open(db).run(context)) ?
        info(DB_RESTORED_X, file.name(), perf) : error(DB_NOT_RESTORED_X, db);
  }

  @Override
  public void databases(final LockResult lr) {
    super.databases(lr);
    final String name = args[0];
    // Not sure whether database or backup name is provided, lock both
    final String dbName = Pattern.compile(DateTime.PATTERN + '$').split(name)[0];
    lr.write.add(name).add(dbName);
  }

  /**
   * Restores the specified database.
   * @param file file
   * @return success flag
   */
  private boolean restore(final IOFile file) {
    try {
      proc(new Zip(file)).unzip(mprop.dbpath());
      return true;
    } catch(final IOException ex) {
      Util.debug(ex);
      return false;
    }
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
