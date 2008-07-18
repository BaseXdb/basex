package org.basex.query.xquery.func;

import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.item.Uri;

/**
 * List of all functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public enum FunDef {

  /* FNAcc functions. */
  
  /** XQuery function. */
  POS(Uri.FN, FNAcc.class, 0, 0, "position()", Type.ITR),
  /** XQuery function. */
  LAST(Uri.FN, FNAcc.class, 0, 0, "last()", Type.ITR),
  /** XQuery function. */
  STRING(Uri.FN, FNAcc.class, 0, 1, "string(item?)", Type.STR),
  /** XQuery function. */
  NUMBER(Uri.FN, FNAcc.class, 0, 1, "number(item?)", Type.DBL),
  /** XQuery function. */
  STRLEN(Uri.FN, FNAcc.class, 0, 1, "string-length(item?)", Type.ITR),
  /** XQuery function. */
  NORM(Uri.FN, FNAcc.class, 0, 1, "normalize-space(string?)", Type.STR),
  /** XQuery function. */
  URIQNAME(Uri.FN, FNAcc.class, 1, 1, "namespace-uri-from-QName(qname)",
      Type.URI),
  
  /* FNAggr functions. */

  /** XQuery function. */
  AVG(Uri.FN, FNAggr.class, 1, 1, "avg(item)", Type.AAT),
  /** XQuery function. */
  COUNT(Uri.FN, FNAggr.class, 1, 1, "count(item)", Type.ITR),
  /** XQuery function. */
  MAX(Uri.FN, FNAggr.class, 1, 2, "max(item)", Type.AAT),
  /** XQuery function. */
  MIN(Uri.FN, FNAggr.class, 1, 2, "min(item)", Type.AAT),
  /** XQuery function. */
  SUM(Uri.FN, FNAggr.class, 1, 2, "sum(item, zero)", Type.AAT),

  /* FNContext functions. */
  
  /** XQuery function. */
  CURRDATE(Uri.FN, FNContext.class, 0, 0, "current-date()", Type.DAT),
  /** XQuery function. */
  CURRDTM(Uri.FN, FNContext.class, 0, 0, "current-dateTime()", Type.DTM),
  /** XQuery function. */
  CURRTIME(Uri.FN, FNContext.class, 0, 0, "current-time()", Type.TIM),
  /** XQuery function. */
  IMPLZONE(Uri.FN, FNContext.class, 0, 0, "implicit-timezone()", Type.DTD),
  /** XQuery function. */
  COLLAT(Uri.FN, FNContext.class, 0, 0, "default-collation()", Type.STR),
  /** XQuery function. */
  STBASEURI(Uri.FN, FNContext.class, 0, 0, "static-base-uri()", Type.URI),

  /* FNDat functions. */

  /** XQuery function. */
  DAYDAT(Uri.FN, FNDate.class, 1, 1, "day-from-date(item)", Type.ITR),
  /** XQuery function. */
  DAYDTM(Uri.FN, FNDate.class, 1, 1, "day-from-dateTime(datetime)", Type.ITR),
  /** XQuery function. */
  DAYDUR(Uri.FN, FNDate.class, 1, 1, "days-from-duration(dur)", Type.ITR),
  /** XQuery function. */
  HOUDTM(Uri.FN, FNDate.class, 1, 1, "hours-from-dateTime(datetime)", Type.ITR),
  /** XQuery function. */
  HOUDUR(Uri.FN, FNDate.class, 1, 1, "hours-from-duration(dur)", Type.ITR),
  /** XQuery function. */
  HOUTIM(Uri.FN, FNDate.class, 1, 1, "hours-from-time(item)", Type.ITR),
  /** XQuery function. */
  MINDTM(Uri.FN, FNDate.class, 1, 1, "minutes-from-dateTime(datetime)",
      Type.ITR),
  /** XQuery function. */
  MINDUR(Uri.FN, FNDate.class, 1, 1, "minutes-from-duration(dur)", Type.ITR),
  /** XQuery function. */
  MINTIM(Uri.FN, FNDate.class, 1, 1, "minutes-from-time(item)", Type.ITR),
  /** XQuery function. */
  MONDAT(Uri.FN, FNDate.class, 1, 1, "month-from-date(item)", Type.ITR),
  /** XQuery function. */
  MONDTM(Uri.FN, FNDate.class, 1, 1, "month-from-dateTime(datetime)", Type.ITR),
  /** XQuery function. */
  MONDUR(Uri.FN, FNDate.class, 1, 1, "months-from-duration(dur)", Type.ITR),
  /** XQuery function. */
  SECDTM(Uri.FN, FNDate.class, 1, 1, "seconds-from-dateTime(datetime)",
      Type.DEC),
  /** XQuery function. */
  SECDUR(Uri.FN, FNDate.class, 1, 1, "seconds-from-duration(dur)", Type.DEC),
  /** XQuery function. */
  SECTIM(Uri.FN, FNDate.class, 1, 1, "seconds-from-time(item)", Type.DEC),
  /** XQuery function. */
  ZONDAT(Uri.FN, FNDate.class, 1, 1, "timezone-from-date(item)", Type.DTD),
  /** XQuery function. */
  ZONDTM(Uri.FN, FNDate.class, 1, 1, "timezone-from-dateTime(item)", Type.DTD),
  /** XQuery function. */
  ZONTIM(Uri.FN, FNDate.class, 1, 1, "timezone-from-time(item)", Type.DTD),
  /** XQuery function. */
  YEADAT(Uri.FN, FNDate.class, 1, 1, "year-from-date(item)", Type.ITR),
  /** XQuery function. */
  YEADTM(Uri.FN, FNDate.class, 1, 1, "year-from-dateTime(datetime)", Type.ITR),
  /** XQuery function. */
  YEADUR(Uri.FN, FNDate.class, 1, 1, "years-from-duration(dur)", Type.ITR),
  /** XQuery function. */
  DATZON(Uri.FN, FNDate.class, 1, 2, "adjust-date-to-timezone(date, zone?)",
      Type.DAT),
  /** XQuery function. */
  DTMZON(Uri.FN, FNDate.class, 1, 2, "adjust-dateTime-to-timezone(date, zone?)",
      Type.DTM),
  /** XQuery function. */
  TIMZON(Uri.FN, FNDate.class, 1, 2, "adjust-time-to-timezone(date, zone?)",
      Type.TIM),
  /** XQuery function. */
  DATETIME(Uri.FN, FNDate.class, 2, 2, "dateTime(date, time)", Type.DTM),

  /* FNGen functions. */

  /** XQuery function. */
  DATA(Uri.FN, FNGen.class, 1, 1, "data(item)", Type.AAT),
  /** XQuery function. */
  COLLECT(Uri.FN, FNGen.class, 0, 1, "collection(item)", Type.NOD),
  /** XQuery function. */
  DOCAVAIL(Uri.FN, FNGen.class, 1, 1, "doc-available(item)", Type.BLN),
  /** XQuery function. */
  DOC(Uri.FN, FNGen.class, 1, 1, "doc(item)", Type.DOC),

  /* FNId functions. */
  
  /** XQuery function. */
  ID(Uri.FN, FNId.class, 1, 2, "id(string, item?)", Type.ELM),
  /** XQuery function. */
  IDREF(Uri.FN, FNId.class, 1, 2, "idref(string, item?)", Type.NOD),
  /** XQuery function. */
  LANG(Uri.FN, FNId.class, 1, 2, "lang(string, item?)", Type.BLN),

  /* FNNode functions. */
  
  /** XQuery function. */
  DOCURI(Uri.FN, FNNode.class, 1, 1, "document-uri(node)", Type.URI),
  /** XQuery function. */
  NILLED(Uri.FN, FNNode.class, 1, 1, "nilled(node)", Type.BLN),
  /** XQuery function. */
  NODENAME(Uri.FN, FNNode.class, 1, 1, "node-name(node)", Type.QNM),
  /** XQuery function. */
  LOCNAME(Uri.FN, FNNode.class, 0, 1, "local-name(node?)", Type.STR),
  /** XQuery function. */
  NAME(Uri.FN, FNNode.class, 0, 1, "name(node?)", Type.STR),
  /** XQuery function. */
  NSURI(Uri.FN, FNNode.class, 0, 1, "namespace-uri(node?)", Type.URI),
  /** XQuery function. */
  ROOT(Uri.FN, FNNode.class, 0, 1, "root(node?)", Type.NOD),
  /** XQuery function. */
  BASEURI(Uri.FN, FNNode.class, 0, 1, "base-uri(node?)", Type.URI),

  /* FNNum functions. */
  
  /** XQuery function. */
  ABS(Uri.FN, FNNum.class, 1, 1, "abs(num)", Type.DEC),
  /** XQuery function. */
  CEIL(Uri.FN, FNNum.class, 1, 1, "ceiling(num)", Type.DEC),
  /** XQuery function. */
  FLOOR(Uri.FN, FNNum.class, 1, 1, "floor(num)", Type.DEC),
  /** XQuery function. */
  RND(Uri.FN, FNNum.class, 1, 1, "round(num)", Type.DEC),
  /** XQuery function. */
  RNDHLF(Uri.FN, FNNum.class, 1, 2, "round-half-to-even(num, prec?)", Type.DEC),

  /* FNOut functions. */
  
  /** XQuery function. */
  ERROR(Uri.FN, FNOut.class, 0, 3, "error(code?, desc?, object?)", null),
  /** XQuery function. */
  TRACE(Uri.FN, FNOut.class, 2, 2, "trace(item, message)", Type.ITEM),

  /* FNPat functions. */

  /** XQuery function. */
  MATCH(Uri.FN, FNPat.class, 2, 3, "matches(item, pattern, mod?)", Type.BLN),
  /** XQuery function. */
  REPLACE(Uri.FN, FNPat.class, 3, 4, "replace(item, pattern, replace, mod?)",
      Type.STR),
  /** XQuery function. */
  TOKEN(Uri.FN, FNPat.class, 2, 3, "tokenize(item, pattern, mod?)", Type.STR),

  /* FNQName functions. */
  
  /** XQuery function. */
  INSCOPE(Uri.FN, FNQName.class, 1, 1, "in-scope-prefixes(elem)", Type.STR),
  /** XQuery function. */
  LOCNAMEQNAME(Uri.FN, FNQName.class, 1, 1, "local-name-from-QName(qname)",
      Type.NCN),
  /** XQuery function. */
  NSURIPRE(Uri.FN, FNQName.class, 2, 2, "namespace-uri-for-prefix(pre, elem)",
      Type.URI),
  /** XQuery function. */
  QNAME(Uri.FN, FNQName.class, 2, 2, "QName(uri, name)", Type.QNM),
  /** XQuery function. */
  RESQNAME(Uri.FN, FNQName.class, 2, 2, "resolve-QName(item, base)", Type.QNM),
  /** XQuery function. */
  RESURI(Uri.FN, FNQName.class, 1, 2, "resolve-uri(name, elem?)", Type.URI),
  /** XQuery function. */
  PREQNAME(Uri.FN, FNQName.class, 1, 1, "prefix-from-QName(qname)", Type.NCN),

  /* FNSeq functions. */
  
  /** XQuery function. */
  DISTINCT(Uri.FN, FNSeq.class, 1, 2, "distinct-values(item, coll?)", Type.AAT),
  /** XQuery function. */
  INDEXOF(Uri.FN, FNSeq.class, 2, 3, "index-of(seq, item, coll?)", Type.ITR),
  /** XQuery function. */
  INSBEF(Uri.FN, FNSeq.class, 3, 3, "insert-before(seq1, pos, seq2)",
      Type.ITEM),
  /** XQuery function. */
  REMOVE(Uri.FN, FNSeq.class, 2, 2, "remove(source, position)", Type.ITEM),
  /** XQuery function. */
  REVERSE(Uri.FN, FNSeq.class, 1, 1, "reverse(item)", Type.ITEM),
  /** XQuery function. */
  SUBSEQ(Uri.FN, FNSeq.class, 2, 3, "subsequence(item, start, len?)",
      Type.ITEM),
  /** XQuery function. */
  DEEPEQ(Uri.FN, FNSeq.class, 2, 3, "deep-equal(item, item, coll?)", Type.BLN),

  /* FNSimple functions. */
  
  /** XQuery function. */
  BOOL(Uri.FN, FNSimple.class, 1, 1, "boolean(item)", Type.BLN),
  /** XQuery function. */
  NOT(Uri.FN, FNSimple.class, 1, 1, "not(item)", Type.BLN),
  /** XQuery function. */
  FALSE(Uri.FN, FNSimple.class, 0, 0, "false()", Type.BLN),
  /** XQuery function. */
  TRUE(Uri.FN, FNSimple.class, 0, 0, "true()", Type.BLN),
  /** XQuery function. */
  EMPTY(Uri.FN, FNSimple.class, 1, 1, "empty(item)", Type.BLN),
  /** XQuery function. */
  EXISTS(Uri.FN, FNSimple.class, 1, 1, "exists(item)", Type.BLN),
  /** XQuery function. */
  UNORDER(Uri.FN, FNSimple.class, 1, 1, "unordered(item)", Type.ITEM),
  /** XQuery function. */
  ZEROONE(Uri.FN, FNSimple.class, 1, 1, "zero-or-one(item)", Type.ITEM),
  /** XQuery function. */
  EXONE(Uri.FN, FNSimple.class, 1, 1, "exactly-one(item)", Type.ITEM),
  /** XQuery function. */
  ONEMORE(Uri.FN, FNSimple.class, 1, 1, "one-or-more(item)", Type.ITEM),

  /* FNStr functions. */

  /** XQuery function. */
  CODEPNT(Uri.FN, FNStr.class, 2, 2, "codepoint-equal(string, string)",
      Type.BLN),
  /** XQuery function. */
  CODESTR(Uri.FN, FNStr.class, 1, 1, "codepoints-to-string(string)", Type.STR),
  /** XQuery function. */
  COMPARE(Uri.FN, FNStr.class, 2, 3, "compare(first, second, coll)", Type.ITR),
  /** XQuery function. */
  CONCAT(Uri.FN, FNStr.class, 2, 0, "concat(item, item+)", Type.STR),
  /** XQuery function. */
  CONTAINS(Uri.FN, FNStr.class, 2, 3, "contains(item, item)", Type.BLN),
  /** XQuery function. */
  ENCURI(Uri.FN, FNStr.class, 1, 1, "encode-for-uri(item)", Type.STR),
  /** XQuery function. */
  ENDS(Uri.FN, FNStr.class, 2, 3, "ends-with(item, item)", Type.BLN),
  /** XQuery function. */
  ESCURI(Uri.FN, FNStr.class, 1, 1, "escape-html-uri(item)", Type.STR),
  /** XQuery function. */
  IRIURI(Uri.FN, FNStr.class, 1, 1, "iri-to-uri(item)", Type.STR),
  /** XQuery function. */
  LOWER(Uri.FN, FNStr.class, 1, 1, "lower-case(item)", Type.STR),
  /** XQuery function. */
  NORMUNI(Uri.FN, FNStr.class, 1, 2, "normalize-unicode(string, form)",
      Type.STR),
  /** XQuery function. */
  STARTS(Uri.FN, FNStr.class, 2, 3, "starts-with(item, item)", Type.BLN),
  /** XQuery function. */
  STRJOIN(Uri.FN, FNStr.class, 2, 2, "string-join(item, sep)", Type.STR),
  /** XQuery function. */
  STCODE(Uri.FN, FNStr.class, 1, 1, "string-to-codepoints(string)", Type.ITR),
  /** XQuery function. */
  SUBSTR(Uri.FN, FNStr.class, 2, 3, "substring(item, start, len?)", Type.STR),
  /** XQuery function. */
  SUBAFTER(Uri.FN, FNStr.class, 2, 3, "substring-after(item, sub, coll)",
      Type.STR),
  /** XQuery function. */
  SUBBEFORE(Uri.FN, FNStr.class, 2, 3, "substring-before(item, sub, coll)",
      Type.STR),
  /** XQuery function. */
  TRANS(Uri.FN, FNStr.class, 3, 3, "translate(arg, map, trans)", Type.STR),
  /** XQuery function. */
  UPPER(Uri.FN, FNStr.class, 1, 1, "upper-case(item)", Type.STR),

  /** Project specific function - evaluates the specified query. */
  EVAL(Uri.BX, FNBaseX.class, 1, 1, "eval(string)", Type.ITEM),
  /** Project specific function - returns a random sequence entry. */
  RANDOM(Uri.BX, FNBaseX.class, 1, 1, "random(seq)", Type.ITEM),
  /** XQuery function - allows a case insensitive substring search. */
  CONTAINSLC(Uri.BX, FNBaseX.class, 2, 3, "containslc(item, item)", Type.BLN),
  /** XQuery function - returns the name of the query file. */
  FILENAME(Uri.BX, FNBaseX.class, 0, 0, "filename()", Type.BLN);
  
  /** Function classes. */
  Class<? extends Fun> func;
  /** Function uri. */
  Uri uri;
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
  FunDef(final Uri ur, final Class<? extends Fun> fun, final int mn,
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
