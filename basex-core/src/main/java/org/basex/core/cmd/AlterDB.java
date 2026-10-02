package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdAlter;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Evaluates the 'alter database' command and renames a database.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class AlterDB extends ACreate {
  /** Indicates if current database was closed. */
  private boolean closed;

  /**
   * Default constructor.
   * @param db database
   * @param name new name
   */
  public AlterDB(final String db, final String name) {
    super(db, name);
  }

  @Override
  protected boolean run() {
    final String src = args[0];
    final String trg = args[1];
    // check if names are valid
    if(!Databases.validName(src)) return error(NAME_INVALID_X, src);
    if(!Databases.validName(trg)) return error(NAME_INVALID_X, trg);

    // database does not exist
    if(!soptions.dbExists(src)) return error(DB_NOT_FOUND_X, src);

    // close database if it's currently opened and not opened by others
    if(!closed) closed = close(context, src);
    // check if source database is still opened
    if(context.pinned(src)) return error(DB_PINNED_X, src);

    // try to alter database
    return alter(src, trg, soptions) && (!closed || new Open(trg).run(context)) ?
        info(DB_RENAMED_X, src, trg) : error(DB_NOT_RENAMED_X, src);
  }

  @Override
  public void addLocks() {
    jc().locks.writes.add(args[0]).add(args[1]);
  }

  /**
   * Renames the specified database.
   * @param source name of the existing database
   * @param target new database name
   * @param sopts static options
   * @return success flag
   */
  public static synchronized boolean alter(final String source, final String target,
      final StaticOptions sopts) {

    // drop target database (unless it is the source, e.g. if only the case differs)
    final IOFile src = sopts.dbPath(source), trg = sopts.dbPath(target);
    if(!src.sameFile(trg)) DropDB.drop(target, sopts);
    try {
      src.moveTo(trg, false);
      return true;
    } catch(final IOException ex) {
      Util.debug(ex);
      return false;
    }
  }

  @Override
  public boolean newData(final Context ctx) {
    closed = close(ctx, args[0]);
    return closed;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.ALTER + " " + CmdAlter.DB).args();
  }
}
