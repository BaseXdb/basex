package org.basex.core;

import java.util.regex.Pattern;

import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.util.list.StringList;
import org.basex.util.list.TwoWayTokenMap;
import static org.basex.util.Token.*;

/**
 * Manages two-way-map of all available databases. Used for locking.
 *
 * @author Jens Erat
 */
public final class Databases {
  /** Available databases. */
  private final TwoWayTokenMap databases = new TwoWayTokenMap();
  /** Available backups. */
  private final TwoWayTokenMap backups = new TwoWayTokenMap();
  /** Database context. */
  private final Context ctx;
  /** Pattern to extract the database name from a backup file name. */
  private static final Pattern PA =
      Pattern.compile(IO.DATEPATTERN + IO.ZIPSUFFIX + '$');

  /** Create new instance, load available databases.
   * @param c Database context */
  Databases(final Context c) {
    ctx = c;

    for(final IOFile f : ctx.mprop.dbpath().children()) {
      final String name = f.name();
      if(name.endsWith(IO.ZIPSUFFIX)) {
        add(dbname(name), true);
      } else if(f.isDir() && !name.startsWith(".")) {
        add(name);
      }
    }
  }

  /**
   * Add database to list. If already present, do nothing.
   * @param db Database name
   */
  public void add(final String db) {
    add(db, false);
  }

  /**
   * Add database or backup to list. If already present, do nothing.
   * @param db Database or backup name
   * @param backup Is Backup?
   */
  public void add(final String db, final boolean backup) {
    final TwoWayTokenMap map = whichMap(backup);
    if(!map.contains(db)) map.add(db);
  }

  /**
   * Renames a database in list without changing its key.
   * @param oldDB Old database name
   * @param newDB New database name
   */
  public void alter(final String oldDB, final String newDB) {
    databases.delete(newDB);
    databases.set(databases.getKey(oldDB), newDB);
  }

  /**
   * Deletes a database from list.
   * @param db Database name
   * @return Found database?
   */
  public boolean delete(final String db) {
    return delete(db, false);
  }

  /**
   * Deletes a database or backup from list.
   * @param db Database or backup name
   * @param backup Is backup?
   * @return Found database or Backup?
   */
  public boolean delete(final String db, final boolean backup) {
    return whichMap(backup).delete(db) != -1;
  }

  /**
   * Lists all available databases and backups.
   * @return Database and backup list
   */
  public StringList list() {
    return list(true, true, null);
  }

  /**
   * Lists all available databases.
   * @return Database list
   */
  public StringList listDBs() {
    return list(true, false, null);
  }

  /**
   * Lists all available databases matching name. Glob patterns supported.
   * @param name Database name, glob patterns allowed
   * @return Database list
   */
  public StringList listDBs(final String name) {
    return list(true, false, name);
  }

  /**
   * Lists all available backups.
   * @return Backup list
   */
  public StringList listBackups() {
    return list(false, true, null);
  }

  /**
   * Lists all available backups.
   * @param name Backup name, glob patterns allowed
   * @return Backup list
   */
  public StringList listBackups(final String name) {
    return list(false, true, name);
  }

  /**
   * Lists all available databases and optionally backups. Filters for
   * {@code name} if not null with glob support.
   * @param db Return databases?
   * @param backup Return backups?
   * @param name Filter for name.
   * @return Database and Backups list
   */
  public StringList list(final boolean db, final boolean backup,
      final String name) {
    final Pattern pattern = Pattern.compile(
        null == name ? ".*" :
          name.matches(".*[*?,].*") ? IOFile.regex(name) : name,
              Prop.WIN ? Pattern.CASE_INSENSITIVE : 0);
    final StringList dbs = new StringList();
    if(db) listAll(databases, dbs, pattern);
    if(backup) listAll(backups, dbs, pattern);
    dbs.sort(false, true);
    if(db && backup) return StringList.unique(dbs);
    return dbs;
  }

  /**
   * Adds all contained databases to StringList. If pattern is given, filter
   * according to it.
   * @param dbs Databases
   * @param list List which contained databases are added to
   * @param pattern Match pattern or {@code null}
   */
  private void listAll(final TwoWayTokenMap dbs, final StringList list,
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
   * Returns database or backup map depended on backup flag.
   * @param backup Is Backup?
   * @return Matching map.
   */
  private TwoWayTokenMap whichMap(final boolean backup) {
    return backup ? backups : databases;
  }

  /**
   * Extracts the name of a database from its backup file.
   * @param s name of backup file
   * @return name of database
   */
  private static String dbname(final String s) {
    return PA.split(s)[0];
  }

  /**
   * Returns paths of all backups of the specified database.
   * @param db database
   * @param ctx database context
   * @param onlyName Only return the file names (delete the path)?
   * @return paths of available backups
   */
  public static StringList listBackupPaths(final String db, final Context ctx,
      final boolean onlyName) {
    final StringList sl = new StringList();
    for(final IOFile f : ctx.mprop.dbpath().children()) {
      final String name = f.name();
      if(name.matches(db + IO.DATEPATTERN + IO.ZIPSUFFIX)) {
        sl.add(onlyName ? f.name() : f.path());
      }
    }
    sl.sort(false, false);
    return sl;
  }
}
