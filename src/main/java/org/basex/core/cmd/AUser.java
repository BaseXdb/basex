package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.util.regex.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.list.*;

/**
 * Abstract class for user commands.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class AUser extends Command {
  /**
   * Protected constructor, specifying command arguments.
   * @param p required permission
   * @param a arguments
   */
  AUser(final Perm p, final String... a) {
    super(p, a);
  }

  /**
   * Protected constructor, specifying command arguments.
   * @param a arguments
   */
  AUser(final String... a) {
    this(Perm.ADMIN, a);
  }

  /**
   * Returns all users matching the specified glob pattern.
   * If the specified pattern does not contain any special characters,
   * it is treated as literal.
   * @param name user name pattern
   * @return array with database names
   */
  private String[] users(final String name) {
    final String pat = name.matches(".*[*?,].*") ? IOFile.regex(name) : name;
    return context.users.find(Pattern.compile(pat,
        Prop.WIN ? Pattern.CASE_INSENSITIVE : 0));
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

    if(!MetaData.validName(u, true)) return error(NAME_INVALID_X, u);
    if(d != null && !MetaData.validName(d, true)) return error(NAME_INVALID_X, d);

    // retrieve all users; stop if no user is found
    final String[] users = users(u);
    if(users.length == 0) return info(UNKNOWN_USER_X, u) && opt;
    // retrieve all databases
    StringList dbs = null;
    if(d != null) {
      dbs = context.databases().listDBs(d);
      if(dbs.size() == 0) return info(DB_NOT_FOUND_X, d) && opt;
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
   * Checks if the specified string is a valid MD5 hash value.
   * @param md5 string to be checked
   * @return result of check
   */
  static boolean isMD5(final String md5) {
    return md5 != null && md5.matches("[0-9a-f]{32}");
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
}
