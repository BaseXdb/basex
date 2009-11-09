package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Process;
import org.basex.core.User;
import org.basex.core.Commands.CmdPerm;
import org.basex.data.Data;

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
    if(args[1].equals(ADMIN)) return error(USERADMIN);
    
    final String db = args[2];

    // find permission
    final CmdPerm cmd = getOption(CmdPerm.class);
    int perm = -1;
    if(cmd == CmdPerm.READ) {
      perm = User.READ;
    } else if(cmd == CmdPerm.WRITE) {
      perm = User.WRITE;
    } else if(cmd == CmdPerm.CREATE && db == null) {
      perm = User.CREATE;
    } else if(cmd == CmdPerm.ADMIN && db == null) {
      perm = User.ADMIN;
    } else if(cmd == CmdPerm.ALL) {
      perm = db == null ? User.READ | User.WRITE | User.CREATE | User.ADMIN :
        User.READ | User.WRITE;
    }
    if(perm == -1) return error(PERMINV);

    final User user = context.users.get(args[1]);
    if(user == null) return error(USERNO, args[1]);

    if(db == null) {
      // global permissions
      user.perm(set, perm);
      context.users.write();
    } else {
      try {
        final Data data = Open.open(context, db);
        User u = data.meta.users.get(args[1]);
        // add local user reference
        if(u == null) {
          u = user.copy();
          u.perm(false, User.ADMIN);
          u.perm(false, User.CREATE);
          data.meta.users.add(u);
        }
        u.perm(set, perm);
        data.flush();
        Close.close(context, data);
      } catch(final IOException ex) {
        Main.debug(ex);
        final String msg = ex.getMessage();
        return msg.isEmpty() ? error(DBOPENERR, db) : error(msg);
      }
    }
    return info(set ? PERMADD : PERMDEL);
  }
}
