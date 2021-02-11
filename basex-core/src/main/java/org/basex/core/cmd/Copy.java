package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'copy' command and creates a copy of a database.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
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
    if(!Databases.validName(src)) return error(NAME_INVALID_X, src);
    if(!Databases.validName(trg)) return error(NAME_INVALID_X, trg);

    // source database does not exist
    if(!soptions.dbExists(src)) return error(DB_NOT_FOUND_X, src);
    // target database already exists
    if(soptions.dbExists(trg)) return error(DB_EXISTS_X, trg);

    // try to copy database
    try {
      copy(src, trg, soptions, this);
      return info(DB_COPIED_X, src, jc().performance);
    } catch(final IOException ex) {
      Util.debug(ex);
      return error(DB_NOT_COPIED_X, src);
    }
  }

  /**
   * Copies the specified database.
   * @param source name of the database
   * @param target new database name
   * @param sopts static options
   * @param cmd calling command (can be {@code null})
   * @throws IOException I/O exception
   */
  public static void copy(final String source, final String target, final StaticOptions sopts,
      final Copy cmd) throws IOException {

    // drop target database
    DropDB.drop(target, sopts);

    final IOFile src = sopts.dbPath(source), trg = sopts.dbPath(target);
    final StringList files = src.descendants();
    if(cmd != null) cmd.tf = files.size();

    // copy all files
    try {
      for(final String file : files) {
        new IOFile(src, file).copyTo(new IOFile(trg, file));
        if(cmd != null) cmd.of++;
      }
    } catch(final IOException ex) {
      // error: drop new database
      Util.debug(ex);
      DropDB.drop(target, sopts);
      throw ex;
    }
  }

  @Override
  public void addLocks() {
    final Locks locks = jc().locks;
    locks.reads.add(args[0]);
    locks.writes.add(args[1]);
  }

  @Override
  public String shortInfo() {
    return COPY;
  }

  @Override
  public boolean supportsProg() {
    return true;
  }

  @Override
  public double progressInfo() {
    return (double) of / tf;
  }
}
