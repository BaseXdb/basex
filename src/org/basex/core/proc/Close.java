package org.basex.core.proc;

import static org.basex.Text.*;

import org.basex.core.Process;
import org.basex.core.Prop;

/**
 * Evaluates the 'close' command. Removes the current database from
 * memory and releases memory resources.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
    return context.close() ? Prop.info ? info(DBCLOSED) : true :
      error(DBCLOSEERR);
  }
}
