package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Command;
import org.basex.data.Data;

/**
 * Evaluates the 'close' command and closes the current database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
      close(context, data);
      context.closeDB();
      return info(DBCLOSED, data.meta.name);
    } catch(final IOException ex) {
      Main.debug(ex);
      return error(DBCLOSEERR);
    }
  }

  /**
   * Closes the specified database.
   * @param ctx database context
   * @param data data reference
   * @throws IOException I/O exception
   */
  public static synchronized void close(final Context ctx, final Data data)
      throws IOException {
    if(ctx.unpin(data)) data.close();
  }
}
