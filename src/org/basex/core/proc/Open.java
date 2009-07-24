package org.basex.core.proc;

import static org.basex.Text.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.basex.io.IO;

/**
 * Opens an existing database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Open extends Process {
  /**
   * Constructor.
   * @param name name of database
   */
  public Open(final String name) {
    super(STANDARD, name);
  }

  @Override
  protected boolean exec() {
    // close old database
    context.close();

    final String db = args[0];

    try {
      final Data data = open(context, db);
      context.data(data);

      if(Prop.info) {
        if(data.meta.oldindex) info(INDUPDATE + NL);
        info(DBOPENED, perf.getTimer());
      }
      return true;
    } catch(final IOException ex) {
      BaseX.debug(ex);
      final String msg = ex.getMessage();
      return error(msg.length() != 0 ? msg : DBOPENERR);
    }
  }
  
  /**
   * Opens the specified database.
   * @param ctx database context
   * @param db name of database
   * @return data reference
   * @throws IOException I/O exception
   */
  public static Data open(final Context ctx, final String db)
      throws IOException {

    // check if document exists
    if(!IO.dbpath(db).exists())
      throw new FileNotFoundException(BaseX.info(DBNOTFOUND, db));

    // [AW] null test should be removed if query processor handles context
    if(ctx == null) return new DiskData(db);

    Data data = ctx.pin(db);
    if(data == null) {
      data = new DiskData(db);
      ctx.addToPool(data);
    }
    return data;
  }
}
