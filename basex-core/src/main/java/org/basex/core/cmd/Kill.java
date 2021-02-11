package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.server.*;

/**
 * Evaluates the 'kill' command and stops user sessions.
 *
 * @author BaseX Team 2005-21, BSD License
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
    if(!run(0, false) || count == 0) {
      error(Locking.CONTEXT);

      // kill all sessions with the specified IP (and optional port)
      final Sessions ss = context.sessions;
      final String arg = args[0];
      for(int s = ss.size() - 1; s >= 0; --s) {
        final ClientListener cl = ss.get(s);
        final String cs = cl.toString().replaceAll("[]\\[]", "");
        if(cl.context() == context) {
          // show error if own session is addressed
          if(cs.equals(arg)) return error(KILL_SELF_X, arg);
        } else if(cs.startsWith(arg)) {
          info(LI + cs);
          cl.close();
          count++;
        }
      }
    }
    return info(SESSIONS_KILLED_X, count);
  }

  @Override
  protected boolean run(final String user, final String db) {
    // kill all sessions of the specified user
    final Sessions ss = context.sessions;
    for(int s = ss.size() - 1; s >= 0; --s) {
      final ClientListener cl = ss.get(s);
      final Context ctx = cl.context();
      if(ctx != context && user.equals(ctx.user().name())) {
        // do not kill own sessions
        cl.close();
        count++;
      }
    }
    return true;
  }
}
