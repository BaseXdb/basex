package org.basex.core;

import java.util.*;

import org.basex.io.*;
import org.basex.util.*;

/**
 * This class defines options which are used all around the project.
 * They are also stored in the project's home directory.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class GlobalOptions extends AOptions {
  /** Indicates if the user's home directory has been chosen as home directory. */
  private static final boolean USERHOME = Prop.HOME.equals(Prop.USERHOME);

  /** Comment: written to options file. */
  public static final Option C_GENERAL = new Option("General Options");

  /** Database path. */
  public static final Option DBPATH = new Option("DBPATH",
    Prop.HOME + (USERHOME ? Prop.NAME + "Data" : "data"));
  /** Package repository path. */
  public static final Option REPOPATH = new Option("REPOPATH",
    Prop.HOME + (USERHOME ? Prop.NAME + "Repo" : "repo"));
  /** Debug mode. */
  public static final Option DEBUG = new Option("DEBUG", false);
  /** Language name. */
  public static final Option LANG = new Option("LANG", Prop.language);
  /** Flag to include key names in the language strings. */
  public static final Option LANGKEYS = new Option("LANGKEYS", false);
  /** Applied locking algorithm: local (database) vs. global (process) locking. */
  public static final Option GLOBALLOCK = new Option("GLOBALLOCK", false);

  /** Comment: written to options file. */
  public static final Option C_CLIENT = new Option("Client/Server Architecture");

  /** Server: host, used for connecting new clients. */
  public static final Option HOST = new Option("HOST", Text.LOCALHOST);
  /** Server: port, used for connecting new clients. */
  public static final Option PORT = new Option("PORT", 1984);
  /** Server: port, used for binding the server. */
  public static final Option SERVERPORT = new Option("SERVERPORT", 1984);
  /** Server: port, used for sending events. */
  public static final Option EVENTPORT = new Option("EVENTPORT", 1985);
  /** Default user. */
  public static final Option USER = new Option("USER", "");
  /** Default password. */
  public static final Option PASSWORD = new Option("PASSWORD", "");

  /** Server: host, used for binding the server. Empty string for wildcard.*/
  public static final Option SERVERHOST = new Option("SERVERHOST", "");
  /** Server: proxy host. */
  public static final Option PROXYHOST = new Option("PROXYHOST", "");
  /** Server: proxy port. */
  public static final Option PROXYPORT = new Option("PROXYPORT", 80);
  /** Server: non-proxy host. */
  public static final Option NONPROXYHOSTS = new Option("NONPROXYHOSTS", "");

  /** Timeout (seconds) for processing client requests; deactivated if set to 0. */
  public static final Option TIMEOUT = new Option("TIMEOUT", 30);
  /** Keep alive time of clients; deactivated if set to 0. */
  public static final Option KEEPALIVE = new Option("KEEPALIVE", 600);
  /** Defines the number of parallel readers. */
  public static final Option PARALLEL = new Option("PARALLEL", 8);
  /** Logging flag. */
  public static final Option LOG = new Option("LOG", true);
  /** Log message cut-off. */
  public static final Option LOGMSGMAXLEN = new Option("LOGMSGMAXLEN", 1000);

  /** Comment: written to options file. */
  public static final Option C_HTTP = new Option("HTTP Services");

  /** Web path. */
  public static final Option WEBPATH = new Option("WEBPATH",
    Prop.HOME + (USERHOME ? Prop.NAME + "Web" : "webapp"));
  /** RESTXQ path (relative to web path). */
  public static final Option RESTXQPATH = new Option("RESTXQPATH", "");
  /** Local (embedded) mode. */
  public static final Option HTTPLOCAL = new Option("HTTPLOCAL", false);
  /** Port for stopping the web server. */
  public static final Option STOPPORT = new Option("STOPPORT", 8985);

  /**
   * Constructor, adopting system properties starting with "org.basex.".
   * @param file if {@code true}, options will be read from disk
   */
  GlobalOptions(final boolean file) {
    super(file ? "" : null);
    // set some static options
    Prop.language = get(LANG);
    Prop.langkeys = is(LANGKEYS);
    Prop.debug = is(DEBUG);
    final String ph = get(PROXYHOST);
    final String pp = Integer.toString(num(PROXYPORT));
    AOptions.setSystem("http.proxyHost", ph);
    AOptions.setSystem("http.proxyPort", pp);
    AOptions.setSystem("https.proxyHost", ph);
    AOptions.setSystem("https.proxyPort", pp);
    AOptions.setSystem("http.nonProxyHosts", get(NONPROXYHOSTS));
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
