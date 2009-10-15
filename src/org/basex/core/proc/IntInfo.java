package org.basex.core.proc;

import org.basex.core.Process;
import org.basex.io.PrintOutput;

/**
 * Internal command, returning process info.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class IntInfo extends Process {
  /**
   * Default constructor.
   */
  public IntInfo() {
    super(STANDARD);
  }
  
  @Override
  protected boolean exec(final PrintOutput out) {
    return true;
  }
}
