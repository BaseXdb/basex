package org.basex.core.cmd;

import static org.basex.core.Text.*;
import org.basex.core.CommandBuilder;
import org.basex.core.Command;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;

/**
 * Evaluates the 'create user' command and creates a new user.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class CreateUser extends Command {
  /**
   * Default constructor.
   * @param name user name
   * @param pw password
   */
  public CreateUser(final String name, final String pw) {
    super(User.ADMIN, name, pw);
  }

  @Override
  protected boolean run() {
    final String user = args[0];
    final String pass = args[1];
    if(!validName(user)) return error(NAMEINVALID, user);

    return pass == null || pass.isEmpty() ? error(PASSNO, user) :
      context.users.create(user, pass) ?
      info(USERCREATE, user) : error(USERKNOWN, user);
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.USER).args();
  }
}
