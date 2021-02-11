package org.basex.core;

import java.util.*;

import org.basex.build.csv.*;
import org.basex.build.html.*;
import org.basex.build.json.*;
import org.basex.build.text.*;
import org.basex.io.serial.*;
import org.basex.util.options.*;

/**
 * This class contains database options which are used all around the project.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class MainOptions extends Options {
  // General

  /** Flag for creating a main memory database. */
  public static final BooleanOption MAINMEM = new BooleanOption("MAINMEM", false);
  /** Flag for closing a database after creating it. */
  public static final BooleanOption CREATEONLY = new BooleanOption("CREATEONLY", false);

  // Parsing

  /** Path for filtering XML Documents. */
  public static final StringOption CREATEFILTER = new StringOption("CREATEFILTER", "*.xml");
  /** Flag for adding archives to a database. */
  public static final BooleanOption ADDARCHIVES = new BooleanOption("ADDARCHIVES", true);
  /** Flag for prefixing database paths with name of archive. */
  public static final BooleanOption ARCHIVENAME = new BooleanOption("ARCHIVENAME", false);
  /** Flag for skipping corrupt files. */
  public static final BooleanOption SKIPCORRUPT = new BooleanOption("SKIPCORRUPT", false);
  /** Flag for adding remaining files as raw files. */
  public static final BooleanOption ADDRAW = new BooleanOption("ADDRAW", false);
  /** Define CSV parser options. */
  public static final OptionsOption<CsvParserOptions> CSVPARSER =
      new OptionsOption<>("CSVPARSER", new CsvParserOptions());
  /** Define text parser options. */
  public static final OptionsOption<TextOptions> TEXTPARSER =
      new OptionsOption<>("TEXTPARSER", new TextOptions());
  /** Define JSON parser options. */
  public static final OptionsOption<JsonParserOptions> JSONPARSER =
      new OptionsOption<>("JSONPARSER", new JsonParserOptions());
  /** Define TagSoup HTML options. */
  public static final OptionsOption<HtmlOptions> HTMLPARSER =
      new OptionsOption<>("HTMLPARSER", new HtmlOptions());
  /** Define import parser. */
  public static final EnumOption<MainParser> PARSER =
      new EnumOption<>("PARSER", MainParser.XML);

  // XML Parsing

  /** Flag for whitespace chopping. */
  public static final BooleanOption CHOP = new BooleanOption("CHOP", true);
  /** Use internal XML parser. */
  public static final BooleanOption INTPARSE = new BooleanOption("INTPARSE", false);
  /** Strips namespaces. */
  public static final BooleanOption STRIPNS = new BooleanOption("STRIPNS", false);
  /** Flag for parsing DTDs. */
  public static final BooleanOption DTD = new BooleanOption("DTD", false);
  /** Flag for using XInclude. */
  public static final BooleanOption XINCLUDE = new BooleanOption("XINCLUDE", true);
  /** Path to XML Catalog file. */
  public static final StringOption CATFILE = new StringOption("CATFILE", "");

  // Adding documents

  /** Cache new documents before adding them to a database. */
  public static final BooleanOption ADDCACHE = new BooleanOption("ADDCACHE", false);

  // Indexing

  /** Flag for creating a text index. */
  public static final BooleanOption TEXTINDEX = new BooleanOption("TEXTINDEX", true);
  /** Flag for creating an attribute value index. */
  public static final BooleanOption ATTRINDEX = new BooleanOption("ATTRINDEX", true);
  /** Flag for creating a token index. */
  public static final BooleanOption TOKENINDEX = new BooleanOption("TOKENINDEX", false);
  /** Flag for creating a full-text index. */
  public static final BooleanOption FTINDEX = new BooleanOption("FTINDEX", false);

  /** Text index: names to include. */
  public static final StringOption TEXTINCLUDE = new StringOption("TEXTINCLUDE", "");
  /** Attribute index: names to include. */
  public static final StringOption ATTRINCLUDE = new StringOption("ATTRINCLUDE", "");
  /** Token index: names to include. */
  public static final StringOption TOKENINCLUDE = new StringOption("TOKENINCLUDE", "");
  /** Full-text index: names to include. */
  public static final StringOption FTINCLUDE = new StringOption("FTINCLUDE", "");

  /** Maximum length of index entries. */
  public static final NumberOption MAXLEN = new NumberOption("MAXLEN", 96);
  /** Maximum number of name categories. */
  public static final NumberOption MAXCATS = new NumberOption("MAXCATS", 100);
  /** Flag for activating incremental index structures. */
  public static final BooleanOption UPDINDEX = new BooleanOption("UPDINDEX", false);
  /** Flag for automatic index updates. */
  public static final BooleanOption AUTOOPTIMIZE = new BooleanOption("AUTOOPTIMIZE", false);
  /** Index split size. */
  public static final NumberOption SPLITSIZE = new NumberOption("SPLITSIZE", 0);

  // Full-Text

  /** Flag for full-text stemming. */
  public static final BooleanOption STEMMING = new BooleanOption("STEMMING", false);
  /** Flag for full-text case sensitivity. */
  public static final BooleanOption CASESENS = new BooleanOption("CASESENS", false);
  /** Flag for full-text diacritics sensitivity. */
  public static final BooleanOption DIACRITICS = new BooleanOption("DIACRITICS", false);
  /** Language for full-text search index. */
  public static final StringOption LANGUAGE = new StringOption("LANGUAGE", "en");
  /** Path to full-text stopword list. */
  public static final StringOption STOPWORDS = new StringOption("STOPWORDS", "");

  // Query Options

  /** Detailed query information. */
  public static final BooleanOption QUERYINFO = new BooleanOption("QUERYINFO", false);
  /** Flag for mixing updates and items. */
  public static final BooleanOption MIXUPDATES = new BooleanOption("MIXUPDATES", false);
  /** External variables, separated by commas. */
  public static final StringOption BINDINGS = new StringOption("BINDINGS", "");
  /** Flag for the size limit on inlineable functions. */
  public static final NumberOption INLINELIMIT = new NumberOption("INLINELIMIT", 50);
  /** Flag for tail-call optimization. */
  public static final NumberOption TAILCALLS = new NumberOption("TAILCALLS", 256);
  /** Look up documents in databases. */
  public static final BooleanOption WITHDB = new BooleanOption("WITHDB", true);
  /** Favor global database when opening resources. */
  public static final BooleanOption DEFAULTDB = new BooleanOption("DEFAULTDB", false);
  /** Forces database creation for unknown documents. */
  public static final BooleanOption FORCECREATE = new BooleanOption("FORCECREATE", false);
  /** Validate string inputs. */
  public static final BooleanOption CHECKSTRINGS = new BooleanOption("CHECKSTRINGS", true);
  /** Levenshtein default error. */
  public static final NumberOption LSERROR = new NumberOption("LSERROR", 0);
  /** Runs the query results, or only parses it. */
  public static final BooleanOption RUNQUERY = new BooleanOption("RUNQUERY", true);
  /** Number of query executions. */
  public static final NumberOption RUNS = new NumberOption("RUNS", 1);
  /** Flag for enforcing index rewritings. */
  public static final BooleanOption ENFORCEINDEX = new BooleanOption("ENFORCEINDEX", false);
  /** Deep node copies. */
  public static final BooleanOption COPYNODE = new BooleanOption("COPYNODE", true);

  // Serialize

  /** Flag for serializing query results. */
  public static final BooleanOption SERIALIZE = new BooleanOption("SERIALIZE", true);
  /** Serialization parameters. */
  public static final OptionsOption<SerializerOptions> SERIALIZER =
      new OptionsOption<>("SERIALIZER", new SerializerOptions());
  /** Exporter serialization parameters. */
  public static final OptionsOption<SerializerOptions> EXPORTER =
      new OptionsOption<>("EXPORTER", new SerializerOptions());

  /** Prints an XML plan. */
  public static final BooleanOption XMLPLAN = new BooleanOption("XMLPLAN", false);
  /** Creates comprehensive query plan information. */
  public static final BooleanOption FULLPLAN = new BooleanOption("FULLPLAN", false);
  /** Creates the query plan before or after compilation. */
  public static final BooleanOption COMPPLAN = new BooleanOption("COMPPLAN", true);

  /** Flushes the database after each update. */
  public static final BooleanOption AUTOFLUSH = new BooleanOption("AUTOFLUSH", true);
  /** Writes original files back after updates. */
  public static final BooleanOption WRITEBACK = new BooleanOption("WRITEBACK", false);
  /** Maximum number of index occurrences to print. */
  public static final NumberOption MAXSTAT = new NumberOption("MAXSTAT", 30);

  // Other

  /** Options that are adopted from parent options. */
  private static final Option<?>[] INHERIT = { CHOP, INTPARSE, STRIPNS, DTD, XINCLUDE, CATFILE };

  /** Parser. */
  public enum MainParser {
    /** XML.  */ XML,
    /** HTML. */ HTML,
    /** Json. */ JSON,
    /** CSV.  */ CSV,
    /** Text. */ TEXT,
    /** RAW.  */ RAW;

    @Override
    public String toString() {
      return super.toString().toLowerCase(Locale.ENGLISH);
    }
  }

  /**
   * Default constructor.
   */
  public MainOptions() {
    this(true);
  }

  /**
   * Default constructor.
   * @param system parse system properties
   */
  public MainOptions(final boolean system) {
    if(system) setSystem();
  }

  /**
   * Constructor, adopting the specified options.
   * @param options parent options
   */
  public MainOptions(final MainOptions options) {
    super(options);
  }

  /**
   * Constructor, adopting XML parsing options from the specified options.
   * @param options parent options
   * @param xml adopt xml options
   */
  public MainOptions(final MainOptions options, final boolean xml) {
    this(false);
    if(xml) {
      for(final Option<?> option : INHERIT) put(option, options.get(option));
    }
  }

  /**
   * Creates a new options instance with whitespace chopping turned off.
   * @return main options
   */
  public static MainOptions get() {
    final MainOptions mopts = new MainOptions(false);
    mopts.set(CHOP, false);
    return mopts;
  }
}
