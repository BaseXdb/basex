package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.data.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'grant' command and grants permissions to users.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Grant extends AUser {
  /** Permission. */
  private Perm perm;

  /**
   * Default constructor.
   * @param permission permission
   * @param user user name
   */
  public Grant(final Object permission, final String user) {
    this(permission, user, null);
  }

  /**
   * Constructor, specifying a database.
   * @param permission permission
   * @param user user name
   * @param db database
   */
  public Grant(final Object permission, final String user, final String db) {
    super(permission.toString(), user, db);
  }

  @Override
  protected boolean run() {
    // find permission
    final CmdPerm cmd = getOption(CmdPerm.class);
    if(cmd == CmdPerm.NONE) {
      perm = Perm.NONE;
    } else if(cmd == CmdPerm.READ) {
      perm = Perm.READ;
    } else if(cmd == CmdPerm.WRITE) {
      perm = Perm.WRITE;
    } else if(cmd == CmdPerm.CREATE && args[2] == null) {
      perm = Perm.CREATE;
    } else if(cmd == CmdPerm.ADMIN && args[2] == null) {
      perm = Perm.ADMIN;
    }
    if(perm == null) return error(PERM_UNKNOWN_X, args[0]);

    return run(1, false);
  }

  @Override
  protected boolean run(final String user, final String db) {
    // admin cannot be modified
    if(user.equals(ADMIN)) return !info(ADMIN_STATIC_X);

    // set global permissions
    if(db == null) {
      context.users.get(user).perm = perm;
      context.users.write();
      return info(GRANTED_X_X, args[0], user);
    }

    // set local permissions
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

    User u = data.meta.users.get(user);
    // add local user reference
    if(u == null) {
      u = context.users.get(user).copy();
      data.meta.users.create(u);
    }
    u.perm = perm;
    data.meta.dirty = true;
    data.finishUpdate();
    Close.close(data, context);
    return info(GRANTED_ON_X_X_X, args[0], user, db);
  }

  @Override
  protected boolean databases(final StringList db) {
    return databases(db, 2);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init().arg(0).arg(ON, 2).arg(C_TO, 1);
  }
}
