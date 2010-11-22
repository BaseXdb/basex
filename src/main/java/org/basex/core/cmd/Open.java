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
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Open extends Command {
  /**
   * Default constructor.
   * @param name name of database
   */
  public Open(final String name) {
    super(STANDARD, name);
  }

  @Override
  protected boolean run() {
    String db = args[0];
    if(!checkName(db)) return error(NAMEINVALID, db);

    new Close().run(context);
    final int i = db.indexOf('/');
    String path = null;
    if(i != -1) {
      path = db.substring(i + 1);
      db = db.substring(0, i);
    }

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

    Close.close(ctx, data);
    throw new IOException(Util.info(PERMNO, CmdPerm.READ));
  }
}
