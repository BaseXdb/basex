package org.basex.core.proc;

import org.basex.core.Process;

/**
 * Internal command, returning process output.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class IntOutput extends Process {
  /**
   * Default constructor.
   */
  public IntOutput() {
    super(STANDARD);
  }
}
