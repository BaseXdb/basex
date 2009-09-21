package org.basex.core.proc;

import static org.basex.core.Text.*;
import org.basex.core.Process;
import org.basex.core.Commands.CmdShow;
import org.basex.server.Sessions;

/**
 * Evaluates the 'kill' command and stops all current sessions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Kill extends Process {
  /**
   * Default constructor.
   */
  public Kill() {
    super(STANDARD);
  }

  @Override
  protected boolean exec() {
    final Sessions ss = context.sessions;
    final int s = ss.size();
    for(int i = 0; i < ss.size();) ss.get(i).exit();
    return info("% sessions killed.", s);
  }

  /**
   * Returns the update type.
   * @return update type.
   */
  protected CmdShow getType() {
    try {
      return CmdShow.valueOf(args[0].toUpperCase());
    } catch(final Exception ex) {
      error(CMDWHICH, args[0]);
      return null;
    }
  }
}
