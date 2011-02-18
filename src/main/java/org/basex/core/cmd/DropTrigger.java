package org.basex.core.cmd;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdDrop;

/**
 * Evaluates the 'drop trigger' command and drops an existing trigger.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Roman Raedle
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
    return context.triggers.drop(name);
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.TRIGGER).args();
  }
}
