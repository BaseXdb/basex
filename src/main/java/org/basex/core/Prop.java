package org.basex.core;

import java.io.File;
import org.basex.util.Util;

/**
 * This class assembles properties which are used all around the project. They
 * are initially read from and finally written to disk.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Prop extends AProp {

  // CONSTANTS ================================================================

  /** New line string. */
  public static final String NL = System.getProperty("line.separator");
  /** Returns the system's default encoding. */
  public static final String ENCODING = System.getProperty("file.encoding");
  /** Directory separator. */
  public static final String DIRSEP = System.getProperty("file.separator");

  /** System's temporary directory. */
  public static final String TMP =
    (System.getProperty("java.io.tmpdir") + "/").replaceAll("[\\\\/]+", "/");

  /** OS flag (should be ignored whenever possible). */
  public static final String OS = System.getProperty("os.name").toUpperCase();
  /** Flag denoting if OS belongs to Mac family. */
  public static final boolean MAC = OS.startsWith("MAC");
  /** Flag denoting if OS belongs to Windows family. */
  public static final boolean WIN = OS.startsWith("WIN");

  /** Directory for storing the property files, database directory, etc. */
  public static final String HOME = Util.homeDir() + '/';

  // The following properties will be saved to disk:

  /** Property information. */
  static final String PROPHEADER = "# Property File." + NL
      + "# You can set additional options at the end of the file." + NL;
  /** Property information. */
  static final String PROPUSER = "# User defined section";

  // DATABASE & PROGRAM PATHS =================================================

  /** Database path. */
  public static final Object[] DBPATH =
    { "DBPATH", HOME + Text.NAME + "Data" };
  /** Web path. */
  public static final Object[] JAXRXPATH =
    { "JAXRXPATH", HOME + Text.NAME + "Web" };

  /** Language name. */
  public static final Object[] LANG = { "LANG", "English" };
  /** Flag to include key names in the language strings. */
  public static final Object[] LANGKEYS = { "LANGKEYS", false };

  /** Client/server communication: host, used for connecting new clients. */
  public static final Object[] HOST = { "HOST", Text.LOCALHOST };
  /** Client/server communication: port, used for connecting new clients. */
  public static final Object[] PORT = { "PORT", 1984 };
  /** Client/server communication: port, used for starting the server. */
  public static final Object[] SERVERPORT = { "SERVERPORT", 1984 };
  /** Client/server communication: port, used for starting the JAX-RX server. */
  public static final Object[] JAXRXPORT = { "JAXRXPORT", 8984 };

  /** Server timeout in seconds; deactivated if set to 0. */
  public static final Object[] TIMEOUT = { "TIMEOUT", 0 };

  // TRANSIENT OPTIONS ========================================================

  /** The following options are not saved to disk; don't remove this flag. */
  public static final Object[] SKIP = { "SKIP", true };

  /** Debug mode. */
  public static final Object[] DEBUG = { "DEBUG", false };

  /** Detailed query information. */
  public static final Object[] QUERYINFO = { "QUERYINFO", false };
  /** Flag for serializing query results. */
  public static final Object[] SERIALIZE = { "SERIALIZE", true };
  /** External variables, separated by commas. */
  public static final Object[] BINDINGS = { "BINDINGS", "" };
  /** Serialization parameters, separated by commas. */
  public static final Object[] SERIALIZER = { "SERIALIZER", "" };
  /** Exporter serialization parameters. */
  public static final Object[] EXPORTER = { "EXPORTER", "" };
  /** Dots the query plan. */
  public static final Object[] DOTPLAN = { "DOTPLAN", false };
  /** Compact dot representation. */
  public static final Object[] DOTCOMPACT = { "DOTCOMPACT", false };
  /** Display dot graph. */
  public static final Object[] DOTDISPLAY = { "DOTDISPLAY", true };
  /** Path to dotty. */
  public static final Object[] DOTTY = { "DOTTY", "dotty" };
  /** Prints an XML plan. */
  public static final Object[] XMLPLAN = { "XMLPLAN", false };
  /** Creates the query plan before or after compilation. */
  public static final Object[] COMPPLAN = { "COMPPLAN", true };

  /** Caches the query results. */
  public static final Object[] CACHEQUERY = { "CACHEQUERY", false };
  /** Writes original files back after updates. */
  public static final Object[] WRITEBACK = { "WRITEBACK", false };
  /** Forces database creation for unknown documents. */
  public static final Object[] FORCECREATE = { "FORCECREATE", false };
  /** Default XQuery version. */
  public static final Object[] XQUERY3 = { "XQUERY3", true };

  /** Defines the number of parallel readers. */
  public static final Object[] PARALLEL = { "PARALLEL", 8 };

  /** Use internal XML parser. */
  public static final Object[] INTPARSE = { "INTPARSE", false };
  /** Flag for parsing DTDs in internal parser. */
  public static final Object[] DTD = { "DTD", false };
  /** Path to XML Catalog file. */
  public static final Object[] CATFILE = { "CATFILE", "" };
  /** Flag for entity parsing in internal parser. */
  public static final Object[] ENTITY = { "ENTITY", false };
  /** Define import parser. */
  public static final Object[] PARSER = { "PARSER", "XML" };
  /** Define parser options. */
  public static final Object[] PARSEROPT = { "PARSEROPT",
    "encoding=UTF-8,lines=true,format=verbose,header=false,separator=comma" };

  /** Number of query executions. */
  public static final Object[] RUNS = { "RUNS", 1 };
  /** Flag for whitespace chopping. */
  public static final Object[] CHOP = { "CHOP", true };
  /** Flag for creating a text index. */
  public static final Object[] TEXTINDEX = { "TEXTINDEX", true };
  /** Flag for creating an attribute value index. */
  public static final Object[] ATTRINDEX = { "ATTRINDEX", true };
  /** Flag for creating a full-text index. */
  public static final Object[] FTINDEX = { "FTINDEX", false };
  /** Flag for creating a path summary. */
  public static final Object[] PATHINDEX = { "PATHINDEX", true };
  /** Maximum number of name categories. */
  public static final Object[] CATEGORIES = { "CATEGORIES", 50 };

  /** Flag for creating a main memory database. */
  public static final Object[] MAINMEM = { "MAINMEM", false };
  /** Path for filtering XML Documents. */
  public static final Object[] CREATEFILTER = { "CREATEFILTER", "*.xml" };

  /** Flag for creating a wildcard index. */
  public static final Object[] WILDCARDS = { "WILDCARDS", false };
  /** Flag for full-text stemming. */
  public static final Object[] STEMMING = { "STEMMING", false };
  /** Flag for full-text case sensitivity. */
  public static final Object[] CASESENS = { "CASESENS", false };
  /** Flag for full-text diacritics sensitivity. */
  public static final Object[] DIACRITICS = { "DIACRITICS", false };
  /** Language for full-text search index. */
  public static final Object[] LANGUAGE = { "LANGUAGE", "English" };
  /** Flag for full-text scoring algorithm.
      Scoring mode: 0 = none, 1 = document nodes, 2 = text nodes. */
  public static final Object[] SCORING = { "SCORING", 0 };
  /** Path to full-text stopword list. */
  public static final Object[] STOPWORDS = { "STOPWORDS", "" };
  /** Levenshtein default error. */
  public static final Object[] LSERROR = { "LSERROR", 0 };

  /** Maximum number of index occurrences to print. */
  public static final Object[] MAXSTAT = { "MAXSTAT", 15 };

  // STATIC PROPERTIES ========================================================

  /** Root properties. */
  private static Prop root;
  /** GUI mode. */
  public static boolean gui;

  /** Language (applied after restart). */
  static String language = LANG[1].toString();
  /** Flag for showing language keys. */
  static boolean langkeys;

  /**
   * Constructor.
   * @param read properties from disk
   */
  public Prop(final boolean read) {
    super(read ? "" : null);
    if(root == null) root = this;
    if(read) finish();
  }

  /**
   * Returns a file instance for the current database path.
   * @param db name of the database
   * @return database filename
   */
  public File dbpath(final String db) {
    return new File(get(DBPATH) + '/' + db);
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
    if(this != root) return;
    // set some static properties
    Prop.language = get(Prop.LANG);
    Prop.langkeys = is(Prop.LANGKEYS);
    Util.debug = is(Prop.DEBUG);
  }
}
