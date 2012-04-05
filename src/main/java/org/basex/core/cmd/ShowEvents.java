package org.basex.core.cmd;

import java.io.*;

import org.basex.core.*;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdShow;

/**
 * Evaluates the 'show events' command and lists all existing events.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class ShowEvents extends Command {
  /**
   * Default constructor.
   */
  public ShowEvents() {
    super(Perm.ADMIN);
  }

  @Override
  protected boolean run() throws IOException {
    out.println(context.events.info());
    return true;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.SHOW + " " + CmdShow.EVENTS).args();
  }
}
