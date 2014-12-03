package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.parse.*;

/**
 * Evaluates the 'password' command and alters the user's password.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Password extends AUser {
  /**
   * Default constructor.
   * @param password password (plain text)
   */
  public Password(final String password) {
    super(Perm.NONE, password);
  }

  @Override
  protected boolean run() {
    final String user = context.user().name(), pass = args[0];
    context.users.alter(user, pass);
    return info(PW_CHANGED_X, user);
  }

  @Override
  protected void build(final CmdBuilder cb) {
    cb.init();
    if(cb.conf()) cb.arg(0);
  }
}
