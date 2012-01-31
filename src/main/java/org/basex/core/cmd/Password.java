package org.basex.core.cmd;

import static org.basex.core.Text.*;

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
    super(STANDARD, pw);
  }

  @Override
  protected boolean run() {
    final String user = context.user.name;
    final String pass = args[0];
    return isMD5(pass) && context.users.alter(user, pass) ?
        info(PW_CHANGED_X, user) : error(PW_NOT_VALID);
  }
}
