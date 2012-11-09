package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'open' command and opens a database.
 *
 * @author BaseX Team 2005-12, BSD License
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
    if(!MetaData.validName(db, false)) return error(NAME_INVALID_X, db);

    try {
      final Data data = open(db, context);
      context.openDB(data);
      if(data.meta.oldindex()) info(H_INDEX_FORMAT);
      if(data.meta.corrupt)  info(DB_CORRUPT);
      return info(DB_OPENED_X, db, perf);
    } catch(final IOException ex) {
      Util.debug(ex);
      final String msg = ex.getMessage();
      return msg.isEmpty() ? error(DB_NOT_OPENED_X, db) : error(msg);
    }
  }

  @Override
  protected boolean databases(final StringList db) {
    db.add("").add(args[0]);
    return true;
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
    Data data;
    synchronized(ctx.datas) { // pin should be atomic
      data = ctx.pin(name);
      if(data == null) {
        // check if document exists
        if(!ctx.mprop.dbexists(name)) throw new BaseXException(DB_NOT_FOUND_X, name);
        data = new DiskData(name, ctx);
        ctx.pin(data);
      }
    }

    // check permissions
    if(!ctx.perm(Perm.READ, data.meta)) {
      Close.close(data, ctx);
      throw new BaseXException(PERM_REQUIRED_X, Perm.READ);
    }

    return data;
  }
}
