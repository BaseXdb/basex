package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;
import org.basex.core.CommandBuilder;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdDrop;
import org.basex.data.Data;
import org.basex.server.ClientListener;
import org.basex.util.Util;

/**
 * Evaluates the 'drop user' command and drops a user.
 *
 * @author BaseX Team 2005-12, BSD License
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
   * Default constructor.
   * @param name name of user
   * @param db database
   */
  public DropUser(final String name, final String db) {
    super(name, db);
  }

  @Override
  protected boolean run() {
    return run(0, true);
  }

  @Override
  protected boolean run(final String user, final String db) {
    // admin cannot be dropped
    if(user.equals(ADMIN)) return !info(ADMIN_STATIC_X);

    // drop global user
    if(db == null) {
      for(final ClientListener s : context.sessions) {
        if(s.context().user.name.equals(user)) return !info(USER_LOGGED_IN_X, user);
      }
      context.users.drop(context.users.get(user));
      return info(USER_DROPPED_X, user);
    }

    final Data data;
    try {
      data = Open.open(db, context);
    } catch(final IOException ex) {
      Util.debug(ex);
      final String msg = ex.getMessage();
      return !info(msg.isEmpty() ? DB_NOT_OPENED_X : msg, db);
    }

    // try to lock database
    if(!data.startUpdate()) return !info(DB_PINNED_X, data.meta.name);

    // drop local user
    if(data.meta.users.drop(data.meta.users.get(user))) {
      info(USER_DROPPED_X_X, user, db);
      data.meta.dirty = true;
    }
    data.finishUpdate();
    Close.close(data, context);
    return true;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.USER).arg(0).arg(ON, 1);
  }
}
