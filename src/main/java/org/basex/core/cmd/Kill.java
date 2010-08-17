package org.basex.core.cmd;

import static org.basex.core.Text.*;
import org.basex.core.Command;
import org.basex.core.User;
import org.basex.server.Sessions;

/**
 * Evaluates the 'kill' command and stops user sessions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Kill extends Command {
  /**
   * Default constructor.
   * @param user user to kill
   */
  public Kill(final String user) {
    super(User.ADMIN, user);
  }

  @Override
  protected boolean run() {
    final String user = args[0];
    if(user.equals(ADMIN)) return error(USERADMIN);
    if(user.equals(context.user.name)) return error(USERKILLSELF, user);

    final Sessions ss = context.sessions;
    final int s = ss.size();
    for(int i = 0; i < ss.size(); ++i) {
      if(user.equals(ss.get(i).context.user.name)) ss.get(i--).exit();
    }
    return info(USERKILL, s - ss.size());
  }
}
