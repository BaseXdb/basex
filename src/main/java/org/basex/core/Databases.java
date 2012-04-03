package org.basex.core;

import java.util.regex.Pattern;

import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.util.list.StringList;
import org.basex.util.list.TwoWayTokenMap;
import static org.basex.util.Token.*;

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
  public static final Pattern ZIPPATTERN =
      Pattern.compile(IO.DATEPATTERN + IO.ZIPSUFFIX + '$');

  /** Database path. */
  final IOFile dbpath;

  /** Available databases. */
  private final TwoWayTokenMap databases = new TwoWayTokenMap();
  /** Available backups. */
  private final TwoWayTokenMap backups = new TwoWayTokenMap();

  /**
   * Creates a new instance and loads available databases.
   * @param c Database context
   */
  Databases(final Context c) {
    dbpath = c.mprop.dbpath();
    for(final IOFile f : dbpath.children()) {
      final String name = f.name();
      if(name.endsWith(IO.ZIPSUFFIX)) {
        add(ZIPPATTERN.split(name)[0], true);
      } else if(f.isDir() && !name.startsWith(".")) {
        add(name);
      }
    }
  }

  /**
   * Adds a database to the list. If already present, does nothing.
   * @param db name of the database
   */
  public void add(final String db) {
    add(db, false);
  }

  /**
   * Adds a database or backup to the list. If already present, does nothing.
   * @param db database or backup name
   * @param backup is backup?
   */
  public void add(final String db, final boolean backup) {
    final TwoWayTokenMap map = map(backup);
    if(!map.contains(db)) map.add(db);
  }

  /**
   * Renames a database in the list without changing its key.
   * @param oldDB old database name
   * @param newDB new database name
   */
  public void alter(final String oldDB, final String newDB) {
    databases.delete(newDB);
    databases.set(databases.getKey(oldDB), newDB);
  }

  /**
   * Deletes a database from the list.
   * @param db name of the database
   * @return found database?
   */
  public boolean delete(final String db) {
    return delete(db, false);
  }

  /**
   * Deletes a database or backup from the list.
   * @param db database or backup name
   * @param backup is backup?
   * @return found database or backup?
   */
  public boolean delete(final String db, final boolean backup) {
    return map(backup).delete(db) != -1;
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
   * Lists all available backups.
   * @return backup list
   */
  public StringList listBackups() {
    return list(false, true, null);
  }

  /**
   * Lists all available backups.
   * @param name backup name, glob patterns allowed
   * @return backup list
   */
  public StringList listBackups(final String name) {
    return list(false, true, name);
  }

  /**
   * Returns the sorted names of all available databases and, optionally, backups.
   * Filters for {@code name} if not null with glob support.
   * @param db return databases?
   * @param backup return backups?
   * @param name filter for name.
   * @return database and backups list
   */
  private StringList list(final boolean db, final boolean backup, final String name) {
    final Pattern pattern = Pattern.compile(
        null == name ? ".*" : name.matches(".*[*?,].*") ? IOFile.regex(name) : name,
            Prop.WIN ? Pattern.CASE_INSENSITIVE : 0);
    final StringList dbs = new StringList();
    if(db) listAll(databases, dbs, pattern);
    if(backup) listAll(backups, dbs, pattern);
    dbs.sort(false, true);
    return db && backup ? dbs.unique() : dbs;
  }

  /**
   * Adds all contained databases to the specified list. If a pattern is given, filters
   * according to it.
   * @param dbs databases
   * @param list list which contained databases are added to
   * @param pattern match pattern or {@code null}
   */
  private static void listAll(final TwoWayTokenMap dbs, final StringList list,
      final Pattern pattern) {
    for(final byte[] database : dbs) {
      if(null == database) continue;
      final String name = string(database);
      if(null == name || pattern.matcher(name).matches()) {
        list.add(name);
      }
    }
  }

  /**
   * Returns the database or backup map dependent on the backup flag.
   * @param backup is backup?
   * @return matching map
   */
  private TwoWayTokenMap map(final boolean backup) {
    return backup ? backups : databases;
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
      if(name.matches(db + IO.DATEPATTERN + IO.ZIPSUFFIX)) {
        sl.add(f.path());
      }
    }
    sl.sort(false, false);
    return sl;
  }
}
