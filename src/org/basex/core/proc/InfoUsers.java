package org.basex.core.proc;

import java.io.IOException;

import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdInfo;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'info users' command and returns user information.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class InfoUsers extends AInfo {
  /**
   * Default constructor.
   */
  public InfoUsers() {
    super(DATAREF | User.ADMIN);
  }

  @Override
  protected boolean exec(final PrintOutput out) throws IOException {
    out.println(context.data().meta.users.info());
    return true;
  }

  @Override
  public String toString() {
    return Cmd.INFO + " " + CmdInfo.USERS;
  }
}
