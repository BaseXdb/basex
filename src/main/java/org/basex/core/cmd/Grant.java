package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;
import org.basex.core.CommandBuilder;
import org.basex.core.User;
import org.basex.core.Commands.CmdPerm;
import org.basex.data.Data;
import org.basex.util.Util;

/**
 * Evaluates the 'grant' command and grants permissions to users.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Grant extends AUser {
  /** Permission. */
  private int prm = -1;

  /**
   * Default constructor.
   * @param perm permission
   * @param user user name
   */
  public Grant(final Object perm, final String user) {
    this(perm, user, null);
  }

  /**
   * Constructor, specifying a certain database.
   * @param perm permission
   * @param user user name
   * @param db database
   */
  public Grant(final Object perm, final String user, final String db) {
    super(perm.toString(), user, db);
  }

  @Override
  protected boolean run() {
    // find permission
    final CmdPerm cmd = getOption(CmdPerm.class);
    if(cmd == CmdPerm.NONE) {
      prm = User.NONE;
    } else if(cmd == CmdPerm.READ) {
      prm = User.READ;
    } else if(cmd == CmdPerm.WRITE) {
      prm = User.WRITE;
    } else if(cmd == CmdPerm.CREATE && args[2] == null) {
      prm = User.CREATE;
    } else if(cmd == CmdPerm.ADMIN && args[2] == null) {
      prm = User.ADMIN;
    }
    if(prm == -1) return error(PERM_UNKNOWN_X, args[0]);

    return run(1, false);
  }

  @Override
  protected boolean run(final String user, final String db) {
    // admin cannot be modified
    if(user.equals(ADMIN)) return !info(ADMIN_STATIC_X);

    // set global permissions
    if(db == null) {
      context.users.get(user).perm = prm;
      context.users.write();
      return info(GRANTED_X_X, args[0], user);
    }

    // set local permissions
    try {
      final Data data = Open.open(db, context);
      User u = data.meta.users.get(user);
      // add local user reference
      if(u == null) {
        u = context.users.get(user).copy();
        data.meta.users.create(u);
      }
      u.perm = prm;
      data.meta.dirty = true;
      data.flush();
      Close.close(data, context);
      return info(GRANTED_ON_X_X_X, args[0], user, db);
    } catch(final IOException ex) {
      Util.debug(ex);
      final String msg = ex.getMessage();
      return !info(msg.isEmpty() ? DB_NOT_OPENED_X : msg, db);
    }
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init().arg(0).arg(ON, 2).arg(TO, 1);
  }
}
