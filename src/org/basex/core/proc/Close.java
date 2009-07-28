package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.data.Data;

/**
 * Evaluates the 'close' command. Removes the current database from
 * memory and releases memory resources.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Close extends Process {
  /**
   * Constructor.
   */
  public Close() {
    super(0);
  }

  @Override
  protected boolean exec() {
    try {
      final Data data = context.data();
      if(data == null) return true;
      final String name = data.meta.name;
      close(context, data);
      context.closeDB();
      return info(DBCLOSED, name);
    } catch(final IOException ex) {
      BaseX.debug(ex);
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
