package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.CommandBuilder;
import org.basex.core.Main;
import org.basex.core.Command;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdDrop;
import org.basex.data.Data;
import org.basex.server.ServerProcess;

/**
 * Evaluates the 'drop user' command and drops a user.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
    final String usern = args[0];
    if(usern.equals(ADMIN)) return error(USERADMIN);

    final User user = context.users.get(usern);
    if(user == null) return error(USERNO, usern);

    final String db = args[1];
    if(db == null) {
      for(final ServerProcess s : context.sessions) {
        if(s.context.user.name.equals(usern)) return error(USERLOG, usern);
      }
      context.users.drop(user);
    } else {
      try {
        final Data data = Open.open(context, db);
        data.meta.users.remove(data.meta.users.get(args[0]));
        data.flush();
        Close.close(context, data);
      } catch(final IOException ex) {
        Main.debug(ex);
        final String msg = ex.getMessage();
        return msg.isEmpty() ? error(DBOPENERR, db) : error(msg);
      }
    }
    return info(USERDROP, usern);
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.USER).arg(0).arg(ON, 1);
  }
}
