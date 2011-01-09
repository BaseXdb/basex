package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.CommandBuilder;
import org.basex.core.Command;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdDrop;
import org.basex.data.Data;
import org.basex.server.ServerProcess;
import org.basex.util.Util;

/**
 * Evaluates the 'drop user' command and drops a user.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class DropUser extends Command {
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
    super(User.ADMIN, name, db);
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    if(name.equals(ADMIN)) return error(USERADMIN);
    if(!validName(name)) return error(NAMEINVALID, name);

    final User user = context.users.get(name);
    if(user == null) return error(USERNO, name);

    final String db = args[1];
    if(db != null && !validName(db)) return error(NAMEINVALID, db);
    if(db == null) {
      for(final ServerProcess s : context.sessions) {
        if(s.context.user.name.equals(name)) return error(USERLOG, name);
      }
      context.users.drop(user);
    } else {
      try {
        final Data data = Open.open(db, context);
        data.meta.users.remove(data.meta.users.get(args[0]));
        data.flush();
        Close.close(context, data);
      } catch(final IOException ex) {
        Util.debug(ex);
        final String msg = ex.getMessage();
        return msg.isEmpty() ? error(DBOPENERR, db) : error(msg);
      }
    }
    return info(USERDROP, name);
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.USER).arg(0).arg(ON, 1);
  }
}
