package org.basex.query.func;

import static org.basex.query.expr.Expr.Flag.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.value.type.SeqType.*;

import static org.basex.query.value.type.SeqType.BIN;

import java.util.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr.Flag;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Definitions of all built-in XQuery functions.
 * Namespace mappings for function prefixes and URIs are specified in the
 * static code in the {@code NSGlobal} class.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public enum Function {

  /* FNAcc functions. */

  /** XQuery function. */
  POSITION(FNAcc.class, "position()", arg(), ITR, flag(FCS)),
  /** XQuery function. */
  LAST(FNAcc.class, "last()", arg(), ITR, flag(FCS)),
  /** XQuery function. */
  STRING(FNAcc.class, "string([item])", arg(ITEM_ZO), STR),
  /** XQuery function. */
  NUMBER(FNAcc.class, "number([item])", arg(AAT_ZO), DBL),
  /** XQuery function. */
  STRING_LENGTH(FNAcc.class, "string-length([item])", arg(STR_ZO), ITR),
  /** XQuery function. */
  NORMALIZE_SPACE(FNAcc.class, "normalize-space([string])", arg(STR_ZO), STR),
  /** XQuery function. */
  NAMESPACE_URI_FROM_QNAME(FNAcc.class, "namespace-uri-from-QName(qname)", arg(QNM_ZO), URI_ZO),

  /* FNAggr functions. */

  /** XQuery function. */
  AVG(FNAggr.class, "avg(item)", arg(AAT_ZM), AAT_ZO),
  /** XQuery function. */
  COUNT(FNAggr.class, "count(item)", arg(ITEM_ZM), ITR),
  /** XQuery function. */
  MAX(FNAggr.class, "max(item[,coll])", arg(AAT_ZM, STR), AAT_ZO),
  /** XQuery function. */
  MIN(FNAggr.class, "min(item[,coll])", arg(AAT_ZM, STR), AAT_ZO),
  /** XQuery function. */
  SUM(FNAggr.class, "sum(item[,zero])", arg(AAT_ZM, AAT_ZO), AAT_ZO),

  /* FNContext functions. */

  /** XQuery function. */
  CURRENT_DATE(FNContext.class, "current-date()", arg(), DAT),
  /** XQuery function. */
  CURRENT_DATETIME(FNContext.class, "current-dateTime()", arg(), DTM),
  /** XQuery function. */
  CURRENT_TIME(FNContext.class, "current-time()", arg(), TIM),
  /** XQuery function. */
  IMPLICIT_TIMEZONE(FNContext.class, "implicit-timezone()", arg(), DTD),
  /** XQuery function. */
  DEFAULT_COLLATION(FNContext.class, "default-collation()", arg(), STR),
  /** XQuery function. */
  STATIC_BASE_URI(FNContext.class, "static-base-uri()", arg(), URI_ZO),

  /* FNDate functions. */

  /** XQuery function. */
  DAY_FROM_DATE(FNDate.class, "day-from-date(item)", arg(DAT_ZO), ITR_ZO),
  /** XQuery function. */
  DAY_FROM_DATETIME(FNDate.class, "day-from-dateTime(datetime)", arg(DTM_ZO), ITR_ZO),
  /** XQuery function. */
  DAYS_FROM_DURATION(FNDate.class, "days-from-duration(duration)", arg(DUR_ZO), ITR_ZO),
  /** XQuery function. */
  HOURS_FROM_DATETIME(FNDate.class, "hours-from-dateTime(datetime)", arg(DTM_ZO), ITR_ZO),
  /** XQuery function. */
  HOURS_FROM_DURATION(FNDate.class, "hours-from-duration(duration)", arg(DUR_ZO), ITR_ZO),
  /** XQuery function. */
  HOURS_FROM_TIME(FNDate.class, "hours-from-time(item)", arg(TIM_ZO), ITR_ZO),
  /** XQuery function. */
  MINUTES_FROM_DATETIME(FNDate.class, "minutes-from-dateTime(datetime)", arg(DTM_ZO), ITR_ZO),
  /** XQuery function. */
  MINUTES_FROM_DURATION(FNDate.class, "minutes-from-duration(duration)", arg(DUR_ZO), ITR_ZO),
  /** XQuery function. */
  MINUTES_FROM_TIME(FNDate.class, "minutes-from-time(item)", arg(TIM_ZO), ITR_ZO),
  /** XQuery function. */
  MONTH_FROM_DATE(FNDate.class, "month-from-date(item)", arg(DAT_ZO), ITR_ZO),
  /** XQuery function. */
  MONTH_FROM_DATETIME(FNDate.class, "month-from-dateTime(datetime)", arg(DTM_ZO), ITR_ZO),
  /** XQuery function. */
  MONTHS_FROM_DURATION(FNDate.class, "months-from-duration(duration)", arg(DUR_ZO), ITR_ZO),
  /** XQuery function. */
  SECONDS_FROM_DATETIME(FNDate.class, "seconds-from-dateTime(datetime)", arg(DTM_ZO), DEC_ZO),
  /** XQuery function. */
  SECONDS_FROM_DURATION(FNDate.class, "seconds-from-duration(duration)", arg(DUR_ZO), DEC_ZO),
  /** XQuery function. */
  SECONDS_FROM_TIME(FNDate.class, "seconds-from-time(item)", arg(TIM_ZO), DEC_ZO),
  /** XQuery function. */
  TIMEZONE_FROM_DATE(FNDate.class, "timezone-from-date(item)", arg(DAT_ZO), DTD_ZO),
  /** XQuery function. */
  TIMEZONE_FROM_DATETIME(FNDate.class, "timezone-from-dateTime(item)", arg(DTM_ZO), DTD_ZO),
  /** XQuery function. */
  TIMEZONE_FROM_TIME(FNDate.class, "timezone-from-time(item)", arg(TIM_ZO), DTD_ZO),
  /** XQuery function. */
  YEAR_FROM_DATE(FNDate.class, "year-from-date(item)", arg(DAT_ZO), ITR_ZO),
  /** XQuery function. */
  YEAR_FROM_DATETIME(FNDate.class, "year-from-dateTime(datetime)", arg(DTM_ZO), ITR_ZO),
  /** XQuery function. */
  YEARS_FROM_DURATION(FNDate.class, "years-from-duration(duration)", arg(DUR_ZO), ITR_ZO),
  /** XQuery function. */
  ADJUST_DATE_TO_TIMEZONE(FNDate.class, "adjust-date-to-timezone(date[,zone])",
      arg(DAT_ZO, DTD_ZO), DAT_ZO),
  /** XQuery function. */
  ADJUST_DATETIME_TO_TIMEZONE(FNDate.class, "adjust-dateTime-to-timezone(date[,zone])",
      arg(DTM_ZO, DTD_ZO), DTM_ZO),
  /** XQuery function. */
  ADJUST_TIME_TO_TIMEZONE(FNDate.class, "adjust-time-to-timezone(date[,zone])",
      arg(TIM_ZO, DTD_ZO), TIM_ZO),
  /** XQuery function. */
  DATETIME(FNDate.class, "dateTime(date,time)", arg(DAT_ZO, TIM_ZO), DTM_ZO),

  /* FNFormat functions. */

  /** XQuery function. */
  FORMAT_INTEGER(FNFormat.class, "format-integer(number,picture[,language])",
      arg(ITR_ZO, STR, STR), STR, flag(X30)),
  /** XQuery function. */
  FORMAT_NUMBER(FNFormat.class, "format-number(number,picture[,format])",
      arg(ITR_ZO, STR, STR_ZO), STR, flag(X30)),
  /** XQuery function. */
  FORMAT_DATETIME(FNFormat.class,
      "format-dateTime(number,picture,[language,calendar,place])",
      arg(DTM_ZO, STR, STR_ZO, STR_ZO, STR_ZO), STR_ZO, flag(X30)),
  /** XQuery function. */
  FORMAT_DATE(FNFormat.class, "format-date(date,picture,[language,calendar,place])",
      arg(DAT_ZO, STR, STR_ZO, STR_ZO, STR_ZO), STR_ZO, flag(X30)),
  /** XQuery function. */
  FORMAT_TIME(FNFormat.class, "format-time(number,picture,[language,calendar,place])",
      arg(TIM_ZO, STR, STR_ZO, STR_ZO, STR_ZO), STR_ZO, flag(X30)),

  /* FNFunc functions. */

  /** XQuery function. */
  FILTER(FNFunc.class, "filter(seq,function)",
      arg(ITEM_ZM, FuncType.get(BLN, ITEM).seqType()), ITEM_ZM, flag(X30, Flag.HOF)),
  /** XQuery function. */
  FUNCTION_NAME(FNFunc.class, "function-name(function)", arg(FUN_O), QNM_ZO, flag(X30)),
  /** XQuery function. */
  FUNCTION_ARITY(FNFunc.class, "function-arity(function)", arg(FUN_O), ITR, flag(X30)),
  /** XQuery function. */
  FUNCTION_LOOKUP(FNFunc.class, "function-lookup(name,arity)",
      arg(QNM, ITR), FUN_OZ, flag(CTX, FCS, NDT, X30, Flag.HOF)),
  /** XQuery function. */
  FOR_EACH(FNFunc.class, "for-each(seq,function)",
      arg(ITEM_ZM, FuncType.get(ITEM_ZM, ITEM).seqType()), ITEM_ZM, flag(X30, Flag.HOF)),
  /** XQuery function. */
  FOR_EACH_PAIR(FNFunc.class, "for-each-pair(seq1,seq2,function)",
      arg(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM, ITEM).seqType()), ITEM_ZM,
      flag(X30, Flag.HOF)),
  /** XQuery function. */
  FOLD_LEFT(FNFunc.class, "fold-left(seq,zero,function)",
      arg(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM).seqType()), ITEM_ZM,
      flag(X30, Flag.HOF)),
  /** XQuery function. */
  FOLD_RIGHT(FNFunc.class, "fold-right(seq,zero,function)",
      arg(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM, ITEM_ZM).seqType()), ITEM_ZM,
      flag(X30, Flag.HOF)),

  /* FNGen functions. */

  /** XQuery function. */
  DATA(FNGen.class, "data([item])", arg(ITEM_ZM), AAT_ZM),
  /** XQuery function. */
  COLLECTION(FNGen.class, "collection([uri])", arg(STR_ZO), NOD_ZM),
  /** XQuery function. */
  DOC(FNGen.class, "doc(uri)", arg(STR_ZO), DOC_ZO),
  /** XQuery function. */
  DOC_AVAILABLE(FNGen.class, "doc-available(uri)", arg(STR_ZO), BLN),
  /** XQuery function. */
  PUT(FNGen.class, "put(node,uri)", arg(NOD, STR_ZO), EMP, flag(UPD, NDT)),
  /** XQuery function. */
  UNPARSED_TEXT(FNGen.class, "unparsed-text(uri[,encoding])", arg(STR_ZO, STR), STR_ZO, flag(X30)),
  /** XQuery function. */
  UNPARSED_TEXT_LINES(FNGen.class, "unparsed-text-lines(uri[,encoding])",
      arg(STR_ZO, STR), STR_ZM, flag(X30)),
  /** XQuery function. */
  UNPARSED_TEXT_AVAILABLE(FNGen.class, "unparsed-text-available(uri[,encoding])",
      arg(STR_ZO, STR), BLN, flag(X30)),
  /** XQuery function. */
  PARSE_XML(FNGen.class, "parse-xml(string)", arg(STR_ZO), DOC_O, flag(CNS, X30)),
  /** XQuery function. */
  PARSE_XML_FRAGMENT(FNGen.class, "parse-xml-fragment(string)", arg(STR_ZO), DOC_ZO),
  /** XQuery function. */
  URI_COLLECTION(FNGen.class, "uri-collection([uri])", arg(STR_ZO), URI_ZM, flag(X30)),
  /** XQuery function. */
  SERIALIZE(FNGen.class, "serialize(items[,params])", arg(ITEM_ZM, ITEM_ZO), STR, flag(X30)),

  /* FNId functions. */

  /** XQuery function. */
  ID(FNId.class, "id(string[,item])", arg(STR_ZM, NOD), ELM_ZM),
  /** XQuery function. */
  IDREF(FNId.class, "idref(string[,item])", arg(STR_ZM, NOD), NOD_ZM),
  /** XQuery function. */
  LANG(FNId.class, "lang(string[,item])", arg(STR_ZO, NOD), BLN),
  /** XQuery function. */
  ELEMENT_WITH_ID(FNId.class, "element-with-id(string[,item])",
      arg(STR_ZM, NOD), ELM_ZM, flag(X30)),

  /* FNInfo functions. */

  /** XQuery function. */
  ERROR(FNInfo.class, "error([code[,desc[,object]]])", arg(QNM_ZO, STR, ITEM_ZM), EMP, flag(NDT)),
  /** XQuery function. */
  TRACE(FNInfo.class, "trace(value,label)", arg(ITEM_ZM, STR), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  ENVIRONMENT_VARIABLE(FNInfo.class, "environment-variable(string)", arg(STR), STR_ZO, flag(X30)),
  /** XQuery function. */
  AVAILABLE_ENVIRONMENT_VARIABLES(FNInfo.class, "available-environment-variables()",
      arg(), STR_ZM, flag(X30)),

  /* FNNode functions. */

  /** XQuery function. */
  DOCUMENT_URI(FNNode.class, "document-uri([node])", arg(NOD_ZO), URI_ZO),
  /** XQuery function. */
  NILLED(FNNode.class, "nilled([node])", arg(NOD_ZO), BLN_ZO),
  /** XQuery function. */
  NODE_NAME(FNNode.class, "node-name([node])", arg(NOD_ZO), QNM_ZO),
  /** XQuery function. */
  LOCAL_NAME(FNNode.class, "local-name([node])", arg(NOD_ZO), STR),
  /** XQuery function. */
  NAME(FNNode.class, "name([node])", arg(NOD_ZO), STR),
  /** XQuery function. */
  NAMESPACE_URI(FNNode.class, "namespace-uri([node])", arg(NOD_ZO), URI),
  /** XQuery function. */
  ROOT(FNNode.class, "root([node])", arg(NOD_ZO), NOD_ZO),
  /** XQuery function. */
  BASE_URI(FNNode.class, "base-uri([node])", arg(NOD_ZO), URI_ZO),
  /** XQuery function. */
  GENERATE_ID(FNNode.class, "generate-id([node])", arg(NOD_ZO), STR, flag(X30)),
  /** XQuery function. */
  HAS_CHILDREN(FNNode.class, "has-children([node])", arg(NOD_ZM), BLN, flag(X30)),
  /** XQuery function. */
  PATH(FNNode.class, "path([node])", arg(NOD_ZO), STR_ZO, flag(X30)),

  /* FNNum functions. */

  /** XQuery function. */
  ABS(FNNum.class, "abs(num)", arg(AAT_ZO), AAT_ZO),
  /** XQuery function. */
  CEILING(FNNum.class, "ceiling(num)", arg(AAT_ZO), AAT_ZO),
  /** XQuery function. */
  FLOOR(FNNum.class, "floor(num)", arg(AAT_ZO), AAT_ZO),
  /** XQuery function. */
  ROUND(FNNum.class, "round(num[,prec])", arg(AAT_ZO, ITR), AAT_ZO),
  /** XQuery function. */
  ROUND_HALF_TO_EVEN(FNNum.class, "round-half-to-even(num[,prec])",
      arg(AAT_ZO, ITR), AAT_ZO),

  /* FNPat functions. */

  /** XQuery function. */
  MATCHES(FNPat.class, "matches(item,pattern[,mod])", arg(STR_ZO, STR, STR), BLN),
  /** XQuery function. */
  REPLACE(FNPat.class, "replace(item,pattern,replace[,mod])", arg(STR_ZO, STR, STR, STR), STR),
  /** XQuery function. */
  TOKENIZE(FNPat.class, "tokenize(item,pattern[,mod])", arg(STR_ZO, STR, STR), STR_ZM),
  /** XQuery function. */
  ANALYZE_STRING(FNPat.class, "analyze-string(input,pattern[,mod])",
      arg(STR_ZO, STR, STR), ELM, flag(X30, CNS)),

  /* FNQName functions. */

  /** XQuery function. */
  IN_SCOPE_PREFIXES(FNQName.class, "in-scope-prefixes(elem)", arg(ELM), STR_ZM),
  /** XQuery function. */
  LOCAL_NAME_FROM_QNAME(FNQName.class, "local-name-from-QName(qname)", arg(QNM_ZO), NCN_ZO),
  /** XQuery function. */
  NAMESPACE_URI_FOR_PREFIX(FNQName.class, "namespace-uri-for-prefix(pref,elem)",
      arg(STR_ZO, ELM), URI_ZO),
  /** XQuery function. */
  QNAME(FNQName.class, "QName(uri,name)", arg(STR_ZO, STR), QNM),
  /** XQuery function. */
  PREFIX_FROM_QNAME(FNQName.class, "prefix-from-QName(qname)", arg(QNM_ZO), NCN_ZO),
  /** XQuery function. */
  RESOLVE_QNAME(FNQName.class, "resolve-QName(item,base)", arg(STR_ZO, ELM), QNM_ZO),
  /** XQuery function. */
  RESOLVE_URI(FNQName.class, "resolve-uri(name[,elem])", arg(STR_ZO, STR), URI_ZO),

  /* FNSeq functions. */

  /** XQuery function. */
  DISTINCT_VALUES(FNSeq.class, "distinct-values(items[,coll])", arg(AAT_ZM, STR), AAT_ZM),
  /** XQuery function. */
  INDEX_OF(FNSeq.class, "index-of(items,item[,coll])", arg(AAT_ZM, AAT, STR), ITR_ZM),
  /** XQuery function. */
  INSERT_BEFORE(FNSeq.class, "insert-before(items,pos,insert)",
      arg(ITEM_ZM, ITR, ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  REMOVE(FNSeq.class, "remove(items,pos)", arg(ITEM_ZM, ITR), ITEM_ZM),
  /** XQuery function. */
  REVERSE(FNSeq.class, "reverse(items)", arg(ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  SUBSEQUENCE(FNSeq.class, "subsequence(items,start[,len])", arg(ITEM_ZM, DBL, DBL), ITEM_ZM),
  /** XQuery function. */
  HEAD(FNSeq.class, "head(items)", arg(ITEM_ZM), ITEM_ZO, flag(X30)),
  /** XQuery function. */
  TAIL(FNSeq.class, "tail(items)", arg(ITEM_ZM), ITEM_ZM, flag(X30)),
  /** XQuery function. */
  OUTERMOST(FNSeq.class, "outermost(nodes)", arg(NOD_ZM), NOD_ZM),
  /** XQuery function. */
  INNERMOST(FNSeq.class, "innermost(nodes)", arg(NOD_ZM), NOD_ZM),

  /* FNSimple functions. */

  /** XQuery function. */
  FALSE(FNSimple.class, "false()", arg(), BLN),
  /** XQuery function. */
  TRUE(FNSimple.class, "true()", arg(), BLN),
  /** XQuery function. */
  BOOLEAN(FNSimple.class, "boolean(item)", arg(ITEM_ZM), BLN),
  /** XQuery function. */
  NOT(FNSimple.class, "not(item)", arg(ITEM_ZM), BLN),
  /** XQuery function. */
  EMPTY(FNSimple.class, "empty(item)", arg(ITEM_ZM), BLN),
  /** XQuery function. */
  EXISTS(FNSimple.class, "exists(item)", arg(ITEM_ZM), BLN),
  /** XQuery function. */
  UNORDERED(FNSimple.class, "unordered(item)", arg(ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  ZERO_OR_ONE(FNSimple.class, "zero-or-one(item)", arg(ITEM_ZM), ITEM_ZO),
  /** XQuery function. */
  EXACTLY_ONE(FNSimple.class, "exactly-one(item)", arg(ITEM_ZM), ITEM),
  /** XQuery function. */
  ONE_OR_MORE(FNSimple.class, "one-or-more(item)", arg(ITEM_ZM), ITEM_OM),
  /** XQuery function. */
  DEEP_EQUAL(FNSimple.class, "deep-equal(seq1,seq2[,coll])", arg(ITEM_ZM, ITEM_ZM, STR), BLN),
  /** XQuery function (project specific). */
  DEEP_EQUAL_OPT(FNSimple.class, "deep-equal-opt(seq1,seq2[,options])",
      arg(ITEM_ZM, ITEM_ZM, ITEM), BLN),

  /* FNStr functions. */

  /** XQuery function. */
  CODEPOINT_EQUAL(FNStr.class, "codepoint-equal(string1,string2)", arg(STR_ZO, STR_ZO), BLN_ZO),
  /** XQuery function. */
  CODEPOINTS_TO_STRING(FNStr.class, "codepoints-to-string(nums)", arg(ITR_ZM), STR),
  /** XQuery function. */
  COMPARE(FNStr.class, "compare(first,second[,coll])", arg(STR_ZO, STR_ZO, STR), ITR_ZO),
  /** XQuery function. */
  CONCAT(FNStr.class, "concat(atom1,atom2[,...])", arg(AAT_ZO, AAT_ZO), STR),
  /** XQuery function. */
  CONTAINS(FNStr.class, "contains(string,sub[,coll])", arg(STR_ZO, STR_ZO, STR), BLN),
  /** XQuery function. */
  ENCODE_FOR_URI(FNStr.class, "encode-for-uri(string)", arg(STR_ZO), STR),
  /** XQuery function. */
  ENDS_WITH(FNStr.class, "ends-with(string,sub[,coll])", arg(STR_ZO, STR_ZO, STR), BLN),
  /** XQuery function. */
  ESCAPE_HTML_URI(FNStr.class, "escape-html-uri(string)", arg(STR_ZO), STR),
  /** XQuery function. */
  IRI_TO_URI(FNStr.class, "iri-to-uri(string)", arg(STR_ZO), STR),
  /** XQuery function. */
  LOWER_CASE(FNStr.class, "lower-case(string)", arg(STR_ZO), STR),
  /** XQuery function. */
  NORMALIZE_UNICODE(FNStr.class, "normalize-unicode(string[,form])", arg(STR_ZO, STR), STR),
  /** XQuery function. */
  STARTS_WITH(FNStr.class, "starts-with(string,sub[,coll])", arg(STR_ZO, STR_ZO, STR), BLN),
  /** XQuery function. */
  STRING_JOIN(FNStr.class, "string-join(strings[,sep])", arg(STR_ZM, STR), STR),
  /** XQuery function. */
  STRING_TO_CODEPOINTS(FNStr.class, "string-to-codepoints(string)", arg(STR_ZO), ITR_ZM),
  /** XQuery function. */
  SUBSTRING(FNStr.class, "substring(string,start[,len])", arg(STR_ZO, DBL, DBL), STR),
  /** XQuery function. */
  SUBSTRING_AFTER(FNStr.class, "substring-after(string,sub[,coll])", arg(STR_ZO, STR_ZO, STR), STR),
  /** XQuery function. */
  SUBSTRING_BEFORE(FNStr.class, "substring-before(string,sub[,coll])",
      arg(STR_ZO, STR_ZO, STR), STR),
  /** XQuery function. */
  TRANSLATE(FNStr.class, "translate(string,map,trans)", arg(STR_ZO, STR, STR), STR),
  /** XQuery function. */
  UPPER_CASE(FNStr.class, "upper-case(string)", arg(STR_ZO), STR),

  /* FNMap functions. */

  /** XQuery function. */
  _MAP_NEW(FNMap.class, "new([maps[,coll]])", arg(MAP_ZM, STR), MAP_O, flag(X30)),
  /** XQuery function. */
  _MAP_ENTRY(FNMap.class, "entry(key,value)", arg(AAT, ITEM_ZM), MAP_O, flag(X30)),
  /** XQuery function. */
  _MAP_GET(FNMap.class, "get(map,key)", arg(MAP_O, AAT), ITEM_ZM, flag(X30)),
  /** XQuery function. */
  _MAP_CONTAINS(FNMap.class, "contains(map,key)", arg(MAP_O, AAT), BLN, flag(X30)),
  /** XQuery function. */
  _MAP_REMOVE(FNMap.class, "remove(map,key)", arg(MAP_O, AAT), MAP_O, flag(X30)),
  /** XQuery function. */
  _MAP_SIZE(FNMap.class, "size(map)", arg(MAP_O), ITR, flag(X30)),
  /** XQuery function. */
  _MAP_KEYS(FNMap.class, "keys(map)", arg(MAP_O), AAT_ZM, flag(X30)),
  /** XQuery function. */
  _MAP_COLLATION(FNMap.class, "collation(map)", arg(MAP_O), STR, flag(X30)),
  /** XQuery function. */
  _MAP_SERIALIZE(FNMap.class, "serialize(map)", arg(MAP_O), STR, flag(X30)),

  /* FNMath functions. */

  /** XQuery function. */
  _MATH_PI(FNMath.class, "pi()", arg(), DBL, flag(X30)),
  /** XQuery function. */
  _MATH_SQRT(FNMath.class, "sqrt(number)", arg(DBL_ZO), DBL_ZO, flag(X30)),
  /** XQuery function. */
  _MATH_SIN(FNMath.class, "sin(number)", arg(DBL_ZO), DBL_ZO, flag(X30)),
  /** XQuery function. */
  _MATH_COS(FNMath.class, "cos(number)", arg(DBL_ZO), DBL_ZO, flag(X30)),
  /** XQuery function. */
  _MATH_TAN(FNMath.class, "tan(number)", arg(DBL_ZO), DBL_ZO, flag(X30)),
  /** XQuery function. */
  _MATH_ASIN(FNMath.class, "asin(number)", arg(DBL_ZO), DBL_ZO, flag(X30)),
  /** XQuery function. */
  _MATH_ACOS(FNMath.class, "acos(number)", arg(DBL_ZO), DBL_ZO, flag(X30)),
  /** XQuery function. */
  _MATH_ATAN(FNMath.class, "atan(number)", arg(DBL_ZO), DBL_ZO, flag(X30)),
  /** XQuery function. */
  _MATH_ATAN2(FNMath.class, "atan2(number1,number2)", arg(DBL, DBL), DBL, flag(X30)),
  /** XQuery function. */
  _MATH_POW(FNMath.class, "pow(number1,number2)", arg(DBL_ZO, ITR), DBL_ZO, flag(X30)),
  /** XQuery function. */
  _MATH_EXP(FNMath.class, "exp(number)", arg(DBL_ZO), DBL_ZO, flag(X30)),
  /** XQuery function. */
  _MATH_EXP10(FNMath.class, "exp10(number)", arg(DBL_ZO), DBL_ZO, flag(X30)),
  /** XQuery function. */
  _MATH_LOG(FNMath.class, "log(number)", arg(DBL_ZO), DBL_ZO, flag(X30)),
  /** XQuery function. */
  _MATH_LOG10(FNMath.class, "log10(number)", arg(DBL_ZO), DBL_ZO, flag(X30)),

  /** XQuery function. */
  _MATH_E(FNMath.class, "e()", arg(), DBL),
  /** XQuery function. */
  _MATH_SINH(FNMath.class, "sinh(number)", arg(DBL_ZO), DBL_ZO),
  /** XQuery function. */
  _MATH_COSH(FNMath.class, "cosh(number)", arg(DBL_ZO), DBL_ZO),
  /** XQuery function. */
  _MATH_TANH(FNMath.class, "tanh(number)", arg(DBL_ZO), DBL_ZO),
  /** XQuery function. */
  _MATH_CRC32(FNMath.class, "crc32(string)", arg(STR), HEX),

  /* FNAdmin functions. */

  /** XQuery function. */
  _ADMIN_USERS(FNAdmin.class, "users([database])", arg(STR), ELM_ZM),
  /** XQuery function. */
  _ADMIN_SESSIONS(FNAdmin.class, "sessions()", arg(), ELM_ZM),
  /** XQuery function. */
  _ADMIN_LOGS(FNAdmin.class, "logs([date[,merge]])", arg(STR, BLN), ELM_ZM),

  /* FNArchive functions. */

  /** XQuery function. */
  _ARCHIVE_CREATE(FNArchive.class, "create(entries,contents[,options])",
      arg(ITEM_ZM, ITEM_ZM, ITEM), B64),
  /** XQuery function. */
  _ARCHIVE_ENTRIES(FNArchive.class, "entries(archive)", arg(B64), ELM_ZM),
  /** XQuery function. */
  _ARCHIVE_EXTRACT_TEXT(FNArchive.class, "extract-text(archive[,entries[,encoding]])",
      arg(B64, ITEM_ZM, STR), STR_ZM),
  /** XQuery function. */
  _ARCHIVE_EXTRACT_BINARY(FNArchive.class, "extract-binary(archive[,entries])",
      arg(B64, ITEM_ZM), B64_ZM),
  /** XQuery function. */
  _ARCHIVE_UPDATE(FNArchive.class, "update(archive,entries,contents)",
      arg(B64, ITEM_ZM, ITEM_ZM), B64),
  /** XQuery function. */
  _ARCHIVE_DELETE(FNArchive.class, "delete(archive,entries)", arg(B64, ITEM_ZM), B64),
  /** XQuery function. */
  _ARCHIVE_OPTIONS(FNArchive.class, "options(archive)", arg(B64), ELM),
  /** XQuery function. */
  _ARCHIVE_WRITE(FNArchive.class, "write(path,archive[,entries])", arg(STR, B64, ITEM_ZM), EMP),

  /* FNBin functions. */

  /** XQuery function. */
  _BIN_HEX(FNBin.class, "hex(string)", arg(STR_ZO), B64_ZO),
  /** XQuery function. */
  _BIN_BIN(FNBin.class, "bin(string)", arg(STR_ZO), B64_ZO),
  /** XQuery function. */
  _BIN_OCTAL(FNBin.class, "octal(string)", arg(STR_ZO), B64_ZO),
  /** XQuery function. */
  _BIN_TO_OCTETS(FNBin.class, "to-octets(binary)", arg(B64_ZO), ITR_ZM),
  /** XQuery function. */
  _BIN_FROM_OCTETS(FNBin.class, "from-octets(integers)", arg(ITR_ZM), B64),
  /** XQuery function. */
  _BIN_LENGTH(FNBin.class, "length(binary)", arg(B64), ITR),
  /** XQuery function. */
  _BIN_PART(FNBin.class, "part(binary,offset[,size])", arg(B64_ZO, ITR, ITR), B64_ZO),
  /** XQuery function. */
  _BIN_JOIN(FNBin.class, "join(binaries)", arg(B64_ZM), B64),
  /** XQuery function. */
  _BIN_INSERT_BEFORE(FNBin.class, "insert-before(binary,offset,extra)",
      arg(B64_ZO, ITR, B64_ZO), B64_ZO),
  /** XQuery function. */
  _BIN_PAD_LEFT(FNBin.class, "pad-left(binary,size[,octet])", arg(B64_ZO, ITR, ITR), B64_ZO),
  /** XQuery function. */
  _BIN_PAD_RIGHT(FNBin.class, "pad-right(binary,size[,octet])", arg(B64_ZO, ITR, ITR), B64_ZO),
  /** XQuery function. */
  _BIN_FIND(FNBin.class, "find(binary,offset,search)", arg(B64_ZO, ITR, B64_ZO), ITR_ZO),
  /** XQuery function. */
  _BIN_DECODE_STRING(FNBin.class, "decode-string(binary[,encoding[,offset[,size]]])",
      arg(B64_ZO, STR, ITR, ITR), STR_ZO),
  /** XQuery function. */
  _BIN_ENCODE_STRING(FNBin.class, "encode-string(string[,encoding])", arg(STR_ZO, STR), B64_ZO),
  /** XQuery function. */
  _BIN_PACK_DOUBLE(FNBin.class, "pack-double(double[,order])", arg(DBL, STR), B64),
  /** XQuery function. */
  _BIN_PACK_FLOAT(FNBin.class, "pack-float(float[,order])", arg(FLT, STR), B64),
  /** XQuery function. */
  _BIN_PACK_INTEGER(FNBin.class, "pack-integer(integer,size[,order])",
      arg(ITR, ITR, STR), B64),
  /** XQuery function. */
  _BIN_UNPACK_DOUBLE(FNBin.class, "unpack-double(binary,offset[,order])",
      arg(B64, ITR, STR), DBL),
  /** XQuery function. */
  _BIN_UNPACK_FLOAT(FNBin.class, "unpack-float(binary,offset[,order])",
      arg(B64, ITR, STR), FLT),
  /** XQuery function. */
  _BIN_UNPACK_INTEGER(FNBin.class, "unpack-integer(binary,offset,size[,order])",
      arg(B64, ITR, ITR, STR), ITR),
  /** XQuery function. */
  _BIN_UNPACK_UNSIGNED_INTEGER(FNBin.class, "unpack-unsigned-integer(binary,offset,size[,order])",
      arg(B64, ITR, ITR, STR), ITR),
  /** XQuery function. */
  _BIN_OR(FNBin.class, "or(binary1,binary2)", arg(B64_ZO, B64_ZO), B64_ZO),
  /** XQuery function. */
  _BIN_XOR(FNBin.class, "xor(binary1,binary2)", arg(B64_ZO, B64_ZO), B64_ZO),
  /** XQuery function. */
  _BIN_AND(FNBin.class, "and(binary1,binary2)", arg(B64_ZO, B64_ZO), B64_ZO),
  /** XQuery function. */
  _BIN_NOT(FNBin.class, "not(binary)", arg(B64_ZO), B64_ZO),
  /** XQuery function. */
  _BIN_SHIFT(FNBin.class, "shift(binary,by)", arg(B64_ZO, ITR), B64_ZO),

  /* FNClient functions. */

  /** XQuery function. */
  _CLIENT_CONNECT(FNClient.class, "connect(url,port,user,password)",
      arg(STR, ITR, STR, STR), URI, flag(NDT)),
  /** XQuery function. */
  _CLIENT_EXECUTE(FNClient.class, "execute(id,command)", arg(URI, STR), STR, flag(NDT)),
  /** XQuery function. */
  _CLIENT_INFO(FNClient.class, "info(id)", arg(URI), STR, flag(NDT)),
  /** XQuery function. */
  _CLIENT_QUERY(FNClient.class, "query(id,query[,bindings])",
      arg(URI, STR, ITEM), ITEM_ZO, flag(NDT)),
  /** XQuery function. */
  _CLIENT_CLOSE(FNClient.class, "close(id)", arg(URI), EMP, flag(NDT)),

  /* FNConvert functions. */

  /** XQuery function. */
  _CONVERT_INTEGER_TO_BASE(FNConvert.class, "integer-to-base(number,base)", arg(ITR, ITR), STR),
  /** XQuery function. */
  _CONVERT_INTEGER_FROM_BASE(FNConvert.class, "integer-from-base(string,base)", arg(STR, ITR), ITR),
  /** XQuery function. */
  _CONVERT_BINARY_TO_BYTES(FNConvert.class, "binary-to-bytes(binary)", arg(ITEM), BYT_ZM),
  /** XQuery function. */
  _CONVERT_BINARY_TO_STRING(FNConvert.class, "binary-to-string(binary[,encoding])",
      arg(ITEM, STR), STR),
  /** XQuery function. */
  _CONVERT_BYTES_TO_HEX(FNConvert.class, "bytes-to-hex(bytes)", arg(BYT_ZM), HEX),
  /** XQuery function. */
  _CONVERT_BYTES_TO_BASE64(FNConvert.class, "bytes-to-base64(bytes)", arg(BYT_ZM), B64),
  /** XQuery function. */
  _CONVERT_STRING_TO_BASE64(FNConvert.class, "string-to-base64(string[,encoding])",
      arg(STR, STR), B64),
  /** XQuery function. */
  _CONVERT_STRING_TO_HEX(FNConvert.class, "string-to-hex(string[,encoding])", arg(STR, STR), HEX),
  /** XQuery function. */
  _CONVERT_INTEGER_TO_DATETIME(FNConvert.class, "integer-to-dateTime(ms)", arg(ITR), DTM),
  /** XQuery function. */
  _CONVERT_DATETIME_TO_INTEGER(FNConvert.class, "dateTime-to-integer(date)", arg(DTM), ITR),
  /** XQuery function. */
  _CONVERT_INTEGER_TO_DAYTIME(FNConvert.class, "integer-to-dayTime(ms)", arg(ITR), DTD),
  /** XQuery function. */
  _CONVERT_DAYTIME_TO_INTEGER(FNConvert.class, "dayTime-to-integer(duration)", arg(DTD), ITR),

  /* FNCrypto functions (EXPath Cryptographic module). */

  /** XQuery function. */
  _CRYPTO_HMAC(FNCrypto.class, "hmac(message,key,algorithm[,encoding])",
      arg(STR, STR, STR, STR_ZO), STR),
  /** XQuery function. */
  _CRYPTO_ENCRYPT(FNCrypto.class, "encrypt(input,encryption,key,algorithm)",
      arg(STR, STR, STR, STR), STR),
  /** XQuery function. */
  _CRYPTO_DECRYPT(FNCrypto.class, "decrypt(input,type,key,algorithm)",
      arg(STR, STR, STR, STR), STR),
  /** XQuery function. */
  _CRYPTO_GENERATE_SIGNATURE(FNCrypto.class, "generate-signature" +
      "(input,canonicalization,digest,signature,prefix,type[,item1][,item2])",
      arg(NOD, STR, STR, STR, STR, STR, ITEM_ZO, ITEM_ZO), NOD),
  /** XQuery function. */
  _CRYPTO_VALIDATE_SIGNATURE(FNCrypto.class, "validate-signature(node)", arg(NOD), BLN),

  /* FNCsv functions. */

  /** XQuery function. */
  _CSV_PARSE(FNCsv.class, "parse(string[,config])", arg(STR, MAP_O), ITEM),
  /** XQuery function. */
  _CSV_SERIALIZE(FNCsv.class, "serialize(item[,params])", arg(ITEM_ZO, ITEM_ZO), STR),

  /* FNDb functions. */

  /** XQuery function. */
  _DB_OPEN(FNDb.class, "open(database[,path])", arg(STR, STR), NOD_ZM),
  /** XQuery function. */
  _DB_OPEN_PRE(FNDb.class, "open-pre(database,pre)", arg(STR, ITR), NOD_ZM),
  /** XQuery function. */
  _DB_OPEN_ID(FNDb.class, "open-id(database,id)", arg(STR, ITR), NOD_ZM),
  /** XQuery function. */
  _DB_TEXT(FNDb.class, "text(database,string)", arg(STR, ITEM), NOD_ZM, flag(NDT)),
  /** XQuery function. */
  _DB_TEXT_RANGE(FNDb.class, "text-range(database,from,to)",
      arg(STR, ITEM, ITEM), NOD_ZM, flag(NDT)),
  /** XQuery function. */
  _DB_ATTRIBUTE(FNDb.class, "attribute(database,string[,name])",
      arg(STR, ITEM, STR), NOD_ZM, flag(NDT)),
  /** XQuery function. */
  _DB_ATTRIBUTE_RANGE(FNDb.class, "attribute-range(database,from,to[,name])",
      arg(STR, ITEM, ITEM, STR), NOD_ZM, flag(NDT)),
  /** XQuery function. */
  _DB_LIST(FNDb.class, "list([database[,path]])", arg(STR, STR), STR_ZM),
  /** XQuery function. */
  _DB_LIST_DETAILS(FNDb.class, "list-details([database[,path]])", arg(STR, STR), ELM_ZM),
  /** XQuery function. */
  _DB_BACKUPS(FNDb.class, "backups([database])", arg(ITEM), ELM_ZM),
  /** XQuery function. */
  _DB_BACKUP(FNDb.class, "backup(database)", arg(STR), EMP, flag(UPD, NDT)),
  /** XQuery function. */
  _DB_RESTORE(FNDb.class, "restore(backup)", arg(STR), EMP, flag(UPD, NDT)),
  /** XQuery function. */
  _DB_SYSTEM(FNDb.class, "system()", arg(), STR),
  /** XQuery function. */
  _DB_INFO(FNDb.class, "info(database)", arg(ITEM), STR),
  /** XQuery function. */
  _DB_NODE_ID(FNDb.class, "node-id(nodes)", arg(NOD_ZM), ITR_ZM),
  /** XQuery function. */
  _DB_NODE_PRE(FNDb.class, "node-pre(nodes)", arg(NOD_ZM), ITR_ZM),
  /** XQuery function. */
  _DB_EVENT(FNDb.class, "event(name,query)", arg(STR, ITEM_ZM), EMP, flag(NDT)),
  /** XQuery function. */
  _DB_OUTPUT(FNDb.class, "output(result)", arg(ITEM_ZM), EMP, flag(UPD, NDT)),
  /** XQuery function. */
  _DB_ADD(FNDb.class, "add(database,input[,path])", arg(STR, NOD, STR), EMP, flag(UPD, NDT)),
  /** XQuery function. */
  _DB_DELETE(FNDb.class, "delete(database,path)", arg(STR, STR), EMP, flag(UPD, NDT)),
  /** XQuery function. */
  _DB_CREATE(FNDb.class, "create(name[,inputs[,paths[,options]]])",
      arg(STR, ITEM_ZM, STR_ZM, ITEM), EMP, flag(UPD, NDT)),
  /** XQuery function. */
  _DB_DROP(FNDb.class, "drop(database)", arg(ITEM), EMP, flag(UPD, NDT)),
  /** XQuery function. */
  _DB_RENAME(FNDb.class, "rename(database,path,newpath)", arg(STR, STR, STR), EMP, flag(UPD, NDT)),
  /** XQuery function. */
  _DB_REPLACE(FNDb.class, "replace(database,path,item)", arg(STR, STR, ITEM), EMP, flag(UPD, NDT)),
  /** XQuery function. */
  _DB_OPTIMIZE(FNDb.class, "optimize(database[,all[,options]])",
      arg(STR, BLN, ITEM), EMP, flag(UPD, NDT)),
  /** XQuery function. */
  _DB_RETRIEVE(FNDb.class, "retrieve(database,path)", arg(STR, STR), B64),
  /** XQuery function. */
  _DB_STORE(FNDb.class, "store(database,path,input)", arg(STR, STR, ITEM), EMP, flag(UPD, NDT)),
  /** XQuery function. */
  _DB_IS_XML(FNDb.class, "is-xml(database,path)", arg(STR, STR), BLN),
  /** XQuery function. */
  _DB_IS_RAW(FNDb.class, "is-raw(database,path)", arg(STR, STR), BLN),
  /** XQuery function. */
  _DB_EXISTS(FNDb.class, "exists(database[,path])", arg(STR, STR), BLN),
  /** XQuery function. */
  _DB_CONTENT_TYPE(FNDb.class, "content-type(database,path)", arg(STR, STR), STR),
  /** XQuery function. */
  _DB_FLUSH(FNDb.class, "flush(database)", arg(ITEM), EMP, flag(UPD, NDT)),
  /** XQuery function. */
  _DB_EXPORT(FNDb.class, "export(database,path[,param]])", arg(STR, STR, ITEM), EMP),
  /** XQuery function. */
  _DB_NAME(FNDb.class, "name(node)", arg(NOD), STR),
  /** XQuery function. */
  _DB_PATH(FNDb.class, "path(node)", arg(NOD), STR),

  /* FNFetch functions. */

  /** XQuery function. */
  _FETCH_TEXT(FNFetch.class, "text(uri[,encoding)", arg(STR, STR), STR, flag(NDT)),
  /** XQuery function. */
  _FETCH_BINARY(FNFetch.class, "binary(uri)", arg(STR), B64, flag(NDT)),
  /** XQuery function. */
  _FETCH_CONTENT_TYPE(FNFetch.class, "content-type(uri)", arg(STR), STR, flag(NDT)),

  /* FNFile functions (EXPath). */

  /** XQuery function. */
  _FILE_PATH_SEPARATOR(FNFile.class, "path-separator()", arg(), STR),
  /** XQuery function. */
  _FILE_DIR_SEPARATOR(FNFile.class, "dir-separator()", arg(), STR),
  /** XQuery function. */
  _FILE_LINE_SEPARATOR(FNFile.class, "line-separator()", arg(), STR),
  /** XQuery function. */
  _FILE_TEMP_DIR(FNFile.class, "temp-dir()", arg(), STR),
  /** XQuery function. */
  _FILE_NAME(FNFile.class, "name(path)", arg(STR), STR),
  /** XQuery function. */
  _FILE_PARENT(FNFile.class, "parent(path)", arg(STR), STR_ZO),
  /** XQuery function. */
  _FILE_PATH_TO_URI(FNFile.class, "path-to-uri(path)", arg(STR), URI),
  /** XQuery function. */
  _FILE_EXISTS(FNFile.class, "exists(path)", arg(STR), BLN, flag(NDT)),
  /** XQuery function. */
  _FILE_IS_DIR(FNFile.class, "is-dir(path)", arg(STR), BLN, flag(NDT)),
  /** XQuery function. */
  _FILE_IS_FILE(FNFile.class, "is-file(path)", arg(STR), BLN, flag(NDT)),
  /** XQuery function. */
  _FILE_LAST_MODIFIED(FNFile.class, "last-modified(path)", arg(STR), DTM, flag(NDT)),
  /** XQuery function. */
  _FILE_SIZE(FNFile.class, "size(path)", arg(STR), ITR, flag(NDT)),
  /** XQuery function. */
  _FILE_PATH_TO_NATIVE(FNFile.class, "path-to-native(path)", arg(STR), STR, flag(NDT)),
  /** XQuery function. */
  _FILE_RESOLVE_PATH(FNFile.class, "resolve-path(path)", arg(STR), STR, flag(NDT)),
  /** XQuery function. */
  _FILE_LIST(FNFile.class, "list(path[,recursive[,pattern]])",
      arg(STR, BLN, STR), STR_ZM, flag(NDT)),
  /** XQuery function. */
  _FILE_CREATE_DIR(FNFile.class, "create-dir(path)", arg(STR), EMP, flag(NDT)),
  /** XQuery function. */
  _FILE_CREATE_TEMP_DIR(FNFile.class, "create-temp-dir(prefix,suffix[,dir])",
      arg(STR, STR, STR), STR, flag(NDT)),
  /** XQuery function. */
  _FILE_CREATE_TEMP_FILE(FNFile.class, "create-temp-file(prefix,suffix[,dir])",
      arg(STR, STR, STR), STR, flag(NDT)),
  /** XQuery function. */
  _FILE_DELETE(FNFile.class, "delete(path[,recursive])", arg(STR, BLN), EMP, flag(NDT)),
  /** XQuery function. */
  _FILE_READ_TEXT(FNFile.class, "read-text(path[,encoding])", arg(STR, STR), STR, flag(NDT)),
  /** XQuery function. */
  _FILE_READ_TEXT_LINES(FNFile.class, "read-text-lines(path[,encoding])",
      arg(STR, STR), STR_ZM, flag(NDT)),
  /** XQuery function. */
  _FILE_READ_BINARY(FNFile.class, "read-binary(path[,offset[,length]])",
      arg(STR, ITR, ITR), B64, flag(NDT)),
  /** XQuery function. */
  _FILE_WRITE(FNFile.class, "write(path,data[,params])", arg(STR, ITEM_ZM, ITEM), EMP, flag(NDT)),
  /** XQuery function. */
  _FILE_WRITE_BINARY(FNFile.class, "write-binary(path,item[,offset])",
      arg(STR, BIN, ITR), EMP, flag(NDT)),
  /** XQuery function. */
  _FILE_WRITE_TEXT(FNFile.class, "write-text(path,text[,encoding])",
      arg(STR, STR, STR), EMP, flag(NDT)),
  /** XQuery function. */
  _FILE_WRITE_TEXT_LINES(FNFile.class, "write-text-lines(path,texts[,encoding])",
      arg(STR, STR_ZM, STR), EMP, flag(NDT)),
  /** XQuery function. */
  _FILE_APPEND(FNFile.class, "append(path,data[,params])", arg(STR, ITEM_ZM, ITEM), EMP, flag(NDT)),
  /** XQuery function. */
  _FILE_APPEND_BINARY(FNFile.class, "append-binary(path,item)", arg(STR, BIN), EMP, flag(NDT)),
  /** XQuery function. */
  _FILE_APPEND_TEXT(FNFile.class, "append-text(path,text[,encoding])",
      arg(STR, STR, STR), EMP, flag(NDT)),
  /** XQuery function. */
  _FILE_APPEND_TEXT_LINES(FNFile.class, "append-text-lines(path,texts[,encoding])",
      arg(STR, STR_ZM, STR), EMP, flag(NDT)),
  /** XQuery function. */
  _FILE_COPY(FNFile.class, "copy(source,target)", arg(STR, STR), EMP, flag(NDT)),
  /** XQuery function. */
  _FILE_MOVE(FNFile.class, "move(source,target)", arg(STR, STR), EMP, flag(NDT)),

  /* FNFt functions. */

  /** XQuery function. */
  _FT_CONTAINS(FNFt.class, "contains(input,terms[,options])",
      arg(ITEM, ITEM_ZM, ITEM), NOD_ZM, flag(NDT)),
  /** XQuery function. */
  _FT_SEARCH(FNFt.class, "search(database,terms[,options])",
      arg(STR, ITEM_ZM, ITEM), NOD_ZM, flag(NDT)),
  /** XQuery function. */
  _FT_COUNT(FNFt.class, "count(nodes)", arg(NOD_ZM), ITR),
  /** XQuery function. */
  _FT_MARK(FNFt.class, "mark(nodes[,tag])", arg(NOD_ZM, STR), NOD_ZM),
  /** XQuery function. */
  _FT_EXTRACT(FNFt.class, "extract(nodes[,tag[,length]])", arg(ITEM_ZM, STR, ITR), NOD_ZM),
  /** XQuery function. */
  _FT_SCORE(FNFt.class, "score(items)", arg(ITEM_ZM), DBL_ZM),
  /** XQuery function. */
  _FT_TOKENS(FNFt.class, "tokens(database[,prefix])", arg(STR, STR), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  _FT_TOKENIZE(FNFt.class, "tokenize(string)", arg(STR), STR_ZM),

  /* FNHash functions. */

  /** XQuery function. */
  _HASH_MD5(FNHash.class, "md5(value)", arg(AAT), B64),
  /** XQuery function. */
  _HASH_SHA1(FNHash.class, "sha1(value)", arg(AAT), B64),
  /** XQuery function. */
  _HASH_SHA256(FNHash.class, "sha256(value)", arg(AAT), B64),
  /** XQuery function. */
  _HASH_HASH(FNHash.class, "hash(value,algorithm)", arg(AAT, STR), B64),

  /* FNHof functions. */

  /** XQuery function. */
  _HOF_SORT_WITH(FNHof.class, "sort-with(seq,lt-fun)",
      arg(ITEM_ZM, FuncType.get(BLN, ITEM, ITEM).seqType()), ITEM_ZM),
  /** XQuery function. */
  _HOF_ID(FNHof.class, "id(value)", arg(ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  _HOF_CONST(FNHof.class, "const(return,ignore)", arg(ITEM_ZM, ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  _HOF_UNTIL(FNHof.class, "until(pred,function,start)", arg(FuncType.get(BLN, ITEM_ZM).seqType(),
      FuncType.get(ITEM_ZM, ITEM_ZM).seqType(), ITEM_ZM), ITEM_ZM, flag(Flag.HOF)),
  /** XQuery function. */
  _HOF_FOLD_LEFT1(FNHof.class, "fold-left1(non-empty-seq,function)",
      arg(ITEM_OM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM).seqType()), ITEM_ZM, flag(Flag.HOF)),
  /** XQuery function. */
  _HOF_TOP_K_BY(FNHof.class, "top-k-by(seq,key-fun,k)",
      arg(ITEM_ZM, FuncType.arity(1).seqType(), ITR), ITEM_ZM, flag(Flag.HOF)),
  /** XQuery function. */
  _HOF_TOP_K_WITH(FNHof.class, "top-k-with(seq,less-than,k)",
      arg(ITEM_ZM, FuncType.get(BLN, ITEM_ZO, ITEM_ZO).seqType(), ITR), ITEM_ZM, flag(Flag.HOF)),

  /* FNHtml functions. */

  /** XQuery function. */
  _HTML_PARSER(FNHtml.class, "parser()", arg(), STR),
  /** XQuery function. */
  _HTML_PARSE(FNHtml.class, "parse(input[,options)", arg(STR, ITEM), DOC_O),

  /* FNHttp functions (EXPath). */

  /** XQuery function. */
  _HTTP_SEND_REQUEST(FNHttp.class, "send-request(request[,href,[bodies]])",
      arg(NOD, STR_ZO, ITEM_ZM), ITEM_ZM, flag(NDT)),

  /* FNIndex functions. */

  /** XQuery function. */
  _INDEX_FACETS(FNIndex.class, "facets(database[,type])", arg(STR, STR), DOC_O),
  /** XQuery function. */
  _INDEX_TEXTS(FNIndex.class, "texts(database[,prefix[,ascending]])",
      arg(STR, STR, BLN), NOD_ZM, flag(NDT)),
  /** XQuery function. */
  _INDEX_ATTRIBUTES(FNIndex.class, "attributes(database[,prefix[,ascending]])",
      arg(STR, STR, BLN), NOD_ZM, flag(NDT)),
  /** XQuery function. */
  _INDEX_ELEMENT_NAMES(FNIndex.class, "element-names(database)", arg(STR), NOD_ZM),
  /** XQuery function. */
  _INDEX_ATTRIBUTE_NAMES(FNIndex.class, "attribute-names(database)", arg(STR), NOD_ZM),

  /* FNInspect functions. */

  /** XQuery function. */
  _INSPECT_FUNCTION(FNInspect.class, "function(function)", arg(STR), ELM),
  /** XQuery function. */
  _INSPECT_MODULE(FNInspect.class, "module(path)", arg(STR), ELM),
  /** XQuery function. */
  _INSPECT_CONTEXT(FNInspect.class, "context()", arg(), ELM),
  /** XQuery function. */
  _INSPECT_FUNCTIONS(FNInspect.class, "functions()", arg(), FUN_ZM, flag(Flag.HOF)),
  /** XQuery function. */
  _INSPECT_XQDOC(FNInspect.class, "xqdoc(path)", arg(STR), ELM),

  /* FNJson functions. */

  /** XQuery function. */
  _JSON_PARSE(FNJson.class, "parse(string[,config])", arg(STR, MAP_O), ITEM),
  /** XQuery function. */
  _JSON_SERIALIZE(FNJson.class, "serialize(item[,params])", arg(ITEM_ZO, ITEM_ZO), STR),

  /* FNOut functions. */

  /** XQuery function. */
  _OUT_NL(FNOut.class, "nl()", arg(), STR),
  /** XQuery function. */
  _OUT_TAB(FNOut.class, "tab()", arg(), STR),
  /** XQuery function. */
  _OUT_FORMAT(FNOut.class, "format(format,item1[,...])", arg(STR, ITEM), STR),

  /* FNProc functions. */

  /** XQuery function. */
  _PROC_SYSTEM(FNProc.class, "system(command[,args[,encoding]])",
      arg(STR, STR_ZM, STR), STR, flag(NDT)),
  /** XQuery function. */
  _PROC_EXECUTE(FNProc.class, "execute(command[,args[,encoding]]])",
      arg(STR, STR_ZM, STR), ELM, flag(NDT)),

  /* FNProf functions. */

  /** XQuery function. */
  _PROF_MEM(FNProf.class, "mem(value[,cache[,label]])", arg(ITEM_ZM, BLN, STR), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  _PROF_TIME(FNProf.class, "time(value[,cache[,label]])",
      arg(ITEM_ZM, BLN, STR), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  _PROF_SLEEP(FNProf.class, "sleep(ms)", arg(ITR), EMP, flag(NDT)),
  /** XQuery function. */
  _PROF_CURRENT_MS(FNProf.class, "current-ms()", arg(), ITR, flag(NDT)),
  /** XQuery function. */
  _PROF_CURRENT_NS(FNProf.class, "current-ns()", arg(), ITR, flag(NDT)),
  /** XQuery function. */
  _PROF_DUMP(FNProf.class, "dump(value[,label])", arg(ITEM_ZM, STR), EMP, flag(NDT)),
  /** XQuery function. */
  _PROF_HUMAN(FNProf.class, "human(integer)", arg(ITR), STR, flag(NDT)),
  /** XQuery function. */
  _PROF_VOID(FNProf.class, "void(value)", arg(ITEM_ZM), EMP, flag(NDT)),

  /* FNRandom functions. */

  /** XQuery function. */
  _RANDOM_DOUBLE(FNRandom.class, "double()", arg(), DBL, flag(NDT)),
  /** XQuery function. */
  _RANDOM_INTEGER(FNRandom.class, "integer([max])", arg(ITR), ITR, flag(NDT)),
  /** XQuery function. */
  _RANDOM_SEEDED_DOUBLE(FNRandom.class, "seeded-double(seed,num)",
      arg(ITR, ITR), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  _RANDOM_SEEDED_INTEGER(FNRandom.class, "seeded-integer(seed,num[,max])",
      arg(ITR, ITR, ITR), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  _RANDOM_GAUSSIAN(FNRandom.class, "gaussian(num)", arg(ITR), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  _RANDOM_UUID(FNRandom.class, "uuid()", arg(), STR, flag(NDT)),

  /* FNRepo functions. */

  /** XQuery function. */
  _REPO_INSTALL(FNRepo.class, "install(uri)", arg(STR), EMP, flag(NDT)),
  /** XQuery function. */
  _REPO_DELETE(FNRepo.class, "delete(uri)", arg(STR), EMP, flag(NDT)),
  /** XQuery function. */
  _REPO_LIST(FNRepo.class, "list()", arg(), STR_ZM, flag(NDT)),

  /* FNSql functions. */

  /** XQuery function. */
  _SQL_INIT(FNSql.class, "init(class)", arg(STR), EMP, flag(NDT)),
  /** XQuery function. */
  _SQL_CONNECT(FNSql.class, "connect(url[,user[,pass[,options]]]]])",
      arg(STR, STR, STR, NOD_ZO), ITR, flag(NDT)),
  /** XQuery function. */
  _SQL_PREPARE(FNSql.class, "prepare(id,statement)", arg(ITR, STR), ITR, flag(NDT)),
  /** XQuery function. */
  _SQL_EXECUTE(FNSql.class, "execute(id,query)", arg(ITR, STR), ELM_ZM, flag(NDT)),
  /** XQuery function. */
  _SQL_EXECUTE_PREPARED(FNSql.class, "execute-prepared(id[,params])",
      arg(ITR, ELM), ELM_ZM, flag(NDT)),
  /** XQuery function. */
  _SQL_CLOSE(FNSql.class, "close(id)", arg(ITR), EMP, flag(NDT)),
  /** XQuery function. */
  _SQL_COMMIT(FNSql.class, "commit(id)", arg(ITR), EMP, flag(NDT)),
  /** XQuery function. */
  _SQL_ROLLBACK(FNSql.class, "rollback(id)", arg(ITR), EMP, flag(NDT)),

  /* FNStream functions. */

  /** XQuery function. */
  _STREAM_MATERIALIZE(FNStream.class, "materialize(item)", arg(ITEM), ITEM),
  /** XQuery function. */
  _STREAM_IS_STREAMABLE(FNStream.class, "is-streamable(item)", arg(ITEM), BLN),

  /* FNUnit functions. */

  /** XQuery function. */
  _UNIT_ASSERT(FNUnit.class, "assert(test[,message])", arg(ITEM_ZM, STR), EMP, flag(NDT)),
  /** XQuery function. */
  _UNIT_ASSERT_EQUALS(FNUnit.class, "assert-equals(result,expected[,message])",
      arg(ITEM_ZM, ITEM_ZM, STR), EMP, flag(NDT)),
  /** XQuery function. */
  _UNIT_FAIL(FNUnit.class, "fail(message)", arg(STR), EMP, flag(NDT)),
  /** XQuery function. */
  _UNIT_TEST(FNUnit.class, "test([functions])", arg(FUN_ZM), ELM, flag(NDT)),
  /** XQuery function. */
  _UNIT_TEST_URIS(FNUnit.class, "test-uris(uris)", arg(STR_ZM), ELM, flag(NDT)),

  /* FNValidate functions. */

  /** XQuery function. */
  _VALIDATE_XSD(FNValidate.class, "xsd(input[,schema])", arg(ITEM, ITEM), EMP, flag(NDT)),
  /** XQuery function. */
  _VALIDATE_XSD_INFO(FNValidate.class, "xsd-info(input[,schema])",
      arg(ITEM, ITEM), STR_ZM, flag(NDT)),
  /** XQuery function. */
  _VALIDATE_DTD(FNValidate.class, "dtd(input[,schema])", arg(ITEM, ITEM), EMP, flag(NDT)),
  /** XQuery function. */
  _VALIDATE_DTD_INFO(FNValidate.class, "dtd-info(input[,schema])",
      arg(ITEM, ITEM), STR_ZM, flag(NDT)),

  /* FNXQuery functions. */

  /** XQuery function. */
  _XQUERY_EVAL(FNXQuery.class, "eval(string[,bindings[,options]])",
      arg(STR, ITEM, ITEM), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  _XQUERY_EVALUATE(FNXQuery.class, "evaluate(string[,bindings[,options]])",
      arg(STR, ITEM, ITEM), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  _XQUERY_INVOKE(FNXQuery.class, "invoke(uri[,bindings[,options]])",
      arg(STR, ITEM, ITEM), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  _XQUERY_TYPE(FNXQuery.class, "type(value)", arg(ITEM_ZM), ITEM_ZM),

  /* FNXslt functions. */

  /** XQuery function. */
  _XSLT_PROCESSOR(FNXslt.class, "processor()", arg(), STR),
  /** XQuery function. */
  _XSLT_VERSION(FNXslt.class, "version()", arg(), STR),
  /** XQuery function. */
  _XSLT_TRANSFORM(FNXslt.class, "transform(input,stylesheet[,params])",
      arg(ITEM, ITEM, ITEM), NOD, flag(NDT)),
  /** XQuery function. */
  _XSLT_TRANSFORM_TEXT(FNXslt.class, "transform-text(input,stylesheet[,params])",
      arg(ITEM, ITEM, ITEM), STR, flag(NDT)),

  /* FNZip functions (EXPath). */

  /** XQuery function. */
  _ZIP_BINARY_ENTRY(FNZip.class, "binary-entry(path,entry)", arg(STR, STR), B64, flag(NDT)),
  /** XQuery function. */
  _ZIP_TEXT_ENTRY(FNZip.class, "text-entry(path,entry[,encoding])",
      arg(STR, STR, STR), STR, flag(NDT)),
  /** XQuery function. */
  _ZIP_HTML_ENTRY(FNZip.class, "html-entry(path,entry)", arg(STR, STR), NOD, flag(NDT)),
  /** XQuery function. */
  _ZIP_XML_ENTRY(FNZip.class, "xml-entry(path,entry)", arg(STR, STR), NOD, flag(NDT)),
  /** XQuery function. */
  _ZIP_ENTRIES(FNZip.class, "entries(path)", arg(STR), ELM, flag(NDT)),
  /** XQuery function. */
  _ZIP_ZIP_FILE(FNZip.class, "zip-file(zip)", arg(ELM), EMP, flag(NDT)),
  /** XQuery function. */
  _ZIP_UPDATE_ENTRIES(FNZip.class, "update-entries(zip,output)", arg(ELM, STR), EMP, flag(NDT));

  /**
   * Mapping between function classes and namespace URIs.
   * If no mapping exists, {@link QueryText#FNURI} will be assumed as default mapping.
   */
  public static final HashMap<Class<? extends StandardFunc>, byte[]> URIS =
    new HashMap<Class<? extends StandardFunc>, byte[]>();

  // initialization of class/uri mappings and statically known modules
  static {
    // W3 functions
    URIS.put(FNMap.class,  MAPURI);
    URIS.put(FNMath.class, MATHURI);
    // EXPath functions
    URIS.put(FNBin.class,    BINURI);
    URIS.put(FNCrypto.class, CRYPTOURI);
    URIS.put(FNFile.class,   FILEURI);
    URIS.put(FNHttp.class,   HTTPURI);
    URIS.put(FNZip.class,    ZIPURI);
    // internal functions
    URIS.put(FNRepo.class,     REPOURI);
    URIS.put(FNAdmin.class,    ADMINURI);
    URIS.put(FNArchive.class,  ARCHIVEURI);
    URIS.put(FNClient.class,   CLIENTURI);
    URIS.put(FNConvert.class,  CONVERTURI);
    URIS.put(FNCsv.class,      CSVURI);
    URIS.put(FNDb.class,       DBURI);
    URIS.put(FNFetch.class,    FETCHURI);
    URIS.put(FNFt.class,       FTURI);
    URIS.put(FNHash.class,     HASHURI);
    URIS.put(FNHof.class,      HOFURI);
    URIS.put(FNHtml.class,     HTMLURI);
    URIS.put(FNIndex.class,    INDEXURI);
    URIS.put(FNInspect.class,  INSPECTURI);
    URIS.put(FNJson.class,     JSONURI);
    URIS.put(FNOut.class,      OUTURI);
    URIS.put(FNProc.class,     PROCURI);
    URIS.put(FNProf.class,     PROFURI);
    URIS.put(FNRandom.class,   RANDOMURI);
    URIS.put(FNSql.class,      SQLURI);
    URIS.put(FNStream.class,   STREAMURI);
    URIS.put(FNUnit.class,     UNITURI);
    URIS.put(FNValidate.class, VALIDATEURI);
    URIS.put(FNXslt.class,     XSLTURI);
    URIS.put(FNXQuery.class,   XQUERYURI);
  }

  /** Argument pattern. */
  private static final Pattern ARG = Pattern.compile(
      "^([-\\w_:\\.]*\\(|<|\"|\\$| ).*", Pattern.DOTALL);

  /** Cached enums (faster). */
  public static final Function[] VALUES = values();
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
  /** Compiler flags. */
  private final EnumSet<Flag> flags;

  /** Function classes. */
  private final Class<? extends StandardFunc> func;


  /**
   * Constructs a function signature; calls
   * {@link #Function(Class, String, SeqType[], SeqType, EnumSet)}.
   * @param clz reference to the class containing the function implementation
   * @param dsc descriptive function string
   * @param typ types of the function arguments
   * @param rtn return type
   */
  Function(final Class<? extends StandardFunc> clz, final String dsc, final SeqType[] typ,
      final SeqType rtn) {
    this(clz, dsc, typ, rtn, EnumSet.noneOf(Flag.class));
  }

  /**
   * Constructs a function signature.
   * @param clz reference to the class containing the function implementation
   * @param dsc descriptive function string, containing the function name and its
   *            arguments in parentheses. Optional arguments are represented in nested
   *            square brackets; three dots indicate that the number of arguments of a
   *            function is not limited
   * @param typ types of the function arguments
   * @param rtn return type
   * @param flg static function properties
   */
  Function(final Class<? extends StandardFunc> clz, final String dsc, final SeqType[] typ,
      final SeqType rtn, final EnumSet<Flag> flg) {

    func = clz;
    desc = dsc;
    ret = rtn;
    args = typ;
    flags = flg;

    // count number of minimum and maximum arguments by analyzing the function string
    final int b = dsc.indexOf('[');
    if(b == -1) {
      min = typ.length;
      max = typ.length;
    } else {
      int c = b + 1 < dsc.length() && dsc.charAt(b + 1) == ',' ? 1 : 0;
      for(int i = 0; i < b; i++) if(dsc.charAt(i) == ',') c++;
      min = c;
      max = dsc.contains("...") ? Integer.MAX_VALUE : typ.length;
    }
  }

  /**
   * Creates a new instance of the function.
   * @param sc static context
   * @param arg arguments
   * @return function
   */
  public StandardFunc get(final StaticContext sc, final Expr... arg) {
    return get(sc, null, arg);
  }

  /**
   * Creates a new instance of the function.
   * @param sc static context
   * @param ii input info
   * @param arg arguments
   * @return function
   */
  public StandardFunc get(final StaticContext sc, final InputInfo ii, final Expr... arg) {
    return Reflect.get(Reflect.find(func, StaticContext.class, InputInfo.class, Function.class,
        Expr[].class), sc, ii, this, arg);
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
   * Indicates if an expression has the specified compiler property.
   * @param flag flag to be found
   * @return result of check
   * @see Expr#has(Flag)
   */
  public boolean has(final Flag flag) {
    return flags.contains(flag);
  }

  /**
   * Returns the function type of this function with the given arity.
   * @param arity number of arguments
   * @return function type
   */
  final FuncType type(final int arity) {
    final Ann ann = new Ann();
    if(has(UPD)) ann.add(Ann.Q_UPDATING, Empty.SEQ, null);
    final SeqType[] arg = new SeqType[arity];
    if(arity != 0 && max == Integer.MAX_VALUE) {
      System.arraycopy(args, 0, arg, 0, args.length);
      final SeqType var = args[args.length - 1];
      for(int i = args.length; i < arg.length; i++) arg[i] = var;
    } else {
      System.arraycopy(args, 0, arg, 0, arity);
    }
    return FuncType.get(ann, ret, arg);
  }

  /**
   * Returns an array representation of the specified sequence types.
   * @param arg arguments
   * @return array
   */
  private static SeqType[] arg(final SeqType... arg) { return arg; }

  /**
   * Returns a set representation of the specified compiler flags.
   * @param flags flags
   * @return set
   */
  private static EnumSet<Flag> flag(final Flag... flags) {
    final EnumSet<Flag> set = EnumSet.noneOf(Flag.class);
    Collections.addAll(set, flags);
    return set;
  }

  /**
   * Returns the function's variable names.
   * @return array of variable names
   */
  final String[] names() {
    final String names = desc.replaceFirst(".*?\\(", "").replace(",...",
        "").replaceAll("[\\[\\]\\)\\s]", "");
    return names.isEmpty() ? new String[0] : names.split(",");
  }

  /**
   * Returns the the variable names for an instance of this function with the given arity.
   * @param arity number of arguments
   * @return array of argument names
   */
  public final QNm[] argNames(final int arity) {
    final String[] names = names();
    final QNm[] res = new QNm[arity];
    for(int i = Math.min(arity, names.length); --i >= 0;) res[i] = new QNm(names[i]);
    if(arity > names.length) {
      final String[] parts = names[names.length - 1].split("(?=\\d+$)", 2);
      final int start = Integer.parseInt(parts[1]);
      for(int i = names.length; i < arity; i++)
        res[i] = new QNm(parts[0] + (start + i - names.length + 1), "");
    }
    return res;
  }

  /**
   * Returns a string representation of the function with the specified
   * arguments. All objects are wrapped with quotes, except for the following ones:
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
      if(ARG.matcher(s).matches() || a instanceof Integer) {
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
    return new TokenBuilder(NSGlobal.prefix(uri())).add(':').add(desc).toString();
  }

  /*
   * Returns the names of all functions. Used to update MediaWiki syntax highlighter.
   * All function names are listed in reverse order to give precedence to longer names.
   * @param args ignored
  public static void main(final String... args) {
    final StringList sl = new StringList();
    for(Function f : VALUES) {
      sl.add(f.toString().replaceAll("^fn:|\\(.*", ""));
    }
    for(final String s : sl.sort(false, false)) {
      Util.out(s + ' ');
    }
    Util.outln("fn:");
  }
   */
}
