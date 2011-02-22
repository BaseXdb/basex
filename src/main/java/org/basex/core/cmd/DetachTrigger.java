package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdDetach;

/**
 * Evaluates the 'attach trigger' command and detaches
 * a session from the trigger pool.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Roman Raedle
 */
public final class DetachTrigger extends Command {
  /**
   * Default constructor.
   * @param name user name
   */
  public DetachTrigger(final String name) {
    super(User.ADMIN, name);
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    if(context.triggers.detach(name, context.session)) {
      return info(TRIGGERDET, name);
    }
    return error(TRIGGERNO, name);
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdDetach.TRIGGER).args();
  }
}
