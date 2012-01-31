package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.User;
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
    File file = mprop.dbpath(db + IO.ZIPSUFFIX);
    if(!file.exists()) {
      final StringList list = ShowBackups.list(db, true, context);
      if(list.size() != 0) file = new File(list.get(0));
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
    return restore(file) && (!closed || new Open(db).run(context)) ?
        info(DB_RESTORED_X, file.getName(), perf) :
          error(DB_NOT_RESTORED_X, db);
  }

  /**
   * Restores the specified database.
   * @param file file
   * @return success flag
   */
  private boolean restore(final File file) {
    try {
      progress(new Zip(new IOFile(file))).unzip(mprop.dbpath());
      return true;
    } catch(final IOException ex) {
      Util.debug(ex);
      return false;
    }
  }

  @Override
  protected String tit() {
    return RESTORE_D;
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