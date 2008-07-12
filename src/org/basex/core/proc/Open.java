package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.basex.io.IO;

/**
 * Evaluates the 'open' command. Opens the specified database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Open extends Proc {
  @Override
  protected boolean exec() {
    final String db = cmd.arg(0);
    if(!IO.dbpath(db).exists()) return error(DBNOTFOUND, db);

    // close old database
    context.close();
    
    try {
      // open new database instance
      final Data data = new DiskData(db);
      context.data(data);
      if(Prop.info) {
        if(data.meta.newindex) info(INDUPDATE + NL);
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
   * Static method for opening a database.
   * No warnings are thrown; instead, an empty reference is returned.
   * @param db database to be opened
   * @return data reference
   */
  public static Data open(final String db) {
    try {
      return new DiskData(db);
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return null;
    }
  }
}
