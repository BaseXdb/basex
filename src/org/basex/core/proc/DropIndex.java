package org.basex.core.proc;

import static org.basex.core.Commands.*;
import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Commands.CmdIndex;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Data.Type;
import org.basex.io.IO;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'drop index' command and deletes indexes in the currently
 * opened database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DropIndex extends ACreate {
  /**
   * Constructor.
   * @param type index type, defined in {@link CmdIndex}
   */
  public DropIndex(final Object type) {
    super(DATAREF, type.toString());
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    final Data data = context.data();

    switch(getOption(CmdIndex.class)) {
      case TEXT:
        data.meta.txtindex = false;
        return drop(Type.TXT, DATATXT);
      case ATTRIBUTE:
        data.meta.atvindex = false;
        return drop(Type.ATV, DATAATV);
      case FULLTEXT:
        data.meta.ftxindex = false;
        return drop(Type.FTX, DATAFTX);
      case SUMMARY:
        if(data.meta.pathindex) {
          data.meta.pathindex = false;
          data.path.root = null;
          data.flush();
        }
        return info(DBDROP, perf.getTimer());
      default:
        return false;
    }
  }

  /**
   * Drops the specified index.
   * @param index index type
   * @param pat pattern
   * @return success of operation
   */
  private boolean drop(final Type index, final String pat) {
    try {
      final Data data = context.data();
      if(data instanceof MemData) return error(PROCMM);
      data.flush();
      data.closeIndex(index);
      return DropDB.delete(data.meta.name, pat + "." + IO.BASEXSUFFIX, prop) ?
          info(DBDROP, perf.getTimer()) : error(DBDROPERR);
    } catch(final IOException ex) {
      Main.debug(ex);
      return error(ex.getMessage());
    }
  }

  @Override
  public String toString() {
    return Cmd.DROP + " " + CmdDrop.INDEX + " " + args();
  }
}
