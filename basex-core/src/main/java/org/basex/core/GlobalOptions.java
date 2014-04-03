package org.basex.core;

import static org.basex.util.Prop.*;

import java.util.*;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * This class defines options which are used all around the project.
 * They are also stored in the project's home directory.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class GlobalOptions extends Options {
  /** Indicates if the user's home directory has been chosen as home directory. */
  private static final boolean USERHOME = Prop.HOME.equals(Prop.USERHOME);

  /** Comment: written to options file. */
  public static final Comment C_GENERAL = new Comment("General Options");

  /** Debug mode. */
  public static final BooleanOption DEBUG = new BooleanOption("DEBUG", false);
  /** Database path. */
  public static final StringOption DBPATH = new StringOption("DBPATH",
    HOME + (USERHOME ? NAME + "Data" : "data"));
  /** Package repository path. */
  public static final StringOption REPOPATH = new StringOption("REPOPATH",
    HOME + (USERHOME ? NAME + "Repo" : "repo"));
  /** Language name. */
  public static final StringOption LANG = new StringOption("LANG", language);
  /** Flag to include key names in the language strings. */
  public static final BooleanOption LANGKEYS = new BooleanOption("LANGKEYS", false);
  /** Applied locking algorithm: local (database) vs. global (process) locking. */
  public static final BooleanOption GLOBALLOCK = new BooleanOption("GLOBALLOCK", false);

  /** Comment: written to options file. */
  public static final Comment C_CLIENT = new Comment("Client/Server Architecture");

  /** Server: host, used for connecting new clients. */
  public static final StringOption HOST = new StringOption("HOST", Text.S_LOCALHOST);
  /** Server: port, used for connecting new clients. */
  public static final NumberOption PORT = new NumberOption("PORT", 1984);
  /** Server: port, used for binding the server. */
  public static final NumberOption SERVERPORT = new NumberOption("SERVERPORT", 1984);
  /** Server: port, used for sending events. */
  public static final NumberOption EVENTPORT = new NumberOption("EVENTPORT", 1985);
  /** Default user. */
  public static final StringOption USER = new StringOption("USER", "");
  /** Default password. */
  public static final StringOption PASSWORD = new StringOption("PASSWORD", "");

  /** Server: host, used for binding the server. Empty string for wildcard.*/
  public static final StringOption SERVERHOST = new StringOption("SERVERHOST", "");
  /** Server: proxy host. */
  public static final StringOption PROXYHOST = new StringOption("PROXYHOST", "");
  /** Server: proxy port. */
  public static final NumberOption PROXYPORT = new NumberOption("PROXYPORT", 80);
  /** Server: non-proxy host. */
  public static final StringOption NONPROXYHOSTS = new StringOption("NONPROXYHOSTS", "");

  /** Timeout (seconds) for processing client requests; deactivated if set to 0. */
  public static final NumberOption TIMEOUT = new NumberOption("TIMEOUT", 30);
  /** Keep alive time of clients; deactivated if set to 0. */
  public static final NumberOption KEEPALIVE = new NumberOption("KEEPALIVE", 600);
  /** Defines the number of parallel readers. */
  public static final NumberOption PARALLEL = new NumberOption("PARALLEL", 8);
  /** Logging flag. */
  public static final BooleanOption LOG = new BooleanOption("LOG", true);
  /** Log message cut-off. */
  public static final NumberOption LOGMSGMAXLEN = new NumberOption("LOGMSGMAXLEN", 1000);

  /** Comment: written to options file. */
  public static final Comment C_HTTP = new Comment("HTTP Services");

  /** Web path. */
  public static final StringOption WEBPATH = new StringOption("WEBPATH",
    HOME + (USERHOME ? NAME + "Web" : "webapp"));
  /** RESTXQ path (relative to web path). */
  public static final StringOption RESTXQPATH = new StringOption("RESTXQPATH", "");
  /** Local (embedded) mode. */
  public static final BooleanOption HTTPLOCAL = new BooleanOption("HTTPLOCAL", false);
  /** Port for stopping the web server. */
  public static final NumberOption STOPPORT = new NumberOption("STOPPORT", 8985);

  /**
   * Constructor, adopting system properties starting with "org.basex.".
   * @param file if {@code true}, options will be read from disk
   */
  GlobalOptions(final boolean file) {
    super(file ? new IOFile(HOME, IO.BASEXSUFFIX) : null);
    setSystem();

    // set some static options
    language = get(LANG);
    langkeys = get(LANGKEYS);
    debug = get(DEBUG);
    final String ph = get(PROXYHOST);
    final String pp = Integer.toString(get(PROXYPORT));
    setSystem("http.proxyHost", ph);
    setSystem("http.proxyPort", pp);
    setSystem("https.proxyHost", ph);
    setSystem("https.proxyPort", pp);
    setSystem("http.nonProxyHosts", get(NONPROXYHOSTS));
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
