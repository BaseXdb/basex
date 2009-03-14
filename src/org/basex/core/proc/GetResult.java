package org.basex.core.proc;

import org.basex.core.Process;

/**
 * Represents the 'get result' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class GetResult extends Process {
  /**
   * Constructor.
   * @param n optional process number
   */
  public GetResult(final String n) {
    super(STANDARD, n);
  }
}
