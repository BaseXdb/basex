package org.basex.core.proc;

import static org.basex.core.Text.*;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'drop user' command and drops a user.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class DropUser extends AAdmin {
  /**
   * Default constructor.
   * @param name name of user
   */
  public DropUser(final String name) {
    super(name);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    // [CG] check if user is logged in
    final String user = args[0];
    return user.equals(ADMIN) ? error(USERADMIN) : context.users.drop(args[0]) ?
      info(USERDROP, user) : error(USERNO, user);
  }

  @Override
  public String toString() {
    return Cmd.DROP + " " + CmdCreate.USER + args();
  }
}
