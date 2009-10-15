package org.basex.core.proc;

import org.basex.core.Process;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'exit' command and quits the console.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Exit extends Process {
  /** Constructor. */
  public Exit() {
    super(STANDARD);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    return new Close().execute(context, out);
  }
}
