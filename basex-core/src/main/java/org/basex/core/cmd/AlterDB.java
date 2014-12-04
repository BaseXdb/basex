package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdAlter;

/**
 * Evaluates the 'alter database' command and renames a database.
 *
 * @author BaseX Team 2005-14, BSD License
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
    if(!soptions.dbexists(src)) return error(DB_NOT_FOUND_X, src);
    // target database already exists
    if(soptions.dbexists(trg)) return error(DB_EXISTS_X, trg);

    // close database if it's currently opened and not opened by others
    if(!closed) closed = close(context, src);
    // check if source database is still opened
    if(context.pinned(src)) return error(DB_PINNED_X, src);

    // try to alter database
    return alter(src, trg, soptions) && (!closed || new Open(trg).run(context)) ?
        info(DB_RENAMED_X, src, trg) : error(DB_NOT_RENAMED_X, src);
  }

  @Override
  public void databases(final LockResult lr) {
    lr.write.add(args);
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

    // drop target database
    DropDB.drop(target, sopts);
    return sopts.dbpath(source).rename(sopts.dbpath(target));
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
