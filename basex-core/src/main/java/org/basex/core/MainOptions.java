package org.basex.core;

import org.basex.util.*;

/**
 * This class contains database options which are used all around the project.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class MainOptions extends Options {
  // General

  /** Flag for creating a main memory database. */
  public static final Option MAINMEM = new Option("MAINMEM", false);
  /** Flag for opening a database after creating it. */
  public static final Option CREATEONLY = new Option("CREATEONLY", false);

  // Parsing

  /** Path for filtering XML Documents. */
  public static final Option CREATEFILTER = new Option("CREATEFILTER", "*.xml");
  /** Flag for adding archives to a database. */
  public static final Option ADDARCHIVES = new Option("ADDARCHIVES", true);
  /** Flag for skipping corrupt files. */
  public static final Option SKIPCORRUPT = new Option("SKIPCORRUPT", false);
  /** Flag for adding remaining files as raw files. */
  public static final Option ADDRAW = new Option("ADDRAW", false);
  /** Cache new documents before adding them to a database. */
  public static final Option ADDCACHE = new Option("ADDCACHE", false);
  /** Define import parser. */
  public static final Option PARSER = new Option("PARSER", "xml");
  /** Define CSV parser options. */
  public static final Option CSVPARSER = new Option("CSVPARSER", "");
  /** Define text parser options. */
  public static final Option TEXTPARSER = new Option("TEXTPARSER", "");
  /** Define JSON parser options. */
  public static final Option JSONPARSER = new Option("JSONPARSER", "");
  /** Define TagSoup HTML options. */
  public static final Option HTMLPARSER = new Option("HTMLPARSER", "");

  // XML Parsing

  /** Flag for whitespace chopping. */
  public static final Option CHOP = new Option("CHOP", true);
  /** Use internal XML parser. */
  public static final Option INTPARSE = new Option("INTPARSE", true);
  /** Strips namespaces. */
  public static final Option STRIPNS = new Option("STRIPNS", false);
  /** Flag for parsing DTDs in internal parser. */
  public static final Option DTD = new Option("DTD", false);
  /** Path to XML Catalog file. */
  public static final Option CATFILE = new Option("CATFILE", "");

  // Indexing

  /** Flag for creating a text index. */
  public static final Option TEXTINDEX = new Option("TEXTINDEX", true);
  /** Flag for creating an attribute value index. */
  public static final Option ATTRINDEX = new Option("ATTRINDEX", true);
  /** Flag for creating a full-text index. */
  public static final Option FTINDEX = new Option("FTINDEX", false);

  /** Maximum number of text/attribute index entries
   *  to keep in memory during index creation. */
  public static final Option INDEXSPLITSIZE = new Option("INDEXSPLITSIZE", 0);
  /** Maximum number of fulltext index entries to keep in memory during index creation. */
  public static final Option FTINDEXSPLITSIZE = new Option("FTINDEXSPLITSIZE", 0);

  /** Maximum length of index entries. */
  public static final Option MAXLEN = new Option("MAXLEN", 96);
  /** Maximum number of name categories. */
  public static final Option MAXCATS = new Option("MAXCATS", 100);
  /** Flag for automatic index update. */
  public static final Option UPDINDEX = new Option("UPDINDEX", false);

  // Full-Text

  /** Flag for full-text stemming. */
  public static final Option STEMMING = new Option("STEMMING", false);
  /** Flag for full-text case sensitivity. */
  public static final Option CASESENS = new Option("CASESENS", false);
  /** Flag for full-text diacritics sensitivity. */
  public static final Option DIACRITICS = new Option("DIACRITICS", false);
  /** Language for full-text search index. */
  public static final Option LANGUAGE = new Option("LANGUAGE", "en");
  /** Path to full-text stopword list. */
  public static final Option STOPWORDS = new Option("STOPWORDS", "");

  // Query Options

  /** Detailed query information. */
  public static final Option QUERYINFO = new Option("QUERYINFO", false);
  /** Default XQuery version. */
  public static final Option XQUERY3 = new Option("XQUERY3", true);
  /** External variables, separated by commas. */
  public static final Option BINDINGS = new Option("BINDINGS", "");
  /** Path to current query. */
  public static final Option QUERYPATH = new Option("QUERYPATH", "");
  /** Caches the query results. */
  public static final Option CACHEQUERY = new Option("CACHEQUERY", false);
  /** Forces database creation for unknown documents. */
  public static final Option FORCECREATE = new Option("FORCECREATE", false);
  /** Validate string inputs. */
  public static final Option CHECKSTRINGS = new Option("CHECKSTRINGS", true);
  /** Levenshtein default error. */
  public static final Option LSERROR = new Option("LSERROR", 0);
  /** Number of query executions. */
  public static final Option RUNS = new Option("RUNS", 1);

  // Serialize

  /** Flag for serializing query results. */
  public static final Option SERIALIZE = new Option("SERIALIZE", true);
  /** Serialization parameters, separated by commas. */
  public static final Option SERIALIZER = new Option("SERIALIZER", "");
  /** Exporter serialization parameters. */
  public static final Option EXPORTER = new Option("EXPORTER", "");

  /** Prints an XML plan. */
  public static final Option XMLPLAN = new Option("XMLPLAN", false);
  /** Creates the query plan before or after compilation. */
  public static final Option COMPPLAN = new Option("COMPPLAN", true);
  /** Dots the query plan. */
  public static final Option DOTPLAN = new Option("DOTPLAN", false);
  /** Compact dot representation. */
  public static final Option DOTCOMPACT = new Option("DOTCOMPACT", false);
  /** Display dot graph. */
  public static final Option DOTDISPLAY = new Option("DOTDISPLAY", true);
  /** Path to dotty. */
  public static final Option DOTTY = new Option("DOTTY", "dotty");

  /** Flushes the database after each update. */
  public static final Option AUTOFLUSH = new Option("AUTOFLUSH", true);
  /** Writes original files back after updates. */
  public static final Option WRITEBACK = new Option("WRITEBACK", false);
  /** Maximum number of index occurrences to print. */
  public static final Option MAXSTAT = new Option("MAXSTAT", 30);

  // Other

  /** Hidden: flag for tail-call optimization. */
  public static final Option TAILCALLS = new Option("TAILCALLS", 256);
  /** Hidden: flag for the size limit on inlineable functions. */
  public static final Option INLINELIMIT = new Option("INLINELIMIT", 0);
  /** Hidden: maximum number of hits to be displayed in the GUI (will be overwritten). */
  public static final Option MAXHITS = new Option("MAXHITS", -1);
}
