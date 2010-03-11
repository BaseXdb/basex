package org.basex.core.proc;

import java.io.IOException;
import org.basex.core.Proc;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdShow;

/**
 * Evaluates the 'show databases' command and shows opened databases.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class ShowDatabases extends Proc {
  /**
   * Default constructor.
   */
  public ShowDatabases() {
    super(User.ADMIN);
  }

  @Override
  protected boolean run() throws IOException {
    out.println(context.pool.info());
    return true;
  }

  @Override
  public String toString() {
    return Cmd.SHOW + " " + CmdShow.DATABASES;
  }
}
