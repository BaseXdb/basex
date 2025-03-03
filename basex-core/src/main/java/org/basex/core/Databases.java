package org.basex.core;

import java.util.*;
import java.util.regex.*;

import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Provides central access to all databases and backups.
 *
 * @author BaseX Team, BSD License
 * @author Jens Erat
 */
public final class Databases {
  /** Allowed characters for database names (additional to letters and digits).
   * The following characters are invalid:
   * <ul>
   *   <li> {@code ,?*}" are used by the glob syntax</li>
   *   <li> {@code ;} is reserved for separating commands.</li>
   *   <li> {@code :*?\"<>\/|}" are used for filenames and paths</li>
   * </ul>
   */
  public static final String DBCHARS = "-+=~!#$%^&()[]{}@'`";

  /** Date pattern. */
  private static final String DATE = "\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}";
  /** Regex representation of allowed database characters. */
  private static final String REGEXCHARS = DBCHARS.replaceAll("(.)", "\\\\$1");
  /** Pattern to extract the database name from a backup file name. */
  private static final Pattern ZIPPATTERN = Pattern.compile('-' + DATE + '\\' + IO.ZIPSUFFIX + '$');
  /** Regex indicator. */
  private static final Pattern REGEX = Pattern.compile(".*[*?,].*");

  /** Static options. */
  private final StaticOptions soptions;

  /**
   * Creates a new instance and loads available databases.
   * @param soptions static options
   */
  Databases(final StaticOptions soptions) {
    this.soptions = soptions;
  }

  /**
   * Returns the sorted names of all databases and backups.
   * @return databases and backups
   */
  public StringList all() {
    return list(null, null, true);
  }

  /**
   * Returns the sorted names of all databases.
   * @return databases and backups
   */
  public StringList list() {
    return list(null, null, false);
  }

  /**
   * Returns the sorted names of all databases.
   * @param user accessing user
   * @param pattern database pattern (can be {@code null})
   * @return databases
   */
  public StringList list(final User user, final String pattern) {
    return list(user, pattern, false);
  }

  /**
   * Returns the sorted names of all databases and, optionally, backups.
   * @param user accessing user (can be {@code null})
   * @param pattern database pattern (can be {@code null})
   * @param all include names of backed up databases
   * @return databases and, optionally, backups
   */
  private StringList list(final User user, final String pattern, final boolean all) {
    final Pattern pt = pattern == null ? null : regex(pattern);
    final StringList list = new StringList();
    final HashSet<String> map = new HashSet<>();
    for(final IOFile file : soptions.dbPath().children()) {
      final String name = file.name();
      String entry = null;
      if(all && name.endsWith(IO.ZIPSUFFIX)) {
        final String[] split = ZIPPATTERN.split(name);
        if(split.length > 0 && !split[0].equals(name)) entry = split[0];
      } else if(file.isDir() && !Strings.startsWith(name, '.')) {
        entry = name;
      }
      // add entry if it has not already been added, matches the pattern, and is accessible
      if(entry != null && map.add(entry) && (pt == null || pt.matcher(entry).matches()) &&
          (user == null || user.has(Perm.READ, entry))) {
        list.add(entry);
      }
    }
    return list.sort(false);
  }

  /**
   * Returns the names of all backups.
   * @return backups
   */
  public StringList backups() {
    final StringList backups = new StringList();
    for(final IOFile file : soptions.dbPath().children()) {
      final String name = file.name();
      if(name.endsWith(IO.ZIPSUFFIX)) backups.add(name.substring(0, name.lastIndexOf('.')));
    }
    return backups;
  }

  /**
   * Returns the name of a specific backup, or all backups found for a specific database,
   * in descending order.
   * @param name name of backup with or without date suffix (empty string for general data)
   * @return names of the backups
   */
  public StringList backups(final String name) {
    final StringList backups = new StringList();
    final IOFile path = soptions.dbPath(name + IO.ZIPSUFFIX);
    if(path.exists()) {
      backups.add(name);
    } else {
      final Pattern regex = regex(name, '-' + DATE + '\\' + IO.ZIPSUFFIX);
      for(final IOFile file : soptions.dbPath().children()) {
        final String n = file.name();
        if(regex.matcher(n).matches()) backups.add(n.substring(0, n.lastIndexOf('.')));
      }
    }
    return backups.sort(Prop.CASE, false);
  }

  /**
   * Returns a regular expression for the specified name pattern.
   * @param pattern pattern
   * @return regular expression
   */
  public static Pattern regex(final String pattern) {
    return regex(pattern, "");
  }

  /**
   * Extracts the name of a database from the name of a backup.
   * @param backup name of the backup (empty string for general data), optionally followed by date
   * @return name of the database (empty string for general data)
   */
  public static String name(final String backup) {
    final String[] strings = Pattern.compile('-' + DATE + '$').split(backup);
    return strings.length > 0 ? strings[0] : "";
  }

  /**
   * Extracts the date of a database from the name of a backup.
   * @param backup name of the backup, including the date
   * @return date string
   */
  public static String date(final String backup) {
    return backup.replaceAll("^.*-(" + DATE + ")$", "$1");
  }

  /**
   * Checks if the specified character is a valid character for a database name.
   * @param ch the character to be checked
   * @param firstLast character is first or last
   * @return result of check
   */
  public static boolean validChar(final int ch, final boolean firstLast) {
    return Token.letterOrDigit(ch) || DBCHARS.indexOf(ch) != -1 || !firstLast && ch == '.';
  }

  /**
   * Checks if the specified string is a valid database name.
   * @param name name to be checked (can be {@code null})
   * @return result of check
   */
  public static boolean validName(final String name) {
    return valid(name, false);
  }

  /**
   * Checks if the specified string is a valid database pattern.
   * @param pattern pattern to be checked (can be {@code null})
   * @return result of check
   */
  public static boolean validPattern(final String pattern) {
    return valid(pattern, true);
  }

  /**
   * Returns a regular expression for the specified name pattern.
   * @param pattern pattern (can be {@code null})
   * @param suffix regular expression suffix
   * @return regular expression or {@code null}
   */
  private static Pattern regex(final String pattern, final String suffix) {
    if(pattern == null) return null;
    final String nm = REGEX.matcher(pattern).matches() ? IOFile.regex(pattern) :
      pattern.replaceAll("([" + REGEXCHARS + "])", "\\\\$1") + suffix;
    return Pattern.compile(nm, Prop.CASE ? 0 : Pattern.CASE_INSENSITIVE);
  }

  /**
   * Checks if the specified string is a valid database name.
   * @param name name to be checked (can be {@code null})
   * @param glob allow glob syntax
   * @return result of check
   */
  private static boolean valid(final String name, final boolean glob) {
    if(name == null) return false;
    final int nl = name.length();
    for(int n = 0; n < nl; n++) {
      final char ch = name.charAt(n);
      if((!glob || ch != '?' && ch != '*' && ch != ',') && !validChar(ch, n == 0 || n + 1 == nl))
        return false;
    }
    return nl != 0;
  }
}
