package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.data.*;

/**
 * Evaluates the 'alter user' command and alters the password of a user.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class AlterUser extends AUser {
  /**
   * Default constructor.
   * @param name user name
   * @param pw password
   */
  public AlterUser(final String name, final String pw) {
    super(name, pw);
  }

  @Override
  protected boolean run() {
    final String user = args[0];
    final String pass = args[1];
    if(!MetaData.validName(user, false)) return error(NAME_INVALID_X, user);
    return !isMD5(pass) ? error(PW_NOT_VALID) : context.users.alter(user, pass) ?
        info(PW_CHANGED_X, user) : error(UNKNOWN_USER_X, user);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.ALTER + " " + CmdAlter.USER).args();
  }
}
