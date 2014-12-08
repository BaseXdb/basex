package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdCreate;
import org.basex.core.users.*;

/**
 * Evaluates the 'create user' command and creates a new user.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class CreateUser extends AUser {
  /**
   * Default constructor.
   * @param name user name
   * @param pw password
   */
  public CreateUser(final String name, final String pw) {
    super(name, pw);
  }

  @Override
  protected boolean run() {
    final String name = args[0], pw = args[1];
    if(!Databases.validName(name)) return error(NAME_INVALID_X, name);
    if(name.equals(UserText.ADMIN)) return error(ADMIN_STATIC);

    context.users.create(name, pw);
    return info(USER_CREATED_X, args[0]);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.USER);
    cb.arg(0);
    if(!cb.conf()) cb.arg(1);
  }
}
