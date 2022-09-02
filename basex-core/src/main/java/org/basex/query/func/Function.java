package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Flag.*;
import static org.basex.query.value.type.SeqType.*;

import java.util.*;
import java.util.function.*;

import org.basex.core.users.*;
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
import org.basex.query.func.job.*;
import org.basex.query.func.json.*;
import org.basex.query.func.lazy.*;
import org.basex.query.func.map.*;
import org.basex.query.func.math.*;
import org.basex.query.func.proc.*;
import org.basex.query.func.prof.*;
import org.basex.query.func.random.*;
import org.basex.query.func.repo.*;
import org.basex.query.func.sql.*;
import org.basex.query.func.store.*;
import org.basex.query.func.string.*;
import org.basex.query.func.unit.*;
import org.basex.query.func.update.*;
import org.basex.query.func.user.*;
import org.basex.query.func.util.*;
import org.basex.query.func.validate.*;
import org.basex.query.func.web.*;
import org.basex.query.func.xquery.*;
import org.basex.query.func.xslt.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;

/**
 * Definitions of all built-in XQuery functions.
 * New namespace mappings for function prefixes and URIs must be added to the static initializer of
 * the {@link NSGlobal} class.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public enum Function implements AFunction {

  // Standard functions

  /** XQuery function. */
  ABS(FnAbs::new, "abs(value)",
      params(NUMERIC_ZO), NUMERIC_ZO),
  /** XQuery function. */
  ADJUST_DATE_TO_TIMEZONE(FnAdjustDateToTimezone::new, "adjust-date-to-timezone(value[,timezone])",
      params(DATE_ZO, DAY_TIME_DURATION_ZO), DATE_ZO),
  /** XQuery function. */
  ADJUST_DATETIME_TO_TIMEZONE(FnAdustDateTimeToTimezone::new,
      "adjust-dateTime-to-timezone(value[,timezone])",
      params(DATE_TIME_ZO, DAY_TIME_DURATION_ZO), DATE_TIME_ZO),
  /** XQuery function. */
  ADJUST_TIME_TO_TIMEZONE(FnAdjustTimeToTimezone::new, "adjust-time-to-timezone(value[,timezone])",
      params(TIME_ZO, DAY_TIME_DURATION_ZO), TIME_ZO),
  /** XQuery function. */
  ALL(FnAll::new, "all(input,predicate)",
      params(ITEM_ZM, PREDICATE_O), BOOLEAN_O, flag(HOF)),
  /** XQuery function. */
  ANALYZE_STRING(FnAnalyzeString::new, "analyze-string(value,pattern[,flags])",
      params(STRING_ZO, STRING_O, STRING_O), ELEMENT_O, flag(CNS)),
  /** XQuery function. */
  APPLY(FnApply::new, "apply(function,arguments)",
      params(FUNCTION_O, ARRAY_O), ITEM_ZM, flag(POS, CTX, NDT, HOF), FN_URI, Perm.ADMIN),
  /** XQuery function. */
  AVAILABLE_ENVIRONMENT_VARIABLES(FnAvailableEnvironmentVariables::new,
      "available-environment-variables()",
      params(), STRING_ZM),
  /** XQuery function. */
  AVG(FnAvg::new, "avg(values)",
      params(ANY_ATOMIC_TYPE_ZM), ANY_ATOMIC_TYPE_ZO),
  /** XQuery function. */
  BASE_URI(FnBaseUri::new, "base-uri([node])",
      params(NODE_ZO), ANY_URI_ZO),
  /** XQuery function. */
  BOOLEAN(FnBoolean::new, "boolean(input)",
      params(ITEM_ZM), BOOLEAN_O),
  /** XQuery function. */
  CEILING(FnCeiling::new, "ceiling(value)",
      params(NUMERIC_ZO), NUMERIC_ZO),
  /** XQuery function. */
  CHARACTERS(FnCharacters::new, "characters(value)", params(STRING_ZO), STRING_ZM),
  /** XQuery function. */
  CODEPOINT_EQUAL(FnCodepointEqual::new, "codepoint-equal(value1,value2)",
      params(STRING_ZO, STRING_ZO), BOOLEAN_ZO),
  /** XQuery function. */
  CODEPOINTS_TO_STRING(FnCodepointsToString::new, "codepoints-to-string(values)",
      params(INTEGER_ZM), STRING_O),
  /** XQuery function. */
  COLLECTION(FnCollection::new, "collection([uri])",
      params(STRING_ZO), DOCUMENT_NODE_ZM, flag(NDT)),
  /** XQuery function. */
  COMPARE(FnCompare::new, "compare(value1,value2[,collation])",
      params(STRING_ZO, STRING_ZO, STRING_O), INTEGER_ZO),
  /** XQuery function. */
  CONCAT(FnConcat::new, "concat(value1,value2[,...])",
      params(ANY_ATOMIC_TYPE_ZO, ANY_ATOMIC_TYPE_ZO), STRING_O),
  /** XQuery function. */
  CONTAINS(FnContains::new, "contains(value,substring[,collation])",
      params(STRING_ZO, STRING_ZO, STRING_O), BOOLEAN_O),
  /** XQuery function. */
  CONTAINS_TOKEN(FnContainsToken::new, "contains-token(value,token[,collation])",
      params(STRING_ZM, STRING_O, STRING_O), BOOLEAN_O),
  /** XQuery function. */
  COUNT(FnCount::new, "count(input)",
      params(ITEM_ZM), INTEGER_O),
  /** XQuery function. */
  CURRENT_DATE(FnCurrentDate::new, "current-date()",
      params(), DATE_O, flag(NDT)),
  /** XQuery function. */
  CURRENT_DATETIME(FnCurrentDateTime::new, "current-dateTime()",
      params(), DATE_TIME_O, flag(NDT)),
  /** XQuery function. */
  CURRENT_TIME(FnCurrentTime::new, "current-time()",
      params(), TIME_O, flag(NDT)),
  /** XQuery function. */
  DATA(FnData::new, "data([input])",
      params(ITEM_ZM), ANY_ATOMIC_TYPE_ZM),
  /** XQuery function. */
  DATETIME(FnDateTime::new, "dateTime(date,time)",
      params(DATE_ZO, TIME_ZO), DATE_TIME_ZO),
  /** XQuery function. */
  DAY_FROM_DATE(FnDayFromDate::new, "day-from-date(value)",
      params(DATE_ZO), INTEGER_ZO),
  /** XQuery function. */
  DAY_FROM_DATETIME(FnDayFromDateTime::new, "day-from-dateTime(value)",
      params(DATE_TIME_ZO), INTEGER_ZO),
  /** XQuery function. */
  DAYS_FROM_DURATION(FnDayFromDuration::new, "days-from-duration(value)",
      params(DURATION_ZO), INTEGER_ZO),
  /** XQuery function. */
  DEEP_EQUAL(FnDeepEqual::new, "deep-equal(input1,input2[,collation])",
      params(ITEM_ZM, ITEM_ZM, STRING_O), BOOLEAN_O),
  /** XQuery function. */
  DEFAULT_COLLATION(FnDefaultCollation::new, "default-collation()",
      params(), STRING_O),
  /** XQuery function. */
  DEFAULT_LANGUAGE(FnDefaultLanguage::new, "default-language()",
      params(), LANGUAGE_O),
  /** XQuery function. */
  DISTINCT_VALUES(FnDistinctValues::new, "distinct-values(values[,collation])",
      params(ANY_ATOMIC_TYPE_ZM, STRING_O), ANY_ATOMIC_TYPE_ZM),
  /** XQuery function. */
  DOC(FnDoc::new, "doc(href)",
      params(STRING_ZO), DOCUMENT_NODE_ZO, flag(NDT)),
  /** XQuery function. */
  DOC_AVAILABLE(FnDocAvailable::new, "doc-available(href)",
      params(STRING_ZO), BOOLEAN_O, flag(NDT)),
  /** XQuery function. */
  DOCUMENT_URI(FnDocumentUri::new, "document-uri([node])",
      params(NODE_ZO), ANY_URI_ZO),
  /** XQuery function. */
  ELEMENT_WITH_ID(FnElementWithId::new, "element-with-id(values[,node])",
      params(STRING_ZM, NODE_O), ELEMENT_ZM),
  /** XQuery function. */
  EMPTY(FnEmpty::new, "empty(input)",
      params(ITEM_ZM), BOOLEAN_O),
  /** XQuery function. */
  ENCODE_FOR_URI(FnEncodeForUri::new, "encode-for-uri(value)",
      params(STRING_ZO), STRING_O),
  /** XQuery function. */
  ENDS_WITH(FnEndsWith::new, "ends-with(value,substring[,collation])",
      params(STRING_ZO, STRING_ZO, STRING_O), BOOLEAN_O),
  /** XQuery function. */
  ENVIRONMENT_VARIABLE(FnEnvironmentVariable::new, "environment-variable(name)",
      params(STRING_O), STRING_ZO),
  /** XQuery function. */
  ERROR(FnError::new, "error([code[,description[,error-object]]])",
      params(QNAME_ZO, STRING_O, ITEM_ZM), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  ESCAPE_HTML_URI(FnEscapeHtmlUri::new, "escape-html-uri(value)",
      params(STRING_ZO), STRING_O),
  /** XQuery function. */
  EXACTLY_ONE(FnExactlyOne::new, "exactly-one(input)",
      params(ITEM_ZM), ITEM_O),
  /** XQuery function. */
  EXISTS(FnExists::new, "exists(input)",
      params(ITEM_ZM), BOOLEAN_O),
  /** XQuery function. */
  FALSE(FnFalse::new, "false()",
      params(), BOOLEAN_O),
  /** XQuery function. */
  FILTER(FnFilter::new, "filter(input,predicate)",
      params(ITEM_ZM, PREDICATE_O), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FLOOR(FnFloor::new, "floor(value)",
      params(NUMERIC_ZO), NUMERIC_ZO),
  /** XQuery function. */
  FOLD_LEFT(FnFoldLeft::new, "fold-left(input,zero,action)",
      params(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM_O).seqType()),
      ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FOLD_RIGHT(FnFoldRight::new, "fold-right(input,zero,action)",
      params(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O, ITEM_ZM).seqType()),
      ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FOR_EACH(FnForEach::new, "for-each(input,action)",
      params(ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O).seqType()), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FOR_EACH_PAIR(FnForEachPair::new, "for-each-pair(input1,input2,action)",
      params(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O, ITEM_O).seqType()),
      ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FORMAT_DATE(FnFormatDate::new, "format-date(value,picture[,language,calendar,place])",
      params(DATE_ZO, STRING_O, STRING_ZO, STRING_ZO, STRING_ZO), STRING_ZO),
  /** XQuery function. */
  FORMAT_DATETIME(FnFormatDateTime::new,
      "format-dateTime(value,picture[,language,calendar,place])",
      params(DATE_TIME_ZO, STRING_O, STRING_ZO, STRING_ZO, STRING_ZO), STRING_ZO),
  /** XQuery function. */
  FORMAT_INTEGER(FnFormatInteger::new, "format-integer(value,picture[,language])",
      params(INTEGER_ZO, STRING_O, STRING_O), STRING_O),
  /** XQuery function. */
  FORMAT_NUMBER(FnFormatNumber::new, "format-number(value,picture[,decimal-format-name])",
      params(NUMERIC_ZO, STRING_O, STRING_ZO), STRING_O),
  /** XQuery function. */
  FORMAT_TIME(FnFormatTime::new, "format-time(value,picture[,language,calendar,place])",
      params(TIME_ZO, STRING_O, STRING_ZO, STRING_ZO, STRING_ZO), STRING_ZO),
  /** XQuery function. */
  FUNCTION_ARITY(FnFunctionArity::new, "function-arity(function)",
      params(FUNCTION_O), INTEGER_O),
  /** XQuery function. */
  FUNCTION_LOOKUP(FnFunctionLookup::new, "function-lookup(name,arity)",
      params(QNAME_O, INTEGER_O), FUNCTION_ZO, flag(POS, CTX, CNS, NDT), FN_URI, Perm.ADMIN),
  /** XQuery function. */
  FUNCTION_NAME(FnFunctionName::new, "function-name(function)",
      params(FUNCTION_O), QNAME_ZO),
  /** XQuery function. */
  GENERATE_ID(FnGenerateId::new, "generate-id([node])",
      params(NODE_ZO), STRING_O),
  /** XQuery function. */
  HAS_CHILDREN(FnHasChildren::new, "has-children([node])",
      params(NODE_ZM), BOOLEAN_O),
  /** XQuery function. */
  HEAD(FnHead::new, "head(input)",
      params(ITEM_ZM), ITEM_ZO),
  /** XQuery function. */
  HIGHEST(FnHighest::new, "highest(input[,collation[,key]])",
      params(ITEM_ZM, STRING_ZO, FuncType.get(ANY_ATOMIC_TYPE_ZM, ITEM_O).seqType()), ITEM_ZM),
  /** XQuery function. */
  HOURS_FROM_DATETIME(FnHoursFromDateTime::new, "hours-from-dateTime(value)",
      params(DATE_TIME_ZO), INTEGER_ZO),
  /** XQuery function. */
  HOURS_FROM_DURATION(FnHoursFromDuration::new, "hours-from-duration(value)",
      params(DURATION_ZO), INTEGER_ZO),
  /** XQuery function. */
  HOURS_FROM_TIME(FnHoursFromTime::new, "hours-from-time(value)",
      params(TIME_ZO), INTEGER_ZO),
  /** XQuery function. */
  ID(FnId::new, "id(values[,node])",
      params(STRING_ZM, NODE_O), ELEMENT_ZM),
  /** XQuery function. */
  IDENTITY(FnIdentity::new, "identity(value)", params(ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  IDREF(FnIdref::new, "idref(values[,node])",
      params(STRING_ZM, NODE_O), NODE_ZM),
  /** XQuery function. */
  IMPLICIT_TIMEZONE(FnImplicitTimezone::new, "implicit-timezone()",
      params(), DAY_TIME_DURATION_O, flag(NDT)),
  /** XQuery function. */
  IN_SCOPE_NAMESPACES(FnInScopeNamespaces::new, "in-scope-namespaces(element)",
      params(ELEMENT_O), MAP_O),
  /** XQuery function. */
  IN_SCOPE_PREFIXES(FnInScopePrefixes::new, "in-scope-prefixes(element)",
      params(ELEMENT_O), STRING_ZM),
  /** XQuery function. */
  INDEX_OF(FnIndexOf::new, "index-of(input,search[,collation])",
      params(ANY_ATOMIC_TYPE_ZM, ANY_ATOMIC_TYPE_O, STRING_O), INTEGER_ZM),
  /** XQuery function. */
  INDEX_WHERE(FnIndexWhere::new, "index-where(input,predicate)",
      params(ITEM_ZM, PREDICATE_O), INTEGER_ZM, flag(HOF)),
  /** XQuery function. */
  INNERMOST(FnInnermost::new, "innermost(nodes)",
      params(NODE_ZM), NODE_ZM),
  /** XQuery function. */
  INSERT_BEFORE(FnInsertBefore::new, "insert-before(input,position,insert)",
      params(ITEM_ZM, INTEGER_O, ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  IRI_TO_URI(FnIriToUri::new, "iri-to-uri(value)",
      params(STRING_ZO), STRING_O),
  /** XQuery function. */
  IS_NAN(FnIsNaN::new, "is-NaN(value)", params(ANY_ATOMIC_TYPE_O), BOOLEAN_O),
  /** XQuery function. */
  JSON_DOC(FnJsonDoc::new, "json-doc(href[,options])",
      params(STRING_ZO, MAP_O), ITEM_ZO, flag(), FN_URI, Perm.CREATE),
  /** XQuery function. */
  JSON_TO_XML(FnJsonToXml::new, "json-to-xml(json[,options])",
      params(STRING_ZO, MAP_O), NODE_ZO, flag(CNS)),
  /** XQuery function. */
  LANG(FnLang::new, "lang(language[,node])",
      params(STRING_ZO, NODE_O), BOOLEAN_O),
  /** XQuery function. */
  LAST(FnLast::new, "last()",
      params(), INTEGER_O, flag(POS, CTX)),
  /** XQuery function. */
  LOCAL_NAME(FnLocalName::new, "local-name([node])",
      params(NODE_ZO), STRING_O),
  /** XQuery function. */
  LOCAL_NAME_FROM_QNAME(FnLocalNameFromQName::new, "local-name-from-QName(value)",
      params(QNAME_ZO), NCNAME_ZO),
  /** XQuery function. */
  LOWER_CASE(FnLowerCase::new, "lower-case(value)",
      params(STRING_ZO), STRING_O),
  /** XQuery function. */
  LOWEST(FnLowest::new, "lowest(input[,collation[,key]])",
      params(ITEM_ZM, STRING_ZO, FuncType.get(ANY_ATOMIC_TYPE_ZM, ITEM_O).seqType()), ITEM_ZM),
  /** XQuery function. */
  MATCHES(FnMatches::new, "matches(value,pattern[,flags])",
      params(STRING_ZO, STRING_O, STRING_O), BOOLEAN_O),
  /** XQuery function. */
  MAX(FnMax::new, "max(values[,collation])",
      params(ANY_ATOMIC_TYPE_ZM, STRING_O), ANY_ATOMIC_TYPE_ZO),
  /** XQuery function. */
  MIN(FnMin::new, "min(values[,collation])",
      params(ANY_ATOMIC_TYPE_ZM, STRING_O), ANY_ATOMIC_TYPE_ZO),
  /** XQuery function. */
  MINUTES_FROM_DATETIME(FnMinutesFromDateTime::new, "minutes-from-dateTime(value)",
      params(DATE_TIME_ZO), INTEGER_ZO),
  /** XQuery function. */
  MINUTES_FROM_DURATION(FnMinutesFromDuration::new, "minutes-from-duration(value)",
      params(DURATION_ZO), INTEGER_ZO),
  /** XQuery function. */
  MINUTES_FROM_TIME(FnMinutesFromTime::new, "minutes-from-time(value)",
      params(TIME_ZO), INTEGER_ZO),
  /** XQuery function. */
  MONTH_FROM_DATE(FnMonthFromDate::new, "month-from-date(value)",
      params(DATE_ZO), INTEGER_ZO),
  /** XQuery function. */
  MONTH_FROM_DATETIME(FnMonthFromDateTime::new, "month-from-dateTime(value)",
      params(DATE_TIME_ZO), INTEGER_ZO),
  /** XQuery function. */
  MONTHS_FROM_DURATION(FnMonthsFromDuration::new, "months-from-duration(value)",
      params(DURATION_ZO), INTEGER_ZO),
  /** XQuery function. */
  NAME(FnName::new, "name([node])",
      params(NODE_ZO), STRING_O),
  /** XQuery function. */
  NAMESPACE_URI(FnNamespaceUri::new, "namespace-uri([node])",
      params(NODE_ZO), ANY_URI_O),
  /** XQuery function. */
  NAMESPACE_URI_FOR_PREFIX(FnNamespaceUriForPrefix::new, "namespace-uri-for-prefix(prefix,element)",
      params(STRING_ZO, ELEMENT_O), ANY_URI_ZO),
  /** XQuery function. */
  NAMESPACE_URI_FROM_QNAME(FnNamespaceUriFromQName::new, "namespace-uri-from-QName(value)",
      params(QNAME_ZO), ANY_URI_ZO),
  /** XQuery function. */
  NILLED(FnNilled::new, "nilled([node])",
      params(NODE_ZO), BOOLEAN_ZO),
  /** XQuery function. */
  NODE_NAME(FnNodeName::new, "node-name([node])",
      params(NODE_ZO), QNAME_ZO),
  /** XQuery function. */
  NORMALIZE_SPACE(FnNormalizeSpace::new, "normalize-space([value])",
      params(STRING_ZO), STRING_O),
  /** XQuery function. */
  NORMALIZE_UNICODE(FnNormalizeUnicode::new, "normalize-unicode(value[,form])",
      params(STRING_ZO, STRING_O), STRING_O),
  /** XQuery function. */
  NOT(FnNot::new, "not(input)",
      params(ITEM_ZM), BOOLEAN_O),
  /** XQuery function. */
  NUMBER(FnNumber::new, "number([value])",
      params(ANY_ATOMIC_TYPE_ZO), DOUBLE_O),
  /** XQuery function. */
  ONE_OR_MORE(FnOneOrMore::new, "one-or-more(input)",
      params(ITEM_ZM), ITEM_OM),
  /** XQuery function. */
  OUTERMOST(FnOutermost::new, "outermost(nodes)",
      params(NODE_ZM), NODE_ZM),
  /** XQuery function. */
  PARSE_IETF_DATE(FnParseIetfDate::new, "parse-ietf-date(value)",
      params(STRING_ZO), DATE_TIME_ZO),
  /** XQuery function. */
  PARSE_JSON(FnParseJson::new, "parse-json(json[,options])",
      params(STRING_ZO, MAP_O), ITEM_ZO),
  /** XQuery function. */
  PARSE_XML(FnParseXml::new, "parse-xml(value)",
      params(STRING_ZO), DOCUMENT_NODE_ZO, flag(CNS)),
  /** XQuery function. */
  PARSE_XML_FRAGMENT(FnParseXmlFragment::new, "parse-xml-fragment(value)",
      params(STRING_ZO), DOCUMENT_NODE_ZO, flag(CNS)),
  /** XQuery function. */
  PATH(FnPath::new, "path([node])",
      params(NODE_ZO), STRING_ZO),
  /** XQuery function. */
  POSITION(FnPosition::new, "position()",
      params(), INTEGER_O, flag(POS, CTX)),
  /** XQuery function. */
  PREFIX_FROM_QNAME(FnPrefixFromQName::new, "prefix-from-QName(value)",
      params(QNAME_ZO), NCNAME_ZO),
  /** XQuery function. */
  PUT(FnPut::new, "put(node,href[,options])",
      params(NODE_O, STRING_ZO, ITEM_ZO), EMPTY_SEQUENCE_Z, flag(UPD), FN_URI, Perm.CREATE),
  /** XQuery function. */
  QNAME(FnQName::new, "QName(uri,qname)",
      params(STRING_ZO, STRING_O), QNAME_O),
  /** XQuery function. */
  RANDOM_NUMBER_GENERATOR(FnRandomNumberGenerator::new, "random-number-generator([seed])",
      params(ANY_ATOMIC_TYPE_O), MAP_O, flag(HOF, NDT)),
  /** XQuery function. */
  RANGE_FROM(FnRangeFrom::new, "range-from(input,start)",
      params(ITEM_ZM, PREDICATE_O), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  RANGE_TO(FnRangeTo::new, "range-to(input,end)",
      params(ITEM_ZM, PREDICATE_O), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  REMOVE(FnRemove::new, "remove(input,position)",
      params(ITEM_ZM, INTEGER_O), ITEM_ZM),
  /** XQuery function. */
  REPLACE(FnReplace::new, "replace(value,pattern,replacement[,flags[,action])",
      params(STRING_ZO, STRING_O, STRING_ZO, STRING_ZO,
      FuncType.get(STRING_ZO, STRING_O, STRING_ZM).seqType()), STRING_O),
  /** XQuery function. */
  REPLICATE(FnReplicate::new, "replicate(input,count[,multiple])",
      params(ITEM_ZM, INTEGER_O, BOOLEAN_O), ITEM_ZM),
  /** XQuery function. */
  RESOLVE_QNAME(FnResolveQName::new, "resolve-QName(qname,element)",
      params(STRING_ZO, ELEMENT_O), QNAME_ZO),
  /** XQuery function. */
  RESOLVE_URI(FnResolveUri::new, "resolve-uri(relative[,base])",
      params(STRING_ZO, STRING_O), ANY_URI_ZO),
  /** XQuery function. */
  REVERSE(FnReverse::new, "reverse(input)",
      params(ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  ROOT(FnRoot::new, "root([node])",
      params(NODE_ZO), NODE_ZO),
  /** XQuery function. */
  ROUND(FnRound::new, "round(value[,precision])",
      params(NUMERIC_ZO, INTEGER_O), NUMERIC_ZO),
  /** XQuery function. */
  ROUND_HALF_TO_EVEN(FnRoundHalfToEven::new, "round-half-to-even(value[,precision])",
      params(NUMERIC_ZO, INTEGER_O), NUMERIC_ZO),
  /** XQuery function. */
  SECONDS_FROM_DATETIME(FnSecondsFromDateTime::new, "seconds-from-dateTime(value)",
      params(DATE_TIME_ZO), DECIMAL_ZO),
  /** XQuery function. */
  SECONDS_FROM_DURATION(FnSecondsFromDuration::new, "seconds-from-duration(value)",
      params(DURATION_ZO), DECIMAL_ZO),
  /** XQuery function. */
  SECONDS_FROM_TIME(FnSecondsFromTime::new, "seconds-from-time(value)",
      params(TIME_ZO), DECIMAL_ZO),
  /** XQuery function. */
  SERIALIZE(FnSerialize::new, "serialize(input[,options])",
      params(ITEM_ZM, ITEM_ZO), STRING_O),
  /** XQuery function. */
  SLICE(FnSlice::new, "slice(input[,start[,end[,step]]])",
      params(ITEM_ZM, INTEGER_ZO, INTEGER_ZO, INTEGER_ZO), ITEM_ZM),
  /** XQuery function. */
  SOME(FnSome::new, "some(input,predicate)",
      params(ITEM_ZM, PREDICATE_O), BOOLEAN_O, flag(HOF)),
  /** XQuery function. */
  SORT(FnSort::new, "sort(input[,collation[,key]])",
      params(ITEM_ZM, STRING_ZO, FuncType.get(ANY_ATOMIC_TYPE_ZM, ITEM_O).seqType()), ITEM_ZM),
  /** XQuery function. */
  STARTS_WITH(FnStartsWith::new, "starts-with(value,substring[,collation])",
      params(STRING_ZO, STRING_ZO, STRING_O), BOOLEAN_O),
  /** XQuery function. */
  STATIC_BASE_URI(FnStaticBaseUri::new, "static-base-uri()",
      params(), ANY_URI_ZO),
  /** XQuery function. */
  STRING(FnString::new, "string([item])",
      params(ITEM_ZO), STRING_O),
  /** XQuery function. */
  STRING_JOIN(FnStringJoin::new, "string-join(values[,separator])",
      params(ANY_ATOMIC_TYPE_ZM, STRING_O), STRING_O),
  /** XQuery function. */
  STRING_LENGTH(FnStringLength::new, "string-length([value])",
      params(STRING_ZO), INTEGER_O),
  /** XQuery function. */
  STRING_TO_CODEPOINTS(FnStringToCodepoints::new, "string-to-codepoints(value)",
      params(STRING_ZO), INTEGER_ZM),
  /** XQuery function. */
  SUBSEQUENCE(FnSubsequence::new, "subsequence(input,start[,length])",
      params(ITEM_ZM, DOUBLE_O, DOUBLE_O), ITEM_ZM),
  /** XQuery function. */
  SUBSTRING(FnSubstring::new, "substring(value,start[,length])",
      params(STRING_ZO, DOUBLE_O, DOUBLE_O), STRING_O),
  /** XQuery function. */
  SUBSTRING_AFTER(FnSubstringAfter::new, "substring-after(value,substring[,collation])",
      params(STRING_ZO, STRING_ZO, STRING_O), STRING_O),
  /** XQuery function. */
  SUBSTRING_BEFORE(FnSubstringBefore::new, "substring-before(value,substring[,collation])",
      params(STRING_ZO, STRING_ZO, STRING_O), STRING_O),
  /** XQuery function. */
  SUM(FnSum::new, "sum(values[,zero])",
      params(ANY_ATOMIC_TYPE_ZM, ANY_ATOMIC_TYPE_ZO), ANY_ATOMIC_TYPE_ZO),
  /** XQuery function. */
  TAIL(FnTail::new, "tail(input)",
      params(ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  TIMEZONE_FROM_DATE(FnTimezoneFromDate::new, "timezone-from-date(value)",
      params(DATE_ZO), DAY_TIME_DURATION_ZO),
  /** XQuery function. */
  TIMEZONE_FROM_DATETIME(FnTimezoneFromDateTime::new, "timezone-from-dateTime(value)",
      params(DATE_TIME_ZO), DAY_TIME_DURATION_ZO),
  /** XQuery function. */
  TIMEZONE_FROM_TIME(FnTimezoneFromTime::new, "timezone-from-time(value)",
      params(TIME_ZO), DAY_TIME_DURATION_ZO),
  /** XQuery function. */
  TOKENIZE(FnTokenize::new, "tokenize(value[,pattern[,flags]])",
      params(STRING_ZO, STRING_O, STRING_O), STRING_ZM),
  /** XQuery function. */
  TRACE(FnTrace::new, "trace(value[,label])",
      params(ITEM_ZM, STRING_O), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  TRANSLATE(FnTranslate::new, "translate(value,replace,with)",
      params(STRING_ZO, STRING_O, STRING_O), STRING_O),
  /** XQuery function. */
  TRUE(FnTrue::new, "true()",
      params(), BOOLEAN_O),
  /** XQuery function. */
  UNIFORM(FnUniform::new, "uniform(values[,collation])",
      params(ANY_ATOMIC_TYPE_ZM, STRING_O), BOOLEAN_O),
  /** XQuery function. */
  UNIQUE(FnUnique::new, "unique(values[,collation])",
      params(ANY_ATOMIC_TYPE_ZM, STRING_O), BOOLEAN_O),
  /** XQuery function. */
  UNORDERED(FnUnordered::new, "unordered(input)",
      params(ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  UNPARSED_TEXT(FnUnparsedText::new, "unparsed-text(href[,encoding])",
      params(STRING_ZO, STRING_O), STRING_ZO, flag(NDT), FN_URI, Perm.CREATE),
  /** XQuery function. */
  UNPARSED_TEXT_AVAILABLE(FnUnparsedTextAvailable::new, "unparsed-text-available(href[,encoding])",
      params(STRING_ZO, STRING_O), BOOLEAN_O, flag(NDT), FN_URI, Perm.CREATE),
  /** XQuery function. */
  UNPARSED_TEXT_LINES(FnUnparsedTextLines::new, "unparsed-text-lines(href[,encoding])",
      params(STRING_ZO, STRING_O), STRING_ZM, flag(NDT), FN_URI, Perm.CREATE),
  /** XQuery function. */
  UPPER_CASE(FnUpperCase::new, "upper-case(value)",
      params(STRING_ZO), STRING_O),
  /** XQuery function. */
  URI_COLLECTION(FnUriCollection::new, "uri-collection([uri])",
      params(STRING_ZO), ANY_URI_ZM, flag(NDT)),
  /** XQuery function. */
  XML_TO_JSON(FnXmlToJson::new, "xml-to-json(node[,options])",
      params(NODE_ZO, MAP_O), STRING_ZO),
  /** XQuery function. */
  YEAR_FROM_DATE(FnYearFromDate::new, "year-from-date(value)",
      params(DATE_ZO), INTEGER_ZO),
  /** XQuery function. */
  YEAR_FROM_DATETIME(FnYearFromDateTime::new, "year-from-dateTime(value)",
      params(DATE_TIME_ZO), INTEGER_ZO),
  /** XQuery function. */
  YEARS_FROM_DURATION(FnYearsFromDuration::new, "years-from-duration(value)",
      params(DURATION_ZO), INTEGER_ZO),
  /** XQuery function. */
  ZERO_OR_ONE(FnZeroOrOne::new, "zero-or-one(input)",
      params(ITEM_ZM), ITEM_ZO),

  // Map Module

  /** XQuery function. */
  _MAP_CONTAINS(MapContains::new, "contains(map,key)",
      params(MAP_O, ANY_ATOMIC_TYPE_O), BOOLEAN_O, MAP_URI),
  /** XQuery function. */
  _MAP_ENTRY(MapEntry::new, "entry(key,value)",
      params(ANY_ATOMIC_TYPE_O, ITEM_ZM), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_FILTER(MapFilter::new, "filter(map,predicate)",
      params(MAP_O, FuncType.get(BOOLEAN_O, ANY_ATOMIC_TYPE_O, ITEM_ZM).seqType()),
      MAP_O, flag(HOF), MAP_URI),
  /** XQuery function. */
  _MAP_FIND(MapFind::new, "find(input,key)",
      params(ITEM_ZM, ANY_ATOMIC_TYPE_O), ARRAY_O, MAP_URI),
  /** XQuery function. */
  _MAP_FOR_EACH(MapForEach::new, "for-each(map,action)",
      params(MAP_O, FuncType.get(ITEM_ZM, ANY_ATOMIC_TYPE_O, ITEM_ZM).seqType()),
      ITEM_ZM, flag(HOF), MAP_URI),
  /** XQuery function. */
  _MAP_GET(MapGet::new, "get(map,key)",
      params(MAP_O, ANY_ATOMIC_TYPE_O), ITEM_ZM, MAP_URI),
  /** XQuery function. */
  _MAP_GROUP_BY(MapGroupBy::new, "group-by(input,key)",
      params(ITEM_ZO, FuncType.get(ANY_ATOMIC_TYPE_ZO, ITEM_O).seqType()),
      MAP_O, flag(HOF), MAP_URI),
  /** XQuery function. */
  _MAP_KEYS(MapKeys::new, "keys(map)",
      params(MAP_O), ANY_ATOMIC_TYPE_ZM, MAP_URI),
  /** XQuery function. */
  _MAP_MERGE(MapMerge::new, "merge(maps[,options])",
      params(MAP_ZM, MAP_O), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_PUT(MapPut::new, "put(map,key,value)",
      params(MAP_O, ANY_ATOMIC_TYPE_O, ITEM_ZM), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_REMOVE(MapRemove::new, "remove(map,keys)",
      params(MAP_O, ANY_ATOMIC_TYPE_ZM), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_SIZE(MapSize::new, "size(map)",
      params(MAP_O), INTEGER_O, MAP_URI),

  // Array Module

  /** XQuery function. */
  _ARRAY_APPEND(ArrayAppend::new, "append(array,add)",
      params(ARRAY_O, ITEM_ZM), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FILTER(ArrayFilter::new, "filter(array,predicate)",
      params(ARRAY_O, FuncType.get(BOOLEAN_O, ITEM_ZM).seqType()), ARRAY_O, flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FLATTEN(ArrayFlatten::new, "flatten(input)",
      params(ITEM_ZM), ITEM_ZM, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FOLD_LEFT(ArrayFoldLeft::new, "fold-left(array,zero,action)",
      params(ARRAY_O, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM_O).seqType()),
      ITEM_ZM, flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FOLD_RIGHT(ArrayFoldRight::new, "fold-right(array,zero,action)",
      params(ARRAY_O, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O, ITEM_ZM).seqType()),
      ITEM_ZM, flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FOR_EACH(ArrayForEach::new, "for-each(array,action)",
      params(ARRAY_O, FuncType.get(ITEM_ZM, ITEM_O).seqType()), ARRAY_O, flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FOR_EACH_PAIR(ArrayForEachPair::new, "for-each-pair(array1,array2,action)",
      params(ARRAY_O, ARRAY_O, FuncType.get(ITEM_ZM, ITEM_O, ITEM_O).seqType()),
      ARRAY_O, flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_GET(ArrayGet::new, "get(array,position)",
      params(ARRAY_O, INTEGER_O), ITEM_ZM, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_HEAD(ArrayHead::new, "head(array)",
      params(ARRAY_O), ITEM_ZM, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_INSERT_BEFORE(ArrayInsertBefore::new, "insert-before(array,position,member)",
      params(ARRAY_O, INTEGER_O, ITEM_ZO), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_JOIN(ArrayJoin::new, "join(arrays)",
      params(ARRAY_ZM), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_PARITION(ArrayPartition::new, "partition(input,break-when)",
      params(ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O).seqType()), ARRAY_ZM, flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_PUT(ArrayPut::new, "put(array,position,member)",
      params(ARRAY_O, INTEGER_O, ITEM_ZM), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_REMOVE(ArrayRemove::new, "remove(array,positions)",
      params(ARRAY_O, INTEGER_ZM), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_REVERSE(ArrayReverse::new, "reverse(array)",
      params(ARRAY_O), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_SIZE(ArraySize::new, "size(array)",
      params(ARRAY_O), INTEGER_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_SLICE(ArraySlice::new, "slice(array[,start[,end[,step]]])",
      params(ARRAY_O, INTEGER_ZO, INTEGER_ZO, INTEGER_ZO), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_SORT(ArraySort::new, "sort(array[,collation[,key]])",
      params(ARRAY_O, STRING_ZO, FuncType.get(ANY_ATOMIC_TYPE_ZM, ITEM_O).seqType()),
      ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_SUBARRAY(ArraySubarray::new, "subarray(array,start[,length])",
      params(ARRAY_O, INTEGER_O, INTEGER_O), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_TAIL(ArrayTail::new, "tail(array)",
      params(ARRAY_O), ARRAY_O, ARRAY_URI),

  // Math Module

  /** XQuery function. */
  _MATH_ACOS(MathAcos::new, "acos(value)",
      params(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_ASIN(MathAsin::new, "asin(value)",
      params(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_ATAN(MathAtan::new, "atan(value)",
      params(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_ATAN2(MathAtan2::new, "atan2(y,x)",
      params(DOUBLE_O, DOUBLE_O), DOUBLE_O, MATH_URI),
  /** XQuery function. */
  _MATH_COS(MathCos::new, "cos(radians)",
      params(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_EXP(MathExp::new, "exp(value)",
      params(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_EXP10(MathExp10::new, "exp10(value)",
      params(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_LOG(MathLog::new, "log(value)",
      params(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_LOG10(MathLog10::new, "log10(value)",
      params(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_PI(MathPi::new, "pi()",
      params(), DOUBLE_O, MATH_URI),
  /** XQuery function. */
  _MATH_POW(MathPow::new, "pow(x,y)",
      params(DOUBLE_ZO, NUMERIC_O), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_SIN(MathSin::new, "sin(radians)",
      params(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_SQRT(MathSqrt::new, "sqrt(value)",
      params(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_TAN(MathTan::new, "tan(radians)",
      params(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),

  // Math Module (custom)

  /** XQuery function. */
  _MATH_COSH(MathCosh::new, "cosh(radians)",
      params(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_CRC32(MathCrc32::new, "crc32(value)",
      params(STRING_ZO), HEX_BINARY_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_E(MathE::new, "e()",
      params(), DOUBLE_O, MATH_URI),
  /** XQuery function. */
  _MATH_SINH(MathSinh::new, "sinh(radians)",
      params(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_TANH(MathTanh::new, "tanh(radians)",
      params(DOUBLE_ZO), DOUBLE_ZO, MATH_URI),

  // Admin Module

  /** XQuery function. */
  _ADMIN_DELETE_LOGS(AdminDeleteLogs::new, "delete-logs(date)",
      params(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), ADMIN_URI, Perm.ADMIN),
  /** XQuery function. */
  _ADMIN_LOGS(AdminLogs::new, "logs([date[,merge]])",
      params(STRING_O, BOOLEAN_O), ELEMENT_ZM, flag(NDT), ADMIN_URI, Perm.ADMIN),
  /** XQuery function. */
  _ADMIN_SESSIONS(AdminSessions::new, "sessions()",
      params(), ELEMENT_ZM, flag(NDT), ADMIN_URI, Perm.ADMIN),
  /** XQuery function. */
  _ADMIN_WRITE_LOG(AdminWriteLog::new, "write-log(message[,type])",
      params(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), ADMIN_URI, Perm.ADMIN),

  // Archive Module

  /** XQuery function. */
  _ARCHIVE_CREATE(ArchiveCreate::new, "create(entries,contents[,options])",
      params(ITEM_ZM, ITEM_ZM, MAP_ZO), BASE64_BINARY_O, flag(NDT), ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_CREATE_FROM(ArchiveCreateFrom::new, "create-from(path[,options[,entries]])",
      params(STRING_O, MAP_ZO, ITEM_ZM), BASE64_BINARY_O, flag(NDT), ARCHIVE_URI, Perm.CREATE),
  /** XQuery function. */
  _ARCHIVE_DELETE(ArchiveDelete::new, "delete(archive,entries)",
      params(BASE64_BINARY_O, ITEM_ZM), BASE64_BINARY_O, flag(NDT), ARCHIVE_URI, Perm.CREATE),
  /** XQuery function. */
  _ARCHIVE_ENTRIES(ArchiveEntries::new, "entries(archive)",
      params(BASE64_BINARY_O), ELEMENT_ZM, flag(NDT), ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_EXTRACT_BINARY(ArchiveExtractBinary::new, "extract-binary(archive[,entries])",
      params(BASE64_BINARY_O, ITEM_ZM), BASE64_BINARY_ZM, flag(NDT), ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_EXTRACT_TEXT(ArchiveExtractText::new, "extract-text(archive[,entries[,encoding]])",
      params(BASE64_BINARY_O, ITEM_ZM, STRING_O), STRING_ZM, flag(NDT), ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_EXTRACT_TO(ArchiveExtractTo::new, "extract-to(path,archive[,entries])",
      params(STRING_O, BASE64_BINARY_O, ITEM_ZM),
      EMPTY_SEQUENCE_Z, flag(NDT), ARCHIVE_URI, Perm.CREATE),
  /** XQuery function. */
  _ARCHIVE_OPTIONS(ArchiveOptions::new, "options(archive)",
      params(BASE64_BINARY_O), MAP_O, flag(NDT), ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_UPDATE(ArchiveUpdate::new, "update(archive,entries,contents)",
      params(BASE64_BINARY_O, ITEM_ZM, ITEM_ZM),
      BASE64_BINARY_O, flag(NDT), ARCHIVE_URI, Perm.CREATE),
  /** XQuery function. */
  _ARCHIVE_WRITE(ArchiveWrite::new, "write(path,entries,contents[,options])",
      params(STRING_O, ITEM_ZM, ITEM_ZM, MAP_ZO), EMPTY_SEQUENCE_Z, flag(NDT), ARCHIVE_URI),

  // Binary Module

  /** XQuery function. */
  _BIN_AND(BinAnd::new, "and(binary1,binary2)",
      params(BASE64_BINARY_ZO, BASE64_BINARY_ZO), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_BIN(BinBin::new, "bin(string)",
      params(STRING_ZO), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_DECODE_STRING(BinDecodeString::new, "decode-string(binary[,encoding[,offset[,size]]])",
      params(BASE64_BINARY_ZO, STRING_O, INTEGER_O, INTEGER_O), STRING_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_ENCODE_STRING(BinEncodeString::new, "encode-string(string[,encoding])",
      params(STRING_ZO, STRING_O), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_FIND(BinFind::new, "find(binary,offset,search)",
      params(BASE64_BINARY_ZO, INTEGER_O, BASE64_BINARY_ZO), INTEGER_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_FROM_OCTETS(BinFromOctets::new, "from-octets(integers)",
      params(INTEGER_ZM), BASE64_BINARY_O, BIN_URI),
  /** XQuery function. */
  _BIN_HEX(BinHex::new, "hex(string)",
      params(STRING_ZO), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_INSERT_BEFORE(BinInsertBefore::new, "insert-before(binary,offset,extra)",
      params(BASE64_BINARY_ZO, INTEGER_O, BASE64_BINARY_ZO), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_JOIN(BinJoin::new, "join(binaries)",
      params(BASE64_BINARY_ZM), BASE64_BINARY_O, BIN_URI),
  /** XQuery function. */
  _BIN_LENGTH(BinLength::new, "length(binary)",
      params(BASE64_BINARY_O), INTEGER_O, BIN_URI),
  /** XQuery function. */
  _BIN_NOT(BinNot::new, "not(binary)",
      params(BASE64_BINARY_ZO), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_OCTAL(BinOctal::new, "octal(string)",
      params(STRING_ZO), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_OR(BinOr::new, "or(binary1,binary2)",
      params(BASE64_BINARY_ZO, BASE64_BINARY_ZO), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_PACK_DOUBLE(BinPackDouble::new, "pack-double(double[,order])",
      params(DOUBLE_O, STRING_O), BASE64_BINARY_O, BIN_URI),
  /** XQuery function. */
  _BIN_PACK_FLOAT(BinPackFloat::new, "pack-float(float[,order])",
      params(FLOAT_O, STRING_O), BASE64_BINARY_O, BIN_URI),
  /** XQuery function. */
  _BIN_PACK_INTEGER(BinPackInteger::new, "pack-integer(integer,size[,order])",
      params(INTEGER_O, INTEGER_O, STRING_O), BASE64_BINARY_O, BIN_URI),
  /** XQuery function. */
  _BIN_PAD_LEFT(BinPadLeft::new, "pad-left(binary,size[,octet])",
      params(BASE64_BINARY_ZO, INTEGER_O, INTEGER_O), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_PAD_RIGHT(BinPadRight::new, "pad-right(binary,size[,octet])",
      params(BASE64_BINARY_ZO, INTEGER_O, INTEGER_O), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_PART(BinPart::new, "part(binary,offset[,size])",
      params(BASE64_BINARY_ZO, INTEGER_O, INTEGER_O), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_SHIFT(BinShift::new, "shift(binary,by)",
      params(BASE64_BINARY_ZO, INTEGER_O), BASE64_BINARY_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_TO_OCTETS(BinToOctets::new, "to-octets(binary)",
      params(BASE64_BINARY_ZO), INTEGER_ZM, BIN_URI),
  /** XQuery function. */
  _BIN_UNPACK_DOUBLE(BinUnpackDouble::new, "unpack-double(binary,offset[,order])",
      params(BASE64_BINARY_O, INTEGER_O, STRING_O), DOUBLE_O, BIN_URI),
  /** XQuery function. */
  _BIN_UNPACK_FLOAT(BinUnpackFloat::new, "unpack-float(binary,offset[,order])",
      params(BASE64_BINARY_O, INTEGER_O, STRING_O), FLOAT_O, BIN_URI),
  /** XQuery function. */
  _BIN_UNPACK_INTEGER(BinUnpackInteger::new, "unpack-integer(binary,offset,size[,order])",
      params(BASE64_BINARY_O, INTEGER_O, INTEGER_O, STRING_O), INTEGER_O, BIN_URI),
  /** XQuery function. */
  _BIN_UNPACK_UNSIGNED_INTEGER(BinUnpackUnsignedInteger::new,
      "unpack-unsigned-integer(binary,offset,size[,order])",
      params(BASE64_BINARY_O, INTEGER_O, INTEGER_O, STRING_O), INTEGER_O, BIN_URI),
  /** XQuery function. */
  _BIN_XOR(BinXor::new, "xor(binary1,binary2)",
      params(BASE64_BINARY_ZO, BASE64_BINARY_ZO), BASE64_BINARY_ZO, BIN_URI),

  // Client Module

  /** XQuery function. */
  _CLIENT_CLOSE(ClientClose::new, "close(id)",
      params(ANY_URI_O), EMPTY_SEQUENCE_Z, flag(NDT), CLIENT_URI, Perm.CREATE),
  /** XQuery function. */
  _CLIENT_CONNECT(ClientConnect::new, "connect(host,port,username,password)",
      params(STRING_O, INTEGER_O, STRING_O, STRING_O),
      ANY_URI_O, flag(NDT), CLIENT_URI, Perm.CREATE),
  /** XQuery function. */
  _CLIENT_EXECUTE(ClientExecute::new, "execute(id,command)",
      params(ANY_URI_O, STRING_O), STRING_O, flag(NDT), CLIENT_URI, Perm.CREATE),
  /** XQuery function. */
  _CLIENT_INFO(ClientInfo::new, "info(id)",
      params(ANY_URI_O), STRING_O, flag(NDT), CLIENT_URI, Perm.CREATE),
  /** XQuery function. */
  _CLIENT_QUERY(ClientQuery::new, "query(id,query[,bindings])",
      params(ANY_URI_O, STRING_O, MAP_ZO), ITEM_ZM, flag(NDT), CLIENT_URI, Perm.CREATE),

  // Conversion Module

  /** XQuery function. */
  _CONVERT_BINARY_TO_BYTES(ConvertBinaryToBytes::new, "binary-to-bytes(value)",
      params(BINARY_O), BYTE_ZM, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_BINARY_TO_INTEGERS(ConvertBinaryToIntegers::new, "binary-to-integers(value)",
      params(BINARY_O), INTEGER_ZM, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_BINARY_TO_STRING(ConvertBinaryToString::new,
      "binary-to-string(value[,encoding[,fallback]])",
      params(BINARY_O, STRING_O, BOOLEAN_O), STRING_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_DATETIME_TO_INTEGER(ConvertDateTimeToInteger::new, "dateTime-to-integer(value)",
      params(DATE_TIME_O), INTEGER_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_DAYTIME_TO_INTEGER(ConvertDayTimeToInteger::new, "dayTime-to-integer(value)",
      params(DAY_TIME_DURATION_O), INTEGER_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_DECODE_KEY(ConvertDecodeKey::new, "decode-key(key[,lax])",
      params(STRING_O, BOOLEAN_O), STRING_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_ENCODE_KEY(ConvertEncodeKey::new, "encode-key(key[,lax])",
      params(STRING_O, BOOLEAN_O), STRING_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGER_FROM_BASE(ConvertIntegerFromBase::new, "integer-from-base(value,base)",
      params(STRING_O, INTEGER_O), INTEGER_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGER_TO_BASE(ConvertIntegerToBase::new, "integer-to-base(value,base)",
      params(INTEGER_O, INTEGER_O), STRING_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGER_TO_DATETIME(ConvertIntegerToDateTime::new, "integer-to-dateTime(value)",
      params(INTEGER_O), DATE_TIME_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGER_TO_DAYTIME(ConvertIntegerToDayTime::new, "integer-to-dayTime(value)",
      params(INTEGER_O), DAY_TIME_DURATION_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGERS_TO_BASE64(ConvertIntegersToBase64::new, "integers-to-base64(values)",
      params(INTEGER_ZM), BASE64_BINARY_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGERS_TO_HEX(ConvertIntegersToHex::new, "integers-to-hex(values)",
      params(INTEGER_ZM), HEX_BINARY_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_STRING_TO_BASE64(ConvertStringToBase64::new, "string-to-base64(value[,encoding])",
      params(STRING_O, STRING_O), BASE64_BINARY_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_STRING_TO_HEX(ConvertStringToHex::new, "string-to-hex(value[,encoding])",
      params(STRING_O, STRING_O), HEX_BINARY_O, CONVERT_URI),

  // Cryptographic Module

  /** XQuery function. */
  _CRYPTO_DECRYPT(CryptoDecrypt::new, "decrypt(value,type,key,algorithm)",
      params(STRING_O, STRING_O, STRING_O, STRING_O), STRING_O, CRYPTO_URI),
  /** XQuery function. */
  _CRYPTO_ENCRYPT(CryptoEncrypt::new, "encrypt(value,type,key,algorithm)",
      params(STRING_O, STRING_O, STRING_O, STRING_O), STRING_O, CRYPTO_URI),
  /** XQuery function. */
  _CRYPTO_GENERATE_SIGNATURE(CryptoGenerateSignature::new, "generate-signature" +
      "(node,canonicalization,digest,signature,prefix,type[,item1][,item2])",
      params(NODE_O, STRING_O, STRING_O, STRING_O, STRING_O, STRING_O, ITEM_ZO, ITEM_ZO),
      NODE_O, CRYPTO_URI),
  /** XQuery function. */
  _CRYPTO_HMAC(CryptoHmac::new, "hmac(value,key,algorithm[,encoding])",
      params(STRING_O, STRING_O, STRING_O, STRING_ZO), STRING_O, CRYPTO_URI),
  /** XQuery function. */
  _CRYPTO_VALIDATE_SIGNATURE(CryptoValidateSignature::new, "validate-signature(node)",
      params(NODE_O), BOOLEAN_O, CRYPTO_URI),

  // CSV Module

  /** XQuery function. */
  _CSV_DOC(CsvDoc::new, "doc(href[,options])",
      params(STRING_O, MAP_ZO), ITEM_ZO, flag(NDT), CSV_URI),
  /** XQuery function. */
  _CSV_PARSE(CsvParse::new, "parse(value[,options])",
      params(STRING_ZO, MAP_ZO), ITEM_ZO, CSV_URI),
  /** XQuery function. */
  _CSV_SERIALIZE(CsvSerialize::new, "serialize(input[,options])",
      params(ITEM_ZO, ITEM_ZO), STRING_O, CSV_URI),

  // Database Module

  /** XQuery function. */
  _DB_ADD(DbAdd::new, "add(database,input[,path[,options]])",
      params(STRING_O, ITEM_O, STRING_O, MAP_ZO), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_ALTER(DbAlter::new, "alter(database, new-name)",
      params(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_ALTER_BACKUP(DbAlterBackup::new, "alter-backup(name, new-name)",
      params(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_ATTRIBUTE(DbAttribute::new, "attribute(database,strings[,name])",
      params(STRING_O, ITEM_ZM, STRING_O), ATTRIBUTE_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_ATTRIBUTE_RANGE(DbAttributeRange::new, "attribute-range(database,from,to[,name])",
      params(STRING_O, ITEM_O, ITEM_O, STRING_O), ATTRIBUTE_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_BACKUPS(DbBackups::new, "backups([database])",
      params(ITEM_O), ELEMENT_ZM, flag(NDT), DB_URI, Perm.CREATE),
  /** XQuery function. */
  _DB_CONTENT_TYPE(DbContentType::new, "content-type(database,path)",
      params(STRING_O, STRING_O), STRING_O, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_COPY(DbCopy::new, "copy(database, new-name)",
      params(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_CREATE(DbCreate::new, "create(name[,inputs[,paths[,options]]])",
      params(STRING_O, ITEM_ZM, STRING_ZM, MAP_ZO), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_CREATE_BACKUP(DbCreateBackup::new, "create-backup(database[,options])",
      params(STRING_O, MAP_ZO), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_DELETE(DbDelete::new, "delete(database,path)",
      params(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_DIR(DbDir::new, "dir(database,path)",
      params(STRING_O, STRING_O), ELEMENT_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_DROP(DbDrop::new, "drop(database)",
      params(ITEM_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_DROP_BACKUP(DbDropBackup::new, "drop-backup(name)",
      params(STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_EXISTS(DbExists::new, "exists(database[,path])",
      params(STRING_O, STRING_O), BOOLEAN_O, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_EXPORT(DbExport::new, "export(database,path[,param]])",
      params(STRING_O, STRING_O, ITEM_O), EMPTY_SEQUENCE_Z, flag(NDT), DB_URI, Perm.CREATE),
  /** XQuery function. */
  _DB_FLUSH(DbFlush::new, "flush(database)",
      params(ITEM_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_GET(DbGet::new, "get(database[,path])",
      params(STRING_O, STRING_O), DOCUMENT_NODE_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_GET_BINARY(DbGetBinary::new, "get-binary(database[,path])",
      params(STRING_O, STRING_O), ITEM_O, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_GET_ID(DbGetId::new, "get-id(database,ids)",
      params(STRING_O, INTEGER_ZM), NODE_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_GET_PRE(DbGetPre::new, "get-pre(database,pres)",
      params(STRING_O, INTEGER_ZM), NODE_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_GET_VALUE(DbGetValue::new, "get-value(database[,path])",
      params(STRING_O, STRING_O), ITEM_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_INFO(DbInfo::new, "info(database)",
      params(STRING_O), ELEMENT_O, flag(NDT, CNS), DB_URI),
  /** XQuery function. */
  _DB_LIST(DbList::new, "list([database[,path]])",
      params(STRING_O, STRING_O), STRING_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_LIST_DETAILS(DbListDetails::new, "list-details([database[,path]])",
      params(STRING_O, STRING_O), ELEMENT_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_NAME(DbName::new, "name(node)",
      params(NODE_O), STRING_O, DB_URI),
  /** XQuery function. */
  _DB_NODE_ID(DbNodeId::new, "node-id(nodes)",
      params(NODE_ZM), INTEGER_ZM, DB_URI),
  /** XQuery function. */
  _DB_NODE_PRE(DbNodePre::new, "node-pre(nodes)",
      params(NODE_ZM), INTEGER_ZM, DB_URI),
  /** XQuery function (deprecated). */
  _DB_OPEN(DbOpen::new, "open(database[,path])",
      params(STRING_O, STRING_O), DOCUMENT_NODE_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_OPTIMIZE(DbOptimize::new, "optimize(database[,all[,options]])",
      params(STRING_O, BOOLEAN_O, MAP_ZO), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_OPTION(DbOption::new, "option(name)",
      params(STRING_ZO), ITEM_O, DB_URI),
  /** XQuery function. */
  _DB_PATH(DbPath::new, "path(node)",
      params(NODE_O), STRING_O, DB_URI),
  /** XQuery function. */
  _DB_PROPERTY(DbProperty::new, "property(database,name)",
      params(STRING_O, STRING_O), ANY_ATOMIC_TYPE_O, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_PUT(DbPut::new, "put(database,input,path[,options])",
      params(STRING_O, ITEM_O, STRING_O, MAP_ZO), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_PUT_BINARY(DbPutBinary::new, "put-binary(database,input,path)",
      params(STRING_O, STRING_O, ITEM_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_PUT_VALUE(DbPutValue::new, "put-value(database,input,path)",
      params(STRING_O, STRING_O, ITEM_ZM), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_RENAME(DbRename::new, "rename(database,path,new-path)",
      params(STRING_O, STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_RESTORE(DbRestore::new, "restore(backup)",
      params(STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_SYSTEM(DbSystem::new, "system()",
      params(), ELEMENT_O, flag(CNS), DB_URI),
  /** XQuery function. */
  _DB_TEXT(DbText::new, "text(database,strings)",
      params(STRING_O, ITEM_ZM), TEXT_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_TEXT_RANGE(DbTextRange::new, "text-range(database,from,to)",
      params(STRING_O, ITEM_O, ITEM_O), TEXT_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_TOKEN(DbToken::new, "token(database,strings[,name])",
      params(STRING_O, ITEM_ZM, STRING_O), ATTRIBUTE_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_TYPE(DbType::new, "type(database,path)",
      params(STRING_O, STRING_O), STRING_O, flag(NDT), DB_URI),

  // Fetch Module

  /** XQuery function. */
  _FETCH_BINARY(FetchBinary::new, "binary(href)",
      params(STRING_O), BASE64_BINARY_O, flag(NDT), FETCH_URI),
  /** XQuery function. */
  _FETCH_BINARY_DOC(FetchBinaryDoc::new, "binary-doc(value[,options])",
      params(BINARY_O, MAP_ZO), DOCUMENT_NODE_O, flag(NDT), FETCH_URI),
  /** XQuery function. */
  _FETCH_CONTENT_TYPE(FetchContentType::new, "content-type(href)",
      params(STRING_O), STRING_O, flag(NDT), FETCH_URI),
  /** XQuery function. */
  _FETCH_DOC(FetchDoc::new, "doc(href[,options])",
      params(STRING_O, MAP_ZO), DOCUMENT_NODE_O, flag(NDT), FETCH_URI),
  /** XQuery function. */
  _FETCH_TEXT(FetchText::new, "text(href[,encoding[,fallback]])",
      params(STRING_O, STRING_O, BOOLEAN_O), STRING_O, flag(NDT), FETCH_URI),

  // File Module

  /** XQuery function. */
  _FILE_APPEND(FileAppend::new, "append(path,values[,options])",
      params(STRING_O, ITEM_ZM, ITEM_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_APPEND_BINARY(FileAppendBinary::new, "append-binary(path,value)",
      params(STRING_O, BINARY_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_APPEND_TEXT(FileAppendText::new, "append-text(path,value[,encoding])",
      params(STRING_O, STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_APPEND_TEXT_LINES(FileAppendTextLines::new, "append-text-lines(path,values[,encoding])",
      params(STRING_O, STRING_ZM, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_BASE_DIR(FileBaseDir::new, "base-dir()",
      params(), STRING_O, flag(), FILE_URI, Perm.CREATE),
  /** XQuery function. */
  _FILE_CHILDREN(FileChildren::new, "children(path)",
      params(STRING_O), STRING_ZM, flag(NDT), FILE_URI, Perm.CREATE),
  /** XQuery function. */
  _FILE_COPY(FileCopy::new, "copy(source,target)",
      params(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_CREATE_DIR(FileCreateDir::new, "create-dir(path)",
      params(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_CREATE_TEMP_DIR(FileCreateTempDir::new, "create-temp-dir(prefix,suffix[,dir])",
      params(STRING_O, STRING_O, STRING_O), STRING_O, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_CREATE_TEMP_FILE(FileCreateTempFile::new, "create-temp-file(prefix,suffix[,dir])",
      params(STRING_O, STRING_O, STRING_O), STRING_O, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_CURRENT_DIR(FileCurrentDir::new, "current-dir()",
      params(), STRING_O, flag(), FILE_URI, Perm.CREATE),
  /** XQuery function. */
  _FILE_DELETE(FileDelete::new, "delete(path[,recursive])",
      params(STRING_O, BOOLEAN_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_DESCENDANTS(FileDescendants::new, "descendants(path)",
      params(STRING_O), STRING_ZM, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_DIR_SEPARATOR(FileDirSeparator::new, "dir-separator()",
      params(), STRING_O, FILE_URI),
  /** XQuery function. */
  _FILE_EXISTS(FileExists::new, "exists(path)",
      params(STRING_O), BOOLEAN_O, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_IS_ABSOLUTE(FileIsAbsolute::new, "is-absolute(path)",
      params(STRING_O), BOOLEAN_O, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_IS_DIR(FileIsDir::new, "is-dir(path)",
      params(STRING_O), BOOLEAN_O, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_IS_FILE(FileIsFile::new, "is-file(path)",
      params(STRING_O), BOOLEAN_O, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_LAST_MODIFIED(FileLastModified::new, "last-modified(path)",
      params(STRING_O), DATE_TIME_O, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_LINE_SEPARATOR(FileLineSeparator::new, "line-separator()",
      params(), STRING_O, FILE_URI),
  /** XQuery function. */
  _FILE_LIST(FileList::new, "list(path[,recursive[,pattern]])",
      params(STRING_O, BOOLEAN_O, STRING_O), STRING_ZM, flag(NDT), FILE_URI, Perm.CREATE),
  /** XQuery function. */
  _FILE_MOVE(FileMove::new, "move(source,target)",
      params(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_NAME(FileName::new, "name(path)",
      params(STRING_O), STRING_O, FILE_URI),
  /** XQuery function. */
  _FILE_PARENT(FileParent::new, "parent(path)",
      params(STRING_O), STRING_ZO, FILE_URI),
  /** XQuery function. */
  _FILE_PATH_SEPARATOR(FilePathSeparator::new, "path-separator()",
      params(), STRING_O, FILE_URI),
  /** XQuery function. */
  _FILE_PATH_TO_NATIVE(FilePathToNative::new, "path-to-native(path)",
      params(STRING_O), STRING_O, flag(NDT), FILE_URI, Perm.CREATE),
  /** XQuery function. */
  _FILE_PATH_TO_URI(FilePathToUri::new, "path-to-uri(path)",
      params(STRING_O), ANY_URI_O, FILE_URI),
  /** XQuery function. */
  _FILE_READ_BINARY(FileReadBinary::new, "read-binary(path[,offset[,length]])",
      params(STRING_O, INTEGER_O, INTEGER_O), BASE64_BINARY_O, flag(NDT), FILE_URI, Perm.CREATE),
  /** XQuery function. */
  _FILE_READ_TEXT(FileReadText::new, "read-text(path[,encoding[,fallback]])",
      params(STRING_O, STRING_O, BOOLEAN_O), STRING_O, flag(NDT), FILE_URI, Perm.CREATE),
  /** XQuery function. */
  _FILE_READ_TEXT_LINES(FileReadTextLines::new,
      "read-text-lines(path[,encoding[,fallback[,offset[,length]]]])",
      params(STRING_O, STRING_O, BOOLEAN_O, INTEGER_O, INTEGER_O),
      STRING_ZM, flag(NDT), FILE_URI, Perm.CREATE),
  /** XQuery function. */
  _FILE_RESOLVE_PATH(FileResolvePath::new, "resolve-path(path[,base])",
      params(STRING_O, STRING_O), STRING_O, flag(NDT), FILE_URI, Perm.CREATE),
  /** XQuery function. */
  _FILE_SIZE(FileSize::new, "size(path)",
      params(STRING_O), INTEGER_O, flag(NDT), FILE_URI, Perm.CREATE),
  /** XQuery function. */
  _FILE_TEMP_DIR(FileTempDir::new, "temp-dir()",
      params(), STRING_O, FILE_URI),
  /** XQuery function. */
  _FILE_WRITE(FileWrite::new, "write(path,values[,options])",
      params(STRING_O, ITEM_ZM, ITEM_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_WRITE_BINARY(FileWriteBinary::new, "write-binary(path,value[,offset])",
      params(STRING_O, BINARY_O, INTEGER_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_WRITE_TEXT(FileWriteText::new, "write-text(path,value[,encoding])",
      params(STRING_O, STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI, Perm.ADMIN),
  /** XQuery function. */
  _FILE_WRITE_TEXT_LINES(FileWriteTextLines::new, "write-text-lines(path,values[,encoding])",
      params(STRING_O, STRING_ZM, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), FILE_URI, Perm.ADMIN),

  // Fulltext Module

  /** XQuery function. */
  _FT_CONTAINS(FtContains::new, "contains(input,terms[,options])",
      params(ITEM_ZM, ITEM_ZM, MAP_ZO), BOOLEAN_O, flag(NDT), FT_URI),
  /** XQuery function. */
  _FT_COUNT(FtCount::new, "count(nodes)",
      params(NODE_ZM), INTEGER_O, FT_URI),
  /** XQuery function. */
  _FT_EXTRACT(FtExtract::new, "extract(nodes[,name[,length]])",
      params(ITEM_ZM, STRING_O, INTEGER_O), NODE_ZM, FT_URI),
  /** XQuery function. */
  _FT_MARK(FtMark::new, "mark(nodes[,name])",
      params(NODE_ZM, STRING_O), NODE_ZM, FT_URI),
  /** XQuery function. */
  _FT_NORMALIZE(FtNormalize::new, "normalize(value[,options])",
      params(STRING_ZO, MAP_ZO), STRING_O, FT_URI),
  /** XQuery function. */
  _FT_SCORE(FtScore::new, "score(input)",
      params(ITEM_ZM), DOUBLE_ZM, FT_URI),
  /** XQuery function. */
  _FT_SEARCH(FtSearch::new, "search(database,terms[,options])",
      params(STRING_O, ITEM_ZM, MAP_ZO), TEXT_ZM, flag(NDT), FT_URI),
  /** XQuery function. */
  _FT_THESAURUS(FtThesaurus::new, "thesaurus(node,term[,options])",
      params(NODE_O, STRING_O, MAP_ZO), STRING_ZM, flag(NDT), FT_URI),
  /** XQuery function. */
  _FT_TOKENIZE(FtTokenize::new, "tokenize(value[,options])",
      params(STRING_ZO, MAP_ZO), STRING_ZM, FT_URI),
  /** XQuery function. */
  _FT_TOKENS(FtTokens::new, "tokens(database[,prefix])",
      params(STRING_O, STRING_O), ELEMENT_ZM, flag(NDT), FT_URI),

  // Hash Module

  /** XQuery function. */
  _HASH_HASH(HashHash::new, "hash(value,algorithm)",
      params(ANY_ATOMIC_TYPE_O, STRING_O), BASE64_BINARY_O, HASH_URI),
  /** XQuery function. */
  _HASH_MD5(HashMd5::new, "md5(value)",
      params(ANY_ATOMIC_TYPE_O), BASE64_BINARY_O, HASH_URI),
  /** XQuery function. */
  _HASH_SHA1(HashSha1::new, "sha1(value)",
      params(ANY_ATOMIC_TYPE_O), BASE64_BINARY_O, HASH_URI),
  /** XQuery function. */
  _HASH_SHA256(HashSha256::new, "sha256(value)",
      params(ANY_ATOMIC_TYPE_O), BASE64_BINARY_O, HASH_URI),

  // HOF Module

  /** XQuery function. */
  _HOF_CONST(HofConst::new, "const(input,ignore)",
      params(ITEM_ZM, ITEM_ZM), ITEM_ZM, HOF_URI),
  /** XQuery function. */
  _HOF_DROP_WHILE(HofDropWhile::new, "drop-while(input,predicate)",
      params(ITEM_ZM, PREDICATE_O), ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_FOLD_LEFT1(HofFoldLeft1::new, "fold-left1(input,action)",
      params(ITEM_OM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM_O).seqType()),
      ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_ID(HofId::new, "id(value)",
      params(ITEM_ZM), ITEM_ZM, HOF_URI),
  /** XQuery function. */
  _HOF_SCAN_LEFT(HofScanLeft::new, "scan-left(input,zero,action)",
      params(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM_O).seqType()),
      ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_SORT_WITH(HofSortWith::new, "sort-with(input,comparator)",
      params(ITEM_ZM, FuncType.get(BOOLEAN_O, ITEM_O, ITEM_O).seqType()),
      ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_TAKE_WHILE(HofTakeWhile::new, "take-while(input,predicate)",
      params(ITEM_ZM, PREDICATE_O), ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_TOP_K_BY(HofTopKBy::new, "top-k-by(input,key,k)",
      params(ITEM_ZM, FuncType.get(ITEM_O, ITEM_O).seqType(), INTEGER_O),
      ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_TOP_K_WITH(HofTopKWith::new, "top-k-with(input,comparator,k)",
      params(ITEM_ZM, FuncType.get(BOOLEAN_O, ITEM_O, ITEM_O).seqType(), INTEGER_O),
      ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_UNTIL(HofUntil::new, "until(predicate,action,zero)",
      params(FuncType.get(BOOLEAN_O, ITEM_ZM).seqType(),
      FuncType.get(ITEM_ZM, ITEM_ZM).seqType(), ITEM_ZM), ITEM_ZM, flag(HOF), HOF_URI),

  // HTML Module

  /** XQuery function. */
  _HTML_DOC(HtmlDoc::new, "doc(href[,options])",
      params(STRING_O, MAP_ZO), ITEM_ZO, flag(NDT), HTML_URI),
  /** XQuery function. */
  _HTML_PARSE(HtmlParse::new, "parse(value[,options])",
      params(STRING_ZO, MAP_ZO), DOCUMENT_NODE_ZO, HTML_URI),
  /** XQuery function. */
  _HTML_PARSER(HtmlParser::new, "parser()",
      params(), STRING_O, HTML_URI),

  // HTTP Module

  /** XQuery function. */
  _HTTP_SEND_REQUEST(HttpSendRequest::new, "send-request(request[,href[,bodies]])",
      params(NODE_O, STRING_ZO, ITEM_ZM), ITEM_ZM, flag(NDT), HTTP_URI, Perm.CREATE),

  // Index Module

  /** XQuery function. */
  _INDEX_ATTRIBUTE_NAMES(IndexAttributeNames::new, "attribute-names(database)",
      params(STRING_O), ELEMENT_ZM, flag(NDT), INDEX_URI),
  /** XQuery function. */
  _INDEX_ATTRIBUTES(IndexAttributes::new, "attributes(database[,prefix[,ascending]])",
      params(STRING_O, STRING_O, BOOLEAN_O), ELEMENT_ZM, flag(NDT), INDEX_URI),
  /** XQuery function. */
  _INDEX_ELEMENT_NAMES(IndexElementNames::new, "element-names(database)",
      params(STRING_O), ELEMENT_ZM, flag(NDT), INDEX_URI),
  /** XQuery function. */
  _INDEX_FACETS(IndexFacets::new, "facets(database[,type])",
      params(STRING_O, STRING_O), DOCUMENT_NODE_O, flag(NDT), INDEX_URI),
  /** XQuery function. */
  _INDEX_TEXTS(IndexTexts::new, "texts(database[,prefix[,ascending]])",
      params(STRING_O, STRING_O, BOOLEAN_O), ELEMENT_ZM, flag(NDT), INDEX_URI),
  /** XQuery function. */
  _INDEX_TOKENS(IndexTokens::new, "tokens(database)",
      params(STRING_O), ELEMENT_ZM, flag(NDT), INDEX_URI),

  // Inspection Module

  /** XQuery function. */
  _INSPECT_CONTEXT(InspectContext::new, "context()",
      params(), ELEMENT_O, flag(NDT), INSPECT_URI, Perm.CREATE),
  /** XQuery function. */
  _INSPECT_FUNCTION(InspectFunction::new, "function(function)",
      params(STRING_O), ELEMENT_O, INSPECT_URI),
  /** XQuery function. */
  _INSPECT_FUNCTION_ANNOTATIONS(InspectFunctionAnnotations::new, "function-annotations(function)",
      params(FUNCTION_O), MAP_ZO, INSPECT_URI),
  /** XQuery function. */
  _INSPECT_FUNCTIONS(InspectFunctions::new, "functions([uri])",
      params(STRING_O), FUNCTION_ZM, flag(POS, CTX, CNS, NDT), INSPECT_URI, Perm.ADMIN),
  /** XQuery function. */
  _INSPECT_MODULE(InspectModule::new, "module(href)",
      params(STRING_O), ELEMENT_O, flag(NDT), INSPECT_URI, Perm.CREATE),
  /** XQuery function. */
  _INSPECT_TYPE(InspectType::new, "type(value[,options])",
      params(ITEM_ZM, MAP_ZO), STRING_O, INSPECT_URI),
  /** XQuery function. */
  _INSPECT_STATIC_CONTEXT(InspectStaticContext::new, "static-context(function,name)",
      params(FUNCTION_O, STRING_O), ITEM_ZM, INSPECT_URI),
  /** XQuery function. */
  _INSPECT_XQDOC(InspectXqdoc::new, "xqdoc(href)",
      params(STRING_O), ELEMENT_O, flag(NDT), INSPECT_URI, Perm.CREATE),

  // Jobs Module

  /** XQuery function. */
  _JOB_BINDINGS(JobBindings::new, "bindings(id)",
      params(STRING_O), MAP_O, flag(NDT), JOB_URI, Perm.ADMIN),
  /** XQuery function. */
  _JOB_CURRENT(JobCurrent::new, "current()",
      params(), STRING_O, flag(NDT), JOB_URI),
  /** XQuery function. */
  _JOB_EVAL(JobEval::new, "eval(input[,bindings[,options]])",
      params(ANY_ATOMIC_TYPE_O, MAP_ZO, MAP_ZO), STRING_O, flag(NDT), JOB_URI, Perm.ADMIN),
  /** XQuery function. */
  _JOB_FINISHED(JobFinished::new, "finished(id)",
      params(STRING_O), BOOLEAN_O, flag(NDT), JOB_URI, Perm.ADMIN),
  /** XQuery function. */
  _JOB_LIST(JobList::new, "list()",
      params(), STRING_ZM, flag(NDT), JOB_URI, Perm.ADMIN),
  /** XQuery function. */
  _JOB_LIST_DETAILS(JobListDetails::new, "list-details([id])",
      params(STRING_O), ELEMENT_ZM, flag(NDT), JOB_URI, Perm.ADMIN),
  /** XQuery function. */
  _JOB_REMOVE(JobRemove::new, "remove(id[,options])",
      params(STRING_O, MAP_ZO), EMPTY_SEQUENCE_Z, flag(NDT), JOB_URI, Perm.ADMIN),
  /** XQuery function. */
  _JOB_RESULT(JobResult::new, "result(id[,options])",
      params(STRING_O, MAP_ZO), ITEM_ZM, flag(NDT), JOB_URI, Perm.ADMIN),
  /** XQuery function. */
  _JOB_SERVICES(JobServices::new, "services()",
      params(), ELEMENT_ZM, flag(NDT), JOB_URI, Perm.ADMIN),
  /** XQuery function. */
  _JOB_WAIT(JobWait::new, "wait(id)",
      params(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), JOB_URI, Perm.ADMIN),

  // JSON Module

  /** XQuery function. */
  _JSON_DOC(JsonDoc::new, "doc(href[,options])",
      params(STRING_O, MAP_ZO), ITEM_ZO, flag(NDT), JSON_URI),
  /** XQuery function. */
  _JSON_PARSE(JsonParse::new, "parse(value[,options])",
      params(STRING_ZO, MAP_ZO), ITEM_ZO, JSON_URI),
  /** XQuery function. */
  _JSON_SERIALIZE(JsonSerialize::new, "serialize(input[,options])",
      params(ITEM_ZO, MAP_ZO), STRING_O, JSON_URI),

  // Lazy Module

  /** XQuery function. */
  _LAZY_CACHE(LazyCache::new, "cache(value[,lazy])",
      params(ITEM_ZM, BOOLEAN_O), ITEM_ZM, LAZY_URI),
  /** XQuery function. */
  _LAZY_IS_CACHED(LazyIsCached::new, "is-cached(value)",
      params(ITEM_O), BOOLEAN_O, LAZY_URI),
  /** XQuery function. */
  _LAZY_IS_LAZY(LazyIsLazy::new, "is-lazy(value)",
      params(ITEM_O), BOOLEAN_O, LAZY_URI),

  // Process Module

  /** XQuery function. */
  _PROC_EXECUTE(ProcExecute::new, "execute(command[,arguments[,options]])",
      params(STRING_O, STRING_ZM, ANY_ATOMIC_TYPE_O), ELEMENT_O, flag(NDT), PROC_URI, Perm.ADMIN),
  /** XQuery function. */
  _PROC_FORK(ProcFork::new, "fork(command[,arguments[,options]])",
      params(STRING_O, STRING_ZM, ANY_ATOMIC_TYPE_O),
      EMPTY_SEQUENCE_Z, flag(NDT), PROC_URI, Perm.ADMIN),
  /** XQuery function. */
  _PROC_PROPERTY(ProcProperty::new, "property(name)",
      params(STRING_O), STRING_ZO, flag(NDT), PROC_URI),
  /** XQuery function. */
  _PROC_PROPERTY_NAMES(ProcPropertyNames::new, "property-names()",
      params(), STRING_ZM, flag(NDT), PROC_URI),
  /** XQuery function. */
  _PROC_SYSTEM(ProcSystem::new, "system(command[,arguments[,options]])",
      params(STRING_O, STRING_ZM, ANY_ATOMIC_TYPE_O), STRING_O, flag(NDT), PROC_URI, Perm.ADMIN),

  // Profiling Module

  /** XQuery function. */
  _PROF_CURRENT_MS(ProfCurrentMs::new, "current-ms()",
      params(), INTEGER_O, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_CURRENT_NS(ProfCurrentNs::new, "current-ns()",
      params(), INTEGER_O, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_DUMP(ProfDump::new, "dump(value[,label])",
      params(ITEM_ZM, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_GC(ProfGc::new, "gc([count])",
      params(INTEGER_O), EMPTY_SEQUENCE_Z, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_HUMAN(ProfHuman::new, "human(value)",
      params(INTEGER_O), STRING_O, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_MEMORY(ProfMemory::new, "memory(value[,label])",
      params(ITEM_ZM, STRING_O), ITEM_ZM, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_SLEEP(ProfSleep::new, "sleep(ms)",
      params(INTEGER_O), EMPTY_SEQUENCE_Z, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_RUNTIME(ProfRuntime::new, "runtime(name)",
      params(STRING_O), ITEM_O, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_TIME(ProfTime::new, "time(value[,label])",
      params(ITEM_ZM, STRING_O), ITEM_ZM, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_TRACK(ProfTrack::new, "track(value[,options])",
      params(ITEM_ZM, MAP_ZO), MAP_O, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_TYPE(ProfType::new, "type(value)",
      params(ITEM_ZM), ITEM_ZM, PROF_URI),
  /** XQuery function. */
  _PROF_VARIABLES(ProfVariables::new, "variables()",
      params(), EMPTY_SEQUENCE_Z, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_VOID(ProfVoid::new, "void(value[,skip])",
      params(ITEM_ZM, BOOLEAN_O), EMPTY_SEQUENCE_Z, flag(NDT), PROF_URI),

  // Random Module

  /** XQuery function. */
  _RANDOM_DOUBLE(RandomDouble::new, "double()",
      params(), DOUBLE_O, flag(NDT), RANDOM_URI),
  /** XQuery function. */
  _RANDOM_GAUSSIAN(RandomGaussian::new, "gaussian(count)",
      params(INTEGER_O), DOUBLE_ZM, flag(NDT), RANDOM_URI),
  /** XQuery function. */
  _RANDOM_INTEGER(RandomInteger::new, "integer([max])",
      params(INTEGER_O), INTEGER_O, flag(NDT), RANDOM_URI),
  /** XQuery function. */
  _RANDOM_SEEDED_DOUBLE(RandomSeededDouble::new, "seeded-double(seed,count)",
      params(INTEGER_O, INTEGER_O), DOUBLE_ZM, RANDOM_URI),
  /** XQuery function. */
  _RANDOM_SEEDED_INTEGER(RandomSeededInteger::new, "seeded-integer(seed,count[,max])",
      params(INTEGER_O, INTEGER_O, INTEGER_O), INTEGER_ZM, RANDOM_URI),
  /** XQuery function. */
  _RANDOM_SEEDED_PERMUTATION(RandomSeededPermutation::new, "seeded-permutation(seed,input)",
      params(INTEGER_O, ITEM_ZM), ITEM_ZM, RANDOM_URI),
  /** XQuery function. */
  _RANDOM_UUID(RandomUuid::new, "uuid()",
      params(), STRING_O, flag(NDT), RANDOM_URI),

  // Repository Module

  /** XQuery function. */
  _REPO_DELETE(RepoDelete::new, "delete(href)",
      params(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), REPO_URI, Perm.CREATE),
  /** XQuery function. */
  _REPO_INSTALL(RepoInstall::new, "install(href)",
      params(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), REPO_URI, Perm.CREATE),
  /** XQuery function. */
  _REPO_LIST(RepoList::new, "list()",
      params(), STRING_ZM, flag(NDT), REPO_URI, Perm.CREATE),

  // SQL Module

  /** XQuery function. */
  _SQL_CLOSE(SqlClose::new, "close(id)",
      params(INTEGER_O), EMPTY_SEQUENCE_Z, flag(NDT), SQL_URI, Perm.CREATE),
  /** XQuery function. */
  _SQL_COMMIT(SqlCommit::new, "commit(id)",
      params(INTEGER_O), EMPTY_SEQUENCE_Z, flag(NDT), SQL_URI, Perm.CREATE),
  /** XQuery function. */
  _SQL_CONNECT(SqlConnect::new, "connect(url[,username[,password[,options]]]]])",
      params(STRING_O, STRING_O, STRING_O, MAP_ZO), INTEGER_O, flag(NDT), SQL_URI, Perm.CREATE),
  /** XQuery function. */
  _SQL_EXECUTE(SqlExecute::new, "execute(id,query[,options])",
      params(INTEGER_O, STRING_O, MAP_ZO), ITEM_ZM, flag(NDT), SQL_URI, Perm.CREATE),
  /** XQuery function. */
  _SQL_EXECUTE_PREPARED(SqlExecutePrepared::new, "execute-prepared(id[,params[,options]])",
      params(INTEGER_O, ELEMENT_O, MAP_ZO), ITEM_ZM, flag(NDT), SQL_URI, Perm.CREATE),
  /** XQuery function. */
  _SQL_INIT(SqlInit::new, "init(class)",
      params(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), SQL_URI, Perm.CREATE),
  /** XQuery function. */
  _SQL_PREPARE(SqlPrepare::new, "prepare(id,statement)",
      params(INTEGER_O, STRING_O), INTEGER_O, flag(NDT), SQL_URI, Perm.CREATE),
  /** XQuery function. */
  _SQL_ROLLBACK(SqlRollback::new, "rollback(id)",
      params(INTEGER_O), EMPTY_SEQUENCE_Z, flag(NDT), SQL_URI, Perm.CREATE),

  // Store Module

  /** XQuery function. */
  _STORE_CLEAR(StoreClear::new, "clear()",
      params(), EMPTY_SEQUENCE_Z, flag(NDT), STORE_URI, Perm.CREATE),
  /** XQuery function. */
  _STORE_DELETE(StoreDelete::new, "delete(name)",
      params(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), STORE_URI, Perm.CREATE),
  /** XQuery function. */
  _STORE_GET(StoreGet::new, "get(key)",
      params(STRING_O), ITEM_ZM, flag(NDT), STORE_URI, Perm.CREATE),
  /** XQuery function. */
  _STORE_GET_OR_PUT(StoreGetOrPut::new, "get-or-put(key,put)",
      params(STRING_O, FuncType.get(ITEM_ZM).seqType()),
      ITEM_ZM, flag(HOF, NDT), STORE_URI, Perm.CREATE),
  /** XQuery function. */
  _STORE_KEYS(StoreKeys::new, "keys()", params(), STRING_ZM, flag(NDT), STORE_URI, Perm.CREATE),
  /** XQuery function. */
  _STORE_LIST(StoreList::new, "list()", params(), STRING_ZM, flag(NDT), STORE_URI, Perm.CREATE),
  /** XQuery function. */
  _STORE_PUT(StorePut::new, "put(key,value)",
      params(STRING_O, ITEM_ZM), EMPTY_SEQUENCE_Z, flag(NDT), STORE_URI, Perm.CREATE),
  /** XQuery function. */
  _STORE_READ(StoreRead::new, "read([name])",
      params(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), STORE_URI, Perm.CREATE),
  /** XQuery function. */
  _STORE_REMOVE(StoreRemove::new, "remove(key)",
      params(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), STORE_URI, Perm.CREATE),
  /** XQuery function. */
  _STORE_WRITE(StoreWrite::new, "write([name])",
      params(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), STORE_URI, Perm.CREATE),

  // Strings Module

  /** XQuery function. */
  _STRING_COLOGNE_PHONETIC(StringColognePhonetic::new, "cologne-phonetic(value)",
      params(STRING_O), STRING_O, STRING_URI),
  /** XQuery function. */
  _STRING_CR(StringCr::new, "cr()",
      params(), STRING_O, STRING_URI),
  /** XQuery function. */
  _STRING_FORMAT(StringFormat::new, "format(format,value1[,...])",
      params(STRING_O, ITEM_O), STRING_O, STRING_URI),
  /** XQuery function. */
  _STRING_LEVENSHTEIN(StringLevenshtein::new, "levenshtein(value1,value2)",
      params(STRING_O, STRING_O), DOUBLE_O, STRING_URI),
  /** XQuery function. */
  _STRING_NL(StringNl::new, "nl()",
      params(), STRING_O, STRING_URI),
  /** XQuery function. */
  _STRING_SOUNDEX(StringSoundex::new, "soundex(value)",
      params(STRING_O), STRING_O, STRING_URI),
  /** XQuery function. */
  _STRING_TAB(StringTab::new, "tab()",
      params(), STRING_O, STRING_URI),


  // Unit Module

  /** XQuery function. */
  _UNIT_ASSERT(UnitAssert::new, "assert(test[,failure])",
      params(ITEM_ZM, ITEM_O), ITEM_ZM, flag(NDT), UNIT_URI),
  /** XQuery function. */
  _UNIT_ASSERT_EQUALS(UnitAssertEquals::new, "assert-equals(result,expected[,failure])",
      params(ITEM_ZM, ITEM_ZM, ITEM_O), ITEM_ZM, flag(NDT), UNIT_URI),
  /** XQuery function. */
  _UNIT_FAIL(UnitFail::new, "fail([failure])",
      params(ITEM_O), ITEM_ZM, flag(NDT), UNIT_URI),

  // Update Module

  /** XQuery function. */
  _UPDATE_APPLY(UpdateApply::new, "apply(function,arguments)",
      params(FUNCTION_O, ARRAY_O),
      EMPTY_SEQUENCE_Z, flag(POS, CTX, UPD, HOF), UPDATE_URI, Perm.ADMIN),
  /** XQuery function. */
  _UPDATE_CACHE(UpdateCache::new, "cache([reset])",
      params(BOOLEAN_O), ITEM_ZM, flag(NDT), UPDATE_URI),
  /** XQuery function. */
  _UPDATE_FOR_EACH(UpdateForEach::new, "for-each(input,action)",
      params(ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O).seqType()),
      EMPTY_SEQUENCE_Z, flag(UPD, HOF), UPDATE_URI),
  /** XQuery function. */
  _UPDATE_FOR_EACH_PAIR(UpdateForEachPair::new, "for-each-pair(input1,input2,action)",
      params(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O, ITEM_O).seqType()),
      EMPTY_SEQUENCE_Z, flag(UPD, HOF), UPDATE_URI),
  /** XQuery function. */
  _UPDATE_MAP_FOR_EACH(UpdateMapForEach::new, "map-for-each(map,action)",
      params(MAP_O, FuncType.get(ITEM_ZM, ANY_ATOMIC_TYPE_O, ITEM_ZM).seqType()),
      EMPTY_SEQUENCE_Z, flag(UPD, HOF), UPDATE_URI),
  /** XQuery function. */
  _UPDATE_OUTPUT(UpdateOutput::new, "output(value)",
      params(ITEM_ZM), EMPTY_SEQUENCE_Z, flag(UPD), UPDATE_URI),

  // User Module

  /** XQuery function. */
  _USER_ALTER(UserAlter::new, "alter(name,newname)",
      params(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), USER_URI, Perm.ADMIN),
  /** XQuery function. */
  _USER_CHECK(UserCheck::new, "check(name,password)",
      params(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), USER_URI),
  /** XQuery function. */
  _USER_CREATE(UserCreate::new, "create(name,password[,permissions[,patterns[,info]]])",
      params(STRING_O, STRING_O, STRING_ZM, STRING_ZM, ELEMENT_O),
      EMPTY_SEQUENCE_Z, flag(UPD), USER_URI, Perm.ADMIN),
  /** XQuery function. */
  _USER_CURRENT(UserCurrent::new, "current()",
      params(), STRING_O, USER_URI),
  /** XQuery function. */
  _USER_DROP(UserDrop::new, "drop(name[,patterns])",
      params(STRING_O, STRING_ZM), EMPTY_SEQUENCE_Z, flag(UPD), USER_URI),
  /** XQuery function. */
  _USER_EXISTS(UserExists::new, "exists(name)",
      params(STRING_O), BOOLEAN_O, flag(NDT), USER_URI, Perm.ADMIN),
  /** XQuery function. */
  _USER_GRANT(UserGrant::new, "grant(name,permissions[,patterns])",
      params(STRING_O, STRING_ZM, STRING_ZM), EMPTY_SEQUENCE_Z, flag(UPD), USER_URI, Perm.ADMIN),
  /** XQuery function. */
  _USER_INFO(UserInfo::new, "info([name])",
      params(STRING_O), ELEMENT_O, USER_URI),
  /** XQuery function. */
  _USER_LIST(UserList::new, "list()",
      params(), ELEMENT_ZM, flag(NDT), USER_URI),
  /** XQuery function. */
  _USER_LIST_DETAILS(UserListDetails::new, "list-details([name])",
      params(STRING_O), ELEMENT_ZM, flag(NDT), USER_URI),
  /** XQuery function. */
  _USER_PASSWORD(UserPassword::new, "password(name,password)",
      params(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), USER_URI),
  /** XQuery function. */
  _USER_UPDATE_INFO(UserUpdateInfo::new, "update-info(element[,name])",
      params(ELEMENT_O, STRING_O), EMPTY_SEQUENCE_Z, flag(UPD), USER_URI),

  // Utility Module

  /** XQuery function. */
  _UTIL_ARRAY_MEMBER(UtilArrayMember::new, "array-member(input)",
      params(ITEM_ZM), ARRAY_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_ARRAY_MEMBERS(UtilArrayMembers::new, "array-members(array)",
      params(ARRAY_O), ARRAY_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_ARRAY_VALUES(UtilArrayValues::new, "array-values(array)",
      params(ARRAY_O), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_CHARS(UtilChars::new, "chars(value)",
      params(STRING_O), STRING_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_COUNT_WITHIN(UtilCountWithin::new, "count-within(input,min[,max])",
      params(ITEM_ZM, INTEGER_O, INTEGER_O), BOOLEAN_O, UTIL_URI),
  /** XQuery function. */
  _UTIL_DEEP_EQUAL(UtilDeepEqual::new, "deep-equal(input1,input2[,options])",
      params(ITEM_ZM, ITEM_ZM, STRING_ZM), BOOLEAN_O, UTIL_URI),
  /** XQuery function. */
  _UTIL_DDO(UtilDdo::new, "ddo(nodes)",
      params(NODE_ZM), NODE_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_DUPLICATES(UtilDuplicates::new, "duplicates(values[,collation])",
      params(ANY_ATOMIC_TYPE_ZM, STRING_O), ANY_ATOMIC_TYPE_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_IF(UtilIf::new, "if(condition,then[,else])",
      params(ITEM_ZM, ITEM_ZM, ITEM_ZM), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_INIT(UtilInit::new, "init(input)",
      params(ITEM_ZM), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_INTERSPERSE(UtilIntersperse::new, "intersperse(values[,separator])",
      params(ITEM_ZM, ITEM_ZM), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_ITEM(UtilItem::new, "item(input,position)",
      params(ITEM_ZM, DOUBLE_O), ITEM_ZO, UTIL_URI),
  /** XQuery function. */
  _UTIL_LAST(UtilLast::new, "last(input)",
      params(ITEM_ZM), ITEM_ZO, UTIL_URI),
  /** XQuery function. */
  _UTIL_MAP_ENTRIES(UtilMapEntries::new, "map-entries(map)",
      params(MAP_O), MAP_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_MAP_VALUES(UtilMapValues::new, "map-values(map)",
      params(MAP_O), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_OR(UtilOr::new, "or(input,default)",
      params(ITEM_ZM, ITEM_ZM), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_RANGE(UtilRange::new, "range(input,first,last)",
      params(ITEM_ZM, DOUBLE_O, DOUBLE_O), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_REPLICATE(UtilReplicate::new, "replicate(input,count[,multiple])",
      params(ITEM_ZM, INTEGER_O, BOOLEAN_O), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_ROOT(UtilRoot::new, "root(nodes)",
      params(NODE_ZM), DOCUMENT_NODE_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_STRIP_NAMESPACES(UtilStripNamespaces::new, "strip-namespaces(node[,prefixes])",
      params(NODE_O, STRING_ZM), NODE_O, UTIL_URI),

  // Validate Module

  /** XQuery function. */
  _VALIDATE_DTD(ValidateDtd::new, "dtd(input[,schema])",
      params(ITEM_O, ITEM_O), EMPTY_SEQUENCE_Z, flag(NDT), VALIDATE_URI, Perm.CREATE),
  /** XQuery function. */
  _VALIDATE_DTD_INFO(ValidateDtdInfo::new, "dtd-info(input[,schema])",
      params(ITEM_O, ITEM_O), STRING_ZM, flag(NDT), VALIDATE_URI, Perm.CREATE),
  /** XQuery function. */
  _VALIDATE_DTD_REPORT(ValidateDtdReport::new, "dtd-report(input[,schema])",
      params(ITEM_O, ITEM_O), ELEMENT_O, flag(NDT), VALIDATE_URI, Perm.CREATE),
  /** XQuery function. */
  _VALIDATE_RNG(ValidateRng::new, "rng(input,schema[,compact])",
      params(ITEM_O, ITEM_O, BOOLEAN_O), STRING_ZM, flag(NDT), VALIDATE_URI, Perm.CREATE),
  /** XQuery function. */
  _VALIDATE_RNG_INFO(ValidateRngInfo::new, "rng-info(input,schema[,compact])",
      params(ITEM_O, ITEM_O, BOOLEAN_O), STRING_ZM, flag(NDT), VALIDATE_URI, Perm.CREATE),
  /** XQuery function. */
  _VALIDATE_RNG_REPORT(ValidateRngReport::new, "rng-report(input,schema[,compact])",
      params(ITEM_O, ITEM_O, BOOLEAN_O), ELEMENT_O, flag(NDT), VALIDATE_URI, Perm.CREATE),
  /** XQuery function. */
  _VALIDATE_XSD(ValidateXsd::new, "xsd(input[,schema[,options]])",
      params(ITEM_O, ITEM_O, MAP_ZO), EMPTY_SEQUENCE_Z, flag(NDT), VALIDATE_URI, Perm.CREATE),
  /** XQuery function. */
  _VALIDATE_XSD_INFO(ValidateXsdInfo::new, "xsd-info(input[,schema[,options]])",
      params(ITEM_O, ITEM_O, MAP_ZO), STRING_ZM, flag(NDT), VALIDATE_URI, Perm.CREATE),
  /** XQuery function. */
  _VALIDATE_XSD_PROCESSOR(ValidateXsdProcessor::new, "xsd-processor()",
      params(), STRING_O, VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_XSD_REPORT(ValidateXsdReport::new, "xsd-report(input[,schema[,options]])",
      params(ITEM_O, ITEM_O, MAP_ZO), ELEMENT_O, flag(NDT), VALIDATE_URI, Perm.CREATE),
  /** XQuery function. */
  _VALIDATE_XSD_VERSION(ValidateXsdVersion::new, "xsd-version()",
      params(), STRING_O, VALIDATE_URI),

  // Web Module

  /** XQuery function. */
  _WEB_CONTENT_TYPE(WebContentType::new, "content-type(href)",
      params(STRING_O), STRING_O, WEB_URI),
  /** XQuery function. */
  _WEB_CREATE_URL(WebCreateUrl::new, "create-url(url,params[,anchor])",
      params(STRING_O, MAP_O, STRING_O), STRING_O, WEB_URI),
  /** XQuery function. */
  _WEB_DECODE_URL(WebDecodeUrl::new, "decode-url(value)",
      params(STRING_O), STRING_O, WEB_URI),
  /** XQuery function. */
  _WEB_ENCODE_URL(WebEncodeUrl::new, "encode-url(value)",
      params(STRING_O), STRING_O, WEB_URI),
  /** XQuery function. */
  _WEB_ERROR(WebError::new, "error(code,description)",
      params(INTEGER_O, STRING_O), ITEM_ZM, flag(NDT), WEB_URI),
  /** XQuery function. */
  _WEB_FORWARD(WebForward::new, "forward(href[,params])",
      params(STRING_O, MAP_O), ELEMENT_O, WEB_URI),
  /** XQuery function. */
  _WEB_REDIRECT(WebRedirect::new, "redirect(href[,params[,anchor]])",
      params(STRING_O, MAP_O, STRING_O), ELEMENT_O, WEB_URI),
  /** XQuery function. */
  _WEB_RESPONSE_HEADER(WebResponseHeader::new, "response-header([output[,headers[,attributes]]])",
      params(MAP_ZO, MAP_ZO, MAP_ZO), ELEMENT_O, WEB_URI),

  // XQuery Module

  /** XQuery function. */
  _XQUERY_EVAL(XQueryEval::new, "eval(input[,bindings[,options]])",
      params(ANY_ATOMIC_TYPE_O, MAP_ZO, MAP_ZO), ITEM_ZM, flag(NDT), XQUERY_URI),
  /** XQuery function. */
  _XQUERY_EVAL_UPDATE(XQueryEvalUpdate::new, "eval-update(input[,bindings[,options]])",
      params(ANY_ATOMIC_TYPE_O, MAP_ZO, MAP_ZO), EMPTY_SEQUENCE_Z,
      flag(UPD), XQUERY_URI),
  /** XQuery function. */
  _XQUERY_FORK_JOIN(XQueryForkJoin::new, "fork-join(functions)",
      params(FUNCTION_ZM), ITEM_ZM, flag(HOF), XQUERY_URI),
  /** XQuery function. */
  _XQUERY_PARSE(XQueryParse::new, "parse(input[,options])",
      params(ANY_ATOMIC_TYPE_O, MAP_ZO), NODE_O, flag(NDT), XQUERY_URI, Perm.CREATE),

  // XSLT Module

  /** XQuery function. */
  _XSLT_INIT(XsltInit::new, "init()",
      params(), NODE_O, flag(NDT), XSLT_URI, Perm.CREATE),
  /** XQuery function. */
  _XSLT_PROCESSOR(XsltProcessor::new, "processor()",
      params(), STRING_O, XSLT_URI),
  /** XQuery function. */
  _XSLT_TRANSFORM(XsltTransform::new, "transform(input,stylesheet[,params[,options]])",
      params(ITEM_O, ITEM_O, MAP_ZO, MAP_ZO), NODE_O, flag(NDT), XSLT_URI, Perm.CREATE),
  /** XQuery function. */
  _XSLT_TRANSFORM_REPORT(XsltTransformReport::new,
      "transform-report(input,stylesheet[,params[,options]])",
      params(ITEM_O, ITEM_O, MAP_ZO, MAP_ZO), MAP_O, flag(NDT), XSLT_URI, Perm.CREATE),
  /** XQuery function. */
  _XSLT_TRANSFORM_TEXT(XsltTransformText::new,
      "transform-text(input,stylesheet[,params[,options]])",
      params(ITEM_O, ITEM_O, MAP_ZO, MAP_ZO), STRING_O, flag(NDT), XSLT_URI, Perm.CREATE),
  /** XQuery function. */
  _XSLT_VERSION(XsltVersion::new, "version()",
      params(), STRING_O, XSLT_URI);

  /** Function definition. */
  private final FuncDefinition definition;

  /**
   * Constructs a function signature; calls
   * {@link #Function(Supplier, String, SeqType[], SeqType, EnumSet)}.
   * @param supplier function implementation constructor
   * @param desc descriptive function string
   * @param params parameter types
   * @param seqType return type
   */
  Function(final Supplier<? extends StandardFunc> supplier, final String desc,
      final SeqType[] params, final SeqType seqType) {
    this(supplier, desc, params, seqType, EnumSet.noneOf(Flag.class));
  }

  /**
   * Constructs a function signature; calls
   * {@link #Function(Supplier, String, SeqType[], SeqType, EnumSet)}.
   * @param supplier function implementation constructor
   * @param desc descriptive function string
   * @param params parameter types
   * @param type return type
   * @param uri uri
   */
  Function(final Supplier<? extends StandardFunc> supplier, final String desc,
      final SeqType[] params, final SeqType type, final byte[] uri) {
    this(supplier, desc, params, type, EnumSet.noneOf(Flag.class), uri);
  }

  /**
   * Constructs a function signature; calls
   * {@link #Function(Supplier, String, SeqType[], SeqType, EnumSet, byte[])}.
   * @param supplier function implementation constructor
   * @param desc descriptive function string
   * @param params parameter types
   * @param seqType return type
   * @param flag static function properties
   */
  Function(final Supplier<? extends StandardFunc> supplier, final String desc,
      final SeqType[] params, final SeqType seqType, final EnumSet<Flag> flag) {
    this(supplier, desc, params, seqType, flag, FN_URI);
  }

  /**
   * Constructs a function signature.
   * @param supplier function implementation constructor
   * @param desc descriptive function string, containing the function name and its parameters in
   *   parentheses. Optional parameters are represented in nested square brackets; three dots
   *   indicate that the number of parameters of a function is not limited.
   * @param params parameter types
   * @param seqType return type
   * @param flags static function properties
   * @param uri uri
   */
  Function(final Supplier<? extends StandardFunc> supplier, final String desc,
      final SeqType[] params, final SeqType seqType, final EnumSet<Flag> flags, final byte[] uri) {
    this(supplier, desc, params, seqType, flags, uri, Perm.NONE);
  }

  /**
   * Constructs a function signature.
   * @param supplier function implementation constructor
   * @param desc descriptive function string, containing the function name and its parameters in
   *   parentheses. Optional parameters are represented in nested square brackets; three dots
   *   indicate that the number of parameters of a function is not limited.
   * @param params parameter types
   * @param seqType return type
   * @param flags static function properties
   * @param uri uri
   * @param perm minimum permission
   */
  Function(final Supplier<? extends StandardFunc> supplier, final String desc,
      final SeqType[] params, final SeqType seqType, final EnumSet<Flag> flags, final byte[] uri,
      final Perm perm) {
    definition = new FuncDefinition(supplier, desc, params, seqType, flags, uri, perm);
  }

  @Override
  public FuncDefinition definition() {
    return definition;
  }

  /**
   * Returns an array representation of the specified sequence types.
   * @param params parameters
   * @return array
   */
  private static SeqType[] params(final SeqType... params) {
    return params;
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
