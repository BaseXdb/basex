package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'password' command and alters the user's password.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Password extends AUser {
  /**
   * Default constructor.
   * @param pw password
   */
  public Password(final String pw) {
    super(Perm.NONE, pw);
  }

  @Override
  protected boolean run() {
    final String user = context.user.name;
    final String pass = args[0];
    return isMD5(pass) && context.users.alter(user, pass) ?
        info(PW_CHANGED_X, user) : error(PW_NOT_VALID);
  }

  @Override
  protected boolean databases(final StringList db) {
    return true;
  }
}
