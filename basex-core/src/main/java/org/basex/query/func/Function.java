package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.expr.Expr.Flag.*;
import static org.basex.query.value.type.SeqType.*;

import java.util.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr.Flag;
import org.basex.query.func.admin.*;
import org.basex.query.func.archive.*;
import org.basex.query.func.array.*;
import org.basex.query.func.bin.*;
import org.basex.query.func.client.*;
import org.basex.query.func.convert.*;
import org.basex.query.func.crypto.*;
import org.basex.query.func.csv.*;
import org.basex.query.func.db.*;
import org.basex.query.func.fetch.*;
import org.basex.query.func.file.*;
import org.basex.query.func.fn.*;
import org.basex.query.func.ft.*;
import org.basex.query.func.hash.*;
import org.basex.query.func.hof.*;
import org.basex.query.func.html.*;
import org.basex.query.func.http.*;
import org.basex.query.func.index.*;
import org.basex.query.func.inspect.*;
import org.basex.query.func.json.*;
import org.basex.query.func.map.*;
import org.basex.query.func.math.*;
import org.basex.query.func.out.*;
import org.basex.query.func.proc.*;
import org.basex.query.func.prof.*;
import org.basex.query.func.random.*;
import org.basex.query.func.repo.*;
import org.basex.query.func.sql.*;
import org.basex.query.func.stream.*;
import org.basex.query.func.unit.*;
import org.basex.query.func.validate.*;
import org.basex.query.func.xquery.*;
import org.basex.query.func.xslt.*;
import org.basex.query.func.zip.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Definitions of all built-in XQuery functions.
 * New namespace mappings for function prefixes and URIs must be added to the static intializer of
 * the {@code NSGlobal} class.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public enum Function {

  // Standard functions

  /** XQuery function. */
  ABS(FnAbs.class, "abs(num)", arg(AAT_ZO), AAT_ZO),
  /** XQuery function. */
  ADJUST_DATE_TO_TIMEZONE(FnAdjustDateToTimezone.class, "adjust-date-to-timezone(date[,zone])",
      arg(DAT_ZO, DTD_ZO), DAT_ZO),
  /** XQuery function. */
  ADJUST_DATETIME_TO_TIMEZONE(FnAdustDateTimeToTimezone.class,
      "adjust-dateTime-to-timezone(date[,zone])", arg(DTM_ZO, DTD_ZO), DTM_ZO),
  /** XQuery function. */
  ADJUST_TIME_TO_TIMEZONE(FnAdjustTimeToTimezone.class, "adjust-time-to-timezone(date[,zone])",
      arg(TIM_ZO, DTD_ZO), TIM_ZO),
  /** XQuery function. */
  ANALYZE_STRING(FnAnalyzeString.class, "analyze-string(input,pattern[,mod])",
      arg(STR_ZO, STR, STR), ELM, flag(CNS)),
  /** XQuery function. */
  AVAILABLE_ENVIRONMENT_VARIABLES(FnAvailableEnvironmentVariables.class,
      "available-environment-variables()", arg(), STR_ZM),
  /** XQuery function. */
  AVG(FnAvg.class, "avg(items)", arg(AAT_ZM), AAT_ZO),
  /** XQuery function. */
  BASE_URI(FnBaseUri.class, "base-uri([node])", arg(NOD_ZO), URI_ZO),
  /** XQuery function. */
  BOOLEAN(FnBoolean.class, "boolean(items)", arg(ITEM_ZM), BLN),
  /** XQuery function. */
  CEILING(FnCeiling.class, "ceiling(num)", arg(AAT_ZO), AAT_ZO),
  /** XQuery function. */
  CODEPOINT_EQUAL(FnCodepointEqual.class, "codepoint-equal(string1,string2)",
      arg(STR_ZO, STR_ZO), BLN_ZO),
  /** XQuery function. */
  CODEPOINTS_TO_STRING(FnCodepointsToString.class, "codepoints-to-string(nums)", arg(ITR_ZM), STR),
  /** XQuery function. */
  COLLECTION(FnCollection.class, "collection([uri])", arg(STR_ZO), NOD_ZM),
  /** XQuery function. */
  COMPARE(FnCompare.class, "compare(first,second[,collation])", arg(STR_ZO, STR_ZO, STR), ITR_ZO),
  /** XQuery function. */
  CONCAT(FnConcat.class, "concat(atom1,atom2[,...])", arg(AAT_ZO, AAT_ZO), STR),
  /** XQuery function. */
  CONTAINS(FnContains.class, "contains(string,sub[,collation])", arg(STR_ZO, STR_ZO, STR), BLN),
  /** XQuery function. */
  CONTAINS_TOKEN(FnContainsToken.class, "contains-token(strings,token[,collation])",
      arg(STR_ZM, STR, STR), BLN),
  /** XQuery function. */
  COUNT(FnCount.class, "count(items)", arg(ITEM_ZM), ITR),
  /** XQuery function. */
  CURRENT_DATE(FnCurrentDate.class, "current-date()", arg(), DAT),
  /** XQuery function. */
  CURRENT_DATETIME(FnCurrentDateTime.class, "current-dateTime()", arg(), DTM),
  /** XQuery function. */
  CURRENT_TIME(FnCurrentTime.class, "current-time()", arg(), TIM),
  /** XQuery function. */
  DATA(FnData.class, "data([items])", arg(ITEM_ZM), AAT_ZM),
  /** XQuery function. */
  DATETIME(FnDateTime.class, "dateTime(date,time)", arg(DAT_ZO, TIM_ZO), DTM_ZO),
  /** XQuery function. */
  DAY_FROM_DATE(FnDayFromDate.class, "day-from-date(date)", arg(DAT_ZO), ITR_ZO),
  /** XQuery function. */
  DAY_FROM_DATETIME(FnDayFromDateTime.class, "day-from-dateTime(datetime)", arg(DTM_ZO), ITR_ZO),
  /** XQuery function. */
  DAYS_FROM_DURATION(FnDayFromDuration.class, "days-from-duration(duration)", arg(DUR_ZO), ITR_ZO),
  /** XQuery function. */
  DEEP_EQUAL(FnDeepEqual.class, "deep-equal(items1,items2[,collation])",
      arg(ITEM_ZM, ITEM_ZM, STR), BLN),
  /** XQuery function. */
  DEEP_EQUAL_OPT(FnDeepEqualOpt.class, "deep-equal-opt(items1,items2[,options])",
      arg(ITEM_ZM, ITEM_ZM, ITEM), BLN),
  /** XQuery function. */
  DEFAULT_COLLATION(FnDefaultCollation.class, "default-collation()", arg(), STR),
  /** XQuery function. */
  DISTINCT_VALUES(FnDistinctValues.class, "distinct-values(items[,collation])",
      arg(AAT_ZM, STR), AAT_ZM),
  /** XQuery function. */
  DOC(FnDoc.class, "doc(uri)", arg(STR_ZO), DOC_ZO),
  /** XQuery function. */
  DOC_AVAILABLE(FnDocAvailable.class, "doc-available(uri)", arg(STR_ZO), BLN),
  /** XQuery function. */
  DOCUMENT_URI(FnDocumentUri.class, "document-uri([node])", arg(NOD_ZO), URI_ZO),
  /** XQuery function. */
  ELEMENT_WITH_ID(FnElementWithId.class, "element-with-id(string[,node])",
      arg(STR_ZM, NOD), ELM_ZM),
  /** XQuery function. */
  EMPTY(FnEmpty.class, "empty(items)", arg(ITEM_ZM), BLN),
  /** XQuery function. */
  ENCODE_FOR_URI(FnEncodeForUri.class, "encode-for-uri(string)", arg(STR_ZO), STR),
  /** XQuery function. */
  ENDS_WITH(FnEndsWith.class, "ends-with(string,sub[,collation])", arg(STR_ZO, STR_ZO, STR), BLN),
  /** XQuery function. */
  ENVIRONMENT_VARIABLE(FnEnvironmentVariable.class, "environment-variable(string)",
      arg(STR), STR_ZO),
  /** XQuery function. */
  ERROR(FnError.class, "error([code[,desc[,object]]])",
      arg(QNM_ZO, STR, ITEM_ZM), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  ESCAPE_HTML_URI(FnEscapeHtmlUri.class, "escape-html-uri(string)", arg(STR_ZO), STR),
  /** XQuery function. */
  EXACTLY_ONE(FnExactlyOne.class, "exactly-one(items)", arg(ITEM_ZM), ITEM),
  /** XQuery function. */
  EXISTS(FnExists.class, "exists(items)", arg(ITEM_ZM), BLN),
  /** XQuery function. */
  FALSE(FnFalse.class, "false()", arg(), BLN),
  /** XQuery function. */
  FILTER(FnFilter.class, "filter(items,function)",
      arg(ITEM_ZM, FuncType.get(BLN, ITEM).seqType()), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FLOOR(FnFloor.class, "floor(num)", arg(AAT_ZO), AAT_ZO),
  /** XQuery function. */
  FOLD_LEFT(FnFoldLeft.class, "fold-left(items,zero,function)",
      arg(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM).seqType()),
      ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FOLD_RIGHT(FnFoldRight.class, "fold-right(items,zero,function)",
      arg(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM, ITEM_ZM).seqType()),
      ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FOR_EACH(FnForEach.class, "for-each(items,function)",
      arg(ITEM_ZM, FuncType.get(ITEM_ZM, ITEM).seqType()), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FOR_EACH_PAIR(FnForEachPair.class, "for-each-pair(items1,items2,function)",
      arg(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM, ITEM).seqType()),
      ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FORMAT_DATE(FnFormatDate.class, "format-date(date,picture,[language,calendar,place])",
      arg(DAT_ZO, STR, STR_ZO, STR_ZO, STR_ZO), STR_ZO),
  /** XQuery function. */
  FORMAT_DATETIME(FnFormatDateTime.class,
      "format-dateTime(number,picture,[language,calendar,place])",
      arg(DTM_ZO, STR, STR_ZO, STR_ZO, STR_ZO), STR_ZO),
  /** XQuery function. */
  FORMAT_INTEGER(FnFormatInteger.class, "format-integer(number,picture[,language])",
      arg(ITR_ZO, STR, STR), STR),
  /** XQuery function. */
  FORMAT_NUMBER(FnFormatNumber.class, "format-number(number,picture[,format])",
      arg(ITR_ZO, STR, STR_ZO), STR),
  /** XQuery function. */
  FORMAT_TIME(FnFormatTime.class, "format-time(number,picture,[language,calendar,place])",
      arg(TIM_ZO, STR, STR_ZO, STR_ZO, STR_ZO), STR_ZO),
  /** XQuery function. */
  FUNCTION_ARITY(FnFunctionArity.class, "function-arity(function)", arg(FUN_O), ITR),
  /** XQuery function. */
  FUNCTION_LOOKUP(FnFunctionLookup.class, "function-lookup(name,arity)",
      arg(QNM, ITR), FUN_OZ, flag(CTX, FCS, NDT, HOF)),
  /** XQuery function. */
  FUNCTION_NAME(FnFunctionName.class, "function-name(function)", arg(FUN_O), QNM_ZO),
  /** XQuery function. */
  GENERATE_ID(FnGenerateId.class, "generate-id([node])", arg(NOD_ZO), STR),
  /** XQuery function. */
  HAS_CHILDREN(FnHasChildren.class, "has-children([node])", arg(NOD_ZM), BLN),
  /** XQuery function. */
  HEAD(FnHead.class, "head(items)", arg(ITEM_ZM), ITEM_ZO),
  /** XQuery function. */
  HOURS_FROM_DATETIME(FnHoursFromDateTime.class, "hours-from-dateTime(datetime)",
      arg(DTM_ZO), ITR_ZO),
  /** XQuery function. */
  HOURS_FROM_DURATION(FnHoursFromDuration.class, "hours-from-duration(duration)",
      arg(DUR_ZO), ITR_ZO),
  /** XQuery function. */
  HOURS_FROM_TIME(FnHoursFromTime.class, "hours-from-time(time)", arg(TIM_ZO), ITR_ZO),
  /** XQuery function. */
  ID(FnId.class, "id(ids[,node])", arg(STR_ZM, NOD), ELM_ZM),
  /** XQuery function. */
  IDREF(FnIdref.class, "idref(ids[,node])", arg(STR_ZM, NOD), NOD_ZM),
  /** XQuery function. */
  IMPLICIT_TIMEZONE(FnImplicitTimezone.class, "implicit-timezone()", arg(), DTD),
  /** XQuery function. */
  IN_SCOPE_PREFIXES(FnInScopePrefixes.class, "in-scope-prefixes(elem)", arg(ELM), STR_ZM),
  /** XQuery function. */
  INDEX_OF(FnIndexOf.class, "index-of(items,item[,collation])", arg(AAT_ZM, AAT, STR), ITR_ZM),
  /** XQuery function. */
  INNERMOST(FnInnermost.class, "innermost(nodes)", arg(NOD_ZM), NOD_ZM),
  /** XQuery function. */
  INSERT_BEFORE(FnInsertBefore.class, "insert-before(items,pos,insert)",
      arg(ITEM_ZM, ITR, ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  IRI_TO_URI(FnIriToUri.class, "iri-to-uri(string)", arg(STR_ZO), STR),
  /** XQuery function. */
  LANG(FnLang.class, "lang(ids[,node])", arg(STR_ZO, NOD), BLN),
  /** XQuery function. */
  LAST(FnLast.class, "last()", arg(), ITR, flag(FCS, CTX)),
  /** XQuery function. */
  LOCAL_NAME(FnLocalName.class, "local-name([node])", arg(NOD_ZO), STR),
  /** XQuery function. */
  LOCAL_NAME_FROM_QNAME(FnLocalNameFromQName.class, "local-name-from-QName(qname)",
      arg(QNM_ZO), NCN_ZO),
  /** XQuery function. */
  LOWER_CASE(FnLowerCase.class, "lower-case(string)", arg(STR_ZO), STR),
  /** XQuery function. */
  MATCHES(FnMatches.class, "matches(string,pattern[,mod])", arg(STR_ZO, STR, STR), BLN),
  /** XQuery function. */
  MAX(FnMax.class, "max(items[,collation])", arg(AAT_ZM, STR), AAT_ZO),
  /** XQuery function. */
  MIN(FnMin.class, "min(items[,collation])", arg(AAT_ZM, STR), AAT_ZO),
  /** XQuery function. */
  MINUTES_FROM_DATETIME(FnMinutesFromDateTime.class, "minutes-from-dateTime(datetime)",
      arg(DTM_ZO), ITR_ZO),
  /** XQuery function. */
  MINUTES_FROM_DURATION(FnMinutesFromDuration.class, "minutes-from-duration(duration)",
      arg(DUR_ZO), ITR_ZO),
  /** XQuery function. */
  MINUTES_FROM_TIME(FnMinutesFromTime.class, "minutes-from-time(time)", arg(TIM_ZO), ITR_ZO),
  /** XQuery function. */
  MONTH_FROM_DATE(FnMonthFromDate.class, "month-from-date(date)", arg(DAT_ZO), ITR_ZO),
  /** XQuery function. */
  MONTH_FROM_DATETIME(FnMonthFromDateTime.class, "month-from-dateTime(datetime)",
      arg(DTM_ZO), ITR_ZO),
  /** XQuery function. */
  MONTHS_FROM_DURATION(FnMonthsFromDuration.class, "months-from-duration(duration)",
      arg(DUR_ZO), ITR_ZO),
  /** XQuery function. */
  NAME(FnName.class, "name([node])", arg(NOD_ZO), STR),
  /** XQuery function. */
  NAMESPACE_URI(FnNamespaceUri.class, "namespace-uri([node])", arg(NOD_ZO), URI),
  /** XQuery function. */
  NAMESPACE_URI_FOR_PREFIX(FnNamespaceUriForPrefix.class, "namespace-uri-for-prefix(pref,elem)",
      arg(STR_ZO, ELM), URI_ZO),
  /** XQuery function. */
  NAMESPACE_URI_FROM_QNAME(FnNamespaceUriFromQName.class, "namespace-uri-from-QName(qname)",
      arg(QNM_ZO), URI_ZO),
  /** XQuery function. */
  NILLED(FnNilled.class, "nilled([node])", arg(NOD_ZO), BLN_ZO),
  /** XQuery function. */
  NODE_NAME(FnNodeName.class, "node-name([node])", arg(NOD_ZO), QNM_ZO),
  /** XQuery function. */
  NORMALIZE_SPACE(FnNormalizeSpace.class, "normalize-space([string])", arg(STR_ZO), STR),
  /** XQuery function. */
  NORMALIZE_UNICODE(FnNormalizeUnicode.class, "normalize-unicode(string[,form])",
      arg(STR_ZO, STR), STR),
  /** XQuery function. */
  NOT(FnNot.class, "not(items)", arg(ITEM_ZM), BLN),
  /** XQuery function. */
  NUMBER(FnNumber.class, "number([item])", arg(AAT_ZO), DBL),
  /** XQuery function. */
  ONE_OR_MORE(FnOneOrMore.class, "one-or-more(items)", arg(ITEM_ZM), ITEM_OM),
  /** XQuery function. */
  OUTERMOST(FnOutermost.class, "outermost(nodes)", arg(NOD_ZM), NOD_ZM),
  /** XQuery function. */
  PARSE_IETF_DATE(FnParseIetfDate.class, "parse-ietf-date(string)", arg(STR_ZO), DTM_ZO),
  /** XQuery function. */
  PARSE_XML(FnParseXml.class, "parse-xml(string)", arg(STR_ZO), DOC_O, flag(CNS)),
  /** XQuery function. */
  PARSE_XML_FRAGMENT(FnParseXmlFragment.class, "parse-xml-fragment(string)", arg(STR_ZO), DOC_ZO),
  /** XQuery function. */
  PATH(FnPath.class, "path([node])", arg(NOD_ZO), STR_ZO),
  /** XQuery function. */
  POSITION(FnPosition.class, "position()", arg(), ITR, flag(FCS, CTX)),
  /** XQuery function. */
  PREFIX_FROM_QNAME(FnPrefixFromQName.class, "prefix-from-QName(qname)", arg(QNM_ZO), NCN_ZO),
  /** XQuery function. */
  PUT(FnPut.class, "put(node,uri)", arg(NOD, STR_ZO), EMP, flag(UPD, NDT)),
  /** XQuery function. */
  QNAME(FnQName.class, "QName(uri,name)", arg(STR_ZO, STR), QNM),
  /** XQuery function. */
  REMOVE(FnRemove.class, "remove(items,pos)", arg(ITEM_ZM, ITR), ITEM_ZM),
  /** XQuery function. */
  REPLACE(FnReplace.class, "replace(string,pattern,replace[,mod])",
      arg(STR_ZO, STR, STR, STR), STR),
  /** XQuery function. */
  RESOLVE_QNAME(FnResolveQName.class, "resolve-QName(name,base)", arg(STR_ZO, ELM), QNM_ZO),
  /** XQuery function. */
  RESOLVE_URI(FnResolveUri.class, "resolve-uri(name[,elem])", arg(STR_ZO, STR), URI_ZO),
  /** XQuery function. */
  REVERSE(FnReverse.class, "reverse(items)", arg(ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  ROOT(FnRoot.class, "root([node])", arg(NOD_ZO), NOD_ZO),
  /** XQuery function. */
  ROUND(FnRound.class, "round(num[,prec])", arg(AAT_ZO, ITR), AAT_ZO),
  /** XQuery function. */
  ROUND_HALF_TO_EVEN(FnRoundHalfToEven.class, "round-half-to-even(num[,prec])",
      arg(AAT_ZO, ITR), AAT_ZO),
  /** XQuery function. */
  SECONDS_FROM_DATETIME(FnSecondsFromDateTime.class, "seconds-from-dateTime(datetime)",
      arg(DTM_ZO), DEC_ZO),
  /** XQuery function. */
  SECONDS_FROM_DURATION(FnSecondsFromDuration.class, "seconds-from-duration(duration)",
      arg(DUR_ZO), DEC_ZO),
  /** XQuery function. */
  SECONDS_FROM_TIME(FnSecondsFromTime.class, "seconds-from-time(time)", arg(TIM_ZO), DEC_ZO),
  /** XQuery function. */
  SERIALIZE(FnSerialize.class, "serialize(items[,params])", arg(ITEM_ZM, ITEM_ZO), STR),
  /** XQuery function. */
  STARTS_WITH(FnStartsWith.class, "starts-with(string,sub[,collation])",
      arg(STR_ZO, STR_ZO, STR), BLN),
  /** XQuery function. */
  STATIC_BASE_URI(FnStaticBaseUri.class, "static-base-uri()", arg(), URI_ZO),
  /** XQuery function. */
  STRING(FnString.class, "string([item])", arg(ITEM_ZO), STR),
  /** XQuery function. */
  STRING_JOIN(FnStringJoin.class, "string-join(strings[,sep])", arg(STR_ZM, STR), STR),
  /** XQuery function. */
  STRING_LENGTH(FnStringLength.class, "string-length([string])", arg(STR_ZO), ITR),
  /** XQuery function. */
  STRING_TO_CODEPOINTS(FnStringToCodepoints.class, "string-to-codepoints(string)",
      arg(STR_ZO), ITR_ZM),
  /** XQuery function. */
  SUBSEQUENCE(FnSubsequence.class, "subsequence(items,start[,len])",
      arg(ITEM_ZM, DBL, DBL), ITEM_ZM),
  /** XQuery function. */
  SUBSTRING(FnSubstring.class, "substring(string,start[,len])", arg(STR_ZO, DBL, DBL), STR),
  /** XQuery function. */
  SUBSTRING_AFTER(FnSubstringAfter.class, "substring-after(string,sub[,collation])",
      arg(STR_ZO, STR_ZO, STR), STR),
  /** XQuery function. */
  SUBSTRING_BEFORE(FnSubstringBefore.class, "substring-before(string,sub[,collation])",
      arg(STR_ZO, STR_ZO, STR), STR),
  /** XQuery function. */
  SUM(FnSum.class, "sum(items[,zero])", arg(AAT_ZM, AAT_ZO), AAT_ZO),
  /** XQuery function. */
  TAIL(FnTail.class, "tail(items)", arg(ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  TIMEZONE_FROM_DATE(FnTimezoneFromDate.class, "timezone-from-date(date)", arg(DAT_ZO), DTD_ZO),
  /** XQuery function. */
  TIMEZONE_FROM_DATETIME(FnTimezoneFromDateTime.class, "timezone-from-dateTime(dateTime)",
      arg(DTM_ZO), DTD_ZO),
  /** XQuery function. */
  TIMEZONE_FROM_TIME(FnTimezoneFromTime.class, "timezone-from-time(time)", arg(TIM_ZO), DTD_ZO),
  /** XQuery function. */
  TOKENIZE(FnTokenize.class, "tokenize(string[,pattern[,mod]])", arg(STR_ZO, STR, STR), STR_ZM),
  /** XQuery function. */
  TRACE(FnTrace.class, "trace(value[,label])", arg(ITEM_ZM, STR), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  TRANSLATE(FnTranslate.class, "translate(string,map,trans)", arg(STR_ZO, STR, STR), STR),
  /** XQuery function. */
  TRUE(FnTrue.class, "true()", arg(), BLN),
  /** XQuery function. */
  UNORDERED(FnUnordered.class, "unordered(items)", arg(ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  UNPARSED_TEXT(FnUnparsedText.class, "unparsed-text(uri[,encoding])", arg(STR_ZO, STR), STR_ZO),
  /** XQuery function. */
  UNPARSED_TEXT_AVAILABLE(FnUnparsedTextAvailable.class, "unparsed-text-available(uri[,encoding])",
      arg(STR_ZO, STR), BLN),
  /** XQuery function. */
  UNPARSED_TEXT_LINES(FnUnparsedTextLines.class, "unparsed-text-lines(uri[,encoding])",
      arg(STR_ZO, STR), STR_ZM),
  /** XQuery function. */
  UPPER_CASE(FnUpperCase.class, "upper-case(string)", arg(STR_ZO), STR),
  /** XQuery function. */
  URI_COLLECTION(FnUriCollection.class, "uri-collection([uri])", arg(STR_ZO), URI_ZM),
  /** XQuery function. */
  YEAR_FROM_DATE(FnYearFromDate.class, "year-from-date(date)", arg(DAT_ZO), ITR_ZO),
  /** XQuery function. */
  YEAR_FROM_DATETIME(FnYearFromDateTime.class, "year-from-dateTime(datetime)", arg(DTM_ZO), ITR_ZO),
  /** XQuery function. */
  YEARS_FROM_DURATION(FnYearsFromDuration.class, "years-from-duration(duration)",
      arg(DUR_ZO), ITR_ZO),
  /** XQuery function. */
  ZERO_OR_ONE(FnZeroOrOne.class, "zero-or-one(items)", arg(ITEM_ZM), ITEM_ZO),

  /* Map Module. */

  /** XQuery function. */
  _MAP_NEW(MapNew.class, "new([maps])", arg(MAP_ZM), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_MERGE(MapMerge.class, "merge(maps)", arg(MAP_ZM), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_PUT(MapPut.class, "put(map,key,value)", arg(MAP_O, AAT, ITEM_ZM), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_ENTRY(MapEntry.class, "entry(key,value)", arg(AAT, ITEM_ZM), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_GET(MapGet.class, "get(map,key)", arg(MAP_O, AAT), ITEM_ZM, MAP_URI),
  /** XQuery function. */
  _MAP_CONTAINS(MapContains.class, "contains(map,key)", arg(MAP_O, AAT), BLN, MAP_URI),
  /** XQuery function. */
  _MAP_REMOVE(MapRemove.class, "remove(map,key)", arg(MAP_O, AAT), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_SIZE(MapSize.class, "size(map)", arg(MAP_O), ITR, MAP_URI),
  /** XQuery function. */
  _MAP_KEYS(MapKeys.class, "keys(map)", arg(MAP_O), AAT_ZM, MAP_URI),
  /** XQuery function. */
  _MAP_FOR_EACH(MapForEach.class, "for-each(map,function)",
      arg(MAP_O, FuncType.get(ITEM_ZM, AAT, ITEM_ZM).seqType()), ITEM_ZM, flag(HOF), MAP_URI),
  /** XQuery function. */
  _MAP_SERIALIZE(MapSerialize.class, "serialize(map)", arg(MAP_O), STR, MAP_URI),

  /* Array Module. */

  /** XQuery function. */
  _ARRAY_SIZE(ArraySize.class, "size(array)", arg(ARRAY_O), ITR, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_GET(ArrayGet.class, "get(array,pos)", arg(ARRAY_O, ITR), ITEM_ZM, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_APPEND(ArrayAppend.class, "append(array,value)",
      arg(ARRAY_O, ITEM_ZM), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_SUBARRAY(ArraySubarray.class, "subarray(array,pos[,length])", arg(ARRAY_O, ITR, ITR),
      ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_REMOVE(ArrayRemove.class, "remove(array,pos)", arg(ARRAY_O, ITR), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_INSERT_BEFORE(ArrayInsertBefore.class, "insert-before(array,pos,value)",
      arg(ARRAY_O, ITR, ITEM_ZO), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_HEAD(ArrayHead.class, "head(array)", arg(ARRAY_O), ITEM_ZM, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_TAIL(ArrayTail.class, "tail(array)", arg(ARRAY_O), ITEM_ZM, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_REVERSE(ArrayReverse.class, "reverse(array)", arg(ARRAY_O), ITEM_ZM, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_JOIN(ArrayJoin.class, "join(array)", arg(ARRAY_ZM), ITEM_ZM, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FOR_EACH(ArrayForEach.class, "for-each(array,function)",
      arg(ARRAY_O, FuncType.get(ITEM_ZM, ITEM_ZM).seqType()), ARRAY_O, flag(HOF),
      ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FILTER(ArrayFilter.class, "filter(array,function)",
      arg(ARRAY_O, FuncType.get(BLN, ITEM_ZM).seqType()), ARRAY_O, flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FOLD_LEFT(ArrayFoldLeft.class, "fold-left(array,zero,function)",
      arg(ARRAY_O, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM_ZM).seqType()), ITEM_ZM,
      flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FOLD_RIGHT(ArrayFoldRight.class, "fold-right(array,zero,function)",
      arg(ARRAY_O, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM_ZM).seqType()), ITEM_ZM,
      flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FOR_EACH_PAIR(ArrayForEachPair.class, "for-each-pair(array1,array2,function)",
      arg(ARRAY_O, ARRAY_O, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM_ZM).seqType()), ARRAY_O,
      flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_SERIALIZE(ArraySerialize.class, "serialize(array)", arg(ARRAY_O), STR, ARRAY_URI),

  /* Math Module. */

  /** XQuery function. */
  _MATH_SQRT(MathSqrt.class, "sqrt(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_SIN(MathSin.class, "sin(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_COS(MathCos.class, "cos(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_TAN(MathTan.class, "tan(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_ASIN(MathAsin.class, "asin(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_ACOS(MathAcos.class, "acos(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_ATAN(MathAtan.class, "atan(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_ATAN2(MathAtan2.class, "atan2(number1,number2)", arg(DBL, DBL), DBL, MATH_URI),
  /** XQuery function. */
  _MATH_POW(MathPow.class, "pow(number1,number2)", arg(DBL_ZO, ITR), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_EXP(MathExp.class, "exp(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_EXP10(MathExp10.class, "exp10(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_LOG(MathLog.class, "log(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_LOG10(MathLog10.class, "log10(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_PI(MathPi.class, "pi()", arg(), DBL, MATH_URI),

  /** XQuery function. */
  _MATH_SINH(MathSinh.class, "sinh(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_COSH(MathCosh.class, "cosh(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_TANH(MathTanh.class, "tanh(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_CRC32(MathCrc32.class, "crc32(string)", arg(STR), HEX, MATH_URI),
  /** XQuery function. */
  _MATH_E(MathE.class, "e()", arg(), DBL, MATH_URI),

  /* Admin Module. */

  /** XQuery function. */
  _ADMIN_USERS(AdminUsers.class, "users([database])", arg(STR), ELM_ZM, flag(NDT), ADMIN_URI),
  /** XQuery function. */
  _ADMIN_SESSIONS(AdminSessions.class, "sessions()", arg(), ELM_ZM, flag(NDT), ADMIN_URI),
  /** XQuery function. */
  _ADMIN_LOGS(AdminLogs.class, "logs([date[,merge]])", arg(STR, BLN), ELM_ZM, flag(NDT), ADMIN_URI),

  /* Archive Module. */

  /** XQuery function. */
  _ARCHIVE_CREATE(ArchiveCreate.class, "create(entries,contents[,options])",
      arg(ITEM_ZM, ITEM_ZM, ITEM), B64, ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_ENTRIES(ArchiveEntries.class, "entries(archive)", arg(B64), ELM_ZM, ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_EXTRACT_TEXT(ArchiveExtractText.class, "extract-text(archive[,entries[,encoding]])",
      arg(B64, ITEM_ZM, STR), STR_ZM, ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_EXTRACT_BINARY(ArchiveExtractBinary.class, "extract-binary(archive[,entries])",
      arg(B64, ITEM_ZM), B64_ZM, ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_UPDATE(ArchiveUpdate.class, "update(archive,entries,contents)",
      arg(B64, ITEM_ZM, ITEM_ZM), B64, ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_DELETE(ArchiveDelete.class, "delete(archive,entries)",
      arg(B64, ITEM_ZM), B64, ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_OPTIONS(ArchiveOptions.class, "options(archive)", arg(B64), ELM, ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_WRITE(ArchiveWrite.class, "write(path,archive[,entries])",
      arg(STR, B64, ITEM_ZM), EMP, ARCHIVE_URI),

  /* Binary Module. */

  /** XQuery function. */
  _BIN_HEX(BinHex.class, "hex(string)", arg(STR_ZO), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_BIN(BinBin.class, "bin(string)", arg(STR_ZO), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_OCTAL(BinOctal.class, "octal(string)", arg(STR_ZO), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_TO_OCTETS(BinToOctets.class, "to-octets(binary)", arg(B64_ZO), ITR_ZM, BIN_URI),
  /** XQuery function. */
  _BIN_FROM_OCTETS(BinFromOctets.class, "from-octets(integers)",
      arg(ITR_ZM), B64, BIN_URI),
  /** XQuery function. */
  _BIN_LENGTH(BinLength.class, "length(binary)", arg(B64), ITR, BIN_URI),
  /** XQuery function. */
  _BIN_PART(BinPart.class, "part(binary,offset[,size])",
      arg(B64_ZO, ITR, ITR), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_JOIN(BinJoin.class, "join(binaries)", arg(B64_ZM), B64, BIN_URI),
  /** XQuery function. */
  _BIN_INSERT_BEFORE(BinInsertBefore.class, "insert-before(binary,offset,extra)",
      arg(B64_ZO, ITR, B64_ZO), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_PAD_LEFT(BinPadLeft.class, "pad-left(binary,size[,octet])",
      arg(B64_ZO, ITR, ITR), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_PAD_RIGHT(BinPadRight.class, "pad-right(binary,size[,octet])",
      arg(B64_ZO, ITR, ITR), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_FIND(BinFind.class, "find(binary,offset,search)",
      arg(B64_ZO, ITR, B64_ZO), ITR_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_DECODE_STRING(BinDecodeString.class, "decode-string(binary[,encoding[,offset[,size]]])",
      arg(B64_ZO, STR, ITR, ITR), STR_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_ENCODE_STRING(BinEncodeString.class, "encode-string(string[,encoding])",
      arg(STR_ZO, STR), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_PACK_DOUBLE(BinPackDouble.class, "pack-double(double[,order])", arg(DBL, STR), B64, BIN_URI),
  /** XQuery function. */
  _BIN_PACK_FLOAT(BinPackFloat.class, "pack-float(float[,order])", arg(FLT, STR), B64, BIN_URI),
  /** XQuery function. */
  _BIN_PACK_INTEGER(BinPackInteger.class, "pack-integer(integer,size[,order])",
      arg(ITR, ITR, STR), B64, BIN_URI),
  /** XQuery function. */
  _BIN_UNPACK_DOUBLE(BinUnpackDouble.class, "unpack-double(binary,offset[,order])",
      arg(B64, ITR, STR), DBL, BIN_URI),
  /** XQuery function. */
  _BIN_UNPACK_FLOAT(BinUnpackFloat.class, "unpack-float(binary,offset[,order])",
      arg(B64, ITR, STR), FLT, BIN_URI),
  /** XQuery function. */
  _BIN_UNPACK_INTEGER(BinUnpackInteger.class, "unpack-integer(binary,offset,size[,order])",
      arg(B64, ITR, ITR, STR), ITR, BIN_URI),
  /** XQuery function. */
  _BIN_UNPACK_UNSIGNED_INTEGER(BinUnpackUnsignedInteger.class,
      "unpack-unsigned-integer(binary,offset,size[,order])",
      arg(B64, ITR, ITR, STR), ITR, BIN_URI),
  /** XQuery function. */
  _BIN_OR(BinOr.class, "or(binary1,binary2)", arg(B64_ZO, B64_ZO), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_XOR(BinXor.class, "xor(binary1,binary2)", arg(B64_ZO, B64_ZO), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_AND(BinAnd.class, "and(binary1,binary2)", arg(B64_ZO, B64_ZO), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_NOT(BinNot.class, "not(binary)", arg(B64_ZO), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_SHIFT(BinShift.class, "shift(binary,by)", arg(B64_ZO, ITR), B64_ZO, BIN_URI),

  /* Client Module. */

  /** XQuery function. */
  _CLIENT_CONNECT(ClientConnect.class, "connect(url,port,user,password)",
      arg(STR, ITR, STR, STR), URI, flag(NDT), CLIENT_URI),
  /** XQuery function. */
  _CLIENT_EXECUTE(ClientExecute.class, "execute(id,command)", arg(URI, STR), STR, flag(NDT),
      CLIENT_URI),
  /** XQuery function. */
  _CLIENT_INFO(ClientInfo.class, "info(id)", arg(URI), STR, flag(NDT), CLIENT_URI),
  /** XQuery function. */
  _CLIENT_QUERY(ClientQuery.class, "query(id,query[,bindings])",
      arg(URI, STR, ITEM), ITEM_ZO, flag(NDT), CLIENT_URI),
  /** XQuery function. */
  _CLIENT_CLOSE(ClientClose.class, "close(id)", arg(URI), EMP, flag(NDT), CLIENT_URI),

  /* Conversion Module. */

  /** XQuery function. */
  _CONVERT_INTEGER_TO_BASE(ConvertIntegerToBase.class, "integer-to-base(number,base)",
      arg(ITR, ITR), STR, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGER_FROM_BASE(ConvertIntegerFromBase.class, "integer-from-base(string,base)",
      arg(STR, ITR), ITR, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_BINARY_TO_BYTES(ConvertBinaryToBytes.class, "binary-to-bytes(binary)", arg(ITEM), BYT_ZM,
      CONVERT_URI),
  /** XQuery function. */
  _CONVERT_BINARY_TO_STRING(ConvertBinaryToString.class, "binary-to-string(binary[,encoding])",
      arg(ITEM, STR), STR, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_BYTES_TO_HEX(ConvertBytesToHex.class, "bytes-to-hex(bytes)", arg(BYT_ZM), HEX,
      CONVERT_URI),
  /** XQuery function. */
  _CONVERT_BYTES_TO_BASE64(ConvertBytesToBase64.class, "bytes-to-base64(bytes)", arg(BYT_ZM), B64,
      CONVERT_URI),
  /** XQuery function. */
  _CONVERT_STRING_TO_BASE64(ConvertStringToBase64.class, "string-to-base64(string[,encoding])",
      arg(STR, STR), B64, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_STRING_TO_HEX(ConvertStringToHex.class, "string-to-hex(string[,encoding])",
      arg(STR, STR), HEX, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGER_TO_DATETIME(ConvertIntegerToDateTime.class, "integer-to-dateTime(ms)", arg(ITR),
      DTM, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_DATETIME_TO_INTEGER(ConvertDateTimeToInteger.class, "dateTime-to-integer(date)",
      arg(DTM), ITR, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGER_TO_DAYTIME(ConvertIntegerToDayTime.class, "integer-to-dayTime(ms)", arg(ITR),
      DTD, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_DAYTIME_TO_INTEGER(ConvertDayTimeToInteger.class, "dayTime-to-integer(duration)",
      arg(DTD), ITR, CONVERT_URI),

  /* FNCrypto functions (EXPath Cryptographic module). */

  /** XQuery function. */
  _CRYPTO_HMAC(CryptoHmac.class, "hmac(message,key,algorithm[,encoding])",
      arg(STR, STR, STR, STR_ZO), STR, CRYPTO_URI),
  /** XQuery function. */
  _CRYPTO_ENCRYPT(CryptoEncrypt.class, "encrypt(input,encryption,key,algorithm)",
      arg(STR, STR, STR, STR), STR, CRYPTO_URI),
  /** XQuery function. */
  _CRYPTO_DECRYPT(CryptoDecrypt.class, "decrypt(input,type,key,algorithm)",
      arg(STR, STR, STR, STR), STR, CRYPTO_URI),
  /** XQuery function. */
  _CRYPTO_GENERATE_SIGNATURE(CryptoGenerateSignature.class, "generate-signature" +
      "(input,canonicalization,digest,signature,prefix,type[,item1][,item2])",
      arg(NOD, STR, STR, STR, STR, STR, ITEM_ZO, ITEM_ZO), NOD, CRYPTO_URI),
  /** XQuery function. */
  _CRYPTO_VALIDATE_SIGNATURE(CryptoValidateSignature.class, "validate-signature(node)",
      arg(NOD), BLN, CRYPTO_URI),

  /* CSV Module. */

  /** XQuery function. */
  _CSV_PARSE(CsvParse.class, "parse(string[,config])", arg(STR, MAP_O), ITEM, CSV_URI),
  /** XQuery function. */
  _CSV_SERIALIZE(CsvSerialize.class, "serialize(item[,params])", arg(ITEM_ZO, ITEM_ZO), STR,
      CSV_URI),

  /* Database Module. */

  /** XQuery function. */
  _DB_OPEN(DbOpen.class, "open(database[,path])", arg(STR, STR), NOD_ZM, DB_URI),
  /** XQuery function. */
  _DB_OPEN_PRE(DbOpenPre.class, "open-pre(database,pre)", arg(STR, ITR), NOD_ZM, DB_URI),
  /** XQuery function. */
  _DB_OPEN_ID(DbOpenId.class, "open-id(database,id)", arg(STR, ITR), NOD_ZM, DB_URI),
  /** XQuery function. */
  _DB_TEXT(DbText.class, "text(database,string)", arg(STR, ITEM), NOD_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_TEXT_RANGE(DbTextRange.class, "text-range(database,from,to)",
      arg(STR, ITEM, ITEM), NOD_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_ATTRIBUTE(DbAttribute.class, "attribute(database,string[,name])",
      arg(STR, ITEM, STR), NOD_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_ATTRIBUTE_RANGE(DbAttributeRange.class, "attribute-range(database,from,to[,name])",
      arg(STR, ITEM, ITEM, STR), NOD_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_LIST(DbList.class, "list([database[,path]])", arg(STR, STR), STR_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_LIST_DETAILS(DbListDetails.class, "list-details([database[,path]])", arg(STR, STR), ELM_ZM,
      flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_BACKUPS(DbBackups.class, "backups([database])", arg(ITEM), ELM_ZM, DB_URI),
  /** XQuery function. */
  _DB_CREATE_BACKUP(DbCreateBackup.class, "create-backup(database)", arg(STR), EMP,
      flag(UPD, NDT), DB_URI),
  /** XQuery function. */
  _DB_COPY(DbCopy.class, "copy(database, new-name)", arg(STR, STR), EMP, flag(UPD, NDT),
      DB_URI),
  /** XQuery function. */
  _DB_ALTER(DbAlter.class, "alter(database, new-name)", arg(STR, STR), EMP, flag(UPD, NDT), DB_URI),
  /** XQuery function. */
  _DB_DROP_BACKUP(DbDropBackup.class, "drop-backup(name)", arg(STR), EMP, flag(UPD, NDT), DB_URI),
  /** XQuery function. */
  _DB_RESTORE(DbRestore.class, "restore(backup)", arg(STR), EMP, flag(UPD, NDT), DB_URI),
  /** XQuery function. */
  _DB_SYSTEM(DbSystem.class, "system()", arg(), STR, DB_URI),
  /** XQuery function. */
  _DB_INFO(DbInfo.class, "info(database)", arg(ITEM), STR, DB_URI),
  /** XQuery function. */
  _DB_NODE_ID(DbNodeId.class, "node-id(nodes)", arg(NOD_ZM), ITR_ZM, DB_URI),
  /** XQuery function. */
  _DB_NODE_PRE(DbNodePre.class, "node-pre(nodes)", arg(NOD_ZM), ITR_ZM, DB_URI),
  /** XQuery function. */
  _DB_EVENT(DbEvent.class, "event(name,query)", arg(STR, ITEM_ZM), EMP, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_OUTPUT(DbOutput.class, "output(result)", arg(ITEM_ZM), EMP, flag(UPD, NDT), DB_URI),
  /** XQuery function. */
  _DB_ADD(DbAdd.class, "add(database,input[,path[,options]])",
      arg(STR, NOD, STR, ITEM), EMP, flag(UPD, NDT), DB_URI),
  /** XQuery function. */
  _DB_DELETE(DbDelete.class, "delete(database,path)", arg(STR, STR), EMP, flag(UPD, NDT), DB_URI),
  /** XQuery function. */
  _DB_CREATE(DbCreate.class, "create(name[,inputs[,paths[,options]]])",
      arg(STR, ITEM_ZM, STR_ZM, ITEM), EMP, flag(UPD, NDT), DB_URI),
  /** XQuery function. */
  _DB_DROP(DbDrop.class, "drop(database)", arg(ITEM), EMP, flag(UPD, NDT), DB_URI),
  /** XQuery function. */
  _DB_RENAME(DbRename.class, "rename(database,path,new-path)", arg(STR, STR, STR), EMP,
      flag(UPD, NDT), DB_URI),
  /** XQuery function. */
  _DB_REPLACE(DbReplace.class, "replace(database,path,input[,options])",
      arg(STR, STR, ITEM, ITEM), EMP, flag(UPD, NDT), DB_URI),
  /** XQuery function. */
  _DB_OPTIMIZE(DbOptimize.class, "optimize(database[,all[,options]])",
      arg(STR, BLN, ITEM), EMP, flag(UPD, NDT), DB_URI),
  /** XQuery function. */
  _DB_RETRIEVE(DbRetrieve.class, "retrieve(database,path)", arg(STR, STR), B64, DB_URI),
  /** XQuery function. */
  _DB_STORE(DbStore.class, "store(database,path,input)", arg(STR, STR, ITEM), EMP, flag(UPD, NDT),
      DB_URI),
  /** XQuery function. */
  _DB_IS_XML(DbIsXml.class, "is-xml(database,path)", arg(STR, STR), BLN, DB_URI),
  /** XQuery function. */
  _DB_IS_RAW(DbIsRaw.class, "is-raw(database,path)", arg(STR, STR), BLN, DB_URI),
  /** XQuery function. */
  _DB_EXISTS(DbExists.class, "exists(database[,path])", arg(STR, STR), BLN, DB_URI),
  /** XQuery function. */
  _DB_CONTENT_TYPE(DbContentType.class, "content-type(database,path)", arg(STR, STR), STR, DB_URI),
  /** XQuery function. */
  _DB_FLUSH(DbFlush.class, "flush(database)", arg(ITEM), EMP, flag(UPD, NDT), DB_URI),
  /** XQuery function. */
  _DB_EXPORT(DbExport.class, "export(database,path[,param]])", arg(STR, STR, ITEM), EMP, flag(NDT),
      DB_URI),
  /** XQuery function. */
  _DB_NAME(DbName.class, "name(node)", arg(NOD), STR, DB_URI),
  /** XQuery function. */
  _DB_PATH(DbPath.class, "path(node)", arg(NOD), STR, DB_URI),

  /* Fetch Module. */

  /** XQuery function. */
  _FETCH_TEXT(FetchText.class, "text(uri[,encoding)", arg(STR, STR), STR, flag(NDT), FETCH_URI),
  /** XQuery function. */
  _FETCH_BINARY(FetchBinary.class, "binary(uri)", arg(STR), B64, flag(NDT), FETCH_URI),
  /** XQuery function. */
  _FETCH_CONTENT_TYPE(FetchContentType.class, "content-type(uri)", arg(STR), STR, flag(NDT),
      FETCH_URI),

  /* File Module. */

  /** XQuery function. */
  _FILE_PATH_SEPARATOR(FilePathSeparator.class, "path-separator()", arg(), STR, FILE_URI),
  /** XQuery function. */
  _FILE_DIR_SEPARATOR(FileDirSeparator.class, "dir-separator()", arg(), STR, FILE_URI),
  /** XQuery function. */
  _FILE_LINE_SEPARATOR(FileLineSeparator.class, "line-separator()", arg(), STR, FILE_URI),
  /** XQuery function. */
  _FILE_TEMP_DIR(FileTempDir.class, "temp-dir()", arg(), STR, FILE_URI),
  /** XQuery function. */
  _FILE_NAME(FileName.class, "name(path)", arg(STR), STR, FILE_URI),
  /** XQuery function. */
  _FILE_PARENT(FileParent.class, "parent(path)", arg(STR), STR_ZO, FILE_URI),
  /** XQuery function. */
  _FILE_PATH_TO_URI(FilePathToUri.class, "path-to-uri(path)", arg(STR), URI, FILE_URI),
  /** XQuery function. */
  _FILE_EXISTS(FileExists.class, "exists(path)", arg(STR), BLN, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_IS_DIR(FileIsDir.class, "is-dir(path)", arg(STR), BLN, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_IS_FILE(FileIsFile.class, "is-file(path)", arg(STR), BLN, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_LAST_MODIFIED(FileLastModified.class, "last-modified(path)",
      arg(STR), DTM, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_SIZE(FileSize.class, "size(path)", arg(STR), ITR, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_PATH_TO_NATIVE(FilePathToNative.class, "path-to-native(path)",
      arg(STR), STR, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_RESOLVE_PATH(FileResolvePath.class, "resolve-path(path)",
      arg(STR), STR, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_LIST(FileList.class, "list(path[,recursive[,pattern]])",
      arg(STR, BLN, STR), STR_ZM, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_CREATE_DIR(FileCreateDir.class, "create-dir(path)", arg(STR), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_CREATE_TEMP_DIR(FileCreateTempDir.class, "create-temp-dir(prefix,suffix[,dir])",
      arg(STR, STR, STR), STR, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_CREATE_TEMP_FILE(FileCreateTempFile.class, "create-temp-file(prefix,suffix[,dir])",
      arg(STR, STR, STR), STR, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_DELETE(FileDelete.class, "delete(path[,recursive])",
      arg(STR, BLN), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_READ_TEXT(FileReadText.class, "read-text(path[,encoding])",
      arg(STR, STR), STR, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_READ_TEXT_LINES(FileReadTextLines.class, "read-text-lines(path[,encoding])",
      arg(STR, STR), STR_ZM, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_READ_BINARY(FileReadBinary.class, "read-binary(path[,offset[,length]])",
      arg(STR, ITR, ITR), B64, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_WRITE(FileWrite.class, "write(path,data[,params])",
      arg(STR, ITEM_ZM, ITEM), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_WRITE_BINARY(FileWriteBinary.class, "write-binary(path,item[,offset])",
      arg(STR, BIN, ITR), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_WRITE_TEXT(FileWriteText.class, "write-text(path,text[,encoding])",
      arg(STR, STR, STR), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_WRITE_TEXT_LINES(FileWriteTextLines.class, "write-text-lines(path,texts[,encoding])",
      arg(STR, STR_ZM, STR), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_APPEND(FileAppend.class, "append(path,data[,params])",
      arg(STR, ITEM_ZM, ITEM), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_APPEND_BINARY(FileAppendBinary.class, "append-binary(path,item)",
      arg(STR, BIN), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_APPEND_TEXT(FileAppendText.class, "append-text(path,text[,encoding])",
      arg(STR, STR, STR), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_APPEND_TEXT_LINES(FileAppendTextLines.class, "append-text-lines(path,texts[,encoding])",
      arg(STR, STR_ZM, STR), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_COPY(FileCopy.class, "copy(source,target)", arg(STR, STR), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_MOVE(FileMove.class, "move(source,target)", arg(STR, STR), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_CURRENT_DIR(FileCurrentDir.class, "current-dir()", arg(), STR, FILE_URI),
  /** XQuery function. */
  _FILE_BASE_DIR(FileBaseDir.class, "base-dir()", arg(), STR, FILE_URI),
  /** XQuery function. */
  _FILE_CHILDREN(FileChildren.class, "children(path)", arg(STR), STR_ZM, flag(NDT), FILE_URI),

  /* Fulltext Module. */

  /** XQuery function. */
  _FT_CONTAINS(FtContains.class, "contains(input,terms[,options])",
      arg(ITEM, ITEM_ZM, ITEM), NOD_ZM, flag(NDT), FT_URI),
  /** XQuery function. */
  _FT_SEARCH(FtSearch.class, "search(database,terms[,options])",
      arg(STR, ITEM_ZM, ITEM), NOD_ZM, flag(NDT), FT_URI),
  /** XQuery function. */
  _FT_COUNT(FtCount.class, "count(nodes)", arg(NOD_ZM), ITR, FT_URI),
  /** XQuery function. */
  _FT_MARK(FtMark.class, "mark(nodes[,name])", arg(NOD_ZM, STR), NOD_ZM, FT_URI),
  /** XQuery function. */
  _FT_EXTRACT(FtExtract.class, "extract(nodes[,name[,length]])", arg(ITEM_ZM, STR, ITR), NOD_ZM,
      FT_URI),
  /** XQuery function. */
  _FT_SCORE(FtScore.class, "score(items)", arg(ITEM_ZM), DBL_ZM, FT_URI),
  /** XQuery function. */
  _FT_TOKENS(FtTokens.class, "tokens(database[,prefix])", arg(STR, STR), ITEM_ZM, flag(NDT),
      FT_URI),
  /** XQuery function. */
  _FT_TOKENIZE(FtTokenize.class, "tokenize(string)", arg(STR), STR_ZM, FT_URI),

  /* Hash Module. */

  /** XQuery function. */
  _HASH_MD5(HashMd5.class, "md5(value)", arg(AAT), B64, HASH_URI),
  /** XQuery function. */
  _HASH_SHA1(HashSha1.class, "sha1(value)", arg(AAT), B64, HASH_URI),
  /** XQuery function. */
  _HASH_SHA256(HashSha256.class, "sha256(value)", arg(AAT), B64, HASH_URI),
  /** XQuery function. */
  _HASH_HASH(HashHash.class, "hash(value,algorithm)", arg(AAT, STR), B64, HASH_URI),

  /* HOF Module. */

  /** XQuery function. */
  _HOF_SORT_WITH(HofSortWith.class, "sort-with(items,lt-fun)",
      arg(ITEM_ZM, FuncType.get(BLN, ITEM, ITEM).seqType()), ITEM_ZM, HOF_URI),
  /** XQuery function. */
  _HOF_ID(HofId.class, "id(value)", arg(ITEM_ZM), ITEM_ZM, HOF_URI),
  /** XQuery function. */
  _HOF_CONST(HofConst.class, "const(return,ignore)", arg(ITEM_ZM, ITEM_ZM), ITEM_ZM,
      HOF_URI),
  /** XQuery function. */
  _HOF_UNTIL(HofUntil.class, "until(pred,function,start)", arg(FuncType.get(BLN, ITEM_ZM).seqType(),
      FuncType.get(ITEM_ZM, ITEM_ZM).seqType(), ITEM_ZM), ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_FOLD_LEFT1(HofFoldLeft1.class, "fold-left1(non-empty-items,function)",
      arg(ITEM_OM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM).seqType()), ITEM_ZM, flag(HOF),
      HOF_URI),
  /** XQuery function. */
  _HOF_TOP_K_BY(HofTopKBy.class, "top-k-by(items,key-fun,k)",
      arg(ITEM_ZM, FuncType.arity(1).seqType(), ITR), ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_TOP_K_WITH(HofTopKWith.class, "top-k-with(items,less-than,k)",
      arg(ITEM_ZM, FuncType.get(BLN, ITEM_ZO, ITEM_ZO).seqType(), ITR), ITEM_ZM, flag(HOF),
      HOF_URI),

  /* HTML Module. */

  /** XQuery function. */
  _HTML_PARSER(HtmlParser.class, "parser()", arg(), STR, HTML_URI),
  /** XQuery function. */
  _HTML_PARSE(HtmlParse.class, "parse(input[,options)", arg(STR, ITEM), DOC_O, HTML_URI),

  /* Http Module. */

  /** XQuery function. */
  _HTTP_SEND_REQUEST(HttpSendRequest.class, "send-request(request[,href,[bodies]])",
      arg(NOD, STR_ZO, ITEM_ZM), ITEM_ZM, flag(NDT), HTTP_URI),

  /* Index Module. */

  /** XQuery function. */
  _INDEX_FACETS(IndexFacets.class, "facets(database[,type])", arg(STR, STR), DOC_O, flag(NDT),
      INDEX_URI),
  /** XQuery function. */
  _INDEX_TEXTS(IndexTexts.class, "texts(database[,prefix[,ascending]])",
      arg(STR, STR, BLN), NOD_ZM, flag(NDT), INDEX_URI),
  /** XQuery function. */
  _INDEX_ATTRIBUTES(IndexAttributes.class, "attributes(database[,prefix[,ascending]])",
      arg(STR, STR, BLN), NOD_ZM, flag(NDT), INDEX_URI),
  /** XQuery function. */
  _INDEX_ELEMENT_NAMES(IndexElementNames.class, "element-names(database)", arg(STR), NOD_ZM,
      INDEX_URI),
  /** XQuery function. */
  _INDEX_ATTRIBUTE_NAMES(IndexAttributeNames.class, "attribute-names(database)", arg(STR), NOD_ZM,
      INDEX_URI),

  /* Inspection Module. */

  /** XQuery function. */
  _INSPECT_FUNCTION(InspectFunction.class, "function(function)", arg(STR), ELM, INSPECT_URI),
  /** XQuery function. */
  _INSPECT_MODULE(InspectModule.class, "module(path)", arg(STR), ELM, INSPECT_URI),
  /** XQuery function. */
  _INSPECT_CONTEXT(InspectContext.class, "context()", arg(), ELM, INSPECT_URI),
  /** XQuery function. */
  _INSPECT_FUNCTIONS(InspectFunctions.class, "functions([uri])", arg(STR), FUN_ZM, flag(HOF),
      INSPECT_URI),
  /** XQuery function. */
  _INSPECT_XQDOC(InspectXqdoc.class, "xqdoc(path)", arg(STR), ELM, INSPECT_URI),

  /* JSON Module. */

  /** XQuery function. */
  _JSON_PARSE(JsonParse.class, "parse(string[,config])", arg(STR, MAP_O), ITEM, JSON_URI),
  /** XQuery function. */
  _JSON_SERIALIZE(JsonSerialize.class, "serialize(items[,params])", arg(ITEM_ZO, ITEM_ZO), STR,
      JSON_URI),

  /* Output Module. */

  /** XQuery function. */
  _OUT_NL(OutNl.class, "nl()", arg(), STR, OUT_URI),
  /** XQuery function. */
  _OUT_TAB(OutTab.class, "tab()", arg(), STR, OUT_URI),
  /** XQuery function. */
  _OUT_FORMAT(OutFormat.class, "format(format,item1[,...])", arg(STR, ITEM), STR, OUT_URI),

  /* Process Module. */

  /** XQuery function. */
  _PROC_SYSTEM(ProcSystem.class, "system(command[,args[,encoding]])",
      arg(STR, STR_ZM, STR), STR, flag(NDT), PROC_URI),
  /** XQuery function. */
  _PROC_EXECUTE(ProcExecute.class, "execute(command[,args[,encoding]]])",
      arg(STR, STR_ZM, STR), ELM, flag(NDT), PROC_URI),

  /* Profiling Module. */

  /** XQuery function. */
  _PROF_MEM(ProfMem.class, "mem(value[,cache[,label]])", arg(ITEM_ZM, BLN, STR), ITEM_ZM, flag(NDT),
      PROF_URI),
  /** XQuery function. */
  _PROF_TIME(ProfTime.class, "time(value[,cache[,label]])",
      arg(ITEM_ZM, BLN, STR), ITEM_ZM, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_SLEEP(ProfSleep.class, "sleep(ms)", arg(ITR), EMP, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_CURRENT_MS(ProfCurrentMs.class, "current-ms()", arg(), ITR, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_CURRENT_NS(ProfCurrentNs.class, "current-ns()", arg(), ITR, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_DUMP(ProfDump.class, "dump(value[,label])", arg(ITEM_ZM, STR), EMP, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_HUMAN(ProfHuman.class, "human(integer)", arg(ITR), STR, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_VOID(ProfVoid.class, "void(value)", arg(ITEM_ZM), EMP, flag(NDT), PROF_URI),

  /* Random Module. */

  /** XQuery function. */
  _RANDOM_DOUBLE(RandomDouble.class, "double()", arg(), DBL, flag(NDT), RANDOM_URI),
  /** XQuery function. */
  _RANDOM_INTEGER(RandomInteger.class, "integer([max])", arg(ITR), ITR, flag(NDT), RANDOM_URI),
  /** XQuery function. */
  _RANDOM_SEEDED_DOUBLE(RandomSeededDouble.class, "seeded-double(seed,num)",
      arg(ITR, ITR), ITEM_ZM, flag(NDT), RANDOM_URI),
  /** XQuery function. */
  _RANDOM_SEEDED_INTEGER(RandomSeededInteger.class, "seeded-integer(seed,num[,max])",
      arg(ITR, ITR, ITR), ITEM_ZM, flag(NDT), RANDOM_URI),
  /** XQuery function. */
  _RANDOM_GAUSSIAN(RandomGaussian.class, "gaussian(num)", arg(ITR), ITEM_ZM, flag(NDT), RANDOM_URI),
  /** XQuery function. */
  _RANDOM_UUID(RandomUuid.class, "uuid()", arg(), STR, flag(NDT), RANDOM_URI),

  /* Repository Module. */

  /** XQuery function. */
  _REPO_INSTALL(RepoInstall.class, "install(uri)", arg(STR), EMP, flag(NDT), REPO_URI),
  /** XQuery function. */
  _REPO_DELETE(RepoDelete.class, "delete(uri)", arg(STR), EMP, flag(NDT), REPO_URI),
  /** XQuery function. */
  _REPO_LIST(RepoList.class, "list()", arg(), STR_ZM, flag(NDT), REPO_URI),

  /* SQL Module. */

  /** XQuery function. */
  _SQL_INIT(SqlInit.class, "init(class)", arg(STR), EMP, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_CONNECT(SqlConnect.class, "connect(url[,user[,pass[,options]]]]])",
      arg(STR, STR, STR, NOD_ZO), ITR, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_PREPARE(SqlPrepare.class, "prepare(id,statement)", arg(ITR, STR), ITR, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_EXECUTE(SqlExecute.class, "execute(id,query)", arg(ITR, STR), ELM_ZM, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_EXECUTE_PREPARED(SqlExecutePrepared.class, "execute-prepared(id[,params])",
      arg(ITR, ELM), ELM_ZM, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_CLOSE(SqlClose.class, "close(id)", arg(ITR), EMP, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_COMMIT(SqlCommit.class, "commit(id)", arg(ITR), EMP, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_ROLLBACK(SqlRollback.class, "rollback(id)", arg(ITR), EMP, flag(NDT), SQL_URI),

  /* Streaming Module. */

  /** XQuery function. */
  _STREAM_MATERIALIZE(StreamMaterialize.class, "materialize(value)", arg(ITEM_ZM), ITEM_ZM,
      STREAM_URI),
  /** XQuery function. */
  _STREAM_IS_STREAMABLE(StreamIsStreamable.class, "is-streamable(item)", arg(ITEM), BLN,
      STREAM_URI),

  /* Unit Module. */

  /** XQuery function. */
  _UNIT_ASSERT(UnitAssert.class, "assert(test[,failure])", arg(ITEM_ZM, ITEM), EMP, flag(NDT),
      UNIT_URI),
  /** XQuery function. */
  _UNIT_ASSERT_EQUALS(UnitAssertEquals.class, "assert-equals(result,expected[,failure])",
      arg(ITEM_ZM, ITEM_ZM, ITEM), EMP, flag(NDT), UNIT_URI),
  /** XQuery function. */
  _UNIT_FAIL(UnitFail.class, "fail([failure])", arg(ITEM), ITEM_ZM, flag(NDT), UNIT_URI),

  /* Validate Module. */

  /** XQuery function. */
  _VALIDATE_XSD(ValidateXsd.class, "xsd(input[,schema])", arg(ITEM, ITEM), EMP, flag(NDT),
      VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_XSD_INFO(ValidateXsdInfo.class, "xsd-info(input[,schema])",
      arg(ITEM, ITEM), STR_ZM, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_DTD(ValidateDtd.class, "dtd(input[,schema])", arg(ITEM, ITEM), EMP, flag(NDT),
      VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_DTD_INFO(ValidateDtdInfo.class, "dtd-info(input[,schema])",
      arg(ITEM, ITEM), STR_ZM, flag(NDT), VALIDATE_URI),

  /* XQuery Module. */

  /** XQuery function. */
  _XQUERY_EVAL(XQueryEval.class, "eval(string[,bindings[,options]])",
      arg(STR, ITEM, ITEM), ITEM_ZM, flag(NDT), XQUERY_URI),
  /** XQuery function. */
  _XQUERY_UPDATE(XQueryUpdate.class, "update(string[,bindings[,options]])",
      arg(STR, ITEM, ITEM), ITEM_ZM, flag(UPD, NDT), XQUERY_URI),
  /** XQuery function. */
  _XQUERY_INVOKE(XQueryInvoke.class, "invoke(uri[,bindings[,options]])",
      arg(STR, ITEM, ITEM), ITEM_ZM, flag(NDT), XQUERY_URI),
  /** XQuery function. */
  _XQUERY_TYPE(XQueryType.class, "type(value)", arg(ITEM_ZM), ITEM_ZM, XQUERY_URI),

  /* XSLT Module. */

  /** XQuery function. */
  _XSLT_PROCESSOR(XsltProcessor.class, "processor()", arg(), STR, XSLT_URI),
  /** XQuery function. */
  _XSLT_VERSION(XsltVersion.class, "version()", arg(), STR, XSLT_URI),
  /** XQuery function. */
  _XSLT_TRANSFORM(XsltTransform.class, "transform(input,stylesheet[,params])",
      arg(ITEM, ITEM, ITEM), NOD, flag(NDT), XSLT_URI),
  /** XQuery function. */
  _XSLT_TRANSFORM_TEXT(XsltTransformText.class, "transform-text(input,stylesheet[,params])",
      arg(ITEM, ITEM, ITEM), STR, flag(NDT), XSLT_URI),

  /* ZIP Module. */

  /** XQuery function. */
  _ZIP_BINARY_ENTRY(ZipBinaryEntry.class, "binary-entry(path,entry)",
      arg(STR, STR), B64, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_TEXT_ENTRY(ZipTextEntry.class, "text-entry(path,entry[,encoding])",
      arg(STR, STR, STR), STR, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_HTML_ENTRY(ZipHtmlEntry.class, "html-entry(path,entry)",
      arg(STR, STR), NOD, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_XML_ENTRY(ZipXmlEntry.class, "xml-entry(path,entry)",
      arg(STR, STR), NOD, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_ENTRIES(ZipEntries.class, "entries(path)", arg(STR), ELM, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_ZIP_FILE(ZipZipFile.class, "zip-file(zip)", arg(ELM), EMP, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_UPDATE_ENTRIES(ZipUpdateEntries.class, "update-entries(zip,output)",
      arg(ELM, STR), EMP, flag(NDT), ZIP_URI);

  /**
   * Mapping between function classes and namespace URIs.
   * If no mapping exists, {@link QueryText#FN_URI} will be assumed as default mapping.
   */
  public static final HashMap<Class<? extends StandardFunc>, byte[]> URIS = new HashMap<>();

  // initialization of class/uri mappings
  static {
    for(final Function f : values()) {
      if(f.uri != null) URIS.put(f.func, f.uri);
    }
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
  /** URI. */
  private final byte[] uri;

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
   * Constructs a function signature; calls
   * {@link #Function(Class, String, SeqType[], SeqType, EnumSet)}.
   * @param clz reference to the class containing the function implementation
   * @param dsc descriptive function string
   * @param typ types of the function arguments
   * @param rtn return type
   * @param uri uri
   */
  Function(final Class<? extends StandardFunc> clz, final String dsc, final SeqType[] typ,
      final SeqType rtn, final byte[] uri) {
    this(clz, dsc, typ, rtn, EnumSet.noneOf(Flag.class), uri);
  }

  /**
   * Constructs a function signature; calls
   * {@link #Function(Class, String, SeqType[], SeqType, EnumSet)}.
   * @param clz reference to the class containing the function implementation
   * @param dsc descriptive function string
   * @param typ types of the function arguments
   * @param rtn return type
   * @param flg static function properties
   */
  Function(final Class<? extends StandardFunc> clz, final String dsc, final SeqType[] typ,
      final SeqType rtn, final EnumSet<Flag> flg) {
    this(clz, dsc, typ, rtn, flg, null);
  }

  /**
   * Constructs a function signature.
   * @param func reference to the class containing the function implementation
   * @param desc descriptive function string, containing the function name and its
   *            arguments in parentheses. Optional arguments are represented in nested
   *            square brackets; three dots indicate that the number of arguments of a
   *            function is not limited
   * @param args types of the function arguments
   * @param ret return type
   * @param flag static function properties
   * @param uri uri
   */
  Function(final Class<? extends StandardFunc> func, final String desc, final SeqType[] args,
      final SeqType ret, final EnumSet<Flag> flag, final byte[] uri) {

    this.func = func;
    this.desc = desc;
    this.ret = ret;
    this.args = args;
    this.flags = flag;
    this.uri = uri;

    // count number of minimum and maximum arguments by analyzing the function string
    final int b = desc.indexOf('[');
    if(b == -1) {
      min = args.length;
      max = args.length;
    } else {
      int c = b + 1 < desc.length() && desc.charAt(b + 1) == ',' ? 1 : 0;
      for(int i = 0; i < b; i++) if(desc.charAt(i) == ',') c++;
      min = c;
      max = desc.contains("...") ? Integer.MAX_VALUE : args.length;
    }
  }

  /**
   * Creates a new instance of the function.
   * @param sc static context
   * @param info input info
   * @param exprs arguments
   * @return function
   */
  public StandardFunc get(final StaticContext sc, final InputInfo info, final Expr... exprs) {
    return Reflect.get(func).init(sc, info, this, exprs);
  }

  /**
   * Returns the namespace URI of this function.
   * @return function
   */
  final byte[] uri() {
    final byte[] u = URIS.get(func);
    return u == null ? FN_URI : u;
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
   * @param ann annotations
   * @return function type
   */
  final FuncType type(final int arity, final Ann ann) {
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
      if(ARG.matcher(s).matches() || a instanceof Integer || a instanceof Long) {
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
