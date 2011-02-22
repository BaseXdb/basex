package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdDrop;

/**
 * Evaluates the 'drop trigger' command and drops an existing trigger.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class DropTrigger extends Command {
  /**
   * Default constructor.
   * @param name user name
   */
  public DropTrigger(final String name) {
    super(User.ADMIN, name);
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    if(context.triggers.drop(name)) {
      return info(TRIGGERDROP, name);
    }
    return error(TRIGGERNO, name);
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.TRIGGER).args();
  }
}
