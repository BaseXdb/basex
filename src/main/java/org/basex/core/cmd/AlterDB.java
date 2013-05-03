package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.data.*;

/**
 * Evaluates the 'alter database' command and renames a database.
 *
 * @author BaseX Team 2005-12, BSD License
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
    if(!MetaData.validName(src, false)) return error(NAME_INVALID_X, src);
    if(!MetaData.validName(trg, false)) return error(NAME_INVALID_X, trg);

    // database does not exist
    if(!mprop.dbexists(src)) return error(DB_NOT_FOUND_X, src);
    // target database already exists
    if(mprop.dbexists(trg)) return error(DB_EXISTS_X, trg);

    // close database if it's currently opened and not opened by others
    if(!closed) closed = close(context, src);
    // check if source database is still opened
    if(context.pinned(src)) return error(DB_PINNED_X, src);

    // try to alter database
    return alter(src, trg, context) && (!closed || new Open(trg).run(context)) ?
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
   * @param ctx database context
   * @return success flag
   */
  public static synchronized boolean alter(final String source, final String target,
      final Context ctx) {
    return ctx.mprop.dbpath(source).rename(ctx.mprop.dbpath(target));
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
