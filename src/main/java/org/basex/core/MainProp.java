package org.basex.core;

import java.util.*;

import org.basex.io.*;

/**
 * This class assembles admin properties which are used all around the project.
 * They are also stored in the project's home directory.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class MainProp extends AProp {
  /** Indicates if the user's home directory has been chosen as home directory. */
  private static final boolean USERHOME = Prop.HOME.equals(Prop.USERHOME);

  /** Comment: written to property file. */
  public static final Object[] C_GENERAL = { "General Options" };

  /** Database path. */
  public static final Object[] DBPATH = { "DBPATH",
    Prop.HOME + (USERHOME ? Prop.NAME + "Data" : "data") };
  /** Package repository path. */
  public static final Object[] REPOPATH = { "REPOPATH",
    Prop.HOME + (USERHOME ? Prop.NAME + "Repo" : "repo") };
  /** Debug mode. */
  public static final Object[] DEBUG = { "DEBUG", false };
  /** Language name. */
  public static final Object[] LANG = { "LANG", Prop.language };
  /** Flag to include key names in the language strings. */
  public static final Object[] LANGKEYS = { "LANGKEYS", false };

  /** Comment: written to property file. */
  public static final Object[] C_CLIENT = { "Client/Server Architecture" };

  /** Server: host, used for connecting new clients. */
  public static final Object[] HOST = { "HOST", Text.LOCALHOST };
  /** Server: port, used for connecting new clients. */
  public static final Object[] PORT = { "PORT", 1984 };
  /** Server: port, used for binding the server. */
  public static final Object[] SERVERPORT = { "SERVERPORT", 1984 };
  /** Server: port, used for sending events. */
  public static final Object[] EVENTPORT = { "EVENTPORT", 1985 };
  /** Default user. */
  public static final Object[] USER = { "USER", "" };
  /** Default password. */
  public static final Object[] PASSWORD = { "PASSWORD", "" };

  /** Server: host, used for binding the server. Empty string for wildcard.*/
  public static final Object[] SERVERHOST = { "SERVERHOST", "" };
  /** Server: proxy host. */
  public static final Object[] PROXYHOST = { "PROXYHOST", "" };
  /** Server: proxy port. */
  public static final Object[] PROXYPORT = { "PROXYPORT", 80 };
  /** Server: non-proxy host. */
  public static final Object[] NONPROXYHOSTS = { "NONPROXYHOSTS", "" };

  /** Timeout (seconds) for processing client requests; deactivated if set to 0. */
  public static final Object[] TIMEOUT = { "TIMEOUT", 30 };
  /** Keep alive time of clients; deactivated if set to 0. */
  public static final Object[] KEEPALIVE = { "KEEPALIVE", 600 };
  /** Defines the number of parallel readers. */
  public static final Object[] PARALLEL = { "PARALLEL", 8 };
  /** Logging flag. */
  public static final Object[] LOG = { "LOG", true };
  /** Log message cut-off. */
  public static final Object[] LOGMSGMAXLEN = { "LOGMSGMAXLEN", 1000 };

  /** Comment: written to property file. */
  public static final Object[] C_HTTP = { "HTTP Services" };

  /** Web path. */
  public static final Object[] WEBPATH = { "WEBPATH",
    Prop.HOME + (USERHOME ? Prop.NAME + "Web" : "webapp") };
  /** RESTXQ path (relative to web path). */
  public static final Object[] RESTXQPATH = { "RESTXQPATH", "" };
  /** Local (embedded) mode. */
  public static final Object[] HTTPLOCAL = { "HTTPLOCAL", false };
  /** Port for stopping the web server. */
  public static final Object[] STOPPORT = { "STOPPORT", 8985 };

  /** Comment: written to property file. */
  public static final Object[] C_EXP = { "Experimental Options" };

  /** Hidden option: defines the locking algorithm (process vs. database locking);
   *  will be removed as soon as database locking is stable. */
  public static final Object[] DBLOCKING = { "DBLOCKING", false };

  /**
   * Constructor, adopting system properties starting with "org.basex.".
   * @param file if {@code true}, properties will also be read from disk
   */
  MainProp(final boolean file) {
    super(file ? "" : null);
    // set some static properties
    Prop.language = get(LANG);
    Prop.langkeys = is(LANGKEYS);
    Prop.debug = is(DEBUG);
    final String ph = get(PROXYHOST);
    final String pp = Integer.toString(num(PROXYPORT));
    Prop.setSystem("http.proxyHost", ph);
    Prop.setSystem("http.proxyPort", pp);
    Prop.setSystem("https.proxyHost", ph);
    Prop.setSystem("https.proxyPort", pp);
    Prop.setSystem("http.nonProxyHosts", get(NONPROXYHOSTS));
  }

  /**
   * Returns a reference to a database directory.
   * @param db name of the database
   * @return database directory
   */
  public IOFile dbpath(final String db) {
    return new IOFile(get(DBPATH), db);
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
}
