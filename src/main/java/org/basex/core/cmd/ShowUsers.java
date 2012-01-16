package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;

import org.basex.core.CommandBuilder;
import org.basex.core.Command;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdShow;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.util.Util;

/**
 * Evaluates the 'show users' command and shows existing users.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ShowUsers extends Command {
  /**
   * Default constructor.
   */
  public ShowUsers() {
    this(null);
  }

  /**
   * Constructor, specifying a database.
   * @param db database (for showing users)
   */
  public ShowUsers(final String db) {
    super(User.ADMIN, db);
  }

  @Override
  protected boolean run() throws IOException {
    final String name = args[0];
    if(name != null && !MetaData.validName(name, false))
      return error(NAMEINVALID, name);

    if(name == null) {
      out.println(context.users.info(null));
    } else {
      try {
        final Data data = Open.open(name, context);
        out.println(data.meta.users.info(context.users));
        Close.close(data, context);
        return true;
      } catch(final IOException ex) {
        Util.debug(ex);
        final String msg = ex.getMessage();
        return msg.isEmpty() ? error(DBOPENERR, name) : error(msg);
      }
    }
    return true;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.SHOW + " " + CmdShow.USERS).arg(ON, 0);
  }
}
