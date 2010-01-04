package org.basex.core.proc;

import java.io.IOException;

import org.basex.core.Proc;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdShow;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'show sessions' command and shows server sessions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class ShowSessions extends Proc {
  /**
   * Default constructor.
   */
  public ShowSessions() {
    super(User.ADMIN);
  }

  @Override
  protected boolean exec(final PrintOutput out) throws IOException {
    out.println(context.sessions.info());
    return true;
  }

  @Override
  public String toString() {
    return Cmd.SHOW + " " + CmdShow.SESSIONS;
  }
}
