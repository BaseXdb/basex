package org.basex.core.proc;

import static org.basex.Text.*;

import java.io.IOException;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
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
    super(DATAREF);
  }

  @Override
  protected boolean exec() {
    return context.close() ? !Prop.info || info(DBCLOSED) : error(DBCLOSEERR);
  }
  
  /**
   * Closes the specified database.
   * @param ctx database context
   * @param data data reference
   * @throws IOException I/O exception
   */
  public static void close(final Context ctx, final Data data)
      throws IOException {
    // [AW] null test should be removed if query processor handles context
    if(ctx == null || ctx.unpin(data)) data.close();
  }
}
