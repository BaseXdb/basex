package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.util.regex.Pattern;

import org.basex.core.*;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.io.Zip;
import org.basex.util.Util;
import org.basex.util.list.StringList;

/**
 * Evaluates the 'restore' command and restores a backup of a database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Restore extends Command {
  /** States if current database was closed. */
  private boolean closed;

  /**
   * Default constructor.
   * @param arg optional argument
   */
  public Restore(final String arg) {
    super(User.CREATE, arg);
  }

  @Override
  protected boolean run() {
    String db = args[0];
    if(!MetaData.validName(db, false)) return error(NAME_INVALID_X, db);

    // find backup file with or without date suffix
    IOFile file = mprop.dbpath(db + IO.ZIPSUFFIX);
    if(!file.exists()) {
      final StringList list = Databases.backupPaths(db, context);
      if(list.size() != 0) file = new IOFile(list.get(0));
    } else {
      // db is already the name of a backup -> extract db name
      final Pattern pa = Pattern.compile(IO.DATEPATTERN + '$');
      db = pa.split(db)[0];
    }
    if(!file.exists()) return error(BACKUP_NOT_FOUND_X, db);

    // close database if it's currently opened and not opened by others
    if(!closed) closed = close(context, db);
    // check if database is still pinned
    if(context.pinned(db)) return error(DB_PINNED_X, db);

    // try to restore database
    return restore(file, db) && (!closed || new Open(db).run(context)) ?
        info(DB_RESTORED_X, file.name(), perf) :
          error(DB_NOT_RESTORED_X, db);
  }

  /**
   * Restores the specified database.
   * @param file file
   * @param db database name
   * @return success flag
   */
  private boolean restore(final IOFile file, final String db) {
    try {
      progress(new Zip(file)).unzip(mprop.dbpath());
      context.databases().add(db);
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