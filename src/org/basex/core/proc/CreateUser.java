package org.basex.core.proc;

import static org.basex.core.Text.*;

import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'create user' command and creates a new user.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class CreateUser extends AAdmin {
  /**
   * Default constructor.
   * @param name user name
   * @param pw password
   */
  public CreateUser(final String name, final String pw) {
    super(name, pw);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    final String user = args[0];
    return context.users.create(user, args[1]) ?
      info(USERCREATE, user) : error(USERKNOWN, user);
  }

  @Override
  public String toString() {
    return Cmd.CREATE + " " + CmdCreate.USER + args();
  }
}
