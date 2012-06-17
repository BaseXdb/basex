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

  /** Database path. */
  public static final Object[] DBPATH = { "DBPATH",
    Prop.HOME + (USERHOME ? Prop.NAME + "Data" : "data") };
  /** HTTP path. */
  public static final Object[] HTTPPATH = { "HTTPPATH",
    Prop.HOME + (USERHOME ? Prop.NAME + "HTTP" : "http") };
  /** Package repository path. */
  public static final Object[] REPOPATH = { "REPOPATH",
    Prop.HOME + (USERHOME ? Prop.NAME + "Repo" : "repo") };

  /** Language name. */
  public static final Object[] LANG = { "LANG", Prop.language };
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

  /** Server: proxy host. */
  public static final Object[] PROXYHOST = { "PROXYHOST", "" };
  /** Server: proxy port. */
  public static final Object[] PROXYPORT = { "PROXYPORT", 80 };
  /** Server: non-proxy host. */
  public static final Object[] NONPROXYHOSTS = { "NONPROXYHOSTS", "" };

  /** Timeout (seconds) for processing client requests; deactivated if set to 0. */
  public static final Object[] TIMEOUT = { "TIMEOUT", 0 };
  /** Keep alive time of clients; deactivated if set to 0. */
  public static final Object[] KEEPALIVE = { "KEEPALIVE", 0 };
  /** Debug mode. */
  public static final Object[] DEBUG = { "DEBUG", false };
  /** Defines the number of parallel readers. */
  public static final Object[] PARALLEL = { "PARALLEL", 8 };

  /**
   * Constructor, reading properties from disk.
   */
  MainProp() {
    read("");
    finish();
  }

  /**
   * Constructor, assigning the specified properties.
   * @param map initial properties
   */
  MainProp(final HashMap<String, String> map) {
    for(final Map.Entry<String, String> entry : map.entrySet()) {
      set(entry.getKey(), entry.getValue());
    }
    finish();
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

  @Override
  protected void finish() {
    // set some static properties
    Prop.language = get(LANG);
    Prop.langkeys = is(LANGKEYS);
    Prop.debug = is(DEBUG);
    final String ph = get(PROXYHOST);
    final String pp = Integer.toString(num(PROXYPORT));
    System.setProperty("http.proxyHost", ph);
    System.setProperty("http.proxyPort", pp);
    System.setProperty("https.proxyHost", ph);
    System.setProperty("https.proxyPort", pp);
    System.setProperty("http.nonProxyHosts", get(NONPROXYHOSTS));
  }
}
