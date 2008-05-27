package org.basex.core.proc;

import static org.basex.Text.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Commands;
import org.basex.data.Data;
import org.basex.index.Index;
import org.basex.io.IO;

/**
 * Evaluates the 'drop' command. Deletes a database or index structure.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Drop extends Proc {
  /** Drop option. */
  public static final String DB = "database";
  /** Drop option. */
  public static final String INDEX = "index";

  @Override
  protected boolean exec() {
    final String type = cmd.arg(0).toLowerCase();
    if(type.equals(DB)) return db();
    if(type.equals(INDEX)) return index();
    throw new IllegalArgumentException();
  }

  /**
   * Drops the specified database.
   * @return success of operation
   */
  private boolean db() {
    // close database if it's open
    final String db = cmd.arg(1);
    final Data data = context.data();
    if(data != null && data.meta.dbname.equals(db))
      execute(context, Commands.CLOSE);

    if(!IO.dbpath(db).exists()) return error(DBNOTFOUND, db);
    return drop(db) ? info(DBDROPPED) : error(DBNOTDROPPED);
  }

  /**
   * Drops index structures.
   * @return success of operation
   */
  private boolean index() {
    if(context.data() == null) return error(PROCNODB);
    if(cmd.nrArgs() != 2) throw new IllegalArgumentException();
    return index(cmd.arg(1));
  }

  /**
   * Drops the specified index.
   * @param type index type
   * @return success of operation
   */
  private boolean index(final String type) {
    final Data data = context.data();

    if(type.equals(Create.TXT)) {
      data.meta.txtindex = false;
      return dropIndex(Index.TYPE.TXT, DATATXT);
    }
    if(type.equals(Create.ATV)) {
      data.meta.atvindex = false;
      return dropIndex(Index.TYPE.ATV, DATAATV);
    }
    if(type.equals(Create.FTX)) {
      data.meta.ftxindex = false;
      Index.TYPE typ = data.meta.fzindex ? Index.TYPE.FUY : Index.TYPE.FTX;
      return dropIndex(typ, DATAFTX);
    }
    throw new IllegalArgumentException();
  }

  /**
   * Drops the specified index.
   * @param index index type
   * @param pat pattern
   * @return success of operation
   */
  private boolean dropIndex(final Index.TYPE index, final String pat) {
    try {
      final Data data = context.data();
      data.meta.finish(data.size);
      data.closeIndex(index);
      return IO.dbdelete(data.meta.dbname, pat + "." + IO.BASEXSUFFIX) ?
          info(DBDROP) : error(DBDROPERR);
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return error(ex.getMessage());
    }
  }

  /**
   * Delete a database.
   * @param db database name
   * @return success of operation
   */
  public static boolean drop(final String db) {
    return IO.dbdelete(new IO(db).dbname(), null);
  }
}
