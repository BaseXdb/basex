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
  /** Pattern to exclude locking files from database transfer operations. */
  public static final Pattern FILES = Pattern.compile(".{3,5}" + IO.BASEXSUFFIX);
  /** Pattern to extract the database name from a backup file name. */
  private static final Pattern ZIPPATTERN =
      Pattern.compile(DateTime.PATTERN + IO.ZIPSUFFIX + '$');
  /** Regex indicator. */
  private static final Pattern REGEX = Pattern.compile(".*[*?,].*");

  /** Main properties. */
  final MainProp mprop;

  /**
   * Creates a new instance and loads available databases.
   * @param c Database context
   */
  Databases(final Context c) {
    mprop = c.mprop;
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
    for(final IOFile f : mprop.dbpath().children()) {
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
    final Pattern pt = name == null ? null : Pattern.compile(
        REGEX.matcher(name).matches() ? IOFile.regex(name) : name,
        Prop.CASE ? 0 : Pattern.CASE_INSENSITIVE);

    final IOFile[] children = mprop.dbpath().children();
    final StringList list = new StringList(children.length);
    final HashSet<String> map = new HashSet<String>(children.length);
    for(final IOFile f : children) {
      final String fn = f.name();
      String add = null;
      if(backup && fn.endsWith(IO.ZIPSUFFIX)) {
        add = ZIPPATTERN.split(fn)[0];
      } else if(db && f.isDir() && fn.indexOf('.') == -1) {
        add = fn;
      }
      // add entry if it matches the pattern, and has not already been added
      if(add != null && (pt == null || pt.matcher(add).matches()) && map.add(add)) {
        list.add(add);
      }
    }
    return list.sort(false, true);
  }

  /**
   * Returns the sorted paths of all backups of the specified database.
   * @param db database
   * @param ctx database context
   * @return paths of available backups
   */
  public static StringList backupPaths(final String db, final Context ctx) {
    final StringList sl = new StringList();
    for(final IOFile f : ctx.mprop.dbpath().children()) {
      final String name = f.name();
      if(name.matches(db + DateTime.PATTERN + IO.ZIPSUFFIX)) sl.add(f.path());
    }
    return sl.sort(false, false);
  }
}
