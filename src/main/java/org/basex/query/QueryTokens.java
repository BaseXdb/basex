package org.basex.query;

import static org.basex.util.Token.*;
import org.basex.core.Text;

/**
 * This class contains common tokens for the query implementations.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public interface QueryTokens {
  // FULL-TEXT TOKENS =========================================================

  /** Parser token. */
  String AFTER = "after";
  /** Parser token. */
  String ALL = "all";
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
  String BASEURI = "base-uri";
  /** Parser token. */
  String BEFORE = "before";
  /** Parser token. */
  String BOUNDARY = "boundary-space";
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
  String COPY = "copy";
  /** Parser token. */
  String COPYNS = "copy-namespaces";
  /** Parser token. */
  String DECFORMAT = "decimal-format";
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
  String FTOPTION = "ft-option";
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
  String LANGUAGE = "language";
  /** Parser token. */
  String LAST = "last";
  /** Parser token. */
  String LAX = "lax";
  /** Parser token. */
  String LEAST = "least";
  /** Parser token. */
  String LET = "let";
  /** Parser token. */
  String LEVELS = "levels";
  /** Parser token. */
  String LOWERCASE = "lowercase";
  /** Parser token. */
  String MOD = "mod";
  /** Parser token. */
  String MODIFY = "modify";
  /** Parser token. */
  String MODULE = "module";
  /** Parser token. */
  String MOST = "most";
  /** Parser token. */
  String NSPACE = "namespace";
  /** Parser token. */
  String NOINHERIT = "no-inherit";
  /** Parser token. */
  String NOPRESERVE = "no-preserve";
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
  String PI = "processing-instruction";
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
  String SCORE = "score";
  /** Parser token. */
  String SCORED = "scored";
  /** Parser token. */
  String SENSITIVE = "sensitive";
  /** Parser token. */
  String SENTENCE = "sentence";
  /** Parser token. */
  String SENTENCES = "sentences";
  /** Parser token. */
  String SKIP = "skip";
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

  /** Skip flag for the syntax highlighter. */
  String IGNORE = null;

  /** Decimal declarations. */
  String[] DECFORMATS = {
    DF_DEC, DF_DIG, DF_GRP, DF_INF, DF_MIN, DF_NAN, DF_PAT, DF_PC, DF_PM, DF_ZG
  };
  /** Updating tokens. */
  String[] UPDATES = { INSERT, DELETE, RENAME, REPLACE };

  /** Parser token. */
  String ASSIGN = ":=";
  /** Parser token. */
  String BR1 = "[";
  /** Parser token. */
  String BR2 = "]";
  /** Parser token. */
  String BRACE1 = "{";
  /** Parser token. */
  String BRACE2 = "}";
  /** Parser token. */
  String CDATA = "<![CDATA[";
  /** Parser token. */
  String COLS = "::";
  /** Parser token. */
  String COMMA = ",";
  /** Parser token. */
  String DOLLAR = "$";
  /** Parser token. */
  String HASH = "#";
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

  /** EXPath URI. */
  String EXPATH = "http://expath.org/ns";
  /** ZIP URI.*/
  byte[] ZIPURI = token(EXPATH + "/zip");
  /** HTTP Client URI. */
  byte[] HTTPURI = token(EXPATH + "/http");

  /** Util module URI. */
  byte[] UTILURI = token(Text.URL + "/util");
  /** DB module URI. */
  byte[] DBURI = token(Text.URL + "/db");
  /** FT module URI. */
  byte[] FTURI = token(Text.URL + "/ft");
  /** File module URI. */
  byte[] FILEURI = token(Text.URL + "/file");
  /** Sentiment URI.*/
  byte[] SENTURI = token(Text.URL + "/sent");

  /** Java prefix. */
  byte[] JAVAPRE = token("java:");
  /** Default collation. */
  byte[] URLCOLL = concat(FNURI, token("/collation/codepoint"));

  // TYPES ====================================================================

  /** AnyType. */
  byte[] ANYTYPE = token("anyType");
  /** AnySimpleType. */
  byte[] ANYSIMPLE = token("anySimpleType");
  /** Untyped. */
  byte[] UNTYPED = token("untyped");

  // PREFIXES =================================================================

  /** Base token. */
  byte[] BASE = token("base");
  /** ID token. */
  byte[] ID = token("id");
  /** IDRef token. */
  byte[] IDREF = token("idref");
  /** FN token. */
  byte[] FN = token("fn");
  /** Math token. */
  byte[] MATH = token("math");
  /** Output token. */
  byte[] OUTPUT = token("output");
  /** DB token. */
  byte[] DB = token("db");
  /** FT token. */
  byte[] FT = token("ft");
  /** XS token. */
  byte[] XS = token("xs");
  /** XS token. */
  byte[] XSI = token("xsi");
  /** XS token. */
  byte[] LOCAL = token("local");
  /** File module token. */
  byte[] FILE = token("file");
  /** HTTP Client token. */
  byte[] HTTP = token("http");
  /** Sentiment token. */
  byte[] SENT = token("sent");
  /** Util token. */
  byte[] UTIL = token("util");
  /** ZIP token. */
  byte[] ZIP = token("zip");

  /** Language attribute. */
  byte[] LANG = token("xml:lang");

  // QUERY PLAN ===============================================================

  /** Separator. */
  String SEP = ", ";
  /** Dots. */
  String DOTS = Text.DOTS;
  /** Query Plan. */
  String ARG = "arg";
  /** Query Plan. */
  String FUNC = "Function";
  /** Query Plan. */
  String FITM = "Function item";
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
  byte[] ITM = token("Item");
  /** Query Plan. */
  byte[] THN = token("Then");
  /** Query Plan. */
  byte[] ELS = token("Else");
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
}
