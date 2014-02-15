package org.basex.core.cmd;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;

/**
 * Evaluates the 'show sessions' command and shows server sessions.
 *
 * @author BaseX Team 2005-14, BSD License
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
  public void databases(final LockResult lr) {
    // No locks needed
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.SHOW + " " + CmdShow.SESSIONS);
  }
}
