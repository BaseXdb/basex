package org.basex.core;

import java.io.File;

/**
 * This class assembles properties which are used all around the project. They
 * are initially read from and finally written to disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Prop extends AProp {
  // CONSTANTS ================================================================

  /** New line string. */
  public static final String NL = System.getProperty("line.separator");
  /** File separator string. */
  public static final String SEP = System.getProperty("file.separator");
  /** Returns the system's default encoding. */
  public static final String ENCODING = System.getProperty("file.encoding");

  /** Returns the current working directory. */
  public static final String WORK = System.getProperty("user.dir") + SEP;
  /** User's home directory. */
  public static final String HOME = System.getProperty("user.home") + SEP;
  /** System's temporary directory. */
  public static final String TMP = System.getProperty("java.io.tmpdir") + SEP;

  /** OS Flag (should be ignored whenever possible). */
  private static final String OS = System.getProperty("os.name");
  /** Flag denoting if OS belongs to Mac family. */
  public static final boolean MAC = OS.charAt(0) == 'M';

  /** Property information. */
  static final String PROPHEADER = "# Property File." + NL +
      "# This here will be overwritten every time, but" + NL +
      "# you can set your own options at the end of the file." + NL;
  /** Property information. */
  static final String PROPUSER = "# User defined section";

  // The following properties will be saved to disk:

  // DATABASE & PROGRAM PATHS =================================================

  /** Database path. */
  public static final Object[] DBPATH = { "DBPATH", HOME + Text.NAME + "Data" };

  /** Language Name (currently: English or German). */
  public static final Object[] LANGUAGE = { "LANGUAGE", "English" };
  /** Flag to include key names in the language strings. */
  public static final Object[] LANGKEYS = { "LANGKEYS", false };

  /** Client/server communication: host. */
  public static final Object[] HOST = { "HOST", "localhost" };
  /** Client/server communication: client port. */
  public static final Object[] PORT = { "PORT", 1984 };
  /** Client/server communication: server port. */
  public static final Object[] SERVERPORT = { "SERVERPORT", 1984 };

  // TRANSIENT OPTIONS ========================================================

  /** The following options are not saved to disk; don't remove this flag. */
  public static final Object[] SKIP = { "SKIP", true };

  /** Debug mode. */
  public static final Object[] DEBUG = { "DEBUG", false };

  /** Short processing info. */
  public static final Object[] INFO = { "INFO", false };
  /** Detailed processing info. */
  public static final Object[] ALLINFO = { "ALLINFO", false };
  /** Flag for serializing query results. */
  public static final Object[] SERIALIZE = { "SERIALIZE", true };
  /** Flag for wrapping result nodes. */
  public static final Object[] WRAPOUTPUT = { "WRAPOUTPUT", false };
  /** Format output. */
  public static final Object[] XMLFORMAT = { "XMLFORMAT", true };
  /** Format output. */
  public static final Object[] XMLENCODING = { "XMLENCODING", "UTF-8" };
  /** Dots the query plan. */
  public static final Object[] DOTPLAN = { "DOTPLAN", false };
  /** Path to dotty. */
  public static final Object[] DOTTY = { "DOTTY", "dotty" };
  /** Prints a XML plan. */
  public static final Object[] XMLPLAN = { "XMLPLAN", false };
  /** Creates the query plan before or after compilation. */
  public static final Object[] COMPPLAN = { "COMPPLAN", true };

  /** Caches the query results. */
  public static final Object[] CACHEQUERY = { "CACHEQUERY", false };
  /** Writes original files back after updates. */
  public static final Object[] WRITEBACK = { "WRITEBACK", false };

  /** Use internal XML parser. */
  public static final Object[] INTPARSE = { "INTPARSE", false };
  /** Flag for parsing DTDs in internal parser. */
  public static final Object[] DTD = { "DTD", false };
  /** Flag for entity parsing in internal parser. */
  public static final Object[] ENTITY = { "ENTITY", false };

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

  /** Flag for loading database table into main memory. */
  public static final Object[] TABLEMEM = { "TABLEMEM", false };
  /** Flag for creating a main memory database. */
  public static final Object[] MAINMEM = { "MAINMEM", false };
  /** Path for filtering XML Documents. */
  public static final Object[] CREATEFILTER = { "CREATEFILTER", "*.xml" };
  /** Maximum text size to be displayed. */
  public static final Object[] MAXTEXT = { "MAXTEXT", 1 << 21 };

  /** Flag for creating a wildcard index. */
  public static final Object[] WILDCARDS = { "WILDCARDS", false };
  /** Flag for full-text stemming. */
  public static final Object[] STEMMING = { "STEMMING", false };
  /** Flag for full-text case sensitivity. */
  public static final Object[] CASESENS = { "CASESENS", false };
  /** Flag for full-text diacritics sensitivity. */
  public static final Object[] DIACRITICS = { "DIACRITICS", false };
  /** Flag for full-text scoring algorithm. */
  /** Scoring mode: 0 = none, 1 = document nodes, 2 = text nodes. */
  public static final Object[] SCORING = { "SCORING", 0 };
  /** Path to full-text stopword list. */
  public static final Object[] STOPWORDS = { "STOPWORDS", "" };

  /** Levenshtein default error. */
  public static final Object[] LSERROR = { "LSERR", 0 };
  /** Flag for creating flat MAB2 data. */
  public static final Object[] MAB2FLAT = { "MAB2flat", false };
  /** Maximum number of index occurrences to print. */
  public static final Object[] MAXSTAT = { "MAXSTAT", 15 };

  /** Server timeout in seconds; deactivated if set to 0. */
  public static final Object[] TIMEOUT = { "TIMEOUT", 0 };

  // DEEPFS OPTIONS ===========================================================

  /** Flag for importing file metadata. */
  public static final Object[] FSMETA = { "FSMETA", true };
  /** Flag for importing file contents. */
  public static final Object[] FSCONT = { "FSCONT", false };
  /** Flag for importing xml contents. */
  public static final Object[] FSXML = { "FSXML", false };
  /** Maximum size for textual imports. */
  public static final Object[] FSTEXTMAX = { "FSTEXTMAX", 10240 };
  /** Verbose debug informations for FSTraversal. */
  public static final Object[] FSVERBOSE = { "FSVERBOSE", false };

  /** Fuse support. */
  public static final Object[] FUSE = { "FUSE", false };

  /**
   * Spotlight integration. If true, on mac platforms spotlight index is used
   * instead of the internal parser implementations.
   */
  public static final Object[] SPOTLIGHT = { "SPOTLIGHT", false };

  // STATIC PROPERTIES ========================================================

  /** GUI mode. */
  public static boolean gui;
  /** Debug mode. */
  public static boolean debug;

  /** Language (applied after restart). */
  public static String language = LANGUAGE[1].toString();
  /** Flag for showing language keys. */
  public static boolean langkeys;

  /**
   * Constructor.
   * @param read properties from disk
   */
  public Prop(final boolean read) {
    super(read ? "" : null);
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
    return dbpath(db).exists();
  }

  @Override
  protected void finish() {
    // set static properties
    Prop.language = get(Prop.LANGUAGE);
    Prop.langkeys = is(Prop.LANGKEYS);
    Prop.debug = is(Prop.DEBUG);
  }
}
