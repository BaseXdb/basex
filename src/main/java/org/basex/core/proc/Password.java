package org.basex.core.proc;

import static org.basex.core.Text.*;
import org.basex.core.Proc;

/**
 * Evaluates the 'password' command and alters the user's password.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Password extends Proc {
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
