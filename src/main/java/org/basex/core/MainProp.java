package org.basex.core;

import java.io.File;
import java.util.Random;

import org.basex.io.IOFile;
import org.basex.util.Util;

/**
 * This class assembles admin properties which are used all around the project.
 * They are also stored in the project's home directory.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class MainProp extends AProp {
  /** Home path for project directories. */
  private static final boolean USERHOME = Prop.HOME.equals(Prop.USERHOME);

  /** Database path. */
  public static final Object[] DBPATH = { "DBPATH",
    Prop.HOME + (USERHOME ? Text.NAME + "Data" : "data") };
  /** HTTP path. */
  public static final Object[] HTTPPATH = { "HTTPPATH",
    Prop.HOME + (USERHOME ? Text.NAME + "HTTP" : "http") };
  /** Package repository path. */
  public static final Object[] REPOPATH = { "REPOPATH",
    Prop.HOME + (USERHOME ? Text.NAME + "Repo" : "repo") };

  /** Language name. */
  public static final Object[] LANG = { "LANG", Text.LANGUAGE };
  /** Flag to include key names in the language strings. */
  public static final Object[] LANGKEYS = { "LANGKEYS", false };

  /** Server: host, used for connecting new clients. */
  public static final Object[] HOST = { "HOST", Text.LOCALHOST };
  /** Server: port, used for connecting new clients. */
  public static final Object[] PORT = { "PORT", 1984 };
  /** Server: host, used for binding the server. Empty
   * string for wildcard.*/
  public static final Object[] SERVERHOST = { "SERVERHOST", "" };
  /** Server: port, used for binding the server. */
  public static final Object[] SERVERPORT = { "SERVERPORT", 1984 };
  /** Server: port, used for sending events. */
  public static final Object[] EVENTPORT = { "EVENTPORT", 1985 };
  /** Server: port, used for starting the HTTP server. */
  public static final Object[] HTTPPORT = { "HTTPPORT", 8984 };
  /** Server: port, used for stopping the HTTP server. */
  public static final Object[] STOPPORT = { "STOPPORT", 8985 };

  /** Timeout for processing client requests; deactivated if set to 0. */
  public static final Object[] TIMEOUT = { "TIMEOUT", 0 };
  /** Keep alive time of clients; deactivated if set to 0. */
  public static final Object[] KEEPALIVE = { "KEEPALIVE", 0 };
  /** Debug mode. */
  public static final Object[] DEBUG = { "DEBUG", false };
  /** Defines the number of parallel readers. */
  public static final Object[] PARALLEL = { "PARALLEL", 8 };

  /**
   * Constructor.
   */
  public MainProp() {
    super("");
    finish();
  }

  /**
   * Returns a file instance for the specified database.
   * @param db name of the database
   * @return database filename
   */
  public File dbpath(final String db) {
    return new File(get(DBPATH), db);
  }

  /**
   * Returns a random temporary name for the specified database.
   * @param db name of database
   * @return random name
   */
  public String random(final String db) {
    String nm;
    do {
      nm = db + '_' + new Random().nextInt(0x7FFFFFFF);
    } while(dbpath(nm).exists());
    return nm;
  }

  /**
   * Returns the current database path.
   * @return database filename
   */
  public IOFile dbpath() {
    return new IOFile(get(DBPATH));
  }

  /**
   * Checks if the specified database exists.
   * @param db name of the database
   * @return result of check
   */
  public boolean dbexists(final String db) {
    return !db.isEmpty() && dbpath(db).exists();
  }

  @Override
  protected void finish() {
    // set some static properties
    Util.language = get(LANG);
    Util.langkeys = is(LANGKEYS);
    Util.debug = is(DEBUG);
  }
}
