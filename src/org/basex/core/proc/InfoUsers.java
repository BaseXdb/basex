package org.basex.core.proc;

import java.io.IOException;

import org.basex.core.User;
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
    // [AW] ...should return users of current database
    out.println(context.users.info());
    return true;
  }
}
