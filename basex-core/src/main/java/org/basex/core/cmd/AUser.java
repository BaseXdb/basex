package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;

/**
 * Abstract class for user commands.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class AUser extends Command {
  /**
   * Protected constructor, specifying command arguments.
   * @param perm required permission
   * @param args arguments
   */
  AUser(final Perm perm, final String... args) {
    super(perm, args);
  }

  /**
   * Protected constructor, specifying command arguments.
   * @param args arguments
   */
  AUser(final String... args) {
    this(Perm.ADMIN, args);
  }

  /**
   * Runs the command for all users and databases.
   * @param offset offset for users and optional databases ({@code offset + 1})
   * @param optional indicates if user/database argument is optional
   * @return success flag
   */
  final boolean run(final int offset, final boolean optional) {
    final String name = args[offset];
    final String db = offset + 1 < args.length ? args[offset + 1] : "";

    if(!Databases.validPattern(name)) return error(NAME_INVALID_X, name);
    if(!db.isEmpty() && !Databases.validPattern(db))
      return error(NAME_INVALID_X, db);

    // retrieve all users; stop if no user is found
    final String[] users = context.users.find(Databases.regex(name));
    if(users.length == 0) return info(UNKNOWN_USER_X, name) && optional;

    // loop through all users
    boolean ok = true;
    for(final String user : users) {
      ok &= run(user, db);
    }
    context.users.write();
    return ok;
  }

  /**
   * Runs the command for the specified user and database pattern.
   * @param user user to be modified
   * @param db database pattern
   * @return success flag
   */
  @SuppressWarnings("unused")
  boolean run(final String user, final String db) {
    return true;
  }

  @Override
  public void addLocks() {
    jc().locks.writes.add(Locking.USER);
  }
}
