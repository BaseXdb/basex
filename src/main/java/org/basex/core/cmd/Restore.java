package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.File;
import java.io.IOException;
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
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Restore extends Command {
  /** Date pattern. */
  private static final String PATTERN =
    "-\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}";
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
    if(!MetaData.validName(db, false)) return error(NAMEINVALID, db);

    // find backup file with or without date suffix
    File file = mprop.dbpath(db + IO.ZIPSUFFIX);
    if(!file.exists()) {
      final StringList list = list(db, context);
      if(list.size() != 0) file = new File(list.get(0));
    } else {
      db = db.replace(PATTERN + '$', "");
    }
    if(!file.exists()) return error(DBBACKNF, db);

    // close database if it's currently opened and not opened by others
    if(!closed) closed = close(context, db);
    // check if database is still pinned
    if(context.pinned(db)) return error(DBPINNED, db);

    // try to restore database
    return restore(file) && (!closed || new Open(db).run(context)) ?
        info(DBRESTORE, file.getName(), perf) : error(DBNORESTORE, db);
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

  /**
   * Returns all backups of the specified database.
   * @param db database
   * @param ctx database context
   * @return available backups
   */
  public static StringList list(final String db, final Context ctx) {
    final StringList list = new StringList();

    final IOFile dir = ctx.mprop.dbpath();
    if(!dir.exists()) return list;

    for(final IOFile f : dir.children()) {
      if(f.name().matches(db + PATTERN + IO.ZIPSUFFIX)) list.add(f.path());
    }
    list.sort(false, false);
    return list;
  }

  @Override
  protected String tit() {
    return BUTTONRESTORE;
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