package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.item.SeqType.*;

import java.util.*;

import org.basex.query.expr.*;
import org.basex.query.item.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Definitions of all built-in XQuery functions.
 * Namespace mappings for function prefixes and URIs are specified in the
 * static code in the {@code NSGlobal} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public enum Function {

  /* FNAcc functions. */

  /** XQuery function. */
  POSITION(FNAcc.class, "position()", ITR),
  /** XQuery function. */
  LAST(FNAcc.class, "last()", ITR),
  /** XQuery function. */
  STRING(FNAcc.class, "string([item])", STR, 0, ITEM_ZO),
  /** XQuery function. */
  NUMBER(FNAcc.class, "number([item])", DBL, 0, AAT_ZO),
  /** XQuery function. */
  STRING_LENGTH(FNAcc.class, "string-length([item])", ITR, 0, STR_ZO),
  /** XQuery function. */
  NORMALIZE_SPACE(FNAcc.class, "normalize-space([string])", STR, 0, STR_ZO),
  /** XQuery function. */
  NAMESPACE_URI_FROM_QNAME(FNAcc.class, "namespace-uri-from-QName(qname)",
      URI_ZO, QNM_ZO),

  /* FNAggr functions. */

  /** XQuery function. */
  AVG(FNAggr.class, "avg(item)", AAT_ZO, AAT_ZM),
  /** XQuery function. */
  COUNT(FNAggr.class, "count(item)", ITR, ITEM_ZM),
  /** XQuery function. */
  MAX(FNAggr.class, "max(item[,coll])", AAT_ZO, 1, AAT_ZM, STR),
  /** XQuery function. */
  MIN(FNAggr.class, "min(item[,coll])", AAT_ZO, 1, AAT_ZM, STR),
  /** XQuery function. */
  SUM(FNAggr.class, "sum(item[,zero])", AAT_ZO, 1, AAT_ZM, AAT_ZO),

  /* FNContext functions. */

  /** XQuery function. */
  CURRENT_DATE(FNContext.class, "current-date()", DAT),
  /** XQuery function. */
  CURRENT_DATETIME(FNContext.class, "current-dateTime()", DTM),
  /** XQuery function. */
  CURRENT_TIME(FNContext.class, "current-time()", TIM),
  /** XQuery function. */
  IMPLICIT_TIMEZONE(FNContext.class, "implicit-timezone()", DTD),
  /** XQuery function. */
  DEFAULT_COLLATION(FNContext.class, "default-collation()", STR),
  /** XQuery function. */
  STATIC_BASE_URI(FNContext.class, "static-base-uri()", URI_ZO),

  /* FNDate functions. */

  /** XQuery function. */
  DAY_FROM_DATE(FNDate.class, "day-from-date(item)", ITR_ZO, DAT_ZO),
  /** XQuery function. */
  DAY_FROM_DATETIME(FNDate.class, "day-from-dateTime(datetime)", ITR_ZO, DTM_ZO),
  /** XQuery function. */
  DAYS_FROM_DURATION(FNDate.class, "days-from-duration(dur)", ITR_ZO, DUR_ZO),
  /** XQuery function. */
  HOURS_FROM_DATETIME(FNDate.class, "hours-from-dateTime(datetime)", ITR_ZO, DTM_ZO),
  /** XQuery function. */
  HOURS_FROM_DURATION(FNDate.class, "hours-from-duration(dur)", ITR_ZO, DUR_ZO),
  /** XQuery function. */
  HOURS_FROM_TIME(FNDate.class, "hours-from-time(item)", ITR_ZO, TIM_ZO),
  /** XQuery function. */
  MINUTES_FROM_DATETIME(FNDate.class, "minutes-from-dateTime(datetime)", ITR_ZO, DTM_ZO),
  /** XQuery function. */
  MINUTES_FROM_DURATION(FNDate.class, "minutes-from-duration(dur)", ITR_ZO, DUR_ZO),
  /** XQuery function. */
  MINUTES_FROM_TIME(FNDate.class, "minutes-from-time(item)", ITR_ZO, TIM_ZO),
  /** XQuery function. */
  MONTH_FROM_DATE(FNDate.class, "month-from-date(item)", ITR_ZO, DAT_ZO),
  /** XQuery function. */
  MONTH_FROM_DATETIME(FNDate.class, "month-from-dateTime(datetime)", ITR_ZO, DTM_ZO),
  /** XQuery function. */
  MONTHS_FROM_DURATION(FNDate.class, "months-from-duration(dur)", ITR_ZO, DUR_ZO),
  /** XQuery function. */
  SECONDS_FROM_DATETIME(FNDate.class, "seconds-from-dateTime(datetime)", DEC_ZO, DTM_ZO),
  /** XQuery function. */
  SECONDS_FROM_DURATION(FNDate.class, "seconds-from-duration(dur)", DEC_ZO, DUR_ZO),
  /** XQuery function. */
  SECONDS_FROM_TIME(FNDate.class, "seconds-from-time(item)", DEC_ZO, TIM_ZO),
  /** XQuery function. */
  TIMEZONE_FROM_DATE(FNDate.class, "timezone-from-date(item)", DTD_ZO, DAT_ZO),
  /** XQuery function. */
  TIMEZONE_FROM_DATETIME(FNDate.class, "timezone-from-dateTime(item)", DTD_ZO, DTM_ZO),
  /** XQuery function. */
  TIMEZONE_FROM_TIME(FNDate.class, "timezone-from-time(item)", DTD_ZO, TIM_ZO),
  /** XQuery function. */
  YEAR_FROM_DATE(FNDate.class, "year-from-date(item)", ITR_ZO, DAT_ZO),
  /** XQuery function. */
  YEAR_FROM_DATETIME(FNDate.class, "year-from-dateTime(datetime)", ITR_ZO, DTM_ZO),
  /** XQuery function. */
  YEARS_FROM_DURATION(FNDate.class, "years-from-duration(dur)", ITR_ZO, DUR_ZO),
  /** XQuery function. */
  ADJUST_DATE_TO_TIMEZONE(FNDate.class, "adjust-date-to-timezone(date[,zone])",
      DAT_ZO, 1, DAT_ZO, DTD_ZO),
  /** XQuery function. */
  ADJUST_DATETIME_TO_TIMEZONE(FNDate.class, "adjust-dateTime-to-timezone(date[,zone])",
      DTM, 1, DTM_ZO, DTD_ZO),
  /** XQuery function. */
  ADJUST_TIME_TO_TIMEZONE(FNDate.class, "adjust-time-to-timezone(date[,zone])",
      TIM_ZO, 1, TIM_ZO, DTD_ZO),
  /** XQuery function. */
  DATETIME(FNDate.class, "dateTime(date,time)", DTM_ZO, DAT_ZO, TIM_ZO),

  /* FNFormat functions. */

  /** XQuery function. */
  FORMAT_INTEGER(FNFormat.class, "format-integer(number,picture[,lang])",
      STR, 2, ITR_ZO, STR, STR),
  /** XQuery function. */
  FORMAT_NUMBER(FNFormat.class, "format-number(number,picture[,format])",
      STR, 2, ITR_ZO, STR, STR),
  /** XQuery function. */
  FORMAT_DATETIME(FNFormat.class, "format-dateTime(number,picture,[lang,cal,place])",
      STR_ZO, 2, DTM_ZO, STR, STR_ZO, STR_ZO, STR_ZO),
  /** XQuery function. */
  FORMAT_DATE(FNFormat.class, "format-date(date,picture,[lang,cal,place])",
      STR_ZO, 2, DAT_ZO, STR, STR_ZO, STR_ZO, STR_ZO),
  /** XQuery function. */
  FORMAT_TIME(FNFormat.class, "format-time(number,picture,[lang,cal,place])",
      STR_ZO, 2, TIM_ZO, STR, STR_ZO, STR_ZO, STR_ZO),

  /* FNFunc functions. */

  /** XQuery function. */
  FILTER(FNFunc.class, "filter(function,seq)", ITEM_ZM,
      FuncType.get(BLN, ITEM).seqType(), ITEM_ZM),
  /** XQuery function. */
  FUNCTION_NAME(FNFunc.class, "function-name(function)", QNM_ZO, FUN_O),
  /** XQuery function. */
  FUNCTION_ARITY(FNFunc.class, "function-arity(function)", ITR, FUN_O),
  /** XQuery function. */
  FUNCTION_LOOKUP(FNFunc.class, "function-lookup(name,arity)", FUN_O, QNM, ITR),
  /** XQuery function. */
  MAP(FNFunc.class, "map(function,seq)", ITEM_ZM,
      FuncType.get(ITEM_ZM, ITEM).seqType(), ITEM_ZM),
  /** XQuery function. */
  MAP_PAIRS(FNFunc.class, "map-pairs(function,seq1,seq2)", ITEM_ZM,
      FuncType.get(ITEM_ZM, ITEM, ITEM).seqType(), ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  FOLD_LEFT(FNFunc.class, "fold-left(function,zero,seq)", ITEM_ZM,
      FuncType.get(ITEM_ZM, ITEM_ZM, ITEM).seqType(), ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  FOLD_RIGHT(FNFunc.class, "fold-right(function,zero,seq)", ITEM_ZM,
      FuncType.get(ITEM_ZM, ITEM, ITEM_ZM).seqType(), ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  PARTIAL_APPLY(FNFunc.class, "partial-apply(function,arg[,pos])",
      FUN_O, 2, FUN_O, ITEM_ZM, ITR),

  /* FNGen functions. */

  /** XQuery function. */
  DATA(FNGen.class, "data([item])", AAT_ZM, 0, ITEM_ZM),
  /** XQuery function. */
  COLLECTION(FNGen.class, "collection([uri])", NOD_ZM, 0, STR_ZO),
  /** XQuery function. */
  DOC(FNGen.class, "doc(uri)", DOC_ZO, STR_ZO),
  /** XQuery function. */
  DOC_AVAILABLE(FNGen.class, "doc-available(uri)", BLN, STR_ZO),
  /** XQuery function. */
  PUT(FNGen.class, "put(node,uri)", EMP, NOD, STR_ZO),
  /** XQuery function. */
  UNPARSED_TEXT(FNGen.class, "unparsed-text(uri[,encoding])", STR_ZO, 1, STR_ZO, STR),
  /** XQuery function. */
  UNPARSED_TEXT_LINES(FNGen.class, "unparsed-text-lines(uri[,encoding])",
      STR_ZM, 1, STR_ZO, STR),
  /** XQuery function. */
  UNPARSED_TEXT_AVAILABLE(FNGen.class, "unparsed-text-available(uri[,encoding])",
      BLN, 1, STR_ZO, STR),
  /** XQuery function. */
  PARSE_XML(FNGen.class, "parse-xml(string[,base])", DOC_O, 1, STR_ZO, STR),
  /** XQuery function. */
  URI_COLLECTION(FNGen.class, "uri-collection([uri])", URI_ZM, 0, STR_ZO),
  /** XQuery function. */
  SERIALIZE(FNGen.class, "serialize(items[,params])", STR, 1, ITEM_ZM, ITEM),

  /* FNId functions. */

  /** XQuery function. */
  ID(FNId.class, "id(string[,item])", ELM_ZM, 1, STR_ZM, NOD),
  /** XQuery function. */
  IDREF(FNId.class, "idref(string[,item])", NOD_ZM, 1, STR_ZM, NOD),
  /** XQuery function. */
  LANG(FNId.class, "lang(string[,item])", BLN, 1, STR_ZO, NOD),
  /** XQuery function. */
  ELEMENT_WITH_ID(FNId.class, "element-with-id(string[,item])", ELM_ZM, 1, STR_ZM, NOD),

  /* FNInfo functions. */

  /** XQuery function. */
  ERROR(FNInfo.class, "error([code[,desc[,object]]])", EMP, 0, QNM_ZO, STR, ITEM_ZM),
  /** XQuery function. */
  TRACE(FNInfo.class, "trace(value,label)", ITEM_ZM, ITEM_ZM, STR),
  /** XQuery function. */
  ENVIRONMENT_VARIABLE(FNInfo.class, "environment-variable(string)", STR_ZO, STR),
  /** XQuery function. */
  AVAILABLE_ENVIRONMENT_VARIABLES(FNInfo.class, "available-environment-variables()",
      STR_ZM),

  /* FNNode functions. */

  /** XQuery function. */
  DOCUMENT_URI(FNNode.class, "document-uri([node])", URI_ZO, 0, NOD_ZO),
  /** XQuery function. */
  NILLED(FNNode.class, "nilled(node)", BLN_ZO, NOD_ZO),
  /** XQuery function. */
  NODE_NAME(FNNode.class, "node-name([node])", QNM_ZO, 0, NOD_ZO),
  /** XQuery function. */
  LOCAL_NAME(FNNode.class, "local-name([node])", STR, 0, NOD_ZO),
  /** XQuery function. */
  NAME(FNNode.class, "name([node])", STR, 0, NOD_ZO),
  /** XQuery function. */
  NAMESPACE_URI(FNNode.class, "namespace-uri([node])", URI, 0, NOD_ZO),
  /** XQuery function. */
  ROOT(FNNode.class, "root([node])", NOD_ZO, 0, NOD_ZO),
  /** XQuery function. */
  BASE_URI(FNNode.class, "base-uri([node])", URI_ZO, 0, NOD_ZO),
  /** XQuery function. */
  GENERATE_ID(FNNode.class, "generate-id([node])", STR, 0, NOD_ZO),
  /** XQuery function. */
  HAS_CHILDREN(FNNode.class, "has-children([node])", BLN, 0, NOD_ZM),
  /** XQuery function. */
  PATH(FNNode.class, "path([node])", STR_ZO, 0, NOD_ZO),

  /* FNNum functions. */

  /** XQuery function. */
  ABS(FNNum.class, "abs(num)", AAT_ZO, AAT_ZO),
  /** XQuery function. */
  CEILING(FNNum.class, "ceiling(num)", AAT_ZO, AAT_ZO),
  /** XQuery function. */
  FLOOR(FNNum.class, "floor(num)", AAT_ZO, AAT_ZO),
  /** XQuery function. */
  ROUND(FNNum.class, "round(num[,prec])", AAT_ZO, 1, AAT_ZO, ITR),
  /** XQuery function. */
  ROUND_HALF_TO_EVEN(FNNum.class, "round-half-to-even(num[,prec])",
      AAT_ZO, 1, AAT_ZO, ITR),

  /* FNPat functions. */

  /** XQuery function. */
  MATCHES(FNPat.class, "matches(item,pattern[,mod])", BLN, 2, STR_ZO, STR, STR),
  /** XQuery function. */
  REPLACE(FNPat.class, "replace(item,pattern,replace[,mod])",
      STR, 3, STR_ZO, STR, STR, STR),
  /** XQuery function. */
  TOKENIZE(FNPat.class, "tokenize(item,pattern[,mod])", STR_ZM, 2, STR_ZO, STR, STR),
  /** XQuery function. */
  ANALYZE_STRING(FNPat.class, "analyze-string(input,pattern[,mod])",
      ELM, 2, STR_ZO, STR, STR),

  /* FNQName functions. */

  /** XQuery function. */
  IN_SCOPE_PREFIXES(FNQName.class, "in-scope-prefixes(elem)", STR_ZM, ELM),
  /** XQuery function. */
  LOCAL_NAME_FROM_QNAME(FNQName.class, "local-name-from-QName(qname)", NCN_ZO, QNM_ZO),
  /** XQuery function. */
  NAMESPACE_URI_FOR_PREFIX(FNQName.class, "namespace-uri-for-prefix(pref,elem)",
      URI_ZO, STR_ZO, ELM),
  /** XQuery function. */
  QNAME(FNQName.class, "QName(uri,name)", QNM, STR_ZO, STR),
  /** XQuery function. */
  PREFIX_FROM_QNAME(FNQName.class, "prefix-from-QName(qname)", NCN_ZO, QNM_ZO),
  /** XQuery function. */
  RESOLVE_QNAME(FNQName.class, "resolve-QName(item,base)", QNM_ZO, STR_ZO, ELM),
  /** XQuery function. */
  RESOLVE_URI(FNQName.class, "resolve-uri(name[,elem])", URI_ZO, 1, STR_ZO, STR),

  /* FNSeq functions. */

  /** XQuery function. */
  DISTINCT_VALUES(FNSeq.class, "distinct-values(items[,coll])", AAT_ZM, 1, AAT_ZM, STR),
  /** XQuery function. */
  INDEX_OF(FNSeq.class, "index-of(items,item[,coll])", ITR_ZM, 2, AAT_ZM, AAT, STR),
  /** XQuery function. */
  INSERT_BEFORE(FNSeq.class, "insert-before(items,pos,insert)",
      ITEM_ZM, ITEM_ZM, ITR, ITEM_ZM),
  /** XQuery function. */
  REMOVE(FNSeq.class, "remove(items,pos)", ITEM_ZM, ITEM_ZM, ITR),
  /** XQuery function. */
  REVERSE(FNSeq.class, "reverse(items)", ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  SUBSEQUENCE(FNSeq.class, "subsequence(items,start[,len])",
      ITEM_ZM, 2, ITEM_ZM, DBL, DBL),
  /** XQuery function. */
  HEAD(FNSeq.class, "head(items)", ITEM_ZO, ITEM_ZM),
  /** XQuery function. */
  TAIL(FNSeq.class, "tail(items)", ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  OUTERMOST(FNSeq.class, "outermost(nodes)", NOD_ZM, NOD_ZM),
  /** XQuery function. */
  INNERMOST(FNSeq.class, "innermost(nodes)", NOD_ZM, NOD_ZM),

  /* FNSimple functions. */

  /** XQuery function. */
  FALSE(FNSimple.class, "false()", BLN),
  /** XQuery function. */
  TRUE(FNSimple.class, "true()", BLN),
  /** XQuery function. */
  BOOLEAN(FNSimple.class, "boolean(item)", BLN, ITEM_ZM),
  /** XQuery function. */
  NOT(FNSimple.class, "not(item)", BLN, ITEM_ZM),
  /** XQuery function. */
  EMPTY(FNSimple.class, "empty(item)", BLN, ITEM_ZM),
  /** XQuery function. */
  EXISTS(FNSimple.class, "exists(item)", BLN, ITEM_ZM),
  /** XQuery function. */
  UNORDERED(FNSimple.class, "unordered(item)", ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  ZERO_OR_ONE(FNSimple.class, "zero-or-one(item)", ITEM_ZO, ITEM_ZM),
  /** XQuery function. */
  EXACTLY_ONE(FNSimple.class, "exactly-one(item)", ITEM, ITEM_ZM),
  /** XQuery function. */
  ONE_OR_MORE(FNSimple.class, "one-or-more(item)", ITEM_OM, ITEM_ZM),
  /** XQuery function. */
  DEEP_EQUAL(FNSimple.class, "deep-equal(item,item[,coll])",
      BLN, 2, ITEM_ZM, ITEM_ZM, STR),

  /* FNStr functions. */

  /** XQuery function. */
  CODEPOINT_EQUAL(FNStr.class, "codepoint-equal(string,string)", BLN_ZO, STR_ZO, STR_ZO),
  /** XQuery function. */
  CODEPOINTS_TO_STRING(FNStr.class, "codepoints-to-string(nums)", STR, ITR_ZM),
  /** XQuery function. */
  COMPARE(FNStr.class, "compare(first,second[,coll])", ITR_ZO, 2, STR_ZO, STR_ZO, STR),
  /** XQuery function. */
  CONCAT(FNStr.class, "concat(atom,atom[,...])", STR, -2, AAT_ZO, AAT_ZO),
  /** XQuery function. */
  CONTAINS(FNStr.class, "contains(string,sub[,coll])", BLN, 2, STR_ZO, STR_ZO, STR),
  /** XQuery function. */
  ENCODE_FOR_URI(FNStr.class, "encode-for-uri(string)", STR, STR_ZO),
  /** XQuery function. */
  ENDS_WITH(FNStr.class, "ends-with(string,sub[,coll])", BLN, 2, STR_ZO, STR_ZO, STR),
  /** XQuery function. */
  ESCAPE_HTML_URI(FNStr.class, "escape-html-uri(string)", STR, STR_ZO),
  /** XQuery function. */
  IRI_TO_URI(FNStr.class, "iri-to-uri(string)", STR, STR_ZO),
  /** XQuery function. */
  LOWER_CASE(FNStr.class, "lower-case(string)", STR, STR_ZO),
  /** XQuery function. */
  NORMALIZE_UNICODE(FNStr.class, "normalize-unicode(string[,form])", STR, 1, STR_ZO, STR),
  /** XQuery function. */
  STARTS_WITH(FNStr.class, "starts-with(string,sub[,coll])", BLN, 2, STR_ZO, STR_ZO, STR),
  /** XQuery function. */
  STRING_JOIN(FNStr.class, "string-join(strings[,sep])", STR, 1, STR_ZM, STR),
  /** XQuery function. */
  STRING_TO_CODEPOINTS(FNStr.class, "string-to-codepoints(string)", ITR_ZM, STR_ZO),
  /** XQuery function. */
  SUBSTRING(FNStr.class, "substring(string,start[,len])", STR, 2, STR_ZO, DBL, DBL),
  /** XQuery function. */
  SUBSTRING_AFTER(FNStr.class, "substring-after(string,sub[,coll])",
      STR, 2, STR_ZO, STR_ZO, STR),
  /** XQuery function. */
  SUBSTRING_BEFORE(FNStr.class, "substring-before(string,sub[,coll])",
      STR, 2, STR_ZO, STR_ZO, STR),
  /** XQuery function. */
  TRANSLATE(FNStr.class, "translate(string,map,trans)", STR, STR_ZO, STR, STR),
  /** XQuery function. */
  UPPER_CASE(FNStr.class, "upper-case(string)", STR, STR_ZO),

  /* FNMap functions. */

  /** XQuery Function. */
  _MAP_NEW(FNMap.class, "new([maps[,coll]])", MAP_O, 0, MAP_ZM, STR),
  /** XQuery Function. */
  _MAP_ENTRY(FNMap.class, "entry(key,value)", MAP_O, AAT, ITEM_ZM),
  /** XQuery Function. */
  _MAP_GET(FNMap.class, "get(map,key)", ITEM_ZM, MAP_O, AAT),
  /** XQuery Function. */
  _MAP_CONTAINS(FNMap.class, "contains(map,key)", BLN, MAP_O, AAT),
  /** XQuery Function. */
  _MAP_REMOVE(FNMap.class, "remove(map,key)", MAP_O, MAP_O, AAT),
  /** XQuery Function. */
  _MAP_SIZE(FNMap.class, "size(map)", ITR, MAP_O),
  /** XQuery Function. */
  _MAP_KEYS(FNMap.class, "keys(map)", AAT_ZM, MAP_O),
  /** XQuery Function. */
  _MAP_COLLATION(FNMap.class, "collation(map)", STR, MAP_O),

  /* FNMath functions. */

  /** XQuery math function. */
  _MATH_PI(FNMath.class, "pi()", DBL),
  /** XQuery math function. */
  _MATH_SQRT(FNMath.class, "sqrt(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  _MATH_SIN(FNMath.class, "sin(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  _MATH_COS(FNMath.class, "cos(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  _MATH_TAN(FNMath.class, "tan(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  _MATH_ASIN(FNMath.class, "asin(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  _MATH_ACOS(FNMath.class, "acos(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  _MATH_ATAN(FNMath.class, "atan(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  _MATH_ATAN2(FNMath.class, "atan2(number,number)", DBL, DBL, DBL),
  /** XQuery math function. */
  _MATH_POW(FNMath.class, "pow(number,number)", DBL_ZO, DBL_ZO, ITR),
  /** XQuery math function. */
  _MATH_EXP(FNMath.class, "exp(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  _MATH_EXP10(FNMath.class, "exp10(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  _MATH_LOG(FNMath.class, "log(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  _MATH_LOG10(FNMath.class, "log10(number)", DBL_ZO, DBL_ZO),

  /** XQuery math function (project specific). */
  _MATH_RANDOM(FNMath.class, "random()", DBL),
  /** XQuery math function (project specific). */
  _MATH_E(FNMath.class, "e()", DBL),
  /** XQuery math function (project specific). */
  _MATH_SINH(FNMath.class, "sinh(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function (project specific). */
  _MATH_COSH(FNMath.class, "cosh(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function (project specific). */
  _MATH_TANH(FNMath.class, "tanh(number)", DBL_ZO, DBL_ZO),

  /* FNArchive functions. */

  /** XQuery function */
  _ARCHIVE_CREATE(FNArchive.class, "create(entries,contents)", B64, ELM_ZM, ITEM_ZM),
  /** XQuery function */
  _ARCHIVE_ENTRIES(FNArchive.class, "entries(zip)", ELM_ZM, B64),
  /** XQuery function */
  _ARCHIVE_EXTRACT_TEXT(FNArchive.class, "extract-text(zip[,names[,encoding]])",
      STR_ZM, 1, B64, STR_ZM, STR),
  /** XQuery function */
  _ARCHIVE_EXTRACT_BINARY(FNArchive.class, "extract-binary(zip[,names])",
      B64_ZM, 1, B64, STR_ZM),
  /** XQuery function */
  _ARCHIVE_UPDATE(FNArchive.class, "update(zip,entries,contents)",
      B64, B64, ELM_ZM, ITEM_ZM),
  /** XQuery function */
  _ARCHIVE_DELETE(FNArchive.class, "delete(zip,entries)", B64, B64, STR_ZM),

  /* FNConvert functions. */

  /** Conversion function. */
  _CONVERT_INTEGER_TO_BASE(FNConvert.class, "integer-to-base(num,base)", STR, ITR, ITR),
  /** Conversion function. */
  _CONVERT_INTEGER_FROM_BASE(FNConvert.class, "integer-from-base(str,base)",
      ITR, STR, ITR),
  /** Conversion function. */
  _CONVERT_TO_BYTES(FNConvert.class, "to-bytes(binary)", BYT_ZM, ITEM),
  /** Conversion function. */
  _CONVERT_TO_STRING(FNConvert.class, "to-string(binary[,encoding])", STR, 1, ITEM, STR),
  /** Conversion function. */
  _CONVERT_TO_BASE64BINARY(FNConvert.class, "to-base64Binary(string[,encoding])",
      B64, 1, STR, STR),
  /** Conversion function. */
  _CONVERT_TO_HEXBINARY(FNConvert.class, "to-hexBinary(string[,encoding])",
      HEX, 1, STR, STR),

  /* FNDb functions. */

  /** Database function: opens a database. */
  _DB_OPEN(FNDb.class, "open(database[,path])", NOD_ZM, 1, STR, STR),
  /** Database function: opens a specific database node. */
  _DB_OPEN_PRE(FNDb.class, "open-pre(database,pre)", NOD_ZM, STR, ITR),
  /** Database function: opens a specific database node. */
  _DB_OPEN_ID(FNDb.class, "open-id(database,id)", NOD_ZM, STR, ITR),
  /** Database function: searches the text index. */
  _DB_TEXT(FNDb.class, "text(database,string)", NOD_ZM, STR, ITEM),
  /** Database function: searches the text index. */
  _DB_TEXT_RANGE(FNDb.class, "text-range(database,from,to)", NOD_ZM, STR, ITEM, ITEM),
  /** Database function: searches the attribute index. */
  _DB_ATTRIBUTE(FNDb.class, "attribute(database,string[,name])",
      NOD_ZM, 2, STR, ITEM, STR),
  /** Database function: searches the attribute index. */
  _DB_ATTRIBUTE_RANGE(FNDb.class, "attribute-range(database,from,to[,name])",
      NOD_ZM, 3, STR, ITEM, ITEM, STR),
  /** Database function: searches the full-text index. */
  _DB_FULLTEXT(FNDb.class, "fulltext(database,string)", NOD_ZM, STR, STR),
  /** Database function: lists all databases or documents in a database. */
  _DB_LIST(FNDb.class, "list([database[,path]])", STR_ZM, 0, STR, STR),
  /** Database function: lists databases details. */
  _DB_LIST_DETAILS(FNDb.class, "list-details([database[,path]])", NOD_ZM, 0, STR, STR),
  /** Database function: lists system information. */
  _DB_SYSTEM(FNDb.class, "system()", STR),
  /** Database function: returns database or index information. */
  _DB_INFO(FNDb.class, "info(database)", STR, STR),
  /** Database function: returns the node ids of database nodes. */
  _DB_NODE_ID(FNDb.class, "node-id(nodes)", ITR_ZM, NOD_ZM),
  /** Database function: returns the pre values of database nodes. */
  _DB_NODE_PRE(FNDb.class, "node-pre(nodes)", ITR_ZM, NOD_ZM),
  /** Database function: sends result to connected clients. */
  _DB_EVENT(FNDb.class, "event(name,query)", EMP, STR, ITEM_ZM),
  /** Database function: returns the result after all other operations. */
  _DB_OUTPUT(FNDb.class, "output(expression)", EMP, ITEM_ZM),
  /** Database function: add document(s) to a database. */
  _DB_ADD(FNDb.class, "add(database,nodes[,name[,path]])", EMP, 2, STR, DOC_ZM, STR, STR),
  /** Database function: delete document(s) from a database. */
  _DB_DELETE(FNDb.class, "delete(database,path)", EMP, STR, STR),
  /** Database function: rename document(s). */
  _DB_RENAME(FNDb.class, "rename(database,path,newpath)", EMP, STR, STR, STR),
  /** Database function: replace document(s). */
  _DB_REPLACE(FNDb.class, "replace(database,path,item)", EMP, STR, STR, ITEM),
  /** Database function: optimize database structures. */
  _DB_OPTIMIZE(FNDb.class, "optimize(database[,all])", EMP, 1, STR, BLN),
  /** Database function: retrieves binary data. */
  _DB_RETRIEVE(FNDb.class, "retrieve(database,path)", B64, STR, STR),
  /** Database function: stores binary data. */
  _DB_STORE(FNDb.class, "store(database,path,value)", EMP, STR, STR, ITEM),
  /** Database function: checks if the specified resource is an xml document. */
  _DB_IS_XML(FNDb.class, "is-xml(database,path)", BLN, STR, STR),
  /** Database function: checks if the specified resource is a raw file. */
  _DB_IS_RAW(FNDb.class, "is-raw(database,path)", BLN, STR, STR),
  /** Database function: checks if the specified database or resource exists. */
  _DB_EXISTS(FNDb.class, "exists(database[,path])", BLN, 1, STR, STR),
  /** Database function: returns the content type of a database file. */
  _DB_CONTENT_TYPE(FNDb.class, "content-type(database,path)", ITEM, STR, STR),

  /* FNIndex functions. */

  /** Index function: returns index facet information. */
  _INDEX_FACETS(FNIndex.class, "facets(database,format)", DOC_O, 1, STR, STR),
  /** Index function: returns texts. */
  _INDEX_TEXTS(FNIndex.class, "texts(database,entry[,order])", NOD_ZM, 1, STR, STR, BLN),
  /** Index function: returns attribute values. */
  _INDEX_ATTRIBUTES(FNIndex.class, "attributes(database,prefix[,order])",
      NOD_ZM, 1, STR, STR, BLN),
  /** Index function: returns element names. */
  _INDEX_ELEMENT_NAMES(FNIndex.class, "element-names(database)", NOD_ZM, STR),
  /** Index function: returns attribute names. */
  _INDEX_ATTRIBUTE_NAMES(FNIndex.class, "attribute-names(database)", NOD_ZM, STR),

  /* FNFile functions (EXPath). */

  /** XQuery function */
  _FILE_EXISTS(FNFile.class, "exists(path)", BLN, STR),
  /** XQuery function */
  _FILE_IS_DIRECTORY(FNFile.class, "is-directory(path)", BLN, STR),
  /** XQuery function */
  _FILE_IS_FILE(FNFile.class, "is-file(path)", BLN, STR),
  /** XQuery function */
  _FILE_LAST_MODIFIED(FNFile.class, "last-modified(path)", DTM, STR),
  /** XQuery function */
  _FILE_SIZE(FNFile.class, "size(path)", ITR, STR),
  /** XQuery function */
  _FILE_BASE_NAME(FNFile.class, "base-name(path[,suffix])", STR, 1, STR, STR),
  /** XQuery function */
  _FILE_DIR_NAME(FNFile.class, "dir-name(path)", STR, STR),
  /** XQuery function */
  _FILE_PATH_TO_NATIVE(FNFile.class, "path-to-native(path)", STR, STR),
  /** XQuery function */
  _FILE_PATH_TO_URI(FNFile.class, "path-to-uri(path)", URI, STR),
  /** XQuery function */
  _FILE_RESOLVE_PATH(FNFile.class, "resolve-path(path)", STR, STR),
  /** XQuery function */
  _FILE_LIST(FNFile.class, "list(path[,recursive[,pattern]])", STR_ZM, 1, STR, BLN, STR),
  /** XQuery function */
  _FILE_CREATE_DIRECTORY(FNFile.class, "create-directory(path)", EMP, STR),
  /** XQuery function */
  _FILE_DELETE(FNFile.class, "delete(path)", EMP, 1, STR, BLN),
  /** XQuery function */
  _FILE_READ_TEXT(FNFile.class, "read-text(path[,encoding])", STR, 1, STR, STR),
  /** XQuery function */
  _FILE_READ_TEXT_LINES(FNFile.class, "read-text-lines(path[,encoding])",
      STR_ZM, 1, STR, STR),
  /** XQuery function */
  _FILE_READ_BINARY(FNFile.class, "read-binary(path)", B64, STR),
  /** XQuery function */
  _FILE_WRITE(FNFile.class, "write(path,data[,params])", EMP, 2, STR, ITEM_ZM, NOD),
  /** XQuery function */
  _FILE_WRITE_BINARY(FNFile.class, "write-binary(path,item)", EMP, STR, ITEM_ZM),
  /** XQuery function */
  _FILE_APPEND(FNFile.class, "append(path,data[,params])", EMP, 2, STR, ITEM_ZM, NOD),
  /** XQuery function */
  _FILE_APPEND_BINARY(FNFile.class, "append-binary(path,item)", EMP, STR, ITEM_ZM),
  /** XQuery function */
  _FILE_COPY(FNFile.class, "copy(source,target)", EMP, STR, STR),
  /** XQuery function */
  _FILE_MOVE(FNFile.class, "move(source,target)", EMP, STR, STR),

  /* FNProc functions. */

  /** XQuery function. */
  _PROC_SYSTEM(FNProc.class, "system(command[,args[,encoding]])",
      STR, 1, STR, STR_ZM, STR),
  /** XQuery function. */
  _PROC_EXECUTE(FNProc.class, "execute(command[,args[,encoding]]])",
      ELM, 1, STR, STR_ZM, STR),

  /* FNClient functions. */

  /** XQuery function. */
  _CLIENT_CONNECT(FNClient.class, "connect(url,port,user,pass)", URI, STR, ITR, STR, STR),
  /** XQuery function. */
  _CLIENT_EXECUTE(FNClient.class, "execute(id,command)", STR, URI, STR),
  /** XQuery function. */
  _CLIENT_QUERY(FNClient.class, "query(id,query)", ITEM_ZO, URI, STR),
  /** XQuery function. */
  _CLIENT_CLOSE(FNClient.class, "close(id)", EMP, URI),

  /* FNSql functions. */

  /** XQuery function */
  _SQL_INIT(FNSql.class, "init(class)", EMP, 1, STR),
  /** XQuery function */
  _SQL_CONNECT(FNSql.class, "connect(url[,user[,pass[,options]]]]])",
      ITR, 1, STR, STR, STR, NOD_ZO),
  /** XQuery function */
  _SQL_PREPARE(FNSql.class, "prepare(id,statement)", ITR, ITR, STR),
  /** XQuery function */
  _SQL_EXECUTE(FNSql.class, "execute(id[,item])", ELM_ZM, 1, ITR, ITEM_ZO),
  /** XQuery function */
  _SQL_CLOSE(FNSql.class, "close(id)", EMP, ITR),
  /** XQuery function */
  _SQL_COMMIT(FNSql.class, "commit(id)", EMP, ITR),
  /** XQuery function */
  _SQL_ROLLBACK(FNSql.class, "rollback(id)", EMP, ITR),

  /* FNRepo functions. */

  /** XQuery function. */
  _REPO_INSTALL(FNRepo.class, "install(uri)", EMP, STR),
  /** XQuery function. */
  _REPO_DELETE(FNRepo.class, "delete(uri)", EMP, STR),
  /** XQuery function. */
  _REPO_LIST(FNRepo.class, "list()", STR_ZM),

  /* FNFt functions. */

  /** Full-text function: searches the full-text index. */
  _FT_SEARCH(FNFt.class, "search(node,terms[,options])", NOD_ZM, 2, NOD, ITEM_ZM, ITEM),
  /** Full-text function: counts the hits of a full-text request. */
  _FT_COUNT(FNFt.class, "count(nodes)", ITR, NOD_ZM),
  /** Full-text function: marks the hits of a full-text request. */
  _FT_MARK(FNFt.class, "mark(nodes[,tag])", NOD_ZM, 1, NOD_ZM, STR),
  /** Full-text function: extracts full-text results. */
  _FT_EXTRACT(FNFt.class, "extract(nodes[,tag[,length]])", NOD_ZM, 1, ITEM_ZM, STR, ITR),
  /** Full-text function: returns the full-text score. */
  _FT_SCORE(FNFt.class, "score(items)", DBL_ZM, ITEM_ZM),
  /** Full-text function: returns indexed tokens. */
  _FT_TOKENS(FNFt.class, "tokens(database,entry[,order])", ITEM_ZM, 1, STR, STR, BLN),
  /** Full-text function: tokenizes the specified string. */
  _FT_TOKENIZE(FNFt.class, "tokenize(string)", STR_ZM, STR),

  /* FNHof functions. */

  /** XQuery function. */
  _HOF_SORT_WITH(FNHof.class, "sort-with(lt-fun,seq)", ITEM_ZM,
      FuncType.get(BLN, ITEM, ITEM).seqType(), ITEM_ZM),
  /** XQuery function. */
  _HOF_ID(FNHof.class, "id(expr)", ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  _HOF_CONST(FNHof.class, "const(return,ignore)", ITEM_ZM, ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  _HOF_UNTIL(FNHof.class, "until(pred,func,start)", ITEM_ZM,
      FuncType.get(BLN, ITEM_ZM).seqType(),
      FuncType.get(ITEM_ZM, ITEM_ZM).seqType(), ITEM_ZM),
  /** XQuery function. */
  _HOF_FOLD_LEFT1(FNHof.class, "fold-left1(function,non-empty-seq)", ITEM_ZM,
      FuncType.get(ITEM_ZM, ITEM_ZM, ITEM).seqType(), ITEM_OM),
  /** XQuery Function. */
  _HOF_TOP_K_BY(FNHof.class, "top-k-by(seq,key-fun,k)", ITEM_ZM, ITEM_ZM,
      FuncType.arity(1).seqType(), ITR),
  /** XQuery function. */
  _HOF_TOP_K_WITH(FNHof.class, "top-k-with(seq,less-than,k)", ITEM_ZM, ITEM_ZM,
      FuncType.get(BLN, ITEM_ZO, ITEM_ZO).seqType(), ITR),

  /* FNCrypto functions (EXPath Cryptographic module). */

  /** Create message authentication code (HMAC). */
  _CRYPTO_HMAC(FNCrypto.class, "hmac(string,string,string[,string])",
      STR, 3, STR, STR, STR, STR_ZO),
  /** Encrypt message. */
  _CRYPTO_ENCRYPT(FNCrypto.class, "encrypt(string,string,string,string)",
      STR, STR, STR, STR, STR),
  /** Decrypt message. */
  _CRYPTO_DECRYPT(FNCrypto.class, "decrypt(string,string,string,string)",
      STR, STR, STR, STR, STR),
  /** Generate signature. */
  _CRYPTO_GENERATE_SIGNATURE(FNCrypto.class, "generate-signature" +
      "(node,string,string,string,string,string[,item][,item])",
      NOD, 6, NOD, STR, STR, STR, STR, STR, ITEM_ZO, ITEM_ZO),
  /** Validate signature. */
  _CRYPTO_VALIDATE_SIGNATURE(FNCrypto.class, "validate-signature(node)", BLN, NOD),

  /* FNHttp functions (EXPath). */

  /** XQuery function */
  _HTTP_SEND_REQUEST(FNHttp.class, "send-request(request[,href,[bodies]])",
      ITEM_ZM, 1, NOD, STR_ZO, ITEM_ZM),

  /* FNJson functions. */

  /** JSON function: convert JSON to XML. */
  _JSON_PARSE(FNJson.class, "parse(string)", NOD, STR),
  /** JSON function: convert JSON to XML. */
  _JSON_PARSE_ML(FNJson.class, "parse-ml(string)", NOD, STR),
  /** JSON function: convert XML to JSON. */
  _JSON_SERIALIZE(FNJson.class, "serialize(node)", STR, NOD),
  /** JSON function: convert XML to JsonML. */
  _JSON_SERIALIZE_ML(FNJson.class, "serialize-ml(node)", STR, NOD),

  /* FNUtil functions. */

  /** Utility function: evaluates the specified query. */
  _UTIL_EVAL(FNUtil.class, "eval(string)", ITEM_ZM, STR_ZO),
  /** Utility function: evaluates the specified query file. */
  _UTIL_RUN(FNUtil.class, "run(string)", ITEM_ZM, STR),
  /** Utility function: formats a string using the printf syntax. */
  _UTIL_FORMAT(FNUtil.class, "format(format,item1[,...])", STR, -2, STR, ITEM),
  /** Utility function: dumps the memory consumption. */
  _UTIL_MEM(FNUtil.class, "mem(expr[,cache[,label]])", ITEM_ZM, 1, ITEM_ZM, BLN, STR),
  /** Utility function: dumps the execution time of an expression. */
  _UTIL_TIME(FNUtil.class, "time(expr[,cache[,label]])", ITEM_ZM, 1, ITEM_ZM, BLN, STR),
  /** Utility function: sleeps for the specified number of milliseconds. */
  _UTIL_SLEEP(FNUtil.class, "sleep(ms)", EMP, ITR),
  /** Utility function: calculates the MD5 hash of the given string. */
  _UTIL_MD5(FNUtil.class, "md5(str)", HEX, STR),
  /** Utility function: calculates the SHA1 hash of the given string. */
  _UTIL_SHA1(FNUtil.class, "sha1(str)", HEX, STR),
  /** Utility function: calculates the CRC32 hash of the given string. */
  _UTIL_CRC32(FNUtil.class, "crc32(str)", HEX, STR),
  /** Utility function: returns a random unique id. */
  _UTIL_UUID(FNUtil.class, "uuid()", STR),
  /** Utility function: compares items in depth and offers some more options. */
  _UTIL_DEEP_EQUAL(FNUtil.class, "deep-equal(item,item[,options])",
      BLN, 2, ITEM_ZM, ITEM_ZM, ITEM),
  /** Utility function: returns the path to the query file. */
  _UTIL_PATH(FNUtil.class, "path()", STR),
  /** Utility function: passes type information to {@code fn:trace()}. */
  _UTIL_TYPE(FNUtil.class, "type(expr)", ITEM_ZM, ITEM_ZM),

  /* FNValidate functions. */

  /** Validate Query function */
  _VALIDATE_XSD(FNValidate.class, "xsd(input[,schema])", EMP, 1, ITEM, ITEM),
  /** Validate function */
  _VALIDATE_DTD(FNValidate.class, "dtd(input[,schema])", EMP, 1, ITEM, ITEM),

  /* FNXslt functions. */

  /** XSLT function: performs an XSLT transformation. */
  _UTIL_TRANSFORM(FNXslt.class, "transform(input,stylesheet[,params])",
      NOD, 2, ITEM, ITEM, NOD_ZO),

  /* FNZip functions (EXPath). */

  /** XQuery function */
  _ZIP_BINARY_ENTRY(FNZip.class, "binary-entry(path,entry)", B64, STR, STR),
  /** XQuery function */
  _ZIP_TEXT_ENTRY(FNZip.class, "text-entry(path,entry[,encoding])",
      STR, 2, STR, STR, STR),
  /** XQuery function */
  _ZIP_HTML_ENTRY(FNZip.class, "html-entry(path,entry)", NOD, STR, STR),
  /** XQuery function */
  _ZIP_XML_ENTRY(FNZip.class, "xml-entry(path,entry)", NOD, STR, STR),
  /** XQuery function */
  _ZIP_ENTRIES(FNZip.class, "entries(path)", ELM, STR),
  /** XQuery function */
  _ZIP_ZIP_FILE(FNZip.class, "zip-file(zip)", EMP, ELM),
  /** XQuery function */
  _ZIP_UPDATE_ENTRIES(FNZip.class, "update-entries(zip,output)", EMP, ELM, STR);

  /** Updating functions. */
  static final Function[] UPDATING = {
    PUT, _DB_ADD, _DB_DELETE, _DB_RENAME, _DB_REPLACE, _DB_OPTIMIZE, _DB_STORE, _DB_OUTPUT
  };

  /**
   * Mapping between function classes and namespace URIs.
   * If no mapping exists, {@link #FNURI} will be assumed as default mapping.
   */
  public static final HashMap<Class<? extends StandardFunc>, byte[]> URIS =
    new HashMap<Class<? extends StandardFunc>, byte[]>();

  // initialization of class/uri mappings and statically known modules
  static {
    // W3 functions
    URIS.put(FNMap.class,  MAPURI);
    URIS.put(FNMath.class, MATHURI);
    // EXPath functions
    URIS.put(FNCrypto.class, CRYPTOURI);
    URIS.put(FNFile.class,   FILEURI);
    URIS.put(FNHttp.class,   HTTPURI);
    URIS.put(FNZip.class,    ZIPURI);
    URIS.put(FNRepo.class,   REPOURI);
    // internal functions
    URIS.put(FNArchive.class,  ARCHIVEURI);
    URIS.put(FNClient.class,   CLIENTURI);
    URIS.put(FNConvert.class,  CONVERTURI);
    URIS.put(FNDb.class,       DBURI);
    URIS.put(FNFt.class,       FTURI);
    URIS.put(FNHof.class,      HOFURI);
    URIS.put(FNIndex.class,    INDEXURI);
    URIS.put(FNJson.class,     JSONURI);
    URIS.put(FNProc.class,     PROCURI);
    URIS.put(FNSql.class,      SQLURI);
    URIS.put(FNUtil.class,     UTILURI);
    URIS.put(FNValidate.class, VALIDATEURI);
    URIS.put(FNXslt.class,     XSLTURI);
  }

  /** Minimum number of arguments. */
  public final int min;
  /** Maximum number of arguments. */
  public final int max;
  /** Argument types. */
  public final SeqType[] args;

  /** Descriptions. */
  final String desc;
  /** Return type. */
  final SeqType ret;

  /** Function classes. */
  private final Class<? extends StandardFunc> func;

  /**
   * Default constructor.
   * @param fun function class
   * @param dsc description
   * @param r return type
   * @param typ arguments types
   */
  Function(final Class<? extends StandardFunc> fun, final String dsc,
      final SeqType r, final SeqType... typ) {
    this(fun, dsc, r, typ.length, typ);
  }

  /**
   * Full constructor for functions with multiple signatures.
   * @param fun function class
   * @param dsc description
   * @param r return type
   * @param m minimum number of arguments; if the value is negative,
   *   the maximum number of arguments is variable
   * @param typ arguments types
   */
  Function(final Class<? extends StandardFunc> fun, final String dsc,
      final SeqType r, final int m, final SeqType... typ) {

    func = fun;
    desc = dsc;
    ret = r;
    min = m < 0 ? -m : m;
    max = m < 0 ? Integer.MAX_VALUE : typ.length;
    args = typ;
  }

  /**
   * Creates a new instance of the function.
   * @param arg arguments
   * @return function
   */
  public StandardFunc get(final Expr... arg) {
    return get(null, arg);
  }

  /**
   * Creates a new instance of the function.
   * @param ii input info
   * @param arg arguments
   * @return function
   */
  public StandardFunc get(final InputInfo ii, final Expr... arg) {
    return (StandardFunc) Reflect.get(Reflect.find(
        func, InputInfo.class, Function.class, Expr[].class), ii, this, arg);
  }

  /**
   * Returns the namespace URI of this function.
   * @return function
   */
  final byte[] uri() {
    final byte[] u = URIS.get(func);
    return u == null ? FNURI : u;
  }

  /**
   * Returns the function type of this function with the given arity.
   * @param arity number of arguments
   * @return function type
   */
  final FuncType type(final int arity) {
    final SeqType[] arg = new SeqType[arity];
    if(arity != 0 && max == Integer.MAX_VALUE) {
      System.arraycopy(args, 0, arg, 0, args.length);
      final SeqType var = args[args.length - 1];
      for(int i = args.length; i < arg.length; i++) arg[i] = var;
    } else {
      System.arraycopy(args, 0, arg, 0, arity);
    }
    return FuncType.get(ret, arg);
  }

  /**
   * Returns a string representation of the function with the specified
   * arguments. All objects are wrapped with quotes,
   * except for the following ones:
   * <ul>
   * <li>integers</li>
   * <li>booleans (which will be suffixed with parentheses)</li>
   * <li>strings starting with an optional NCName and opening parenthesis</li>
   * <li>strings starting with angle bracket, quote, dollar sign, or space</li>
   * </ul>
   * @param arg arguments
   * @return string representation
   */
  public final String args(final Object... arg) {
    final TokenBuilder tb = new TokenBuilder();
    for(final Object a : arg) {
      if(!tb.isEmpty()) tb.add(',');
      final String s = a.toString();
      if(s.matches("^([-\\w_:\\.]*\\(|<|\"|\\$| ).*") ||
          a instanceof Integer) {
        tb.add(s);
      } else if(a instanceof Boolean) {
        tb.add(s + "()");
      } else {
        tb.add('"' + s.replaceAll("\"", "\"\"") + '"');
      }
    }
    return toString().replaceAll("\\(.*", "(") + tb + ')';
  }

  @Override
  public final String toString() {
    final byte[] pref = NSGlobal.prefix(uri());
    return new TokenBuilder(pref).add(':').add(desc).toString();
  }
}
