package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.CommandBuilder;
import org.basex.core.Command;
import org.basex.core.User;
import org.basex.core.Commands.CmdPerm;
import org.basex.data.Data;
import org.basex.util.Util;

/**
 * Evaluates the 'grant' command and grants permissions to users.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class Grant extends Command {
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
        final Data data = Open.open(db, context);
        User u = data.meta.users.get(name);
        // add local user reference
        if(u == null) {
          u = user.copy();
          data.meta.users.add(u);
        }
        u.perm = perm;
        data.flush();
        Close.close(data, context);
      } catch(final IOException ex) {
        Util.debug(ex);
        final String msg = ex.getMessage();
        return msg.isEmpty() ? error(DBOPENERR, db) : error(msg);
      }
    }
    return info(PERMUP);
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init().arg(0).arg(ON, 2).arg(TO, 1);
  }
}
