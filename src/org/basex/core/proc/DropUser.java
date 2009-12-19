package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.server.ServerProcess;

/**
 * Evaluates the 'drop user' command and drops a user.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DropUser extends AAdmin {
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
    super(name, db);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    final String usern = args[0];
    if(usern.equals(ADMIN)) return error(USERADMIN);

    for(final ServerProcess s : context.sessions) {
      if(s.context.user.name.equals(usern)) return error(USERLOG, usern);
    }

    final User user = context.users.get(usern);
    if(user == null) return error(USERNO, usern);

    final String db = args[1];
    if(db == null) {
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
  public String toString() {
    final StringBuilder sb = new StringBuilder(
        Cmd.DROP + " " + CmdCreate.USER + " " + args[0]);
    if(args[1] != null) sb.append(" " + ON + " " + args[1]);
    return sb.toString();
  }
}
