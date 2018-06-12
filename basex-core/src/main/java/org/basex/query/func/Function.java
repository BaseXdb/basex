package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Flag.*;
import static org.basex.query.value.type.SeqType.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
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
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Definitions of all built-in XQuery functions.
 * New namespace mappings for function prefixes and URIs must be added to the static initializer of
 * the {@link NSGlobal} class.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public enum Function {

  // Standard functions

  /** XQuery function. */
  ABS(FnAbs.class, "abs(num)", arg(NUM_ZO), NUM_ZO),
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
      arg(STR_ZO, STR_O, STR_O), ELM_O, flag(CNS)),
  /** XQuery function. */
  APPLY(FnApply.class, "apply(function,args)", arg(FUNC_O, ARRAY_O), ITEM_ZM,
      flag(Flag.POS, CTX, NDT, HOF)),
  /** XQuery function. */
  AVAILABLE_ENVIRONMENT_VARIABLES(FnAvailableEnvironmentVariables.class,
      "available-environment-variables()", arg(), STR_ZM),
  /** XQuery function. */
  AVG(FnAvg.class, "avg(items)", arg(AAT_ZM), AAT_ZO),
  /** XQuery function. */
  BASE_URI(FnBaseUri.class, "base-uri([node])", arg(NOD_ZO), URI_ZO),
  /** XQuery function. */
  BOOLEAN(FnBoolean.class, "boolean(items)", arg(ITEM_ZM), BLN_O),
  /** XQuery function. */
  CEILING(FnCeiling.class, "ceiling(num)", arg(NUM_ZO), NUM_ZO),
  /** XQuery function. */
  CODEPOINT_EQUAL(FnCodepointEqual.class, "codepoint-equal(string1,string2)",
      arg(STR_ZO, STR_ZO), BLN_ZO),
  /** XQuery function. */
  CODEPOINTS_TO_STRING(FnCodepointsToString.class, "codepoints-to-string(nums)",
      arg(ITR_ZM), STR_O),
  /** XQuery function. */
  COLLECTION(FnCollection.class, "collection([uri])", arg(STR_ZO), DOC_ZM),
  /** XQuery function. */
  COMPARE(FnCompare.class, "compare(first,second[,collation])", arg(STR_ZO, STR_ZO, STR_O), ITR_ZO),
  /** XQuery function. */
  CONCAT(FnConcat.class, "concat(atom1,atom2[,...])", arg(AAT_ZO, AAT_ZO), STR_O),
  /** XQuery function. */
  CONTAINS(FnContains.class, "contains(string,sub[,collation])", arg(STR_ZO, STR_ZO, STR_O), BLN_O),
  /** XQuery function. */
  CONTAINS_TOKEN(FnContainsToken.class, "contains-token(strings,token[,collation])",
      arg(STR_ZM, STR_O, STR_O), BLN_O),
  /** XQuery function. */
  COUNT(FnCount.class, "count(items)", arg(ITEM_ZM), ITR_O),
  /** XQuery function. */
  CURRENT_DATE(FnCurrentDate.class, "current-date()", arg(), DAT_O),
  /** XQuery function. */
  CURRENT_DATETIME(FnCurrentDateTime.class, "current-dateTime()", arg(), DTM_O),
  /** XQuery function. */
  CURRENT_TIME(FnCurrentTime.class, "current-time()", arg(), TIM_O),
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
      arg(ITEM_ZM, ITEM_ZM, STR_O), BLN_O),
  /** XQuery function. */
  DEFAULT_COLLATION(FnDefaultCollation.class, "default-collation()", arg(), STR_O),
  /** XQuery function. */
  DEFAULT_LANGUAGE(FnDefaultLanguage.class, "default-language()", arg(), LAN_O),
  /** XQuery function. */
  DISTINCT_VALUES(FnDistinctValues.class, "distinct-values(items[,collation])",
      arg(AAT_ZM, STR_O), AAT_ZM),
  /** XQuery function. */
  DOC(FnDoc.class, "doc(uri)", arg(STR_ZO), DOC_ZO),
  /** XQuery function. */
  DOC_AVAILABLE(FnDocAvailable.class, "doc-available(uri)", arg(STR_ZO), BLN_O),
  /** XQuery function. */
  DOCUMENT_URI(FnDocumentUri.class, "document-uri([node])", arg(NOD_ZO), URI_ZO),
  /** XQuery function. */
  ELEMENT_WITH_ID(FnElementWithId.class, "element-with-id(string[,node])",
      arg(STR_ZM, NOD_O), ELM_ZM),
  /** XQuery function. */
  EMPTY(FnEmpty.class, "empty(items)", arg(ITEM_ZM), BLN_O),
  /** XQuery function. */
  ENCODE_FOR_URI(FnEncodeForUri.class, "encode-for-uri(string)", arg(STR_ZO), STR_O),
  /** XQuery function. */
  ENDS_WITH(FnEndsWith.class, "ends-with(string,sub[,collation])",
      arg(STR_ZO, STR_ZO, STR_O), BLN_O),
  /** XQuery function. */
  ENVIRONMENT_VARIABLE(FnEnvironmentVariable.class, "environment-variable(string)",
      arg(STR_O), STR_ZO),
  /** XQuery function. */
  ERROR(FnError.class, "error([code[,desc[,object]]])",
      arg(QNM_ZO, STR_O, ITEM_ZM), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  ESCAPE_HTML_URI(FnEscapeHtmlUri.class, "escape-html-uri(string)", arg(STR_ZO), STR_O),
  /** XQuery function. */
  EXACTLY_ONE(FnExactlyOne.class, "exactly-one(items)", arg(ITEM_ZM), ITEM_O),
  /** XQuery function. */
  EXISTS(FnExists.class, "exists(items)", arg(ITEM_ZM), BLN_O),
  /** XQuery function. */
  FALSE(FnFalse.class, "false()", arg(), BLN_O),
  /** XQuery function. */
  FILTER(FnFilter.class, "filter(items,function)",
      arg(ITEM_ZM, FuncType.get(BLN_O, ITEM_O).seqType()), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FLOOR(FnFloor.class, "floor(num)", arg(NUM_ZO), NUM_ZO),
  /** XQuery function. */
  FOLD_LEFT(FnFoldLeft.class, "fold-left(items,zero,function)",
      arg(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM_O).seqType()), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FOLD_RIGHT(FnFoldRight.class, "fold-right(items,zero,function)",
      arg(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O, ITEM_ZM).seqType()), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FOR_EACH(FnForEach.class, "for-each(items,function)",
      arg(ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O).seqType()), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FOR_EACH_PAIR(FnForEachPair.class, "for-each-pair(items1,items2,function)",
      arg(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O, ITEM_O).seqType()), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  FORMAT_DATE(FnFormatDate.class, "format-date(date,picture,[language,calendar,place])",
      arg(DAT_ZO, STR_O, STR_ZO, STR_ZO, STR_ZO), STR_ZO),
  /** XQuery function. */
  FORMAT_DATETIME(FnFormatDateTime.class,
      "format-dateTime(number,picture,[language,calendar,place])",
      arg(DTM_ZO, STR_O, STR_ZO, STR_ZO, STR_ZO), STR_ZO),
  /** XQuery function. */
  FORMAT_INTEGER(FnFormatInteger.class, "format-integer(number,picture[,language])",
      arg(ITR_ZO, STR_O, STR_O), STR_O),
  /** XQuery function. */
  FORMAT_NUMBER(FnFormatNumber.class, "format-number(number,picture[,format])",
      arg(NUM_ZO, STR_O, STR_ZO), STR_O),
  /** XQuery function. */
  FORMAT_TIME(FnFormatTime.class, "format-time(number,picture,[language,calendar,place])",
      arg(TIM_ZO, STR_O, STR_ZO, STR_ZO, STR_ZO), STR_ZO),
  /** XQuery function. */
  FUNCTION_ARITY(FnFunctionArity.class, "function-arity(function)", arg(FUNC_O), ITR_O),
  /** XQuery function. */
  FUNCTION_LOOKUP(FnFunctionLookup.class, "function-lookup(name,arity)",
      arg(QNM_O, ITR_O), FUNC_ZO, flag(Flag.POS, CTX, NDT, HOF)),
  /** XQuery function. */
  FUNCTION_NAME(FnFunctionName.class, "function-name(function)", arg(FUNC_O), QNM_ZO),
  /** XQuery function. */
  GENERATE_ID(FnGenerateId.class, "generate-id([node])", arg(NOD_ZO), STR_O),
  /** XQuery function. */
  HAS_CHILDREN(FnHasChildren.class, "has-children([node])", arg(NOD_ZM), BLN_O),
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
  ID(FnId.class, "id(ids[,node])", arg(STR_ZM, NOD_O), ELM_ZM),
  /** XQuery function. */
  IDREF(FnIdref.class, "idref(ids[,node])", arg(STR_ZM, NOD_O), NOD_ZM),
  /** XQuery function. */
  IMPLICIT_TIMEZONE(FnImplicitTimezone.class, "implicit-timezone()", arg(), DTD_O),
  /** XQuery function. */
  IN_SCOPE_PREFIXES(FnInScopePrefixes.class, "in-scope-prefixes(elem)", arg(ELM_O), STR_ZM),
  /** XQuery function. */
  INDEX_OF(FnIndexOf.class, "index-of(items,item[,collation])", arg(AAT_ZM, AAT_O, STR_O), ITR_ZM),
  /** XQuery function. */
  INNERMOST(FnInnermost.class, "innermost(nodes)", arg(NOD_ZM), NOD_ZM),
  /** XQuery function. */
  INSERT_BEFORE(FnInsertBefore.class, "insert-before(items,pos,insert)",
      arg(ITEM_ZM, ITR_O, ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  IRI_TO_URI(FnIriToUri.class, "iri-to-uri(string)", arg(STR_ZO), STR_O),
  /** XQuery function. */
  JSON_DOC(FnJsonDoc.class, "json-doc(uri[,options])", arg(STR_ZO, MAP_O), DOC_ZO),
  /** XQuery function. */
  JSON_TO_XML(FnJsonToXml.class, "json-to-xml(string[,options])",
      arg(STR_ZO, MAP_O), NOD_ZO, flag(CNS)),
  /** XQuery function. */
  LANG(FnLang.class, "lang(ids[,node])", arg(STR_ZO, NOD_O), BLN_O),
  /** XQuery function. */
  LAST(FnLast.class, "last()", arg(), ITR_O, flag(Flag.POS, CTX)),
  /** XQuery function. */
  LOCAL_NAME(FnLocalName.class, "local-name([node])", arg(NOD_ZO), STR_O),
  /** XQuery function. */
  LOCAL_NAME_FROM_QNAME(FnLocalNameFromQName.class, "local-name-from-QName(qname)",
      arg(QNM_ZO), NCN_ZO),
  /** XQuery function. */
  LOWER_CASE(FnLowerCase.class, "lower-case(string)", arg(STR_ZO), STR_O),
  /** XQuery function. */
  MATCHES(FnMatches.class, "matches(string,pattern[,mod])", arg(STR_ZO, STR_O, STR_O), BLN_O),
  /** XQuery function. */
  MAX(FnMax.class, "max(items[,collation])", arg(AAT_ZM, STR_O), AAT_ZO),
  /** XQuery function. */
  MIN(FnMin.class, "min(items[,collation])", arg(AAT_ZM, STR_O), AAT_ZO),
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
  NAME(FnName.class, "name([node])", arg(NOD_ZO), STR_O),
  /** XQuery function. */
  NAMESPACE_URI(FnNamespaceUri.class, "namespace-uri([node])", arg(NOD_ZO), URI_O),
  /** XQuery function. */
  NAMESPACE_URI_FOR_PREFIX(FnNamespaceUriForPrefix.class, "namespace-uri-for-prefix(pref,elem)",
      arg(STR_ZO, ELM_O), URI_ZO),
  /** XQuery function. */
  NAMESPACE_URI_FROM_QNAME(FnNamespaceUriFromQName.class, "namespace-uri-from-QName(qname)",
      arg(QNM_ZO), URI_ZO),
  /** XQuery function. */
  NILLED(FnNilled.class, "nilled([node])", arg(NOD_ZO), BLN_ZO),
  /** XQuery function. */
  NODE_NAME(FnNodeName.class, "node-name([node])", arg(NOD_ZO), QNM_ZO),
  /** XQuery function. */
  NORMALIZE_SPACE(FnNormalizeSpace.class, "normalize-space([string])", arg(STR_ZO), STR_O),
  /** XQuery function. */
  NORMALIZE_UNICODE(FnNormalizeUnicode.class, "normalize-unicode(string[,form])",
      arg(STR_ZO, STR_O), STR_O),
  /** XQuery function. */
  NOT(FnNot.class, "not(items)", arg(ITEM_ZM), BLN_O),
  /** XQuery function. */
  NUMBER(FnNumber.class, "number([item])", arg(AAT_ZO), DBL_O),
  /** XQuery function. */
  ONE_OR_MORE(FnOneOrMore.class, "one-or-more(items)", arg(ITEM_ZM), ITEM_OM),
  /** XQuery function. */
  OUTERMOST(FnOutermost.class, "outermost(nodes)", arg(NOD_ZM), NOD_ZM),
  /** XQuery function. */
  PARSE_IETF_DATE(FnParseIetfDate.class, "parse-ietf-date(string)", arg(STR_ZO), DTM_ZO),
  /** XQuery function. */
  PARSE_JSON(FnParseJson.class, "parse-json(string[,options])", arg(STR_ZO, MAP_O), ITEM_ZO),
  /** XQuery function. */
  PARSE_XML(FnParseXml.class, "parse-xml(string)", arg(STR_ZO), DOC_ZO, flag(CNS)),
  /** XQuery function. */
  PARSE_XML_FRAGMENT(FnParseXmlFragment.class, "parse-xml-fragment(string)", arg(STR_ZO), DOC_ZO,
      flag(CNS)),
  /** XQuery function. */
  PATH(FnPath.class, "path([node])", arg(NOD_ZO), STR_ZO),
  /** XQuery function. */
  POSITION(FnPosition.class, "position()", arg(), ITR_O, flag(Flag.POS, CTX)),
  /** XQuery function. */
  PREFIX_FROM_QNAME(FnPrefixFromQName.class, "prefix-from-QName(qname)", arg(QNM_ZO), NCN_ZO),
  /** XQuery function. */
  PUT(FnPut.class, "put(node,uri[,params])", arg(NOD_O, STR_ZO, ITEM_ZO), EMP, flag(UPD)),
  /** XQuery function. */
  QNAME(FnQName.class, "QName(uri,name)", arg(STR_ZO, STR_O), QNM_O),
  /** XQuery function. */
  RANDOM_NUMBER_GENERATOR(FnRandomNumberGenerator.class, "random-number-generator([seed])",
      arg(AAT_O), MAP_O),
  /** XQuery function. */
  REMOVE(FnRemove.class, "remove(items,pos)", arg(ITEM_ZM, ITR_O), ITEM_ZM),
  /** XQuery function. */
  REPLACE(FnReplace.class, "replace(string,pattern,replace[,mod])",
      arg(STR_ZO, STR_O, STR_O, STR_O), STR_O),
  /** XQuery function. */
  RESOLVE_QNAME(FnResolveQName.class, "resolve-QName(name,base)", arg(STR_ZO, ELM_O), QNM_ZO),
  /** XQuery function. */
  RESOLVE_URI(FnResolveUri.class, "resolve-uri(name[,elem])", arg(STR_ZO, STR_O), URI_ZO),
  /** XQuery function. */
  REVERSE(FnReverse.class, "reverse(items)", arg(ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  ROOT(FnRoot.class, "root([node])", arg(NOD_ZO), NOD_ZO),
  /** XQuery function. */
  ROUND(FnRound.class, "round(num[,prec])", arg(NUM_ZO, ITR_O), NUM_ZO),
  /** XQuery function. */
  ROUND_HALF_TO_EVEN(FnRoundHalfToEven.class, "round-half-to-even(num[,prec])",
      arg(NUM_ZO, ITR_O), NUM_ZO),
  /** XQuery function. */
  SECONDS_FROM_DATETIME(FnSecondsFromDateTime.class, "seconds-from-dateTime(datetime)",
      arg(DTM_ZO), DEC_ZO),
  /** XQuery function. */
  SECONDS_FROM_DURATION(FnSecondsFromDuration.class, "seconds-from-duration(duration)",
      arg(DUR_ZO), DEC_ZO),
  /** XQuery function. */
  SECONDS_FROM_TIME(FnSecondsFromTime.class, "seconds-from-time(time)", arg(TIM_ZO), DEC_ZO),
  /** XQuery function. */
  SERIALIZE(FnSerialize.class, "serialize(items[,params])", arg(ITEM_ZM, ITEM_ZO), STR_O),
  /** XQuery function. */
  SORT(FnSort.class, "sort(items[,collation[,function]])",
      arg(ITEM_ZM, STR_ZO, FuncType.get(AAT_ZM, ITEM_O).seqType()), ITEM_ZM, flag(HOF)),
  /** XQuery function. */
  STARTS_WITH(FnStartsWith.class, "starts-with(string,sub[,collation])",
      arg(STR_ZO, STR_ZO, STR_O), BLN_O),
  /** XQuery function. */
  STATIC_BASE_URI(FnStaticBaseUri.class, "static-base-uri()", arg(), URI_ZO),
  /** XQuery function. */
  STRING(FnString.class, "string([item])", arg(ITEM_ZO), STR_O),
  /** XQuery function. */
  STRING_JOIN(FnStringJoin.class, "string-join(items[,sep])", arg(ITEM_ZM, STR_O), STR_O),
  /** XQuery function. */
  STRING_LENGTH(FnStringLength.class, "string-length([string])", arg(STR_ZO), ITR_O),
  /** XQuery function. */
  STRING_TO_CODEPOINTS(FnStringToCodepoints.class, "string-to-codepoints(string)",
      arg(STR_ZO), ITR_ZM),
  /** XQuery function. */
  SUBSEQUENCE(FnSubsequence.class, "subsequence(items,first[,len])",
      arg(ITEM_ZM, DBL_O, DBL_O), ITEM_ZM),
  /** XQuery function. */
  SUBSTRING(FnSubstring.class, "substring(string,start[,len])", arg(STR_ZO, DBL_O, DBL_O), STR_O),
  /** XQuery function. */
  SUBSTRING_AFTER(FnSubstringAfter.class, "substring-after(string,sub[,collation])",
      arg(STR_ZO, STR_ZO, STR_O), STR_O),
  /** XQuery function. */
  SUBSTRING_BEFORE(FnSubstringBefore.class, "substring-before(string,sub[,collation])",
      arg(STR_ZO, STR_ZO, STR_O), STR_O),
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
  TOKENIZE(FnTokenize.class, "tokenize(string[,pattern[,mod]])", arg(STR_ZO, STR_O, STR_O), STR_ZM),
  /** XQuery function. */
  TRACE(FnTrace.class, "trace(value[,label])", arg(ITEM_ZM, STR_O), ITEM_ZM, flag(NDT)),
  /** XQuery function. */
  TRANSLATE(FnTranslate.class, "translate(string,map,trans)", arg(STR_ZO, STR_O, STR_O), STR_O),
  /** XQuery function. */
  TRUE(FnTrue.class, "true()", arg(), BLN_O),
  /** XQuery function. */
  UNORDERED(FnUnordered.class, "unordered(items)", arg(ITEM_ZM), ITEM_ZM),
  /** XQuery function. */
  UNPARSED_TEXT(FnUnparsedText.class, "unparsed-text(uri[,encoding])", arg(STR_ZO, STR_O), STR_ZO),
  /** XQuery function. */
  UNPARSED_TEXT_AVAILABLE(FnUnparsedTextAvailable.class, "unparsed-text-available(uri[,encoding])",
      arg(STR_ZO, STR_O), BLN_O),
  /** XQuery function. */
  UNPARSED_TEXT_LINES(FnUnparsedTextLines.class, "unparsed-text-lines(uri[,encoding])",
      arg(STR_ZO, STR_O), STR_ZM),
  /** XQuery function. */
  UPPER_CASE(FnUpperCase.class, "upper-case(string)", arg(STR_ZO), STR_O),
  /** XQuery function. */
  URI_COLLECTION(FnUriCollection.class, "uri-collection([uri])", arg(STR_ZO), URI_ZM),
  /** XQuery function. */
  XML_TO_JSON(FnXmlToJson.class, "xml-to-json(node[,options])", arg(NOD_ZO, MAP_O), STR_ZO),
  /** XQuery function. */
  YEAR_FROM_DATE(FnYearFromDate.class, "year-from-date(date)", arg(DAT_ZO), ITR_ZO),
  /** XQuery function. */
  YEAR_FROM_DATETIME(FnYearFromDateTime.class, "year-from-dateTime(datetime)", arg(DTM_ZO), ITR_ZO),
  /** XQuery function. */
  YEARS_FROM_DURATION(FnYearsFromDuration.class, "years-from-duration(duration)",
      arg(DUR_ZO), ITR_ZO),
  /** XQuery function. */
  ZERO_OR_ONE(FnZeroOrOne.class, "zero-or-one(items)", arg(ITEM_ZM), ITEM_ZO),

  // Map Module

  /** XQuery function. */
  _MAP_CONTAINS(MapContains.class, "contains(map,key)", arg(MAP_O, AAT_O), BLN_O, MAP_URI),
  /** XQuery function. */
  _MAP_ENTRY(MapEntry.class, "entry(key,value)", arg(AAT_O, ITEM_ZM), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_FIND(MapFind.class, "find(input,key)", arg(ITEM_ZM, AAT_O), ARRAY_O, MAP_URI),
  /** XQuery function. */
  _MAP_FOR_EACH(MapForEach.class, "for-each(map,function)",
      arg(MAP_O, FuncType.get(ITEM_ZM, AAT_O, ITEM_ZM).seqType()), ITEM_ZM, flag(HOF), MAP_URI),
  /** XQuery function. */
  _MAP_GET(MapGet.class, "get(map,key)", arg(MAP_O, AAT_O), ITEM_ZM, MAP_URI),
  /** XQuery function. */
  _MAP_KEYS(MapKeys.class, "keys(map)", arg(MAP_O), AAT_ZM, MAP_URI),
  /** XQuery function. */
  _MAP_MERGE(MapMerge.class, "merge(maps[,options])", arg(MAP_ZM, MAP_O), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_PUT(MapPut.class, "put(map,key,value)", arg(MAP_O, AAT_O, ITEM_ZM), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_REMOVE(MapRemove.class, "remove(map,keys)", arg(MAP_O, AAT_ZM), MAP_O, MAP_URI),
  /** XQuery function. */
  _MAP_SIZE(MapSize.class, "size(map)", arg(MAP_O), ITR_O, MAP_URI),

  // Array Module

  /** XQuery function. */
  _ARRAY_APPEND(ArrayAppend.class, "append(array,value)",
      arg(ARRAY_O, ITEM_ZM), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FILTER(ArrayFilter.class, "filter(array,function)",
      arg(ARRAY_O, FuncType.get(BLN_O, ITEM_ZM).seqType()), ARRAY_O, flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FLATTEN(ArrayFlatten.class, "flatten(item()*)", arg(ITEM_ZM), ITEM_ZM, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FOLD_LEFT(ArrayFoldLeft.class, "fold-left(array,zero,function)",
      arg(ARRAY_O, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM_O).seqType()), ITEM_ZM,
      flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FOLD_RIGHT(ArrayFoldRight.class, "fold-right(array,zero,function)",
      arg(ARRAY_O, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O, ITEM_ZM).seqType()), ITEM_ZM,
      flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FOR_EACH(ArrayForEach.class, "for-each(array,function)",
      arg(ARRAY_O, FuncType.get(ITEM_ZM, ITEM_O).seqType()), ARRAY_O, flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_FOR_EACH_PAIR(ArrayForEachPair.class, "for-each-pair(array1,array2,function)",
      arg(ARRAY_O, ARRAY_O, FuncType.get(ITEM_ZM, ITEM_O, ITEM_O).seqType()), ARRAY_O,
      flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_GET(ArrayGet.class, "get(array,pos)", arg(ARRAY_O, ITR_O), ITEM_ZM, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_HEAD(ArrayHead.class, "head(array)", arg(ARRAY_O), ITEM_ZM, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_INSERT_BEFORE(ArrayInsertBefore.class, "insert-before(array,pos,value)",
      arg(ARRAY_O, ITR_O, ITEM_ZO), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_JOIN(ArrayJoin.class, "join(arrays)", arg(ARRAY_ZM), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_PUT(ArrayPut.class, "put(array,pos,value)", arg(ARRAY_O, ITR_O, ITEM_ZM),
      ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_REMOVE(ArrayRemove.class, "remove(array,pos)", arg(ARRAY_O, ITR_ZM), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_REVERSE(ArrayReverse.class, "reverse(array)", arg(ARRAY_O), ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_SIZE(ArraySize.class, "size(array)", arg(ARRAY_O), ITR_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_SORT(ArraySort.class, "sort(array[,collation[,function]])",
      arg(ARRAY_O, STR_ZO, FuncType.get(AAT_ZM, ITEM_O).seqType()), ARRAY_O, flag(HOF), ARRAY_URI),
  /** XQuery function. */
  _ARRAY_SUBARRAY(ArraySubarray.class, "subarray(array,pos[,length])", arg(ARRAY_O, ITR_O, ITR_O),
      ARRAY_O, ARRAY_URI),
  /** XQuery function. */
  _ARRAY_TAIL(ArrayTail.class, "tail(array)", arg(ARRAY_O), ARRAY_O, ARRAY_URI),

  // Math Module

  /** XQuery function. */
  _MATH_ACOS(MathAcos.class, "acos(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_ASIN(MathAsin.class, "asin(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_ATAN(MathAtan.class, "atan(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_ATAN2(MathAtan2.class, "atan2(number1,number2)", arg(DBL_O, DBL_O), DBL_O, MATH_URI),
  /** XQuery function. */
  _MATH_COS(MathCos.class, "cos(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_EXP(MathExp.class, "exp(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_EXP10(MathExp10.class, "exp10(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_LOG(MathLog.class, "log(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_LOG10(MathLog10.class, "log10(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_PI(MathPi.class, "pi()", arg(), DBL_O, MATH_URI),
  /** XQuery function. */
  _MATH_POW(MathPow.class, "pow(number1,number2)", arg(DBL_ZO, NUM_O), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_SIN(MathSin.class, "sin(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_SQRT(MathSqrt.class, "sqrt(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_TAN(MathTan.class, "tan(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),

  // Math Module (custom)

  /** XQuery function. */
  _MATH_COSH(MathCosh.class, "cosh(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_CRC32(MathCrc32.class, "crc32(string)", arg(STR_ZO), HEX_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_E(MathE.class, "e()", arg(), DBL_O, MATH_URI),
  /** XQuery function. */
  _MATH_SINH(MathSinh.class, "sinh(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),
  /** XQuery function. */
  _MATH_TANH(MathTanh.class, "tanh(number)", arg(DBL_ZO), DBL_ZO, MATH_URI),

  // Admin Module

  /** XQuery function. */
  _ADMIN_DELETE_LOGS(AdminDeleteLogs.class, "delete-logs(date)",
      arg(STR_O), EMP, flag(NDT), ADMIN_URI),
  /** XQuery function. */
  _ADMIN_LOGS(AdminLogs.class, "logs([date[,merge]])",
      arg(STR_O, BLN_O), ELM_ZM, flag(NDT), ADMIN_URI),
  /** XQuery function. */
  _ADMIN_SESSIONS(AdminSessions.class, "sessions()", arg(), ELM_ZM, flag(NDT), ADMIN_URI),
  /** XQuery function. */
  _ADMIN_WRITE_LOG(AdminWriteLog.class, "write-log(message[,type])",
      arg(STR_O, STR_O), EMP, flag(NDT), ADMIN_URI),

  // Archive Module

  /** XQuery function. */
  _ARCHIVE_CREATE(ArchiveCreate.class, "create(entries,contents[,options])",
      arg(ITEM_ZM, ITEM_ZM, MAP_ZO), B64_O, flag(NDT), ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_CREATE_FROM(ArchiveCreateFrom.class, "create-from(path[,options[,entries]])",
      arg(STR_O, MAP_ZO, ITEM_ZM), EMP, flag(NDT), ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_DELETE(ArchiveDelete.class, "delete(archive,entries)",
      arg(B64_O, ITEM_ZM), B64_O, flag(NDT), ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_ENTRIES(ArchiveEntries.class, "entries(archive)",
      arg(B64_O), ELM_ZM, flag(NDT), ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_EXTRACT_BINARY(ArchiveExtractBinary.class, "extract-binary(archive[,entries])",
      arg(B64_O, ITEM_ZM), B64_ZM, flag(NDT), ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_EXTRACT_TEXT(ArchiveExtractText.class, "extract-text(archive[,entries[,encoding]])",
      arg(B64_O, ITEM_ZM, STR_O), STR_ZM, flag(NDT), ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_EXTRACT_TO(ArchiveExtractTo.class, "extract-to(path,archive[,entries])",
      arg(STR_O, B64_O, ITEM_ZM), EMP, flag(NDT), ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_OPTIONS(ArchiveOptions.class, "options(archive)",
      arg(B64_O), MAP_O, flag(NDT), ARCHIVE_URI),
  /** XQuery function. */
  _ARCHIVE_UPDATE(ArchiveUpdate.class, "update(archive,entries,contents)",
      arg(B64_O, ITEM_ZM, ITEM_ZM), B64_O, flag(NDT), ARCHIVE_URI),

  // Binary Module

  /** XQuery function. */
  _BIN_AND(BinAnd.class, "and(binary1,binary2)", arg(B64_ZO, B64_ZO), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_BIN(BinBin.class, "bin(string)", arg(STR_ZO), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_DECODE_STRING(BinDecodeString.class, "decode-string(binary[,encoding[,offset[,size]]])",
      arg(B64_ZO, STR_O, ITR_O, ITR_O), STR_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_ENCODE_STRING(BinEncodeString.class, "encode-string(string[,encoding])",
      arg(STR_ZO, STR_O), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_FIND(BinFind.class, "find(binary,offset,search)",
      arg(B64_ZO, ITR_O, B64_ZO), ITR_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_FROM_OCTETS(BinFromOctets.class, "from-octets(integers)", arg(ITR_ZM), B64_O, BIN_URI),
  /** XQuery function. */
  _BIN_HEX(BinHex.class, "hex(string)", arg(STR_ZO), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_INSERT_BEFORE(BinInsertBefore.class, "insert-before(binary,offset,extra)",
      arg(B64_ZO, ITR_O, B64_ZO), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_JOIN(BinJoin.class, "join(binaries)", arg(B64_ZM), B64_O, BIN_URI),
  /** XQuery function. */
  _BIN_LENGTH(BinLength.class, "length(binary)", arg(B64_O), ITR_O, BIN_URI),
  /** XQuery function. */
  _BIN_NOT(BinNot.class, "not(binary)", arg(B64_ZO), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_OCTAL(BinOctal.class, "octal(string)", arg(STR_ZO), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_OR(BinOr.class, "or(binary1,binary2)", arg(B64_ZO, B64_ZO), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_PACK_DOUBLE(BinPackDouble.class, "pack-double(double[,order])",
      arg(DBL_O, STR_O), B64_O, BIN_URI),
  /** XQuery function. */
  _BIN_PACK_FLOAT(BinPackFloat.class, "pack-float(float[,order])",
      arg(FLT_O, STR_O), B64_O, BIN_URI),
  /** XQuery function. */
  _BIN_PACK_INTEGER(BinPackInteger.class, "pack-integer(integer,size[,order])",
      arg(ITR_O, ITR_O, STR_O), B64_O, BIN_URI),
  /** XQuery function. */
  _BIN_PAD_LEFT(BinPadLeft.class, "pad-left(binary,size[,octet])",
      arg(B64_ZO, ITR_O, ITR_O), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_PAD_RIGHT(BinPadRight.class, "pad-right(binary,size[,octet])",
      arg(B64_ZO, ITR_O, ITR_O), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_PART(BinPart.class, "part(binary,offset[,size])",
      arg(B64_ZO, ITR_O, ITR_O), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_SHIFT(BinShift.class, "shift(binary,by)", arg(B64_ZO, ITR_O), B64_ZO, BIN_URI),
  /** XQuery function. */
  _BIN_TO_OCTETS(BinToOctets.class, "to-octets(binary)", arg(B64_ZO), ITR_ZM, BIN_URI),
  /** XQuery function. */
  _BIN_UNPACK_DOUBLE(BinUnpackDouble.class, "unpack-double(binary,offset[,order])",
      arg(B64_O, ITR_O, STR_O), DBL_O, BIN_URI),
  /** XQuery function. */
  _BIN_UNPACK_FLOAT(BinUnpackFloat.class, "unpack-float(binary,offset[,order])",
      arg(B64_O, ITR_O, STR_O), FLT_O, BIN_URI),
  /** XQuery function. */
  _BIN_UNPACK_INTEGER(BinUnpackInteger.class, "unpack-integer(binary,offset,size[,order])",
      arg(B64_O, ITR_O, ITR_O, STR_O), ITR_O, BIN_URI),
  /** XQuery function. */
  _BIN_UNPACK_UNSIGNED_INTEGER(BinUnpackUnsignedInteger.class,
      "unpack-unsigned-integer(binary,offset,size[,order])",
      arg(B64_O, ITR_O, ITR_O, STR_O), ITR_O, BIN_URI),
  /** XQuery function. */
  _BIN_XOR(BinXor.class, "xor(binary1,binary2)", arg(B64_ZO, B64_ZO), B64_ZO, BIN_URI),

  // Client Module

  /** XQuery function. */
  _CLIENT_CLOSE(ClientClose.class, "close(id)", arg(URI_O), EMP, flag(NDT), CLIENT_URI),
  /** XQuery function. */
  _CLIENT_CONNECT(ClientConnect.class, "connect(url,port,user,password)",
      arg(STR_O, ITR_O, STR_O, STR_O), URI_O, flag(NDT), CLIENT_URI),
  /** XQuery function. */
  _CLIENT_EXECUTE(ClientExecute.class, "execute(id,command)", arg(URI_O, STR_O), STR_O, flag(NDT),
      CLIENT_URI),
  /** XQuery function. */
  _CLIENT_INFO(ClientInfo.class, "info(id)", arg(URI_O), STR_O, flag(NDT), CLIENT_URI),
  /** XQuery function. */
  _CLIENT_QUERY(ClientQuery.class, "query(id,query[,bindings])",
      arg(URI_O, STR_O, MAP_ZO), ITEM_ZO, flag(NDT), CLIENT_URI),

  // Conversion Module

  /** XQuery function. */
  _CONVERT_BINARY_TO_BYTES(ConvertBinaryToBytes.class, "binary-to-bytes(binary)",
      arg(AAT_O), BYT_ZM, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_BINARY_TO_INTEGERS(ConvertBinaryToIntegers.class, "binary-to-integers(binary)",
      arg(AAT_O), ITR_ZM, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_BINARY_TO_STRING(ConvertBinaryToString.class,
      "binary-to-string(binary[,encoding[,fallback]])",
      arg(ITEM_O, STR_O, BLN_O), STR_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGER_FROM_BASE(ConvertIntegerFromBase.class, "integer-from-base(string,base)",
      arg(STR_O, ITR_O), ITR_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGER_TO_BASE(ConvertIntegerToBase.class, "integer-to-base(number,base)",
      arg(ITR_O, ITR_O), STR_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGER_TO_DATETIME(ConvertIntegerToDateTime.class, "integer-to-dateTime(ms)",
      arg(ITR_O), DTM_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGER_TO_DAYTIME(ConvertIntegerToDayTime.class, "integer-to-dayTime(ms)",
      arg(ITR_O), DTD_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_DATETIME_TO_INTEGER(ConvertDateTimeToInteger.class, "dateTime-to-integer(date)",
      arg(DTM_O), ITR_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_DAYTIME_TO_INTEGER(ConvertDayTimeToInteger.class, "dayTime-to-integer(duration)",
      arg(DTD_O), ITR_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGERS_TO_BASE64(ConvertIntegersToBase64.class, "integers-to-base64(numbers)",
      arg(ITR_ZM), B64_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_INTEGERS_TO_HEX(ConvertIntegersToHex.class, "integers-to-hex(numbers)",
      arg(ITR_ZM), HEX_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_STRING_TO_BASE64(ConvertStringToBase64.class, "string-to-base64(string[,encoding])",
      arg(STR_O, STR_O), B64_O, CONVERT_URI),
  /** XQuery function. */
  _CONVERT_STRING_TO_HEX(ConvertStringToHex.class, "string-to-hex(string[,encoding])",
      arg(STR_O, STR_O), HEX_O, CONVERT_URI),

  // Cryptographic Module

  /** XQuery function. */
  _CRYPTO_DECRYPT(CryptoDecrypt.class, "decrypt(input,type,key,algorithm)",
      arg(STR_O, STR_O, STR_O, STR_O), STR_O, CRYPTO_URI),
  /** XQuery function. */
  _CRYPTO_ENCRYPT(CryptoEncrypt.class, "encrypt(input,encryption,key,algorithm)",
      arg(STR_O, STR_O, STR_O, STR_O), STR_O, CRYPTO_URI),
  /** XQuery function. */
  _CRYPTO_HMAC(CryptoHmac.class, "hmac(message,key,algorithm[,encoding])",
      arg(STR_O, STR_O, STR_O, STR_ZO), STR_O, CRYPTO_URI),
  /** XQuery function. */
  _CRYPTO_GENERATE_SIGNATURE(CryptoGenerateSignature.class, "generate-signature" +
      "(input,canonicalization,digest,signature,prefix,type[,item1][,item2])",
      arg(NOD_O, STR_O, STR_O, STR_O, STR_O, STR_O, ITEM_ZO, ITEM_ZO), NOD_O, CRYPTO_URI),
  /** XQuery function. */
  _CRYPTO_VALIDATE_SIGNATURE(CryptoValidateSignature.class, "validate-signature(node)",
      arg(NOD_O), BLN_O, CRYPTO_URI),

  // CSV Module

  /** XQuery function. */
  _CSV_PARSE(CsvParse.class, "parse(string[,options])", arg(STR_ZO, MAP_ZO), ITEM_ZO, CSV_URI),
  /** XQuery function. */
  _CSV_SERIALIZE(CsvSerialize.class, "serialize(item[,options])", arg(ITEM_ZO, ITEM_ZO), STR_O,
      CSV_URI),

  // Database Module

  /** XQuery function. */
  _DB_ADD(DbAdd.class, "add(database,input[,path[,options]])",
      arg(STR_O, ITEM_O, STR_O, MAP_ZO), EMP, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_ALTER(DbAlter.class, "alter(database, new-name)", arg(STR_O, STR_O), EMP, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_ATTRIBUTE(DbAttribute.class, "attribute(database,strings[,name])",
      arg(STR_O, ITEM_ZM, STR_O), ATT_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_ATTRIBUTE_RANGE(DbAttributeRange.class, "attribute-range(database,from,to[,name])",
      arg(STR_O, ITEM_O, ITEM_O, STR_O), ATT_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_BACKUPS(DbBackups.class, "backups([database])", arg(ITEM_O), ELM_ZM, DB_URI),
  /** XQuery function. */
  _DB_CONTENT_TYPE(DbContentType.class, "content-type(database,path)",
      arg(STR_O, STR_O), STR_O, DB_URI),
  /** XQuery function. */
  _DB_COPY(DbCopy.class, "copy(database, new-name)", arg(STR_O, STR_O), EMP, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_CREATE(DbCreate.class, "create(name[,inputs[,paths[,options]]])",
      arg(STR_O, ITEM_ZM, STR_ZM, MAP_ZO), EMP, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_CREATE_BACKUP(DbCreateBackup.class, "create-backup(database)",
      arg(STR_O), EMP, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_DELETE(DbDelete.class, "delete(database,path)", arg(STR_O, STR_O), EMP, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_DROP(DbDrop.class, "drop(database)", arg(ITEM_O), EMP, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_DROP_BACKUP(DbDropBackup.class, "drop-backup(name)", arg(STR_O), EMP, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_EXISTS(DbExists.class, "exists(database[,path])",
      arg(STR_O, STR_O), BLN_O, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_EXPORT(DbExport.class, "export(database,path[,param]])",
      arg(STR_O, STR_O, ITEM_O), EMP, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_FLUSH(DbFlush.class, "flush(database)", arg(ITEM_O), EMP, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_INFO(DbInfo.class, "info(database)", arg(STR_O), STR_O, DB_URI),
  /** XQuery function. */
  _DB_IS_RAW(DbIsRaw.class, "is-raw(database,path)", arg(STR_O, STR_O), BLN_O, DB_URI),
  /** XQuery function. */
  _DB_IS_XML(DbIsXml.class, "is-xml(database,path)", arg(STR_O, STR_O), BLN_O, DB_URI),
  /** XQuery function. */
  _DB_LIST(DbList.class, "list([database[,path]])", arg(STR_O, STR_O), STR_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_LIST_DETAILS(DbListDetails.class, "list-details([database[,path]])",
      arg(STR_O, STR_O), ELM_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_NAME(DbName.class, "name(node)", arg(NOD_O), STR_O, DB_URI),
  /** XQuery function. */
  _DB_NODE_ID(DbNodeId.class, "node-id(nodes)", arg(NOD_ZM), ITR_ZM, DB_URI),
  /** XQuery function. */
  _DB_NODE_PRE(DbNodePre.class, "node-pre(nodes)", arg(NOD_ZM), ITR_ZM, DB_URI),
  /** XQuery function. */
  _DB_OPEN(DbOpen.class, "open(database[,path])", arg(STR_O, STR_O), DOC_ZM, DB_URI),
  /** XQuery function. */
  _DB_OPEN_ID(DbOpenId.class, "open-id(database,id)", arg(STR_O, ITR_O), NOD_ZM, DB_URI),
  /** XQuery function. */
  _DB_OPEN_PRE(DbOpenPre.class, "open-pre(database,pre)", arg(STR_O, ITR_O), NOD_ZM, DB_URI),
  /** XQuery function. */
  _DB_OPTIMIZE(DbOptimize.class, "optimize(database[,all[,options]])",
      arg(STR_O, BLN_O, MAP_ZO), EMP, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_OPTION(DbOption.class, "option(name)", arg(STR_ZO), ITEM_O, DB_URI),
  /** XQuery function. */
  _DB_PATH(DbPath.class, "path(node)", arg(NOD_O), STR_O, DB_URI),
  /** XQuery function. */
  _DB_PROPERTY(DbProperty.class, "property(database,name)", arg(STR_O, STR_O), AAT_O, DB_URI),
  /** XQuery function. */
  _DB_RENAME(DbRename.class, "rename(database,path,new-path)",
      arg(STR_O, STR_O, STR_O), EMP, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_REPLACE(DbReplace.class, "replace(database,path,input[,options])",
      arg(STR_O, STR_O, ITEM_O, MAP_ZO), EMP, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_RESTORE(DbRestore.class, "restore(backup)", arg(STR_O), EMP, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_RETRIEVE(DbRetrieve.class, "retrieve(database,path)",
      arg(STR_O, STR_O), B64_O, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_STORE(DbStore.class, "store(database,path,input)",
      arg(STR_O, STR_O, ITEM_O), EMP, flag(UPD), DB_URI),
  /** XQuery function. */
  _DB_SYSTEM(DbSystem.class, "system()", arg(), STR_O, DB_URI),
  /** XQuery function. */
  _DB_TEXT(DbText.class, "text(database,strings)", arg(STR_O, ITEM_ZM), TXT_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_TEXT_RANGE(DbTextRange.class, "text-range(database,from,to)",
      arg(STR_O, ITEM_O, ITEM_O), TXT_ZM, flag(NDT), DB_URI),
  /** XQuery function. */
  _DB_TOKEN(DbToken.class, "token(database,strings[,name])",
      arg(STR_O, ITEM_ZM, STR_O), ATT_ZM, flag(NDT), DB_URI),

  // Fetch Module

  /** XQuery function. */
  _FETCH_BINARY(FetchBinary.class, "binary(uri)", arg(STR_O), B64_O, flag(NDT), FETCH_URI),
  /** XQuery function. */
  _FETCH_CONTENT_TYPE(FetchContentType.class, "content-type(uri)", arg(STR_O), STR_O, flag(NDT),
      FETCH_URI),
  /** XQuery function. */
  _FETCH_TEXT(FetchText.class, "text(uri[,encoding[,fallback]])",
      arg(STR_O, STR_O, BLN_O), STR_O, flag(NDT), FETCH_URI),
  /** XQuery function. */
  _FETCH_XML(FetchXml.class, "xml(uri[,options])", arg(STR_O, MAP_ZO), DOC_O, flag(NDT), FETCH_URI),
  /** XQuery function. */
  _FETCH_XML_BINARY(FetchXmlBinary.class, "xml-binary(binary[,options])",
      arg(B64_O, MAP_ZO), DOC_O, flag(NDT), FETCH_URI),

  // File Module

  /** XQuery function. */
  _FILE_APPEND(FileAppend.class, "append(path,data[,params])",
      arg(STR_O, ITEM_ZM, ITEM_O), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_APPEND_BINARY(FileAppendBinary.class, "append-binary(path,item)",
      arg(STR_O, BIN_O), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_APPEND_TEXT(FileAppendText.class, "append-text(path,text[,encoding])",
      arg(STR_O, STR_O, STR_O), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_APPEND_TEXT_LINES(FileAppendTextLines.class, "append-text-lines(path,texts[,encoding])",
      arg(STR_O, STR_ZM, STR_O), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_BASE_DIR(FileBaseDir.class, "base-dir()", arg(), STR_O, FILE_URI),
  /** XQuery function. */
  _FILE_CHILDREN(FileChildren.class, "children(path)", arg(STR_O), STR_ZM, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_COPY(FileCopy.class, "copy(source,target)", arg(STR_O, STR_O), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_CREATE_DIR(FileCreateDir.class, "create-dir(path)", arg(STR_O), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_CREATE_TEMP_DIR(FileCreateTempDir.class, "create-temp-dir(prefix,suffix[,dir])",
      arg(STR_O, STR_O, STR_O), STR_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_CREATE_TEMP_FILE(FileCreateTempFile.class, "create-temp-file(prefix,suffix[,dir])",
      arg(STR_O, STR_O, STR_O), STR_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_CURRENT_DIR(FileCurrentDir.class, "current-dir()", arg(), STR_O, FILE_URI),
  /** XQuery function. */
  _FILE_DELETE(FileDelete.class, "delete(path[,recursive])",
      arg(STR_O, BLN_O), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_DIR_SEPARATOR(FileDirSeparator.class, "dir-separator()", arg(), STR_O, FILE_URI),
  /** XQuery function. */
  _FILE_EXISTS(FileExists.class, "exists(path)", arg(STR_O), BLN_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_IS_ABSOLUTE(FileIsAbsolute.class, "is-absolute(path)",
      arg(STR_O), BLN_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_IS_DIR(FileIsDir.class, "is-dir(path)", arg(STR_O), BLN_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_IS_FILE(FileIsFile.class, "is-file(path)", arg(STR_O), BLN_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_LAST_MODIFIED(FileLastModified.class, "last-modified(path)",
      arg(STR_O), DTM_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_LINE_SEPARATOR(FileLineSeparator.class, "line-separator()", arg(), STR_O, FILE_URI),
  /** XQuery function. */
  _FILE_LIST(FileList.class, "list(path[,recursive[,pattern]])",
      arg(STR_O, BLN_O, STR_O), STR_ZM, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_MOVE(FileMove.class, "move(source,target)", arg(STR_O, STR_O), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_NAME(FileName.class, "name(path)", arg(STR_O), STR_O, FILE_URI),
  /** XQuery function. */
  _FILE_PARENT(FileParent.class, "parent(path)", arg(STR_O), STR_ZO, FILE_URI),
  /** XQuery function. */
  _FILE_PATH_SEPARATOR(FilePathSeparator.class, "path-separator()", arg(), STR_O, FILE_URI),
  /** XQuery function. */
  _FILE_PATH_TO_NATIVE(FilePathToNative.class, "path-to-native(path)",
      arg(STR_O), STR_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_PATH_TO_URI(FilePathToUri.class, "path-to-uri(path)", arg(STR_O), URI_O, FILE_URI),
  /** XQuery function. */
  _FILE_READ_BINARY(FileReadBinary.class, "read-binary(path[,offset[,length]])",
      arg(STR_O, ITR_O, ITR_O), B64_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_READ_TEXT(FileReadText.class, "read-text(path[,encoding[,fallback]])",
      arg(STR_O, STR_O, BLN_O), STR_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_READ_TEXT_LINES(FileReadTextLines.class,
      "read-text-lines(path[,encoding[,fallback[,offset[,length]]]])",
      arg(STR_O, STR_O, BLN_O, ITR_O, ITR_O), STR_ZM, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_RESOLVE_PATH(FileResolvePath.class, "resolve-path(path[,base])",
      arg(STR_O, STR_O), STR_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_SIZE(FileSize.class, "size(path)", arg(STR_O), ITR_O, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_TEMP_DIR(FileTempDir.class, "temp-dir()", arg(), STR_O, FILE_URI),
  /** XQuery function. */
  _FILE_WRITE(FileWrite.class, "write(path,data[,params])",
      arg(STR_O, ITEM_ZM, ITEM_O), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_WRITE_BINARY(FileWriteBinary.class, "write-binary(path,item[,offset])",
      arg(STR_O, BIN_O, ITR_O), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_WRITE_TEXT(FileWriteText.class, "write-text(path,text[,encoding])",
      arg(STR_O, STR_O, STR_O), EMP, flag(NDT), FILE_URI),
  /** XQuery function. */
  _FILE_WRITE_TEXT_LINES(FileWriteTextLines.class, "write-text-lines(path,texts[,encoding])",
      arg(STR_O, STR_ZM, STR_O), EMP, flag(NDT), FILE_URI),

  // Fulltext Module

  /** XQuery function. */
  _FT_CONTAINS(FtContains.class, "contains(input,terms[,options])",
      arg(ITEM_O, ITEM_ZM, MAP_ZO), BLN_O, flag(NDT), FT_URI),
  /** XQuery function. */
  _FT_COUNT(FtCount.class, "count(nodes)", arg(NOD_ZM), ITR_O, FT_URI),
  /** XQuery function. */
  _FT_EXTRACT(FtExtract.class, "extract(nodes[,name[,length]])",
      arg(ITEM_ZM, STR_O, ITR_O), NOD_ZM, FT_URI),
  /** XQuery function. */
  _FT_MARK(FtMark.class, "mark(nodes[,name])", arg(NOD_ZM, STR_O), NOD_ZM, FT_URI),
  /** XQuery function. */
  _FT_NORMALIZE(FtNormalize.class, "normalize(string[,options])",
      arg(STR_ZO, MAP_ZO), STR_O, FT_URI),
  /** XQuery function. */
  _FT_SCORE(FtScore.class, "score(items)", arg(ITEM_ZM), DBL_ZM, FT_URI),
  /** XQuery function. */
  _FT_SEARCH(FtSearch.class, "search(database,terms[,options])",
      arg(STR_O, ITEM_ZM, MAP_ZO), TXT_ZM, flag(NDT), FT_URI),
  /** XQuery function. */
  _FT_TOKENIZE(FtTokenize.class, "tokenize(string[,options])", arg(STR_ZO, MAP_ZO), STR_ZM, FT_URI),
  /** XQuery function. */
  _FT_TOKENS(FtTokens.class, "tokens(database[,prefix])",
      arg(STR_O, STR_O), ELM_ZM, flag(NDT), FT_URI),

  // Hash Module

  /** XQuery function. */
  _HASH_HASH(HashHash.class, "hash(value,algorithm)", arg(AAT_O, STR_O), B64_O, HASH_URI),
  /** XQuery function. */
  _HASH_MD5(HashMd5.class, "md5(value)", arg(AAT_O), B64_O, HASH_URI),
  /** XQuery function. */
  _HASH_SHA1(HashSha1.class, "sha1(value)", arg(AAT_O), B64_O, HASH_URI),
  /** XQuery function. */
  _HASH_SHA256(HashSha256.class, "sha256(value)", arg(AAT_O), B64_O, HASH_URI),

  // HOF Module

  /** XQuery function. */
  _HOF_CONST(HofConst.class, "const(return,ignore)",
      arg(ITEM_ZM, ITEM_ZM), ITEM_ZM, HOF_URI),
  /** XQuery function. */
  _HOF_FOLD_LEFT1(HofFoldLeft1.class, "fold-left1(non-empty-items,function)",
      arg(ITEM_OM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM_O).seqType()), ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_ID(HofId.class, "id(value)", arg(ITEM_ZM), ITEM_ZM, HOF_URI),
  /** XQuery function. */
  _HOF_SCAN_LEFT(HofScanLeft.class, "scan-left(items,zero,function)",
      arg(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_ZM, ITEM_O).seqType()), ITEM_ZM, flag(HOF),
      HOF_URI),
  /** XQuery function. */
  _HOF_SORT_WITH(HofSortWith.class, "sort-with(items,lt-fun)",
      arg(ITEM_ZM, FuncType.get(BLN_O, ITEM_O, ITEM_O).seqType()), ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_TAKE_WHILE(HofTakeWhile.class, "take-while(items,pred)",
      arg(ITEM_ZM, FuncType.get(BLN_O, ITEM_O).seqType()), ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_TOP_K_BY(HofTopKBy.class, "top-k-by(items,key-func,k)",
      arg(ITEM_ZM, FuncType.get(ITEM_O, ITEM_O).seqType(), ITR_O), ITEM_ZM, flag(HOF), HOF_URI),
  /** XQuery function. */
  _HOF_TOP_K_WITH(HofTopKWith.class, "top-k-with(items,less-than-func,k)",
      arg(ITEM_ZM, FuncType.get(BLN_O, ITEM_ZO, ITEM_ZO).seqType(), ITR_O), ITEM_ZM, flag(HOF),
      HOF_URI),
  /** XQuery function. */
  _HOF_UNTIL(HofUntil.class, "until(pred,function,start)",
      arg(FuncType.get(BLN_O, ITEM_ZM).seqType(),
      FuncType.get(ITEM_ZM, ITEM_ZM).seqType(), ITEM_ZM), ITEM_ZM, flag(HOF), HOF_URI),

  // HTML Module

  /** XQuery function. */
  _HTML_PARSE(HtmlParse.class, "parse(string[,options])", arg(STR_ZO, MAP_ZO), DOC_ZO, HTML_URI),
  /** XQuery function. */
  _HTML_PARSER(HtmlParser.class, "parser()", arg(), STR_O, HTML_URI),

  // HTTP Module

  /** XQuery function. */
  _HTTP_SEND_REQUEST(HttpSendRequest.class, "send-request(request[,href,[bodies]])",
      arg(NOD_O, STR_ZO, ITEM_ZM), ITEM_ZM, flag(NDT), HTTP_URI),

  // Index Module

  /** XQuery function. */
  _INDEX_ATTRIBUTE_NAMES(IndexAttributeNames.class, "attribute-names(database)",
      arg(STR_O), ELM_ZM, INDEX_URI),
  /** XQuery function. */
  _INDEX_ATTRIBUTES(IndexAttributes.class, "attributes(database[,prefix[,ascending]])",
      arg(STR_O, STR_O, BLN_O), ELM_ZM, flag(NDT), INDEX_URI),
  /** XQuery function. */
  _INDEX_ELEMENT_NAMES(IndexElementNames.class, "element-names(database)",
      arg(STR_O), ELM_ZM, INDEX_URI),
  /** XQuery function. */
  _INDEX_FACETS(IndexFacets.class, "facets(database[,type])",
      arg(STR_O, STR_O), DOC_O, flag(NDT), INDEX_URI),
  /** XQuery function. */
  _INDEX_TEXTS(IndexTexts.class, "texts(database[,prefix[,ascending]])",
      arg(STR_O, STR_O, BLN_O), ELM_ZM, flag(NDT), INDEX_URI),
  /** XQuery function. */
  _INDEX_TOKENS(IndexTokens.class, "tokens(database)", arg(STR_O), ELM_ZM, flag(NDT), INDEX_URI),

  // Inspection Module

  /** XQuery function. */
  _INSPECT_CONTEXT(InspectContext.class, "context()", arg(), ELM_O, INSPECT_URI),
  /** XQuery function. */
  _INSPECT_FUNCTION(InspectFunction.class, "function(function)",
      arg(STR_O), ELM_O, flag(HOF), INSPECT_URI),
  /** XQuery function. */
  _INSPECT_FUNCTION_ANNOTATIONS(InspectFunctionAnnotations.class, "function-annotations(function)",
      arg(FUNC_O), MAP_ZO, INSPECT_URI),
  /** XQuery function. */
  _INSPECT_FUNCTIONS(InspectFunctions.class, "functions([uri])",
      arg(STR_O), FUNC_ZM, flag(HOF), INSPECT_URI),
  /** XQuery function. */
  _INSPECT_MODULE(InspectModule.class, "module(uri)", arg(STR_O), ELM_O, INSPECT_URI),
  /** XQuery function. */
  _INSPECT_STATIC_CONTEXT(InspectStaticContext.class, "static-context(function,name)",
      arg(FUNC_O, STR_O), ITEM_ZM, INSPECT_URI),
  /** XQuery function. */
  _INSPECT_XQDOC(InspectXqdoc.class, "xqdoc(uri)", arg(STR_O), ELM_O, INSPECT_URI),

  // Jobs Module

  /** XQuery function. */
  _JOBS_CURRENT(JobsCurrent.class, "current()", arg(), STR_O, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_EVAL(JobsEval.class, "eval(string[,bindings[,options]])",
      arg(STR_O, MAP_ZO, MAP_ZO), STR_O, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_FINISHED(JobsFinished.class, "finished(id)", arg(STR_O), BLN_O, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_INVOKE(JobsInvoke.class, "invoke(uri[,bindings[,options]])",
      arg(STR_O, MAP_ZO, MAP_ZO), STR_O, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_LIST(JobsList.class, "list()", arg(), STR_ZM, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_LIST_DETAILS(JobsListDetails.class, "list-details([id])",
      arg(STR_O), ELM_ZM, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_RESULT(JobsResult.class, "result(id)", arg(STR_O), ITEM_ZM, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_SERVICES(JobsServices.class, "services()", arg(), ELM_ZM, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_STOP(JobsStop.class, "stop(id[,options])", arg(STR_O, MAP_ZO), EMP, flag(NDT), JOBS_URI),
  /** XQuery function. */
  _JOBS_WAIT(JobsWait.class, "wait(id)", arg(STR_O), EMP, flag(NDT), JOBS_URI),

  // JSON Module

  /** XQuery function. */
  _JSON_PARSE(JsonParse.class, "parse(string[,options])", arg(STR_ZO, MAP_ZO), ITEM_ZO, JSON_URI),
  /** XQuery function. */
  _JSON_SERIALIZE(JsonSerialize.class, "serialize(items[,options])",
      arg(ITEM_ZO, MAP_ZO), STR_O, JSON_URI),

  // Lazy Module

  /** XQuery function. */
  _LAZY_CACHE(LazyCache.class, "cache(value)", arg(ITEM_ZM), ITEM_ZM, LAZY_URI),
  /** XQuery function. */
  _LAZY_IS_CACHED(LazyIsCached.class, "is-cached(item)", arg(ITEM_O), BLN_O, LAZY_URI),
  /** XQuery function. */
  _LAZY_IS_LAZY(LazyIsLazy.class, "is-lazy(item)", arg(ITEM_O), BLN_O, LAZY_URI),

  // Output Module

  /** XQuery function. */
  _OUT_CR(OutCr.class, "cr()", arg(), STR_O, OUT_URI),
  /** XQuery function. */
  _OUT_FORMAT(OutFormat.class, "format(format,item1[,...])", arg(STR_O, ITEM_O), STR_O, OUT_URI),
  /** XQuery function. */
  _OUT_NL(OutNl.class, "nl()", arg(), STR_O, OUT_URI),
  /** XQuery function. */
  _OUT_TAB(OutTab.class, "tab()", arg(), STR_O, OUT_URI),

  // Process Module

  /** XQuery function. */
  _PROC_EXECUTE(ProcExecute.class, "execute(command[,args[,options]])",
      arg(STR_O, STR_ZM, AAT_O), ELM_O, flag(NDT), PROC_URI),
  /** XQuery function. */
  _PROC_FORK(ProcFork.class, "fork(command[,args[,options]])",
      arg(STR_O, STR_ZM, AAT_O), EMP, flag(NDT), PROC_URI),
  /** XQuery function. */
  _PROC_PROPERTY(ProcProperty.class, "property(name)", arg(STR_O), STR_ZO, flag(NDT), PROC_URI),
  /** XQuery function. */
  _PROC_PROPERTY_NAMES(ProcPropertyNames.class, "property-names()",
      arg(), STR_ZM, flag(NDT), PROC_URI),
  /** XQuery function. */
  _PROC_SYSTEM(ProcSystem.class, "system(command[,args[,options]])",
      arg(STR_O, STR_ZM, AAT_O), STR_O, flag(NDT), PROC_URI),

  // Profiling Module

  /** XQuery function. */
  _PROF_CURRENT_MS(ProfCurrentMs.class, "current-ms()", arg(), ITR_O, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_CURRENT_NS(ProfCurrentNs.class, "current-ns()", arg(), ITR_O, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_DUMP(ProfDump.class, "dump(value[,label])", arg(ITEM_ZM, STR_O), EMP, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_HUMAN(ProfHuman.class, "human(integer)", arg(ITR_O), STR_O, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_MEMORY(ProfMemory.class, "memory(value[,label])",
      arg(ITEM_ZM, STR_O), ITEM_ZM, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_SLEEP(ProfSleep.class, "sleep(ms)", arg(ITR_O), EMP, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_TIME(ProfTime.class, "time(value[,label])",
      arg(ITEM_ZM, STR_O), ITEM_ZM, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_TRACK(ProfTrack.class, "track(value[,options])",
      arg(ITEM_ZM, MAP_ZO), ITEM_ZM, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_TYPE(ProfType.class, "type(value)", arg(ITEM_ZM), ITEM_ZM, PROF_URI),
  /** XQuery function. */
  _PROF_VARIABLES(ProfVariables.class, "variables()", arg(), EMP, flag(NDT), PROF_URI),
  /** XQuery function. */
  _PROF_VOID(ProfVoid.class, "void(value)", arg(ITEM_ZM), EMP, flag(NDT), PROF_URI),

  // Random Module

  /** XQuery function. */
  _RANDOM_DOUBLE(RandomDouble.class, "double()", arg(), DBL_O, flag(NDT), RANDOM_URI),
  /** XQuery function. */
  _RANDOM_GAUSSIAN(RandomGaussian.class, "gaussian(num)",
      arg(ITR_O), DBL_ZM, flag(NDT), RANDOM_URI),
  /** XQuery function. */
  _RANDOM_INTEGER(RandomInteger.class, "integer([max])", arg(ITR_O), ITR_O, flag(NDT), RANDOM_URI),
  /** XQuery function. */
  _RANDOM_SEEDED_DOUBLE(RandomSeededDouble.class, "seeded-double(seed,num)",
      arg(ITR_O, ITR_O), DBL_ZM, RANDOM_URI),
  /** XQuery function. */
  _RANDOM_SEEDED_INTEGER(RandomSeededInteger.class, "seeded-integer(seed,num[,max])",
      arg(ITR_O, ITR_O, ITR_O), ITR_ZM, RANDOM_URI),
  /** XQuery function. */
  _RANDOM_SEEDED_PERMUTATION(RandomSeededPermutation.class, "seeded-permutation(seed,items)",
      arg(ITR_O, ITEM_ZM), ITEM_ZM, RANDOM_URI),
  /** XQuery function. */
  _RANDOM_UUID(RandomUuid.class, "uuid()", arg(), STR_O, flag(NDT), RANDOM_URI),

  // Repository Module

  /** XQuery function. */
  _REPO_DELETE(RepoDelete.class, "delete(uri)", arg(STR_O), EMP, flag(NDT), REPO_URI),
  /** XQuery function. */
  _REPO_INSTALL(RepoInstall.class, "install(uri)", arg(STR_O), EMP, flag(NDT), REPO_URI),
  /** XQuery function. */
  _REPO_LIST(RepoList.class, "list()", arg(), STR_ZM, flag(NDT), REPO_URI),

  // SQL Module

  /** XQuery function. */
  _SQL_CLOSE(SqlClose.class, "close(id)", arg(ITR_O), EMP, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_COMMIT(SqlCommit.class, "commit(id)", arg(ITR_O), EMP, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_CONNECT(SqlConnect.class, "connect(url[,user[,pass[,options]]]]])",
      arg(STR_O, STR_O, STR_O, MAP_ZO), ITR_O, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_EXECUTE(SqlExecute.class, "execute(id,query[,options])",
      arg(ITR_O, STR_O, MAP_ZO), ITEM_ZM, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_EXECUTE_PREPARED(SqlExecutePrepared.class, "execute-prepared(id[,params[,options]])",
      arg(ITR_O, ELM_O, MAP_ZO), ITEM_ZM, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_INIT(SqlInit.class, "init(class)", arg(STR_O), EMP, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_PREPARE(SqlPrepare.class, "prepare(id,statement)",
      arg(ITR_O, STR_O), ITR_O, flag(NDT), SQL_URI),
  /** XQuery function. */
  _SQL_ROLLBACK(SqlRollback.class, "rollback(id)", arg(ITR_O), EMP, flag(NDT), SQL_URI),

  // Strings Module

  /** XQuery function. */
  _STRINGS_COLOGNE_PHONETIC(StringsColognePhonetic.class, "cologne-phonetic(string)",
      arg(STR_O), STR_O, STRINGS_URI),
  /** XQuery function. */
  _STRINGS_LEVENSHTEIN(StringsLevenshtein.class, "levenshtein(string1,string2)",
      arg(STR_O, STR_O), DBL_O, STRINGS_URI),
  /** XQuery function. */
  _STRINGS_SOUNDEX(StringsSoundex.class, "soundex(string)", arg(STR_O), STR_O, STRINGS_URI),

  // Unit Module

  /** XQuery function. */
  _UNIT_ASSERT(UnitAssert.class, "assert(test[,failure])",
      arg(ITEM_ZM, ITEM_O), ITEM_ZM, flag(NDT), UNIT_URI),
  /** XQuery function. */
  _UNIT_ASSERT_EQUALS(UnitAssertEquals.class, "assert-equals(result,expected[,failure])",
      arg(ITEM_ZM, ITEM_ZM, ITEM_O), ITEM_ZM, flag(NDT), UNIT_URI),
  /** XQuery function. */
  _UNIT_FAIL(UnitFail.class, "fail([failure])", arg(ITEM_O), ITEM_ZM, flag(NDT), UNIT_URI),

  // Update Module

  /** XQuery function. */
  _UPDATE_APPLY(UpdateApply.class, "apply(function,args)", arg(FUNC_O, ARRAY_O), EMP,
      flag(Flag.POS, UPD, NDT, HOF), UPDATE_URI),
  /** XQuery function. */
  _UPDATE_CACHE(UpdateCache.class, "cache()", arg(), ITEM_ZO, flag(NDT), UPDATE_URI),
  /** XQuery function. */
  _UPDATE_FOR_EACH(UpdateForEach.class, "for-each(items,function)",
      arg(ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O).seqType()), EMP, flag(UPD, HOF), UPDATE_URI),
  /** XQuery function. */
  _UPDATE_FOR_EACH_PAIR(UpdateForEachPair.class, "for-each-pair(items1,items2,function)",
      arg(ITEM_ZM, ITEM_ZM, FuncType.get(ITEM_ZM, ITEM_O, ITEM_O).seqType()),
      EMP, flag(UPD, HOF), UPDATE_URI),
  /** XQuery function. */
  _UPDATE_MAP_FOR_EACH(UpdateMapForEach.class, "map-for-each(map,function)",
      arg(MAP_O, FuncType.get(ITEM_ZM, AAT_O, ITEM_ZM).seqType()), EMP, flag(UPD, HOF), UPDATE_URI),
  /** XQuery function. */
  _UPDATE_OUTPUT(UpdateOutput.class, "output(result)", arg(ITEM_ZM), EMP, flag(UPD), UPDATE_URI),

  // User Module

  /** XQuery function. */
  _USER_ALTER(UserAlter.class, "alter(name,newname)", arg(STR_O, STR_O), EMP, flag(UPD), USER_URI),
  /** XQuery function. */
  _USER_CHECK(UserCheck.class, "check(name,password)", arg(STR_O, STR_O), EMP, flag(NDT), USER_URI),
  /** XQuery function. */
  _USER_CREATE(UserCreate.class, "create(name,password[,permissions[,patterns]])",
      arg(STR_O, STR_O, STR_ZM, STR_ZM), EMP, flag(UPD), USER_URI),
  /** XQuery function. */
  _USER_CURRENT(UserCurrent.class, "current()", arg(), STR_O, USER_URI),
  /** XQuery function. */
  _USER_DROP(UserDrop.class, "drop(name[,patterns])", arg(STR_O, STR_ZM), EMP, flag(UPD), USER_URI),
  /** XQuery function. */
  _USER_EXISTS(UserExists.class, "exists(name)", arg(STR_O), BLN_O, flag(NDT), USER_URI),
  /** XQuery function. */
  _USER_GRANT(UserGrant.class, "grant(name,permissions[,patterns])",
      arg(STR_O, STR_ZM, STR_ZM), EMP, flag(UPD), USER_URI),
  /** XQuery function. */
  _USER_INFO(UserInfo.class, "info()", arg(), ELM_O, USER_URI),
  /** XQuery function. */
  _USER_LIST(UserList.class, "list()", arg(), ELM_ZM, flag(NDT), USER_URI),
  /** XQuery function. */
  _USER_LIST_DETAILS(UserListDetails.class, "list-details([name])",
      arg(STR_O), ELM_ZM, flag(NDT), USER_URI),
  /** XQuery function. */
  _USER_PASSWORD(UserPassword.class, "password(name,password)",
      arg(STR_O, STR_O), EMP, flag(UPD), USER_URI),
  /** XQuery function. */
  _USER_UPDATE_INFO(UserUpdateInfo.class, "update-info(element)",
      arg(ELM_O), EMP, flag(UPD), USER_URI),

  // Utility Module

  /** XQuery function. */
  _UTIL_DEEP_EQUAL(UtilDeepEqual.class, "deep-equal(items1,items2[,options])",
      arg(ITEM_ZM, ITEM_ZM, ITEM_O), BLN_O, UTIL_URI),
  /** XQuery function. */
  _UTIL_ITEM_AT(UtilItemAt.class, "item-at(items,pos)", arg(ITEM_ZM, DBL_O), ITEM_ZO, UTIL_URI),
  /** XQuery function. */
  _UTIL_ITEM_RANGE(UtilItemRange.class, "item-range(items,first,last)",
      arg(ITEM_ZM, DBL_O, DBL_O), ITEM_ZM, UTIL_URI),
  /** XQuery function. */
  _UTIL_LAST_FROM(UtilLastFrom.class, "last-from(items)", arg(ITEM_ZM), ITEM_ZO, UTIL_URI),
  /** XQuery function. */
  _UTIL_REPLICATE(UtilReplicate.class, "replicate(items,count)",
      arg(ITEM_ZM, ITR_O), ITEM_ZM, UTIL_URI),

  // Validate Module

  /** XQuery function. */
  _VALIDATE_DTD(ValidateDtd.class, "dtd(input[,schema])",
      arg(ITEM_O, ITEM_O), EMP, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_DTD_INFO(ValidateDtdInfo.class, "dtd-info(input[,schema])",
      arg(ITEM_O, ITEM_O), STR_ZM, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_DTD_REPORT(ValidateDtdReport.class, "dtd-report(input[,schema])",
      arg(ITEM_O, ITEM_O), ELM_O, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_RNG(ValidateRng.class, "rng(input,schema[,compact])",
      arg(ITEM_O, ITEM_O, BLN_O), STR_ZM, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_RNG_INFO(ValidateRngInfo.class, "rng-info(input,schema[,compact])",
      arg(ITEM_O, ITEM_O, BLN_O), STR_ZM, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_RNG_REPORT(ValidateRngReport.class, "rng-report(input,schema[,compact])",
      arg(ITEM_O, ITEM_O, BLN_O), ELM_O, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_XSD(ValidateXsd.class, "xsd(input[,schema[,version]])",
      arg(ITEM_O, ITEM_O, STR_O), EMP, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_XSD_INFO(ValidateXsdInfo.class, "xsd-info(input[,schema[,version]])",
      arg(ITEM_O, ITEM_O, STR_O), STR_ZM, flag(NDT), VALIDATE_URI),
  /** XQuery function. */
  _VALIDATE_XSD_REPORT(ValidateXsdReport.class, "xsd-report(input[,schema[,version]])",
      arg(ITEM_O, ITEM_O, STR_O), ELM_O, flag(NDT), VALIDATE_URI),

  // Web Module

  /** XQuery function. */
  _WEB_CONTENT_TYPE(WebContentType.class, "content-type(path)", arg(STR_O), STR_O, WEB_URI),
  /** XQuery function. */
  _WEB_CREATE_URL(WebCreateUrl.class, "create-url(url,params)", arg(STR_O, MAP_O), STR_O, WEB_URI),
  /** XQuery function. */
  _WEB_DECODE_URL(WebDecodeUrl.class, "decode-url(string)", arg(STR_O), STR_O, WEB_URI),
  /** XQuery function. */
  _WEB_ENCODE_URL(WebEncodeUrl.class, "encode-url(string)", arg(STR_O), STR_O, WEB_URI),
  /** XQuery function. */
  _WEB_REDIRECT(WebRedirect.class, "redirect(location[,params])",
      arg(STR_O, MAP_O), ELM_O, WEB_URI),
  /** XQuery function. */
  _WEB_RESPONSE_HEADER(WebResponseHeader.class, "response-header([output[,headers[,attributes]]])",
      arg(MAP_ZO, MAP_ZO, MAP_ZO), ELM_O, WEB_URI),

  // XQuery Module

  /** XQuery function. */
  _XQUERY_EVAL(XQueryEval.class, "eval(string[,bindings[,options]])",
      arg(STR_O, MAP_ZO, MAP_ZO), ITEM_ZM, flag(NDT), XQUERY_URI),
  /** XQuery function. */
  _XQUERY_EVAL_UPDATE(XQueryEvalUpdate.class, "eval-update(string[,bindings[,options]])",
      arg(STR_O, MAP_ZO, MAP_ZO), EMP, flag(UPD), XQUERY_URI),
  /** XQuery function. */
  _XQUERY_FORK_JOIN(XQueryForkJoin.class, "fork-join(functions[,options])",
      arg(FUNC_ZM, MAP_ZO), ITEM_ZM, flag(HOF), XQUERY_URI),
  /** XQuery function. */
  _XQUERY_INVOKE(XQueryInvoke.class, "invoke(uri[,bindings[,options]])",
      arg(STR_O, MAP_ZO, MAP_ZO), ITEM_ZM, flag(NDT), XQUERY_URI),
  /** XQuery function. */
  _XQUERY_INVOKE_UPDATE(XQueryInvokeUpdate.class, "invoke-update(uri[,bindings[,options]])",
      arg(STR_O, MAP_ZO, MAP_ZO), EMP, flag(UPD), XQUERY_URI),
  /** XQuery function. */
  _XQUERY_PARSE(XQueryParse.class, "parse(string[,options])",
      arg(STR_O, MAP_ZO), NOD_O, flag(NDT), XQUERY_URI),
  /** XQuery function. */
  _XQUERY_PARSE_URI(XQueryParseUri.class, "parse-uri(uri[,options])",
      arg(STR_O, MAP_ZO), NOD_O, flag(NDT), XQUERY_URI),

  // XSLT Module

  /** XQuery function. */
  _XSLT_INIT(XsltInit.class, "init()", arg(), NOD_O, flag(NDT), XSLT_URI),
  /** XQuery function. */
  _XSLT_PROCESSOR(XsltProcessor.class, "processor()", arg(), STR_O, XSLT_URI),
  /** XQuery function. */
  _XSLT_TRANSFORM(XsltTransform.class, "transform(input,stylesheet[,params[,options]])",
      arg(ITEM_O, ITEM_O, MAP_ZO, MAP_ZO), NOD_O, flag(NDT), XSLT_URI),
  /** XQuery function. */
  _XSLT_TRANSFORM_TEXT(XsltTransformText.class,
      "transform-text(input,stylesheet[,params[,options]])",
      arg(ITEM_O, ITEM_O, MAP_ZO, MAP_ZO), STR_O, flag(NDT), XSLT_URI),
  /** XQuery function. */
  _XSLT_VERSION(XsltVersion.class, "version()", arg(), STR_O, XSLT_URI),

  // ZIP Module

  /** XQuery function. */
  _ZIP_BINARY_ENTRY(ZipBinaryEntry.class, "binary-entry(path,entry)",
      arg(STR_O, STR_O), B64_O, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_ENTRIES(ZipEntries.class, "entries(path)", arg(STR_O), ELM_O, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_HTML_ENTRY(ZipHtmlEntry.class, "html-entry(path,entry)",
      arg(STR_O, STR_O), NOD_O, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_TEXT_ENTRY(ZipTextEntry.class, "text-entry(path,entry[,encoding])",
      arg(STR_O, STR_O, STR_O), STR_O, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_XML_ENTRY(ZipXmlEntry.class, "xml-entry(path,entry)",
      arg(STR_O, STR_O), NOD_O, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_UPDATE_ENTRIES(ZipUpdateEntries.class, "update-entries(zip,output)",
      arg(ELM_O, STR_O), EMP, flag(NDT), ZIP_URI),
  /** XQuery function. */
  _ZIP_ZIP_FILE(ZipZipFile.class, "zip-file(zip)", arg(ELM_O), EMP, flag(NDT), ZIP_URI);

  /** URIs of built-in functions. */
  public static final TokenSet URIS = new TokenSet();

  static {
    for(final Function f : values()) {
      final byte[] u = f.uri;
      if(u != null) URIS.add(u);
    }
  }

  /** Cached enums (faster). */
  public static final Function[] VALUES = values();
  /** Minimum and maximum number of arguments. */
  public final int[] minMax;
  /** Parameter types. */
  public final SeqType[] params;
  /** Function class. */
  public final Class<? extends StandardFunc> clazz;

  /** Description. */
  final String desc;
  /** Sequence type. */
  final SeqType seqType;

  /** Compiler flags. */
  private final EnumSet<Flag> flags;
  /** URI. */
  private final byte[] uri;

  /**
   * Constructs a function signature; calls
   * {@link #Function(Class, String, SeqType[], SeqType, EnumSet)}.
   * @param func reference to the class containing the function implementation
   * @param desc descriptive function string
   * @param args types of the function arguments
   * @param seqType return type
   */
  Function(final Class<? extends StandardFunc> func, final String desc, final SeqType[] args,
      final SeqType seqType) {
    this(func, desc, args, seqType, EnumSet.noneOf(Flag.class));
  }

  /**
   * Constructs a function signature; calls
   * {@link #Function(Class, String, SeqType[], SeqType, EnumSet)}.
   * @param func reference to the class containing the function implementation
   * @param desc descriptive function string
   * @param args types of the function arguments
   * @param type return type
   * @param uri uri
   */
  Function(final Class<? extends StandardFunc> func, final String desc, final SeqType[] args,
      final SeqType type, final byte[] uri) {
    this(func, desc, args, type, EnumSet.noneOf(Flag.class), uri);
  }

  /**
   * Constructs a function signature; calls
   * {@link #Function(Class, String, SeqType[], SeqType, EnumSet, byte[])}.
   * @param func reference to the class containing the function implementation
   * @param desc descriptive function string
   * @param args types of the function arguments
   * @param seqType return type
   * @param flag static function properties
   */
  Function(final Class<? extends StandardFunc> func, final String desc, final SeqType[] args,
      final SeqType seqType, final EnumSet<Flag> flag) {
    this(func, desc, args, seqType, flag, FN_URI);
  }

  /**
   * Constructs a function signature.
   * @param func reference to the class containing the function implementation
   * @param desc descriptive function string, containing the function name and its
   *             arguments in parentheses. Optional arguments are represented in nested
   *             square brackets; three dots indicate that the number of arguments of a
   *             function is not limited
   * @param params parameter types
   * @param seqType return type
   * @param flags static function properties
   * @param uri uri
   */
  Function(final Class<? extends StandardFunc> func, final String desc, final SeqType[] params,
      final SeqType seqType, final EnumSet<Flag> flags, final byte[] uri) {

    this.clazz = func;
    this.desc = desc;
    this.seqType = seqType;
    this.params = params;
    this.flags = flags;
    this.uri = uri;
    minMax = minMax(desc, params);

    // treat updating expressions as non-deterministic
    if(flags.contains(Flag.UPD)) flags.add(Flag.NDT);
  }

  /**
   * Computes the minimum and maximum number of arguments by analyzing the description string.
   * @param desc description
   * @param args arguments
   * @return min/max values
   */
  public static int[] minMax(final String desc, final SeqType[] args) {
    // count number of minimum and maximum arguments by analyzing the description
    final int b = desc.indexOf('['), al = args.length;
    if(b == -1) return new int[] { al, al };

    int c = b + 1 < desc.length() && desc.charAt(b + 1) == ',' ? 1 : 0;
    for(int i = 0; i < b; i++) if(desc.charAt(i) == ',') c++;
    return new int[] { c, desc.contains(DOTS) ? Integer.MAX_VALUE : al };
  }

  /**
   * Creates a new instance of the function.
   * @param sc static context
   * @param info input info
   * @param exprs arguments
   * @return function
   */
  public StandardFunc get(final StaticContext sc, final InputInfo info, final Expr... exprs) {
    return Reflect.get(clazz).init(sc, info, this, exprs);
  }

  /**
   * Returns the namespace URI of this function.
   * @return function
   */
  public final byte[] uri() {
    return uri;
  }

  /**
   * Indicates if an expression has the specified compiler property.
   * @param flag flag
   * @return result of check
   * @see Expr#has(Flag...)
    */
  public boolean has(final Flag flag) {
    return flags.contains(flag);
  }

  /**
   * Returns the function type of this function with the given arity.
   * @param arity number of arguments
   * @param anns annotations
   * @return function type
   */
  final FuncType type(final int arity, final AnnList anns) {
    final SeqType[] st = new SeqType[arity];
    if(arity != 0 && minMax[1] == Integer.MAX_VALUE) {
      final int pl = params.length;
      System.arraycopy(params, 0, st, 0, pl);
      final SeqType var = params[pl - 1];
      for(int p = pl; p < arity; p++) st[p] = var;
    } else {
      System.arraycopy(params, 0, st, 0, arity);
    }
    return FuncType.get(anns, seqType, st);
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
   * Returns the names of the function parameters.
   * @return names of function parameters
   */
  final String[] names() {
    final String names = desc.replaceFirst(".*?\\(", "").replace(",...", "").
        replaceAll("[\\[\\])\\s]", "");
    return names.isEmpty() ? new String[0] : Strings.split(names, ',');
  }

  /**
   * Returns the local name of the function.
   * @return name
   */
  public byte[] local() {
    return new TokenBuilder(desc.substring(0, desc.indexOf('('))).finish();
  }

  /**
   * Returns the prefixed name of the annotation.
   * @return name
   */
  byte[] id() {
    final TokenBuilder tb = new TokenBuilder();
    if(!Token.eq(uri, FN_URI)) tb.add(NSGlobal.prefix(uri)).add(':');
    return tb.add(local()).finish();
  }

  /**
   * Returns the the parameter names for an instance of this function with the given arity.
   * @param arity number of arguments
   * @return names of parameters
   */
  final QNm[] paramNames(final int arity) {
    final String[] strings = names();
    final QNm[] names = new QNm[arity];
    final int nl = strings.length;
    for(int n = Math.min(arity, nl); --n >= 0;) names[n] = new QNm(strings[n]);
    if(arity > nl) {
      final String[] parts = strings[nl - 1].split("(?=\\d+$)", 2);
      final int start = Integer.parseInt(parts[1]);
      for(int n = nl; n < arity; n++) names[n] = new QNm(parts[0] + (start + n - nl + 1), "");
    }
    return names;
  }

  /**
   * Returns a string representation of the function with the specified
   * arguments. All objects are wrapped with quotes, except for the following ones:
   * <ul>
   * <li>numbers (integer, long, float, double)</li>
   * <li>booleans (which will be suffixed with parentheses)</li>
   * <li>strings starting with a space</li>
   * </ul>
   * @param args arguments
   * @return string representation (prefixed with a space to simplify nesting of returned string)
   */
  public final String args(final Object... args) {
    final TokenBuilder tb = new TokenBuilder();
    for(final Object arg : args) {
      if(!tb.isEmpty()) tb.add(", ");
      if(arg == null) {
        tb.add("()");
      } else if(arg instanceof Expr) {
        tb.addExt(arg);
      } else if(arg instanceof Number) {
        tb.addExt(arg);
      } else if(arg instanceof Boolean) {
        tb.add(arg + "()");
      } else {
        final String str = arg.toString();
        if(str.startsWith(" ")) {
          tb.add(str.substring(1));
        } else {
          tb.add('"' + str.replaceAll("\"", "\"\"") + '"');
        }
      }
    }
    return ' ' + toString().replaceAll("\\(.*", "(") + tb + ')';
  }

  @Override
  public final String toString() {
    return new TokenBuilder(NSGlobal.prefix(uri)).add(':').add(desc).toString();
  }

  /*
   * Returns the names of all functions. Used to update MediaWiki syntax highlighter.
   * All function names are listed in reverse order to give precedence to longer names.
   * @param args ignored
  public static void main(final String... args) {
    final org.basex.util.list.StringList sl = new org.basex.util.list.StringList();
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
