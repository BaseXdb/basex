package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.server.Sessions;

/**
 * Evaluates the 'kill' command and stops user sessions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Kill extends AUser {
  /** Number of modifications. */
  private int count;

  /**
   * Default constructor.
   * @param user user to kill
   */
  public Kill(final String user) {
    super(user);
  }

  @Override
  protected boolean run() {
    return run(0, false) && info(USERKILL, count);
  }

  @Override
  protected boolean run(final String user, final String db) {
    // admin cannot be killed, and user cannot kill itself
    if(user.equals(ADMIN)) return !info(USERADMIN);
    if(user.equals(context.user.name)) return !info(USERKILLSELF, user);

    // kill all sessions of the specified user
    final Sessions ss = context.sessions;
    for(int i = 0; i < ss.size(); ++i) {
      if(user.equals(ss.get(i).user().name)) {
        ss.get(i--).quit();
        count++;
      }
    }
    return true;
  }
}
