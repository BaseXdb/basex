package org.basex.core.cmd;

import java.io.IOException;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdShow;
import org.basex.core.User;

/**
 * Evaluates the 'show events' command and lists all existing events.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class ShowEvents extends Command {
  /**
   * Default constructor.
   */
  public ShowEvents() {
    super(User.ADMIN);
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
