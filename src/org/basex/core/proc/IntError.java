package org.basex.core.proc;

import org.basex.core.Process;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'info users' command and returns user information.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class IntError extends Process {
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
    return true;
  }
}
