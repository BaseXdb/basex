package org.basex.query.func;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.item.SeqType.*;
import java.util.HashMap;
import org.basex.query.expr.Expr;
import org.basex.query.item.FunType;
import org.basex.query.item.SeqType;
import org.basex.query.util.NSGlobal;
import org.basex.util.InputInfo;
import org.basex.util.Reflect;
import org.basex.util.TokenBuilder;

/**
 * Signatures of all statically available XQuery functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public enum FunDef {

  /* FNAcc functions. */

  /** XQuery function. */
  POS(FNAcc.class, "position()", ITR),
  /** XQuery function. */
  LAST(FNAcc.class, "last()", ITR),
  /** XQuery function. */
  STRING(FNAcc.class, "string([item])", STR, 0, ITEM_ZO),
  /** XQuery function. */
  NUMBER(FNAcc.class, "number([item])", DBL, 0, AAT_ZO),
  /** XQuery function. */
  STRLEN(FNAcc.class, "string-length([item])", ITR, 0, STR_ZO),
  /** XQuery function. */
  NRMSTR(FNAcc.class, "normalize-space([string])", STR, 0, STR_ZO),
  /** XQuery function. */
  URIQNM(FNAcc.class, "namespace-uri-from-QName(qname)", URI_ZO, QNM_ZO),

  /* FNAggr functions. */

  /** XQuery function. */
  AVG(FNAggr.class, "avg(item)", AAT_ZO, AAT_ZM),
  /** XQuery function. */
  CNT(FNAggr.class, "count(item)", ITR, ITEM_ZM),
  /** XQuery function. */
  MAX(FNAggr.class, "max(item[,coll])", AAT_ZO, 1, AAT_ZM, STR),
  /** XQuery function. */
  MIN(FNAggr.class, "min(item[,coll])", AAT_ZO, 1, AAT_ZM, STR),
  /** XQuery function. */
  SUM(FNAggr.class, "sum(item[,zero])", AAT_ZO, 1, AAT_ZM, AAT_ZO),

  /* FNContext functions. */

  /** XQuery function. */
  CURRDATE(FNContext.class, "current-date()", DAT),
  /** XQuery function. */
  CURRDTM(FNContext.class, "current-dateTime()", DTM),
  /** XQuery function. */
  CURRTIME(FNContext.class, "current-time()", TIM),
  /** XQuery function. */
  IMPLZONE(FNContext.class, "implicit-timezone()", DTD),
  /** XQuery function. */
  COLLAT(FNContext.class, "default-collation()", STR),
  /** XQuery function. */
  STBASEURI(FNContext.class, "static-base-uri()", URI_ZO),

  /* FNDate functions. */

  /** XQuery function. */
  DAYDAT(FNDate.class, "day-from-date(item)", ITR_ZO, DAT_ZO),
  /** XQuery function. */
  DAYDTM(FNDate.class, "day-from-dateTime(datetime)", ITR_ZO, DTM_ZO),
  /** XQuery function. */
  DAYDUR(FNDate.class, "days-from-duration(dur)", ITR_ZO, DUR_ZO),
  /** XQuery function. */
  HOUDTM(FNDate.class, "hours-from-dateTime(datetime)", ITR_ZO, DTM_ZO),
  /** XQuery function. */
  HOUDUR(FNDate.class, "hours-from-duration(dur)", ITR_ZO, DUR_ZO),
  /** XQuery function. */
  HOUTIM(FNDate.class, "hours-from-time(item)", ITR_ZO, TIM_ZO),
  /** XQuery function. */
  MINDTM(FNDate.class, "minutes-from-dateTime(datetime)", ITR_ZO, DTM_ZO),
  /** XQuery function. */
  MINDUR(FNDate.class, "minutes-from-duration(dur)", ITR_ZO, DUR_ZO),
  /** XQuery function. */
  MINTIM(FNDate.class, "minutes-from-time(item)", ITR_ZO, TIM_ZO),
  /** XQuery function. */
  MONDAT(FNDate.class, "month-from-date(item)", ITR_ZO, DAT_ZO),
  /** XQuery function. */
  MONDTM(FNDate.class, "month-from-dateTime(datetime)", ITR_ZO, DTM_ZO),
  /** XQuery function. */
  MONDUR(FNDate.class, "months-from-duration(dur)", ITR_ZO, DUR_ZO),
  /** XQuery function. */
  SECDTM(FNDate.class, "seconds-from-dateTime(datetime)", DEC_ZO, DTM_ZO),
  /** XQuery function. */
  SECDUR(FNDate.class, "seconds-from-duration(dur)", DEC_ZO, DUR_ZO),
  /** XQuery function. */
  SECTIM(FNDate.class, "seconds-from-time(item)", DEC_ZO, TIM_ZO),
  /** XQuery function. */
  ZONDAT(FNDate.class, "timezone-from-date(item)", DTD_ZO, DAT_ZO),
  /** XQuery function. */
  ZONDTM(FNDate.class, "timezone-from-dateTime(item)", DTD_ZO, DTM_ZO),
  /** XQuery function. */
  ZONTIM(FNDate.class, "timezone-from-time(item)", DTD_ZO, TIM_ZO),
  /** XQuery function. */
  YEADAT(FNDate.class, "year-from-date(item)", ITR_ZO, DAT_ZO),
  /** XQuery function. */
  YEADTM(FNDate.class, "year-from-dateTime(datetime)", ITR_ZO, DTM_ZO),
  /** XQuery function. */
  YEADUR(FNDate.class, "years-from-duration(dur)", ITR_ZO, DUR_ZO),
  /** XQuery function. */
  DATZON(FNDate.class, "adjust-date-to-timezone(date[,zone])", DAT_ZO, 1,
      DAT_ZO, DTD_ZO),
  /** XQuery function. */
  DTMZON(FNDate.class, "adjust-dateTime-to-timezone(date[,zone])", DTM, 1,
      DTM_ZO, DTD_ZO),
  /** XQuery function. */
  TIMZON(FNDate.class, "adjust-time-to-timezone(date[,zone])", TIM_ZO, 1,
      TIM_ZO, DTD_ZO),
  /** XQuery function. */
  DATETIME(FNDate.class, "dateTime(date,time)", DTM_ZO, DAT_ZO, TIM_ZO),

  /* FNFormat functions. */

  /** XQuery function. */
  FORMINT(FNFormat.class, "format-integer(number,picture[,lang])", STR, 2,
      ITR_ZO, STR, STR),
  /** XQuery function. */
  FORMNUM(FNFormat.class, "format-number(number,picture[,format])", STR, 2,
      ITR_ZO, STR, STR),
  /** XQuery function. */
  FORMDTM(FNFormat.class,
      "format-dateTime(number,picture,[lang[,cal[,place]]])",
      STR_ZO, 2, DTM_ZO, STR, STR_ZO, STR_ZO, STR_ZO),
  /** XQuery function. */
  FORMDAT(FNFormat.class, "format-date(date,picture,[lang[,cal[,place]]])",
      STR_ZO, 2, DAT_ZO, STR, STR_ZO, STR_ZO, STR_ZO),
  /** XQuery function. */
  FORMTIM(FNFormat.class, "format-time(number,picture,[lang[,cal[,place]]])",
      STR_ZO, 2, TIM_ZO, STR, STR_ZO, STR_ZO, STR_ZO),

  /* FNFunc functions. */

  /** XQuery function. */
  FILTER(FNFunc.class, "filter(function,seq)", ITEM_ZM,
      FunType.get(new SeqType[] { ITEM }, BLN).seq(), ITEM_ZM),
  /** XQuery function. */
  FUNCNAME(FNFunc.class, "function-name(function)", QNM_ZO,
      FunType.ANY_FUN.seq()),
  /** XQuery function. */
  FUNCARITY(FNFunc.class, "function-arity(function)", ITR,
      FunType.ANY_FUN.seq()),
  /** XQuery function. */
  MAP(FNFunc.class, "map(function,seq)", ITEM_ZM,
      FunType.get(new SeqType[] { ITEM }, ITEM_ZM).seq(), ITEM_ZM),
  /** XQuery function. */
  MAPPAIRS(FNFunc.class, "map-pairs(function,seq1,seq2)", ITEM_ZM,
      FunType.get(new SeqType[] { ITEM, ITEM }, ITEM_ZM).seq(),
      ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  FOLDLEFT(FNFunc.class, "fold-left(function,zero,seq)", ITEM_ZM,
      FunType.get(new SeqType[] { ITEM_ZM, ITEM }, ITEM_ZM).seq(),
      ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  FOLDRIGHT(FNFunc.class, "fold-right(function,zero,seq)", ITEM_ZM,
      FunType.get(new SeqType[] { ITEM, ITEM_ZM }, ITEM_ZM).seq(),
      ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  PARTAPP(FNFunc.class, "partial-apply(function,arg[,pos])", FUN_O, 2,
      FUN_O, ITEM_ZM, ITR),

  /* FNGen functions. */

  /** XQuery function. */
  DATA(FNGen.class, "data([item])", AAT_ZM, 0, ITEM_ZM),
  /** XQuery function. */
  COLL(FNGen.class, "collection([uri])", NOD_ZM, 0, STR_ZO),
  /** XQuery function. */
  DOC(FNGen.class, "doc(uri)", DOC_ZO, STR_ZO),
  /** XQuery function. */
  DOCAVL(FNGen.class, "doc-available(uri)", BLN, STR_ZO),
  /** XQuery function. */
  PUT(FNGen.class, "put(node,path)", EMP, NOD, STR_ZO),
  /** XQuery function. */
  PARSETXT(FNGen.class, "unparsed-text(uri[,encoding])", STR_ZO, 1, STR_ZO,
      STR),
  /** XQuery function. */
  PARSETXTLIN(FNGen.class, "unparsed-text-lines(uri[,encoding])", STR_ZM, 1,
      STR_ZO, STR),
  /** XQuery function. */
  PARSETXTAVL(FNGen.class, "unparsed-text-available(uri[,encoding])", BLN, 1,
      STR_ZO, STR),
  /** XQuery function. */
  PARSEXML(FNGen.class, "parse-xml(string[,base])", DOC_O, 1, STR_ZO, STR),
  /** XQuery function. */
  URICOLL(FNGen.class, "uri-collection([uri])", URI_ZM, 0, STR_ZO),
  /** XQuery function. */
  SERIALIZE(FNGen.class, "serialize(node[,params])", STR, 1, ITEM_ZM, ELM_ZO),

  /* FNId functions. */

  /** XQuery function. */
  ID(FNId.class, "id(string[,item])", ELM_ZM, 1, STR_ZM, NOD),
  /** XQuery function. */
  IDREF(FNId.class, "idref(string[,item])", NOD_ZM, 1, STR_ZM, NOD),
  /** XQuery function. */
  LANG(FNId.class, "lang(string[,item])", BLN, 1, STR_ZO, NOD),
  /** XQuery function. */
  ELID(FNId.class, "element-with-id(string[,item])", ELM_ZM, 1, STR_ZM, NOD),

  /* FNInfo functions. */

  /** XQuery function. */
  ERROR(FNInfo.class, "error([code[,desc[,object]]])", EMP, 0,
      QNM_ZO, STR, ITEM_ZM),
  /** XQuery function. */
  TRACE(FNInfo.class, "trace(item,msg)", ITEM_ZM, ITEM_ZM, STR),
  /** XQuery function. */
  ENV(FNInfo.class, "environment-variable(string)", STR_ZO, STR),
  /** XQuery function. */
  ENVS(FNInfo.class, "available-environment-variables()", STR_ZM),

  /* FNNode functions. */

  /** XQuery function. */
  DOCURI(FNNode.class, "document-uri([node])", URI_ZO, 0, NOD_ZO),
  /** XQuery function. */
  NILLED(FNNode.class, "nilled(node)", BLN_ZO, NOD_ZO),
  /** XQuery function. */
  NODENAME(FNNode.class, "node-name([node])", QNM_ZO, 0, NOD_ZO),
  /** XQuery function. */
  LOCNAME(FNNode.class, "local-name([node])", STR, 0, NOD_ZO),
  /** XQuery function. */
  NAME(FNNode.class, "name([node])", STR, 0, NOD_ZO),
  /** XQuery function. */
  NSURI(FNNode.class, "namespace-uri([node])", URI, 0, NOD_ZO),
  /** XQuery function. */
  ROOT(FNNode.class, "root([node])", NOD_ZO, 0, NOD_ZO),
  /** XQuery function. */
  BASEURI(FNNode.class, "base-uri([node])", URI_ZO, 0, NOD_ZO),
  /** XQuery function. */
  GENID(FNNode.class, "generate-id([node])", STR, 0, NOD_ZO),
  /** XQuery function. */
  CHILDREN(FNNode.class, "has-children(node)", BLN, NOD_ZM),

  /* FNNum functions. */

  /** XQuery function. */
  ABS(FNNum.class, "abs(num)", AAT_ZO, AAT_ZO),
  /** XQuery function. */
  CEIL(FNNum.class, "ceiling(num)", AAT_ZO, AAT_ZO),
  /** XQuery function. */
  FLOOR(FNNum.class, "floor(num)", AAT_ZO, AAT_ZO),
  /** XQuery function. */
  ROUND(FNNum.class, "round(num[,prec])", AAT_ZO, 1, AAT_ZO, ITR),
  /** XQuery function. */
  RNDHLF(FNNum.class, "round-half-to-even(num[,prec])", AAT_ZO, 1, AAT_ZO, ITR),

  /* FNPat functions. */

  /** XQuery function. */
  MATCH(FNPat.class, "matches(item,pattern[,mod])", BLN, 2, STR_ZO, STR, STR),
  /** XQuery function. */
  REPLACE(FNPat.class, "replace(item,pattern,replace[,mod])", STR, 3, STR_ZO,
      STR, STR, STR),
  /** XQuery function. */
  TOKEN(FNPat.class, "tokenize(item,pattern[,mod])", STR_ZM, 2, STR_ZO,
      STR, STR),
  /** XQuery function. */
  ANALZYE(FNPat.class, "analyze-string(input,pattern[,mod])", ELM, 2, STR_ZO,
      STR, STR),

  /* FNQName functions. */

  /** XQuery function. */
  INSCOPE(FNQName.class, "in-scope-prefixes(elem)", STR_ZM, ELM),
  /** XQuery function. */
  LOCNAMEQNAME(FNQName.class, "local-name-from-QName(qname)", NCN_ZO, QNM_ZO),
  /** XQuery function. */
  NSURIPRE(FNQName.class, "namespace-uri-for-prefix(pref,elem)", URI_ZO, STR_ZO,
      ELM),
  /** XQuery function. */
  QNAME(FNQName.class, "QName(uri,name)", QNM, STR_ZO, STR),
  /** XQuery function. */
  PREQNAME(FNQName.class, "prefix-from-QName(qname)", NCN_ZO, QNM_ZO),
  /** XQuery function. */
  RESQNAME(FNQName.class, "resolve-QName(item,base)", QNM_ZO, STR_ZO, ELM),
  /** XQuery function. */
  RESURI(FNQName.class, "resolve-uri(name[,elem])", URI_ZO, 1, STR_ZO, STR),

  /* FNSeq functions. */

  /** XQuery function. */
  DISTINCT(FNSeq.class, "distinct-values(items[,coll])", AAT_ZM, 1, AAT_ZM,
      STR),
  /** XQuery function. */
  INDEXOF(FNSeq.class, "index-of(items,item[,coll])", ITR_ZM, 2, AAT_ZM, AAT,
      STR),
  /** XQuery function. */
  INSBEF(FNSeq.class, "insert-before(items,pos,insert)", ITEM_ZM, ITEM_ZM, ITR,
      ITEM_ZM),
  /** XQuery function. */
  REMOVE(FNSeq.class, "remove(items,pos)", ITEM_ZM, ITEM_ZM, ITR),
  /** XQuery function. */
  REVERSE(FNSeq.class, "reverse(items)", ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  SUBSEQ(FNSeq.class, "subsequence(items,start[,len])", ITEM_ZM, 2, ITEM_ZM,
      DBL, DBL),
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
  UNORDER(FNSimple.class, "unordered(item)", ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  ZEROORONE(FNSimple.class, "zero-or-one(item)", ITEM_ZO, ITEM_ZM),
  /** XQuery function. */
  EXACTLYONE(FNSimple.class, "exactly-one(item)", ITEM, ITEM_ZM),
  /** XQuery function. */
  ONEORMORE(FNSimple.class, "one-or-more(item)", ITEM_OM, ITEM_ZM),
  /** XQuery function. */
  DEEPEQUAL(FNSimple.class, "deep-equal(item,item[,coll])", BLN, 2, ITEM_ZM,
      ITEM_ZM, STR),

  /* FNStr functions. */

  /** XQuery function. */
  CODEPNT(FNStr.class, "codepoint-equal(string,string)", BLN_ZO, STR_ZO,
      STR_ZO),
  /** XQuery function. */
  CODESTR(FNStr.class, "codepoints-to-string(nums)", STR, ITR_ZM),
  /** XQuery function. */
  COMPARE(FNStr.class, "compare(first,second[,coll])", ITR_ZO, 2, STR_ZO,
      STR_ZO, STR),
  /** XQuery function. */
  CONCAT(FNStr.class, "concat(atom,atom[,...])", STR, -2, AAT_ZO, AAT_ZO),
  /** XQuery function. */
  CONTAINS(FNStr.class, "contains(string,sub[,coll])", BLN, 2, STR_ZO, STR_ZO,
      STR),
  /** XQuery function. */
  ENCURI(FNStr.class, "encode-for-uri(string)", STR, STR_ZO),
  /** XQuery function. */
  ENDS(FNStr.class, "ends-with(string,sub[,coll])", BLN, 2, STR_ZO, STR_ZO,
      STR),
  /** XQuery function. */
  ESCURI(FNStr.class, "escape-html-uri(string)", STR, STR_ZO),
  /** XQuery function. */
  IRIURI(FNStr.class, "iri-to-uri(string)", STR, STR_ZO),
  /** XQuery function. */
  LOWER(FNStr.class, "lower-case(string)", STR, STR_ZO),
  /** XQuery function. */
  NORMUNI(FNStr.class, "normalize-unicode(string[,form])", STR, 1, STR_ZO, STR),
  /** XQuery function. */
  STARTS(FNStr.class, "starts-with(string,sub[,coll])", BLN, 2, STR_ZO, STR_ZO,
      STR),
  /** XQuery function. */
  STRJOIN(FNStr.class, "string-join(strings[,sep])", STR, 1, STR_ZM, STR),
  /** XQuery function. */
  STCODE(FNStr.class, "string-to-codepoints(string)", ITR_ZM, STR_ZO),
  /** XQuery function. */
  SUBSTR(FNStr.class, "substring(string,start[,len])", STR, 2, STR_ZO, DBL,
      DBL),
  /** XQuery function. */
  SUBAFTER(FNStr.class, "substring-after(string,sub[,coll])", STR, 2, STR_ZO,
      STR_ZO, STR),
  /** XQuery function. */
  SUBBEFORE(FNStr.class, "substring-before(string,sub[,coll])", STR, 2, STR_ZO,
      STR_ZO, STR),
  /** XQuery function. */
  TRANS(FNStr.class, "translate(string,map,trans)", STR, STR_ZO, STR, STR),
  /** XQuery function. */
  UPPER(FNStr.class, "upper-case(string)", STR, STR_ZO),

  /* FNMap functions. */

  /** XQuery Function. */
  MAPNEW(FNMap.class, "new([maps[,coll]])", MAP_O, 0, MAP_ZM, STR),
  /** XQuery Function. */
  MAPENTRY(FNMap.class, "entry(key,value)", MAP_O, ITEM, ITEM_ZM),
  /** XQuery Function. */
  MAPGET(FNMap.class, "get(map,key)", ITEM_ZM, MAP_O, ITEM),
  /** XQuery Function. */
  MAPCONT(FNMap.class, "contains(map,key)", BLN, MAP_O, ITEM),
  /** XQuery Function. */
  MAPREM(FNMap.class, "remove(map,key)", MAP_O, MAP_O, ITEM),
  /** XQuery Function. */
  MAPSIZE(FNMap.class, "size(map)", ITR, MAP_O),
  /** XQuery Function. */
  MAPKEYS(FNMap.class, "keys(map)", AAT_ZM, MAP_O),
  /** XQuery Function. */
  MAPCOLL(FNMap.class, "collation(map)", STR, MAP_O),

  /* FNMath functions. */

  /** XQuery math function. */
  PI(FNMath.class, "pi()", DBL),
  /** XQuery math function. */
  SQRT(FNMath.class, "sqrt(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  SIN(FNMath.class, "sin(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  COS(FNMath.class, "cos(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  TAN(FNMath.class, "tan(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  ASIN(FNMath.class, "asin(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  ACOS(FNMath.class, "acos(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  ATAN(FNMath.class, "atan(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  ATAN2(FNMath.class, "atan2(number,number)", DBL, DBL, DBL),
  /** XQuery math function. */
  POW(FNMath.class, "pow(number,number)", DBL_ZO, DBL_ZO, ITR),
  /** XQuery math function. */
  EXP(FNMath.class, "exp(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  EXP10(FNMath.class, "exp10(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  LOG(FNMath.class, "log(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function. */
  LOG10(FNMath.class, "log10(number)", DBL_ZO, DBL_ZO),

  /** XQuery math function (project specific). */
  RAND(FNMath.class, "random()", DBL),
  /** XQuery math function (project specific). */
  E(FNMath.class, "e()", DBL),
  /** XQuery math function (project specific). */
  SINH(FNMath.class, "sinh(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function (project specific). */
  COSH(FNMath.class, "cosh(number)", DBL_ZO, DBL_ZO),
  /** XQuery math function (project specific). */
  TANH(FNMath.class, "tanh(number)", DBL_ZO, DBL_ZO),

  /* FNDb functions. */

  /** Database function: opens a database. */
  OPEN(FNDb.class, "open(string)", NOD_ZM, STR),
  /** Database function: opens a specific database node. */
  OPENPRE(FNDb.class, "open-pre(string,pre)", NOD_ZM, STR, ITR),
  /** Database function: opens a specific database node. */
  OPENID(FNDb.class, "open-id(string,id)", NOD_ZM, STR, ITR),
  /** Database function: searches the text index. */
  TEXT(FNDb.class, "text(string)", NOD_ZM, ITEM),
  /** Database function: searches the attribute index. */
  ATTR(FNDb.class, "attribute(string[,name])", NOD_ZM, 1, ITEM, STR),
  /** Database function: searches the full-text index. */
  FULLTEXT(FNDb.class, "fulltext(string)", NOD_ZM, STR),
  /** Database function: lists all database. */
  LIST(FNDb.class, "list()", STR_ZM),
  /** Database function: lists system information. */
  SYSTEM(FNDb.class, "system()", STR),
  /** Database function: returns database or index information. */
  INFO(FNDb.class, "info([type])", STR, 0, STR),
  /** Database function: returns the node ids of database nodes. */
  NODEID(FNDb.class, "node-id(nodes)", ITR_ZM, NOD_ZM),
  /** Database function: returns the pre values of database nodes. */
  NODEPRE(FNDb.class, "node-pre(nodes)", ITR_ZM, NOD_ZM),

  /* FNFile functions (EXPath). */

  /** XQuery function */
  FEXISTS(FNFile.class, "exists(path)", BLN, STR),
  /** XQuery function */
  ISDIR(FNFile.class, "is-directory(path)", BLN, STR),
  /** XQuery function */
  ISFILE(FNFile.class, "is-file(path)", BLN, STR),
  /** XQuery function */
  LASTMOD(FNFile.class, "last-modified(path)", DTM, STR),
  /** XQuery function */
  SIZE(FNFile.class, "size(path)", ITR, STR),
  /** XQuery function */
  BASENAME(FNFile.class, "base-name(path[,suffix])", STR, 1, STR, STR),
  /** XQuery function */
  DIRNAME(FNFile.class, "dir-name(path)", STR, STR),
  /** XQuery function */
  PATHNATIVE(FNFile.class, "path-to-native(path)", STR, STR),
  /** XQuery function */
  PATHTOURI(FNFile.class, "path-to-uri(path)", URI, STR),
  /** XQuery function */
  RESOLVEPATH(FNFile.class, "resolve-path(path)", STR, STR),
  /** XQuery function */
  FLIST(FNFile.class, "list(path[,recursive[,pattern]])", STR_ZM, 1, STR, BLN,
      STR),
  /** XQuery function */
  CREATEDIR(FNFile.class, "create-directory(path)", EMP, STR),
  /** XQuery function */
  DELETE(FNFile.class, "delete(path)", EMP, STR),
  /** XQuery function */
  READTEXT(FNFile.class, "read-text(path[,encoding])", STR, 1, STR, STR),
  /** XQuery function */
  READLINES(FNFile.class, "read-text-lines(path[,encoding])", STR_ZM, 1, STR,
      STR),
  /** XQuery function */
  READBIN(FNFile.class, "read-binary(path)", B64, STR),
  /** XQuery function */
  WRITE(FNFile.class, "write(path,data[,params])", EMP, 2, STR, ITEM_ZM, NOD),
  /** XQuery function */
  WRITEBIN(FNFile.class, "write-binary(path,base64)", EMP, STR, B64_ZM),
  /** XQuery function */
  APPEND(FNFile.class, "append(path,data[,params])", EMP, 2, STR, ITEM_ZM, NOD),
  /** XQuery function */
  APPENDBIN(FNFile.class, "append-binary(path,base64)", EMP, STR, B64_ZM),
  /** XQuery function */
  COPY(FNFile.class, "copy(source,target)", EMP, STR, STR),
  /** XQuery function */
  MOVE(FNFile.class, "move(source,target)", EMP, STR, STR),

  /* FNFt functions. */

  /** Database function: searches the full-text index. */
  SEARCH(FNFt.class, "search(node,string)", NOD_ZM, NOD, STR),
  /** Database function: marks the hits of a full-text request. */
  MARK(FNFt.class, "mark(nodes[,tag])", NOD_ZM, 1, NOD_ZM, STR),
  /** Database function: extracts full-text results. */
  EXTRACT(FNFt.class, "extract(nodes[,tag[,length]])", NOD_ZM, 1, ITEM_ZM, STR,
      ITR),
  /** Database function: returns the full-text score. */
  SCORE(FNFt.class, "score(items)", DBL_ZM, ITEM_ZM),

  /* FNHof functions. */

  /** XQuery function. */
  SORTWITH(FNHof.class, "sort-with(lt-fun,seq)", ITEM_ZM,
      FunType.get(new SeqType[] { ITEM, ITEM }, BLN).seq(), ITEM_ZM),
  /** XQuery function. */
  HOFID(FNHof.class, "id(expr)", ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  CONST(FNHof.class, "const(return,ignore)", ITEM_ZM, ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  UNTIL(FNHof.class, "until(pred,func,start)", ITEM_ZM,
      FunType.get(new SeqType[] { ITEM_ZM }, BLN).seq(),
      FunType.get(new SeqType[] { ITEM_ZM }, ITEM_ZM).seq(), ITEM_ZM),
  /** XQuery function. */
  FOLDLEFT1(FNHof.class, "fold-left1(function,non-empty-seq)", ITEM_ZM,
      FunType.get(new SeqType[] { ITEM_ZM, ITEM }, ITEM_ZM).seq(), ITEM_OM),
  /** XQuery Function. */
  ITERATE(FNHof.class, "iterate(fun, seq)", ITEM_ZM, FunType.arity(1).seq(),
      ITEM_ZM),

  /* FNHttp functions (EXPath). */

  /** XQuery function */
  SENDREQUEST(FNHttp.class, "send-request(request[,href,[bodies]])", ITEM_ZM, 1,
      NOD, STR_ZO, ITEM_ZM),

  /* FNSent functions. */

  /** Sentiment function: returns a text sentiment. */
  SENT(FNSent.class, "polarity(string,uri)", DBL, STR, STR),
  /** Sentiment function: returns a normed polarity value. */
  NORMSENT(FNSent.class, "normed-polarity(string,uri)", DBL, STR, STR),

  /* FNUtil functions. */

  /** Utility function: evaluates the specified query. */
  EVAL(FNUtil.class, "eval(string)", ITEM_ZM, STR_ZO),
  /** Utility function: evaluates the specified query file. */
  RUN(FNUtil.class, "run(string)", ITEM_ZM, STR),
  /** Utility function: formats a string using the printf syntax. */
  FORMAT(FNUtil.class, "format(format,item1[,...])", STR, -2, STR, ITEM),
  /** Utility function: returns the memory consumption in mb. */
  MB(FNUtil.class, "mb(expr[,cache])", DBL, 1, ITEM_ZM, BLN),
  /** Utility function: measures the execution time of an expression. */
  MS(FNUtil.class, "ms(expr[,cache])", DBL, 1, ITEM_ZM, BLN),
  /** Utility function: converts a number to a given base. */
  TO_BASE(FNUtil.class, "integer-to-base(num,base)", STR, ITR, ITR),
  /** Utility function: decodes a number from a given base. */
  FRM_BASE(FNUtil.class, "integer-from-base(str,base)", ITR, STR, ITR),
  /** Utility function: calculates the MD5 hash of the given xs:string. */
  MD5(FNUtil.class, "md5(str)", HEX, STR),
  /** Utility function: calculates the SHA1 hash of the given xs:string. */
  SHA1(FNUtil.class, "sha1(str)", HEX, STR),
  /** Utility function: calculates the CRC32 hash of the given xs:string. */
  CRC32(FNUtil.class, "crc32(str)", HEX, STR),
  /** Utility function: gets the bytes from the given xs:base64Binary data. */
  TO_BYTES(FNUtil.class, "to-bytes(base64)", BYT_ZM, B64),

  /* FNZIP functions. */

  /** XQuery function */
  BENTRY(FNZip.class, "binary-entry(path,entry)", B64, STR, STR),
  /** XQuery function */
  TEXTENTRY(FNZip.class, "text-entry(path,entry[,encoding])", STR, 2, STR, STR,
      STR),
  /** XQuery function */
  HTMLENTRY(FNZip.class, "html-entry(path,entry)", NOD, STR, STR),
  /** XQuery function */
  XMLENTRY(FNZip.class, "xml-entry(path,entry)", NOD, STR, STR),
  /** XQuery function */
  ENTRIES(FNZip.class, "entries(path)", ELM, STR),
  /** XQuery function */
  ZIPFILE(FNZip.class, "zip-file(zip)", EMP, ELM),
  /** XQuery function */
  UPDATE(FNZip.class, "update-entries(zip,output)", EMP, ELM, STR);

  /**
   * Mapping between function classes and namespace URIs.
   * If no mapping exists, {@link #FNURI} will be assumed as default mapping.
   */
  private static final HashMap<Class<? extends Fun>, byte[]> URIS =
    new HashMap<Class<? extends Fun>, byte[]>();

  // initialization of class/uri mappings
  static {
    URIS.put(FNDb.class,   DBURI);
    URIS.put(FNFile.class, FILEURI);
    URIS.put(FNFt.class,   FTURI);
    URIS.put(FNHof.class,  HOFURI);
    URIS.put(FNHttp.class, HTTPURI);
    URIS.put(FNMap.class,  MAPURI);
    URIS.put(FNMath.class, MATHURI);
    URIS.put(FNSent.class, SENTURI);
    URIS.put(FNUtil.class, UTILURI);
    URIS.put(FNZip.class,  ZIPURI);
  }

  /** Function classes. */
  final Class<? extends Fun> func;
  /** Descriptions. */
  final String desc;
  /** Minimum number of arguments. */
  public final int min;
  /** Maximum number of arguments. */
  public final int max;
  /** Argument types. */
  public final SeqType[] args;
  /** Return type. */
  final SeqType ret;

  /**
   * Default constructor.
   * @param fun function class
   * @param dsc description
   * @param r return type
   * @param typ arguments types
   */
  private FunDef(final Class<? extends Fun> fun, final String dsc,
      final SeqType r, final SeqType... typ) {
    this(fun, dsc, r, typ.length, typ);
  }

  /**
   * Full constructor for functions with multiple signatures.
   * @param fun function class
   * @param dsc description
   * @param r return type
   * @param m minimum number of arguments; if the value is negative,
   * the function can have a variable number of maximum arguments
   * @param typ arguments types
   */
  private FunDef(final Class<? extends Fun> fun, final String dsc,
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
   * @param ii input info
   * @param arg arguments
   * @return function
   */
  public Fun get(final InputInfo ii, final Expr... arg) {
    return (Fun) Reflect.get(Reflect.find(
        func, InputInfo.class, FunDef.class, Expr[].class), ii, this, arg);
  }

  /**
   * Returns the namespace URI of this function.
   * @return function
   */
  public byte[] uri() {
    final byte[] u = URIS.get(func);
    return u == null ? FNURI : u;
  }

  /**
   * Returns the function type of this function with the given arity.
   * @param arity number of arguments
   * @return function type
   */
  public FunType type(final int arity) {
    final SeqType[] arg = new SeqType[arity];
    if(arity != 0 && max == Integer.MAX_VALUE) {
      System.arraycopy(args, 0, arg, 0, args.length);
      final SeqType var = args[args.length - 1];
      for(int i = args.length; i < arg.length; i++) arg[i] = var;
    } else {
      System.arraycopy(args, 0, arg, 0, arity);
    }
    return FunType.get(arg, ret);
  }

  @Override
  public final String toString() {
    final byte[] pre = NSGlobal.prefix(uri());
    return new TokenBuilder(pre).add(':').add(desc).toString();
  }
}
