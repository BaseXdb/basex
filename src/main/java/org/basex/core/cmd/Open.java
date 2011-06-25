package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Commands.CmdPerm;
import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.basex.util.Util;

/**
 * Evaluates the 'open' command and opens a database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Open extends Command {
  /**
   * Default constructor.
   * @param path database name and optional path
   */
  public Open(final String path) {
    super(STANDARD, path);
  }

  @Override
  protected boolean run() {
    String db = args[0];

    new Close().run(context);
    final int i = db.indexOf('/');
    String path = null;
    if(i != -1) {
      path = db.substring(i + 1);
      db = db.substring(0, i);
    }
    if(!validName(db, false)) return error(NAMEINVALID, db);

    try {
      final Data data = open(db, context);
      context.openDB(data, path);
      if(data.meta.oldindex) info(INDUPDATE);
      return info(DBOPENED, db, perf);
    } catch(final IOException ex) {
      Util.debug(ex);
      final String msg = ex.getMessage();
      return msg.isEmpty() ? error(DBOPENERR, db) : error(msg);
    }
  }

  @Override
  public boolean newData(final Context ctx) {
    new Close().run(ctx);
    return true;
  }

  /**
   * Opens the specified database.
   * @param name name of database
   * @param ctx database context
   * @return data reference
   * @throws IOException I/O exception
   */
  public static synchronized Data open(final String name, final Context ctx)
      throws IOException {

    Data data = ctx.pin(name);
    if(data == null) {
      // check if document exists
      if(!ctx.prop.dbexists(name))
        throw new IOException(Util.info(DBNOTFOUND, name));

      data = new DiskData(name, ctx.prop);
      ctx.pin(data);
    }
    // check permissions
    if(ctx.perm(User.READ, data.meta)) return data;

    Close.close(data, ctx);
    throw new IOException(Util.info(PERMNO, CmdPerm.READ));
  }
}
