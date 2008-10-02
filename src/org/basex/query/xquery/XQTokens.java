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
  String AS = "as";
  /** Parser token. */
  String ASCENDING = "ascending";
  /** Parser token. */
  String ATTRIBUTE = "attribute";
  /** Parser token. */
  String BASEURI = "base-uri";
  /** Parser token. */
  String BOUNDARY = "boundary-space";
  /** Parser token. */
  String BY = "by";
  /** Parser token. */
  String CAST = "cast";
  /** Parser token. */
  String CASTABLE = "castable";
  /** Parser token. */
  String CATCH = "catch";
  /** Parser token. */
  String COLLATION = "collation";
  /** Parser token. */
  String CONSTRUCTION = "construction";
  /** Parser token. */
  String COPYNS = "copy-namespaces";
  /** Parser token. */
  String DECLARE = "declare";
  /** Parser token. */
  String DESCENDING = "descending";
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
  String EVERY = "every";
  /** Parser token. */
  String EXTERNAL = "external";
  /** Parser token. */
  String FOR = "for";
  /** Parser token. */
  String FTOPTION = "ft-option";
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
  String INHERIT = "inherit";
  /** Parser token. */
  String INSTANCE = "instance";
  /** Parser token. */
  String INTERSECT = "intersect";
  /** Parser token. */
  String LAX = "lax";
  /** Parser token. */
  String LET = "let";
  /** Parser token. */
  String MOD = "mod";
  /** Parser token. */
  String MODULE = "module";
  /** Parser token. */
  String NAMESPACE = "namespace";
  /** Parser token. */
  String NOINHERIT = "no-inherit";
  /** Parser token. */
  String NOPRESERVE = "no-preserve";
  /** Parser token. */
  String OF = "of";
  /** Parser token. */
  String OPTION = "option";
  /** Parser token. */
  String EMPTYORDER = "order";
  /** Parser token. */
  String ORDERING = "ordering";
  /** Parser token. */
  String PRESERVE = "preserve";
  /** Parser token. */
  String RETURN = "return";
  /** Parser token. */
  String SATISFIES = "satisfies";
  /** Parser token. */
  String SCHEMA = "schema";
  /** Parser token. */
  String SCORE = "score";
  /** Parser token. */
  String SOME = "some";
  /** Parser token. */
  String STABLE = "stable";
  /** Parser token. */
  String STRICT = "strict";
  /** Parser token. */
  String STRIP = "strip";
  /** Parser token. */
  String THEN = "then";
  /** Parser token. */
  String TREAT = "treat";
  /** Parser token. */
  String TRY = "try";
  /** Parser token. */
  String TYPESWITCH = "typeswitch";
  /** Parser token. */
  String UNORDERED = "unordered";
  /** Parser token. */
  String VALIDATE = "validate";
  /** Parser token. */
  String VARIABLE = "variable";
  /** Parser token. */
  String VERSION = "version";
  /** Parser token. */
  String WEIGHT = "weight";
  /** Parser token. */
  String WHERE = "where";
  /** Parser token. */
  String XQUERY = "xquery";

  /** Skip flag for the syntax highlighter. */
  String SKIP = null;

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
  String COL2 = "::";
  /** Parser token. */
  String COMMA = ",";
  /** Parser token. */
  String DOLLAR = "$";
  /** Parser token. */
  String DOT2 = "..";
  /** Parser token. */
  String IS = "=";
  /** Parser token. */
  String ONEZERO = "1.0";
  /** Parser token. */
  String PAR1 = "(";
  /** Parser token. */
  String PAR2 = ")";
  /** Parser token. */
  String PIPE = "|";
  /** Parser token. */
  String PRAGMA = "(#";
  /** Parser token. */
  byte[] WILD = { '*' };

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
//
  // PREFIXES =================================================================

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
