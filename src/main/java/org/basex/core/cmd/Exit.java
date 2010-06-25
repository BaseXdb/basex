package org.basex.core.cmd;

import org.basex.core.Command;

/**
 * Evaluates the 'exit' command and quits the console.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Exit extends Command {
  /** Constructor. */
  public Exit() {
    super(STANDARD);
  }

  @Override
  protected boolean run() {
    return new Close().run(context);
  }
}
