package org.basex.core.cmd;

import static org.basex.core.Text.*;
import org.basex.core.Command;

/**
 * Evaluates the 'password' command and alters the user's password.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class Password extends Command {
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
    return pass != null && !pass.isEmpty() && context.users.alter(user, pass) ?
        info(USERALTER, user) : error(PASSNO, user);
  }
}
