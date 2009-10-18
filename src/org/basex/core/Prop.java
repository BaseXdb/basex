package org.basex.core;

import java.io.File;
import org.basex.io.IO;

/**
 * This class assembles properties which are used all around the project. They
 * are initially read from and finally written to disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
  /** Flag denoting if OS belongs not to Windows family.
  public static final boolean UNIX = OS.charAt(0) != 'W';*/
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
  /** Web Server path. */
  public static final Object[] WEBPATH = { "WEBPATH", WORK + "web" };
  /** Path to dotty. */
  public static final Object[] DOTTY = { "DOTTY", "dotty" };

  /** Language Name (currently: English or German). */
  public static final Object[] LANGUAGE = { "LANGUAGE", "English" };
  /** Flag to include key names in the language strings. */
  public static final Object[] LANGKEYS = { "LANGKEYS", false };

  /** Host for client/server communication. */
  public static final Object[] HOST = { "HOST", "localhost" };
  /** Port for client/server communication. */
  public static final Object[] PORT = { "PORT", 1984 };
  /** Port for web server. */
  public static final Object[] WEBPORT = { "WEBPORT", 8080 };

  // TRANSIENT OPTIONS ========================================================

  /** The following options are not saved to disk; don't remove this flag. */
  public static final Object[] SKIP = { "SKIP", true };

  /** Debug mode. */
  public static final Object[] DEBUG = { "DEBUG", false };
  /** Web server mode. */
  public static final Object[] WEB = { "WEB", false };

  /** Short query info. */
  public static final Object[] INFO = { "INFO", false };
  /** Detailed query info. */
  public static final Object[] ALLINFO = { "ALLINFO", false };
  /** Flag for serializing query results. */
  public static final Object[] SERIALIZE = { "SERIALIZE", true };
  /** Flag for serialization as XML. */
  public static final Object[] XMLOUTPUT = { "XMLOUTPUT", false };
  /** Dots the query plan. */
  public static final Object[] DOTPLAN = { "DOTPLAN", false };
  /** Prints a XML plan. */
  public static final Object[] XMLPLAN = { "XMLPLAN", false };
  /** Creates the query plan before or after compilation. */
  public static final Object[] COMPPLAN = { "COMPPLAN", true };

  /** Format XQuery output. */
  public static final Object[] XQFORMAT = { "XQFORMAT", true };

  /** Use internal XML parser. */
  public static final Object[] INTPARSE = { "INTPARSE", false };
  /** Flag for parsing DTDs in internal parser. */
  public static final Object[] DTD = { "DTD", true };
  /** Flag for entity parsing in internal parser. */
  public static final Object[] ENTITY = { "ENTITY", true };

  /** Number of query executions. */
  public static final Object[] RUNS = { "RUNS", 1 };
  /** Flag for whitespace chopping. */
  public static final Object[] CHOP = { "CHOP", true };
  /** Flag for creating a path summary. */
  public static final Object[] PATHINDEX = { "PATHINDEX", true };
  /** Flag for creating a text index. */
  public static final Object[] TEXTINDEX = { "TEXTINDEX", true };
  /** Flag for creating an attribute value index. */
  public static final Object[] ATTRINDEX = { "ATTRINDEX", true };
  /** Flag for creating a full-text index. */
  public static final Object[] FTINDEX = { "FTINDEX", false };

  /** Flag for loading database table into main memory. */
  public static final Object[] TABLEMEM = { "TABLEMEM", false };
  /** Flag for creating a main memory database. */
  public static final Object[] MAINMEM = { "MAINMEM", false };
  /** Path for filtering XML Documents. */
  public static final Object[] CREATEFILTER = { "CREATEFILTER", "*.xml" };
  /** Maximum text size to be displayed. */
  public static final Object[] MAXTEXT = { "MAXTEXT", 1 << 21 };
  /** Show all index info. */
  public static final Object[] INDEXALL = { "INDEXALL", false };

  /** Flag for creating a fuzzy index. */
  public static final Object[] FTFUZZY = { "FTFUZZY", true };
  /** Flag for full-text stemming. */
  public static final Object[] FTST = { "FTST", false };
  /** Flag for full-text case sensitivity. */
  public static final Object[] FTCS = { "FTCS", false };
  /** Flag for full-text diacritics sensitivity. */
  public static final Object[] FTDC = { "FTDC", false };

  /** Levenshtein default error. */
  public static final Object[] LSERR = { "LSERR", 0 };
  /** Flag for creating flat MAB2 data. */
  public static final Object[] MAB2FLAT = { "MAB2flat", false };

  /** Server timeout in seconds; deactivated if set to 0. */
  public static final Object[] TIMEOUT = { "TIMEOUT", 0 };

  //[SG] INEX budget time
  /** INEX budget for queries. */
  public static final Object[] IBT = { "INDEXBUDGETTIME", -1 };

  /** Indexing scores in place of pos values. */
  public static final Object[] INDEXSCORES = { "INDEXSCORES", false };

  
  // DEEPFS OPTIONS ===========================================================
  
  /** Flag for importing file metadata. */
  public static final Object[] FSMETA = { "FSMETA", true };
  /** Flag for importing file contents. */
  public static final Object[] FSCONT = { "FSCONT", true };
  /** Maximum size for textual imports. */
  public static final Object[] FSTEXTMAX = { "FSTEXTMAX", 1048576 };

  /** FSParser implementation. If true, the new implementation is used. */
  public static final Object[] NEWFSPARSER = { "NEWFSPARSER", true };
  /** Fuse support. */
  public static final Object[] FUSE = { "FUSE", false };
  /** Flag for creating a native (joint) database. */
  public static final Object[] NATIVEDATA = { "NATIVEDATA", false };
  /** Propagate changes in DeepFS back to original filesystem. */
  public static final Object[] WTHROUGH = {"WTHROUGH", false };
  /**
   * Spotlight integration. If true, on mac platforms spotlight index is used
   * instead of the internal parser implementations.
   */
  public static final Object[] SPOTLIGHT = { "SPOTLIGHT", false };

  // WEBSERVER OPTIONS ========================================================

  /** PHP Path. */
  public static final Object[] PHPPATH = { "PHPPATH", "php" };

  // STATIC PROPERTIES ========================================================

  /** GUI mode. */
  public static boolean gui;
  /** Web Server mode. */
  public static boolean web;
  /** Debug mode. */
  public static boolean debug;

  /** Language (applied after restart). */
  public static String language = LANGUAGE[1].toString();
  /** Flag for showing language keys. */
  public static boolean langkeys;

  /** Last XQuery file. */
  public static IO xquery;

  /**
   * Constructor.
   */
  public Prop() {
    super("");
    finish();
  }

  /**
   * Adds the database suffix to the specified filename and creates
   * a file instance.
   * @param db name of the database
   * @param file filename
   * @return database filename
   */
  public File dbfile(final String db, final String file) {
    return new File(get(DBPATH) + '/' + db + '/' + file + IO.BASEXSUFFIX);
  }

  /**
   * Returns a file instance for the current database path.
   * @param db name of the database
   * @return database filename
   */
  public File dbpath(final String db) {
    return new File(get(DBPATH) + '/' + db);
  }

  @Override
  protected void finish() {
    // set static properties
    Prop.language = get(Prop.LANGUAGE);
    Prop.langkeys = is(Prop.LANGKEYS);
    Prop.debug = is(Prop.DEBUG);
    Prop.web = is(Prop.WEB);

    /* [AH] JFUSE property could be set here
    Prop.... =
    try {
      Class<?> cls = Class.forName("org.deepfs.jfuse.JFUSEAdapter");
      cls.getMethod("loadJFUSELibrary").invoke(null);
      //JFUSEAdapter.loadJFUSELibrary()
      enabled = true;
    } catch(final Exception ex) {
      ex.printStackTrace();
    }
    
    // Alternative:
    Class<?> cls = Class.forName("org.deepfs.DeepFS");
    cls.getMethod("load").invoke(null);
     */
  }
}
