package org.basex.core.proc;

import org.basex.io.PrintOutput;

/**
 * Evaluates the 'drop user' command and drops a user.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class DropUser extends AAdmin {
  /**
   * Default constructor.
   * @param name name of user
   */
  public DropUser(final String name) {
    super(name);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    return context.users.drop(args[0]) ?
      info("User dropped.") : error("User is unknown.");
  }
}
