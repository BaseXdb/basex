package org.basex.core.proc;

/**
 * Evaluates the 'create user' command and creates a new user.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class CreateUser extends ACreate {
  /**
   * Default constructor.
   * @param name username
   * @param pw password
   */
  public CreateUser(final String name, final String pw) {
    super(STANDARD, name, pw);
  }

  @Override
  protected boolean exec() {
    final boolean check = context.users.createUser(args[0], args[1]);
    if(check) return check;
    return error("User couldnt be created");
  }
}
