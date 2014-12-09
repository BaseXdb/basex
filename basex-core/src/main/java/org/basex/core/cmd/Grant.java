package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.parse.*;
import org.basex.core.parse.Commands.CmdPerm;
import org.basex.core.users.*;

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

    final Users users = context.users;
    final User user = users.get(name);
    users.perm(user, prm, db);
    return info(db == null ? GRANTED_X_X : GRANTED_ON_X_X_X, args[0], name, db);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init().arg(0).arg(ON, 2).arg(S_TO, 1);
  }
}
