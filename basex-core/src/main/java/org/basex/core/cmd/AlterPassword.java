package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdAlter;
import org.basex.core.users.*;

/**
 * Evaluates the 'alter password' command and alters the password of a user.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class AlterPassword extends AUser {
  /**
   * Default constructor.
   * @param name user name
   * @param pw password
   */
  public AlterPassword(final String name, final String pw) {
    super(name, pw);
  }

  @Override
  protected boolean run() {
    final String name = args[0], pass = args[1];
    if(!Databases.validName(name)) return error(NAME_INVALID_X, name);

    final Users users = context.users;
    final User user = users.get(name);
    if(user == null) return error(UNKNOWN_USER_X, name);

    users.password(user, pass);
    users.write();
    return info(PW_CHANGED_X, name);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.ALTER + " " + CmdAlter.PASSWORD).arg(0);
    if(!cb.conf()) cb.arg(1);
  }
}
