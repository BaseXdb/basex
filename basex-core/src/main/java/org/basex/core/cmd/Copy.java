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
 * @author BaseX Team, BSD License
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
    super(Perm.ADMIN, db, newdb);
  }

  @Override
  public boolean updating(final Context ctx) {
    return true;
  }

  @Override
  protected boolean run() {
    final String src = args[0];
    final String trg = args[1];
    // check if names are valid
    if(!Databases.validName(src)) return error(NAME_INVALID_X, src);
    if(!Databases.validName(trg)) return error(NAME_INVALID_X, trg);
    // check local permissions
    if(!create(src, trg)) return error(PERM_REQUIRED_X, Perm.ADMIN);

    // source database does not exist
    if(!soptions.dbExists(src)) return error(DB_NOT_FOUND_X, src);
    // close target database if it's currently opened and not opened by others
    close(context, trg);
    if(context.pinned(trg)) return error(DB_PINNED_X, trg);

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

    // reject target that is the source
    final IOFile src = sopts.dbPath(source);
    if(src.sameFile(sopts.dbPath(target))) throw new BaseXException(DB_NOT_COPIED_X, source);
    final StringList files = src.descendants();
    if(cmd != null) cmd.tf = files.size();

    // copy all files to a temporary database, which then replaces the target
    final String tmpName = sopts.createTempDb(target);
    final IOFile tmp = sopts.dbPath(tmpName);
    try {
      for(final String file : files) {
        new IOFile(src, file).copyTo(new IOFile(tmp, file));
        if(cmd != null) cmd.of++;
      }
      if(!AlterDB.alter(tmpName, target, sopts)) throw new BaseXException(DB_NOT_COPIED_X, source);
    } catch(final IOException ex) {
      DropDB.drop(tmpName, sopts);
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
  public boolean newData(final Context ctx) {
    return close(ctx, args[1]);
  }

  @Override
  public String shortInfo() {
    return COPY + DOTS;
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
