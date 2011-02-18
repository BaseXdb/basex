package org.basex.core.cmd;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdAttach;

/**
 * Evaluates the 'attach trigger' command and attaches
 * a session to the trigger pool.
 *
 * @author Workgroup HCI, University of Konstanz 2005-10, ISC License
 * @author Roman Raedle
 */
public final class AttachTrigger extends Command {
  /**
   * Default constructor.
   * @param name user name
   */
  public AttachTrigger(final String name) {
    super(User.ADMIN, name);
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    return context.triggers.attach(name, context.session);
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.ATTACH + " " + CmdAttach.TRIGGER).args();
  }
}
