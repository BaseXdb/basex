package org.basex.core;

import java.util.*;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * This class defines options which are used all around the project.
 * The initial keys and values are also stored in the project's home directory.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class StaticOptions extends Options {
  /** Comment: written to options file. */
  public static final Comment C_GENERAL = new Comment("General Options");

  /** Debug mode. */
  public static final BooleanOption DEBUG = new BooleanOption("DEBUG", false);
  /** Database path. */
  public static final StringOption DBPATH = new StringOption("DBPATH", Prop.HOMEDIR + "data");
  /** Log path (relative to database path). */
  public static final StringOption LOGPATH = new StringOption("LOGPATH", ".logs");
  /** Package repository path. */
  public static final StringOption REPOPATH = new StringOption("REPOPATH", Prop.HOMEDIR + "repo");
  /** Language name. */
  public static final StringOption LANG = new StringOption("LANG", Prop.language);
  /** Flag to include key names in the language strings. */
  public static final BooleanOption LANGKEYS = new BooleanOption("LANGKEYS", false);
  /** Locking strategy. */
  public static final BooleanOption FAIRLOCK = new BooleanOption("FAIRLOCK", false);
  /** Timeout (seconds) for remembering result of asynchronous queries. */
  public static final NumberOption CACHETIMEOUT = new NumberOption("CACHETIMEOUT", 3600);

  /** Comment: written to options file. */
  public static final Comment C_CLIENT = new Comment("Client/Server Architecture");

  /** Server: host, used for connecting new clients. */
  public static final StringOption HOST = new StringOption("HOST", Text.S_LOCALHOST);
  /** Server: port, used for connecting new clients. */
  public static final NumberOption PORT = new NumberOption("PORT", 1984);
  /** Server: port, used for binding the server. */
  public static final NumberOption SERVERPORT = new NumberOption("SERVERPORT", 1984);
  /** Default user. */
  public static final StringOption USER = new StringOption("USER", "");
  /** Default password. */
  public static final StringOption PASSWORD = new StringOption("PASSWORD", "");
  /** Server: host, used for binding the server. Empty string for wildcard. */
  public static final StringOption SERVERHOST = new StringOption("SERVERHOST", "");
  /** Server: proxy host (default: ignored). */
  public static final StringOption PROXYHOST = new StringOption("PROXYHOST", "");
  /** Server: proxy port (default: ignored). */
  public static final NumberOption PROXYPORT = new NumberOption("PROXYPORT", 0);
  /** Server: non-proxy host. */
  public static final StringOption NONPROXYHOSTS = new StringOption("NONPROXYHOSTS", "");
  /** Ignore missing certificates. */
  public static final BooleanOption IGNORECERT = new BooleanOption("IGNORECERT", false);
  /** Ignore verification of hostname in certificates. */
  public static final BooleanOption IGNOREHOSTNAME = new BooleanOption("IGNOREHOSTNAME", false);

  /** Timeout (seconds) for processing client requests; deactivated if set to 0. */
  public static final NumberOption TIMEOUT = new NumberOption("TIMEOUT", 30);
  /** Keep alive time (seconds) for clients; deactivated if set to 0. */
  public static final NumberOption KEEPALIVE = new NumberOption("KEEPALIVE", 600);
  /** Defines the number of parallel readers. */
  public static final NumberOption PARALLEL = new NumberOption("PARALLEL", 8);
  /** Logging flag. */
  public static final BooleanOption LOG = new BooleanOption("LOG", true);
  /** Log message cut-off. */
  public static final NumberOption LOGMSGMAXLEN = new NumberOption("LOGMSGMAXLEN", 1000);
  /** Write trace output to logs. */
  public static final BooleanOption LOGTRACE = new BooleanOption("LOGTRACE", true);

  /** Comment: written to options file. */
  public static final Comment C_HTTP = new Comment("HTTP Services");

  /** Web path (cannot be specified in web.xml). */
  public static final StringOption WEBPATH = new StringOption("WEBPATH", Prop.HOMEDIR + "webapp");
  /** Enable GZIP support (cannot be specified in web.xml). */
  public static final BooleanOption GZIP = new BooleanOption("GZIP", false);

  /** REST path (relative to web path). */
  public static final StringOption RESTPATH = new StringOption("RESTPATH", "");
  /** RESTXQ path (relative to web path). */
  public static final StringOption RESTXQPATH = new StringOption("RESTXQPATH", "");
  /** Minimum timeout for parsing RESTXQ files. */
  public static final NumberOption PARSERESTXQ = new NumberOption("PARSERESTXQ", 3);
  /** Show errors in RESTXQ directory. */
  public static final BooleanOption RESTXQERRORS = new BooleanOption("RESTXQERRORS", true);
  /** Local (embedded) mode. */
  public static final BooleanOption HTTPLOCAL = new BooleanOption("HTTPLOCAL", false);
  /** Port for stopping the web server. */
  public static final NumberOption STOPPORT = new NumberOption("STOPPORT", 8985);
  /** Default authentication method. */
  public static final EnumOption<AuthMethod> AUTHMETHOD =
      new EnumOption<>("AUTHMETHOD", AuthMethod.BASIC);

  /** Authorization method. */
  public enum AuthMethod {
    /** Basic.  */ BASIC,
    /** Digest. */ DIGEST,
    /** Custom. */ CUSTOM;

    @Override
    public String toString() {
      final String name = name();
      return name.charAt(0) + name.substring(1).toLowerCase(Locale.ENGLISH);
    }
  }

  /**
   * Constructor, adopting system properties starting with "org.basex.".
   * @param file if {@code true}, options will be read from disk
   */
  public StaticOptions(final boolean file) {
    super(file ? new IOFile(Prop.HOMEDIR, IO.BASEXSUFFIX) : null);
    setSystem();
  }

  @Override
  public void setSystem() {
    super.setSystem();

    // assigns static variables and system properties
    Prop.language = get(LANG);
    Prop.langkeys = get(LANGKEYS);
    Prop.debug = get(DEBUG);
    final String ph = get(PROXYHOST);
    if(!ph.isEmpty()) {
      Prop.setSystem("http.proxyHost", ph);
      Prop.setSystem("https.proxyHost", ph);
    }
    final String pp = Integer.toString(get(PROXYPORT));
    if(!pp.equals("0")) {
      Prop.setSystem("http.proxyPort", pp);
      Prop.setSystem("https.proxyPort", pp);
    }
    final String nph = get(NONPROXYHOSTS);
    if(!nph.isEmpty()) {
      Prop.setSystem("http.nonProxyHosts", nph);
    }
    if(get(IGNORECERT)) IOUrl.ignoreCert();
    if(get(IGNOREHOSTNAME)) IOUrl.ignoreHostname();
  }

  /**
   * Returns a reference to a file or database in the database directory.
   * @param name name of the file or database
   * @return database directory
   */
  public IOFile dbPath(final String name) {
    return new IOFile(get(DBPATH), name);
  }

  /**
   * Creates a temporary database directory and returns its name.
   * @param name name of the original database
   * @return name of random database
   */
  public String createTempDb(final String name) {
    String db;
    int c = 0;
    do {
      db = name + '.' + c++;
      final IOFile io = dbPath(db);
      if(!io.exists()) {
        io.md();
        return db;
      }
    } while(true);
  }

  /**
   * Returns the path to the directory that contains all databases.
   * @return database filename
   */
  public IOFile dbPath() {
    return new IOFile(get(DBPATH));
  }

  /**
   * Checks if the specified database exists.
   * @param db name of the database
   * @return result of check
   */
  public boolean dbExists(final String db) {
    return !db.isEmpty() && dbPath(db).exists();
  }
}
