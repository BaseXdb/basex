package org.basex.core.proc;

import org.basex.core.Process;
import org.basex.core.User;
import org.basex.core.Commands.CmdPerm;

/**
 * Evaluates the 'create user' command and creates a new user.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public abstract class AAdmin extends Process {
  /**
   * Default constructor.
   * @param a arguments
   */
  protected AAdmin(final String... a) {
    super(User.ADMIN, a);
  }

  /**
   * Returns the permissions for the specified admin process.
   * @param set flag for setting/removing permission
   * @return permissions or -1
   */
  protected boolean perm(final boolean set) {
    final String db = args[2];

    final CmdPerm cmd = getOption(CmdPerm.class);
    int perm = -1;
    if(cmd == CmdPerm.READ) {
      perm = User.READ;
    } else if(cmd == CmdPerm.WRITE) {
      perm = User.WRITE;
    } else if(db == null && cmd == CmdPerm.CREATE) {
      perm = User.CREATE;
    } else if(db == null && cmd == CmdPerm.ADMIN) {
      perm = User.ADMIN;
    }
    if(perm == -1) return error("Invalid permission flag.");

    // [AW] Missing: permissions for specific databases
    return context.users.perm(args[1], set, perm) ?
        info("Permission removed.") : error("User is unknown");
  }
}
