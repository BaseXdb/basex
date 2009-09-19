package org.basex.core.proc;

import org.basex.core.Process;

/**
 * Evaluates the 'exit' command and quits the console.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Exit extends Process {
  /** Constructor. */
  public Exit() {
    super(PRINTING);
  }

  @Override
  protected boolean exec() {
    new Close().execute(context);
    return true;
  }
}
