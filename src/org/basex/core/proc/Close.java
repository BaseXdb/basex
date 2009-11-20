package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.data.Data;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'close' command and closes the current database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Close extends Proc {
  /**
   * Default constructor.
   */
  public Close() {
    super(STANDARD);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
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
  public static void close(final Context ctx, final Data data)
      throws IOException {
    if(ctx.unpin(data)) data.close();
  }
}
