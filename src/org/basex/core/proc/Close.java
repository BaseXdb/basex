package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;

/**
 * Evaluates the 'close' command. Removes the current database from
 * memory and releases memory resources.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Close extends Proc {
  @Override
  protected boolean exec() {
    // no database in memory?
    if(context.data() == null) return Prop.info ? info(DBEMPTY) : true;

    // close database
    return context.close() ? Prop.info ? info(DBCLOSED) : true :
      error(DBCLOSEERR);
  }
  
  /**
   * Closes the specified database.
   * @param data database to be closed
   * @return success of operation
   */
  public static boolean close(final Data data) {
    try {
      data.close();
      return true;
    } catch(final IOException ex) {
      return BaseX.debug(ex);
    }
  }
}
