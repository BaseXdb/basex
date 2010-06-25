package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Commands.CmdPerm;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Command;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.DiskData;

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
    new Close().run(context);
    final String db = args[0];

    try {
      final Data data = open(context, db);
      context.openDB(data);
      if(data.meta.oldindex) info(INDUPDATE);
      return info(DBOPENED, db, perf);
    } catch(final IOException ex) {
      Main.debug(ex);
      final String msg = ex.getMessage();
      return msg.isEmpty() ? error(DBOPENERR, db) : error(msg);
    }
  }

  /**
   * Opens the specified database.
   * @param ctx database context
   * @param db name of database
   * @return data reference
   * @throws IOException I/O exception
   */
  public static synchronized Data open(final Context ctx, final String db)
      throws IOException {

    Data data = ctx.pin(db);
    if(data == null) {
      // check if document exists
      if(!ctx.prop.dbexists(db))
        throw new IOException(Main.info(DBNOTFOUND, db));

      data = new DiskData(db, ctx.prop);
      ctx.pin(data);
    }
    // check permissions
    if(ctx.perm(User.READ, data.meta)) return data;

    Close.close(ctx, data);
    throw new IOException(Main.info(PERMNO, CmdPerm.READ));
  }
}
