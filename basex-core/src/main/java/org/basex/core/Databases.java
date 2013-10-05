package org.basex.core;

import java.util.*;
import java.util.regex.*;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Manages a two-way-map of all available databases and backups. Used for locking.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Jens Erat
 */
public final class Databases {
  /** Allowed characters for database names (additional to letters and digits).
   * The following characters are invalid:
   * <ul>
   * <li> {@code ,?*}" are used by the glob syntax</li>
   * <li> {@code ;} is reserved for separating commands.</li>
   * <li> {@code :*?\"<>\/|}" are used for filenames and paths</li>
   * </ul>
   */
  public static final String DBCHARS = "-+=~!#$%^&()[]{}@'`";
  /** Regex representation of allowed database characters. */
  public static final String REGEXCHARS = DBCHARS.replaceAll("(.)", "\\\\$1");

  /** Pattern to extract the database name from a backup file name. */
  private static final Pattern ZIPPATTERN =
      Pattern.compile(DateTime.PATTERN + '\\' + IO.ZIPSUFFIX + '$');
  /** Regex indicator. */
  private static final Pattern REGEX = Pattern.compile(".*[*?,].*");

  /** Global options. */
  private final GlobalOptions gopts;

  /**
   * Creates a new instance and loads available databases.
   * @param c Database context
   */
  Databases(final Context c) {
    gopts = c.globalopts;
  }

  /**
   * Lists all available databases and backups.
   * @return database and backup list
   */
  public StringList list() {
    return list(true, true, null);
  }

  /**
   * Lists all available databases.
   * @return database list
   */
  public StringList listDBs() {
    return list(true, false, null);
  }

  /**
   * Lists all available databases matching the given name. Supports glob patterns.
   * @param name database name, glob patterns allowed
   * @return database list
   */
  public StringList listDBs(final String name) {
    return list(true, false, name);
  }

  /**
   * Lists all available backups matching the specified prefix.
   * @param prefix prefix (may be {@code null})
   * @return database list
   */
  public StringList backups(final String prefix) {
    final StringList list = new StringList();
    for(final IOFile f : gopts.dbpath().children()) {
      final String name = f.name();
      if(name.endsWith(IO.ZIPSUFFIX) && (prefix == null || name.startsWith(prefix))) {
        list.add(name.replaceFirst("\\..*", ""));
      }
    }
    return list;
  }

  /**
   * Returns the sorted names of all available databases and, optionally, backups.
   * Filters for {@code name} if not {@code null} with glob support.
   * @param db return databases?
   * @param backup return backups?
   * @param name name filter (may be {@code null})
   * @return database and backups list
   */
  private StringList list(final boolean db, final boolean backup, final String name) {
    final Pattern pt;
    if(name != null) {
      final String nm = REGEX.matcher(name).matches() ? IOFile.regex(name) :
        name.replaceAll("([" + REGEXCHARS + "])", "\\\\$1");
      pt = Pattern.compile(nm, Prop.CASE ? 0 : Pattern.CASE_INSENSITIVE);
    } else {
      pt = null;
    }

    final IOFile[] children = gopts.dbpath().children();
    final StringList list = new StringList(children.length);
    final HashSet<String> map = new HashSet<String>(children.length);
    for(final IOFile f : children) {
      final String fn = f.name();
      String add = null;
      if(backup && fn.endsWith(IO.ZIPSUFFIX)) {
        final String nn = ZIPPATTERN.split(fn)[0];
        if(!nn.equals(fn)) add = nn;
      } else if(db && f.isDir() && fn.indexOf('.') == -1) {
        add = fn;
      }
      // add entry if it matches the pattern, and has not already been added
      if(add != null && (pt == null || pt.matcher(add).matches()) && map.add(add)) {
        list.add(add);
      }
    }
    return list.sort(false);
  }

  /**
   * Returns the paths of all backups of the specified database.
   * @param db database
   * @param ctx database context
   * @return paths of available backups
   */
  public static StringList backupPaths(final String db, final Context ctx) {
    final StringList sl = new StringList();
    final String regex = db.replaceAll("([" + REGEXCHARS + "])", "\\\\$1") +
        DateTime.PATTERN + IO.ZIPSUFFIX;
    for(final IOFile f : ctx.globalopts.dbpath().children()) {
      if(f.name().matches(regex)) sl.add(f.path());
    }
    return sl;
  }

  /**
   * Checks if the specified character is a valid character for a database name.
   * @param ch the character to be checked
   * @return result of check
   */
  public static boolean validChar(final int ch) {
    return Token.letterOrDigit(ch) || DBCHARS.indexOf(ch) != -1;
  }

  /**
   * Checks if the specified string is a valid database name.
   * @param name name to be checked
   * @return result of check
   */
  public static boolean validName(final String name) {
    return validName(name, false);
  }

  /**
   * Checks if the specified string is a valid database name.
   * @param name name to be checked
   * @param glob allow glob syntax
   * @return result of check
   */
  public static boolean validName(final String name, final boolean glob) {
    if(name == null) return false;
    final int nl = name.length();
    for(int n = 0; n < nl; n++) {
      final char ch = name.charAt(n);
      if((!glob || ch != '?' && ch != '*' && ch != ',') && !validChar(ch)) return false;
    }
    return nl != 0;
  }
}
