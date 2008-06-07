package org.basex.query.xquery;

import org.basex.core.Prop;

/**
 * This class assembles textual information of the XQuery package.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public interface XQText {
  /** FOAR Error type. */ String FOAR = "FOAR";
  /** FOCA Error type. */ String FOCA = "FOCA";
  /** FOCH Error type. */ String FOCH = "FOCH";
  /** FODC Error type. */ String FODC = "FODC";
  /** FODT Error type. */ String FODT = "FODT";
  /** FOER Error type. */ String FOER = "FOER";
  /** FONS Error type. */ String FONS = "FONS";
  /** FORG Error type. */ String FORG = "FORG";
  /** FORX Error type. */ String FORX = "FORX";
  /** FOTY Error type. */ String FOTY = "FOTY";
  /** FTST Error type. */ String FTST = "FTST";
  /** XPDY Error type. */ String XPDY = "XPDY";
  /** XPST Error type. */ String XPST = "XPST";
  /** XPTY Error type. */ String XPTY = "XPTY";
  /** XQDY Error type. */ String XQDY = "XQDY";
  /** XQST Error type. */ String XQST = "XQST";
  /** XQTY Error type. */ String XQTY = "XQTY";

  /** Position info. */
  String STOPPED = "Stopped at ";
  /** Position info. */
  String POSINFO = STOPPED + "line %, column %:" + Prop.NL;
  /** Position info. */
  String POSFILEINFO = STOPPED + "line %, column % in %:" + Prop.NL;
  /** Position info. */
  String FILE = "File not found: %" + Prop.NL;

  /** FOAR0001: Evaluation exception. */
  Object[] DIVZERO = { FOAR, 1, "'%' was divided by zero." };
  /** FOAR0002: Evaluation exception. */
  Object[] DIVFLOW = { FOAR, 2, "Invalid division result: % / %." };
  /** FOAR0002: Evaluation exception. */
  Object[] RANGE = { FOAR, 2, "Value out of range: %" };

  /** FOCA0001: Evaluation exception. */
  Object[] DECRANGE = { FOCA, 1, "Decimal value out of range: %" };
  /** FOCA0002: Evaluation exception. */
  Object[] INVALUE = { FOCA, 2, "Invalid value for %: %" };
  /** FOCA0003: Evaluation exception. */
  Object[] INTRANGE = { FOCA, 3, "Integer value out of range: %" };
  /** FOCA0005: Evaluation exception. */
  Object[] DATECALC = { FOCA, 5, "Invalid % calculation (%)." };

  /** FOCH0001: Evaluation exception. */
  Object[] INVCODE = { FOCH, 1, "Codepoint '%' invalid in %." };
  /** FOCH0002: Evaluation exception. */
  Object[] IMPLCOL = { FOCH, 2, "Unknown collation %." };
  /** FOCH0003: Evaluation exception. */
  Object[] NORMUNI = { FOCH, 3, "Unsupported normalization form (%)." };

  /** FODC0002: Evaluation exception. */
  Object[] NODOC = { FODC, 2, "Document \"%\" could not be loaded." };
  /** FODC0004: Evaluation exception. */
  Object[] COLLINV = { FODC, 2, "Collection name \"%\" is invalid." };
  /** FODC0004: Evaluation exception. */
  Object[] COLLDEF = { FODC, 2, "No default collection found." };
  /** FODC0005: Evaluation exception. */
  Object[] INVDOC = { FODC, 5, "Invalid file path: \"%\"." };

  /** FODT0002: Evaluation exception. */
  Object[] DATEZERO = { FODT, 2, "Invalid infinity/zero calculation in %." };
  /** FODT0003: Evaluation exception. */
  Object[] INVALZONE = { FODT, 3, "Invalid timezone: %." };

  /** FOER0000: Evaluation exception. */
  String FUNERR1 = "Halted on error().";

  /** FONS0004: Evaluation exception. */
  Object[] NSDECL = { FONS, 4, "Namespace prefix not declared: \"%\"." };

  /** FORG0001: Evaluation exception. */
  Object[] INVFUNCAST = { FORG, 1, "Invalid cast from % to %: %." };

  /** FORG0001: Evaluation exception. */
  Object[] LANINVALID = { FORG, 1, "Invalid language: \"%\"" };
  /** FORG0001: Evaluation exception. */
  Object[] INVALIDZONE = { FORG, 1, "Invalid timezone: %." };
  /** FORG0001: Evaluation exception. */
  Object[] CASTBOOL = { FORG, 1, "Invalid boolean cast: \"%\"." };
  /** FORG0001: Evaluation exception. */
  Object[] FUNCAST = { FORG, 1, "Invalid % cast: %." };
  /** FORG0001: Evaluation exception. */
  Object[] DATERANGE = { FORG, 1, "%(\"%\") out of range." };
  /** FORG0001: Evaluation exception. */
  Object[] DATEFORMAT = { FORG, 1, "Wrong % format (Example: '%')." };
  /** FORG0001: Evaluation exception. */
  Object[] QNMINV = { FORG, 1, "Invalid QName: \"%\"" };
  /** FORG0001: Evaluation exception. */
  Object[] FUNPRE = { FORG, 1, "Unknown prefix: \"%\"" };
  /** FORG0002: Evaluation exception. */
  Object[] URIINV = { FORG, 2, "Invalid URI: %" };
  
  /** FORG0003: Evaluation exception. */
  Object[] ZEROONE = { FORG, 3, "Zero or one values expected." };
  /** FORG0004: Evaluation exception. */
  Object[] ONEMORE = { FORG, 4, "One or more values expected." };
  /** FORG0005: Evaluation exception. */
  Object[] EXONE = { FORG, 5, "One or more values expected." };
  /** FORG0006: Evaluation exception. */
  Object[] FUNCMP = { FORG, 6, "%: % expected, % found." };
  /** FORG0006: Evaluation exception. */
  Object[] CONDTYPE = { FORG, 6, "%(%) not allowed as condition type." };
  /** FORG0006: Evaluation exception. */
  Object[] FUNNUMDUR = { FORG, 6, "%: number or duration expected, % found." };
  /** FORG0006: Evaluation exception. */
  Object[] FUNSEQ = { FORG, 6, "Sequence % not allowed as condition type." };
  /** FORG0006: Evaluation exception. */
  Object[] FUNNUM = { FORG, 6, "%: number expected, % found." };
  /** FORG0006: Evaluation exception. */
  Object[] FUNDUR = { FORG, 6, "%: duration expected, % found." };
  /** FORG0006: Evaluation exception. */
  Object[] TYPECMP = { FORG, 6, "% is not comparable." };
  /** FORG0006: Evaluation exception. */
  Object[] FUNJAVA = { FORG, 6, "Invalid arguments for %." };
  /** FORG0008: Function exception. */
  Object[] FUNZONE = { FORG, 8, "% and % have different timezones." };

  /** FORX0001: Evaluation exception. */
  Object[] REGMOD = { FORX, 1, "Invalid regular modifier: '%'" };
  /** FORX0002: Evaluation exception. */
  Object[] REGINV = { FORX, 2, "Invalid regular expression: '%'" };
  /** FORX0003: Evaluation exception. */
  Object[] REGROUP = { FORX, 3, "Pattern matches empty string." };
  /** FORX0004: Evaluation exception. */
  Object[] FUNREGREP = { FORX, 4, "Invalid replacement expression." };
  /** FORX0004: Evaluation exception. */
  Object[] REGERR = { FORX, 4, "Regular expression: '%'" };

  /** FTST0000: Parsing exception. */
  Object[] FTTHES = { FTST, 0, "Thesaurus not supported yet." };
  /** FTST0001: Parsing exception. */
  Object[] FTMILD = { FTST, 1, "'mild not' operator not supported yet." };
  /** FTST0007: Parsing exception. */
  Object[] FTIGNORE = { FTST, 7, "Ignore option not supported yet." };
  /** FTST0008: Parsing exception. */
  Object[] NOSTOPFILE = { FTST, 8, "Stop word file not found: \"%\"." };
  /** FTST0000: Parsing exception. */
  Object[] FTLAN = { FTST, 9, "Language '%' not supported." };

  /** XPDY0002: Evaluation Exception. */
  Object[] XPNOCTX = { XPDY, 2, "No context item set for '%'." };
  /** XPDY0002: Evaluation exception. */
  Object[] XPNODES = { XPDY, 2, "No context item set for %." };
  /** XPDY0050: Evaluation exception. */
  Object[] ROOTDOC = { XPDY, 50, "Context item must be a document node." };
  /** XPDY0050: Evaluation exception. */
  Object[] NOTREAT = { XPDY, 50, "%: % expected, % found." };

  /** XPST0003: Parsing exception. */
  Object[] QUERYEMPTY = { XPST, 3, "Empty query." };
  /** XPST0003: Parsing exception. */
  Object[] QUERYINV = { XPST, 3, "Query contains an illegal character (#%)." };
  /** XPST0003: Parsing exception. */
  Object[] QUOTECLOSE = { XPST, 3, "Expecting closing quote (%)." };
  /** XPST0003: Parsing exception. */
  Object[] NOVALIDATE = { XPST, 3, "Invalid validation expression." };
  /** XPST0003: Parsing exception. */
  Object[] NUMBERWS = { XPST, 3, "Expecting separator after number." };
  /** XPST0003: Parsing exception. */
  Object[] NUMBERINC = { XPST, 3, "Incomplete double value." };
  /** XPST0003: Parsing exception. */
  Object[] QUERYEND = { XPST, 3, "Unexpected end of query: '%'" };
  /** XPST0003: Parsing exception. */
  Object[] QUERYSTEP = { XPST, 3, "Unexpected tokens after %." };
  /** XPST0003: Parsing exception. */
  Object[] CMPEXPR = { XPST, 3, "Comparison is incomplete." };
  /** XPST0003: Parsing exception. */
  Object[] NOTAGNAME = { XPST, 3, "Expecting tag name." };
  /** XPST0003: Parsing exception. */
  Object[] NOLOCSTEP = { XPST, 3, "Incomplete location step." };
  /** XPST0003: Parsing exception. */
  Object[] NOEXPR = { XPST, 3, "Expecting expression." };
  /** XPST0003: Parsing exception. */
  Object[] WRONGCHAR = { XPST, 3, "Expecting %%." };
  /** XPST0003: Parsing exception. */
  String FOUND = ", found \"%\"";
  /** FTST0000: Parsing exception. */
  Object[] FTMATCH = { XPST, 3, "Missing ftcontains option after '%'." };
  
  /** XPST0003: Parsing exception. */
  Object[] WRONGEND = { XPST, 3, "Expecting %." };
  /** XPST0003: Parsing exception. */
  Object[] ENTUNKNOWN = { XPST, 3, "Unknown entity \"%\"." };
  /** XPST0003: Parsing exception. */
  Object[] ENTINVALID = { XPST, 3, "Invalid entity \"%\"." };
  /** XPST0003: Parsing exception. */
  Object[] INCOMPLETE = { XPST, 3, "Incomplete expression." };
  /** XPST0003: Evaluation exception. */
  Object[] EVALUNARY = { XPST, 3, "Unary operator expects a numeric value." };
  /** XPST0003: Parsing exception. */
  Object[] PATHMISS = { XPST, 3, "Expecting location path." };
  /** XPST0003: Parsing exception. */
  Object[] DECLINCOMPLETE = { XPST, 3,
    "Incomplete declaration; expecting 'function', 'variable', ..." };
  /** XPST0003: Parsing exception. */
  Object[] DECLFUNC = { XPST, 3, "Expecting function name." };
  /** XPST0003: Parsing exception. */
  Object[] PREDMISSING = { XPST, 3, "Expecting expression before predicate." };
  /** XPST0003: Parsing exception. */
  Object[] NOVARNAME = { XPST, 3, "Expecting variable name." };
  /** XPST0003: Parsing exception. */
  Object[] NONAME = { XPST, 3, "Expecting name." };
  /** XPST0003: Parsing exception. */
  Object[] TAGWRONG = { XPST, 3, "Start and end tag are different (%/%)." };
  /** XPST0003: Parsing exception. */
  Object[] PIWRONG = { XPST, 3, "Invalid processing-instruction." };
  /** XPST0003: Parsing exception. */
  Object[] NOENCLEXPR = { XPST, 3, "Expecting valid expression after \"{\"." };
  /** XPST0003: Parsing exception. */
  Object[] NODOCCONS = { XPST, 3, "Expecting document construction." };
  /** XPST0003: Parsing exception. */
  Object[] NOTXTCONS = { XPST, 3, "Expecting text construction." };
  /** XPST0003: Parsing exception. */
  Object[] NOCOMCONS = { XPST, 3, "Expecting comment construction." };
  /** XPST0003: Parsing exception. */
  Object[] NOFUNBODY = { XPST, 3, "Expecting function body." };
  /** XPST0003: Parsing exception. */
  Object[] FUNCMISS = { XPST, 3, "Expecting closing bracket for \"%(...\"." };
  /** XPST0003: Parsing exception. */
  Object[] TYPEINVALID = { XPST, 3, "Expecting data type." };
  /** XPST0003: Parsing exception. */
  Object[] NOTYPESWITCH = { XPST, 3, "Incomplete typeswitch expression." };
  /** XPST0003: Parsing exception. */
  Object[] TYPEPAR = { XPST, 3,
      "Expecting '(' after 'typeswitch' expression." };
  /** XPST0003: Parsing exception. */
  Object[] PRAGMAINCOMPLETE = { XPST, 3, "Incomplete pragma expression." };
  /** XPST0003: Parsing exception. */
  Object[] TESTINCOMPLETE = { XPST, 3, "Incomplete node test." };
  /** XPST0003: Parsing exception. */
  Object[] CALCEXPR = { XPST, 3, "Calculation is incomplete." };
  /** XPST0003: Parsing exception. */
  Object[] FLWORSTABLE = { XPST, 3, "Stable order not implemented yet." };
  /** XPST0003: Parsing exception. */
  Object[] NORETURN = { XPST, 3, "Expecting return value." };
  /** XPST0003: Parsing exception. */
  Object[] NOWHERE = { XPST, 3, "Expecting valid expression after 'where'." };
  /** XPST0003: Parsing exception. */
  Object[] ORDERBY = { XPST, 3,
      "Expecting valid expression after 'order by'." };
  /** XPST0003: Parsing exception. */
  Object[] FLWORWHERE = { XPST, 3,
      "Expecting 'where', 'order' or 'return' expression." };
  /** XPST0003: Parsing exception. */
  Object[] FLWORORD = { XPST, 3, "Expecting 'order' or 'return' expression." };
  /** XPST0003: Parsing exception. */
  Object[] FLWORRET = { XPST, 3, "Expecting 'return' expression." };
  /** XPST0003: Parsing exception. */
  Object[] NOSOME = { XPST, 3, "Incomplete quantifier expression." };
  /** XPST0003: Parsing exception. */
  Object[] IFPAR = { XPST, 3, "Expecting '(' after 'if' expression." };
  /** XPST0003: Parsing exception. */
  Object[] NOIF = { XPST, 3, "Incomplete 'if' expression." };
  /** XPST0003: Parsing exception. */
  Object[] NOFOR = { XPST, 3, "Incomplete 'for' expression." };
  /** XPST0003: Parsing exception. */
  Object[] NOLET = { XPST, 3, "Incomplete 'let' expression." };
  /** XPST0003: Parsing exception. */
  Object[] NOCLOSING = { XPST, 3, "Expecting closing tag </%>." };
  /** XPST0003: Parsing exception. */
  Object[] COMCLOSE = { XPST, 3, "Unclosed XQuery comment (: ..." };
  /** XPST0003: Parsing exception. */
  Object[] EXPREMPTY = { XPST, 3, "Unknown function or expression." };
  /** XPST0003: Parsing exception. */
  Object[] NOTYPE = { XPST, 3, "Unknown type %." };
  /** XPST0003: Parsing exception. */
  Object[] PIXML = { XPST, 3, "Illegal PI name: %" };
  /** XPST0003: Parsing exception. */
  Object[] EMPTYSEQOCC = { XPST, 3, "No occurrence indicator defined for %." };
  /** XPST0003: Parsing exception. */
  Object[] TESTINVALID = { XPST, 3, "Invalid % test: \"%\"" };
  /** XPST0003: Parsing exception. */
  Object[] BOUNDS = { XPST, 3, "Integer value % out of bounds." };
  /** XPQST0003: Evaluation exception. */
  Object[] QNAMEINV = { XPST, 3, "Invalid QName." };

  /** XPST0003: Parsing exception. */
  Object[] FTCASE = { XPST, 3, "Only one case option allowed." };
  /** XPST0003: Parsing exception. */
  Object[] FTDIA = { XPST, 3, "Only one diacritics option allowed." };
  /** XPST0003: Parsing exception. */
  Object[] FTRANGE = { XPST, 3, "Expecting full-text range." };

  /** XPST0005: Parsing exception. */
  Object[] COMPSELF = { XPST, 5, "Warning: % won't yield any results." };
  /** XPST0005: Parsing exception. */
  Object[] DOCATTR = { XPST, 5, "Warning: doc nodes can't have attributes." };
  /** XPST0005: Parsing exception. */
  Object[] ATTRCHILD = { XPST, 5, "Warning: attributes can't have children." };

  /** XPST0008: Parsing exception. */
  Object[] VARNOTDEFINED = { XPST, 8, "Undefined variable \"%\"." };
  /** XPST0017: Parsing Exception. */
  Object[] XPARGS = { XPST, 17, "Wrong arguments: % expected." };
  /** XPST0017: Parsing exception. */
  Object[] FUNSIMILAR = { XPST, 17, "Unknown function \"%\" (similar: %())." };
  /** XPST0017: Parsing Exception. */
  Object[] FUNCTYPE = { XPST, 17, "Wrong arguments: %(value) expected." };
  /** XPST0017: Parsing exception. */
  Object[] FUNCUNKNOWN = { XPST, 17, "Unknown function \"%(...)\"." };
  /** XPST0017: Parsing exception. */
  Object[] FUNCJAVA = { XPST, 17, "Unknown Java function \"%(...)\"." };
  /** XPST0051: Parsing exception. */
  Object[] TYPEUNKNOWN = { XPST, 51, "Unknown type %." };
  /** XPST0080: Parsing exception. */
  Object[] CASTUNKNOWN = { XPST, 80, "Unknown cast type %." };
  /** XPST0081: Parsing exception. */
  Object[] PREUNKNOWN = { XPST, 81, "Unknown prefix: \"%\"" };
  /** XPST0081: Parsing exception. */
  Object[] NSMISS = { XPST, 81, "Namespace missing." };

  /** XPTY0004: Typing exception. */
  Object[] XPSORT = { XPTY, 4, "No sequences allowed as sort keys." };
  /** XPTY0004: Typing exception. */
  Object[] XPSEQ = { XPTY, 4, "No sequence % allowed in %." };
  /** XPTY0004: Typing exception. */
  Object[] XPNOSEQ = { XPTY, 4, "No sequence allowed in %." };
  /** XPTY0004: Typing exception. */
  Object[] XPINVCAST = { XPTY, 4, "Invalid cast from % to %: %." };
  /** XPTY0004: Typing exception. */
  Object[] XPCAST = { XPTY, 4, "Invalid % cast: %." };
  /** XPTY0004: Typing Exception. */
  Object[] XPTYPE = { XPTY, 4, "%: % expected, % found." };
  /** XPTY0004: Typing Exception. */
  Object[] XPEMPTYPE = { XPTY, 4, "%: % expected, empty sequence found." };
  /** XPTY0004: Typing exception. */
  Object[] XPEMPTYNUM =
    { XPTY, 4, "%: number expected, empty sequence found." };
  /** XPTY0004: Typing exception. */
  Object[] XPEMPTY = { XPTY, 4, "Empty sequence not allowed in %." };
  /** XPTY0004: Typing exception. */
  Object[] XPDUR = { XPTY, 4, "%: duration expected, % found." };
  /** XPTY0004: Typing Exception. */
  Object[] XPTYPECMP = { XPTY, 4, "% and % can't be compared." };
  /** XPTY0004: Typing exception. */
  Object[] XPTYPENUM = { XPTY, 4, "%: number expected, % found." };
  /** XPTY0004: Typing exception. */
  Object[] XPSINGLE = { XPTY, 4, "% expects single nodes as input." };
  /** XPTY0004: Typing exception. */
  Object[] XPINVNAME = { XPTY, 4, "Invalid name: %" };
  /** XPTY0004: Typing exception. */
  Object[] XPNAME = { XPTY, 4, "Expecting name." };
  /** XPTY0004: Typing exception. */
  Object[] XPATT = { XPTY, 4, "Can't add attributes to a document node." };
  /** XPTY0004: Typing exception. */
  Object[] XPUNARY = { XPTY, 4, "Unary operator expects a numeric value." };
  /** XPTY0004: Typing exception. */
  Object[] CPIWRONG = { XPTY, 4, "% not allowed as PI name: \"%\"" };
  /** XPTY0004: Typing exception. */
  Object[] NAMEWRONG = { XPTY, 4, "Invalid value for name: \"%\"" };
  /** XPTY0004: Typing exception. */
  Object[] NOSCORE = { XPTY, 4, "Variable % needs a scores as result." };
  /** XPTY0004: Typing exception. */
  Object[] FTWEIGHT = { XPTY, 4, "Invalid weight: %." };
  
  /** Example for a Date format. */
  String XPDATE = "2000-12-31";
  /** Example for a Time format. */
  String XPTIME = "23:59:59";
  /** Example for a DateTime format. */
  String XPDTM = XPDATE  + "T" + XPTIME;
  /** Example for a DayTimeDuration format. */
  String XPDTD = "P23DT12M34S";
  /** Example for a YearMonthDuration format. */
  String XPYMD = "P2000Y12M";
  /** Example for a Duration format. */
  String XPDURR = "P2000Y12MT23H12M34S";
  /** Example for a YearMonth format. */
  String XPYMO = "2000-12";
  /** Example for a Year format. */
  String XPYEA = "2000";
  /** Example for a MonthDay format. */
  String XPMDA = "--12-31";
  /** Example for a Day format. */
  String XPDAY = "---31";
  /** Example for a Month format. */
  String XPMON = "--12";

  /** XPTY0018: Typing exception. */
  Object[] EVALNODESVALS = { XPTY, 18,
    "Result yields both nodes and atomic values." };
  /** XPTY0019: Typing exception. */
  Object[] EVALNODES = { XPTY, 19, "Nodes needed for expression '%'." };
  /** XPTY0019: Typing exception. */
  Object[] NODESPATH = { XPTY, 19,
    "Context node required for %; '%' found." };

  /** XQDY0026: Evaluation exception. */
  Object[] CPICONT = { XQDY, 26, "Invalid PI content: \"%\"" };
  /** XQDY0041: Evaluation exception. */
  Object[] CPIINVAL = { XQDY, 41, "Invalid PI name: \"%\"" };
  /** XQDY0044: Evaluation exception. */
  Object[] NSATTCONS = { XQDY, 44,
    "Attribute constructors can't create namespaces." };
  /** XQDY0064: Evaluation exception. */
  Object[] CPIXML = { XQDY, 64, "Illegal PI name: \"%\"" };
  /** XQDY0072: Evaluation exception. */
  Object[] COMINVALID = { XQDY, 72, "Invalid comment." };
  /** XQDY0074: Evaluation exception. */
  Object[] INVAL = { XQDY, 74, "Invalid name: \"%\"" };

  /** XQST0009: Parsing exception. */
  Object[] IMPLSCHEMA = { XQST, 9, "Schema import not supported yet." };
  /** XQST0016: Parsing exception. */
  Object[] IMPLMODULE = { XQST, 16, "Module import not supported yet." };
  /** XQST0022: Parsing exception. */
  Object[] NSCONS = { XQST, 22, "Constant namespace value expected." };
  /** XQST0031: Parsing exception. */
  Object[] XQUERYVER = { XQST, 31, "XQuery version \"%\" not supported." };
  /** XQST0032: Parsing exception. */
  Object[] DUPLBASE = { XQST, 32, "Duplicate 'base-uri' declaration." };
  /** XQST0033: Parsing exception. */
  Object[] DUPLNSDECL = { XQST, 33, "Duplicate declaration of namespace %." };
  /** XQST0034: Parsing exception. */
  Object[] FUNCDEFINED = { XQST, 34, 
      "Duplicate declaration of function \"%\"." };
  /** XQST0038: Parsing exception. */
  Object[] DUPLCOLL = { XQST, 38, "Duplicate 'collation' declaration." };
  /** XQST0076: Parsing exception. */
  Object[] NOCOLL = { XQST, 38, "Unknown collation \"%\"." };
  /** XQST0039: Parsing exception. */
  Object[] FUNCDUPL = { XQST, 39, "Duplicate function argument %." };
  /** XQST0040: Parsing exception. */
  Object[] ATTDUPL = { XQST, 40, "Duplicate attribute \"%\"." };
  /** XQST0040: Parsing exception. */
  Object[] ATTNSDUPL = { XQST, 40, "Duplicate attributes %/%." };
  /** XQST0045: Parsing exception. */
  Object[] NAMERES = { XQST, 45, "Function %(...) uses reserved namespace." };
  /** XQST0045: Parsing exception. */
  Object[] FUNCRES = { XQST, 45, "Node type \"%()\" is reserved." };
  /** XQST0047: Parsing exception. */
  Object[] DUPLMODULE = { XQST, 47, "Module is defined twice: %." };
  /** XQST0047: Parsing exception. */
  Object[] MODNS = { XQST, 48,
    "Declaration % does not match the module namespace." };
  /** XQST0049: Parsing exception. */
  Object[] VARDEFINE = { XQST, 49, "Duplicate definition of %." };
  /** XQST0054: Parsing exception. */
  Object[] XPSTACK = { XQST, 54, "Circular variable definition?" };
  /** XQST0054: Parsing exception. */
  Object[] VARMISSING = { XQST, 54, "Expecting variable declaration." };
  /** XQST0055: Parsing exception. */
  Object[] DUPLCOPYNS = { XQST, 55, "Duplicate 'copy-namespace' declaration." };
  /** XQST0085: Parsing exception. */
  Object[] NSEMPTY = { XQST, 57, "Namespace URI can't be empty." };
  /** XQST0059: Parsing exception. */
  Object[] NOMODULE = { XQST, 59, "Unknown module for namespace \"%\"." };
  /** XQST0059: Parsing exception. */
  Object[] NOMODULEFILE = { XQST, 59, "Module not found: \"%\"." };
  /** XQST0059: Parsing exception. */
  Object[] WRONGMODULE = { XQST, 59, "Wrong uri % in imported module \"%\"." };
  /** XQST0060: Parsing exception. */
  Object[] FUNNONS = { XQST, 60, "Namespace needed for function %(...)." };
  /** XQST0065: Parsing exception. */
  Object[] DUPLORD = { XQST, 65, "Duplicate 'ordering' declaration." };
  /** XQST0066: Parsing exception. */
  Object[] DUPLNS = { XQST, 66, "Duplicate 'default namespace' declaration." };
  /** XQST0067: Parsing exception. */
  Object[] DUPLCONS = { XQST, 67, "Duplicate 'construction' declaration." };
  /** XQST0068: Parsing exception. */
  Object[] DUPLBOUND = { XQST, 68, "Duplicate 'boundary-space' declaration." };
  /** XQST0069: Parsing exception. */
  Object[] DUPLORDEMP = { XQST, 69, "Duplicate 'order empty' declaration." };
  /** XQST0070: Parsing exception. */
  Object[] NSDEF = { XQST, 70, "Can't overwrite namespace %" };
  /** XQST0070: Parsing exception. */
  Object[] NOXMLNS = { XQST, 70, "Can't declare XML namespace." };
  /** XQST0071: Parsing exception. */
  Object[] DUPLNSDEF = { XQST, 71, "Duplicate declaration of namespace %." };
  /** XQST0073: Parsing exception. */
  Object[] CIRCMODULE = { XQST, 73, "Circular module definition." };
  /** XQST0075: Parsing exception. */
  Object[] IMPLVAL = { XQST, 75, "Validation not supported yet." };
  /** XQST0076: Parsing exception. */
  Object[] INVCOLL = { XQST, 76, "Unknown collation \"%\"." };
  /** XQST0079: Parsing exception. */
  Object[] NOPRAGMA = { XQST, 79, "Expecting pragma expression." };
  /** XQST0085: Parsing exception. */
  Object[] NSEMPTYURI = { XQST, 85, "Namespace URI can't be empty." };
  /** XQST0087: Parsing exception. */
  Object[] XQUERYENC2 = { XQST, 87, "Imprecise encoding definition '%'." };
  /** XQST0088: Parsing exception. */
  Object[] NSMODURI = { XQST, 88, "Module namespace can't be empty." };
  /** XQST0089: Parsing exception. */
  Object[] VARDEFINED = { XQST, 89, "Duplicate definition of %." };

  /** XQTY0024: Parsing exception. */
  Object[] NOATTALL = { XQTY, 24, 
      "Attributes must directly follow element nodes." };

  // OPTIMIZATIONS

  /** Compiler info. */
  String OPTPREEVAL = "pre-evaluating %";
  /** Compiler info. */
  String OPTCAST = "casting %";
  /** Compiler info. */
  String OPTDESC = "merging descendant-or-self step(s)";

  /** Evaluation info. */
  String EVALSKIP = "rest of output skipped...";
}
