package org.basex.core.cmd;

import org.basex.core.Command;

/**
 * Evaluates the 'exit' command and quits the console.
 *
 * @author BaseX Team 2005-12, BSD License
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
