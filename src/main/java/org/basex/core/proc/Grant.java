package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdPerm;
import org.basex.data.Data;

/**
 * Evaluates the 'grant' command and grants permissions to users.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Grant extends Proc {
  /**
   * Default constructor.
   * @param perm permission
   * @param name user name
   */
  public Grant(final Object perm, final String name) {
    this(perm, name, null);
  }

  /**
   * Constructor, specifying a certain database.
   * @param perm permission
   * @param name user name
   * @param db database
   */
  public Grant(final Object perm, final String name, final String db) {
    super(User.ADMIN, perm.toString(), name, db);
  }

  @Override
  protected boolean run() {
    final String name = args[1];
    final String db = args[2];
    if(name.equals(ADMIN)) return error(USERADMIN);

    // find permission
    final CmdPerm cmd = getOption(CmdPerm.class);
    int perm = -1;
    if(cmd == CmdPerm.NONE) {
      perm = User.NONE;
    } else if(cmd == CmdPerm.READ) {
      perm = User.READ;
    } else if(cmd == CmdPerm.WRITE) {
      perm = User.WRITE;
    } else if(cmd == CmdPerm.CREATE && db == null) {
      perm = User.CREATE;
    } else if(cmd == CmdPerm.ADMIN && db == null) {
      perm = User.ADMIN;
    }
    if(perm == -1) return error(PERMINV);

    final User user = context.users.get(name);
    if(user == null) return error(USERNO, name);

    if(db == null) {
      // global permissions
      user.perm = perm;
      context.users.write();
    } else {
      try {
        final Data data = Open.open(context, db);
        User u = data.meta.users.get(name);
        // add local user reference
        if(u == null) {
          u = user.copy();
          data.meta.users.add(u);
        }
        u.perm = perm;
        data.flush();
        Close.close(context, data);
      } catch(final IOException ex) {
        Main.debug(ex);
        final String msg = ex.getMessage();
        return msg.isEmpty() ? error(DBOPENERR, db) : error(msg);
      }
    }
    return info(PERMUP);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Cmd.GRANT + " " + args[0]);
    if(args[2] != null) sb.append(" " + ON + " " + args[2]);
    return sb.append(" " + TO + " " + args[1]).toString();
  }
}
