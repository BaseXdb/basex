package org.basex.core;

import java.util.*;
import java.util.stream.*;

import javax.xml.transform.*;

import org.basex.build.csv.*;
import org.basex.build.html.*;
import org.basex.build.json.*;
import org.basex.io.serial.*;
import org.basex.util.*;
import org.basex.util.options.*;
import org.w3c.dom.ls.*;
import org.xml.sax.*;

/**
 * This class contains database options which are used all around the project.
 *
 * @author BaseX Team, BSD License
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
  /** Flag for adding remaining files as binary files. */
  public static final BooleanOption ADDRAW = new BooleanOption("ADDRAW", false);
  /** Define CSV parser options. */
  public static final OptionsOption<CsvParserOptions> CSVPARSER =
      new OptionsOption<>("CSVPARSER", new CsvParserOptions());
  /** Define JSON parser options. */
  public static final OptionsOption<JsonParserOptions> JSONPARSER =
      new OptionsOption<>("JSONPARSER", new JsonParserOptions());
  /** Define HTML options. */
  public static final OptionsOption<HtmlOptions> HTMLPARSER =
      new OptionsOption<>("HTMLPARSER", new HtmlOptions());
  /** Define import parser. */
  public static final EnumOption<MainParser> PARSER =
      new EnumOption<>("PARSER", MainParser.XML);

  // XML Parsing

  /** Use internal XML parser. */
  public static final BooleanOption INTPARSE = new BooleanOption("INTPARSE", false);
  /** Strip whitespace. */
  public static final BooleanOption STRIPWS = new BooleanOption("STRIPWS", false);
  /** Strip namespaces. */
  public static final BooleanOption STRIPNS = new BooleanOption("STRIPNS", false);
  /** Flag for parsing DTDs. */
  public static final BooleanOption DTD = new BooleanOption("DTD", false);
  /** Flag for DTD validation. */
  public static final BooleanOption DTDVALIDATION = new BooleanOption("DTDVALIDATION", false);
  /** XSD validation. */
  public static final StringOption XSDVALIDATION = new StringOption("XSDVALIDATION",
      CommonOptions.SKIP);
  /** Flag for handling xsi:schemaLocation and xsi:noNamespaceSchemaLocation attributes. */
  public static final BooleanOption XSILOCATION = new BooleanOption("XSILOCATION", true);
  /** Flag for using XInclude. */
  public static final BooleanOption XINCLUDE = new BooleanOption("XINCLUDE", false);
  /** Path to XML Catalog files. */
  public static final StringOption CATALOG = new StringOption("CATALOG", "");

  // Adding documents

  /** Cache new documents before adding them to a database. */
  public static final BooleanOption ADDCACHE = new BooleanOption("ADDCACHE", false);
  /** Replace existing documents. */
  public static final BooleanOption REPLACE = new BooleanOption("REPLACE", true);

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
  /** Flag for full-text case-sensitivity. */
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
  /** Limit for inlining functions. */
  public static final NumberOption INLINELIMIT = new NumberOption("INLINELIMIT", 50);
  /** Limit for unrolling loops. */
  public static final NumberOption UNROLLLIMIT = new NumberOption("UNROLLLIMIT", 5);
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
  /** Unwrap Java object. */
  public static final EnumOption<WrapOptions> WRAPJAVA =
      new EnumOption<>("WRAPJAVA", WrapOptions.SOME);

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
  /** Creates the query plan before or after optimization. */
  public static final BooleanOption OPTPLAN = new BooleanOption("OPTPLAN", true);

  /** Flushes the database after each update. */
  public static final BooleanOption AUTOFLUSH = new BooleanOption("AUTOFLUSH", true);
  /** Writes original files back after updates. */
  public static final BooleanOption WRITEBACK = new BooleanOption("WRITEBACK", false);
  /** Maximum number of index occurrences to print. */
  public static final NumberOption MAXSTAT = new NumberOption("MAXSTAT", 30);

  // Other

  /** Indexing options. */
  public static final Option<?>[] INDEXING = { MAXCATS, MAXLEN, SPLITSIZE, LANGUAGE, STOPWORDS,
    TEXTINDEX, ATTRINDEX, TOKENINDEX, FTINDEX, TEXTINCLUDE, ATTRINCLUDE, TOKENINCLUDE, FTINCLUDE,
    STEMMING, CASESENS, DIACRITICS, UPDINDEX, AUTOOPTIMIZE };

  /** Mapping of XML parsing options. */
  private static final Map<Option<?>, Option<?>> XMLPARSINGMAP = new HashMap<>();
  static {
    XMLPARSINGMAP.put(CommonOptions.INTPARSE, INTPARSE);
    XMLPARSINGMAP.put(CommonOptions.STRIP_SPACE, STRIPWS);
    XMLPARSINGMAP.put(CommonOptions.STRIPNS, STRIPNS);
    XMLPARSINGMAP.put(CommonOptions.DTD, DTD);
    XMLPARSINGMAP.put(CommonOptions.DTD_VALIDATION, DTDVALIDATION);
    XMLPARSINGMAP.put(CommonOptions.XSD_VALIDATION, XSDVALIDATION);
    XMLPARSINGMAP.put(CommonOptions.XSI_SCHEMA_LOCATION, XSILOCATION);
    XMLPARSINGMAP.put(CommonOptions.XINCLUDE, XINCLUDE);
    XMLPARSINGMAP.put(CommonOptions.CATALOG, CATALOG);
  }

  /** XML parsing options. */
  private static final Option<?>[] XMLPARSING = XMLPARSINGMAP.values().toArray(Option[]::new);
  /** Extended parsing options. */
  public static final Option<?>[] EXTPARSING = { CREATEFILTER, ADDARCHIVES, ARCHIVENAME,
      SKIPCORRUPT, ADDRAW, ADDCACHE, CSVPARSER, JSONPARSER, HTMLPARSER, PARSER };
  /** All parsing options. */
  public static final Option<?>[] PARSING = Stream.concat(Stream.of(XMLPARSING),
      Stream.of(EXTPARSING)).toArray(Option<?>[]::new);
  /** All create options. */
  public static final Option<?>[] CREATING = Stream.concat(Stream.of(INDEXING),
      Stream.of(PARSING)).toArray(Option<?>[]::new);

  /** Parser. */
  public enum MainParser {
    /** XML.  */ XML,
    /** HTML. */ HTML,
    /** Json. */ JSON,
    /** CSV.  */ CSV,
    /** RAW.  */ RAW;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }

  /** Java wrapper. */
  public enum WrapOptions {
    /** INSTANCE. */ INSTANCE,
    /** ALL.      */ ALL,
    /** VOID.     */ VOID,
    /** SOM.      */ SOME,
    /** NONE.     */ NONE;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }

  /** Resolver instance (lazy instantiation). */
  private XMLResolver resolver;

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
   * Constructor, adopting all options from the specified instance.
   * @param options parent options
   */
  public MainOptions(final MainOptions options) {
    super(options);
    resolver = options.resolver;
  }

  /**
   * Constructor, adopting parsing options from the specified instance.
   * @param options parent options
   * @param xml limit to XML parsing options
   */
  public MainOptions(final MainOptions options, final boolean xml) {
    this(false);
    for(final Option<?> option : xml ? XMLPARSING : PARSING) put(option, options.get(option));
    resolver = options.resolver;
  }

  /**
   * Constructor, adopting parsing options from the specified instance.
   * @param options options
   */
  public MainOptions(final Options options) {
    this(false);
    XMLPARSINGMAP.forEach((source, target) -> {
      final Object value = options.get(source);
      if(value != null) put(target, value);
    });
  }

  /**
   * Assigns a resolver, which implements the {@link EntityResolver}, {@link LSResourceResolver},
   *   and {@link URIResolver} interfaces.
   * @param rslvr resolver
   * @throws BaseXException resolver does not implement all required interfaces
   */
  public void setResolver(final Object rslvr) throws BaseXException {
    resolver = new XMLResolver(rslvr);
  }

  /**
   * Assigns the XML resolver from the specified options.
   * @param options main options
   */
  public void setResolver(final MainOptions options) {
    final XMLResolver rslvr = options.resolver;
    put(CATALOG, rslvr.catalog());
    resolver = rslvr;
  }

  /**
   * Returns an XML resolver.
   * @return XML resolver
   */
  public XMLResolver resolver() {
    final String catalog = get(CATALOG);
    if(resolver == null || !catalog.equals(resolver.catalog())) resolver = new XMLResolver(catalog);
    return resolver;
  }
}
