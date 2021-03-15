package org.basex.query;

import static org.basex.util.Token.*;

import org.basex.util.*;

/**
 * This class assembles text string and tokens required by the XQuery processor
 * implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public interface QueryText {

  // PARSER KEYWORDS ==============================================================================

  /** Parser token. */ String AFTER = "after";
  /** Parser token. */ String ALL = "all";
  /** Parser token. */ String ALLOWING = "allowing";
  /** Parser token. */ String AND = "and";
  /** Parser token. */ String ANY = "any";
  /** Parser token. */ String ARRAY = "array";
  /** Parser token. */ String AS = "as";
  /** Parser token. */ String ASCENDING = "ascending";
  /** Parser token. */ String AT = "at";
  /** Parser token. */ String ATTRIBUTE = "attribute";
  /** Parser token. */ String BASE_URI = "base-uri";
  /** Parser token. */ String BEFORE = "before";
  /** Parser token. */ String BOUNDARY_SPACE = "boundary-space";
  /** Parser token. */ String BY = "by";
  /** Parser token. */ String CASE = "case";
  /** Parser token. */ String CAST = "cast";
  /** Parser token. */ String CASTABLE = "castable";
  /** Parser token. */ String CATCH = "catch";
  /** Parser token. */ String COLLATION = "collation";
  /** Parser token. */ String COMMENT = "comment";
  /** Parser token. */ String CONSTRUCTION = "construction";
  /** Parser token. */ String CONTAINS = "contains";
  /** Parser token. */ String CONTENT = "content";
  /** Parser token. */ String CONTEXT = "context";
  /** Parser token. */ String COPY_NAMESPACES = "copy-namespaces";
  /** Parser token. */ String COPY = "copy";
  /** Parser token. */ String COUNT = "count";
  /** Parser token. */ String DECIMAL_FORMAT = "decimal-format";
  /** Parser token. */ String DECLARE = "declare";
  /** Parser token. */ String DEFAULT = "default";
  /** Parser token. */ String DELETE = "delete";
  /** Parser token. */ String DESCENDING = "descending";
  /** Parser token. */ String DIACRITICS = "diacritics";
  /** Parser token. */ String DIFFERENT = "different";
  /** Parser token. */ String DISTANCE = "distance";
  /** Parser token. */ String DIV = "div";
  /** Parser token. */ String DOCUMENT = "document";
  /** Parser token. */ String ELEMENT = "element";
  /** Parser token. */ String ELSE = "else";
  /** Parser token. */ String EMPTY_SEQUENCE = "empty-sequence";
  /** Parser token. */ String EMPTYY = "empty";
  /** Parser token. */ String ENCODING = "encoding";
  /** Parser token. */ String END = "end";
  /** Parser token. */ String ENTIRE = "entire";
  /** Parser token. */ String ERRORS = "errors";
  /** Parser token. */ String EVERY = "every";
  /** Parser token. */ String EXACTLY = "exactly";
  /** Parser token. */ String EXCEPT = "except";
  /** Parser token. */ String EXTERNAL = "external";
  /** Parser token. */ String FIRST = "first";
  /** Parser token. */ String FOR = "for";
  /** Parser token. */ String FROM = "from";
  /** Parser token. */ String FT_OPTION = "ft-option";
  /** Parser token. */ String FTAND = "ftand";
  /** Parser token. */ String FTNOT = "ftnot";
  /** Parser token. */ String FTOR = "ftor";
  /** Parser token. */ String FUNCTION = "function";
  /** Parser token. */ String FUZZY = "fuzzy";
  /** Parser token. */ String GREATEST = "greatest";
  /** Parser token. */ String GROUP = "group";
  /** Parser token. */ String IDIV = "idiv";
  /** Parser token. */ String IF = "if";
  /** Parser token. */ String IMPORT = "import";
  /** Parser token. */ String IN = "in";
  /** Parser token. */ String INHERIT = "inherit";
  /** Parser token. */ String INSENSITIVE = "insensitive";
  /** Parser token. */ String INSERT = "insert";
  /** Parser token. */ String INSTANCE = "instance";
  /** Parser token. */ String INTERSECT = "intersect";
  /** Parser token. */ String INTO = "into";
  /** Parser token. */ String INVOKE = "invoke";
  /** Parser token. */ String ITEM = "item";
  /** Parser token. */ String LANGUAGE = "language";
  /** Parser token. */ String LAST = "last";
  /** Parser token. */ String LAX = "lax";
  /** Parser token. */ String LEAST = "least";
  /** Parser token. */ String LET = "let";
  /** Parser token. */ String LEVELS = "levels";
  /** Parser token. */ String LOWERCASE = "lowercase";
  /** Parser token. */ String MAP = "map";
  /** Parser token. */ String MOD = "mod";
  /** Parser token. */ String MODIFY = "modify";
  /** Parser token. */ String MODULE = "module";
  /** Parser token. */ String MOST = "most";
  /** Parser token. */ String NAMESPACE = "namespace";
  /** Parser token. */ String NEXT = "next";
  /** Parser token. */ String NO_INHERIT = "no-inherit";
  /** Parser token. */ String NO_PRESERVE = "no-preserve";
  /** Parser token. */ String NO = "no";
  /** Parser token. */ String NODE = "node";
  /** Parser token. */ String NODES = "nodes";
  /** Parser token. */ String NON_DETERMINISTIC = "non-deterministic";
  /** Parser token. */ String NOT = "not";
  /** Parser token. */ String OCCURS = "occurs";
  /** Parser token. */ String OF = "of";
  /** Parser token. */ String ONLY = "only";
  /** Parser token. */ String OPTION = "option";
  /** Parser token. */ String OR = "or";
  /** Parser token. */ String ORDER = "order";
  /** Parser token. */ String ORDERED = "ordered";
  /** Parser token. */ String ORDERING = "ordering";
  /** Parser token. */ String PARAGRAPH = "paragraph";
  /** Parser token. */ String PARAGRAPHS = "paragraphs";
  /** Parser token. */ String PHRASE = "phrase";
  /** Parser token. */ String PROCESSING_INSTRUCTION = "processing-instruction";
  /** Parser token. */ String PRESERVE = "preserve";
  /** Parser token. */ String PREVIOUS = "previous";
  /** Parser token. */ String RELATIONSHIP = "relationship";
  /** Parser token. */ String RENAME = "rename";
  /** Parser token. */ String REPLACE = "replace";
  /** Parser token. */ String RETURN = "return";
  /** Parser token. */ String REVALIDATION = "revalidation";
  /** Parser token. */ String SAME = "same";
  /** Parser token. */ String SATISFIES = "satisfies";
  /** Parser token. */ String SCHEMA_ATTRIBUTE = "schema-attribute";
  /** Parser token. */ String SCHEMA_ELEMENT = "schema-element";
  /** Parser token. */ String SCHEMA = "schema";
  /** Parser token. */ String SCORE = "score";
  /** Parser token. */ String SENSITIVE = "sensitive";
  /** Parser token. */ String SENTENCE = "sentence";
  /** Parser token. */ String SENTENCES = "sentences";
  /** Parser token. */ String SEQUENCE = "sequence";
  /** Parser token. */ String SKIP = "skip";
  /** Parser token. */ String SLIDING = "sliding";
  /** Parser token. */ String SOME = "some";
  /** Parser token. */ String STABLE = "stable";
  /** Parser token. */ String START = "start";
  /** Parser token. */ String STEMMING = "stemming";
  /** Parser token. */ String STOP = "stop";
  /** Parser token. */ String STRICT = "strict";
  /** Parser token. */ String STRIP = "strip";
  /** Parser token. */ String SWITCH = "switch";
  /** Parser token. */ String TEXT = "text";
  /** Parser token. */ String THEN = "then";
  /** Parser token. */ String THESAURUS = "thesaurus";
  /** Parser token. */ String TIMES = "times";
  /** Parser token. */ String TO = "to";
  /** Parser token. */ String TRANSFORM = "transform";
  /** Parser token. */ String TREAT = "treat";
  /** Parser token. */ String TRY = "try";
  /** Parser token. */ String TUMBLING = "tumbling";
  /** Parser token. */ String TYPE = "type";
  /** Parser token. */ String TYPESWITCH = "typeswitch";
  /** Parser token. */ String UNION = "union";
  /** Parser token. */ String UNORDERED = "unordered";
  /** Parser token. */ String UPDATE = "update";
  /** Parser token. */ String UPDATING = "updating";
  /** Parser token. */ String UPPERCASE = "uppercase";
  /** Parser token. */ String USING = "using";
  /** Parser token. */ String VALIDATE = "validate";
  /** Parser token. */ String VALUEE = "value";
  /** Parser token. */ String VARIABLE = "variable";
  /** Parser token. */ String VERSION = "version";
  /** Parser token. */ String WEIGHT = "weight";
  /** Parser token. */ String WHEN = "when";
  /** Parser token. */ String WHERE = "where";
  /** Parser token. */ String WILDCARDS = "wildcards";
  /** Parser token. */ String WINDOW = "window";
  /** Parser token. */ String WITH = "with";
  /** Parser token. */ String WITHOUT = "without";
  /** Parser token. */ String WORD = "word";
  /** Parser token. */ String WORDS = "words";
  /** Parser token. */ String XQUERY = "xquery";

  /** Parser token. */ String DF_DEC = "decimal-separator";
  /** Parser token. */ String DF_DIG = "digit";
  /** Parser token. */ String DF_GRP = "grouping-separator";
  /** Parser token. */ String DF_EXP = "exponent-separator";
  /** Parser token. */ String DF_INF = "infinity";
  /** Parser token. */ String DF_MIN = "minus-sign";
  /** Parser token. */ String DF_NAN = "NaN";
  /** Parser token. */ String DF_PAT = "pattern-separator";
  /** Parser token. */ String DF_PC = "percent";
  /** Parser token. */ String DF_PM = "per-mille";
  /** Parser token. */ String DF_ZD = "zero-digit";

  /** Parser token. */ String NAMESPACES = "namespaces";
  /** Parser token. */ String ELEMENT_NAMESPACE = "element-namespace";
  /** Parser token. */ String FUNCTION_NAMESPACE = "function-namespace";
  /** Parser token. */ String DEFAULT_ORDER_EMPTY = "default-order-empty";
  /** Parser token. */ String DECIMAL_FORMATS = "decimal-formats";

  /** Parser token. */ String LOCK = "lock";
  /** Parser token. */ String NON_DETERMNISTIC = "non-deterministic";

  // PARSER KEYWORDS (IGNORED BY THE SYNTAX HIGHLIGHTER) ==========================================

  /** Skip flag for the syntax highlighter (don't remove!). */
  String IGNORE = null;

  /** Parser token. */ String CONCAT = "||";
  /** Parser token. */ String ASSIGN = ":=";
  /** Parser token. */ String EQNAME = "Q{";
  /** Parser token. */ String CDATA = "<![CDATA[";
  /** Parser token. */ String COL = ":";
  /** Parser token. */ String COLS = "::";
  /** Parser token. */ String COLWC = ":*";
  /** Parser token. */ String HSH = "#";
  /** Parser token. */ String SEMICOL = ";";
  /** Parser token. */ String COMMA = ",";
  /** Parser token. */ String DOLLAR = "$";
  /** Parser token. */ String DOTS2 = "..";
  /** Parser token. */ String IS = "=";
  /** Parser token. */ String EXCL = "!";
  /** Parser token. */ String XQ10 = "1.0";
  /** Parser token. */ String XQ11 = "1.1";
  /** Parser token. */ String XQ30 = "3.0";
  /** Parser token. */ String XQ31 = "3.1";
  /** Parser token. */ String PAREN1 = "(";
  /** Parser token. */ String PAREN2 = ")";
  /** Parser token. */ String SQUARE1 = "[";
  /** Parser token. */ String SQUARE2 = "]";
  /** Parser token. */ String CURLY1 = "{";
  /** Parser token. */ String CURLY2 = "}";
  /** Parser token. */ String PIPE = "|";
  /** Parser token. */ String PRAGMA = "(#";
  /** Parser token. */ String PRAGMA2 = "#)";
  /** Parser Token. */ String QUESTION = "?";
  /** Parser Token. */ String ASTERISK = "*";
  /** Parser token. */ String ARROW = "=>";
  /** Parser token. */ String TERNARY1 = "??";
  /** Parser token. */ String TERNARY2 = "!!";
  /** Parser token. */ String ELVIS = "?:";

  /** Java prefix. */ String JAVAPREF = "java:";
  /** New keyword. */ String NEW = "new";

  // PREFIXES =====================================================================================

  /** XQuery prefix. */ byte[] ADMIN_PREFIX = token("admin");
  /** XQuery prefix. */ byte[] ANN_PREFIX = token("ann");
  /** XQuery prefix. */ byte[] ARCHIVE_PREFIX = token("archive");
  /** XQuery prefix. */ byte[] ARRAY_PREFIX = token("array");
  /** XQuery prefix. */ byte[] BASEX_PREFIX = token("basex");
  /** XQuery prefix. */ byte[] BIN_PREFIX = token("bin");
  /** XQuery prefix. */ byte[] CLIENT_PREFIX = token("client");
  /** XQuery prefix. */ byte[] CONVERT_PREFIX = token("convert");
  /** XQuery prefix. */ byte[] CRYPTO_PREFIX = token("crypto");
  /** XQuery prefix. */ byte[] CSV_PREFIX = token("csv");
  /** XQuery prefix. */ byte[] DB_PREFIX = token("db");
  /** XQuery prefix. */ byte[] ERR_PREFIX = token("err");
  /** XQuery prefix. */ byte[] EXPERR_PREFIX = token("experr");
  /** XQuery prefix. */ byte[] FETCH_PREFIX = token("fetch");
  /** XQuery prefix. */ byte[] FILE_PREFIX = token("file");
  /** XQuery prefix. */ byte[] FN_PREFIX = token("fn");
  /** XQuery prefix. */ byte[] FT_PREFIX = token("ft");
  /** XQuery prefix. */ byte[] GEO_PREFIX = token("geo");
  /** XQuery prefix. */ byte[] HASH_PREFIX = token("hash");
  /** XQuery prefix. */ byte[] HOF_PREFIX = token("hof");
  /** XQuery prefix. */ byte[] HTML_PREFIX = token("html");
  /** XQuery prefix. */ byte[] HTTP_PREFIX = token("http");
  /** XQuery prefix. */ byte[] INDEX_PREFIX = token("index");
  /** XQuery prefix. */ byte[] INPUT_PREFIX = token("input");
  /** XQuery prefix. */ byte[] INSPECT_PREFIX = token("inspect");
  /** XQuery prefix. */ byte[] JOBS_PREFIX = token("jobs");
  /** XQuery prefix. */ byte[] JSON_PREFIX = token("json");
  /** XQuery prefix. */ byte[] LAZY_PREFIX = token("lazy");
  /** XQuery prefix. */ byte[] LOCAL_PREFIX = token("local");
  /** XQuery prefix. */ byte[] MAP_PREFIX = token("map");
  /** XQuery prefix. */ byte[] MATH_PREFIX = token("math");
  /** XQuery prefix. */ byte[] OUT_PREFIX = token("out");
  /** XQuery prefix. */ byte[] OUTPUT_PREFIX = token("output");
  /** XQuery prefix. */ byte[] PERM_PREFIX = token("perm");
  /** XQuery prefix. */ byte[] PKG_PREFIX = token("pkg");
  /** XQuery prefix. */ byte[] PROC_PREFIX = token("proc");
  /** XQuery prefix. */ byte[] PROF_PREFIX = token("prof");
  /** XQuery prefix. */ byte[] RANDOM_PREFIX = token("random");
  /** XQuery prefix. */ byte[] REPO_PREFIX = token("repo");
  /** XQuery prefix. */ byte[] REQUEST_PREFIX = token("request");
  /** XQuery prefix. */ byte[] REST_PREFIX = token("rest");
  /** XQuery prefix. */ byte[] SESSION_PREFIX = token("session");
  /** XQuery prefix. */ byte[] SESSIONS_PREFIX = token("sessions");
  /** XQuery prefix. */ byte[] SQL_PREFIX = token("sql");
  /** XQuery prefix. */ byte[] STRINGS_PREFIX = token("strings");
  /** XQuery prefix. */ byte[] UNIT_PREFIX = token("unit");
  /** XQuery prefix. */ byte[] UPDATE_PREFIX = token("update");
  /** XQuery prefix. */ byte[] USER_PREFIX = token("user");
  /** XQuery prefix. */ byte[] UTIL_PREFIX = token("util");
  /** XQuery prefix. */ byte[] VALIDATE_PREFIX = token("validate");
  /** XQuery prefix. */ byte[] WEB_PREFIX = token("web");
  /** XQuery prefix. */ byte[] WS_PREFIX = token("ws");
  /** XQuery prefix. */ byte[] XQUERY_PREFIX = token("xquery");
  /** XQuery prefix. */ byte[] XS_PREFIX = token("xs");
  /** XQuery prefix. */ byte[] XSI_PREFIX = token("xsi");
  /** XQuery prefix. */ byte[] XSLT_PREFIX = token("xslt");
  /** XQuery prefix. */ byte[] ZIP_PREFIX = token("zip");

  // URIS =========================================================================================

  /** W3 URI. */ String W3_URL = "http://www.w3.org";
  /** W3 URI. */ byte[] XML_URI = token(W3_URL + "/XML/1998/namespace");
  /** W3 URI. */ byte[] FN_URI = token(W3_URL + "/2005/xpath-functions");
  /** W3 URI. */ byte[] MATH_URI = token(W3_URL + "/2005/xpath-functions/math");
  /** W3 URI. */ byte[] XMLNS_URI = token(W3_URL + "/2000/xmlns/");
  /** W3 URI. */ byte[] LOCAL_URI = token(W3_URL + "/2005/xquery-local-functions");
  /** W3 URI. */ byte[] XS_URI = token(W3_URL + "/2001/XMLSchema");
  /** W3 URI. */ byte[] XSI_URI = token(W3_URL + "/2001/XMLSchema-instance");
  /** W3 URI. */ byte[] OUTPUT_URI = token(W3_URL + "/2010/xslt-xquery-serialization");
  /** W3 URI. */ byte[] ERROR_URI = token(W3_URL + "/2005/xqt-errors");
  /** W3 URI. */ byte[] MAP_URI = token(W3_URL + "/2005/xpath-functions/map");
  /** W3 URI. */ byte[] ARRAY_URI = token(W3_URL + "/2005/xpath-functions/array");
  /** W3 URI. */ byte[] XQ_URI = token(W3_URL + "/2012/xquery");
  /** W3 URI. */ byte[] COLLATION_URI = concat(FN_URI, token("/collation/codepoint"));

  /** EXPath URI. */ String EXPATH_URI = "http://expath.org/ns/";
  /** EXPath URI. */ byte[] BIN_URI = token(EXPATH_URI + "binary");
  /** EXPath URI. */ byte[] CRYPTO_URI = token(EXPATH_URI + "crypto");
  /** EXPath URI. */ byte[] FILE_URI = token(EXPATH_URI + "file");
  /** EXPath URI. */ byte[] GEO_URI = token(EXPATH_URI + "geo");
  /** EXPath URI. */ byte[] HTTP_URI = token(EXPATH_URI + "http-client");
  /** EXPath URI. */ byte[] PKG_URI = token(EXPATH_URI + "pkg");
  /** EXPath URI. */ byte[] ZIP_URI = token(EXPATH_URI + "zip");
  /** EXPath URI. */ byte[] EXPERROR_URI = token(EXPATH_URI + "error");

  /** EXQuery URI. */ String EXQUERY_URL = "http://exquery.org/ns/";
  /** EXQuery URI. */ byte[] REQUEST_URI = token(EXQUERY_URL + "request");
  /** EXQuery URI. */ byte[] REST_URI = token(EXQUERY_URL + "restxq");

  /** BaseX URI. */ String BASEX_URL = "http://" + Prop.PROJECT + ".org";
  /** BaseX URI. */ byte[] BASEX_URI = token(BASEX_URL);

  /** BaseX URI. */ String BXMODULES_URL = BASEX_URL + "/modules/";
  /** BaseX URI. */ byte[] ADMIN_URI = token(BXMODULES_URL + "admin");
  /** BaseX URI. */ byte[] ARCHIVE_URI = token(BXMODULES_URL + "archive");
  /** BaseX URI. */ byte[] CLIENT_URI = token(BXMODULES_URL + "client");
  /** BaseX URI. */ byte[] CONVERT_URI = token(BXMODULES_URL + "convert");
  /** BaseX URI. */ byte[] CSV_URI = token(BXMODULES_URL + "csv");
  /** BaseX URI. */ byte[] DB_URI = token(BXMODULES_URL + "db");
  /** BaseX URI. */ byte[] FETCH_URI = token(BXMODULES_URL + "fetch");
  /** BaseX URI. */ byte[] FT_URI = token(BXMODULES_URL + "ft");
  /** BaseX URI. */ byte[] HASH_URI = token(BXMODULES_URL + "hash");
  /** BaseX URI. */ byte[] HOF_URI = token(BXMODULES_URL + "hof");
  /** BaseX URI. */ byte[] HTML_URI = token(BXMODULES_URL + "html");
  /** BaseX URI. */ byte[] INDEX_URI = token(BXMODULES_URL + "index");
  /** BaseX URI. */ byte[] INPUT_URI = token(BXMODULES_URL + "input");
  /** BaseX URI. */ byte[] INSPECT_URI = token(BXMODULES_URL + "inspect");
  /** BaseX URI. */ byte[] JOBS_URI = token(BXMODULES_URL + "jobs");
  /** BaseX URI. */ byte[] JSON_URI = token(BXMODULES_URL + "json");
  /** BaseX URI. */ byte[] LAZY_URI = token(BXMODULES_URL + "lazy");
  /** BaseX URI. */ byte[] OUT_URI = token(BXMODULES_URL + "out");
  /** BaseX URI. */ byte[] PERM_URI = token(BXMODULES_URL + "perm");
  /** BaseX URI. */ byte[] PROC_URI = token(BXMODULES_URL + "proc");
  /** BaseX URI. */ byte[] PROF_URI = token(BXMODULES_URL + "prof");
  /** BaseX URI. */ byte[] RANDOM_URI = token(BXMODULES_URL + "random");
  /** BaseX URI. */ byte[] REPO_URI = token(BXMODULES_URL + "repo");
  /** BaseX URI. */ byte[] SQL_URI = token(BXMODULES_URL + "sql");
  /** BaseX URI. */ byte[] SESSION_URI = token(BXMODULES_URL + "session");
  /** BaseX URI. */ byte[] SESSIONS_URI = token(BXMODULES_URL + "sessions");
  /** BaseX URI. */ byte[] STRINGS_URI = token(BXMODULES_URL + "strings");
  /** BaseX URI. */ byte[] UNIT_URI = token(BXMODULES_URL + "unit");
  /** BaseX URI. */ byte[] UPDATE_URI = token(BXMODULES_URL + "update");
  /** BaseX URI. */ byte[] USER_URI = token(BXMODULES_URL + "user");
  /** BaseX URI. */ byte[] UTIL_URI = token(BXMODULES_URL + "util");
  /** BaseX URI. */ byte[] VALIDATE_URI = token(BXMODULES_URL + "validate");
  /** BaseX URI. */ byte[] WEB_URI = token(BXMODULES_URL + "web");
  /** BaseX URI. */ byte[] WS_URI = token(BXMODULES_URL + "ws");
  /** BaseX URI. */ byte[] XQUERY_URI = token(BXMODULES_URL + "xquery");
  /** BaseX URI. */ byte[] XSLT_URI = token(BXMODULES_URL + "xslt");

  // QUERY PLAN ===================================================================================

  /** Query Info. */ String QUERY_PLAN = "QueryPlan";
  /** Query Info. */ String COMPILED = "compiled";
  /** Query Info. */ String OP = "op";
  /** Query Info. */ String VAR = "var";
  /** Query Info. */ String INDEX = "index";
  /** Query Info. */ String NAME = "name";
  /** Query Info. */ String DIR = "dir";
  /** Query Info. */ String PRE = "pre";
  /** Query Info. */ String SIZE = "size";
  /** Query Info. */ String AXIS = "axis";
  /** Query Info. */ String TEST = "test";
  /** Query Info. */ String MIN = "min";
  /** Query Info. */ String MAX = "max";
  /** Query Info. */ String INCLUDE_MIN = "include-min";
  /** Query Info. */ String INCLUDE_MAX = "include-max";
  /** Query Info. */ String INF = "inf";
  /** Query Info. */ String TAILCALL = "tailCall";
  /** Query Info. */ String ENTRIES = "entries";
  /** Query Info. */ String PROMOTE = "promote";
  /** Query Info. */ String DATABASE = "database";
  /** Query Info. */ String ITERATIVE = "iterative";
  /** Query Info. */ String SINGLE = "single";
  /** Query Info. */ String LINE = "line";
  /** Query Info. */ String COLUMN = "column";
  /** Query Info. */ String PATH = "path";

  /** Query Info. */ String MAPASG = ": ";
  /** Query Info. */ String SEP = ", ";
  /** Query Info. */ String DOTS = "...";
  /** Query Info. */ String ARG = "arg";

  // OPTIMIZATIONS ================================================================================

  /** Optimization info. */ String OPTREWRITE = "rewrite";
  /** Optimization info. */ String OPTREWRITE_X_X = "rewrite %: %";
  /** Optimization info. */ String OPTMERGE_X = "merge: %";
  /** Optimization info. */ String OPTTYPE_X = "remove type check: %";
  /** Optimization info. */ String OPTTYPE_X_X = "remove type check: % -> %";
  /** Optimization info. */ String OPTFLAT_X_X = "flatten nested %: %";
  /** Optimization info. */ String OPTTCE_X = "mark as tail call: %";
  /** Optimization info. */ String OPTLET_X = "hoist let clause: %";
  /** Optimization info. */ String OPTFORTOLET_X = "rewrite for to let: %";
  /** Optimization info. */ String OPTSWAP_X = "swap operands: %";
  /** Optimization info. */ String OPTSIMPLE_X_X = "simplify %: %";
  /** Optimization info. */ String OPTINLINE_X = "inline %";
  /** Optimization info. */ String OPTREMOVE_X_X = "remove % from %";
  /** Optimization info. */ String OPTMOVE_X = "move where clause: %";
  /** Optimization info. */ String OPTPRED_X = "rewrite to predicate: %";
  /** Optimization info. */ String OPTVAR_X = "remove unused variable: %";
  /** Optimization info. */ String OPTSTEP_X = "remove step without results: %";
  /** Optimization info. */ String OPTPATH_X = "remove path without results: %";
  /** Optimization info. */ String OPTINDEX_X_X = "apply % index for %";
  /** Optimization info. */ String OPTNORESULTS_X = "no index results: %";
  /** Optimization info. */ String OPTCHILD_X = "convert to child steps: %";
  /** Optimization info. */ String OPTUNROLL_X = "unroll: %";
  /** Optimization info. */ String OPTOPEN_X = "open database \"%\"";

  // MISCELLANEOUS ================================================================================

  /** Base token. */ byte[] BASE = token("base");
  /** Error token. */ byte[] ERROR = token("error");
  /** Language attribute. */ byte[] LANG = token("xml:lang");

  /** Serialization. */ byte[] SERIALIZATION_PARAMETERS = token("serialization-parameters");
  /** Serialization. */ byte[] CHARACTER = token("character");
  /** Serialization. */ byte[] CHARACTER_MAP = token("character-map");
  /** Serialization. */ byte[] MAP_STRING = token("map-string");

  /** Error token. */ byte[] E_CODE = token("code");
  /** Error token. */ byte[] E_DESCRIPTION = token("description");
  /** Error token. */ byte[] E_VALUE = token("value");
  /** Error token. */ byte[] E_MODULE = token("module");
  /** Error token. */ byte[] E_LINE_NUMBER = token("line-number");
  /** Error token. */ byte[] E_COLUMN_NUMBER = token("column-number");
  /** Error token. */ byte[] E_ADDITIONAL = token("additional");

  /** Debugging info. */ String DEBUGLOCAL = "Local Variables";
  /** Debugging info. */ String DEBUGGLOBAL = "Global Variables";

  /** Example for a Date format.              */ String XDATE = "2000-12-31";
  /** Example for a Time format.              */ String XTIME = "23:59:59.999";
  /** Example for a DateTime format.          */ String XDTM = XDATE + 'T' + XTIME;
  /** Example for a DayTimeDuration format.   */ String XDTD = "P23DT12M34S";
  /** Example for a YearMonthDuration format. */ String XYMD = "P2000Y12M";
  /** Example for a Duration format.          */ String XDURR = "P2000Y12MT23H12M34S";
  /** Example for a YearMonth format.         */ String XYMO = "2000-12";
  /** Example for a Year format.              */ String XYEA = "2000";
  /** Example for a MonthDay format.          */ String XMDA = "--12-31";
  /** Example for a Day format.               */ String XDAY = "---31";
  /** Example for a Month format.             */ String XMON = "--12";
}
