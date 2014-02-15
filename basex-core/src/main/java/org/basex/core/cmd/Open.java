package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.util.*;

/**
 * Evaluates the 'open' command and opens a database.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Open extends Command {
  /**
   * Default constructor.
   * @param name name of database
   */
  public Open(final String name) {
    super(Perm.NONE, name);
  }

  @Override
  protected boolean run() {
    // close existing database
    new Close().run(context);

    final String db = args[0];
    if(!Databases.validName(db)) return error(NAME_INVALID_X, db);

    try {
      final Data data = open(db, context);
      context.openDB(data);
      if(data.meta.oldindex()) info(H_INDEX_FORMAT);
      if(data.meta.corrupt)  info(DB_CORRUPT);
      return info(DB_OPENED_X, db, perf);
    } catch(final IOException ex) {
      return error(Util.message(ex));
    }
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.CTX).add(args[0]);
  }

  @Override
  public boolean newData(final Context ctx) {
    return new Close().run(ctx);
  }

  /**
   * Opens the specified database.
   * @param name name of database
   * @param ctx database context
   * @return data reference
   * @throws IOException I/O exception
   */
  public static Data open(final String name, final Context ctx) throws IOException {
    synchronized(ctx.dbs) {
      Data data = ctx.dbs.pin(name);
      if(data == null) {
        // check if database exists
        if(!ctx.globalopts.dbexists(name)) throw new BaseXException(dbnf(name));
        data = new DiskData(name, ctx);
        ctx.dbs.add(data);
      }
      // check permissions
      if(!ctx.perm(Perm.READ, data.meta)) {
        Close.close(data, ctx);
        throw new BaseXException(PERM_REQUIRED_X, Perm.READ);
      }
      return data;
    }
  }

  /**
   * Returns an error message for an unknown database.
   * @param name name of database
   * @return error message
   */
  public static String dbnf(final String name) {
    return Util.info(DB_NOT_FOUND_X, name);
  }
}
