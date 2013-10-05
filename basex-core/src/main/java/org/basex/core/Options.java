package org.basex.core;

/**
 * This class contains database options which are used all around the project.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Options extends AOptions {
  // General

  /** Flag for creating a main memory database. */
  public static final Object[] MAINMEM = { "MAINMEM", false };
  /** Flag for opening a database after creating it. */
  public static final Object[] CREATEONLY = { "CREATEONLY", false };

  // Parsing

  /** Path for filtering XML Documents. */
  public static final Object[] CREATEFILTER = { "CREATEFILTER", "*.xml" };
  /** Flag for adding archives to a database. */
  public static final Object[] ADDARCHIVES = { "ADDARCHIVES", true };
  /** Flag for skipping corrupt files. */
  public static final Object[] SKIPCORRUPT = { "SKIPCORRUPT", false };
  /** Flag for adding remaining files as raw files. */
  public static final Object[] ADDRAW = { "ADDRAW", false };
  /** Cache new documents before adding them to a database. */
  public static final Object[] ADDCACHE = { "ADDCACHE", false };
  /** Define import parser. */
  public static final Object[] PARSER = { "PARSER", "xml" };
  /** Define CSV parser options. */
  public static final Object[] CSVPARSER = { "CSVPARSER", "" };
  /** Define text parser options. */
  public static final Object[] TEXTPARSER = { "TEXTPARSER", "" };
  /** Define JSON parser options. */
  public static final Object[] JSONPARSER = { "JSONPARSER", "" };
  /** Define TagSoup HTML options. */
  public static final Object[] HTMLPARSER = { "HTMLPARSER", "" };

  // XML Parsing

  /** Flag for whitespace chopping. */
  public static final Object[] CHOP = { "CHOP", true };
  /** Use internal XML parser. */
  public static final Object[] INTPARSE = { "INTPARSE", true };
  /** Strips namespaces. */
  public static final Object[] STRIPNS = { "STRIPNS", false };
  /** Flag for parsing DTDs in internal parser. */
  public static final Object[] DTD = { "DTD", false };
  /** Path to XML Catalog file. */
  public static final Object[] CATFILE = { "CATFILE", "" };

  // Indexing

  /** Flag for creating a text index. */
  public static final Object[] TEXTINDEX = { "TEXTINDEX", true };
  /** Flag for creating an attribute value index. */
  public static final Object[] ATTRINDEX = { "ATTRINDEX", true };
  /** Flag for creating a full-text index. */
  public static final Object[] FTINDEX = { "FTINDEX", false };

  /** Maximum number of text/attribute index entries
   *  to keep in memory during index creation. */
  public static final Object[] INDEXSPLITSIZE = { "INDEXSPLITSIZE", 0 };
  /** Maximum number of fulltext index entries to keep in memory during index creation. */
  public static final Object[] FTINDEXSPLITSIZE = { "FTINDEXSPLITSIZE", 0 };

  /** Maximum length of index entries. */
  public static final Object[] MAXLEN = { "MAXLEN", 96 };
  /** Maximum number of name categories. */
  public static final Object[] MAXCATS = { "MAXCATS", 100 };
  /** Flag for automatic index update. */
  public static final Object[] UPDINDEX = { "UPDINDEX", false };

  // Full-Text

  /** Flag for full-text stemming. */
  public static final Object[] STEMMING = { "STEMMING", false };
  /** Flag for full-text case sensitivity. */
  public static final Object[] CASESENS = { "CASESENS", false };
  /** Flag for full-text diacritics sensitivity. */
  public static final Object[] DIACRITICS = { "DIACRITICS", false };
  /** Language for full-text search index. */
  public static final Object[] LANGUAGE = { "LANGUAGE", "en" };
  /** Path to full-text stopword list. */
  public static final Object[] STOPWORDS = { "STOPWORDS", "" };

  // Query Options

  /** Detailed query information. */
  public static final Object[] QUERYINFO = { "QUERYINFO", false };
  /** Default XQuery version. */
  public static final Object[] XQUERY3 = { "XQUERY3", true };
  /** External variables, separated by commas. */
  public static final Object[] BINDINGS = { "BINDINGS", "" };
  /** Path to current query. */
  public static final Object[] QUERYPATH = { "QUERYPATH", "" };
  /** Caches the query results. */
  public static final Object[] CACHEQUERY = { "CACHEQUERY", false };
  /** Forces database creation for unknown documents. */
  public static final Object[] FORCECREATE = { "FORCECREATE", false };
  /** Validate string inputs. */
  public static final Object[] CHECKSTRINGS = { "CHECKSTRINGS", true };
  /** Levenshtein default error. */
  public static final Object[] LSERROR = { "LSERROR", 0 };
  /** Number of query executions. */
  public static final Object[] RUNS = { "RUNS", 1 };

  // Serialize

  /** Flag for serializing query results. */
  public static final Object[] SERIALIZE = { "SERIALIZE", true };
  /** Serialization parameters, separated by commas. */
  public static final Object[] SERIALIZER = { "SERIALIZER", "" };
  /** Exporter serialization parameters. */
  public static final Object[] EXPORTER = { "EXPORTER", "" };

  /** Prints an XML plan. */
  public static final Object[] XMLPLAN = { "XMLPLAN", false };
  /** Creates the query plan before or after compilation. */
  public static final Object[] COMPPLAN = { "COMPPLAN", true };
  /** Dots the query plan. */
  public static final Object[] DOTPLAN = { "DOTPLAN", false };
  /** Compact dot representation. */
  public static final Object[] DOTCOMPACT = { "DOTCOMPACT", false };
  /** Display dot graph. */
  public static final Object[] DOTDISPLAY = { "DOTDISPLAY", true };
  /** Path to dotty. */
  public static final Object[] DOTTY = { "DOTTY", "dotty" };

  /** Flushes the database after each update. */
  public static final Object[] AUTOFLUSH = { "AUTOFLUSH", true };
  /** Writes original files back after updates. */
  public static final Object[] WRITEBACK = { "WRITEBACK", false };
  /** Maximum number of index occurrences to print. */
  public static final Object[] MAXSTAT = { "MAXSTAT", 30 };

  // Other

  /** Hidden: flag for tail-call optimization. */
  public static final Object[] TAILCALLS = { "TAILCALLS", 256 };
  /** Hidden: flag for the size limit on inlineable functions. */
  public static final Object[] INLINELIMIT = { "INLINELIMIT", 0 };
  /** Hidden: maximum number of hits to be displayed in the GUI (will be overwritten). */
  public static final Object[] MAXHITS = { "MAXHITS", -1 };
}
