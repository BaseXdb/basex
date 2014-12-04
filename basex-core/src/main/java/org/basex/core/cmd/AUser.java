package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.util.regex.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

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
    final String u = args[off];
    final String d = off + 1 < args.length ? args[off + 1] : null;

    if(!Databases.validName(u, true)) return error(NAME_INVALID_X, u);
    if(d != null && !Databases.validName(d, true)) return error(NAME_INVALID_X, d);

    // retrieve all users; stop if no user is found
    final String[] users = users(u);
    if(users.length == 0) return info(UNKNOWN_USER_X, u) && opt;
    // retrieve all databases
    StringList dbs = null;
    if(d != null) {
      dbs = context.databases.listDBs(d);
      if(dbs.isEmpty()) return info(DB_NOT_FOUND_X, d) && opt;
    }

    // loop through all users
    boolean ok = true;
    for(final String user : users) {
      if(dbs == null) {
        ok &= run(user, null);
      } else {
        for(final String db : dbs) ok &= run(user, db);
      }
    }
    return ok;
  }

  /**
   * Runs the command for the specified user and database.
   * @param user user to be modified
   * @param db database to be modified
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
