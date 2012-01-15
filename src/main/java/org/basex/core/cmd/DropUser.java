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
    if(user.equals(ADMIN)) return !info(USERADMIN);

    // drop global user
    if(db == null) {
      for(final ClientListener s : context.sessions) {
        if(s.context().user.name.equals(user)) return !info(USERLOG, user);
      }
      context.users.drop(context.users.get(user));
      return info(USERDROP, user);
    }

    // drop local user
    try {
      final Data data = Open.open(db, context);
      if(data.meta.users.drop(data.meta.users.get(user))) {
        info(USERDROPON, user, db);
        data.meta.dirty = true;
        data.flush();
      }
      Close.close(data, context);
      return true;
    } catch(final IOException ex) {
      Util.debug(ex);
      final String msg = ex.getMessage();
      return !info(msg.isEmpty() ? DBOPENERR : msg, db);
    }
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.USER).arg(0).arg(ON, 1);
  }
}
