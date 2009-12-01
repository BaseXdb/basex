package org.basex.query;

/**
 * This class assembles textual information of the XQuery package.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public interface QueryText {
  /** BASX Error type. */ String BASX = "BASX";
  /** FOAR Error type. */ String FOAR = "FOAR";
  /** FOCA Error type. */ String FOCA = "FOCA";
  /** FOCH Error type. */ String FOCH = "FOCH";
  /** FODC Error type. */ String FODC = "FODC";
  /** FODT Error type. */ String FODT = "FODT";
  /** FOER Error type. */ String FOER = "FOER";
  /** FONS Error type. */ String FONS = "FONS";
  /** FORG Error type. */ String FORG = "FORG";
  /** FORX Error type. */ String FORX = "FORX";
  /** FOTY Error type. */ String FOUP = "FOUP";
  /** FTDY Error type. */ String FTDY = "FTDY";
  /** FTST Error type. */ String FTST = "FTST";
  /** XPDY Error type. */ String XPDY = "XPDY";
  /** XPST Error type. */ String XPST = "XPST";
  /** XPTY Error type. */ String XPTY = "XPTY";
  /** XQDY Error type. */ String XQDY = "XQDY";
  /** XQST Error type. */ String XQST = "XQST";
  /** XQTY Error type. */ String XQTY = "XQTY";
  /** XUDY Error type. */ String XUDY = "XUDY";
  /** XQUS Error type. */ String XUST = "XUST";
  /** XUTY Error type. */ String XUTY = "XUTY";

  /** BASX0001: Evaluation exception. */
  Object[] NOIDX = { BASX, 1, "Index not available: '%'." };
  /** BASX0002: Evaluation exception. */
  Object[] WHICHIDX = { BASX, 2, "Unknown index: '%'." };
  /** BASX0003: Evaluation exception. */
  Object[] FILEERR = { BASX, 3, "File '%' could not be evaluated." };
  /** BASX0003: Evaluation exception. */
  Object[] NODB = { BASX, 4, "Database '%' not found." };
  /** BASX0003: Evaluation exception. */
  Object[] NOPRE = { BASX, 5, "Pre value '%' out of range." };

  /** FOAR0001: Evaluation exception. */
  Object[] DIVZERO = { FOAR, 1, "'%' was divided by zero." };
  /** FOAR0002: Evaluation exception. */
  Object[] DIVFLOW = { FOAR, 2, "Invalid division result: % / %." };
  /** FOAR0002: Evaluation exception. */
  Object[] RANGE = { FOAR, 2, "Value out of range: %" };

  /** FOCA0002: Evaluation exception. */
  Object[] INVALUE = { FOCA, 2, "Invalid value for %: %" };
  /** FOCA0003: Evaluation exception. */
  Object[] INTRANGE = { FOCA, 3, "Integer value out of range: %" };
  /** FOCA0005: Evaluation exception. */
  Object[] DATECALC = { FOCA, 5, "Invalid % calculation (%)." };

  /** FOCH0001: Evaluation exception. */
  Object[] INVCODE = { FOCH, 1, "Invalid codepoint '%'." };
  /** FOCH0002: Evaluation exception. */
  Object[] IMPLCOL = { FOCH, 2, "Unknown collation %." };
  /** FOCH0003: Evaluation exception. */
  Object[] NORMUNI = { FOCH, 3, "Unsupported normalization form (%)." };

  /** FODC0001: Evaluation exception. */
  Object[] IDDOC = { FODC, 1, "Root must be a document node." };
  /** FODC0002: Evaluation exception. */
  Object[] UNDOC = { FODC, 2, "Document node could not be created (%)." };
  /** FODC0002: Evaluation exception. */
  Object[] COLLINV = { FODC, 2, "Invalid collection \"%\"." };
  /** FODC0002: Evaluation exception. */
  Object[] COLLDEF = { FODC, 2, "No default collection available." };
  /** FODC0002: Evaluation exception. */
  Object[] NODOC = { FODC, 2, "\"%\" could not be opened." };
  /** FODC0004: Evaluation exception. */
  Object[] NOCOLL = { FODC, 4, "Collection \"%\" could not be opened." };
  /** FODC0005: Evaluation exception. */
  Object[] INVDOC = { FODC, 5, "Invalid document \"%\"." };

  /** FODT0002: Evaluation exception. */
  Object[] DATEZERO = { FODT, 2, "Invalid infinity/zero calculation in %." };
  /** FODT0003: Evaluation exception. */
  Object[] INVALZONE = { FODT, 3, "Invalid timezone: %." };

  /** FOER0000: Evaluation exception. */
  String FUNERR1 = "Halted on error().";

  /** FONS0004: Evaluation exception. */
  Object[] NSDECL = { FONS, 4, "Namespace prefix not declared: \"%\"." };

  /** FORG0001: Evaluation exception. */
  Object[] INVALIDZONE = { FORG, 1, "Invalid timezone: %." };
  /** FORG0001: Evaluation exception. */
  Object[] CASTBOOL = { FORG, 1, "Invalid boolean cast: \"%\"." };
  /** FORG0001: Evaluation exception. */
  Object[] FUNCAST = { FORG, 1, "Invalid % cast: %." };
  /** FORG0001: Evaluation exception. */
  Object[] DATERANGE = { FORG, 1, "%(\"%\") out of range." };
  /** FORG0001: Evaluation exception. */
  Object[] DATEFORMAT = { FORG, 1, "Wrong % format: \"%\" (try: \"%\")." };
  /** FORG0001: Evaluation exception. */
  Object[] QNMINV = { FORG, 1, "Invalid QName: \"%\"" };
  /** FORG0002: Evaluation exception. */
  Object[] URIINV = { FORG, 2, "Invalid URI: %" };

  /** FORG0003: Evaluation exception. */
  Object[] ZEROONE = { FORG, 3, "Zero or one value expected." };
  /** FORG0004: Evaluation exception. */
  Object[] ONEMORE = { FORG, 4, "One or more values expected." };
  /** FORG0005: Evaluation exception. */
  Object[] EXONE = { FORG, 5, "Exactly one value expected." };
  /** FORG0006: Evaluation exception. */
  Object[] FUNCMP = { FORG, 6, "%: % expected, % found." };
  /** FORG0006: Evaluation exception. */
  Object[] CONDTYPE = { FORG, 6, "% not allowed as condition type." };
  /** FORG0006: Evaluation exception. */
  Object[] FUNNUMDUR = { FORG, 6, "%: number or duration expected, % found." };
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
  Object[] REGMOD = { FORX, 1, "Invalid regular modifier: '%'." };
  /** FORX0002: Evaluation exception. */
  Object[] REGINV = { FORX, 2, "Invalid regular expression: '%'." };
  /** FORX0003: Evaluation exception. */
  Object[] REGROUP = { FORX, 3, "Pattern matches empty string." };
  /** FORX0004: Evaluation exception. */
  Object[] FUNREGREP = { FORX, 4, "Invalid replacement expression." };
  /** FORX0004: Evaluation exception. */
  Object[] REGERR = { FORX, 4, "Regular expression: '%'." };

  // [LK] to be revised...
  /** FOUP0001: Evaluation exception. */
  Object[] UPFOTYPE = { FOUP, 1, "Document or element expected, % found." };
  /** FOUP0002: Evaluation exception. */
  Object[] UPFOURI = { FOUP, 2, "No valid URI: '%'." };

  /** FTDY0016: Evaluation exception. */
  Object[] FTWEIGHT = { FTDY, 16, "Invalid weight value: %" };
  /** FTDY0017: Evaluation exception. */
  Object[] FTMILD = { FTDY, 17, "Invalid mild not selection." };
  /** FTDY0020: Evaluation exception. */
  Object[] FTREG = { FTDY, 20, "Invalid regular expression: %" };

  /** FTST0000: Parsing exception. */
  Object[] FTFZWC = { FTST, 0, "Only wildcards or Fuzzy search allowed." };
  /** FTST0000: Parsing exception. */
  Object[] THESRNG = { FTST, 0, "Only integers allowed for thesaurus level." };
  /** FTST0007: Parsing exception. */
  Object[] FTIGNORE = { FTST, 7, "Ignore option not supported." };
  /** FTST0008: Parsing exception. */
  Object[] NOSTOPFILE = { FTST, 8, "Stop word file not found: \"%\"." };
  /** FTST0009: Parsing exception. */
  Object[] FTLAN = { FTST, 9, "Language '%' not supported." };
  /** FTST0018: Parsing exception. */
  Object[] NOTHES = { FTST, 18, "Thesaurus not found: \"%\"." };
  /** FTST0019: Parsing exception. */
  Object[] FTDUP = { FTST, 19, "Match option '%' was defined twice." };

  /** XPDY0002: Evaluation Exception. */
  Object[] XPNOCTX = { XPDY, 2, "No context item set for '%'." };
  /** XPDY0050: Evaluation exception. */
  Object[] CTXNODE = { XPDY, 50,
      "Root of the context item must be a document node." };
  /** XPDY0050: Evaluation exception. */
  Object[] NOTREAT = { XPDY, 50, "%: % expected, % found." };
  /** XPDY0050: Evaluation exception. */
  Object[] NOTREATS = { XPDY, 50, "%: % expected, sequence found." };

  /** XPST0003: Parsing exception. */
  Object[] QUERYEMPTY = { XPST, 3, "Empty query." };
  /** XPST0003: Parsing exception. */
  Object[] QUERYINV = { XPST, 3, "Query contains an illegal character (#%)." };
  /** XPST0003: Parsing exception. */
  Object[] NOQUOTE = { XPST, 3, "Expecting quote%." };
  /** XPST0003: Parsing exception. */
  Object[] NOVALIDATE = { XPST, 3, "Invalid validation expression." };
  /** XPST0003: Parsing exception. */
  Object[] NUMBERWS = { XPST, 3, "Expecting separator after number." };
  /** XPST0003: Parsing exception. */
  Object[] NUMBERINC = { XPST, 3, "Incomplete double value." };
  /** XPST0003: Parsing exception. */
  Object[] QUERYEND = { XPST, 3, "Unexpected end of query: '%'." };
  /** XPST0003: Parsing exception. */
  Object[] CMPEXPR = { XPST, 3, "Comparison is incomplete." };
  /** XPST0003: Parsing exception. */
  Object[] NOTAGNAME = { XPST, 3, "Expecting tag name." };
  /** XPST0003: Parsing exception. */
  Object[] NOATTNAME = { XPST, 3, "Expecting attribute name." };
  /** XPST0003: Parsing exception. */
  Object[] NOLOCSTEP = { XPST, 3, "Incomplete location step." };
  /** XPST0003: Parsing exception. */
  Object[] NOEXPR = { XPST, 3, "Expecting expression." };
  /** XPST0003: Parsing exception. */
  Object[] WRONGCHAR = { XPST, 3, "Expecting \"%\"%." };

  /** XPST0003: Parsing exception. */
  Object[] WRONGEND = { XPST, 3, "Expecting \"%\"." };
  /** XPST0003: Parsing exception. */
  Object[] ENTINVALID = { XPST, 3, "%" };
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
  Object[] NORETURN = { XPST, 3, "Expecting return value." };
  /** XPST0003: Parsing exception. */
  Object[] NOWHERE = { XPST, 3, "Expecting valid expression after 'where'." };
  /** XPST0003: Parsing exception. */
  Object[] ORDERBY = { XPST, 3,
      "Expecting valid expression after 'order by'." };
  /** XPST0003: Parsing exception. */
  Object[] GRPBY = {XPST, 3, "Expecting valid expression after 'group by'."};
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
  Object[] PIXML = { XPST, 3, "Illegal PI name: %." };
  /** XPST0003: Parsing exception. */
  Object[] EMPTYSEQOCC = { XPST, 3, "No occurrence indicator defined for %." };
  /** XPST0003: Parsing exception. */
  Object[] TESTINVALID = { XPST, 3, "Invalid % test: %." };
  /** XPST0003: Parsing exception. */
  Object[] BOUNDS = { XPST, 3, "Integer value % out of bounds." };
  /** XPQST0003: Evaluation exception. */
  Object[] QNAMEINV = { XPST, 3, "Invalid QName." };

  /** XPST0003: Parsing exception. */
  Object[] FTRANGE = { XPST, 3, "Expecting full-text range." };
  /** XPST0003: Parsing exception. */
  Object[] FTSTOP = { XPST, 3, "Stop words expected." };
  /** XPST0003: Parsing exception. */
  Object[] FTMATCH = { XPST, 3, "Unknown match option." };

  /** XPST0005: Parsing exception. */
  Object[] COMPSELF = { XPST, 5, "Warning: '%' won't yield any results." };

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
  Object[] PREUNKNOWN = { XPST, 81, "Unknown prefix: \"%\"." };
  /** XPST0081: Parsing exception. */
  Object[] NSMISS = { XPST, 81, "Namespace missing." };

  /** XPTY0004: Typing exception. */
  Object[] XPSORT = { XPTY, 4, "No sequences allowed as sort keys." };
  /** XPTY0004: Typing exception. */
  Object[] XPSEQ = { XPTY, 4, "No sequence % allowed." };
  /** XPTY0004: Typing exception. */
  Object[] XPEMPTY = { XPTY, 4, "Empty sequence not allowed." };
  /** XPTY0004: Typing exception. */
  Object[] XPINVCAST = { XPTY, 4, "Invalid cast from % to %: %." };
  /** XPTY0004: Typing exception. */
  Object[] XPCAST = { XPTY, 4, "Invalid %(%) cast." };
  /** XPTY0004: Typing Exception. */
  Object[] XPTYPE = { XPTY, 4, "%: % expected, % found." };
  /** XPTY0004: Typing Exception. */
  Object[] XPEMPTYPE = { XPTY, 4, "%: % expected, empty sequence found." };
  /** XPTY0004: Typing exception. */
  Object[] XPEMPTYNUM =
    { XPTY, 4, "%: number expected, empty sequence found." };
  /** XPTY0004: Typing exception. */
  Object[] XPDUR = { XPTY, 4, "%: duration expected, % found." };
  /** XPTY0004: Typing Exception. */
  Object[] XPTYPECMP = { XPTY, 4, "% and % can't be compared." };
  /** XPTY0004: Typing exception. */
  Object[] XPTYPENUM = { XPTY, 4, "%: number expected, % found." };
  /** XPTY0004: Typing exception. */
  Object[] XPINVNAME = { XPTY, 4, "Invalid name: %." };
  /** XPTY0004: Typing exception. */
  Object[] XPNAME = { XPTY, 4, "Expecting name." };
  /** XPTY0004: Typing exception. */
  Object[] XPATT = { XPTY, 4, "Can't add attributes to a document node." };
  /** XPTY0004: Typing exception. */
  Object[] CPIWRONG = { XPTY, 4, "% not allowed as PI name: \"%\"." };
  /** XPTY0004: Typing exception. */
  Object[] NAMEWRONG = { XPTY, 4, "Invalid QName: \"%\"." };

  /** Example for a Date format. */
  String XDATE = "2000-12-31";
  /** Example for a Time format. */
  String XTIME = "23:59:59";
  /** Example for a DateTime format. */
  String XDTM = XDATE + "T" + XTIME;
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

  /** XPTY0018: Typing exception. */
  Object[] EVALNODESVALS = { XPTY, 18,
    "Result yields both nodes and atomic values." };
  /** XPTY0019: Typing exception. */
  Object[] EVALNODES = { XPTY, 19, "Nodes needed for expression '%'." };
  /** XPTY0019: Typing exception. */
  Object[] NODESPATH = { XPTY, 19, "Context node required for %; % found." };

  /** XPDY0002: Parsing exception. */
  Object[] VAREMPTY = { XQDY, 2, "No value defined for \"%\"." };
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
  Object[] IMPLSCHEMA = { XQST, 9, "Schema import not supported." };
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
  Object[] COLLWHICH = { XQST, 38, "Unknown collation \"%\"." };
  /** XQST0039: Parsing exception. */
  Object[] FUNCDUPL = { XQST, 39, "Duplicate function argument %." };
  /** XQST0040: Parsing exception. */
  Object[] ATTDUPL = { XQST, 40, "Duplicate attribute \"%\"." };
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
  Object[] DUPLNSDEF = { XQST, 71,
      "Duplicate declaration of namespace \"%\"." };
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
  Object[] NOATTALL = { XQTY, 24, "Attribute must follow root element." };

  //[LK] xquery update error msgs, work on texts
  /** XUDY0009: XQuery Update dynamic exception. */
  Object[] UPNOPAR = { XUDY, 9, "Target % has no parent." };
  /** XUDY0014: XQuery Update dynamic exception. */
  Object[] UPNOTCOPIED = { XUDY, 14, "% not copied by copy clause." };
  /** XUDY0015: XQuery Update dynamic exception. */
  Object[] UPMULTREN = { XUDY, 15, "Multiple renames on %." };
  /** XUDY0016: XQuery Update dynamic exception. */
  Object[] UPMULTREPL = { XUDY, 16, "Multiple replaces on %." };
  /** XUDY0017: XQuery Update dynamic exception. */
  Object[] UPMULTREPV = { XUDY, 17, "Multiple replaces on %." };
  /** XUDY0021: XQuery Update dynamic exception. */
  Object[] UPATTDUPL = { XUDY, 21, "Duplicate attribute \"%\"." };
  /** XUDY0023: XQuery Update dynamic exception. */
  Object[] UPCONFNS = { XUDY, 23, "Conflicts with existing namespaces." };
  /** XUDY0024: XQuery Update dynamic exception.
  Object[] UPCONFNS = { XUDY, 24, "Namespace conflicts." };
  */
  /** XUDY0027: XQuery Update dynamic exception. */
  Object[] UPSEQEMP = { XUDY, 27,
      "Target expression yields an empty sequence." };
  /** XUDY0029: XQuery Update dynamic exception. */
  Object[] UPPAREMPTY = { XUDY, 29, "Target has no parent node." };
  /** XUDY0030: XQuery Update dynamic exception. */
  Object[] UPATTELM = { XUDY, 30,
      "Attributes must be inserted after/before an element." };
  /** XUDY0031: XQuery Update dynamic exception. */
  Object[] UPURIDUP = { XUDY, 31, "Multiple use of URI: \"%\"." };

  /** XUTY0004: XQuery Update type exception. */
  Object[] UPNOATTRPER = { XUTY, 4, "Attribute must follow root element." };
  /** XUTY0005: XQuery Update type exception. */
  Object[] UPTRGTYP = { XUTY, 5, "Single node expected as target." };
  /** XUTY0006: XQuery Update type exception. */
  Object[] UPTRGTYP2 = { XUTY, 6, "Single node expected as target." };
  /** XUTY0007: XQuery Update type exception. */
  Object[] UPTRGDELEMPT = { XUTY, 7, "Only nodes can be deleted." };
  /** XUTY0008: XQuery Update type exception. */
  Object[] UPTRGMULT = { XUTY, 8, "Single node expected as target." };
  /** XUTY0011: XQuery Update type exception. */
  Object[] UPWRELM = { XUTY, 10, "Replace nodes must not be attribute nodes." };
  /** XUTY0010: XQuery Update type exception. */
  Object[] UPWRATTR = { XUTY, 11, "Replace nodes must be attribute nodes." };
  /** XUTY0012: XQuery Update type exception. */
  Object[] UPWRTRGTYP = { XUTY, 12,
      "Target must be element, attribute or processing instruction." };
  /** XUTY0013: XQuery Update type exception. */
  Object[] UPCOPYMULT = { XUTY, 13,
      "Source expression in copy clause must return single node." };
  /** XUTY0022: XQuery Update type exception. */
  Object[] UPATTELM2 = { XUTY, 22, "Insertion target must be an element." };

  /** XUST0001: Parsing exception. */
  Object[] UPNOT = { XUST, 1, "No updating expression allowed." };
  /** XUST0002: Parsing exception. */
  Object[] UPEXPECT = { XUST, 2, "Updating expression expected." };
  /** XUST0003: Parsing exception. */
  Object[] DUPLREVAL = { XUST, 3, "Duplicate 'revalidation' declaration." };
  /** XUST0026: Parsing exception. */
  Object[] NOREVAL = { XUST, 26, "Revalidation mode not supported." };
  /** XUST0028: Parsing exception. */
  Object[] UPFUNCTYPE = { XUST, 28,
      "No return type allowed in updating functions." };

  // OPTIMIZATIONS

  /** Optimization info. */
  String OPTDESC = "Merging descendant-or-self step(s)";
  /** Optimization info. */
  String OPTPRE = "Pre-evaluating %";
  /** Optimization info. */
  String OPTWRITE = "Rewriting %";
  /** Optimization info. */
  String OPTFALSE = "Removing always false expression: %";
  /** Optimization info. */
  String OPTTRUE = "Removing always true expression: %";
  /** Optimization info. */
  String OPTTEXT = "Adding text() step";
  /** Optimization info. */
  String OPTSIMPLE = "Simplifying: % => %";
  /** Optimization info. */
  String OPTFLWOR = "Simplifying FLWOR expression";
  /** Optimization info. */
  String OPTWHERE = "Converting where clause to predicate";
  /** Optimization info. */
  String OPTVAR = "Removing variable %";
  /** Optimization info. */
  String OPTRED = "Merging redundant location paths";
  /** Optimization info. */
  String OPTNAME = "Removing unknown tag/attribute \"%\"";
  /** Optimization info. */
  String OPTTXTINDEX = "Applying text index";
  /** Optimization info. */
  String OPTATVINDEX = "Applying attribute index";
  /** Optimization info. */
  String OPTFTXINDEX = "Applying full-text index";
  /** Optimization info. */
  String OPTRNGINDEX = "Applying range index";
  /** Optimization info. */
  String OPTEMPTY = "Removing empty sequences.";
  /** Optimization info. */
  String OPTNOINDEX = "Removing path with no index results";
  /** Optimization info. */
  String OPTBIND = "Bind static variable %";
  /** Optimization info. */
  String OPTCHILD = "Converting % to child steps";
}
