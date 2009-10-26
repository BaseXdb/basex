package org.basex.core.proc;

import static org.basex.core.Text.*;
import org.basex.core.Process;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'password' command and alters the user's password.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Password extends Process {
  /**
   * Default constructor.
   * @param pw password
   */
  public Password(final String pw) {
    super(STANDARD, pw);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    final String user = context.user.name;
    return context.users.alter(user, args[0]) ?
        info(USERALTER, user) : error(PASSNO, user);
  }
}
