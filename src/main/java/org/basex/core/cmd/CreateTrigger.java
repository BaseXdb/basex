package org.basex.core.cmd;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;

/**
 * Evaluates the 'create trigger' command and creates a new trigger.
 *
 * @author Workgroup HCI, University of Konstanz 2005-10, ISC License
 * @author Roman Raedle
 */
public final class CreateTrigger extends Command {
  /**
   * Default constructor.
   * @param name user name
   */
  public CreateTrigger(final String name) {
    super(User.ADMIN, name);
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    return context.triggers.create(name);
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.TRIGGER).args();
  }
}
