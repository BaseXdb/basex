package org.basex.query.xquery.func;

import static org.basex.query.xquery.XQTokens.*;
import org.basex.query.xquery.item.Type;

/**
 * Definitions of all XQuery functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public enum FunDef {

  /* FNAcc functions. */
  
  /** XQuery function. */
  POS(FNURI, FNAcc.class, 0, 0, "position()", Type.ITR),
  /** XQuery function. */
  LAST(FNURI, FNAcc.class, 0, 0, "last()", Type.ITR),
  /** XQuery function. */
  STRING(FNURI, FNAcc.class, 0, 1, "string(item?)", Type.STR),
  /** XQuery function. */
  NUMBER(FNURI, FNAcc.class, 0, 1, "number(item?)", Type.DBL),
  /** XQuery function. */
  STRLEN(FNURI, FNAcc.class, 0, 1, "string-length(item?)", Type.ITR),
  /** XQuery function. */
  NORM(FNURI, FNAcc.class, 0, 1, "normalize-space(string?)", Type.STR),
  /** XQuery function. */
  URIQNAME(FNURI, FNAcc.class, 1, 1, "namespace-uri-from-QName(qname)",
      null),
  
  /* FNAggr functions. */

  /** XQuery function. */
  AVG(FNURI, FNAggr.class, 1, 1, "avg(item)", Type.DBL),
  /** XQuery function. */
  COUNT(FNURI, FNAggr.class, 1, 1, "count(item)", Type.ITR),
  /** XQuery function. */
  MAX(FNURI, FNAggr.class, 1, 2, "max(item)", null),
  /** XQuery function. */
  MIN(FNURI, FNAggr.class, 1, 2, "min(item)", null),
  /** XQuery function. */
  SUM(FNURI, FNAggr.class, 1, 2, "sum(item, zero)", Type.DBL),

  /* FNContext functions. */
  
  /** XQuery function. */
  CURRDATE(FNURI, FNContext.class, 0, 0, "current-date()", Type.DAT),
  /** XQuery function. */
  CURRDTM(FNURI, FNContext.class, 0, 0, "current-dateTime()", Type.DTM),
  /** XQuery function. */
  CURRTIME(FNURI, FNContext.class, 0, 0, "current-time()", Type.TIM),
  /** XQuery function. */
  IMPLZONE(FNURI, FNContext.class, 0, 0, "implicit-timezone()", Type.DTD),
  /** XQuery function. */
  COLLAT(FNURI, FNContext.class, 0, 0, "default-collation()", Type.STR),
  /** XQuery function. */
  STBASEURI(FNURI, FNContext.class, 0, 0, "static-base-uri()", null),

  /* FNDat functions. */

  /** XQuery function. */
  DAYDAT(FNURI, FNDate.class, 1, 1, "day-from-date(item)", null),
  /** XQuery function. */
  DAYDTM(FNURI, FNDate.class, 1, 1, "day-from-dateTime(datetime)", null),
  /** XQuery function. */
  DAYDUR(FNURI, FNDate.class, 1, 1, "days-from-duration(dur)", null),
  /** XQuery function. */
  HOUDTM(FNURI, FNDate.class, 1, 1, "hours-from-dateTime(datetime)", null),
  /** XQuery function. */
  HOUDUR(FNURI, FNDate.class, 1, 1, "hours-from-duration(dur)", null),
  /** XQuery function. */
  HOUTIM(FNURI, FNDate.class, 1, 1, "hours-from-time(item)", null),
  /** XQuery function. */
  MINDTM(FNURI, FNDate.class, 1, 1, "minutes-from-dateTime(datetime)", null),
  /** XQuery function. */
  MINDUR(FNURI, FNDate.class, 1, 1, "minutes-from-duration(dur)", null),
  /** XQuery function. */
  MINTIM(FNURI, FNDate.class, 1, 1, "minutes-from-time(item)", null),
  /** XQuery function. */
  MONDAT(FNURI, FNDate.class, 1, 1, "month-from-date(item)", null),
  /** XQuery function. */
  MONDTM(FNURI, FNDate.class, 1, 1, "month-from-dateTime(datetime)", null),
  /** XQuery function. */
  MONDUR(FNURI, FNDate.class, 1, 1, "months-from-duration(dur)", null),
  /** XQuery function. */
  SECDTM(FNURI, FNDate.class, 1, 1, "seconds-from-dateTime(datetime)", null),
  /** XQuery function. */
  SECDUR(FNURI, FNDate.class, 1, 1, "seconds-from-duration(dur)", null),
  /** XQuery function. */
  SECTIM(FNURI, FNDate.class, 1, 1, "seconds-from-time(item)", null),
  /** XQuery function. */
  ZONDAT(FNURI, FNDate.class, 1, 1, "timezone-from-date(item)", null),
  /** XQuery function. */
  ZONDTM(FNURI, FNDate.class, 1, 1, "timezone-from-dateTime(item)", null),
  /** XQuery function. */
  ZONTIM(FNURI, FNDate.class, 1, 1, "timezone-from-time(item)", null),
  /** XQuery function. */
  YEADAT(FNURI, FNDate.class, 1, 1, "year-from-date(item)", null),
  /** XQuery function. */
  YEADTM(FNURI, FNDate.class, 1, 1, "year-from-dateTime(datetime)", null),
  /** XQuery function. */
  YEADUR(FNURI, FNDate.class, 1, 1, "years-from-duration(dur)", null),
  /** XQuery function. */
  DATZON(FNURI, FNDate.class, 1, 2, "adjust-date-to-timezone(date, zone?)",
      null),
  /** XQuery function. */
  DTMZON(FNURI, FNDate.class, 1, 2, "adjust-dateTime-to-timezone(date, zone?)",
      null),
  /** XQuery function. */
  TIMZON(FNURI, FNDate.class, 1, 2, "adjust-time-to-timezone(date, zone?)",
      null),
  /** XQuery function. */
  DATETIME(FNURI, FNDate.class, 2, 2, "dateTime(date, time)", null),

  /* FNGen functions. */

  /** XQuery function. */
  DATA(FNURI, FNGen.class, 1, 1, "data(item)", null),
  /** XQuery function. */
  COLLECT(FNURI, FNGen.class, 0, 1, "collection(item)", null),
  /** XQuery function. */
  DOCAVAIL(FNURI, FNGen.class, 1, 1, "doc-available(item)", Type.BLN),
  /** XQuery function. */
  DOC(FNURI, FNGen.class, 1, 1, "doc(item)", null),

  /* FNId functions. */
  
  /** XQuery function. */
  ID(FNURI, FNId.class, 1, 2, "id(string, item?)", null),
  /** XQuery function. */
  IDREF(FNURI, FNId.class, 1, 2, "idref(string, item?)", null),
  /** XQuery function. */
  LANG(FNURI, FNId.class, 1, 2, "lang(string, item?)", Type.BLN),

  /* FNNode functions. */
  
  /** XQuery function. */
  DOCURI(FNURI, FNNode.class, 1, 1, "document-uri(node)", null),
  /** XQuery function. */
  NILLED(FNURI, FNNode.class, 1, 1, "nilled(node)", null),
  /** XQuery function. */
  NODENAME(FNURI, FNNode.class, 1, 1, "node-name(node)", null),
  /** XQuery function. */
  LOCNAME(FNURI, FNNode.class, 0, 1, "local-name(node?)", Type.STR),
  /** XQuery function. */
  NAME(FNURI, FNNode.class, 0, 1, "name(node?)", Type.STR),
  /** XQuery function. */
  NSURI(FNURI, FNNode.class, 0, 1, "namespace-uri(node?)", Type.URI),
  /** XQuery function. */
  ROOT(FNURI, FNNode.class, 0, 1, "root(node?)", null),
  /** XQuery function. */
  BASEURI(FNURI, FNNode.class, 0, 1, "base-uri(node?)", null),

  /* FNNum functions. */
  
  /** XQuery function. */
  ABS(FNURI, FNNum.class, 1, 1, "abs(num)", null),
  /** XQuery function. */
  CEIL(FNURI, FNNum.class, 1, 1, "ceiling(num)", null),
  /** XQuery function. */
  FLOOR(FNURI, FNNum.class, 1, 1, "floor(num)", null),
  /** XQuery function. */
  RND(FNURI, FNNum.class, 1, 1, "round(num)", null),
  /** XQuery function. */
  RNDHLF(FNURI, FNNum.class, 1, 2, "round-half-to-even(num, prec?)", null),

  /* FNOut functions. */
  
  /** XQuery function. */
  ERROR(FNURI, FNOut.class, 0, 3, "error(code?, desc?, object?)", null),
  /** XQuery function. */
  TRACE(FNURI, FNOut.class, 2, 2, "trace(item, message)", null),

  /* FNPat functions. */

  /** XQuery function. */
  MATCH(FNURI, FNPat.class, 2, 3, "matches(item, pattern, mod?)", Type.BLN),
  /** XQuery function. */
  REPLACE(FNURI, FNPat.class, 3, 4, "replace(item, pattern, replace, mod?)",
      Type.STR),
  /** XQuery function. */
  TOKEN(FNURI, FNPat.class, 2, 3, "tokenize(item, pattern, mod?)", Type.STR),

  /* FNQName functions. */
  
  /** XQuery function. */
  INSCOPE(FNURI, FNQName.class, 1, 1, "in-scope-prefixes(elem)", null),
  /** XQuery function. */
  LOCNAMEQNAME(FNURI, FNQName.class, 1, 1, "local-name-from-QName(qname)",
      null),
  /** XQuery function. */
  NSURIPRE(FNURI, FNQName.class, 2, 2, "namespace-uri-for-prefix(pre, elem)",
      null),
  /** XQuery function. */
  QNAME(FNURI, FNQName.class, 2, 2, "QName(uri, name)", Type.QNM),
  /** XQuery function. */
  RESQNAME(FNURI, FNQName.class, 2, 2, "resolve-QName(item, base)", null),
  /** XQuery function. */
  RESURI(FNURI, FNQName.class, 1, 2, "resolve-uri(name, elem?)", null),
  /** XQuery function. */
  PREQNAME(FNURI, FNQName.class, 1, 1, "prefix-from-QName(qname)", null),

  /* FNSeq functions. */
  
  /** XQuery function. */
  DISTINCT(FNURI, FNSeq.class, 1, 2, "distinct-values(item, coll?)", null),
  /** XQuery function. */
  INDEXOF(FNURI, FNSeq.class, 2, 3, "index-of(seq, item, coll?)", null),
  /** XQuery function. */
  INSBEF(FNURI, FNSeq.class, 3, 3, "insert-before(seq1, pos, seq2)", null),
  /** XQuery function. */
  REMOVE(FNURI, FNSeq.class, 2, 2, "remove(source, position)", null),
  /** XQuery function. */
  REVERSE(FNURI, FNSeq.class, 1, 1, "reverse(item)", null),
  /** XQuery function. */
  SUBSEQ(FNURI, FNSeq.class, 2, 3, "subsequence(item, start, len?)", null),
  /** XQuery function. */
  DEEPEQ(FNURI, FNSeq.class, 2, 3, "deep-equal(item, item, coll?)", Type.BLN),

  /* FNSimple functions. */
  
  /** XQuery function. */
  BOOL(FNURI, FNSimple.class, 1, 1, "boolean(item)", Type.BLN),
  /** XQuery function. */
  NOT(FNURI, FNSimple.class, 1, 1, "not(item)", Type.BLN),
  /** XQuery function. */
  FALSE(FNURI, FNSimple.class, 0, 0, "false()", Type.BLN),
  /** XQuery function. */
  TRUE(FNURI, FNSimple.class, 0, 0, "true()", Type.BLN),
  /** XQuery function. */
  EMPTY(FNURI, FNSimple.class, 1, 1, "empty(item)", Type.BLN),
  /** XQuery function. */
  EXISTS(FNURI, FNSimple.class, 1, 1, "exists(item)", Type.BLN),
  /** XQuery function. */
  UNORDER(FNURI, FNSimple.class, 1, 1, "unordered(item)", null),
  /** XQuery function. */
  ZEROONE(FNURI, FNSimple.class, 1, 1, "zero-or-one(item)", null),
  /** XQuery function. */
  EXONE(FNURI, FNSimple.class, 1, 1, "exactly-one(item)", null),
  /** XQuery function. */
  ONEMORE(FNURI, FNSimple.class, 1, 1, "one-or-more(item)", null),

  /* FNStr functions. */

  /** XQuery function. */
  CODEPNT(FNURI, FNStr.class, 2, 2, "codepoint-equal(string, string)", null),
  /** XQuery function. */
  CODESTR(FNURI, FNStr.class, 1, 1, "codepoints-to-string(string)", Type.STR),
  /** XQuery function. */
  COMPARE(FNURI, FNStr.class, 2, 3, "compare(first, second, coll)", null),
  /** XQuery function. */
  CONCAT(FNURI, FNStr.class, 2, 0, "concat(item, item+)", Type.STR),
  /** XQuery function. */
  CONTAINS(FNURI, FNStr.class, 2, 3, "contains(item, item)", Type.BLN),
  /** XQuery function. */
  ENCURI(FNURI, FNStr.class, 1, 1, "encode-for-uri(item)", Type.STR),
  /** XQuery function. */
  ENDS(FNURI, FNStr.class, 2, 3, "ends-with(item, item)", Type.BLN),
  /** XQuery function. */
  ESCURI(FNURI, FNStr.class, 1, 1, "escape-html-uri(item)", Type.STR),
  /** XQuery function. */
  IRIURI(FNURI, FNStr.class, 1, 1, "iri-to-uri(item)", Type.STR),
  /** XQuery function. */
  LOWER(FNURI, FNStr.class, 1, 1, "lower-case(item)", Type.STR),
  /** XQuery function. */
  NORMUNI(FNURI, FNStr.class, 1, 2, "normalize-unicode(string, form)",
      Type.STR),
  /** XQuery function. */
  STARTS(FNURI, FNStr.class, 2, 3, "starts-with(item, item)", Type.BLN),
  /** XQuery function. */
  STRJOIN(FNURI, FNStr.class, 2, 2, "string-join(item, sep)", Type.STR),
  /** XQuery function. */
  STCODE(FNURI, FNStr.class, 1, 1, "string-to-codepoints(string)", null),
  /** XQuery function. */
  SUBSTR(FNURI, FNStr.class, 2, 3, "substring(item, start, len?)", Type.STR),
  /** XQuery function. */
  SUBAFTER(FNURI, FNStr.class, 2, 3, "substring-after(item, sub, coll)",
      Type.STR),
  /** XQuery function. */
  SUBBEFORE(FNURI, FNStr.class, 2, 3, "substring-before(item, sub, coll)",
      Type.STR),
  /** XQuery function. */
  TRANS(FNURI, FNStr.class, 3, 3, "translate(arg, map, trans)", Type.STR),
  /** XQuery function. */
  UPPER(FNURI, FNStr.class, 1, 1, "upper-case(item)", Type.STR),

  /** Project specific function - evaluates the specified query. */
  EVAL(BXURI, FNBaseX.class, 1, 1, "eval(string)", null),
  /** Project specific function - returns a random number. */
  RANDOM(BXURI, FNBaseX.class, 1, 1, "random()", Type.DBL),
  /** XQuery function - allows a case insensitive substring search. */
  CONTAINSLC(BXURI, FNBaseX.class, 2, 3, "containslc(item, item)", Type.BLN),
  /** XQuery function - returns the name of the query file. */
  FILENAME(BXURI, FNBaseX.class, 0, 0, "filename()", Type.STR);
  
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
  Type ret;
  
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
      final int mx, final String dsc, final Type rt) {
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
