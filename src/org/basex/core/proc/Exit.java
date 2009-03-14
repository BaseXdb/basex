package org.basex.core.proc;

import org.basex.core.Process;

/**
 * Represents the 'stop' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Exit extends Process {
  /** Constructor. */
  public Exit() {
    super(STANDARD);
  }
}
