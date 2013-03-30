package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'copy' command and creates a copy of a database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Andreas Weiler
 */
public final class Copy extends Command {
  /** Counter for outstanding files. */
  private int of;
  /** Counter of total files. */
  private int tf;

  /**
   * Default constructor.
   * @param db db name
   * @param newdb new db name
   */
  public Copy(final String db, final String newdb) {
    super(Perm.CREATE, db, newdb);
  }

  @Override
  protected boolean run() {
    final String src = args[0];
    final String trg = args[1];
    // check if names are valid
    if(!MetaData.validName(src, false)) return error(NAME_INVALID_X, src);
    if(!MetaData.validName(trg, false)) return error(NAME_INVALID_X, trg);

    // source database does not exist
    if(!mprop.dbexists(src)) return error(DB_NOT_FOUND_X, src);
    // target database already exists
    if(mprop.dbexists(trg)) return error(DB_EXISTS_X, trg);

    // try to copy database
    return copy(src, trg) ? info(DB_COPIED_X, src, perf) : error(DB_NOT_COPIED_X, src);
  }

  /**
   * Copies the specified database.
   * @param source name of the database
   * @param target new database name
   * @return success flag
   */
  private boolean copy(final String source, final String target) {
    final IOFile src = mprop.dbpath(source);
    final IOFile trg = mprop.dbpath(target);

    // return false if source cannot be opened, or target cannot be created
    final StringList files = src.descendants();
    tf = files.size();
    try {
      for(final String file : files) {
        if(Databases.FILES.matcher(file).matches()) {
          new IOFile(src, file).copyTo(new IOFile(trg, file));
        }
        of++;
      }
      return true;
    } catch(final IOException ex) {
      // drop new database if error occurred
      Util.debug(ex);
      DropDB.drop(target, context);
      return false;
    }
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(args[0]);
    lr.write.add(args[1]);
  }

  @Override
  protected String tit() {
    return COPY;
  }

  @Override
  public boolean supportsProg() {
    return true;
  }

  @Override
  protected double prog() {
    return (double) of / tf;
  }
}
