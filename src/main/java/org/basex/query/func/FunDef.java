package org.basex.query.func;

import static org.basex.query.item.SeqType.*;
import static org.basex.query.QueryTokens.*;
import org.basex.query.item.SeqType;

/**
 * Definitions of all XQuery functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public enum FunDef {

  /* FNAcc functions. */

  /** XQuery function. */
  POS(FNURI, FNAcc.class, 0, 0, "position()", ITR),
  /** XQuery function. */
  LAST(FNURI, FNAcc.class, 0, 0, "last()", ITR),
  /** XQuery function. */
  STRING(FNURI, FNAcc.class, 0, 1, "string(item?)", STR),
  /** XQuery function. */
  NUMBER(FNURI, FNAcc.class, 0, 1, "number(item?)", ITR),
  /** XQuery function. */
  STRLEN(FNURI, FNAcc.class, 0, 1, "string-length(item?)", ITR),
  /** XQuery function. */
  NORM(FNURI, FNAcc.class, 0, 1, "normalize-space(string?)", STR),
  /** XQuery function. */
  URIQNAME(FNURI, FNAcc.class, 1, 1, "namespace-uri-from-QName(qname)", URI_ZO),

  /* FNAggr functions. */

  /** XQuery function. */
  AVG(FNURI, FNAggr.class, 1, 1, "avg(item)", ITEM_ZO),
  /** XQuery function. */
  COUNT(FNURI, FNAggr.class, 1, 1, "count(item)", ITR),
  /** XQuery function. */
  MAX(FNURI, FNAggr.class, 1, 2, "max(item)", ITEM_ZO),
  /** XQuery function. */
  MIN(FNURI, FNAggr.class, 1, 2, "min(item)", ITEM_ZO),
  /** XQuery function. */
  SUM(FNURI, FNAggr.class, 1, 2, "sum(item, zero)", ITEM_ZO),

  /* FNContext functions. */

  /** XQuery function. */
  CURRDATE(FNURI, FNContext.class, 0, 0, "current-date()", DAT),
  /** XQuery function. */
  CURRDTM(FNURI, FNContext.class, 0, 0, "current-dateTime()", DAT),
  /** XQuery function. */
  CURRTIME(FNURI, FNContext.class, 0, 0, "current-time()", DAT),
  /** XQuery function. */
  IMPLZONE(FNURI, FNContext.class, 0, 0, "implicit-timezone()", DAT),
  /** XQuery function. */
  COLLAT(FNURI, FNContext.class, 0, 0, "default-collation()", STR),
  /** XQuery function. */
  STBASEURI(FNURI, FNContext.class, 0, 0, "static-base-uri()", URI_ZO),

  /* FNDate functions. */

  /** XQuery function. */
  DAYDAT(FNURI, FNDate.class, 1, 1, "day-from-date(item)", ITR_ZO),
  /** XQuery function. */
  DAYDTM(FNURI, FNDate.class, 1, 1, "day-from-dateTime(datetime)", ITR_ZO),
  /** XQuery function. */
  DAYDUR(FNURI, FNDate.class, 1, 1, "days-from-duration(dur)", ITR_ZO),
  /** XQuery function. */
  HOUDTM(FNURI, FNDate.class, 1, 1, "hours-from-dateTime(datetime)", ITR_ZO),
  /** XQuery function. */
  HOUDUR(FNURI, FNDate.class, 1, 1, "hours-from-duration(dur)", ITR_ZO),
  /** XQuery function. */
  HOUTIM(FNURI, FNDate.class, 1, 1, "hours-from-time(item)", ITR_ZO),
  /** XQuery function. */
  MINDTM(FNURI, FNDate.class, 1, 1, "minutes-from-dateTime(datetime)", ITR_ZO),
  /** XQuery function. */
  MINDUR(FNURI, FNDate.class, 1, 1, "minutes-from-duration(dur)", ITR_ZO),
  /** XQuery function. */
  MINTIM(FNURI, FNDate.class, 1, 1, "minutes-from-time(item)", ITR_ZO),
  /** XQuery function. */
  MONDAT(FNURI, FNDate.class, 1, 1, "month-from-date(item)", ITR_ZO),
  /** XQuery function. */
  MONDTM(FNURI, FNDate.class, 1, 1, "month-from-dateTime(datetime)", ITR_ZO),
  /** XQuery function. */
  MONDUR(FNURI, FNDate.class, 1, 1, "months-from-duration(dur)", ITR_ZO),
  /** XQuery function. */
  SECDTM(FNURI, FNDate.class, 1, 1, "seconds-from-dateTime(datetime)", ITR_ZO),
  /** XQuery function. */
  SECDUR(FNURI, FNDate.class, 1, 1, "seconds-from-duration(dur)", ITR_ZO),
  /** XQuery function. */
  SECTIM(FNURI, FNDate.class, 1, 1, "seconds-from-time(item)", ITR_ZO),
  /** XQuery function. */
  ZONDAT(FNURI, FNDate.class, 1, 1, "timezone-from-date(item)", DAT_ZM),
  /** XQuery function. */
  ZONDTM(FNURI, FNDate.class, 1, 1, "timezone-from-dateTime(item)", DAT_ZM),
  /** XQuery function. */
  ZONTIM(FNURI, FNDate.class, 1, 1, "timezone-from-time(item)", DAT_ZM),
  /** XQuery function. */
  YEADAT(FNURI, FNDate.class, 1, 1, "year-from-date(item)", ITR_ZO),
  /** XQuery function. */
  YEADTM(FNURI, FNDate.class, 1, 1, "year-from-dateTime(datetime)", ITR_ZO),
  /** XQuery function. */
  YEADUR(FNURI, FNDate.class, 1, 1, "years-from-duration(dur)", ITR_ZO),
  /** XQuery function. */
  DATZON(FNURI, FNDate.class, 1, 2, "adjust-date-to-timezone(date, zone?)",
      DAT_ZM),
  /** XQuery function. */
  DTMZON(FNURI, FNDate.class, 1, 2, "adjust-dateTime-to-timezone(date, zone?)",
      DAT_ZM),
  /** XQuery function. */
  TIMZON(FNURI, FNDate.class, 1, 2, "adjust-time-to-timezone(date, zone?)",
      DAT_ZM),
  /** XQuery function. */
  DATETIME(FNURI, FNDate.class, 2, 2, "dateTime(date, time)", DAT_ZM),

  /* FNGen functions. */

  /** XQuery function. */
  DATA(FNURI, FNGen.class, 1, 1, "data(item)", ITEM_ZM),
  /** XQuery function. */
  COLLECTION(FNURI, FNGen.class, 0, 1, "collection(item)", NOD_ZM),
  /** XQuery function. */
  DOC(FNURI, FNGen.class, 1, 1, "doc(item)", NOD_ZO),
  /** XQuery function. */
  DOCAVAILABLE(FNURI, FNGen.class, 1, 1, "doc-available(item)", BLN),
  /** XQuery function. */
  PUT(FNURI, FNGen.class, 2, 2, "put(node, string)", ITEM_Z),

  /* FNId functions. */

  /** XQuery function. */
  ID(FNURI, FNId.class, 1, 2, "id(string, item?)", NOD_ZM),
  /** XQuery function. */
  IDREF(FNURI, FNId.class, 1, 2, "idref(string, item?)", NOD_ZM),
  /** XQuery function. */
  LANG(FNURI, FNId.class, 1, 2, "lang(string, item?)", BLN),

  /* FNNode functions. */

  /** XQuery function. */
  DOCURI(FNURI, FNNode.class, 1, 1, "document-uri(node)", URI_ZO),
  /** XQuery function. */
  NILLED(FNURI, FNNode.class, 1, 1, "nilled(node)", BLN_ZO),
  /** XQuery function. */
  NODENAME(FNURI, FNNode.class, 1, 1, "node-name(node)", QNM_ZO),
  /** XQuery function. */
  LOCNAME(FNURI, FNNode.class, 0, 1, "local-name(node?)", STR),
  /** XQuery function. */
  NAME(FNURI, FNNode.class, 0, 1, "name(node?)", STR),
  /** XQuery function. */
  NSURI(FNURI, FNNode.class, 0, 1, "namespace-uri(node?)", URI),
  /** XQuery function. */
  ROOT(FNURI, FNNode.class, 0, 1, "root(node?)", NOD_ZO),
  /** XQuery function. */
  BASEURI(FNURI, FNNode.class, 0, 1, "base-uri(node?)", URI_ZO),

  /* FNFile functions */

  /** XQuery function */
  MKDIR(FILEURI, FNFile.class, 1, 1, "mkdir(item)", BLN),
  /** XQuery function */
  MKDIRS(FILEURI, FNFile.class, 1, 1, "mkdirs(item)", BLN),
  /** XQuery function */
  ISDIR(FILEURI, FNFile.class, 1, 1, "is-directory(item)", BLN),
  /** XQuery function */
  ISFILE(FILEURI, FNFile.class, 1, 1, "is-file(item)", BLN),
  /** XQuery function */
  ISREAD(FILEURI, FNFile.class, 1, 1, "is-readable(item)", BLN),
  /** XQuery function */
  ISWRITE(FILEURI, FNFile.class, 1, 1, "is-writeable(item)", BLN),
  /** XQuery function */
  FILES(FILEURI, FNFile.class, 1, 1, "files(item)", STR_ZM),
  /** XQuery function */
  PATHSEP(FILEURI, FNFile.class, 0, 0, "path-separator()", STR),
  /** XQuery function */
  DELETE(FILEURI, FNFile.class, 1, 1, "delete(item)", BLN),
  /** XQuery function */
  PATHTOFULL(FILEURI, FNFile.class, 1, 1, "path-to-full-path(item)", STR),

  /* FNNum functions. */

  /** XQuery function. */
  ABS(FNURI, FNNum.class, 1, 1, "abs(num)", ITR_ZO),
  /** XQuery function. */
  CEIL(FNURI, FNNum.class, 1, 1, "ceiling(num)", ITR_ZO),
  /** XQuery function. */
  FLOOR(FNURI, FNNum.class, 1, 1, "floor(num)", ITR_ZO),
  /** XQuery function. */
  RND(FNURI, FNNum.class, 1, 1, "round(num)", ITR_ZO),
  /** XQuery function. */
  RNDHLF(FNURI, FNNum.class, 1, 2, "round-half-to-even(num, prec?)", ITR_ZO),

  /* FNOut functions. */

  /** XQuery function. */
  ERROR(FNURI, FNOut.class, 0, 3, "error(code?, desc?, object?)", ITEM_Z),
  /** XQuery function. */
  TRACE(FNURI, FNOut.class, 2, 2, "trace(item, message)", ITEM_ZM),

  /* FNPat functions. */

  /** XQuery function. */
  MATCH(FNURI, FNPat.class, 2, 3, "matches(item, pattern, mod?)", BLN),
  /** XQuery function. */
  REPLACE(FNURI, FNPat.class, 3, 4, "replace(item, pattern, replace, mod?)",
      STR),
  /** XQuery function. */
  TOKEN(FNURI, FNPat.class, 2, 3, "tokenize(item, pattern, mod?)", STR_ZM),

  /* FNQName functions. */

  /** XQuery function. */
  INSCOPE(FNURI, FNQName.class, 1, 1, "in-scope-prefixes(elem)", STR_ZM),
  /** XQuery function. */
  LOCNAMEQNAME(FNURI, FNQName.class, 1, 1, "local-name-from-QName(qname)",
      STR_ZO),
  /** XQuery function. */
  NSURIPRE(FNURI, FNQName.class, 2, 2, "namespace-uri-for-prefix(pre, elem)",
      URI_ZO),
  /** XQuery function. */
  QNAME(FNURI, FNQName.class, 2, 2, "QName(uri, name)", QNM),
  /** XQuery function. */
  PREQNAME(FNURI, FNQName.class, 1, 1, "prefix-from-QName(qname)", STR_ZO),
  /** XQuery function. */
  RESQNAME(FNURI, FNQName.class, 2, 2, "resolve-QName(item, base)", QNM_ZO),
  /** XQuery function. */
  RESURI(FNURI, FNQName.class, 1, 2, "resolve-uri(name, elem?)", URI_ZO),

  /* FNSeq functions. */

  /** XQuery function. */
  DISTINCT(FNURI, FNSeq.class, 1, 2, "distinct-values(item, coll?)", ITEM_ZM),
  /** XQuery function. */
  INDEXOF(FNURI, FNSeq.class, 2, 3, "index-of(seq, item, coll?)", ITR_ZM),
  /** XQuery function. */
  INSBEF(FNURI, FNSeq.class, 3, 3, "insert-before(seq, pos, seq2)", ITEM_ZM),
  /** XQuery function. */
  REMOVE(FNURI, FNSeq.class, 2, 2, "remove(seq, position)", ITEM_ZM),
  /** XQuery function. */
  REVERSE(FNURI, FNSeq.class, 1, 1, "reverse(seq)", ITEM_ZM),
  /** XQuery function. */
  SUBSEQ(FNURI, FNSeq.class, 2, 3, "subsequence(seq, start, len?)", ITEM_ZM),

  /* FNSimple functions. */

  /** XQuery function. */
  FALSE(FNURI, FNSimple.class, 0, 0, "false()", BLN),
  /** XQuery function. */
  TRUE(FNURI, FNSimple.class, 0, 0, "true()", BLN),
  /** XQuery function. */
  BOOLEAN(FNURI, FNSimple.class, 1, 1, "boolean(item)", BLN),
  /** XQuery function. */
  NOT(FNURI, FNSimple.class, 1, 1, "not(item)", BLN),
  /** XQuery function. */
  EMPTY(FNURI, FNSimple.class, 1, 1, "empty(item)", BLN),
  /** XQuery function. */
  EXISTS(FNURI, FNSimple.class, 1, 1, "exists(item)", BLN),
  /** XQuery function. */
  UNORDER(FNURI, FNSimple.class, 1, 1, "unordered(item)", ITEM_ZM),
  /** XQuery function. */
  ZEROORONE(FNURI, FNSimple.class, 1, 1, "zero-or-one(item)", ITEM_ZM),
  /** XQuery function. */
  EXACTLYONE(FNURI, FNSimple.class, 1, 1, "exactly-one(item)", ITEM_ZM),
  /** XQuery function. */
  ONEORMORE(FNURI, FNSimple.class, 1, 1, "one-or-more(item)", ITEM_ZM),
  /** XQuery function. */
  DEEPEQUAL(FNURI, FNSimple.class, 2, 3, "deep-equal(item, item, coll?)", BLN),

  /* FNStr functions. */

  /** XQuery function. */
  CODEPNT(FNURI, FNStr.class, 2, 2, "codepoint-equal(string, string)", BLN_ZO),
  /** XQuery function. */
  CODESTR(FNURI, FNStr.class, 1, 1, "codepoints-to-string(num*)", STR),
  /** XQuery function. */
  COMPARE(FNURI, FNStr.class, 2, 3, "compare(first, second, coll)", ITR_ZO),
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
  STCODE(FNURI, FNStr.class, 1, 1, "string-to-codepoints(string)", ITR_ZM),
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
  EVAL(BXURI, FNBaseX.class, 1, 1, "eval(string)", ITEM_ZM),
  /** Project specific function - returns a random number. */
  RANDOM(BXURI, FNBaseX.class, 0, 0, "random()", ITR),
  /** Project specific function - accesses an index. */
  INDEX(BXURI, FNBaseX.class, 2, 2, "index(item, type)", NOD_ZM),
  /** Project specific function - opens and returns file contents. */
  READ(BXURI, FNBaseX.class, 1, 1, "read(string)", STR),
  /** Project specific function - evaluates the specified query file. */
  RUN(BXURI, FNBaseX.class, 1, 1, "run(string)", ITEM_ZM),
  /** Project specific function - opens a database node. */
  DB(BXURI, FNBaseX.class, 1, 2, "db(string, id?)", NOD),
  /** Project specific function - returns the id of a node. */
  DBID(BXURI, FNBaseX.class, 1, 1, "node-id(item)", ITR),
  /** Project specific function - returns a filesystem path. */
  FSPATH(BXURI, FNBaseX.class, 1, 1, "fspath(item)", STR);

  /** Function classes. */
  final Class<? extends Fun> func;
  /** Function uri. */
  final byte[] uri;
  /** Descriptions. */
  final String desc;
  /** Minimum number of arguments. */
  final int min;
  /** Maximum number of arguments. */
  final int max;
  /** Return type. */
  final SeqType ret;

  /**
   * Constructor.
   * @param ur uri
   * @param fun function class
   * @param mn minimum number of arguments
   * @param mx maximum number of arguments
   * @param dsc description
   * @param rt return value
   */
  private FunDef(final byte[] ur, final Class<? extends Fun> fun, final int mn,
      final int mx, final String dsc, final SeqType rt) {
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
