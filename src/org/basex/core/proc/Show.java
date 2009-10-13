package org.basex.core.proc;

import static org.basex.core.Text.*;
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
    return getType() != null;
  }

  @Override
  protected void out(final PrintOutput o) throws IOException {
    final CmdShow type = getType();

    switch(type) {
      case DATABASES:
        o.println(context.info());
        break;
      case SESSIONS:
        o.println(context.sessions.info());
        break;
      case USERS:
        o.println(context.users.show());
        break;
    }
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
