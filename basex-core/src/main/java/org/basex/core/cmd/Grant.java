package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.CmdPerm;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.util.*;

/**
 * Evaluates the 'grant' command and grants permissions to users.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Grant extends AUser {
  /** Permission. */
  private Perm prm;

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
      prm = Perm.NONE;
    } else if(cmd == CmdPerm.READ) {
      prm = Perm.READ;
    } else if(cmd == CmdPerm.WRITE) {
      prm = Perm.WRITE;
    } else if(cmd == CmdPerm.CREATE && args[2] == null) {
      prm = Perm.CREATE;
    } else if(cmd == CmdPerm.ADMIN && args[2] == null) {
      prm = Perm.ADMIN;
    }
    if(prm == null) return error(PERM_UNKNOWN_X, args[0]);

    return run(1, false);
  }

  @Override
  protected boolean run(final String name, final String db) {
    // admin cannot be modified
    if(name.equals(UserText.ADMIN)) return !info(ADMIN_STATIC);

    // set global permissions
    final Users users = context.users;
    final User user = users.get(name);
    if(db == null) {
      users.perm(user, prm);
      users.write();
      return info(GRANTED_X_X, args[0], name);
    }

    // set local permissions
    final Data data;
    try {
      data = Open.open(db, context, options);
    } catch(final IOException ex) {
      return !info(Util.message(ex));
    }

    // try to lock database
    if(!startUpdate(data)) return false;

    final User us = data.meta.users.get(name);
    if(us == null) {
      // create new local user
      data.meta.users.add(new User(name, prm));
    } else {
      us.perm(prm);
    }
    if(!finishUpdate(data)) return false;

    Close.close(data, context);
    return info(GRANTED_ON_X_X_X, args[0], name, db);
  }

  @Override
  public void databases(final LockResult lr) {
    super.databases(lr);
    if(!databases(lr.write, 2)) lr.writeAll = true;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init().arg(0).arg(ON, 2).arg(S_TO, 1);
  }
}
