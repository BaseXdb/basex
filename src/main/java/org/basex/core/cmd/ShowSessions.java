package org.basex.core.cmd;

import java.io.*;

import org.basex.core.*;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdShow;

/**
 * Evaluates the 'show sessions' command and shows server sessions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ShowSessions extends Command {
  /**
   * Default constructor.
   */
  public ShowSessions() {
    super(Perm.ADMIN);
  }

  @Override
  protected boolean run() throws IOException {
    out.println(context.sessions.info());
    return true;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.SHOW + " " + CmdShow.SESSIONS);
  }
}
