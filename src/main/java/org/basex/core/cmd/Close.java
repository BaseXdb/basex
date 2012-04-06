package org.basex.core.cmd;

import static org.basex.core.Text.*;
import org.basex.core.*;
import org.basex.data.Data;

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

  /**
   * Closes the specified database.
   * @param data data reference
   * @param ctx database context
   */
  public static synchronized void close(final Data data, final Context ctx) {
    if(ctx.unpin(data)) data.close();
  }
}
