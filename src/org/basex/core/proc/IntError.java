package org.basex.core.proc;

import org.basex.core.Proc;
import org.basex.io.PrintOutput;

/**
 * Returns an error message.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class IntError extends Proc {
  /**
   * Default constructor.
   * @param msg error message
   */
  public IntError(final String msg) {
    super(STANDARD);
    error(msg);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    return false;
  }
}
