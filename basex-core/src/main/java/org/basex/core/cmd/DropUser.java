package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdDrop;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * Evaluates the 'drop user' command and drops a user.
 *
 * @author BaseX Team 2005-14, BSD License
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
   * @param db database (may be {@code null})
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
    if(user.equals(UserText.ADMIN)) return !info(ADMIN_STATIC);

    // drop global user
    final Users users = context.users;
    if(db == null) {
      for(final ClientListener s : context.sessions) {
        if(s.context().user().name().equals(user)) return !info(USER_LOGGED_IN_X, user);
      }
      users.drop(users.get(user));
      return info(USER_DROPPED_X, user);
    }

    final Data data;
    try {
      data = Open.open(db, context, options);
    } catch(final IOException ex) {
      return !info(Util.message(ex), db);
    }

    // try to lock database
    if(!startUpdate(data)) return false;

    // drop local user
    if(data.meta.users.drop(data.meta.users.get(user))) info(USER_DROPPED_X_X, user, db);

    if(!finishUpdate(data)) return false;

    Close.close(data, context);
    return true;
  }

  @Override
  public void databases(final LockResult lr) {
    super.databases(lr);
    databases(lr.write, 1);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.USER).arg(0).arg(ON, 1);
  }
}
