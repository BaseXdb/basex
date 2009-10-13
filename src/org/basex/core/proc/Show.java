package org.basex.core.proc;

import java.io.IOException;
import org.basex.core.Process;
import org.basex.core.Commands.CmdShow;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'show' command and shows server information.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Show extends Process {
  /**
   * Default constructor.
   * @param cmd show command
   */
  public Show(final Object cmd) {
    super(PRINTING, cmd.toString());
  }

  @Override
  protected boolean exec() {
    return getOption(CmdShow.class) != null;
  }

  @Override
  protected void out(final PrintOutput o) throws IOException {
    switch(getOption(CmdShow.class)) {
      case DATABASES:
        o.println(context.pool.info());
        break;
      case SESSIONS:
        o.println(context.sessions.info());
        break;
      case USERS:
        o.println(context.users.info());
        break;
    }
  }
}
