package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdDrop;

/**
 * Evaluates the 'drop event' command and drops an existing event.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class DropEvent extends Command {
  /**
   * Default constructor.
   * @param name user name
   */
  public DropEvent(final String name) {
    super(User.ADMIN, name);
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    return context.events.drop(name) ?
        info(EVENT_DROPPED_X, name) : error(EVENT_UNKNOWN_X, name);
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.EVENT).args();
  }
}
