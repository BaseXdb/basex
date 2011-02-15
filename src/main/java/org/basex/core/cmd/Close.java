package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.data.Data;
import org.basex.util.Util;

/**
 * Evaluates the 'close' command and closes the current database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Close extends Command {
  /**
   * Default constructor.
   */
  public Close() {
    super(STANDARD);
  }

  @Override
  protected boolean run() {
    try {
      final Data data = context.data;
      if(data == null) return true;
      close(data, context);
      context.closeDB();
      return info(DBCLOSED, data.meta.name);
    } catch(final IOException ex) {
      Util.debug(ex);
      return error(DBCLOSEERR);
    }
  }

  /**
   * Closes the specified database.
   * @param data data reference
   * @param ctx database context
   * @throws IOException I/O exception
   */
  public static synchronized void close(final Data data, final Context ctx)
      throws IOException {
    if(ctx.unpin(data)) data.close();
  }
}
