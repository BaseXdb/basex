package org.basex.core.proc;

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
    super(STANDARD, name);
  }

  @Override
  protected boolean exec() {
    return context.users.drop(args[0]) ||
      error("User is unknown.");
  }
}
