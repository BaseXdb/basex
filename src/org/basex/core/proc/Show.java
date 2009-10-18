package org.basex.core.proc;

import java.io.IOException;
import org.basex.core.Commands.CmdShow;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'show' command and shows server information.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Show extends AAdmin {
  /**
   * Default constructor.
   * @param cmd show command
   */
  public Show(final Object cmd) {
    super(cmd.toString());
  }

  @Override
  protected boolean exec(final PrintOutput out) throws IOException {
    switch(getOption(CmdShow.class)) {
      case DATABASES:
        out.println(context.pool.info());
        break;
      case SESSIONS:
        out.println(context.sessions.info());
        break;
      case USERS:
        out.println(context.users.info());
        break;
      default:
        return false;
    }
    return true;
  }
}
