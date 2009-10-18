package org.basex.core.proc;

import org.basex.io.PrintOutput;

/**
 * Evaluates the 'create user' command and creates a new user.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class CreateUser extends AAdmin {
  /**
   * Default constructor.
   * @param name user name
   * @param pw password
   */
  public CreateUser(final String name, final String pw) {
    super(name, pw);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    return context.users.create(args[0], args[1]) ?
      info("User created.") : error("User exists already.");
  }
}
