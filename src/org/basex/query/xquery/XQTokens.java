package org.basex.query.xquery;

import static org.basex.util.Token.*;

/**
 * This class assembles tokens which are used in the XQuery package.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public interface XQTokens {
  
  // PARSER TOKENS ============================================================
  
  /** Parser token. */
  byte[] AND = token("and");
  /** Parser token. */
  byte[] AS = token("as");
  /** Parser token. */
  byte[] ASCENDING = token("ascending");
  /** Parser token. */
  byte[] ASSIGN = token(":=");
  /** Parser token. */
  byte[] AT = token("at");
  /** Parser token. */
  byte[] ATMARK = token("@");
  /** Parser token. */
  byte[] ATTRIBUTE = token("attribute");
  /** Parser token. */
  byte[] BASEURI = token("base-uri");
  /** Parser token. */
  byte[] BOUNDARY = token("boundary-space");
  /** Parser token. */
  byte[] BR1 = token("[");
  /** Parser token. */
  byte[] BR2 = token("]");
  /** Parser token. */
  byte[] BRACE1 = token("{");
  /** Parser token. */
  byte[] BRACE2 = token("}");
  /** Parser token. */
  byte[] BY = token("by");
  /** Parser token. */
  byte[] CASE = token("case");
  /** Parser token. */
  byte[] CAST = token("cast");
  /** Parser token. */
  byte[] CASTABLE = token("castable");
  /** Parser token. */
  byte[] CATCH = token("catch");
  /** Parser token. */
  byte[] CDATA = token("<![CDATA[");
  /** Parser token. */
  byte[] COL2 = token("::");
  /** Parser token. */
  byte[] COLLATION = token("collation");
  /** Parser token. */
  byte[] COMMA = token(",");
  /** Parser token. */
  byte[] COMMENT = token("comment");
  /** Parser token. */
  byte[] CONSTRUCTION = token("construction");
  /** Parser token. */
  byte[] COPYNS = token("copy-namespaces");
  /** Parser token. */
  byte[] MINUS = token("-");
  /** Parser token. */
  byte[] DECLARE = token("declare");
  /** Parser token. */
  byte[] DEFAULT = token("default");
  /** Parser token. */
  byte[] DESCENDING = token("descending");
  /** Parser token. */
  byte[] DIV = token("div");
  /** Parser token. */
  byte[] DOCUMENT = token("document");
  /** Parser token. */
  byte[] DOLLAR = token("$");
  /** Parser token. */
  byte[] DOT2 = token("..");
  /** Parser token. */
  byte[] ELEMENT = token("element");
  /** Parser token. */
  byte[] ELSE = token("else");
  /** Parser token. */
  byte[] EMPTYORD = token("empty");
  /** Parser token. */
  byte[] ENCODING = token("encoding");
  /** Parser token. */
  byte[] EVERY = token("every");
  /** Parser token. */
  byte[] EX = token("!");
  /** Parser token. */
  byte[] EXCEPT = token("except");
  /** Parser token. */
  byte[] EXPRLIST = token("ExprList(");
  /** Parser token. */
  byte[] EXTERNAL = token("external");
  /** Parser token. */
  byte[] FOR = token("for");
  /** Parser token. */
  byte[] FUNCTION = token("function");
  /** Parser token. */
  byte[] GREATEST = token("greatest");
  /** Parser token. */
  byte[] IDIV = token("idiv");
  /** Parser token. */
  byte[] IF = token("if");
  /** Parser token. */
  byte[] IMPORT = token("import");
  /** Parser token. */
  byte[] IN = token("in");
  /** Parser token. */
  byte[] INHERIT = token("inherit");
  /** Parser token. */
  byte[] INSTANCE = token("instance");
  /** Parser token. */
  byte[] INTERSECT = token("intersect");
  /** Parser token. */
  byte[] IS = token("=");
  /** Parser token. */
  byte[] LAX = token("lax");
  /** Parser token. */
  byte[] LBRA = token("<");
  /** Parser token. */
  byte[] LEAST = token("least");
  /** Parser token. */
  byte[] LET = token("let");
  /** Parser token. */
  byte[] MOD = token("mod");
  /** Parser token. */
  byte[] MODULE = token("module");
  /** Parser token. */
  byte[] MULT = token("*");
  /** Parser token. */
  byte[] NAMESPACE = token("namespace");
  /** Parser token. */
  byte[] NOINHERIT = token("no-inherit");
  /** Parser token. */
  byte[] NOPRESERVE = token("no-preserve");
  /** Parser token. */
  byte[] OF = token("of");
  /** Parser token. */
  byte[] ONEZERO = token("1.0");
  /** Parser token. */
  byte[] OPTION = token("option");
  /** Parser token. */
  byte[] OR = token("or");
  /** Parser token. */
  byte[] EMPTYORDER = token("order");
  /** Parser token. */
  byte[] ORDERED = token("ordered");
  /** Parser token. */
  byte[] ORDERING = token("ordering");
  /** Parser token. */
  byte[] PAR1 = token("(");
  /** Parser token. */
  byte[] PAR2 = token(")");
  /** Parser token. */
  byte[] PI = token("processing-instruction");
  /** Parser token. */
  byte[] PIPE = token("|");
  /** Parser token. */
  byte[] PLUS = token("+");
  /** Parser token. */
  byte[] PRAGMA = token("(#");
  /** Parser token. */
  byte[] PRESERVE = token("preserve");
  /** Parser token. */
  byte[] QUEST = token("?");
  /** Parser token. */
  byte[] QUMARK = token("\"");
  /** Parser token. */
  byte[] QUOTE = token("quote");
  /** Parser token. */
  byte[] RETURN = token("return");
  /** Parser token. */
  byte[] SATISFIES = token("satisfies");
  /** Parser token. */
  byte[] SCHEMA = token("schema");
  /** Parser token. */
  byte[] SEMI = token(";");
  /** Parser token. */
  byte[] SLASH = token("/");
  /** Parser token. */
  byte[] SOME = token("some");
  /** Parser token. */
  byte[] STABLE = token("stable");
  /** Parser token. */
  byte[] STRICT = token("strict");
  /** Parser token. */
  byte[] STRIP = token("strip");
  /** Parser token. */
  byte[] TEXT = token("text");
  /** Parser token. */
  byte[] THEN = token("then");
  /** Parser token. */
  byte[] TO = token("to");
  /** Parser token. */
  byte[] TREAT = token("treat");
  /** Parser token. */
  byte[] TRY = token("try");
  /** Parser token. */
  byte[] TYPESWITCH = token("typeswitch");
  /** Parser token. */
  byte[] UNION = token("union");
  /** Parser token. */
  byte[] UNORDERED = token("unordered");
  /** Parser token. */
  byte[] VALIDATE = token("validate");
  /** Parser token. */
  byte[] VARIABLE = token("variable");
  /** Parser token. */
  byte[] VERSION = token("version");
  /** Parser token. */
  byte[] WHERE = token("where");
  /** Parser token. */
  byte[] XQUERY = token("xquery");

  /** Parser fulltext token. */
  byte[] FTOPTION = token("ft-option");
  /** Parser fulltext token. */
  byte[] FTCONTAINS = token("ftcontains");
  /** Parser fulltext token. */
  byte[] ALL = token("all");
  /** Parser fulltext token. */
  byte[] ANY = token("any");
  /** Parser fulltext token. */
  byte[] WORD = token("word");
  /** Parser fulltext token. */
  byte[] WORDS = token("words");
  /** Parser fulltext token. */
  byte[] PHRASE = token("phrase");
  /** Parser fulltext token. */
  byte[] OCCURS = token("occurs");
  /** Parser fulltext token. */
  byte[] TIMES = token("times");
  /** Parser fulltext token. */
  byte[] EXACTLY = token("exactly");
  /** Parser fulltext token. */
  byte[] MOST = token("most");
  /** Parser fulltext token. */
  byte[] FROM = token("from");
  /** Parser fulltext token. */
  byte[] NOT = token("not");
  /** Parser fulltext token. */
  byte[] FTOR = token("ftor");
  /** Parser fulltext token. */
  byte[] FTAND = token("ftand");
  /** Parser fulltext token. */
  byte[] FTNOT = token("ftnot");
  /** Parser fulltext token. */
  byte[] LOWERCASE = token("lowercase");
  /** Parser fulltext token. */
  byte[] UPPERCASE = token("uppercase");
  /** Parser fulltext token. */
  byte[] SENSITIVE = token("sensitive");
  /** Parser fulltext token. */
  byte[] INSENSITIVE = token("insensitive");
  /** Parser fulltext token. */
  byte[] WITH = token("with");
  /** Parser fulltext token. */
  byte[] CONTENT = token("content");
  /** Parser fulltext token. */
  byte[] DIACRITICS = token("diacritics");
  /** Parser fulltext token. */
  byte[] DIFFERENT = token("different");
  /** Parser fulltext token. */
  byte[] DISTANCE = token("distance");
  /** Parser fulltext token. */
  byte[] END = token("end");
  /** Parser fulltext token. */
  byte[] ENTIRE = token("entire");
  /** Parser fulltext token. */
  byte[] LANGUAGE = token("language");
  /** Parser fulltext token. */
  byte[] LEVELS = token("levels");
  /** Parser fulltext token. */
  byte[] PARAGRAPH = token("paragraph");
  /** Parser fulltext token. */
  byte[] PARAGRAPHS = token("paragraphs");
  /** Parser fulltext token. */
  byte[] RELATIONSHIP = token("relationship");
  /** Parser fulltext token. */
  byte[] SAME = token("same");
  /** Parser fulltext token. */
  byte[] SENTENCE = token("sentence");
  /** Parser fulltext token. */
  byte[] SENTENCES = token("sentences");
  /** Parser fulltext token. */
  byte[] START = token("start");
  /** Parser fulltext token. */
  byte[] STEMMING = token("stemming");
  /** Parser fulltext token. */
  byte[] STOP = token("stop");
  /** Parser fulltext token. */
  byte[] THESAURUS = token("thesaurus");
  /** Parser fulltext token. */
  byte[] WILDCARDS = token("wildcards");
  /** Parser fulltext token. */
  byte[] WINDOW = token("window");
  /** Parser fulltext token. */
  byte[] WITHOUT = token("without");
  /** Parser fulltext token. */
  byte[] SCORE = token("score");
  /** Parser fulltext token. */
  byte[] WEIGHT = token("weight");

  // URIS =====================================================================

  /** XML URI. */
  byte[] XMLURI = token("http://www.w3.org/XML/1998/namespace");
  /** Functions URI. */
  byte[] FNURI = token("http://www.w3.org/2005/xpath-functions");
  /** XMLNS URI. */
  byte[] XMLNSURI = token("http://www.w3.org/2000/xmlns");
  /** Local Functions URI. */
  byte[] LOCALURI = token("http://www.w3.org/2005/xquery-local-functions");
  /** XMLSchema URI. */
  byte[] XSURI = token("http://www.w3.org/2001/XMLSchema");
  /** BaseX URI. */
  byte[] BXURI = token("http://www.basex.org");
  /** XMLSchema Instance URI. */
  byte[] XSIURI = token("http://www.w3.org/2001/XMLSchema-instance");
  /** Java prefix. */
  byte[] JAVAPRE = token("java:");
  /** Default collation. */
  byte[] URLCOLL = concat(FNURI, token("/collation/codepoint"));

  // PREFIXES =================================================================

  /** XML token. */
  byte[] XML = token("xml");
  /** Namespaces Declaration. */
  byte[] XMLNS = token("xmlns");
  /** Namespaces Declaration. */
  byte[] XMLNSCOL = token("xmlns:");
  /** Base token. */
  byte[] BASE = token("base");
  /** ID token. */
  byte[] ID = token("id");
  /** IDRef token. */
  byte[] IDREF = token("idref");
  /** FN token. */
  byte[] FN = token("fn");
  /** BaseX token. */
  byte[] BASEX = token("basex");
  /** XS token. */
  byte[] XS = token("xs");
  /** XS token. */
  byte[] XSI = token("xsi");
  /** XS token. */
  byte[] LOCAL = token("local");
  /** Language attribute. */
  byte[] LANG = token("xml:lang");

  // QUERY PLAN ===============================================================
  
  /** Query Plan. */
  byte[] POS = token("pos");
  /** Query Plan. */
  byte[] VAR = token("var");
  /** Query Plan. */
  byte[] TYPE = token("type");
  /** Query Plan. */
  byte[] NAME = token("Name");
  /** Query Plan. */
  byte[] NAM = token("name");
  /** Query Plan. */
  byte[] VALUE = token("Value");
  /** Query Plan. */
  byte[] EVAL = token("eval");
  /** Query Plan. */
  byte[] ITER = token("iter");
  /** Query Plan. */
  byte[] RET = token("Return");
  /** Query Plan. */
  byte[] ARG = token("arg");
  /** Query Plan. */
  byte[] ORDER = token("Order");
  /** Query Plan. */
  byte[] NEGATE = token("Negate");
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
  /** Query Plan. */
  byte[] PREDS = token("Preds");
}
