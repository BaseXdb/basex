package org.basex.query;

import static org.basex.util.Token.*;

import org.basex.core.*;

/**
 * This class assembles text string and tokens required by the XQuery processor
 * implementation.
 *
 * @author BaseX Team 2005-12, BSD License
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
  String JSONSTR = "json";
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

  // ERROR INFORMATION =======================================================

  /** Skip flag for the syntax highlighter (don't remove!). */
  String IGNORE = null;

  /** Decimal declarations. */
  String[] DECFORMATS = {
    DF_DEC, DF_DIG, DF_GRP, DF_INF, DF_MIN, DF_NAN, DF_PAT, DF_PC, DF_PM, DF_ZG
  };

  /** Parser token. */
  String CONCAT = "||";
  /** Parser token. */
  String ASSIGN = ":=";
  /** Parser token. */
  String BR1 = "[";
  /** Parser token. */
  String BR2 = "]";
  /** Parser token. */
  String EQNAME = "Q{";
  /** Parser token. */
  String BRACE1 = "{";
  /** Parser token. */
  String BRACE2 = "}";
  /** Parser token. */
  String CDATA = "<![CDATA[";
  /** Parser token. */
  String COLS = "::";
  /** Parser token. */
  String COUNT = "count";
  /** Parser token. */
  String COMMA = ",";
  /** Parser token. */
  String DOLLAR = "$";
  /** Parser token. */
  String DOT2 = "..";
  /** Parser token. */
  String IS = "=";
  /** Parser token. */
  String XQ10 = "1.0";
  /** Parser token. */
  String XQ11 = "1.1";
  /** Parser token. */
  String XQ30 = "3.0";
  /** Parser token. */
  String PAR1 = "(";
  /** Parser token. */
  String PAR2 = ")";
  /** Parser token. */
  String PIPE = "|";
  /** Parser token. */
  String PRAGMA = "(#";
  /** Parser token. */
  String PRAGMA2 = "#)";
  /** Parser Token. */
  String PLHOLDER = "?";
  /** Parser Token. */
  String ASTERISK = "*";

  // PREFIXES =================================================================

  /** Base token. */
  byte[] BASE = token("base");
  /** ID token. */
  byte[] ID = token("id");
  /** IDRef token. */
  byte[] IDREF = token("idref");
  /** Error token. */
  byte[] ECODE = token("code");
  /** Error token. */
  byte[] EVALUE = token("value");
  /** Error token. */
  byte[] EDESC = token("description");
  /** Error token. */
  byte[] EMODULE = token("module");
  /** Error token. */
  byte[] ELINENUM = token("line-number");
  /** Error token. */
  byte[] ECOLNUM = token("column-number");
  /** Error token. */
  byte[] EADD = token("additional");

  /** Error prefix. */
  byte[] ERR = token("err");
  /** FN token. */
  byte[] FN = token("fn");
  /** Math token. */
  byte[] MATH = token("math");
  /** Option token. */
  byte[] OPT = token("opt");
  /** Output token. */
  byte[] OUTPUT = token("output");
  /** BaseX token. */
  byte[] BASEX = token("basex");
  /** BXErr token. */
  byte[] BXERR = token("bxerr");
  /** Client token. */
  byte[] CLIENT = token("client");
  /** Convert token. */
  byte[] CONVERT = token("convert");
  /** Admin token. */
  byte[] ADMIN = token("admin");
  /** DB token. */
  byte[] DB = token("db");
  /** Index token. */
  byte[] INDEX = token("index");
  /** FETCH token. */
  byte[] FETCH = token("fetch");
  /** FT token. */
  byte[] FT = token("ft");
  /** XS token. */
  byte[] XS = token("xs");
  /** XS token. */
  byte[] XSI = token("xsi");
  /** XS token. */
  byte[] LOCAL = token("local");
  /** Archive module token. */
  byte[] ARCHIVE = token("archive");
  /** File module token. */
  byte[] FILE = token("file");
  /** HTTP Client token. */
  byte[] HTTP = token("http");
  /** Output token. */
  byte[] OUT = token("out");
  /** Profiling token. */
  byte[] PROF = token("prof");
  /** Hash token. */
  byte[] HASH = token("hash");
  /** XSLT token. */
  byte[] XSLT = token("xslt");
  /** ZIP token. */
  byte[] ZIP = token("zip");
  /** HOF token. */
  byte[] HOF = token("hof");
  /** HTML token. */
  byte[] HTML = token("html");
  /** JSON token. */
  byte[] JSON = token(JSONSTR);
  /** MAP token. */
  byte[] MAP = token(MAPSTR);
  /** Package token. */
  byte[] PKG = token("pkg");
  /** Process token. */
  byte[] PROC = token("proc");
  /** SQL token. */
  byte[] SQL = token("sql");
  /** Cryptography token. */
  byte[] CRYPTO = token("crytpo");
  /** Random token. */
  byte[] RANDOM = token("random");
  /** Repository token. */
  byte[] REPO = token("repo");
  /** Validate token. */
  byte[] VLDT = token("validate");
  /** XQuery token. */
  byte[] XQRY = token(XQUERY);
  /** XQUnit token. */
  byte[] XQUNIT = token("xqunit");
  /** RestXQ token. */
  byte[] RESTXQ = token("restxq");
  /** EXErr token. */
  byte[] EXPERR = token("experr");

  /** Language attribute. */
  byte[] LANG = token("xml:lang");

  // URIS =====================================================================

  /** W3 URI. */
  String W3URI = "http://www.w3.org";
  /** XML URI. */
  byte[] XMLURI = token(W3URI + "/XML/1998/namespace");
  /** Functions URI. */
  byte[] FNURI = token(W3URI + "/2005/xpath-functions");
  /** Math URI. */
  byte[] MATHURI = token(W3URI + "/2005/xpath-functions/math");
  /** XMLNS URI. */
  byte[] XMLNSURI = token(W3URI + "/2000/xmlns/");
  /** Local Functions URI. */
  byte[] LOCALURI = token(W3URI + "/2005/xquery-local-functions");
  /** XMLSchema URI. */
  byte[] XSURI = token(W3URI + "/2001/XMLSchema");
  /** XMLSchema Instance URI. */
  byte[] XSIURI = token(W3URI + "/2001/XMLSchema-instance");
  /** Output URI. */
  byte[] OUTPUTURI = token(W3URI + "/2010/xslt-xquery-serialization");
  /** Error URI. */
  byte[] ERRORURI = token(W3URI + "/2005/xqt-errors");
  /** Map URI. */
  byte[] MAPURI = token(W3URI + "/2005/xpath-functions/map");
  /** Option URI. */
  byte[] OPTIONURI = token(W3URI + "/2011/xquery-options");

  /** EXQuery URI. */
  String EXQUERY = "http://exquery.org/ns/";
  /** RESTXQ URI. */
  byte[] RESTXQURI = token(EXQUERY + "restxq");

  /** EXPath URI. */
  String EXPATH = "http://expath.org/ns/";
  /** Cryptographic module URI. */
  byte[] CRYPTOURI = token(EXPATH + "crypto");
  /** File module URI. */
  byte[] FILEURI = token(EXPATH + "file");
  /** HTTP Client URI. */
  byte[] HTTPURI = token(EXPATH + "http-client");
  /** Package API URI. */
  byte[] PKGURI = token(EXPATH + "pkg");
  /** ZIP URI.*/
  byte[] ZIPURI = token(EXPATH + "zip");
  /** EXPath Error URI. */
  byte[] EXPERROR = token(EXPATH + "error");

  /** Project URI. */
  byte[] BASEXURI = token(Text.URL);
  /** Project modules. */
  String BXMODULES = Text.URL + "/modules/";
  /** Project errors. */
  byte[] BXERRORS = token(Text.URL + "/errors");
  /** Archive module URI.*/
  byte[] ARCHIVEURI = token(BXMODULES + "archive");
  /** Client module URI. */
  byte[] CLIENTURI = token(BXMODULES + "client");
  /** Convert module URI. */
  byte[] CONVERTURI = token(BXMODULES + "convert");
  /** Database module URI. */
  byte[] ADMINURI = token(BXMODULES + "admin");
  /** Database module URI. */
  byte[] DBURI = token(BXMODULES + "db");
  /** Fetch module URI. */
  byte[] FETCHURI = token(BXMODULES + "fetch");
  /** Full-text module URI. */
  byte[] FTURI = token(BXMODULES + "ft");
  /** Higher-order module URI. */
  byte[] HOFURI = token(BXMODULES + "hof");
  /** Html module URI. */
  byte[] HTMLURI = token(BXMODULES + "html");
  /** Index module URI. */
  byte[] INDEXURI = token(BXMODULES + "index");
  /** JSON module URI. */
  byte[] JSONURI = token(BXMODULES + "json");
  /** Process module URI. */
  byte[] PROCURI = token(BXMODULES + "proc");
  /** Output module URI. */
  byte[] OUTURI = token(BXMODULES + "out");
  /** XQuery module URI. */
  byte[] XQUERYURI = token(BXMODULES + "xquery");
  /** XQUnit module URI. */
  byte[] XQUNITURI = token(BXMODULES + "xqunit");
  /** Profiling module URI. */
  byte[] PROFURI = token(BXMODULES + "prof");
  /** Hash module URI. */
  byte[] HASHURI = token(BXMODULES + "hash");
  /** XSLT module URI. */
  byte[] XSLTURI = token(BXMODULES + "xslt");
  /** SQL module URI. */
  byte[] SQLURI = token(BXMODULES + "sql");
  /** Random module URI. */
  byte[] RANDOMURI = token(BXMODULES + "random");
  /** Repository module URI. */
  byte[] REPOURI = token(BXMODULES + "repo");
  /** Validate module URI. */
  byte[] VALIDATEURI = token(BXMODULES + "validate");

  /** Java prefix. */
  byte[] JAVAPREF = token("java:");
  /** Default collation. */
  byte[] URLCOLL = concat(FNURI, token("/collation/codepoint"));

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
  /** Minimum . */
  byte[] MIN = token("min");
  /** Attribute name. */
  byte[] MAX = token("max");
  /** Minimum . */
  byte[] INF = token("inf");

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
  String OPTDESC = "simplifying descendant-or-self step(s)";
  /** Optimization info. */
  String OPTATOMIC = "atomic evaluation of %";
  /** Optimization info. */
  String OPTMERGE = "merging axis paths";
  /** Optimization info. */
  String OPTPRE = "pre-evaluating %";
  /** Optimization info. */
  String OPTWRITE = "rewriting %";
  /** Optimization info. */
  String OPTFLAT = "flattening %";
  /** Optimization info. */
  String OPTREMOVE = "%: removing %";
  /** Optimization info. */
  String OPTREMCTX = "removing context expression (.)";
  /** Optimization info. */
  String OPTFORLET = "moving for/let clauses";
  /** Optimization info. */
  String OPTFORTOLET = "rewriting singleton for to let";
  /** Optimization info. */
  String OPTSWAP = "swapping operands: %";
  /** Optimization info. */
  String OPTTEXT = "adding text() step";
  /** Optimization info. */
  String OPTFLWOR = "simplifying flwor expression";
  /** Optimization info. */
  String OPTINLINEFN = "inlining function %(...)";
  /** Optimization info. */
  String OPTINLINE = "inlining %";
  /** Optimization info. */
  String OPTWHERE = "rewriting where clause to predicate(s)";
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
  String OPTTXTINDEX = "applying text index";
  /** Optimization info. */
  String OPTATVINDEX = "applying attribute index";
  /** Optimization info. */
  String OPTFTXINDEX = "applying full-text index";
  /** Optimization info. */
  String OPTRNGINDEX = "applying range index";
  /** Optimization info. */
  String OPTSRNGINDEX = "applying string range index";
  /** Optimization info. */
  String OPTNOINDEX = "removing path with no index results";
  /** Optimization info. */
  String OPTBIND = "binding static variable %";
  /** Optimization info. */
  String OPTCHILD = "converting % to child steps";
}
