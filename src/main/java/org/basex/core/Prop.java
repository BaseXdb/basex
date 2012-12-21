package org.basex.core;

import java.io.*;
import java.net.*;
import java.security.*;

import org.basex.io.*;
import org.basex.util.*;

/**
 * This class assembles properties which are used all around the project.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Prop extends AProp {
  // CONSTANTS ==========================================================================

  /** Project name. */
  public static final String NAME = "BaseX";
  /** Code version (may contain major, minor and optional patch number). */
  public static final String VERSION = "7.5";

  /** New line string. */
  public static final String NL = System.getProperty("line.separator");
  /** Returns the system's default encoding. */
  public static final String ENCODING = System.getProperty("file.encoding");

  /** System's temporary directory. */
  public static final String TMP = System.getProperty("java.io.tmpdir") + '/';

  /** OS flag (source: {@code http://lopica.sourceforge.net/os.html}). */
  private static final String OS = System.getProperty("os.name");
  /** Flag denoting if OS belongs to Mac family. */
  public static final boolean MAC = OS.startsWith("Mac");
  /** Flag denoting if OS belongs to Windows family. */
  public static final boolean WIN = OS.startsWith("Windows");

  /** Prefix for project specific properties. */
  public static final String DBPREFIX = "org.basex.";
  /** System property for specifying database home directory. */
  public static final String PATH = DBPREFIX + "path";
  /** User's home directory. */
  public static final String USERHOME = System.getProperty("user.home") + File.separator;
  /** Directory for storing the property files, database directory, etc. */
  public static final String HOME = homePath();

  /** Property information. */
  static final String PROPHEADER = "# " + NAME + " Property File." + NL;

  // STATIC OPTIONS =====================================================================

  /** Language (applied after restart). */
  public static String language = "English";
  /** Flag for prefixing texts with their keys (helps while translating texts). */
  public static boolean langkeys;
  /** Debug mode. */
  public static boolean debug;
  /** GUI mode. */
  public static boolean gui;

  // OPTIONS ============================================================================

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
  /** Define import parser. */
  public static final Object[] PARSER = { "PARSER", "xml" };
  /** Define parser options. */
  public static final Object[] PARSEROPT = { "PARSEROPT",
    "encoding=UTF-8,flat=false,format=verbose,header=false,jsonml=false," +
    "lines=true,separator=comma" };
  /** Define TagSoup HTML options. */
  public static final Object[] HTMLOPT = { "HTMLOPT",
    "html=false,omit-xml-declaration=false,method=xml,nons=false,nobogons=false," +
    "nodefaults=false,nocolons=false,norestart=false,ignorable=false,emptybogons=false," +
    "any=false,norootbogons=false,nocdata=false,lexical=false,encoding=utf-8" };

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

  /** Hidden: garbage collecting mode. */
  public static final Object[] SINGLEGC = { "SINGLEGC", false };
  /** Hidden: flag for tail-call optimization. */
  public static final Object[] TAILCALLS = { "TAILCALLS", 42 };
  /** Hidden: maximum number of hits to be displayed in the GUI (will be overwritten). */
  public static final Object[] MAXHITS = { "MAXHITS", -1 };

  /**
   * <p>Determines the project's home directory for storing property files
   * and directories. The directory is chosen as follows:</p>
   * <ol>
   * <li>First, the <b>system property</b> {@code "org.basex.path"} is checked.
   *   If it contains a value, it is adopted as home directory.</li>
   * <li>If not, the <b>current user directory</b> (defined by the system
   *   property {@code "user.dir"}) is chosen if the {@code .basex}
   *   configuration file is found in this directory.</li>
   * <li>Otherwise, the configuration file is searched in the <b>application
   *   directory</b> (the folder in which the project is located).</li>
   * <li>In all other cases, the <b>user's home directory</b> (defined in
   *   {@code "user.home"}) is chosen.</li>
   * </ol>
   * @return home directory
   */
  private static String homePath() {
    // check user specific property
    String path = System.getProperty(PATH);
    if(path != null) return path + File.separator;

    // check working directory for property file
    path = System.getProperty("user.dir");
    File config = new File(path, IO.BASEXSUFFIX);
    if(config.exists()) return config.getParent() + File.separator;

    // not found; check application directory
    path = applicationPath();
    if(path != null) {
      final File app = new File(path);
      final String dir = app.isFile() ? app.getParent() : app.getPath();
      config = new File(dir, IO.BASEXSUFFIX);
      if(config.exists()) return config.getParent() + File.separator;
    }

    // not found; choose user home directory as default
    return USERHOME;
  }

  /**
   * Returns the absolute path to this application, or {@code null} if the
   * path cannot be evaluated.
   * @return application path.
   */
  private static String applicationPath() {
    final ProtectionDomain pd = Prop.class.getProtectionDomain();
    if(pd == null) return null;
    // code source (may be null)
    final CodeSource cs = pd.getCodeSource();
    if(cs == null) return null;
    // location (may be null)
    final URL url = cs.getLocation();
    if(url == null) return null;
    final String path = url.getPath();
    // decode path; URLDecode returns wrong results
    final TokenBuilder tb = new TokenBuilder();
    final int pl = path.length();
    for(int p = 0; p < pl; ++p) {
      final char ch = path.charAt(p);
      if(ch == '%' && p + 2 < pl) {
        tb.addByte((byte) Integer.parseInt(path.substring(p + 1, p + 3), 16));
        p += 2;
      } else {
        tb.add(ch);
      }
    }
    try {
      // return path, using the correct encoding
      return new String(tb.finish(), ENCODING);
    } catch(final Exception ex) {
      // use default path; not expected to occur
      Util.stack(ex);
      return tb.toString();
    }
  }
}
