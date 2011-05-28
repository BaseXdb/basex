package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.server.Sessions;

/**
 * Evaluates the 'create event' command and creates a new event.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class CreateEvent extends Command {
  /**
   * Default constructor.
   * @param name user name
   */
  public CreateEvent(final String name) {
    super(User.ADMIN, name);
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    if(context.events.containsKey(name)) return error(EVENTKNOWN, name);

    context.events.put(name, new Sessions());
    return info(EVENTCREATE, name);
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.EVENT).args();
  }
}
