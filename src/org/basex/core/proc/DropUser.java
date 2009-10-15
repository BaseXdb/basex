package org.basex.core.proc;

import org.basex.core.User;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'drop user' command and drops a user.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class DropUser extends ACreate {
  /**
   * Default constructor.
   * @param name name of user
   */
  public DropUser(final String name) {
    super(STANDARD | User.ADMIN, name);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    return context.users.drop(args[0]) ||
      error("User is unknown.");
  }
}
