package org.basex.index;

import static org.basex.core.Text.*;
import java.io.IOException;

import org.basex.core.Main;
import org.basex.core.Progress;
import org.basex.core.proc.DropDB;
import org.basex.data.Data;

/**
 * This interface defines the functions which are needed for building
 * new index structures.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class IndexBuilder extends Progress {
  /** Data reference. */
  protected final Data data;
  /** Total parsing value. */
  protected final int total;
  /** Database is kept open if process is canceled. */
  protected final boolean open;
  /** Current parsing value. */
  protected int pre;

  /**
   * Builds the index structure and returns an index instance.
   * @return index instance
   * @throws IOException IO Exception
   */
  public abstract Index build() throws IOException;

  /**
   * Constructor.
   * @param d reference
   * @param o flag for keeping the database open if the process is canceled
   */
  public IndexBuilder(final Data d, final boolean o) {
    data = d;
    total = data.meta.size;
    open = o;
  }

  @Override
  public void abort() {
    data.meta.dirty = true;
    if(open) return;
    try {
      data.close();
    } catch(final Exception ex) {
      Main.debug(ex);
    }
    DropDB.drop(data.meta.name, data.meta.prop);
  }

  @Override
  public final String tit() {
    return PROGINDEX;
  }

  @Override
  public final double prog() {
    return (double) pre / total;
  }
}
