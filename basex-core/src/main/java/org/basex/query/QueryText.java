package org.basex.query;

import static org.basex.util.Token.*;

import org.basex.util.*;

/**
 * This class assembles text string and tokens required by the XQuery processor
 * implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public interface QueryText {
  // FULL-TEXT TOKENS =========================================================

  /** Parser token. */
  String AFTER = "after";
  /** Parser token. */
  String ALL = "all";
  /** Parser token. */
  String ALLOWING = "allowing";
  /** Parser token. */
  String AND = "and";
  /** Parser token. */
  String ANY = "any";
  /** Parser token. */
  String ARRAYSTR = "array";
  /** Parser token. */
  String AS = "as";
  /** Parser token. */
  String ASCENDING = "ascending";
  /** Parser token. */
  String AT = "at";
  /** Parser token. */
  String ATTRIBUTE = "attribute";
  /** Parser token. */
  String BASE_URI = "base-uri";
  /** Parser token. */
  String BEFORE = "before";
  /** Parser token. */
  String BOUNDARY_SPACE = "boundary-space";
  /** Parser token. */
  String BY = "by";
  /** Parser token. */
  String CASE = "case";
  /** Parser token. */
  String CAST = "cast";
  /** Parser token. */
  String CASTABLE = "castable";
  /** Parser token. */
  String CATCH = "catch";
  /** Parser token. */
  String COLLATION = "collation";
  /** Parser token. */
  String COMMENT = "comment";
  /** Parser token. */
  String CONSTRUCTION = "construction";
  /** Parser token. */
  String CONTAINS = "contains";
  /** Parser token. */
  String CONTENT = "content";
  /** Parser token. */
  String CONTEXT = "context";
  /** Parser token. */
  String COPY = "copy";
  /** Parser token. */
  String COPY_NAMESPACES = "copy-namespaces";
  /** Parser token. */
  String COUNT = "count";
  /** Parser token. */
  String DECIMAL_FORMAT = "decimal-format";
  /** Parser token. */
  String DECLARE = "declare";
  /** Parser token. */
  String DEFAULT = "default";
  /** Parser token. */
  String DELETE = "delete";
  /** Parser token. */
  String DESCENDING = "descending";
  /** Parser token. */
  String DIACRITICS = "diacritics";
  /** Parser token. */
  String DIFFERENT = "different";
  /** Parser token. */
  String DISTANCE = "distance";
  /** Parser token. */
  String DIV = "div";
  /** Parser token. */
  String DOCUMENT = "document";
  /** Parser token. */
  String ELEMENT = "element";
  /** Parser token. */
  String EMPTY_SEQUENCE = "empty-sequence";
  /** Parser token. */
  String ELSE = "else";
  /** Parser token. */
  String EMPTYORD = "empty";
  /** Parser token. */
  String ENCODING = "encoding";
  /** Parser token. */
  String END = "end";
  /** Parser token. */
  String ENTIRE = "entire";
  /** Parser token. */
  String EVERY = "every";
  /** Parser token. */
  String EXACTLY = "exactly";
  /** Parser token. */
  String EXCEPT = "except";
  /** Parser token. */
  String EXTERNAL = "external";
  /** Parser token. */
  String FIRST = "first";
  /** Parser token. */
  String FOR = "for";
  /** Parser token. */
  String FROM = "from";
  /** Parser token. */
  String FT_OPTION = "ft-option";
  /** Parser token. */
  String FTAND = "ftand";
  /** Parser token. */
  String FTNOT = "ftnot";
  /** Parser token. */
  String FTOR = "ftor";
  /** Parser token. */
  String FUZZY = "fuzzy";
  /** Parser token. */
  String FUNCTION = "function";
  /** Parser token. */
  String GREATEST = "greatest";
  /** Parser token. */
  String IDIV = "idiv";
  /** Parser token. */
  String IF = "if";
  /** Parser token. */
  String IMPORT = "import";
  /** Parser token. */
  String IN = "in";
  /** Parser token. */
  String INHERIT = "inherit";
  /** Parser token. */
  String INSENSITIVE = "insensitive";
  /** Parser token. */
  String INSERT = "insert";
  /** Parser token. */
  String INSTANCE = "instance";
  /** Parser token. */
  String INTERSECT = "intersect";
  /** Parser token. */
  String INTO = "into";
  /** Parser token. */
  String ITEMM = "item";
  /** Parser token. */
  String LANGUAGE = "language";
  /** Parser token. */
  String LAST = "last";
  /** Parser token. */
  String LAX = "lax";
  /** Parser token. */
  String LAZY = "lazy";
  /** Parser token. */
  String LEAST = "least";
  /** Parser token. */
  String LET = "let";
  /** Parser token. */
  String LEVELS = "levels";
  /** Parser token. */
  String LOWERCASE = "lowercase";
  /** Parser token. */
  String MAPSTR = "map";
  /** Parser token. */
  String MOD = "mod";
  /** Parser token. */
  String MODIFY = "modify";
  /** Parser token. */
  String MODULE = "module";
  /** Parser token. */
  String MOST = "most";
  /** Parser token. */
  String NEXT = "next";
  /** Parser token. */
  String NSPACE = "namespace";
  /** Parser token. */
  String NO_INHERIT = "no-inherit";
  /** Parser token. */
  String NO_PRESERVE = "no-preserve";
  /** Parser token. */
  String NODE = "node";
  /** Parser token. */
  String NODES = "nodes";
  /** Parser token. */
  String NO = "no";
  /** Parser token. */
  String NOT = "not";
  /** Parser token. */
  String OCCURS = "occurs";
  /** Parser token. */
  String OF = "of";
  /** Parser token. */
  String ONLY = "only";
  /** Parser token. */
  String OPTION = "option";
  /** Parser token. */
  String OR = "or";
  /** Parser token. */
  String ORDER = "order";
  /** Parser token. */
  String ORDERED = "ordered";
  /** Parser token. */
  String ORDERING = "ordering";
  /** Parser token. */
  String PARAGRAPH = "paragraph";
  /** Parser token. */
  String PARAGRAPHS = "paragraphs";
  /** Parser token. */
  String PHRASE = "phrase";
  /** Parser token. */
  String PRESERVE = "preserve";
  /** Parser token. */
  String PREVIOUS = "previous";
  /** Public token. */
  String PRIVATE = "private";
  /** Parser token. */
  String PI = "processing-instruction";
  /** Public token. */
  String PUBLIC = "public";
  /** Parser token. */
  String RELATIONSHIP = "relationship";
  /** Parser token. */
  String RENAME = "rename";
  /** Parser token. */
  String REPLACE = "replace";
  /** Parser token. */
  String RETURN = "return";
  /** Parser token. */
  String REVALIDATION = "revalidation";
  /** Parser token. */
  String SAME = "same";
  /** Parser token. */
  String SATISFIES = "satisfies";
  /** Parser token. */
  String SCHEMA = "schema";
  /** Parser token. */
  String SCHEMA_ATTRIBUTE = "schema-attribute";
  /** Parser token. */
  String SCHEMA_ELEMENT = "schema-element";
  /** Parser token. */
  String SCORE = "score";
  /** Parser token. */
  String SENSITIVE = "sensitive";
  /** Parser token. */
  String SENTENCE = "sentence";
  /** Parser token. */
  String SENTENCES = "sentences";
  /** Parser token. */
  String SEQUENCE = "sequence";
  /** Parser token. */
  String SKIP = "skip";
  /** Parser token. */
  String SLIDING = "sliding";
  /** Parser token. */
  String SOME = "some";
  /** Parser token. */
  String STABLE = "stable";
  /** Parser token. */
  String START = "start";
  /** Parser token. */
  String STEMMING = "stemming";
  /** Parser token. */
  String STOP = "stop";
  /** Parser token. */
  String STRICT = "strict";
  /** Parser token. */
  String STRIP = "strip";
  /** Parser token. */
  String SWITCH = "switch";
  /** Parser token. */
  String THEN = "then";
  /** Parser token. */
  String TIMES = "times";
  /** Parser token. */
  String TEXT = "text";
  /** Parser token. */
  String THESAURUS = "thesaurus";
  /** Parser token. */
  String TO = "to";
  /** Parser token. */
  String TREAT = "treat";
  /** Parser token. */
  String TRY = "try";
  /** Parser token. */
  String TUMBLING = "tumbling";
  /** Parser token. */
  String TYPE = "type";
  /** Parser token. */
  String TYPESWITCH = "typeswitch";
  /** Parser token. */
  String UNION = "union";
  /** Parser token. */
  String UNORDERED = "unordered";
  /** Parser token. */
  String UPDATE = "update";
  /** Parser token. */
  String UPDATING = "updating";
  /** Parser token. */
  String UPPERCASE = "uppercase";
  /** Parser token. */
  String USING = "using";
  /** Parser token. */
  String VALIDATE = "validate";
  /** Parser token. */
  String VALUEE = "value";
  /** Parser token. */
  String VARIABLE = "variable";
  /** Parser token. */
  String VERSION = "version";
  /** Parser token. */
  String WEIGHT = "weight";
  /** Parser token. */
  String WHERE = "where";
  /** Parser token. */
  String GROUP = "group";
  /** Parser token. */
  String WILDCARDS = "wildcards";
  /** Parser token. */
  String WHEN = "when";
  /** Parser token. */
  String WINDOW = "window";
  /** Parser token. */
  String WITH = "with";
  /** Parser token. */
  String WITHOUT = "without";
  /** Parser token. */
  String WORD = "word";
  /** Parser token. */
  String WORDS = "words";
  /** Parser token. */
  String XQUERY = "xquery";

  /** Parser token. */
  String DF_DEC = "decimal-separator";
  /** Parser token. */
  String DF_DIG = "digit";
  /** Parser token. */
  String DF_GRP = "grouping-separator";
  /** Parser token. */
  String DF_EXP = "exponent-separator";
  /** Parser token. */
  String DF_INF = "infinity";
  /** Parser token. */
  String DF_MIN = "minus-sign";
  /** Parser token. */
  String DF_NAN = "NaN";
  /** Parser token. */
  String DF_PAT = "pattern-separator";
  /** Parser token. */
  String DF_PC = "percent";
  /** Parser token. */
  String DF_PM = "per-mille";
  /** Parser token. */
  String DF_ZG = "zero-digit";

  /** Option: read-lock. */
  String READ_LOCK = "read-lock";
  /** Option: write-lock. */
  String WRITE_LOCK = "write-lock";

  // ERROR INFORMATION ========================================================

  /** Skip flag for the syntax highlighter (don't remove!). */
  String IGNORE = null;

  /** Decimal declarations. */
  byte[][] DECFORMATS = tokens(
    DF_DEC, DF_DIG, DF_GRP, DF_EXP, DF_INF, DF_MIN, DF_NAN, DF_PAT, DF_PC, DF_PM, DF_ZG
  );

  /** Parser token. */
  String CONCAT = "||";
  /** Parser token. */
  String ASSIGN = ":=";
  /** Parser token. */
  String SQUARE1 = "[";
  /** Parser token. */
  String SQUARE2 = "]";
  /** Parser token. */
  String EQNAME = "Q{";
  /** Parser token. */
  String CURLY1 = "{";
  /** Parser token. */
  String CURLY2 = "}";
  /** Parser token. */
  String CDATA = "<![CDATA[";
  /** Parser token. */
  String COLS = "::";
  /** Parser token. */
  String COMMA = ",";
  /** Parser token. */
  String DOLLAR = "$";
  /** Parser token. */
  String DOT2 = "..";
  /** Parser token. */
  String IS = "=";
  /** Parser token. */
  String EXCL = "!";
  /** Parser token. */
  String XQ10 = "1.0";
  /** Parser token. */
  String XQ11 = "1.1";
  /** Parser token. */
  String XQ30 = "3.0";
  /** Parser token. */
  String XQ31 = "3.1";
  /** Parser token. */
  String PAREN1 = "(";
  /** Parser token. */
  String PAREN2 = ")";
  /** Parser token. */
  String PIPE = "|";
  /** Parser token. */
  String PRAGMA = "(#";
  /** Parser token. */
  String PRAGMA2 = "#)";
  /** Parser Token. */
  String QUESTION = "?";
  /** Parser Token. */
  String ASTERISK = "*";
  /** Parser token. */
  String ARROW = "=>";

  // TOKENS ===================================================================

  /** Base token. */
  byte[] BASE = token("base");
  /** ID token. */
  byte[] ID = token("id");
  /** IDRef token. */
  byte[] IDREF = token("idref");

  /** Error token. */
  byte[] E_CODE = token("code");
  /** Error token. */
  byte[] E_DESCRIPTION = token("description");
  /** Error token. */
  byte[] E_VALUE = token("value");
  /** Error token. */
  byte[] E_MODULE = token("module");
  /** Error token. */
  byte[] E_LINE_NUMBER = token("line-number");
  /** Error token. */
  byte[] E_COLUM_NUMBER = token("column-number");
  /** Error token. */
  byte[] E_ADDITIONAL = token("additional");

  /** Array token. */
  byte[] ARRAY = token(ARRAYSTR);
  /** Map token. */
  byte[] MAP = token(MAPSTR);

  /** Error prefix. */
  byte[] ERR_PREFIX = token("err");
  /** FN token. */
  byte[] FN_PREFIX = token("fn");
  /** Ann token. */
  byte[] ANN_PREFIX = token("ann");
  /** Math token. */
  byte[] MATH_PREFIX = token("math");
  /** Output token. */
  byte[] OUTPUT_PREFIX = token("output");
  /** BaseX token. */
  byte[] BASEX_PREFIX = token("basex");
  /** BXErr token. */
  byte[] BXERR_PREFIX = token("bxerr");
  /** Admin token. */
  byte[] ADMIN_PREFIX = token("admin");
  /** Client token. */
  byte[] CLIENT_PREFIX = token("client");
  /** Convert token. */
  byte[] CONVERT_PREFIX = token("convert");
  /** CSV token. */
  byte[] CSV_PREFIX = token("csv");
  /** DB token. */
  byte[] DB_PREFIX = token("db");
  /** QUERY token. */
  byte[] QUERY_PREFIX = token("query");
  /** Index token. */
  byte[] INDEX_PREFIX = token("index");
  /** FETCH token. */
  byte[] FETCH_PREFIX = token("fetch");
  /** FT token. */
  byte[] FT_PREFIX = token("ft");
  /** XS token. */
  byte[] XS_PREFIX = token("xs");
  /** XS token. */
  byte[] XSI_PREFIX = token("xsi");
  /** XS token. */
  byte[] LOCAL_PREFIX = token("local");
  /** Archive module token. */
  byte[] ARCHIVE_PREFIX = token("archive");
  /** File module token. */
  byte[] FILE_PREFIX = token("file");
  /** HTTP Client token. */
  byte[] HTTP_PREFIX = token("http");
  /** Output token. */
  byte[] OUT_PREFIX = token("out");
  /** Profiling token. */
  byte[] PROF_PREFIX = token("prof");
  /** Hash token. */
  byte[] HASH_PREFIX = token("hash");
  /** XSLT token. */
  byte[] XSLT_PREFIX = token("xslt");
  /** ZIP token. */
  byte[] ZIP_PREFIX = token("zip");
  /** HOF token. */
  byte[] HOF_PREFIX = token("hof");
  /** HTML token. */
  byte[] HTML_PREFIX = token("html");
  /** JSON token. */
  byte[] JSON_PREFIX = token("json");
  /** MAP token. */
  byte[] MAP_PREFIX = MAP;
  /** Array token. */
  byte[] ARRAY_PREFIX = ARRAY;
  /** Package token. */
  byte[] PKG_PREFIX = token("pkg");
  /** Process token. */
  byte[] PROC_PREFIX = token("proc");
  /** SQL token. */
  byte[] SQL_PREFIX = token("sql");
  /** SQL token. */
  byte[] STREAM_PREFIX = token("stream");
  /** Binary token. */
  byte[] BIN_PREFIX = token("bin");
  /** Cryptography token. */
  byte[] CRYPTO_PREFIX = token("crypto");
  /** Random token. */
  byte[] RANDOM_PREFIX = token("random");
  /** Repository token. */
  byte[] REPO_PREFIX = token("repo");
  /** User token. */
  byte[] USER_PREFIX = token("user");
  /** Validate token. */
  byte[] VLDT_PREFIX = token("validate");
  /** XQDoc token. */
  byte[] INSPECT_PREFIX = token("inspect");
  /** XQuery token. */
  byte[] XQUERY_PREFIX = token(XQUERY);
  /** Unit token. */
  byte[] UNIT_PREFIX = token("unit");
  /** Rest token. */
  byte[] REST_PREFIX = token("rest");
  /** RestXQ token. */
  byte[] RESTXQ_PREFIX = token("restxq");
  /** EXErr token. */
  byte[] EXPERR_PREFIX = token("experr");

  /** Language attribute. */
  byte[] LANG = token("xml:lang");

  // URIS =====================================================================

  /** W3 URI. */
  String W3_URI = "http://www.w3.org";
  /** XML URI. */
  byte[] XML_URI = token(W3_URI + "/XML/1998/namespace");
  /** Functions URI. */
  byte[] FN_URI = token(W3_URI + "/2005/xpath-functions");
  /** Math URI. */
  byte[] MATH_URI = token(W3_URI + "/2005/xpath-functions/math");
  /** XMLNS URI. */
  byte[] XMLNS_URI = token(W3_URI + "/2000/xmlns/");
  /** Local Functions URI. */
  byte[] LOCAL_URI = token(W3_URI + "/2005/xquery-local-functions");
  /** XMLSchema URI. */
  byte[] XS_URI = token(W3_URI + "/2001/XMLSchema");
  /** XMLSchema Instance URI. */
  byte[] XSI_URI = token(W3_URI + "/2001/XMLSchema-instance");
  /** Output URI. */
  byte[] OUTPUT_URI = token(W3_URI + "/2010/xslt-xquery-serialization");
  /** Error URI. */
  byte[] ERROR_URI = token(W3_URI + "/2005/xqt-errors");
  /** Map URI. */
  byte[] MAP_URI = token(W3_URI + "/2005/xpath-functions/map");
  /** Array URI. */
  byte[] ARRAY_URI = token(W3_URI + "/2005/xpath-functions/array");
  /** Annotations URI. */
  byte[] XQ_URI = token(W3_URI + "/2012/xquery");

  /** EXQuery URI. */
  String EXQUERY = "http://exquery.org/ns/";
  /** RESTXQ URI. */
  byte[] REST_URI = token(EXQUERY + "restxq");

  /** EXPath URI. */
  String EXPATH = "http://expath.org/ns/";
  /** URI of Binary Module. */
  byte[] BIN_URI = token(EXPATH + "binary");
  /** URI of Cryptographic Module. */
  byte[] CRYPTO_URI = token(EXPATH + "crypto");
  /** URI of File Module. */
  byte[] FILE_URI = token(EXPATH + "file");
  /** URI of HTTP Client Module. */
  byte[] HTTP_URI = token(EXPATH + "http-client");
  /** URI of Package API. */
  byte[] PKG_URI = token(EXPATH + "pkg");
  /** URI of ZIP Module.*/
  byte[] ZIP_URI = token(EXPATH + "zip");
  /** URI of EXPath errors. */
  byte[] EXPERROR_URI = token(EXPATH + "error");

  /** Project URI. */
  byte[] BASEX_URI = token(Prop.URL);
  /** Project modules. */
  String BXMODULES_URI = Prop.URL + "/modules/";
  /** Project errors. */
  byte[] BXERRORS_URI = token(Prop.URL + "/errors");

  /** Database module URI. */
  byte[] ADMIN_URI = token(BXMODULES_URI + "admin");
  /** Archive module URI.*/
  byte[] ARCHIVE_URI = token(BXMODULES_URI + "archive");
  /** Client module URI. */
  byte[] CLIENT_URI = token(BXMODULES_URI + "client");
  /** Conversion module URI. */
  byte[] CONVERT_URI = token(BXMODULES_URI + "convert");
  /** CSV module URI. */
  byte[] CSV_URI = token(BXMODULES_URI + "csv");
  /** Database module URI. */
  byte[] DB_URI = token(BXMODULES_URI + "db");
  /** Fetch module URI. */
  byte[] FETCH_URI = token(BXMODULES_URI + "fetch");
  /** Full-text module URI. */
  byte[] FT_URI = token(BXMODULES_URI + "ft");
  /** Hash module URI. */
  byte[] HASH_URI = token(BXMODULES_URI + "hash");
  /** Higher-order module URI. */
  byte[] HOF_URI = token(BXMODULES_URI + "hof");
  /** Html module URI. */
  byte[] HTML_URI = token(BXMODULES_URI + "html");
  /** Index module URI. */
  byte[] INDEX_URI = token(BXMODULES_URI + "index");
  /** Inspect module URI. */
  byte[] INSPECT_URI = token(BXMODULES_URI + "inspect");
  /** JSON module URI. */
  byte[] JSON_URI = token(BXMODULES_URI + "json");
  /** Output module URI. */
  byte[] OUT_URI = token(BXMODULES_URI + "out");
  /** Process module URI. */
  byte[] PROC_URI = token(BXMODULES_URI + "proc");
  /** Profiling module URI. */
  byte[] PROF_URI = token(BXMODULES_URI + "prof");
  /** Query module URI. */
  byte[] QUERY_URI = token(BXMODULES_URI + "query");
  /** Random module URI. */
  byte[] RANDOM_URI = token(BXMODULES_URI + "random");
  /** Repository module URI. */
  byte[] REPO_URI = token(BXMODULES_URI + "repo");
  /** SQL module URI. */
  byte[] SQL_URI = token(BXMODULES_URI + "sql");
  /** Streaming module URI. */
  byte[] STREAM_URI = token(BXMODULES_URI + "stream");
  /** Unit module URI. */
  byte[] UNIT_URI = token(BXMODULES_URI + "unit");
  /** User module URI. */
  byte[] USER_URI = token(BXMODULES_URI + "user");
  /** Validate module URI. */
  byte[] VALIDATE_URI = token(BXMODULES_URI + "validate");
  /** XQuery module URI. */
  byte[] XQUERY_URI = token(BXMODULES_URI + "xquery");
  /** XSLT module URI. */
  byte[] XSLT_URI = token(BXMODULES_URI + "xslt");

  /** Java prefix. */
  byte[] JAVAPREF = token("java:");
  /** URI of default collation. */
  byte[] COLLATION_URI = concat(FN_URI, token("/collation/codepoint"));

  /** Supported documentation tags. */
  byte[][] DOC_TAGS = tokens("description", "author", "version", "param",
      "return", "error", "deprecated", "see", "since");
  /** Documentation: description tag. */
  byte[] DOC_DESCRIPTION = token("description");
  /** Documentation: param tag. */
  byte[] DOC_PARAM = token("param");
  /** Documentation: return tag. */
  byte[] DOC_RETURN = token("return");

  // QUERY PLAN ===============================================================

  /** Separator. */
  String SEP = ", ";
  /** Dots. */
  String DOTS = "...";
  /** Query Plan. */
  String ARG = "arg";
  /** Query Plan. */
  String FUNC = "Function";
  /** Query Plan. */
  String VARBL = "Variable";

  /** Query Info: Plan. */
  byte[] PLAN = token("QueryPlan");
  /** Query Plan. */
  byte[] OP = token("op");
  /** Query Plan. */
  byte[] OFFSET = token("offset");
  /** Query Plan. */
  byte[] POS = token("pos");
  /** Query Plan. */
  byte[] VAR = token("var");
  /** Query Plan. */
  byte[] DATA = token("data");
  /** Query Plan. */
  byte[] TYP = token("type");
  /** Query Plan. */
  byte[] NAM = token("name");
  /** Query Plan. */
  byte[] WHR = token("Where");
  /** Query Plan. */
  byte[] RET = token("Return");
  /** Query Plan. */
  byte[] DIR = token("dir");
  /** Query Plan. */
  byte[] PRE = token("pre");
  /** Query Plan. */
  byte[] VAL = token("value");
  /** Query Plan. */
  byte[] SIZE = token("size");
  /** Query Plan. */
  byte[] AXIS = token("axis");
  /** Query Plan. */
  byte[] TEST = token("test");
  /** Minimum. */
  byte[] MIN = token("min");
  /** Maximum. */
  byte[] MAX = token("max");
  /** Infinity. */
  byte[] INF = token("inf");
  /** Tailcall. */
  byte[] TCL = token("tailCall");

  /** Example for a Date format. */
  String XDATE = "2000-12-31";
  /** Example for a Time format. */
  String XTIME = "23:59:59.999";
  /** Example for a DateTime format. */
  String XDTM = XDATE + 'T' + XTIME;
  /** Example for a DayTimeDuration format. */
  String XDTD = "P23DT12M34S";
  /** Example for a YearMonthDuration format. */
  String XYMD = "P2000Y12M";
  /** Example for a Duration format. */
  String XDURR = "P2000Y12MT23H12M34S";
  /** Example for a YearMonth format. */
  String XYMO = "2000-12";
  /** Example for a Year format. */
  String XYEA = "2000";
  /** Example for a MonthDay format. */
  String XMDA = "--12-31";
  /** Example for a Day format. */
  String XDAY = "---31";
  /** Example for a Month format. */
  String XMON = "--12";

  // OPTIMIZATIONS

  /** Optimization info. */
  String OPTDESC = "rewriting descendant-or-self step(s)";
  /** Optimization info. */
  String OPTATOMIC = "atomic evaluation of %";
  /** Optimization info. */
  String OPTPRE = "pre-evaluating %";
  /** Optimization info. */
  String OPTWRITE = "rewriting %";
  /** Optimization info. */
  String OPTFLAT = "flattening %";
  /** Optimization info. */
  String OPTREMOVE = "%: removing %";
  /** Optimization info. */
  String OPTTCE = "marking as tail call: %";
  /** Optimization info. */
  String OPTFORLET = "moving for/let clauses";
  /** Optimization info. */
  String OPTFORTOLET = "rewriting singleton for to let";
  /** Optimization info. */
  String OPTSWAP = "swapping operands: %";
  /** Optimization info. */
  String OPTFLWOR = "simplifying flwor expression";
  /** Optimization info. */
  String OPTINLINE = "inlining %";
  /** Optimization info. */
  String OPTWHERE2 = "rewriting where clause(s)";
  /** Optimization info. */
  String OPTPRED = "rewriting % to predicate(s)";
  /** Optimization info. */
  String OPTCAST = "removing redundant % cast.";
  /** Optimization info. */
  String OPTVAR = "removing variable %";
  /** Optimization info. */
  String OPTNAME = "removing unknown element/attribute %";
  /** Optimization info. */
  String OPTPATH = "removing non-existing path %";
  /** Optimization info. */
  String OPTTXTINDEX = "applying text index for %";
  /** Optimization info. */
  String OPTATVINDEX = "applying attribute index for %";
  /** Optimization info. */
  String OPTFTXINDEX = "applying full-text index for %";
  /** Optimization info. */
  String OPTRNGINDEX = "applying range index for %";
  /** Optimization info. */
  String OPTSRNGINDEX = "applying string range index for %";
  /** Optimization info. */
  String OPTNOINDEX = "removing path with no index results";
  /** Optimization info. */
  String OPTCHILD = "converting % to child steps";
  /** Optimization info. */
  String OPTUNROLL = "unrolling %";
}
