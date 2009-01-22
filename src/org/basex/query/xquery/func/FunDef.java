package org.basex.query.xquery.func;

import static org.basex.query.xquery.XQTokens.*;
import static org.basex.query.xquery.expr.Return.*;
import org.basex.query.xquery.expr.Return;

/**
 * Definitions of all XQuery functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public enum FunDef {

  /* FNAcc functions. */
  
  /** XQuery function. */
  POS(FNURI, FNAcc.class, 0, 0, "position()", NUM),
  /** XQuery function. */
  LAST(FNURI, FNAcc.class, 0, 0, "last()", NUM),
  /** XQuery function. */
  STRING(FNURI, FNAcc.class, 0, 1, "string(item?)", STR),
  /** XQuery function. */
  NUMBER(FNURI, FNAcc.class, 0, 1, "number(item?)", NUM),
  /** XQuery function. */
  STRLEN(FNURI, FNAcc.class, 0, 1, "string-length(item?)", NUM),
  /** XQuery function. */
  NORM(FNURI, FNAcc.class, 0, 1, "normalize-space(string?)", STR),
  /** XQuery function. */
  URIQNAME(FNURI, FNAcc.class, 1, 1, "namespace-uri-from-QName(qname)",
      NONUMSEQ),
  
  /* FNAggr functions. */

  /** XQuery function. */
  AVG(FNURI, FNAggr.class, 1, 1, "avg(item)", SEQ),
  /** XQuery function. */
  COUNT(FNURI, FNAggr.class, 1, 1, "count(item)", NUM),
  /** XQuery function. */
  MAX(FNURI, FNAggr.class, 1, 2, "max(item)", SEQ),
  /** XQuery function. */
  MIN(FNURI, FNAggr.class, 1, 2, "min(item)", SEQ),
  /** XQuery function. */
  SUM(FNURI, FNAggr.class, 1, 2, "sum(item, zero)", SEQ),

  /* FNContext functions. */
  
  /** XQuery function. */
  CURRDATE(FNURI, FNContext.class, 0, 0, "current-date()", NONUM),
  /** XQuery function. */
  CURRDTM(FNURI, FNContext.class, 0, 0, "current-dateTime()", NONUM),
  /** XQuery function. */
  CURRTIME(FNURI, FNContext.class, 0, 0, "current-time()", NONUM),
  /** XQuery function. */
  IMPLZONE(FNURI, FNContext.class, 0, 0, "implicit-timezone()", NONUM),
  /** XQuery function. */
  COLLAT(FNURI, FNContext.class, 0, 0, "default-collation()", STR),
  /** XQuery function. */
  STBASEURI(FNURI, FNContext.class, 0, 0, "static-base-uri()", NONUMSEQ),

  /* FNDate functions. */

  /** XQuery function. */
  DAYDAT(FNURI, FNDate.class, 1, 1, "day-from-date(item)", NUMSEQ),
  /** XQuery function. */
  DAYDTM(FNURI, FNDate.class, 1, 1, "day-from-dateTime(datetime)", NUMSEQ),
  /** XQuery function. */
  DAYDUR(FNURI, FNDate.class, 1, 1, "days-from-duration(dur)", NUMSEQ),
  /** XQuery function. */
  HOUDTM(FNURI, FNDate.class, 1, 1, "hours-from-dateTime(datetime)", NUMSEQ),
  /** XQuery function. */
  HOUDUR(FNURI, FNDate.class, 1, 1, "hours-from-duration(dur)", NUMSEQ),
  /** XQuery function. */
  HOUTIM(FNURI, FNDate.class, 1, 1, "hours-from-time(item)", NUMSEQ),
  /** XQuery function. */
  MINDTM(FNURI, FNDate.class, 1, 1, "minutes-from-dateTime(datetime)", NUMSEQ),
  /** XQuery function. */
  MINDUR(FNURI, FNDate.class, 1, 1, "minutes-from-duration(dur)", NUMSEQ),
  /** XQuery function. */
  MINTIM(FNURI, FNDate.class, 1, 1, "minutes-from-time(item)", NUMSEQ),
  /** XQuery function. */
  MONDAT(FNURI, FNDate.class, 1, 1, "month-from-date(item)", NUMSEQ),
  /** XQuery function. */
  MONDTM(FNURI, FNDate.class, 1, 1, "month-from-dateTime(datetime)", NUMSEQ),
  /** XQuery function. */
  MONDUR(FNURI, FNDate.class, 1, 1, "months-from-duration(dur)", NUMSEQ),
  /** XQuery function. */
  SECDTM(FNURI, FNDate.class, 1, 1, "seconds-from-dateTime(datetime)", NUMSEQ),
  /** XQuery function. */
  SECDUR(FNURI, FNDate.class, 1, 1, "seconds-from-duration(dur)", NUMSEQ),
  /** XQuery function. */
  SECTIM(FNURI, FNDate.class, 1, 1, "seconds-from-time(item)", NUMSEQ),
  /** XQuery function. */
  ZONDAT(FNURI, FNDate.class, 1, 1, "timezone-from-date(item)", NONUMSEQ),
  /** XQuery function. */
  ZONDTM(FNURI, FNDate.class, 1, 1, "timezone-from-dateTime(item)", NONUMSEQ),
  /** XQuery function. */
  ZONTIM(FNURI, FNDate.class, 1, 1, "timezone-from-time(item)", NONUMSEQ),
  /** XQuery function. */
  YEADAT(FNURI, FNDate.class, 1, 1, "year-from-date(item)", NUMSEQ),
  /** XQuery function. */
  YEADTM(FNURI, FNDate.class, 1, 1, "year-from-dateTime(datetime)", NUMSEQ),
  /** XQuery function. */
  YEADUR(FNURI, FNDate.class, 1, 1, "years-from-duration(dur)", NUMSEQ),
  /** XQuery function. */
  DATZON(FNURI, FNDate.class, 1, 2, "adjust-date-to-timezone(date, zone?)",
      NONUMSEQ),
  /** XQuery function. */
  DTMZON(FNURI, FNDate.class, 1, 2, "adjust-dateTime-to-timezone(date, zone?)",
      NONUMSEQ),
  /** XQuery function. */
  TIMZON(FNURI, FNDate.class, 1, 2, "adjust-time-to-timezone(date, zone?)",
      NONUMSEQ),
  /** XQuery function. */
  DATETIME(FNURI, FNDate.class, 2, 2, "dateTime(date, time)", NONUMSEQ),

  /* FNGen functions. */

  /** XQuery function. */
  DATA(FNURI, FNGen.class, 1, 1, "data(item)", SEQ),
  /** XQuery function. */
  COLLECT(FNURI, FNGen.class, 0, 1, "collection(item)", NODSEQ),
  /** XQuery function. */
  DOCAVAIL(FNURI, FNGen.class, 1, 1, "doc-available(item)", BLN),
  /** XQuery function. */
  DOC(FNURI, FNGen.class, 1, 1, "doc(item)", NODSEQ),

  /* FNId functions. */
  
  /** XQuery function. */
  ID(FNURI, FNId.class, 1, 2, "id(string, item?)", NODSEQ),
  /** XQuery function. */
  IDREF(FNURI, FNId.class, 1, 2, "idref(string, item?)", NODSEQ),
  /** XQuery function. */
  LANG(FNURI, FNId.class, 1, 2, "lang(string, item?)", BLN),

  /* FNNode functions. */
  
  /** XQuery function. */
  DOCURI(FNURI, FNNode.class, 1, 1, "document-uri(node)", NONUMSEQ),
  /** XQuery function. */
  NILLED(FNURI, FNNode.class, 1, 1, "nilled(node)", NONUMSEQ),
  /** XQuery function. */
  NODENAME(FNURI, FNNode.class, 1, 1, "node-name(node)", NONUMSEQ),
  /** XQuery function. */
  LOCNAME(FNURI, FNNode.class, 0, 1, "local-name(node?)", STR),
  /** XQuery function. */
  NAME(FNURI, FNNode.class, 0, 1, "name(node?)", STR),
  /** XQuery function. */
  NSURI(FNURI, FNNode.class, 0, 1, "namespace-uri(node?)", NONUM),
  /** XQuery function. */
  ROOT(FNURI, FNNode.class, 0, 1, "root(node?)", NODSEQ),
  /** XQuery function. */
  BASEURI(FNURI, FNNode.class, 0, 1, "base-uri(node?)", NONUMSEQ),

  /* FNNum functions. */
  
  /** XQuery function. */
  ABS(FNURI, FNNum.class, 1, 1, "abs(num)", NUMSEQ),
  /** XQuery function. */
  CEIL(FNURI, FNNum.class, 1, 1, "ceiling(num)", NUMSEQ),
  /** XQuery function. */
  FLOOR(FNURI, FNNum.class, 1, 1, "floor(num)", NUMSEQ),
  /** XQuery function. */
  RND(FNURI, FNNum.class, 1, 1, "round(num)", NUMSEQ),
  /** XQuery function. */
  RNDHLF(FNURI, FNNum.class, 1, 2, "round-half-to-even(num, prec?)", NUMSEQ),

  /* FNOut functions. */
  
  /** XQuery function. */
  ERROR(FNURI, FNOut.class, 0, 3, "error(code?, desc?, object?)", NONUMSEQ),
  /** XQuery function. */
  TRACE(FNURI, FNOut.class, 2, 2, "trace(item, message)", NONUMSEQ),

  /* FNPat functions. */

  /** XQuery function. */
  MATCH(FNURI, FNPat.class, 2, 3, "matches(item, pattern, mod?)", BLN),
  /** XQuery function. */
  REPLACE(FNURI, FNPat.class, 3, 4, "replace(item, pattern, replace, mod?)",
      STR),
  /** XQuery function. */
  TOKEN(FNURI, FNPat.class, 2, 3, "tokenize(item, pattern, mod?)", NONUMSEQ),

  /* FNQName functions. */
  
  /** XQuery function. */
  INSCOPE(FNURI, FNQName.class, 1, 1, "in-scope-prefixes(elem)", NONUMSEQ),
  /** XQuery function. */
  LOCNAMEQNAME(FNURI, FNQName.class, 1, 1, "local-name-from-QName(qname)",
      NONUMSEQ),
  /** XQuery function. */
  NSURIPRE(FNURI, FNQName.class, 2, 2, "namespace-uri-for-prefix(pre, elem)",
      NONUMSEQ),
  /** XQuery function. */
  QNAME(FNURI, FNQName.class, 2, 2, "QName(uri, name)", NONUM),
  /** XQuery function. */
  RESQNAME(FNURI, FNQName.class, 2, 2, "resolve-QName(item, base)", NONUMSEQ),
  /** XQuery function. */
  RESURI(FNURI, FNQName.class, 1, 2, "resolve-uri(name, elem?)", NONUMSEQ),
  /** XQuery function. */
  PREQNAME(FNURI, FNQName.class, 1, 1, "prefix-from-QName(qname)", NONUMSEQ),

  /* FNSeq functions. */
  
  /** XQuery function. */
  DISTINCT(FNURI, FNSeq.class, 1, 2, "distinct-values(item, coll?)", SEQ),
  /** XQuery function. */
  INDEXOF(FNURI, FNSeq.class, 2, 3, "index-of(seq, item, coll?)", NUMSEQ),
  /** XQuery function. */
  INSBEF(FNURI, FNSeq.class, 3, 3, "insert-before(seq1, pos, seq2)", SEQ),
  /** XQuery function. */
  REMOVE(FNURI, FNSeq.class, 2, 2, "remove(source, position)", SEQ),
  /** XQuery function. */
  REVERSE(FNURI, FNSeq.class, 1, 1, "reverse(item)", SEQ),
  /** XQuery function. */
  SUBSEQ(FNURI, FNSeq.class, 2, 3, "subsequence(item, start, len?)", SEQ),
  /** XQuery function. */
  DEEPEQ(FNURI, FNSeq.class, 2, 3, "deep-equal(item, item, coll?)", BLN),

  /* FNSimple functions. */
  
  /** XQuery function. */
  BOOL(FNURI, FNSimple.class, 1, 1, "boolean(item)", BLN),
  /** XQuery function. */
  NOT(FNURI, FNSimple.class, 1, 1, "not(item)", BLN),
  /** XQuery function. */
  FALSE(FNURI, FNSimple.class, 0, 0, "false()", BLN),
  /** XQuery function. */
  TRUE(FNURI, FNSimple.class, 0, 0, "true()", BLN),
  /** XQuery function. */
  EMPTY(FNURI, FNSimple.class, 1, 1, "empty(item)", BLN),
  /** XQuery function. */
  EXISTS(FNURI, FNSimple.class, 1, 1, "exists(item)", BLN),
  /** XQuery function. */
  UNORDER(FNURI, FNSimple.class, 1, 1, "unordered(item)", SEQ),
  /** XQuery function. */
  ZEROONE(FNURI, FNSimple.class, 1, 1, "zero-or-one(item)", SEQ),
  /** XQuery function. */
  EXONE(FNURI, FNSimple.class, 1, 1, "exactly-one(item)", NUM),
  /** XQuery function. */
  ONEMORE(FNURI, FNSimple.class, 1, 1, "one-or-more(item)", SEQ),

  /* FNStr functions. */

  /** XQuery function. */
  CODEPNT(FNURI, FNStr.class, 2, 2, "codepoint-equal(string, string)",
      NONUMSEQ),
  /** XQuery function. */
  CODESTR(FNURI, FNStr.class, 1, 1, "codepoints-to-string(string)", STR),
  /** XQuery function. */
  COMPARE(FNURI, FNStr.class, 2, 3, "compare(first, second, coll)", NUMSEQ),
  /** XQuery function. */
  CONCAT(FNURI, FNStr.class, 2, 0, "concat(item, item+)", STR),
  /** XQuery function. */
  CONTAINS(FNURI, FNStr.class, 2, 3, "contains(item, item)", BLN),
  /** XQuery function. */
  ENCURI(FNURI, FNStr.class, 1, 1, "encode-for-uri(item)", STR),
  /** XQuery function. */
  ENDS(FNURI, FNStr.class, 2, 3, "ends-with(item, item)", BLN),
  /** XQuery function. */
  ESCURI(FNURI, FNStr.class, 1, 1, "escape-html-uri(item)", STR),
  /** XQuery function. */
  IRIURI(FNURI, FNStr.class, 1, 1, "iri-to-uri(item)", STR),
  /** XQuery function. */
  LOWER(FNURI, FNStr.class, 1, 1, "lower-case(item)", STR),
  /** XQuery function. */
  NORMUNI(FNURI, FNStr.class, 1, 2, "normalize-unicode(string, form)", STR),
  /** XQuery function. */
  STARTS(FNURI, FNStr.class, 2, 3, "starts-with(item, item)", BLN),
  /** XQuery function. */
  STRJOIN(FNURI, FNStr.class, 2, 2, "string-join(item, sep)", STR),
  /** XQuery function. */
  STCODE(FNURI, FNStr.class, 1, 1, "string-to-codepoints(string)", NUMSEQ),
  /** XQuery function. */
  SUBSTR(FNURI, FNStr.class, 2, 3, "substring(item, start, len?)", STR),
  /** XQuery function. */
  SUBAFTER(FNURI, FNStr.class, 2, 3, "substring-after(item, sub, coll)", STR),
  /** XQuery function. */
  SUBBEFORE(FNURI, FNStr.class, 2, 3, "substring-before(item, sub, coll)", STR),
  /** XQuery function. */
  TRANS(FNURI, FNStr.class, 3, 3, "translate(arg, map, trans)", STR),
  /** XQuery function. */
  UPPER(FNURI, FNStr.class, 1, 1, "upper-case(item)", STR),

  /** Project specific function - evaluates the specified query. */
  EVAL(BXURI, FNBaseX.class, 1, 1, "eval(string)", SEQ),
  /** Project specific function - returns a random number. */
  RANDOM(BXURI, FNBaseX.class, 1, 1, "random()", NUM),
  /** XQuery function - allows a case insensitive substring search. */
  CONTAINSLC(BXURI, FNBaseX.class, 2, 3, "containslc(item, item)", BLN),
  /** XQuery function - returns the name of the query file. */
  FILENAME(BXURI, FNBaseX.class, 0, 0, "filename()", STR);
  
  /** Function classes. */
  Class<? extends Fun> func;
  /** Function uri. */
  byte[] uri;
  /** Descriptions. */
  String desc;
  /** Minimum number of arguments. */
  int min;
  /** Maximum number of arguments. */
  int max;
  /** Return type. */
  Return ret;
  
  /**
   * Constructor.
   * @param ur uri
   * @param fun function class
   * @param mn minimum number of arguments
   * @param mx maximum number of arguments
   * @param dsc description
   * @param rt return value
   */
  FunDef(final byte[] ur, final Class<? extends Fun> fun, final int mn,
      final int mx, final String dsc, final Return rt) {
    uri = ur;
    func = fun;
    min = mn;
    max = mx;
    desc = dsc;
    ret = rt;
  }
  
  @Override
  public final String toString() {
    return desc;
  }
}
