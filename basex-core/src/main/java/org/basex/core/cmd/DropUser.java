package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdDrop;
import org.basex.core.users.*;
import org.basex.server.*;

/**
 * Evaluates the 'drop user' command and drops a user.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DropUser extends AUser {
  /**
   * Default constructor.
   * @param name name of user
   */
  public DropUser(final String name) {
    this(name, null);
  }

  /**
   * Constructor for dropping local database users.
   * @param name name of user
   * @param pattern database pattern (may be {@code null})
   */
  public DropUser(final String name, final String pattern) {
    super(name, pattern == null ? "" : pattern);
  }

  @Override
  protected boolean run() {
    return run(0, true);
  }

  @Override
  protected boolean run(final String name, final String db) {
    // admin cannot be dropped
    if(name.equals(UserText.ADMIN)) return !info(ADMIN_STATIC);

    // drop global user
    final Users users = context.users;
    final User user = users.get(name);
    if(user != null) {
      if(db.isEmpty()) {
        for(final ClientListener cl : context.sessions) {
          if(cl.context().user().name().equals(name)) return !info(USER_LOGGED_IN_X, name);
        }
        users.drop(user);
      } else {
        user.drop(db);
      }
      return info(db.isEmpty() ? USER_DROPPED_X : USER_DROPPED_X_X, name, db);
    }
    return true;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.USER).arg(0).arg(ON, 1);
  }
}
