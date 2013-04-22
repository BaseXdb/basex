package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.data.*;
import org.basex.util.*;

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
    super(Perm.ADMIN, db);
  }

  @Override
  protected boolean run() throws IOException {
    final String name = args[0] == null || args[0].isEmpty() ? null : args[0];
    if(name != null && !MetaData.validName(name, false))
      return error(NAME_INVALID_X, name);

    if(name == null) {
      out.println(context.users.info(null).finish());
    } else {
      try {
        final Data data = Open.open(name, context);
        out.println(data.meta.users.info(context.users).finish());
        Close.close(data, context);
        return true;
      } catch(final IOException ex) {
        return error(Util.message(ex));
      }
    }
    return true;
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.ADMIN);
    if (null != args[0])
      databases(lr.read, 0);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.SHOW + " " + CmdShow.USERS).arg(ON, 0);
  }
}
