package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Flag.*;
import static org.basex.query.value.type.SeqType.*;

import java.util.*;
import java.util.function.Supplier;

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
import org.basex.query.func.jobs.*;
import org.basex.query.func.json.*;
import org.basex.query.func.lazy.*;
import org.basex.query.func.map.*;
import org.basex.query.func.math.*;
import org.basex.query.func.out.*;
import org.basex.query.func.proc.*;
import org.basex.query.func.prof.*;
import org.basex.query.func.random.*;
import org.basex.query.func.repo.*;
import org.basex.query.func.sql.*;
import org.basex.query.func.strings.*;
import org.basex.query.func.unit.*;
import org.basex.query.func.update.*;
import org.basex.query.func.user.*;
import org.basex.query.func.util.*;
import org.basex.query.func.validate.*;
import org.basex.query.func.web.*;
import org.basex.query.func.xquery.*;
import org.basex.query.func.xslt.*;
import org.basex.query.func.zip.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;

/**
 * Definitions of all built-in XQuery functions.
 * New namespace mappings for function prefixes and URIs must be added to the static initializer of
 * the {@link NSGlobal} class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public enum Function implements AFunction {

  // Standard functions

  /** XQuery function. */
  ABS(FnAbs::new, "abs(number)",
      arg(NUMERIC_ZO), NUMERIC_ZO),
  /** XQuery function. */
  ADJUST_DATE_TO_TIMEZONE(FnAdjustDateToTimezone::new, "adjust-date-to-timezone(date[,zone])",
      arg(DATE_ZO, DAY_TIME_DURATION_ZO), DATE_ZO),
  /** XQuery function. */
  ADJUST_DATETIME_TO_TIMEZONE(FnAdustDateTimeToTimezone::new,
      "adjust-dateTime-to-timezone(date[,zone])",
      arg(DATE_TIME_ZO, DAY_TIME_DURATION_ZO), DATE_TIME_ZO),
  /** XQuery function. */
  ADJUST_TIME_TO_TIMEZONE(FnAdjustTimeToTimezone::new, "adjust-time-to-timezone(date[,zone])",
      arg(TIME_ZO, DAY_TIME_DURATION_ZO), TIME_ZO),
  /** XQuery function. */
  ANALYZE_STRING(FnAnalyzeString::new, "analyze-string(input,pattern[,modifier])",
      arg(STRING_ZO, STRING_O, STRING_O), ELEMENT_O, flag(CNS)),
  /** XQuery function. */
  APPLY(FnApply::new, "apply(function,args)",
      arg(FUNCTION_O, ARRAY_O), ITEM_ZM, flag(POS, CTX, NDT, HOF)),
  /** XQuery function. */
  AVAILABLE_ENVIRONMENT_VARIABLES(FnAvailableEnvironmentVariables::new,
      "available-environment-variables()",
      arg(), STRING_ZM),
  /** XQuery function. */
  AVG(FnAvg::new, "avg(items)",
      arg(ANY_ATOMIC_TYPE_ZM), ANY_ATOMIC_TYPE_ZO),
  /** XQuery function. */
  BASE_URI(FnBaseUri::new, "base-uri([node])",
      arg(NODE_ZO), ANY_URI_ZO),
  /** XQuery function. */
  BOOLEAN(FnBoolean::new, "boolean(items)",
      arg(ITEM_ZM), BOOLEAN_O),
  /** XQuery function. */
  CEILING(FnCeiling::new, "ceiling(number)",
      arg(NUMERIC_ZO), NUMERIC_ZO),
  /** XQuery function. */
  CODEPOINT_EQUAL(FnCodepointEqual::new, "codepoint-equal(string1,string2)",
      arg(STRING_ZO, STRING_ZO), BOOLEAN_ZO),
  /** XQuery function. */
  CODEPOINTS_TO_STRING(FnCodepointsToString::new, "codepoints-to-string(nums)",
      arg(INTEGER_ZM), STRING_O),
  /** XQuery function. */
  COLLECTION(FnCollection::new, "collection([uri])",
      arg(STRING_ZO), DOCUMENT_NODE_ZM),
  /** XQuery function. */
  COMPARE(FnCompare::new, "compare(first,second[,collation])",
      arg(STRING_ZO, STRING_ZO, STRING_O), INTEGER_ZO),
  /** XQuery function. */
  CONCAT(FnConcat::new, "concat(value1,value2[,...])",
      arg(ANY_ATOMIC_TYPE_ZO, ANY_ATOMIC_TYPE_ZO), STRING_O),
  /** XQuery function. */
  CONTAINS(FnContains::new, "contains(string,substring[,collation])",
      arg(STRING_ZO, STRING_ZO, STRING_O), BOOLEAN_O),
  /** XQuery function. */
  CONTAINS_TOKEN(FnContainsToken::new, "contains-token(strings,token[,collation])",
      arg(STRING_ZM, STRING_O, STRING_O), BOOLEAN_O),
  /** XQuery function. */
  COUNT(FnCount::new, "count(items)",
      arg(ITEM_ZM), INTEGER_O),
  /** XQuery function. */
  CURRENT_DATE(FnCurrentDate::new, "current-date()",
      arg(), DATE_O),
  /** XQuery function. */
  CURRENT_DATETIME(FnCurrentDateTime::new, "current-dateTime()",
      arg(), DATE_TIME_O),
  /** XQuery function. */
  CURRENT_TIME(FnCurrentTime::new, "current-time()",
      arg(), TIME_O),
  /** XQuery function. */
  DATA(FnData::new, "data([items])",
      arg(ITEM_ZM), ANY_ATOMIC_TYPE_ZM),
  /** XQuery function. */
  DATETIME(FnDateTime::new, "dateTime(date,time)",
      arg(DATE_ZO, TIME_ZO), DATE_TIME_ZO),
  /** XQuery function. */
  DAY_FROM_DATE(FnDayFromDate::new, "day-from-date(date)",
      arg(DATE_ZO), INTEGER_ZO),
  /** XQuery function. */
  DAY_FROM_DATETIME(FnDayFromDateTime::new, "day-from-dateTime(datetime)",
      arg(DATE_TIME_ZO), INTEGER_ZO),
  /** XQuery function. */
  DAYS_FROM_DURATION(FnDayFromDuration::new, "days-from-duration(duration)",
      arg(DURATION_ZO), INTEGER_ZO),
  /** XQuery function. */
  DEEP_EQUAL(FnDeepEqual::new, "deep-equal(items1,items2[,collation])",
      arg(ITEM_ZM, ITEM_ZM, STRING_O), BOOLEAN_O),
  /** XQuery function. */
  DEFAULT_COLLATION(FnDefaultCollation::new, "default-collation()",
      arg(), STRING_O),
  /** XQuery function. */
  DEFAULT_LANGUAGE(FnDefaultLanguage::new, "default-language()",
      arg(), LANGUAGE_O),
  /** XQuery function. */
  DISTINCT_VALUES(FnDistinctValues::new, "distinct-values(items[,collation])",
      arg(ANY_ATOMIC_TYPE_ZM, STRING_O), ANY_ATOMIC_TYPE_ZM),
  /** XQuery function. */
  DOC(FnDoc::new, "doc(uri)",
      arg(STRING_ZO), DOCUMENT_NODE_ZO),
  /** XQuery function. */
  DOC_AVAILABLE(FnDocAvailable::new, "doc-available(uri)",
      arg(STRING_ZO), BOOLEAN_O),
  /** XQuery function. */
  DOCUMENT_URI(FnDocumentUri::new, "document-uri([node])",
      arg(NODE_ZO), ANY_URI_ZO),
  /** XQuery function. */
  ELEMENT_WITH_ID(FnElementWithId::new, "element-with-id(ids[,node])",
      arg(STRING_ZM, NODE_O), ELEMENT_ZM),
  /** XQuery function. */
  EMPTY(FnEmpty::new, "empty(items)",
      arg(ITEM_ZM), BOOLEAN_O),
  /** XQuery function. */
  ENCODE_FOR_URI(FnEncodeForUri::new, "encode-for-uri(string)",
      arg(STRING_ZO), STRING_O),
  /** XQuery function. */
  ENDS_WITH(FnEndsWith::new, "ends-with(string,substring[,collation])",
      arg(STRING_ZO, STRING_ZO, STRING_O), BOOLEAN_O),
  /** XQuery function. */
  ENVIRONMENT_VARIABLE(FnEnvironmentVariable::new, "environment-variable(string)",
      arg(STRING_O), STRING_ZO),
  /** XQuery function. */
  ERROR(FnError::new, "error([code[,description[,object]]])",
      arg(QNAME_ZO, STRING_O, ITEM_ZM), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  ESCAPE_HTML_URI(FnEscapeHtmlUri::new, "escape-html-uri(string)",
      arg(STRING_ZO), STRING_O),
  /** XQuery function. */
  EXACTLY_ONE(FnExactlyOne::new, "exactly-one(items)",
      arg(ITEM_ZM), ITEM_O),
  /** XQuery function. */
  EXISTS(FnExists::new, "exists(items)",
      arg(ITEM_ZM), BOOLEAN_O),
  /** XQuery function. */
  FALSE(FnFalse::new, "false()",
      arg(), BOOLEAN_O),
  /** XQuery function. */
  FILTER(FnFilter::new, "filter(items,function)",
      arg(ITEM_ZM, FuncType.get(BOOLEAN_O, ITEM_O).seqType()), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FLOOR(FnFloor::new, "floor(number)",
      arg(NUMERIC_ZO), NUMERIC_ZO),
  /** XQuery function. */
  FOLD_LEFT(FnFoldLeft::new, "fold-left(items,zero,function)",
      arg(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM_O).seqType()), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FOLD_RIGHT(FnFoldRight::new, "fold-right(items,zero,function)",
      arg(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O, ITEM_ZM).seqType()), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FOR_EACH(FnForEach::new, "for-each(items,function)",
      arg(ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O).seqType()), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FOR_EACH_PAIR(FnForEachPair::new, "for-each-pair(items1,items2,function)",
      arg(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O, ITEM_O).seqType()), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FORMAT_DATE(FnFormatDate::new, "format-date(date,picture[,language,calendar,place])",
      arg(DATE_ZO, STRING_O, STRING_ZO, STRING_ZO, STRING_ZO), STRING_ZO),
  /** XQuery function. */
  FORMAT_DATETIME(FnFormatDateTime::new,
      "format-dateTime(number,picture[,language,calendar,place])",
      arg(DATE_TIME_ZO, STRING_O, STRING_ZO, STRING_ZO, STRING_ZO), STRING_ZO),
  /** XQuery function. */
  FORMAT_INTEGER(FnFormatInteger::new, "format-integer(number,picture[,language])",
      arg(INTEGER_ZO, STRING_O, STRING_O), STRING_O),
  /** XQuery function. */
  FORMAT_NUMBER(FnFormatNumber::new, "format-number(number,picture[,format])",
      arg(NUMERIC_ZO, STRING_O, STRING_ZO), STRING_O),
  /** XQuery function. */
  FORMAT_TIME(FnFormatTime::new, "format-time(number,picture[,language,calendar,place])",
      arg(TIME_ZO, STRING_O, STRING_ZO, STRING_ZO, STRING_ZO), STRING_ZO),
  /** XQuery function. */
  FUNCTION_ARITY(FnFunctionArity::new, "function-arity(function)",
      arg(FUNCTION_O), INTEGER_O),
  /** XQuery function. */
  FUNCTION_LOOKUP(FnFunctionLookup::new, "function-lookup(name,arity)",
      arg(QNAME_O, INTEGER_O), FUNCTION_ZO, flag(POS, CTX, NDT, HOF)),
  /** XQuery function. */
  FUNCTION_NAME(FnFunctionName::new, "function-name(function)",
      arg(FUNCTION_O), QNAME_ZO),
  /** XQuery function. */
  GENERATE_ID(FnGenerateId::new, "generate-id([node])",
      arg(NODE_ZO), STRING_O),
  /** XQuery function. */
  HAS_CHILDREN(FnHasChildren::new, "has-children([node])",
      arg(NODE_ZM), BOOLEAN_O),
  /** XQuery function. */
  HEAD(FnHead::new, "head(items)",
      arg(ITEM_ZM), ITEM_ZO),
  /** XQuery function. */
  HOURS_FROM_DATETIME(FnHoursFromDateTime::new, "hours-from-dateTime(datetime)",
      arg(DATE_TIME_ZO), INTEGER_ZO),
  /** XQuery function. */
  HOURS_FROM_DURATION(FnHoursFromDuration::new, "hours-from-duration(duration)",
      arg(DURATION_ZO), INTEGER_ZO),
  /** XQuery function. */
  HOURS_FROM_TIME(FnHoursFromTime::new, "hours-from-time(time)",
      arg(TIME_ZO), INTEGER_ZO),
  /** XQuery function. */
  ID(FnId::new, "id(ids[,node])",
      arg(STRING_ZM, NODE_O), ELEMENT_ZM),
  /** XQuery function. */
  IDREF(FnIdref::new, "idref(ids[,node])",
      arg(STRING_ZM, NODE_O), NODE_ZM),
  /** XQuery function. */
  IMPLICIT_TIMEZONE(FnImplicitTimezone::new, "implicit-timezone()",
      arg(), DAY_TIME_DURATION_O),
  /** XQuery function. */
  IN_SCOPE_PREFIXES(FnInScopePrefixes::new, "in-scope-prefixes(element)",
      arg(ELEMENT_O), STRING_ZM),
  /** XQuery function. */
  INDEX_OF(FnIndexOf::new, "index-of(items,item[,collation])",
      arg(ANY_ATOMIC_TYPE_ZM, ANY_ATOMIC_TYPE_O, STRING_O), INTEGER_ZM),
  /** XQuery function. */
  INNERMOST(FnInnermost::new, "innermost(nodes)",
      arg(NODE_ZM), NODE_ZM),
  /** XQuery function. */
  INSERT_BEFORE(FnInsertBefore::new, "insert-before(items,position,insert)",
      arg(ITEM_ZM, INTEGER_O, ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  IRI_TO_URI(FnIriToUri::new, "iri-to-uri(string)",
      arg(STRING_ZO), STRING_O),
  /** XQuery function. */
  JSON_DOC(FnJsonDoc::new, "json-doc(uri[,options])",
      arg(STRING_ZO, MAP_O), ITEM_ZO),
  /** XQuery function. */
  JSON_TO_XML(FnJsonToXml::new, "json-to-xml(string[,options])",
      arg(STRING_ZO, MAP_O), NODE_ZO, flag(CNS)),
  /** XQuery function. */
  LANG(FnLang::new, "lang(ids[,node])",
      arg(STRING_ZO, NODE_O), BOOLEAN_O),
  /** XQuery function. */
  LAST(FnLast::new, "last()",
      arg(), INTEGER_O, flag(POS, CTX)),
  /** XQuery function. */
  LOCAL_NAME(FnLocalName::new, "local-name([node])",
      arg(NODE_ZO), STRING_O),
  /** XQuery function. */
  LOCAL_NAME_FROM_QNAME(FnLocalNameFromQName::new, "local-name-from-QName(qname)",
      arg(QNAME_ZO), NCNAME_ZO),
  /** XQuery function. */
  LOWER_CASE(FnLowerCase::new, "lower-case(string)",
      arg(STRING_ZO), STRING_O),
  /** XQuery function. */
  MATCHES(FnMatches::new, "matches(string,pattern[,modifier])",
      arg(STRING_ZO, STRING_O, STRING_O), BOOLEAN_O),
  /** XQuery function. */
  MAX(FnMax::new, "max(items[,collation])",
      arg(ANY_ATOMIC_TYPE_ZM, STRING_O), ANY_ATOMIC_TYPE_ZO),
  /** XQuery function. */
  MIN(FnMin::new, "min(items[,collation])",
      arg(ANY_ATOMIC_TYPE_ZM, STRING_O), ANY_ATOMIC_TYPE_ZO),
  /** XQuery function. */
  MINUTES_FROM_DATETIME(FnMinutesFromDateTime::new, "minutes-from-dateTime(datetime)",
      arg(DATE_TIME_ZO), INTEGER_ZO),
  /** XQuery function. */
  MINUTES_FROM_DURATION(FnMinutesFromDuration::new, "minutes-from-duration(duration)",
      arg(DURATION_ZO), INTEGER_ZO),
  /** XQuery function. */
  MINUTES_FROM_TIME(FnMinutesFromTime::new, "minutes-from-time(time)",
      arg(TIME_ZO), INTEGER_ZO),
  /** XQuery function. */
  MONTH_FROM_DATE(FnMonthFromDate::new, "month-from-date(date)",
      arg(DATE_ZO), INTEGER_ZO),
  /** XQuery function. */
  MONTH_FROM_DATETIME(FnMonthFromDateTime::new, "month-from-dateTime(datetime)",
      arg(DATE_TIME_ZO), INTEGER_ZO),
  /** XQuery function. */
  MONTHS_FROM_DURATION(FnMonthsFromDuration::new, "months-from-duration(duration)",
      arg(DURATION_ZO), INTEGER_ZO),
  /** XQuery function. */
  NAME(FnName::new, "name([node])",
      arg(NODE_ZO), STRING_O),
  /** XQuery function. */
  NAMESPACE_URI(FnNamespaceUri::new, "namespace-uri([node])",
      arg(NODE_ZO), ANY_URI_O),
  /** XQuery function. */
  NAMESPACE_URI_FOR_PREFIX(FnNamespaceUriForPrefix::new,
      "namespace-uri-for-prefix(prefix,element)",
      arg(STRING_ZO, ELEMENT_O), ANY_URI_ZO),
  /** XQuery function. */
  NAMESPACE_URI_FROM_QNAME(FnNamespaceUriFromQName::new, "namespace-uri-from-QName(qname)",
      arg(QNAME_ZO), ANY_URI_ZO),
  /** XQuery function. */
  NILLED(FnNilled::new, "nilled([node])",
      arg(NODE_ZO), BOOLEAN_ZO),
  /** XQuery function. */
  NODE_NAME(FnNodeName::new, "node-name([node])",
      arg(NODE_ZO), QNAME_ZO),
  /** XQuery function. */
  NORMALIZE_SPACE(FnNormalizeSpace::new, "normalize-space([string])",
      arg(STRING_ZO), STRING_O),
  /** XQuery function. */
  NORMALIZE_UNICODE(FnNormalizeUnicode::new, "normalize-unicode(string[,form])",
      arg(STRING_ZO, STRING_O), STRING_O),
  /** XQuery function. */
  NOT(FnNot::new, "not(items)",
      arg(ITEM_ZM), BOOLEAN_O),
  /** XQuery function. */
  NUMBER(FnNumber::new, "number([item])",
      arg(ANY_ATOMIC_TYPE_ZO), DOUBLE_O),
  /** XQuery function. */
  ONE_OR_MORE(FnOneOrMore::new, "one-or-more(items)",
      arg(ITEM_ZM), ITEM_OM),
  /** XQuery function. */
  OUTERMOST(FnOutermost::new, "outermost(nodes)",
      arg(NODE_ZM), NODE_ZM),
  /** XQuery function. */
  PARSE_IETF_DATE(FnParseIetfDate::new, "parse-ietf-date(string)",
      arg(STRING_ZO), DATE_TIME_ZO),
  /** XQuery function. */
  PARSE_JSON(FnParseJson::new, "parse-json(string[,options])",
      arg(STRING_ZO, MAP_O), ITEM_ZO),
  /** XQuery function. */
  PARSE_XML(FnParseXml::new, "parse-xml(string)",
      arg(STRING_ZO), DOCUMENT_NODE_ZO, flag(CNS)),
  /** XQuery function. */
  PARSE_XML_FRAGMENT(FnParseXmlFragment::new, "parse-xml-fragment(string)",
      arg(STRING_ZO), DOCUMENT_NODE_ZO, flag(CNS)),
  /** XQuery function. */
  PATH(FnPath::new, "path([node])",
      arg(NODE_ZO), STRING_ZO),
  /** XQuery function. */
  POSITION(FnPosition::new, "position()",
      arg(), INTEGER_O, flag(POS, CTX)),
  /** XQuery function. */
  PREFIX_FROM_QNAME(FnPrefixFromQName::new, "prefix-from-QName(qname)",
      arg(QNAME_ZO), NCNAME_ZO),
  /** XQuery function. */
  PUT(FnPut::new, "put(node,uri[,params])",
      arg(NODE_O, STRING_ZO, ITEM_ZO), EMPTY_SEQUENCE_Z, flag(UPD)),
  /** XQuery function. */
  QNAME(FnQName::new, "QName(uri,name)",
      arg(STRING_ZO, STRING_O), QNAME_O),
  /** XQuery function. */
  RANDOM_NUMBER_GENERATOR(FnRandomNumberGenerator::new, "random-number-generator([seed])",
      arg(ANY_ATOMIC_TYPE_O), MAP_O),
  /** XQuery function. */
  REMOVE(FnRemove::new, "remove(items,position)",
      arg(ITEM_ZM, INTEGER_O), ITEM_ZM),
  /** XQuery function. */
  REPLACE(FnReplace::new, "replace(string,pattern,replace[,modifier])",
      arg(STRING_ZO, STRING_O, STRING_O, STRING_O), STRING_O),
  /** XQuery function. */
  RESOLVE_QNAME(FnResolveQName::new, "resolve-QName(name,base)",
      arg(STRING_ZO, ELEMENT_O), QNAME_ZO),
  /** XQuery function. */
  RESOLVE_URI(FnResolveUri::new, "resolve-uri(name[,element])",
      arg(STRING_ZO, STRING_O), ANY_URI_ZO),
  /** XQuery function. */
  REVERSE(FnReverse::new, "reverse(items)",
      arg(ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  ROOT(FnRoot::new, "root([node])",
      arg(NODE_ZO), NODE_ZO),
  /** XQuery function. */
  ROUND(FnRound::new, "round(number[,precision])",
      arg(NUMERIC_ZO, INTEGER_O), NUMERIC_ZO),
  /** XQuery function. */
  ROUND_HALF_TO_EVEN(FnRoundHalfToEven::new, "round-half-to-even(number[,precision])",
      arg(NUMERIC_ZO, INTEGER_O), NUMERIC_ZO),
  /** XQuery function. */
  SECONDS_FROM_DATETIME(FnSecondsFromDateTime::new, "seconds-from-dateTime(datetime)",
      arg(DATE_TIME_ZO), DECIMAL_ZO),
  /** XQuery function. */
  SECONDS_FROM_DURATION(FnSecondsFromDuration::new, "seconds-from-duration(duration)",
      arg(DURATION_ZO), DECIMAL_ZO),
  /** XQuery function. */
  SECONDS_FROM_TIME(FnSecondsFromTime::new, "seconds-from-time(time)",
      arg(TIME_ZO), DECIMAL_ZO),
  /** XQuery function. */
  SERIALIZE(FnSerialize::new, "serialize(items[,params])",
      arg(ITEM_ZM, ITEM_ZO), STRING_O),
  /** XQuery function. */
  SORT(FnSort::new, "sort(items[,collation[,function]])",
      arg(ITEM_ZM, STRING_ZO, FuncType.get(ANY_ATOMIC_TYPE_ZM, ITEM_O).seqType()), ITEM_ZM),
  /** XQuery function. */
  STARTS_WITH(FnStartsWith::new, "starts-with(string,substring[,collation])",
      arg(STRING_ZO, STRING_ZO, STRING_O), BOOLEAN_O),
  /** XQuery function. */
  STATIC_BASE_URI(FnStaticBaseUri::new, "static-base-uri()",
      arg(), ANY_URI_ZO),
  /** XQuery function. */
  STRING(FnString::new, "string([item])",
      arg(ITEM_ZO), STRING_O),
  /** XQuery function. */
  STRING_JOIN(FnStringJoin::new, "string-join(items[,separator])",
      arg(ANY_ATOMIC_TYPE_ZM, STRING_O), STRING_O),
  /** XQuery function. */
  STRING_LENGTH(FnStringLength::new, "string-length([string])",
      arg(STRING_ZO), INTEGER_O),
  /** XQuery function. */
  STRING_TO_CODEPOINTS(FnStringToCodepoints::new, "string-to-codepoints(string)",
      arg(STRING_ZO), INTEGER_ZM),
  /** XQuery function. */
  SUBSEQUENCE(FnSubsequence::new, "subsequence(items,first[,length])",
      arg(ITEM_ZM, DOUBLE_O, DOUBLE_O), ITEM_ZM),
  /** XQuery function. */
  SUBSTRING(FnSubstring::new, "substring(string,start[,length])",
      arg(STRING_ZO, DOUBLE_O, DOUBLE_O), STRING_O),
  /** XQuery function. */
  SUBSTRING_AFTER(FnSubstringAfter::new, "substring-after(string,separator[,collation])",
      arg(STRING_ZO, STRING_ZO, STRING_O), STRING_O),
  /** XQuery function. */
  SUBSTRING_BEFORE(FnSubstringBefore::new, "substring-before(string,separator[,collation])",
      arg(STRING_ZO, STRING_ZO, STRING_O), STRING_O),
  /** XQuery function. */
  SUM(FnSum::new, "sum(items[,zero])",
      arg(ANY_ATOMIC_TYPE_ZM, ANY_ATOMIC_TYPE_ZO), ANY_ATOMIC_TYPE_ZO),
  /** XQuery function. */
  TAIL(FnTail::new, "tail(items)",
      arg(ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  TIMEZONE_FROM_DATE(FnTimezoneFromDate::new, "timezone-from-date(date)",
      arg(DATE_ZO), DAY_TIME_DURATION_ZO),
  /** XQuery function. */
  TIMEZONE_FROM_DATETIME(FnTimezoneFromDateTime::new, "timezone-from-dateTime(dateTime)",
      arg(DATE_TIME_ZO), DAY_TIME_DURATION_ZO),
  /** XQuery function. */
  TIMEZONE_FROM_TIME(FnTimezoneFromTime::new, "timezone-from-time(time)",
      arg(TIME_ZO), DAY_TIME_DURATION_ZO),
  /** XQuery function. */
  TOKENIZE(FnTokenize::new, "tokenize(string[,pattern[,modifier]])",
      arg(STRING_ZO, STRING_O, STRING_O), STRING_ZM),
  /** XQuery function. */
  TRACE(FnTrace::new, "trace(value[,label])",
      arg(ITEM_ZM, STRING_O), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  TRANSLATE(FnTranslate::new, "translate(string,map1,map2)",
      arg(STRING_ZO, STRING_O, STRING_O), STRING_O),
  /** XQuery function. */
  TRUE(FnTrue::new, "true()",
      arg(), BOOLEAN_O),
  /** XQuery function. */
  UNORDERED(FnUnordered::new, "unordered(items)",
      arg(ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  UNPARSED_TEXT(FnUnparsedText::new, "unparsed-text(uri[,encoding])",
      arg(STRING_ZO, STRING_O), STRING_ZO),
  /** XQuery function. */
  UNPARSED_TEXT_AVAILABLE(FnUnparsedTextAvailable::new, "unparsed-text-available(uri[,encoding])",
      arg(STRING_ZO, STRING_O), BOOLEAN_O),
  /** XQuery function. */
  UNPARSED_TEXT_LINES(FnUnparsedTextLines::new, "unparsed-text-lines(uri[,encoding])",
      arg(STRING_ZO, STRING_O), STRING_ZM),
  /** XQuery function. */
  UPPER_CASE(FnUpperCase::new, "upper-case(string)",
      arg(STRING_ZO), STRING_O),
  /** XQuery function. */
  URI_COLLECTION(FnUriCollection::new, "uri-collection([uri])",
      arg(STRING_ZO), ANY_URI_ZM),
  /** XQuery function. */
  XML_TO_JSON(FnXmlToJson::new, "xml-to-json(node[,options])",
      arg(NODE_ZO, MAP_O), STRING_ZO),
  /** XQuery function. */
  YEAR_FROM_DATE(FnYearFromDate::new, "year-from-date(date)",
      arg(DATE_ZO), INTEGER_ZO),
  /** XQuery function. */
  YEAR_FROM_DATETIME(FnYearFromDateTime::new, "year-from-dateTime(datetime)",
      arg(DATE_TIME_ZO), INTEGER_ZO),
  /** XQuery function. */
  YEARS_FROM_DURATION(FnYearsFromDuration::new, "years-from-duration(duration)",
      arg(DURATION_ZO), INTEGER_ZO),
  /** XQuery function. */
  ZERO_OR_ONE(FnZeroOrOne::new, "zero-or-one(items)",
      arg(ITEM_ZM), ITEM_ZO),

  // Map Module

  /** XQuery function. */
  _MAP_CONTAINS(MapContains::new, "contains(map,key)",
      arg(MAP_O, ANY_ATOMIC_TYPE_O), BOOLEAN_O, MAP_URI),
  /** XQuery function. */
  _MAP_ENTRY(MapEntry::new, "entry(key,value)",
      arg(ANY_ATOMIC_TYPE_O, ITEM_ZM), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_FIND(MapFind::new, "find(input,key)",
      arg(ITEM_ZM, ANY_ATOMIC_TYPE_O), ARRAY_O, MAP_URI),
  /** XQuery function. */
  _MAP_FOR_EACH(MapForEach::new, "for-each(map,function)",
      arg(MAP_O, FuncType.get(ITEM_ZM, ANY_ATOMIC_TYPE_O, ITEM_ZM).seqType()),
      ITEM_ZM, flag(HOF), MAP_URI),
  /** XQuery function. */
  _MAP_GET(MapGet::new, "get(map,key)",
      arg(MAP_O, ANY_ATOMIC_TYPE_O), ITEM_ZM, MAP_URI),
  /** XQuery function. */
  _MAP_KEYS(MapKeys::new, "keys(map)",
      arg(MAP_O), ANY_ATOMIC_TYPE_ZM, MAP_URI),
  /** XQuery function. */
  _MAP_MERGE(MapMerge::new, "merge(maps[,options])",
      arg(MAP_ZM, MAP_O), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_PUT(MapPut::new, "put(map,key,value)",
      arg(MAP_O, ANY_ATOMIC_TYPE_O, ITEM_ZM), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_REMOVE(MapRemove::new, "remove(map,keys)",
      arg(MAP_O, ANY_ATOMIC_TYPE_ZM), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_SIZE(MapSize::new, "size(map)",
      arg(MAP_O), INTEGER_O, MAP_URI),

  // Array Module

  /** XQuery function. */
  _ARRAY_APPEND(ArrayAppend::new, "append(array,value)",
      arg(ARRAY_O, ITEM_ZM), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FILTER(ArrayFilter::new, "filter(array,function)",
      arg(ARRAY_O, FuncType.get(BOOLEAN_O, ITEM_ZM).seqType()), ARRAY_O, flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FLATTEN(ArrayFlatten::new, "flatten(item()*)",
      arg(ITEM_ZM), ITEM_ZM, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FOLD_LEFT(ArrayFoldLeft::new, "fold-left(array,zero,function)",
      arg(ARRAY_O, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM_O).seqType()), ITEM_ZM,
      flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FOLD_RIGHT(ArrayFoldRight::new, "fold-right(array,zero,function)",
      arg(ARRAY_O, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O, ITEM_ZM).seqType()), ITEM_ZM,
      flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FOR_EACH(ArrayForEach::new, "for-each(array,function)",
      arg(ARRAY_O, FuncType.get(ITEM_ZM, ITEM_O).seqType()), ARRAY_O, flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FOR_EACH_PAIR(ArrayForEachPair::new, "for-each-pair(array1,array2,function)",
      arg(ARRAY_O, ARRAY_O, FuncType.get(ITEM_ZM, ITEM_O, ITEM_O).seqType()), ARRAY_O,
      flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_GET(ArrayGet::new, "get(array,position)",
      arg(ARRAY_O, INTEGER_O), ITEM_ZM, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_HEAD(ArrayHead::new, "head(array)",
      arg(ARRAY_O), ITEM_ZM, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_INSERT_BEFORE(ArrayInsertBefore::new, "insert-before(array,position,value)",
      arg(ARRAY_O, INTEGER_O, ITEM_ZO), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_JOIN(ArrayJoin::new, "join(arrays)",
      arg(ARRAY_ZM), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_PUT(ArrayPut::new, "put(array,position,value)",
      arg(ARRAY_O, INTEGER_O, ITEM_ZM), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_REMOVE(ArrayRemove::new, "remove(array,position)",
      arg(ARRAY_O, INTEGER_ZM), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_REVERSE(ArrayReverse::new, "reverse(array)",
      arg(ARRAY_O), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_SIZE(ArraySize::new, "size(array)",
      arg(ARRAY_O), INTEGER_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_SORT(ArraySort::new, "sort(array[,collation[,function]])",
      arg(ARRAY_O, STRING_ZO, FuncType.get(ANY_ATOMIC_TYPE_ZM, ITEM_O).seqType()),
      ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_SUBARRAY(ArraySubarray::new, "subarray(array,position[,length])",
      arg(ARRAY_O, INTEGER_O, INTEGER_O), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_TAIL(ArrayTail::new, "tail(array)",
      arg(ARRAY_O), ARRAY_O, ARRAY_URI),

  // Math Module

  /** XQuery function. */
  _MATH_ACOS(MathAcos::new, "acos(number)",
      arg(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_ASIN(MathAsin::new, "asin(number)",
      arg(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_ATAN(MathAtan::new, "atan(number)",
      arg(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_ATAN2(MathAtan2::new, "atan2(number1,number2)",
      arg(DOUBLE_O, DOUBLE_O), DOUBLE_O, MATH_URI),
  /** XQuery function. */
  _MATH_COS(MathCos::new, "cos(number)",
      arg(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_EXP(MathExp::new, "exp(number)",
      arg(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_EXP10(MathExp10::new, "exp10(number)",
      arg(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_LOG(MathLog::new, "log(number)",
      arg(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_LOG10(MathLog10::new, "log10(number)",
      arg(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_PI(MathPi::new, "pi()",
      arg(), DOUBLE_O, MATH_URI),
  /** XQuery function. */
  _MATH_POW(MathPow::new, "pow(number1,number2)",
      arg(DOUBLE_ZO, NUMERIC_O), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_SIN(MathSin::new, "sin(number)",
      arg(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_SQRT(MathSqrt::new, "sqrt(number)",
      arg(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_TAN(MathTan::new, "tan(number)",
      arg(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),

  // Math Module (custom)

  /** XQuery function. */
  _MATH_COSH(MathCosh::new, "cosh(number)",
      arg(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_CRC32(MathCrc32::new, "crc32(string)",
      arg(STRING_ZO), HEX_BINARY_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_E(MathE::new, "e()",
      arg(), DOUBLE_O, MATH_URI),
  /** XQuery function. */
  _MATH_SINH(MathSinh::new, "sinh(number)",
      arg(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_TANH(MathTanh::new, "tanh(number)",
      arg(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),

  // Admin Module

  /** XQuery function. */
  _ADMIN_DELETE_LOGS(AdminDeleteLogs::new, "delete-logs(date)",
      arg(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), ADMIN_URI),
  /** XQuery function. */
  _ADMIN_LOGS(AdminLogs::new, "logs([date[,merge]])",
      arg(STRING_O, BOOLEAN_O), ELEMENT_ZM, flag(NDT), ADMIN_URI),
  /** XQuery function. */
  _ADMIN_SESSIONS(AdminSessions::new, "sessions()",
      arg(), ELEMENT_ZM, flag(NDT), ADMIN_URI),
  /** XQuery function. */
  _ADMIN_WRITE_LOG(AdminWriteLog::new, "write-log(message[,type])",
      arg(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), ADMIN_URI),

  // Archive Module

  /** XQuery function. */
  _ARCHIVE_CREATE(ArchiveCreate::new, "create(entries,contents[,options])",
      arg(ITEM_ZM, ITEM_ZM, MAP_ZO), BASE64_BINARY_O, flag(NDT), ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_CREATE_FROM(ArchiveCreateFrom::new, "create-from(path[,options[,entries]])",
      arg(STRING_O, MAP_ZO, ITEM_ZM), EMPTY_SEQUENCE_Z, flag(NDT), ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_DELETE(ArchiveDelete::new, "delete(archive,entries)",
      arg(BASE64_BINARY_O, ITEM_ZM), BASE64_BINARY_O, ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_ENTRIES(ArchiveEntries::new, "entries(archive)",
      arg(BASE64_BINARY_O), ELEMENT_ZM, ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_EXTRACT_BINARY(ArchiveExtractBinary::new, "extract-binary(archive[,entries])",
      arg(BASE64_BINARY_O, ITEM_ZM), BASE64_BINARY_ZM, ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_EXTRACT_TEXT(ArchiveExtractText::new, "extract-text(archive[,entries[,encoding]])",
      arg(BASE64_BINARY_O, ITEM_ZM, STRING_O), STRING_ZM, ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_EXTRACT_TO(ArchiveExtractTo::new, "extract-to(path,archive[,entries])",
      arg(STRING_O, BASE64_BINARY_O, ITEM_ZM), EMPTY_SEQUENCE_Z, flag(NDT), ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_OPTIONS(ArchiveOptions::new, "options(archive)",
      arg(BASE64_BINARY_O), MAP_O, ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_UPDATE(ArchiveUpdate::new, "update(archive,entries,contents)",
      arg(BASE64_BINARY_O, ITEM_ZM, ITEM_ZM), BASE64_BINARY_O, ARCHIVE_URI),

  // Binary Module

  /** XQuery function. */
  _BIN_AND(BinAnd::new, "and(binary1,binary2)",
      arg(BASE64_BINARY_ZO, BASE64_BINARY_ZO), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_BIN(BinBin::new, "bin(string)",
      arg(STRING_ZO), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_DECODE_STRING(BinDecodeString::new, "decode-string(binary[,encoding[,offset[,size]]])",
      arg(BASE64_BINARY_ZO, STRING_O, INTEGER_O, INTEGER_O), STRING_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_ENCODE_STRING(BinEncodeString::new, "encode-string(string[,encoding])",
      arg(STRING_ZO, STRING_O), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_FIND(BinFind::new, "find(binary,offset,search)",
      arg(BASE64_BINARY_ZO, INTEGER_O, BASE64_BINARY_ZO), INTEGER_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_FROM_OCTETS(BinFromOctets::new, "from-octets(integers)",
      arg(INTEGER_ZM), BASE64_BINARY_O, BIN_URI),
  /** XQuery function. */
  _BIN_HEX(BinHex::new, "hex(string)",
      arg(STRING_ZO), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_INSERT_BEFORE(BinInsertBefore::new, "insert-before(binary,offset,extra)",
      arg(BASE64_BINARY_ZO, INTEGER_O, BASE64_BINARY_ZO), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_JOIN(BinJoin::new, "join(binaries)",
      arg(BASE64_BINARY_ZM), BASE64_BINARY_O, BIN_URI),
  /** XQuery function. */
  _BIN_LENGTH(BinLength::new, "length(binary)",
      arg(BASE64_BINARY_O), INTEGER_O, BIN_URI),
  /** XQuery function. */
  _BIN_NOT(BinNot::new, "not(binary)",
      arg(BASE64_BINARY_ZO), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_OCTAL(BinOctal::new, "octal(string)",
      arg(STRING_ZO), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_OR(BinOr::new, "or(binary1,binary2)",
      arg(BASE64_BINARY_ZO, BASE64_BINARY_ZO), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_PACK_DOUBLE(BinPackDouble::new, "pack-double(double[,order])",
      arg(DOUBLE_O, STRING_O), BASE64_BINARY_O, BIN_URI),
  /** XQuery function. */
  _BIN_PACK_FLOAT(BinPackFloat::new, "pack-float(float[,order])",
      arg(FLOAT_O, STRING_O), BASE64_BINARY_O, BIN_URI),
  /** XQuery function. */
  _BIN_PACK_INTEGER(BinPackInteger::new, "pack-integer(integer,size[,order])",
      arg(INTEGER_O, INTEGER_O, STRING_O), BASE64_BINARY_O, BIN_URI),
  /** XQuery function. */
  _BIN_PAD_LEFT(BinPadLeft::new, "pad-left(binary,size[,octet])",
      arg(BASE64_BINARY_ZO, INTEGER_O, INTEGER_O), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_PAD_RIGHT(BinPadRight::new, "pad-right(binary,size[,octet])",
      arg(BASE64_BINARY_ZO, INTEGER_O, INTEGER_O), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_PART(BinPart::new, "part(binary,offset[,size])",
      arg(BASE64_BINARY_ZO, INTEGER_O, INTEGER_O), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_SHIFT(BinShift::new, "shift(binary,by)",
      arg(BASE64_BINARY_ZO, INTEGER_O), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_TO_OCTETS(BinToOctets::new, "to-octets(binary)",
      arg(BASE64_BINARY_ZO), INTEGER_ZM, BIN_URI),
  /** XQuery function. */
  _BIN_UNPACK_DOUBLE(BinUnpackDouble::new, "unpack-double(binary,offset[,order])",
      arg(BASE64_BINARY_O, INTEGER_O, STRING_O), DOUBLE_O, BIN_URI),
  /** XQuery function. */
  _BIN_UNPACK_FLOAT(BinUnpackFloat::new, "unpack-float(binary,offset[,order])",
      arg(BASE64_BINARY_O, INTEGER_O, STRING_O), FLOAT_O, BIN_URI),
  /** XQuery function. */
  _BIN_UNPACK_INTEGER(BinUnpackInteger::new, "unpack-integer(binary,offset,size[,order])",
      arg(BASE64_BINARY_O, INTEGER_O, INTEGER_O, STRING_O), INTEGER_O, BIN_URI),
  /** XQuery function. */
  _BIN_UNPACK_UNSIGNED_INTEGER(BinUnpackUnsignedInteger::new,
      "unpack-unsigned-integer(binary,offset,size[,order])",
      arg(BASE64_BINARY_O, INTEGER_O, INTEGER_O, STRING_O), INTEGER_O, BIN_URI),
  /** XQuery function. */
  _BIN_XOR(BinXor::new, "xor(binary1,binary2)",
      arg(BASE64_BINARY_ZO, BASE64_BINARY_ZO), BASE64_BINARY_ZO, BIN_URI),

  // Client Module

  /** XQuery function. */
  _CLIENT_CLOSE(ClientClose::new, "close(id)",
      arg(ANY_URI_O), EMPTY_SEQUENCE_Z, flag(NDT), CLIENT_URI),
  /** XQuery function. */
  _CLIENT_CONNECT(ClientConnect::new, "connect(url,port,user,password)",
      arg(STRING_O, INTEGER_O, STRING_O, STRING_O), ANY_URI_O, flag(NDT), CLIENT_URI),
  /** XQuery function. */
  _CLIENT_EXECUTE(ClientExecute::new, "execute(id,command)",
      arg(ANY_URI_O, STRING_O), STRING_O, flag(NDT), CLIENT_URI),
  /** XQuery function. */
  _CLIENT_INFO(ClientInfo::new, "info(id)",
      arg(ANY_URI_O), STRING_O, flag(NDT), CLIENT_URI),
  /** XQuery function. */
  _CLIENT_QUERY(ClientQuery::new, "query(id,query[,bindings])",
      arg(ANY_URI_O, STRING_O, MAP_ZO), ITEM_ZM, flag(NDT), CLIENT_URI),

  // Conversion Module

  /** XQuery function. */
  _CONVERT_BINARY_TO_BYTES(ConvertBinaryToBytes::new, "binary-to-bytes(binary)",
      arg(BINARY_O), BYTE_ZM, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_BINARY_TO_INTEGERS(ConvertBinaryToIntegers::new, "binary-to-integers(binary)",
      arg(BINARY_O), INTEGER_ZM, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_BINARY_TO_STRING(ConvertBinaryToString::new,
      "binary-to-string(binary[,encoding[,fallback]])",
      arg(BINARY_O, STRING_O, BOOLEAN_O), STRING_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGER_FROM_BASE(ConvertIntegerFromBase::new, "integer-from-base(string,base)",
      arg(STRING_O, INTEGER_O), INTEGER_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGER_TO_BASE(ConvertIntegerToBase::new, "integer-to-base(number,base)",
      arg(INTEGER_O, INTEGER_O), STRING_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGER_TO_DATETIME(ConvertIntegerToDateTime::new, "integer-to-dateTime(ms)",
      arg(INTEGER_O), DATE_TIME_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGER_TO_DAYTIME(ConvertIntegerToDayTime::new, "integer-to-dayTime(ms)",
      arg(INTEGER_O), DAY_TIME_DURATION_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_DATETIME_TO_INTEGER(ConvertDateTimeToInteger::new, "dateTime-to-integer(date)",
      arg(DATE_TIME_O), INTEGER_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_DAYTIME_TO_INTEGER(ConvertDayTimeToInteger::new, "dayTime-to-integer(duration)",
      arg(DAY_TIME_DURATION_O), INTEGER_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_DECODE_KEY(ConvertDecodeKey::new, "decode-key(string[,lax])",
      arg(STRING_O, BOOLEAN_O), STRING_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_ENCODE_KEY(ConvertEncodeKey::new, "encode-key(string[,lax])",
      arg(STRING_O, BOOLEAN_O), STRING_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGERS_TO_BASE64(ConvertIntegersToBase64::new, "integers-to-base64(numbers)",
      arg(INTEGER_ZM), BASE64_BINARY_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGERS_TO_HEX(ConvertIntegersToHex::new, "integers-to-hex(numbers)",
      arg(INTEGER_ZM), HEX_BINARY_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_STRING_TO_BASE64(ConvertStringToBase64::new, "string-to-base64(string[,encoding])",
      arg(STRING_O, STRING_O), BASE64_BINARY_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_STRING_TO_HEX(ConvertStringToHex::new, "string-to-hex(string[,encoding])",
      arg(STRING_O, STRING_O), HEX_BINARY_O, CONVERT_URI),

  // Cryptographic Module

  /** XQuery function. */
  _CRYPTO_DECRYPT(CryptoDecrypt::new, "decrypt(data,type,key,algorithm)",
      arg(STRING_O, STRING_O, STRING_O, STRING_O), STRING_O, CRYPTO_URI),
  /** XQuery function. */
  _CRYPTO_ENCRYPT(CryptoEncrypt::new, "encrypt(data,type,key,algorithm)",
      arg(STRING_O, STRING_O, STRING_O, STRING_O), STRING_O, CRYPTO_URI),
  /** XQuery function. */
  _CRYPTO_HMAC(CryptoHmac::new, "hmac(data,key,algorithm[,encoding])",
      arg(STRING_O, STRING_O, STRING_O, STRING_ZO), STRING_O, CRYPTO_URI),
  /** XQuery function. */
  _CRYPTO_GENERATE_SIGNATURE(CryptoGenerateSignature::new, "generate-signature" +
      "(data,canonicalization,digest,signature,prefix,type[,item1][,item2])",
      arg(NODE_O, STRING_O, STRING_O, STRING_O, STRING_O, STRING_O, ITEM_ZO, ITEM_ZO),
      NODE_O, CRYPTO_URI),
  /** XQuery function. */
  _CRYPTO_VALIDATE_SIGNATURE(CryptoValidateSignature::new, "validate-signature(node)",
      arg(NODE_O), BOOLEAN_O, CRYPTO_URI),

  // CSV Module

  /** XQuery function. */
  _CSV_DOC(CsvDoc::new, "doc(uri[,options])",
      arg(STRING_O, MAP_ZO), ITEM_ZO, flag(NDT), CSV_URI),
  /** XQuery function. */
  _CSV_PARSE(CsvParse::new, "parse(string[,options])",
      arg(STRING_ZO, MAP_ZO), ITEM_ZO, CSV_URI),
  /** XQuery function. */
  _CSV_SERIALIZE(CsvSerialize::new, "serialize(item[,options])",
      arg(ITEM_ZO, ITEM_ZO), STRING_O, CSV_URI),

  // Database Module

  /** XQuery function. */
  _DB_ADD(DbAdd::new, "add(database,input[,path[,options]])",
      arg(STRING_O, ITEM_O, STRING_O, MAP_ZO), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_ALTER(DbAlter::new, "alter(database, new-name)",
      arg(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_ALTER_BACKUP(DbAlterBackup::new, "alter-backup(name, new-name)",
      arg(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_ATTRIBUTE(DbAttribute::new, "attribute(database,strings[,name])",
      arg(STRING_O, ITEM_ZM, STRING_O), ATTRIBUTE_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_ATTRIBUTE_RANGE(DbAttributeRange::new, "attribute-range(database,from,to[,name])",
      arg(STRING_O, ITEM_O, ITEM_O, STRING_O), ATTRIBUTE_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_BACKUPS(DbBackups::new, "backups([database])",
      arg(ITEM_O), ELEMENT_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_CONTENT_TYPE(DbContentType::new, "content-type(database,path)",
      arg(STRING_O, STRING_O), STRING_O, DB_URI),
  /** XQuery function. */
  _DB_COPY(DbCopy::new, "copy(database, new-name)",
      arg(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_CREATE(DbCreate::new, "create(name[,inputs[,paths[,options]]])",
      arg(STRING_O, ITEM_ZM, STRING_ZM, MAP_ZO), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_CREATE_BACKUP(DbCreateBackup::new, "create-backup(database)",
      arg(STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_DELETE(DbDelete::new, "delete(database,path)",
      arg(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_DIR(DbDir::new, "dir(database,path)",
      arg(STRING_O, STRING_O), ELEMENT_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_DROP(DbDrop::new, "drop(database)",
      arg(ITEM_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_DROP_BACKUP(DbDropBackup::new, "drop-backup(name)",
      arg(STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_EXISTS(DbExists::new, "exists(database[,path])",
      arg(STRING_O, STRING_O), BOOLEAN_O, DB_URI),
  /** XQuery function. */
  _DB_EXPORT(DbExport::new, "export(database,path[,param]])",
      arg(STRING_O, STRING_O, ITEM_O), EMPTY_SEQUENCE_Z, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_FLUSH(DbFlush::new, "flush(database)",
      arg(ITEM_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_INFO(DbInfo::new, "info(database)",
      arg(STRING_O), STRING_O, DB_URI),
  /** XQuery function. */
  _DB_IS_RAW(DbIsRaw::new, "is-raw(database,path)",
      arg(STRING_O, STRING_O), BOOLEAN_O, DB_URI),
  /** XQuery function. */
  _DB_IS_XML(DbIsXml::new, "is-xml(database,path)",
      arg(STRING_O, STRING_O), BOOLEAN_O, DB_URI),
  /** XQuery function. */
  _DB_LIST(DbList::new, "list([database[,path]])",
      arg(STRING_O, STRING_O), STRING_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_LIST_DETAILS(DbListDetails::new, "list-details([database[,path]])",
      arg(STRING_O, STRING_O), ELEMENT_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_NAME(DbName::new, "name(node)",
      arg(NODE_O), STRING_O, DB_URI),
  /** XQuery function. */
  _DB_NODE_ID(DbNodeId::new, "node-id(nodes)",
      arg(NODE_ZM), INTEGER_ZM, DB_URI),
  /** XQuery function. */
  _DB_NODE_PRE(DbNodePre::new, "node-pre(nodes)",
      arg(NODE_ZM), INTEGER_ZM, DB_URI),
  /** XQuery function. */
  _DB_OPEN(DbOpen::new, "open(database[,path])",
      arg(STRING_O, STRING_O), DOCUMENT_NODE_ZM, DB_URI),
  /** XQuery function. */
  _DB_OPEN_ID(DbOpenId::new, "open-id(database,ids)",
      arg(STRING_O, INTEGER_ZM), NODE_ZM, DB_URI),
  /** XQuery function. */
  _DB_OPEN_PRE(DbOpenPre::new, "open-pre(database,pres)",
      arg(STRING_O, INTEGER_ZM), NODE_ZM, DB_URI),
  /** XQuery function. */
  _DB_OPTIMIZE(DbOptimize::new, "optimize(database[,all[,options]])",
      arg(STRING_O, BOOLEAN_O, MAP_ZO), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_OPTION(DbOption::new, "option(name)",
      arg(STRING_ZO), ITEM_O, DB_URI),
  /** XQuery function. */
  _DB_PATH(DbPath::new, "path(node)",
      arg(NODE_O), STRING_O, DB_URI),
  /** XQuery function. */
  _DB_PROPERTY(DbProperty::new, "property(database,name)",
      arg(STRING_O, STRING_O), ANY_ATOMIC_TYPE_O, DB_URI),
  /** XQuery function. */
  _DB_RENAME(DbRename::new, "rename(database,path,new-path)",
      arg(STRING_O, STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_REPLACE(DbReplace::new, "replace(database,path,input[,options])",
      arg(STRING_O, STRING_O, ITEM_O, MAP_ZO), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_RESTORE(DbRestore::new, "restore(backup)",
      arg(STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_RETRIEVE(DbRetrieve::new, "retrieve(database,path)",
      arg(STRING_O, STRING_O), BASE64_BINARY_O, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_STORE(DbStore::new, "store(database,path,input)",
      arg(STRING_O, STRING_O, ITEM_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_SYSTEM(DbSystem::new, "system()",
      arg(), STRING_O, DB_URI),
  /** XQuery function. */
  _DB_TEXT(DbText::new, "text(database,strings)",
      arg(STRING_O, ITEM_ZM), TEXT_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_TEXT_RANGE(DbTextRange::new, "text-range(database,from,to)",
      arg(STRING_O, ITEM_O, ITEM_O), TEXT_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_TOKEN(DbToken::new, "token(database,strings[,name])",
      arg(STRING_O, ITEM_ZM, STRING_O), ATTRIBUTE_ZM, flag(NDT), DB_URI),

  // Fetch Module

  /** XQuery function. */
  _FETCH_BINARY(FetchBinary::new, "binary(uri)",
      arg(STRING_O), BASE64_BINARY_O, flag(NDT), FETCH_URI),
  /** XQuery function. */
  _FETCH_CONTENT_TYPE(FetchContentType::new, "content-type(uri)",
      arg(STRING_O), STRING_O, flag(NDT), FETCH_URI),
  /** XQuery function. */
  _FETCH_TEXT(FetchText::new, "text(uri[,encoding[,fallback]])",
      arg(STRING_O, STRING_O, BOOLEAN_O), STRING_O, flag(NDT), FETCH_URI),
  /** XQuery function. */
  _FETCH_XML(FetchXml::new, "xml(uri[,options])",
      arg(STRING_O, MAP_ZO), DOCUMENT_NODE_O, flag(NDT), FETCH_URI),
  /** XQuery function. */
  _FETCH_XML_BINARY(FetchXmlBinary::new, "xml-binary(binary[,options])",
      arg(BASE64_BINARY_O, MAP_ZO), DOCUMENT_NODE_O, flag(NDT), FETCH_URI),

  // File Module

  /** XQuery function. */
  _FILE_APPEND(FileAppend::new, "append(path,data[,params])",
      arg(STRING_O, ITEM_ZM, ITEM_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_APPEND_BINARY(FileAppendBinary::new, "append-binary(path,item)",
      arg(STRING_O, BINARY_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_APPEND_TEXT(FileAppendText::new, "append-text(path,text[,encoding])",
      arg(STRING_O, STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_APPEND_TEXT_LINES(FileAppendTextLines::new, "append-text-lines(path,texts[,encoding])",
      arg(STRING_O, STRING_ZM, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_BASE_DIR(FileBaseDir::new, "base-dir()",
      arg(), STRING_O, FILE_URI),
  /** XQuery function. */
  _FILE_CHILDREN(FileChildren::new, "children(path)",
      arg(STRING_O), STRING_ZM, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_COPY(FileCopy::new, "copy(source,target)",
      arg(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_CREATE_DIR(FileCreateDir::new, "create-dir(path)",
      arg(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_CREATE_TEMP_DIR(FileCreateTempDir::new, "create-temp-dir(prefix,suffix[,dir])",
      arg(STRING_O, STRING_O, STRING_O), STRING_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_CREATE_TEMP_FILE(FileCreateTempFile::new, "create-temp-file(prefix,suffix[,dir])",
      arg(STRING_O, STRING_O, STRING_O), STRING_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_CURRENT_DIR(FileCurrentDir::new, "current-dir()",
      arg(), STRING_O, FILE_URI),
  /** XQuery function. */
  _FILE_DELETE(FileDelete::new, "delete(path[,recursive])",
      arg(STRING_O, BOOLEAN_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_DESCENDANTS(FileDescendants::new, "descendants(path)",
      arg(STRING_O), STRING_ZM, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_DIR_SEPARATOR(FileDirSeparator::new, "dir-separator()",
      arg(), STRING_O, FILE_URI),
  /** XQuery function. */
  _FILE_EXISTS(FileExists::new, "exists(path)",
      arg(STRING_O), BOOLEAN_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_IS_ABSOLUTE(FileIsAbsolute::new, "is-absolute(path)",
      arg(STRING_O), BOOLEAN_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_IS_DIR(FileIsDir::new, "is-dir(path)",
      arg(STRING_O), BOOLEAN_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_IS_FILE(FileIsFile::new, "is-file(path)",
      arg(STRING_O), BOOLEAN_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_LAST_MODIFIED(FileLastModified::new, "last-modified(path)",
      arg(STRING_O), DATE_TIME_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_LINE_SEPARATOR(FileLineSeparator::new, "line-separator()",
      arg(), STRING_O, FILE_URI),
  /** XQuery function. */
  _FILE_LIST(FileList::new, "list(path[,recursive[,pattern]])",
      arg(STRING_O, BOOLEAN_O, STRING_O), STRING_ZM, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_MOVE(FileMove::new, "move(source,target)",
      arg(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_NAME(FileName::new, "name(path)",
      arg(STRING_O), STRING_O, FILE_URI),
  /** XQuery function. */
  _FILE_PARENT(FileParent::new, "parent(path)",
      arg(STRING_O), STRING_ZO, FILE_URI),
  /** XQuery function. */
  _FILE_PATH_SEPARATOR(FilePathSeparator::new, "path-separator()",
      arg(), STRING_O, FILE_URI),
  /** XQuery function. */
  _FILE_PATH_TO_NATIVE(FilePathToNative::new, "path-to-native(path)",
      arg(STRING_O), STRING_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_PATH_TO_URI(FilePathToUri::new, "path-to-uri(path)",
      arg(STRING_O), ANY_URI_O, FILE_URI),
  /** XQuery function. */
  _FILE_READ_BINARY(FileReadBinary::new, "read-binary(path[,offset[,length]])",
      arg(STRING_O, INTEGER_O, INTEGER_O), BASE64_BINARY_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_READ_TEXT(FileReadText::new, "read-text(path[,encoding[,fallback]])",
      arg(STRING_O, STRING_O, BOOLEAN_O), STRING_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_READ_TEXT_LINES(FileReadTextLines::new,
      "read-text-lines(path[,encoding[,fallback[,offset[,length]]]])",
      arg(STRING_O, STRING_O, BOOLEAN_O, INTEGER_O, INTEGER_O), STRING_ZM, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_RESOLVE_PATH(FileResolvePath::new, "resolve-path(path[,base])",
      arg(STRING_O, STRING_O), STRING_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_SIZE(FileSize::new, "size(path)",
      arg(STRING_O), INTEGER_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_TEMP_DIR(FileTempDir::new, "temp-dir()",
      arg(), STRING_O, FILE_URI),
  /** XQuery function. */
  _FILE_WRITE(FileWrite::new, "write(path,data[,params])",
      arg(STRING_O, ITEM_ZM, ITEM_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_WRITE_BINARY(FileWriteBinary::new, "write-binary(path,item[,offset])",
      arg(STRING_O, BINARY_O, INTEGER_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_WRITE_TEXT(FileWriteText::new, "write-text(path,text[,encoding])",
      arg(STRING_O, STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_WRITE_TEXT_LINES(FileWriteTextLines::new, "write-text-lines(path,texts[,encoding])",
      arg(STRING_O, STRING_ZM, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI),

  // Fulltext Module

  /** XQuery function. */
  _FT_CONTAINS(FtContains::new, "contains(input,terms[,options])",
      arg(ITEM_ZM, ITEM_ZM, MAP_ZO), BOOLEAN_O, flag(NDT), FT_URI),
  /** XQuery function. */
  _FT_COUNT(FtCount::new, "count(nodes)",
      arg(NODE_ZM), INTEGER_O, FT_URI),
  /** XQuery function. */
  _FT_EXTRACT(FtExtract::new, "extract(nodes[,name[,length]])",
      arg(ITEM_ZM, STRING_O, INTEGER_O), NODE_ZM, FT_URI),
  /** XQuery function. */
  _FT_MARK(FtMark::new, "mark(nodes[,name])",
      arg(NODE_ZM, STRING_O), NODE_ZM, FT_URI),
  /** XQuery function. */
  _FT_NORMALIZE(FtNormalize::new, "normalize(string[,options])",
      arg(STRING_ZO, MAP_ZO), STRING_O, FT_URI),
  /** XQuery function. */
  _FT_SCORE(FtScore::new, "score(items)",
      arg(ITEM_ZM), DOUBLE_ZM, FT_URI),
  /** XQuery function. */
  _FT_SEARCH(FtSearch::new, "search(database,terms[,options])",
      arg(STRING_O, ITEM_ZM, MAP_ZO), TEXT_ZM, flag(NDT), FT_URI),
  /** XQuery function. */
  _FT_TOKENIZE(FtTokenize::new, "tokenize(string[,options])",
      arg(STRING_ZO, MAP_ZO), STRING_ZM, FT_URI),
  /** XQuery function. */
  _FT_TOKENS(FtTokens::new, "tokens(database[,prefix])",
      arg(STRING_O, STRING_O), ELEMENT_ZM, flag(NDT), FT_URI),

  // Hash Module

  /** XQuery function. */
  _HASH_HASH(HashHash::new, "hash(value,algorithm)",
      arg(ANY_ATOMIC_TYPE_O, STRING_O), BASE64_BINARY_O, HASH_URI),
  /** XQuery function. */
  _HASH_MD5(HashMd5::new, "md5(value)",
      arg(ANY_ATOMIC_TYPE_O), BASE64_BINARY_O, HASH_URI),
  /** XQuery function. */
  _HASH_SHA1(HashSha1::new, "sha1(value)",
      arg(ANY_ATOMIC_TYPE_O), BASE64_BINARY_O, HASH_URI),
  /** XQuery function. */
  _HASH_SHA256(HashSha256::new, "sha256(value)",
      arg(ANY_ATOMIC_TYPE_O), BASE64_BINARY_O, HASH_URI),

  // HOF Module

  /** XQuery function. */
  _HOF_CONST(HofConst::new, "const(return,ignore)",
      arg(ITEM_ZM, ITEM_ZM), ITEM_ZM, HOF_URI),
  /** XQuery function. */
  _HOF_DROP_WHILE(HofDropWhile::new, "drop-while(items,predicate)",
      arg(ITEM_ZM, FuncType.get(BOOLEAN_O, ITEM_O).seqType()), ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_FOLD_LEFT1(HofFoldLeft1::new, "fold-left1(non-empty-items,function)",
      arg(ITEM_OM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM_O).seqType()), ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_ID(HofId::new, "id(value)",
      arg(ITEM_ZM), ITEM_ZM, HOF_URI),
  /** XQuery function. */
  _HOF_SCAN_LEFT(HofScanLeft::new, "scan-left(items,zero,function)",
      arg(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM_O).seqType()),
      ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_SORT_WITH(HofSortWith::new, "sort-with(items,function)",
      arg(ITEM_ZM, FuncType.get(BOOLEAN_O, ITEM_O, ITEM_O).seqType()), ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_TAKE_WHILE(HofTakeWhile::new, "take-while(items,predicate)",
      arg(ITEM_ZM, FuncType.get(BOOLEAN_O, ITEM_O).seqType()), ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_TOP_K_BY(HofTopKBy::new, "top-k-by(items,function,k)",
      arg(ITEM_ZM, FuncType.get(ITEM_O, ITEM_O).seqType(), INTEGER_O), ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_TOP_K_WITH(HofTopKWith::new, "top-k-with(items,function,k)",
      arg(ITEM_ZM, FuncType.get(BOOLEAN_O, ITEM_ZO, ITEM_ZO).seqType(), INTEGER_O),
      ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_UNTIL(HofUntil::new, "until(predicate,function,start)",
      arg(FuncType.get(BOOLEAN_O, ITEM_ZM).seqType(),
      FuncType.get(ITEM_ZM, ITEM_ZM).seqType(), ITEM_ZM), ITEM_ZM, flag(HOF), HOF_URI),

  // HTML Module

  /** XQuery function. */
  _HTML_DOC(HtmlDoc::new, "doc(uri[,options])",
      arg(STRING_O, MAP_ZO), ITEM_ZO, flag(NDT), HTML_URI),
  /** XQuery function. */
  _HTML_PARSE(HtmlParse::new, "parse(string[,options])",
      arg(STRING_ZO, MAP_ZO), DOCUMENT_NODE_ZO, HTML_URI),
  /** XQuery function. */
  _HTML_PARSER(HtmlParser::new, "parser()",
      arg(), STRING_O, HTML_URI),

  // HTTP Module

  /** XQuery function. */
  _HTTP_SEND_REQUEST(HttpSendRequest::new, "send-request(request[,href[,bodies]])",
      arg(NODE_O, STRING_ZO, ITEM_ZM), ITEM_ZM, flag(NDT), HTTP_URI),

  // Index Module

  /** XQuery function. */
  _INDEX_ATTRIBUTE_NAMES(IndexAttributeNames::new, "attribute-names(database)",
      arg(STRING_O), ELEMENT_ZM, flag(NDT), INDEX_URI),
  /** XQuery function. */
  _INDEX_ATTRIBUTES(IndexAttributes::new, "attributes(database[,prefix[,ascending]])",
      arg(STRING_O, STRING_O, BOOLEAN_O), ELEMENT_ZM, flag(NDT), INDEX_URI),
  /** XQuery function. */
  _INDEX_ELEMENT_NAMES(IndexElementNames::new, "element-names(database)",
      arg(STRING_O), ELEMENT_ZM, flag(NDT), INDEX_URI),
  /** XQuery function. */
  _INDEX_FACETS(IndexFacets::new, "facets(database[,type])",
      arg(STRING_O, STRING_O), DOCUMENT_NODE_O, flag(NDT), INDEX_URI),
  /** XQuery function. */
  _INDEX_TEXTS(IndexTexts::new, "texts(database[,prefix[,ascending]])",
      arg(STRING_O, STRING_O, BOOLEAN_O), ELEMENT_ZM, flag(NDT), INDEX_URI),
  /** XQuery function. */
  _INDEX_TOKENS(IndexTokens::new, "tokens(database)",
      arg(STRING_O), ELEMENT_ZM, flag(NDT), INDEX_URI),

  // Inspection Module

  /** XQuery function. */
  _INSPECT_CONTEXT(InspectContext::new, "context()",
      arg(), ELEMENT_O, INSPECT_URI),
  /** XQuery function. */
  _INSPECT_FUNCTION(InspectFunction::new, "function(function)",
      arg(STRING_O), ELEMENT_O, flag(HOF), INSPECT_URI),
  /** XQuery function. */
  _INSPECT_FUNCTION_ANNOTATIONS(InspectFunctionAnnotations::new, "function-annotations(function)",
      arg(FUNCTION_O), MAP_ZO, INSPECT_URI),
  /** XQuery function. */
  _INSPECT_FUNCTIONS(InspectFunctions::new, "functions([uri])",
      arg(STRING_O), FUNCTION_ZM, flag(HOF), INSPECT_URI),
  /** XQuery function. */
  _INSPECT_MODULE(InspectModule::new, "module(uri)",
      arg(STRING_O), ELEMENT_O, INSPECT_URI),
  /** XQuery function. */
  _INSPECT_TYPE(InspectType::new, "type(value)",
      arg(ITEM_ZM), STRING_O, INSPECT_URI),
  /** XQuery function. */
  _INSPECT_STATIC_CONTEXT(InspectStaticContext::new, "static-context(function,name)",
      arg(FUNCTION_O, STRING_O), ITEM_ZM, INSPECT_URI),
  /** XQuery function. */
  _INSPECT_XQDOC(InspectXqdoc::new, "xqdoc(uri)",
      arg(STRING_O), ELEMENT_O, INSPECT_URI),

  // Jobs Module

  /** XQuery function. */
  _JOBS_CURRENT(JobsCurrent::new, "current()",
      arg(), STRING_O, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_EVAL(JobsEval::new, "eval(string[,bindings[,options]])",
      arg(STRING_O, MAP_ZO, MAP_ZO), STRING_O, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_FINISHED(JobsFinished::new, "finished(id)",
      arg(STRING_O), BOOLEAN_O, flag(NDT), JOBS_URI),
  /** XQuery function (legacy, now: jobs:eval). */
  _JOBS_INVOKE(JobsInvoke::new, "invoke(uri[,bindings[,options]])",
      arg(STRING_O, MAP_ZO, MAP_ZO), STRING_O, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_LIST(JobsList::new, "list()",
      arg(), STRING_ZM, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_LIST_DETAILS(JobsListDetails::new, "list-details([id])",
      arg(STRING_O), ELEMENT_ZM, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_RESULT(JobsResult::new, "result(id)",
      arg(STRING_O), ITEM_ZM, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_SERVICES(JobsServices::new, "services()",
      arg(), ELEMENT_ZM, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_STOP(JobsStop::new, "stop(id[,options])",
      arg(STRING_O, MAP_ZO), EMPTY_SEQUENCE_Z, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_WAIT(JobsWait::new, "wait(id)",
      arg(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), JOBS_URI),

  // JSON Module

  /** XQuery function. */
  _JSON_DOC(JsonDoc::new, "doc(uri[,options])",
      arg(STRING_O, MAP_ZO), ITEM_ZO, flag(NDT), JSON_URI),
  /** XQuery function. */
  _JSON_PARSE(JsonParse::new, "parse(string[,options])",
      arg(STRING_ZO, MAP_ZO), ITEM_ZO, JSON_URI),
  /** XQuery function. */
  _JSON_SERIALIZE(JsonSerialize::new, "serialize(items[,options])",
      arg(ITEM_ZO, MAP_ZO), STRING_O, JSON_URI),

  // Lazy Module

  /** XQuery function. */
  _LAZY_CACHE(LazyCache::new, "cache(value[,lazy])",
      arg(ITEM_ZM, BOOLEAN_O), ITEM_ZM, LAZY_URI),
  /** XQuery function. */
  _LAZY_IS_CACHED(LazyIsCached::new, "is-cached(item)",
      arg(ITEM_O), BOOLEAN_O, LAZY_URI),
  /** XQuery function. */
  _LAZY_IS_LAZY(LazyIsLazy::new, "is-lazy(item)",
      arg(ITEM_O), BOOLEAN_O, LAZY_URI),

  // Output Module

  /** XQuery function. */
  _OUT_CR(OutCr::new, "cr()",
      arg(), STRING_O, OUT_URI),
  /** XQuery function. */
  _OUT_FORMAT(OutFormat::new, "format(format,item1[,...])",
      arg(STRING_O, ITEM_O), STRING_O, OUT_URI),
  /** XQuery function. */
  _OUT_NL(OutNl::new, "nl()",
      arg(), STRING_O, OUT_URI),
  /** XQuery function. */
  _OUT_TAB(OutTab::new, "tab()",
      arg(), STRING_O, OUT_URI),

  // Process Module

  /** XQuery function. */
  _PROC_EXECUTE(ProcExecute::new, "execute(command[,args[,options]])",
      arg(STRING_O, STRING_ZM, ANY_ATOMIC_TYPE_O), ELEMENT_O, flag(NDT), PROC_URI),
  /** XQuery function. */
  _PROC_FORK(ProcFork::new, "fork(command[,args[,options]])",
      arg(STRING_O, STRING_ZM, ANY_ATOMIC_TYPE_O), EMPTY_SEQUENCE_Z, flag(NDT), PROC_URI),
  /** XQuery function. */
  _PROC_PROPERTY(ProcProperty::new, "property(name)",
      arg(STRING_O), STRING_ZO, flag(NDT), PROC_URI),
  /** XQuery function. */
  _PROC_PROPERTY_NAMES(ProcPropertyNames::new, "property-names()",
      arg(), STRING_ZM, flag(NDT), PROC_URI),
  /** XQuery function. */
  _PROC_SYSTEM(ProcSystem::new, "system(command[,args[,options]])",
      arg(STRING_O, STRING_ZM, ANY_ATOMIC_TYPE_O), STRING_O, flag(NDT), PROC_URI),

  // Profiling Module

  /** XQuery function. */
  _PROF_CURRENT_MS(ProfCurrentMs::new, "current-ms()",
      arg(), INTEGER_O, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_CURRENT_NS(ProfCurrentNs::new, "current-ns()",
      arg(), INTEGER_O, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_DUMP(ProfDump::new, "dump(value[,label])",
      arg(ITEM_ZM, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_GC(ProfGc::new, "gc([count])",
      arg(INTEGER_O), EMPTY_SEQUENCE_Z, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_HUMAN(ProfHuman::new, "human(integer)",
      arg(INTEGER_O), STRING_O, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_MEMORY(ProfMemory::new, "memory(value[,label])",
      arg(ITEM_ZM, STRING_O), ITEM_ZM, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_SLEEP(ProfSleep::new, "sleep(ms)",
      arg(INTEGER_O), EMPTY_SEQUENCE_Z, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_RUNTIME(ProfRuntime::new, "runtime(name)",
      arg(STRING_O), ITEM_O, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_TIME(ProfTime::new, "time(value[,label])",
      arg(ITEM_ZM, STRING_O), ITEM_ZM, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_TRACK(ProfTrack::new, "track(value[,options])",
      arg(ITEM_ZM, MAP_ZO), MAP_O, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_TYPE(ProfType::new, "type(value)",
      arg(ITEM_ZM), ITEM_ZM, PROF_URI),
  /** XQuery function. */
  _PROF_VARIABLES(ProfVariables::new, "variables()",
      arg(), EMPTY_SEQUENCE_Z, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_VOID(ProfVoid::new, "void(value)",
      arg(ITEM_ZM), EMPTY_SEQUENCE_Z, flag(NDT), PROF_URI),

  // Random Module

  /** XQuery function. */
  _RANDOM_DOUBLE(RandomDouble::new, "double()",
      arg(), DOUBLE_O, flag(NDT), RANDOM_URI),
  /** XQuery function. */
  _RANDOM_GAUSSIAN(RandomGaussian::new, "gaussian(number)",
      arg(INTEGER_O), DOUBLE_ZM, flag(NDT), RANDOM_URI),
  /** XQuery function. */
  _RANDOM_INTEGER(RandomInteger::new, "integer([max])",
      arg(INTEGER_O), INTEGER_O, flag(NDT), RANDOM_URI),
  /** XQuery function. */
  _RANDOM_SEEDED_DOUBLE(RandomSeededDouble::new, "seeded-double(seed,number)",
      arg(INTEGER_O, INTEGER_O), DOUBLE_ZM, RANDOM_URI),
  /** XQuery function. */
  _RANDOM_SEEDED_INTEGER(RandomSeededInteger::new, "seeded-integer(seed,number[,max])",
      arg(INTEGER_O, INTEGER_O, INTEGER_O), INTEGER_ZM, RANDOM_URI),
  /** XQuery function. */
  _RANDOM_SEEDED_PERMUTATION(RandomSeededPermutation::new, "seeded-permutation(seed,items)",
      arg(INTEGER_O, ITEM_ZM), ITEM_ZM, RANDOM_URI),
  /** XQuery function. */
  _RANDOM_UUID(RandomUuid::new, "uuid()",
      arg(), STRING_O, flag(NDT), RANDOM_URI),

  // Repository Module

  /** XQuery function. */
  _REPO_DELETE(RepoDelete::new, "delete(uri)",
      arg(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), REPO_URI),
  /** XQuery function. */
  _REPO_INSTALL(RepoInstall::new, "install(uri)",
      arg(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), REPO_URI),
  /** XQuery function. */
  _REPO_LIST(RepoList::new, "list()",
      arg(), STRING_ZM, flag(NDT), REPO_URI),

  // SQL Module

  /** XQuery function. */
  _SQL_CLOSE(SqlClose::new, "close(id)",
      arg(INTEGER_O), EMPTY_SEQUENCE_Z, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_COMMIT(SqlCommit::new, "commit(id)",
      arg(INTEGER_O), EMPTY_SEQUENCE_Z, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_CONNECT(SqlConnect::new, "connect(url[,user[,pass[,options]]]]])",
      arg(STRING_O, STRING_O, STRING_O, MAP_ZO), INTEGER_O, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_EXECUTE(SqlExecute::new, "execute(id,query[,options])",
      arg(INTEGER_O, STRING_O, MAP_ZO), ITEM_ZM, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_EXECUTE_PREPARED(SqlExecutePrepared::new, "execute-prepared(id[,params[,options]])",
      arg(INTEGER_O, ELEMENT_O, MAP_ZO), ITEM_ZM, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_INIT(SqlInit::new, "init(class)",
      arg(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_PREPARE(SqlPrepare::new, "prepare(id,statement)",
      arg(INTEGER_O, STRING_O), INTEGER_O, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_ROLLBACK(SqlRollback::new, "rollback(id)",
      arg(INTEGER_O), EMPTY_SEQUENCE_Z, flag(NDT), SQL_URI),

  // Strings Module

  /** XQuery function. */
  _STRINGS_COLOGNE_PHONETIC(StringsColognePhonetic::new, "cologne-phonetic(string)",
      arg(STRING_O), STRING_O, STRINGS_URI),
  /** XQuery function. */
  _STRINGS_LEVENSHTEIN(StringsLevenshtein::new, "levenshtein(string1,string2)",
      arg(STRING_O, STRING_O), DOUBLE_O, STRINGS_URI),
  /** XQuery function. */
  _STRINGS_SOUNDEX(StringsSoundex::new, "soundex(string)",
      arg(STRING_O), STRING_O, STRINGS_URI),

  // Unit Module

  /** XQuery function. */
  _UNIT_ASSERT(UnitAssert::new, "assert(test[,failure])",
      arg(ITEM_ZM, ITEM_O), ITEM_ZM, flag(NDT), UNIT_URI),
  /** XQuery function. */
  _UNIT_ASSERT_EQUALS(UnitAssertEquals::new, "assert-equals(result,expected[,failure])",
      arg(ITEM_ZM, ITEM_ZM, ITEM_O), ITEM_ZM, flag(NDT), UNIT_URI),
  /** XQuery function. */
  _UNIT_FAIL(UnitFail::new, "fail([failure])",
      arg(ITEM_O), ITEM_ZM, flag(NDT), UNIT_URI),

  // Update Module

  /** XQuery function. */
  _UPDATE_APPLY(UpdateApply::new, "apply(function,args)",
      arg(FUNCTION_O, ARRAY_O), EMPTY_SEQUENCE_Z,
      flag(POS, CTX, UPD, HOF), UPDATE_URI),
  /** XQuery function. */
  _UPDATE_CACHE(UpdateCache::new, "cache([reset])",
      arg(BOOLEAN_O), ITEM_ZM, flag(NDT), UPDATE_URI),
  /** XQuery function. */
  _UPDATE_FOR_EACH(UpdateForEach::new, "for-each(items,function)",
      arg(ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O).seqType()),
      EMPTY_SEQUENCE_Z, flag(UPD, HOF), UPDATE_URI),
  /** XQuery function. */
  _UPDATE_FOR_EACH_PAIR(UpdateForEachPair::new, "for-each-pair(items1,items2,function)",
      arg(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O, ITEM_O).seqType()),
      EMPTY_SEQUENCE_Z, flag(UPD, HOF), UPDATE_URI),
  /** XQuery function. */
  _UPDATE_MAP_FOR_EACH(UpdateMapForEach::new, "map-for-each(map,function)",
      arg(MAP_O, FuncType.get(ITEM_ZM, ANY_ATOMIC_TYPE_O, ITEM_ZM).seqType()),
      EMPTY_SEQUENCE_Z, flag(UPD, HOF), UPDATE_URI),
  /** XQuery function. */
  _UPDATE_OUTPUT(UpdateOutput::new, "output(items)",
      arg(ITEM_ZM), EMPTY_SEQUENCE_Z, flag(UPD), UPDATE_URI),

  // User Module

  /** XQuery function. */
  _USER_ALTER(UserAlter::new, "alter(name,newname)",
      arg(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), USER_URI),
  /** XQuery function. */
  _USER_CHECK(UserCheck::new, "check(name,password)",
      arg(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), USER_URI),
  /** XQuery function. */
  _USER_CREATE(UserCreate::new, "create(name,password[,permissions[,patterns[,info]]])",
      arg(STRING_O, STRING_O, STRING_ZM, STRING_ZM, ELEMENT_O),
      EMPTY_SEQUENCE_Z, flag(UPD), USER_URI),
  /** XQuery function. */
  _USER_CURRENT(UserCurrent::new, "current()",
      arg(), STRING_O, USER_URI),
  /** XQuery function. */
  _USER_DROP(UserDrop::new, "drop(name[,patterns])",
      arg(STRING_O, STRING_ZM), EMPTY_SEQUENCE_Z, flag(UPD), USER_URI),
  /** XQuery function. */
  _USER_EXISTS(UserExists::new, "exists(name)",
      arg(STRING_O), BOOLEAN_O, flag(NDT), USER_URI),
  /** XQuery function. */
  _USER_GRANT(UserGrant::new, "grant(name,permissions[,patterns])",
      arg(STRING_O, STRING_ZM, STRING_ZM), EMPTY_SEQUENCE_Z, flag(UPD), USER_URI),
  /** XQuery function. */
  _USER_INFO(UserInfo::new, "info([name])",
      arg(STRING_O), ELEMENT_O, USER_URI),
  /** XQuery function. */
  _USER_LIST(UserList::new, "list()",
      arg(), ELEMENT_ZM, flag(NDT), USER_URI),
  /** XQuery function. */
  _USER_LIST_DETAILS(UserListDetails::new, "list-details([name])",
      arg(STRING_O), ELEMENT_ZM, flag(NDT), USER_URI),
  /** XQuery function. */
  _USER_PASSWORD(UserPassword::new, "password(name,password)",
      arg(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), USER_URI),
  /** XQuery function. */
  _USER_UPDATE_INFO(UserUpdateInfo::new, "update-info(element[,name])",
      arg(ELEMENT_O, STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), USER_URI),

  // Utility Module

  /** XQuery function. */
  _UTIL_ARRAY_MEMBERS(UtilArrayMembers::new, "array-members(array)",
      arg(ARRAY_O), ARRAY_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_ARRAY_VALUES(UtilArrayValues::new, "array-values(array)",
      arg(ARRAY_O), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_CHARS(UtilChars::new, "chars(string)",
      arg(STRING_O), STRING_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_DEEP_EQUAL(UtilDeepEqual::new, "deep-equal(items1,items2[,options])",
      arg(ITEM_ZM, ITEM_ZM, STRING_ZM), BOOLEAN_O, UTIL_URI),
  /** XQuery function. */
  _UTIL_DDO(UtilDdo::new, "ddo(nodes)",
      arg(NODE_ZM), NODE_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_DUPLICATES(UtilDuplicates::new, "duplicates(items[,collation])",
      arg(ANY_ATOMIC_TYPE_ZM, STRING_O), ANY_ATOMIC_TYPE_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_IF(UtilIf::new, "if(condition,then[,else])",
      arg(ITEM_ZM, ITEM_ZM, ITEM_ZM), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_INIT(UtilInit::new, "init(items)",
      arg(ITEM_ZM), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_INTERSPERSE(UtilIntersperse::new, "intersperse(items[,separator])",
      arg(ITEM_ZM, ITEM_ZM), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_ITEM(UtilItem::new, "item(items,position)",
      arg(ITEM_ZM, DOUBLE_O), ITEM_ZO, UTIL_URI),
  /** XQuery function. */
  _UTIL_LAST(UtilLast::new, "last(items)",
      arg(ITEM_ZM), ITEM_ZO, UTIL_URI),
  /** XQuery function. */
  _UTIL_MAP_ENTRIES(UtilMapEntries::new, "map-entries(map)",
      arg(MAP_O), MAP_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_MAP_VALUES(UtilMapValues::new, "map-values(map)",
      arg(MAP_O), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_OR(UtilOr::new, "or(items,default)",
      arg(ITEM_ZM, ITEM_ZM), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_RANGE(UtilRange::new, "range(items,first,last)",
      arg(ITEM_ZM, DOUBLE_O, DOUBLE_O), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_REPLICATE(UtilReplicate::new, "replicate(items,count[,multiple])",
      arg(ITEM_ZM, INTEGER_O, BOOLEAN_O), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_ROOT(UtilRoot::new, "root(nodes)",
      arg(NODE_ZM), DOCUMENT_NODE_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_WITHIN(UtilWithin::new, "within(items,min[,max])",
      arg(ITEM_ZM, INTEGER_O, INTEGER_O), BOOLEAN_O, UTIL_URI),

  // Validate Module

  /** XQuery function. */
  _VALIDATE_DTD(ValidateDtd::new, "dtd(input[,schema])",
      arg(ITEM_O, ITEM_O), EMPTY_SEQUENCE_Z, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_DTD_INFO(ValidateDtdInfo::new, "dtd-info(input[,schema])",
      arg(ITEM_O, ITEM_O), STRING_ZM, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_DTD_REPORT(ValidateDtdReport::new, "dtd-report(input[,schema])",
      arg(ITEM_O, ITEM_O), ELEMENT_O, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_RNG(ValidateRng::new, "rng(input,schema[,compact])",
      arg(ITEM_O, ITEM_O, BOOLEAN_O), STRING_ZM, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_RNG_INFO(ValidateRngInfo::new, "rng-info(input,schema[,compact])",
      arg(ITEM_O, ITEM_O, BOOLEAN_O), STRING_ZM, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_RNG_REPORT(ValidateRngReport::new, "rng-report(input,schema[,compact])",
      arg(ITEM_O, ITEM_O, BOOLEAN_O), ELEMENT_O, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_XSD(ValidateXsd::new, "xsd(input[,schema[,options]])",
      arg(ITEM_O, ITEM_O, MAP_O), EMPTY_SEQUENCE_Z, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_XSD_INFO(ValidateXsdInfo::new, "xsd-info(input[,schema[,options]])",
      arg(ITEM_O, ITEM_O, MAP_O), STRING_ZM, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_XSD_PROCESSOR(ValidateXsdProcessor::new, "xsd-processor()",
      arg(), STRING_O, VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_XSD_REPORT(ValidateXsdReport::new, "xsd-report(input[,schema[,options]])",
      arg(ITEM_O, ITEM_O, MAP_O), ELEMENT_O, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_XSD_VERSION(ValidateXsdVersion::new, "xsd-version()",
      arg(), STRING_O, VALIDATE_URI),

  // Web Module

  /** XQuery function. */
  _WEB_CONTENT_TYPE(WebContentType::new, "content-type(path)",
      arg(STRING_O), STRING_O, WEB_URI),
  /** XQuery function. */
  _WEB_CREATE_URL(WebCreateUrl::new, "create-url(url,params[,anchor])",
      arg(STRING_O, MAP_O, STRING_O), STRING_O, WEB_URI),
  /** XQuery function. */
  _WEB_DECODE_URL(WebDecodeUrl::new, "decode-url(string)",
      arg(STRING_O), STRING_O, WEB_URI),
  /** XQuery function. */
  _WEB_ENCODE_URL(WebEncodeUrl::new, "encode-url(string)",
      arg(STRING_O), STRING_O, WEB_URI),
  /** XQuery function. */
  _WEB_ERROR(WebError::new, "error(code[,message])",
      arg(INTEGER_O, STRING_O), ITEM_ZM, flag(NDT), WEB_URI),
  /** XQuery function. */
  _WEB_FORWARD(WebForward::new, "forward(location[,params])",
      arg(STRING_O, MAP_O), ELEMENT_O, WEB_URI),
  /** XQuery function. */
  _WEB_REDIRECT(WebRedirect::new, "redirect(location[,params[,anchor]])",
      arg(STRING_O, MAP_O, STRING_O), ELEMENT_O, WEB_URI),
  /** XQuery function. */
  _WEB_RESPONSE_HEADER(WebResponseHeader::new, "response-header([output[,headers[,attributes]]])",
      arg(MAP_ZO, MAP_ZO, MAP_ZO), ELEMENT_O, WEB_URI),

  // XQuery Module

  /** XQuery function. */
  _XQUERY_EVAL(XQueryEval::new, "eval(string[,bindings[,options]])",
      arg(STRING_O, MAP_ZO, MAP_ZO), ITEM_ZM, flag(NDT), XQUERY_URI),
  /** XQuery function. */
  _XQUERY_EVAL_UPDATE(XQueryEvalUpdate::new, "eval-update(string[,bindings[,options]])",
      arg(STRING_O, MAP_ZO, MAP_ZO), EMPTY_SEQUENCE_Z, flag(UPD), XQUERY_URI),
  /** XQuery function. */
  _XQUERY_FORK_JOIN(XQueryForkJoin::new, "fork-join(functions)",
      arg(FUNCTION_ZM), ITEM_ZM, flag(HOF), XQUERY_URI),
  /** XQuery function (legacy, now: xquery:eval). */
  _XQUERY_INVOKE(XQueryInvoke::new, "invoke(uri[,bindings[,options]])",
      arg(STRING_O, MAP_ZO, MAP_ZO), ITEM_ZM, flag(NDT), XQUERY_URI),
  /** XQuery function (legacy, now: jobs:eval-update). */
  _XQUERY_INVOKE_UPDATE(XQueryInvokeUpdate::new, "invoke-update(uri[,bindings[,options]])",
      arg(STRING_O, MAP_ZO, MAP_ZO), EMPTY_SEQUENCE_Z, flag(UPD), XQUERY_URI),
  /** XQuery function. */
  _XQUERY_PARSE(XQueryParse::new, "parse(string[,options])",
      arg(STRING_O, MAP_ZO), NODE_O, flag(NDT), XQUERY_URI),
  /** XQuery function. */
  _XQUERY_PARSE_URI(XQueryParseUri::new, "parse-uri(uri[,options])",
      arg(STRING_O, MAP_ZO), NODE_O, flag(NDT), XQUERY_URI),

  // XSLT Module

  /** XQuery function. */
  _XSLT_INIT(XsltInit::new, "init()",
      arg(), NODE_O, flag(NDT), XSLT_URI),
  /** XQuery function. */
  _XSLT_PROCESSOR(XsltProcessor::new, "processor()",
      arg(), STRING_O, XSLT_URI),
  /** XQuery function. */
  _XSLT_TRANSFORM(XsltTransform::new, "transform(input,stylesheet[,params[,options]])",
      arg(ITEM_O, ITEM_O, MAP_ZO, MAP_ZO), NODE_O, flag(NDT), XSLT_URI),
  /** XQuery function. */
  _XSLT_TRANSFORM_TEXT(XsltTransformText::new,
      "transform-text(input,stylesheet[,params[,options]])",
      arg(ITEM_O, ITEM_O, MAP_ZO, MAP_ZO), STRING_O, flag(NDT), XSLT_URI),
  /** XQuery function. */
  _XSLT_VERSION(XsltVersion::new, "version()",
      arg(), STRING_O, XSLT_URI),

  // ZIP Module

  /** XQuery function. */
  _ZIP_BINARY_ENTRY(ZipBinaryEntry::new, "binary-entry(path,entry)",
      arg(STRING_O, STRING_O), BASE64_BINARY_O, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_ENTRIES(ZipEntries::new, "entries(path)",
      arg(STRING_O), ELEMENT_O, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_HTML_ENTRY(ZipHtmlEntry::new, "html-entry(path,entry)",
      arg(STRING_O, STRING_O), NODE_O, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_TEXT_ENTRY(ZipTextEntry::new, "text-entry(path,entry[,encoding])",
      arg(STRING_O, STRING_O, STRING_O), STRING_O, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_XML_ENTRY(ZipXmlEntry::new, "xml-entry(path,entry)",
      arg(STRING_O, STRING_O), NODE_O, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_UPDATE_ENTRIES(ZipUpdateEntries::new, "update-entries(zip,output)",
      arg(ELEMENT_O, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_ZIP_FILE(ZipZipFile::new, "zip-file(zip)",
      arg(ELEMENT_O), EMPTY_SEQUENCE_Z, flag(NDT), ZIP_URI);

  /** Function definition. */
  private final FuncDefinition definition;

  /**
   * Constructs a function signature; calls
   * {@link #Function(Supplier, String, SeqType[], SeqType, EnumSet)}.
   * @param supplier function implementation constructor
   * @param desc descriptive function string
   * @param args types of the function arguments
   * @param seqType return type
   */
  Function(final Supplier<? extends StandardFunc> supplier, final String desc, final SeqType[] args,
      final SeqType seqType) {
    this(supplier, desc, args, seqType, EnumSet.noneOf(Flag.class));
  }

  /**
   * Constructs a function signature; calls
   * {@link #Function(Supplier, String, SeqType[], SeqType, EnumSet)}.
   * @param supplier function implementation constructor
   * @param desc descriptive function string
   * @param args types of the function arguments
   * @param type return type
   * @param uri uri
   */
  Function(final Supplier<? extends StandardFunc> supplier, final String desc, final SeqType[] args,
      final SeqType type, final byte[] uri) {
    this(supplier, desc, args, type, EnumSet.noneOf(Flag.class), uri);
  }

  /**
   * Constructs a function signature; calls
   * {@link #Function(Supplier, String, SeqType[], SeqType, EnumSet, byte[])}.
   * @param supplier function implementation constructor
   * @param desc descriptive function string
   * @param args types of the function arguments
   * @param seqType return type
   * @param flag static function properties
   */
  Function(final Supplier<? extends StandardFunc> supplier, final String desc, final SeqType[] args,
      final SeqType seqType, final EnumSet<Flag> flag) {
    this(supplier, desc, args, seqType, flag, FN_URI);
  }

  /**
   * Constructs a function signature.
   * @param supplier function implementation constructor
   * @param desc descriptive function string, containing the function name and its arguments in
   *   parentheses. Optional arguments are represented in nested square brackets; three dots
   *   indicate that the number of arguments of a function is not limited.
   * @param params parameter types
   * @param seqType return type
   * @param flags static function properties
   * @param uri uri
   */
  Function(final Supplier<? extends StandardFunc> supplier, final String desc,
      final SeqType[] params, final SeqType seqType, final EnumSet<Flag> flags, final byte[] uri) {
    definition = new FuncDefinition(supplier, desc, params, seqType, flags, uri);
  }

  @Override
  public FuncDefinition definition() {
    return definition;
  }

  /**
   * Returns an array representation of the specified sequence types.
   * @param arg arguments
   * @return array
   */
  private static SeqType[] arg(final SeqType... arg) {
    return arg;
  }

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
   * Adds function signatures to the list. Required for initialization.
   * @param list list of function signatures
   */
  public static void init(final ArrayList<FuncDefinition> list) {
    for(final Function function : values()) list.add(function.definition);
  }

  @Override
  public final String toString() {
    return definition.toString();
  }
}
