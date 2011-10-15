package org.basex.core;

import org.basex.util.Util;

/**
 * This class assembles properties which are used all around the project.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Prop extends AProp {

  // CONSTANTS ================================================================

  /** Project name. */
  public static final String NAME = "BaseX";
  /** Code version (may contain major, minor and optional patch number). */
  public static final String VERSION = "7.0.1 beta";

  /** New line string. */
  public static final String NL = System.getProperty("line.separator");
  /** Returns the system's default encoding. */
  public static final String ENCODING = System.getProperty("file.encoding");

  /** System's temporary directory. */
  public static final String TMP = System.getProperty("java.io.tmpdir") + '/';

  /** OS flag (should be ignored whenever possible). */
  public static final String OS = System.getProperty("os.name").toUpperCase();
  /** Flag denoting if OS belongs to Mac family. */
  public static final boolean MAC = OS.startsWith("MAC");
  /** Flag denoting if OS belongs to Windows family. */
  public static final boolean WIN = OS.startsWith("WIN");

  /** Directory for storing the property files, database directory, etc. */
  public static final String HOME = Util.homeDir() + '/';

  /** Property information. */
  static final String PROPHEADER = "# Property File." + Prop.NL
      + "# You can set additional options at the end of the file." + Prop.NL;
  /** Property information. */
  static final String PROPUSER = "# User defined section";

  // OPTIONS ==================================================================

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
  /** Path to current query. */
  public static final Object[] QUERYPATH = { "QUERYPATH", "" };
  /** Writes original files back after updates. */
  public static final Object[] WRITEBACK = { "WRITEBACK", false };
  /** Forces database creation for unknown documents. */
  public static final Object[] FORCECREATE = { "FORCECREATE", false };
  /** Flushes the database after each update. */
  public static final Object[] AUTOFLUSH = { "AUTOFLUSH", true };
  /** Default XQuery version. */
  public static final Object[] XQUERY3 = { "XQUERY3", true };

  /** Use internal XML parser. */
  public static final Object[] INTPARSE = { "INTPARSE", true };
  /** Flag for parsing DTDs in internal parser. */
  public static final Object[] DTD = { "DTD", false };
  /** Path to XML Catalog file. */
  public static final Object[] CATFILE = { "CATFILE", "" };
  /** Define import parser. */
  public static final Object[] PARSER = { "PARSER", "xml" };
  /** Define parser options. */
  public static final Object[] PARSEROPT = { "PARSEROPT",
    "flat=false,encoding=UTF-8,lines=true,format=verbose,header=false," +
    "separator=comma" };

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
  /** Flag for adding archives to a database. */
  public static final Object[] ADDARCHIVES = { "ADDARCHIVES", true };
  /** Flag for skipping corrupt files. */
  public static final Object[] SKIPCORRUPT = { "SKIPCORRUPT", false };

  /** Flag for creating a wildcard index. */
  public static final Object[] WILDCARDS = { "WILDCARDS", false };
  /** Flag for full-text stemming. */
  public static final Object[] STEMMING = { "STEMMING", false };
  /** Flag for full-text case sensitivity. */
  public static final Object[] CASESENS = { "CASESENS", false };
  /** Flag for full-text diacritics sensitivity. */
  public static final Object[] DIACRITICS = { "DIACRITICS", false };
  /** Language for full-text search index. */
  public static final Object[] LANGUAGE = { "LANGUAGE", "en" };
  /** Flag for full-text scoring algorithm.
      Scoring mode: 0 = none, 1 = document nodes, 2 = text nodes. */
  public static final Object[] SCORING = { "SCORING", 0 };
  /** Path to full-text stopword list. */
  public static final Object[] STOPWORDS = { "STOPWORDS", "" };
  /** Levenshtein default error. */
  public static final Object[] LSERROR = { "LSERROR", 0 };

  /** Maximum number of index occurrences to print. */
  public static final Object[] MAXSTAT = { "MAXSTAT", 30 };
  /** Flag for tail-call optimization. */
  public static final Object[] TAILCALLS = { "TAILCALLS", 42 };

  // STATIC PROPERTIES ========================================================

  /** GUI mode. */
  public static boolean gui;

  /**
   * Constructor.
   */
  public Prop() {
    super(null);
  }
}
