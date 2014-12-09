package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.util.regex.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Abstract class for user commands.
 *
 * @author BaseX Team 2005-14, BSD License
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
   * @param off offset for users and optional databases ({@code off + 1})
   * @param opt indicates if user/database argument is optional
   * @return success flag
   */
  boolean run(final int off, final boolean opt) {
    final String name = args[off];
    final String db = off + 1 < args.length ? args[off + 1] : null;

    if(!Databases.validName(name, true)) return error(NAME_INVALID_X, name);
    if(db != null && !Databases.validName(db, true)) return error(NAME_INVALID_X, db);

    // retrieve all users; stop if no user is found
    final String[] users = users(name);
    if(users.length == 0) return info(UNKNOWN_USER_X, name) && opt;

    // loop through all users
    boolean ok = true;
    for(final String user : users) ok &= run(user, db);
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

  /**
   * Returns all users matching the specified glob pattern.
   * If the specified pattern does not contain any special characters,
   * it is treated as literal.
   * @param name user name pattern
   * @return array with database names
   */
  private String[] users(final String name) {
    final String pat = name.matches(".*[*?,].*") ? IOFile.regex(name) :
      name.replaceAll("([" + Databases.REGEXCHARS + "])", "\\\\$1");
    return context.users.find(Pattern.compile(pat, Prop.CASE ? 0 : Pattern.CASE_INSENSITIVE));
  }

  @Override
  public void databases(final LockResult lr) {
    lr.write.add(DBLocking.ADMIN); // Admin operations are exclusive
  }
}
