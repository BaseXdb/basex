package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'close' command and closes the current database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Close extends Command {
  /**
   * Default constructor.
   */
  public Close() {
    super(Perm.NONE);
  }

  @Override
  protected boolean run() {
    final Data data = context.data();
    if(data == null) return true;

    close(data, context);
    context.closeDB();
    return info(DB_CLOSED_X, data.meta.name);
  }

  @Override
  public boolean databases(final StringList db) {
    db.add("");
    return true;
  }

  /**
   * Closes the specified database.
   * @param data data reference
   * @param ctx database context
   */
  public static void close(final Data data, final Context ctx) {
    synchronized(ctx.dbs) {
      if(ctx.dbs.unpin(data)) data.close();
    }
  }
}
