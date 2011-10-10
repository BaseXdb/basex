package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.item.SeqType.*;

import java.util.HashMap;

import org.basex.query.expr.Expr;
import org.basex.query.item.FuncType;
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
public enum Function {

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
  NORMSPC(FNAcc.class, "normalize-space([string])", STR, 0, STR_ZO),
  /** XQuery function. */
  URIQNM(FNAcc.class, "namespace-uri-from-QName(qname)", URI_ZO, QNM_ZO),

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
      FuncType.get(new SeqType[] { ITEM }, BLN).seq(), ITEM_ZM),
  /** XQuery function. */
  FUNCNAME(FNFunc.class, "function-name(function)", QNM_ZO,
      FuncType.ANY_FUN.seq()),
  /** XQuery function. */
  FUNCARITY(FNFunc.class, "function-arity(function)", ITR,
      FuncType.ANY_FUN.seq()),
  /** XQuery function. */
  MAP(FNFunc.class, "map(function,seq)", ITEM_ZM,
      FuncType.get(new SeqType[] { ITEM }, ITEM_ZM).seq(), ITEM_ZM),
  /** XQuery function. */
  MAPPAIRS(FNFunc.class, "map-pairs(function,seq1,seq2)", ITEM_ZM,
      FuncType.get(new SeqType[] { ITEM, ITEM }, ITEM_ZM).seq(),
      ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  FOLDLEFT(FNFunc.class, "fold-left(function,zero,seq)", ITEM_ZM,
      FuncType.get(new SeqType[] { ITEM_ZM, ITEM }, ITEM_ZM).seq(),
      ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  FOLDRIGHT(FNFunc.class, "fold-right(function,zero,seq)", ITEM_ZM,
      FuncType.get(new SeqType[] { ITEM, ITEM_ZM }, ITEM_ZM).seq(),
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
  SERIALIZE(FNGen.class, "serialize(node[,params])", STR, 1, ITEM_ZM, ITEM),

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
  ENVAR(FNInfo.class, "environment-variable(string)", STR_ZO, STR),
  /** XQuery function. */
  ENVARS(FNInfo.class, "available-environment-variables()", STR_ZM),

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
  /** XQuery function. */
  PATH(FNNode.class, "path([node])", STR_ZO, 0, NOD_ZO),

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
  MAPENTRY(FNMap.class, "entry(key,value)", MAP_O, AAT, ITEM_ZM),
  /** XQuery Function. */
  MAPGET(FNMap.class, "get(map,key)", ITEM_ZM, MAP_O, AAT),
  /** XQuery Function. */
  MAPCONT(FNMap.class, "contains(map,key)", BLN, MAP_O, AAT),
  /** XQuery Function. */
  MAPREM(FNMap.class, "remove(map,key)", MAP_O, MAP_O, AAT),
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
  RANDOM(FNMath.class, "random()", DBL),
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
  DBOPEN(FNDb.class, "open(database[,path])", NOD_ZM, 1, STR, STR),
  /** Database function: opens a specific database node. */
  DBOPENPRE(FNDb.class, "open-pre(database,pre)", NOD_ZM, ITEM, ITR),
  /** Database function: opens a specific database node. */
  DBOPENID(FNDb.class, "open-id(database,id)", NOD_ZM, ITEM, ITR),
  /** Database function: searches the text index. */
  DBTEXT(FNDb.class, "text(database,string)", NOD_ZM, ITEM, ITEM),
  /** Database function: searches the attribute index. */
  DBATTR(FNDb.class, "attribute(database,string[,name])", NOD_ZM, 2,
      ITEM, ITEM, STR),
  /** Database function: searches the full-text index. */
  DBFULLTEXT(FNDb.class, "fulltext(database,string)", NOD_ZM, ITEM, STR),
  /** Database function: lists all database. */
  DBLIST(FNDb.class, "list([database[,path]])", STR_ZM, 0, STR, STR),
  /** Database function: lists system information. */
  DBSYSTEM(FNDb.class, "system()", STR),
  /** Database function: returns database or index information. */
  DBINFO(FNDb.class, "info(database[,type])", STR, 1, ITEM, STR),
  /** Database function: returns the node ids of database nodes. */
  DBNODEID(FNDb.class, "node-id(nodes)", ITR_ZM, NOD_ZM),
  /** Database function: returns the pre values of database nodes. */
  DBNODEPRE(FNDb.class, "node-pre(nodes)", ITR_ZM, NOD_ZM),
  /** Database function: sends result to connected clients. */
  DBEVENT(FNDb.class, "event(name,query)", EMP, STR, ITEM_ZM),
  /** Database function: add document(s) to a database. */
  DBADD(FNDb.class, "add(database,nodes[,name[,path]])", EMP, 2,
      STR, DOC_ZM, STR, STR),
  /** Database function: delete document(s) from a database. */
  DBDELETE(FNDb.class, "delete(database,path)", EMP, ITEM, STR),
  /** Database function: rename document(s). */
  DBRENAME(FNDb.class, "rename(database,path,newpath)", EMP, STR, STR, STR),
  /** Database function: replace document(s). */
  DBREPLACE(FNDb.class, "replace(database,path,item)", EMP, ITEM, STR, ITEM),
  /** Database function: optimize database structures. */
  DBOPTIMIZE(FNDb.class, "optimize(name[,all])", EMP, 1, STR, BLN),
  /** Database function: retrieves binary data. */
  DBRETRIEVE(FNDb.class, "retrieve(database,path)", RAW, STR, STR),
  /** Database function: stores binary data. */
  DBSTORE(FNDb.class, "store(database,path,value)", EMP, STR, STR, ITEM),
  /** Database function: checks if the specified resource is an xml document. */
  DBISXML(FNDb.class, "is-xml(database,path)", BLN, STR, STR),
  /** Database function: checks if the specified resource is a raw file. */
  DBISRAW(FNDb.class, "is-raw(database,path)", BLN, STR, STR),
  /** Database function: checks if the specified database or resource exists. */
  DBEXISTS(FNDb.class, "exists(database[,path])", BLN, 1, STR, STR),
  /** Database function: returns the content type of a database file. */
  DBCTYPE(FNDb.class, "content-type(database,path)", STR, STR, STR),

  /* FNFile functions (EXPath). */

  /** XQuery function */
  FLEXISTS(FNFile.class, "exists(path)", BLN, STR),
  /** XQuery function */
  FLISDIR(FNFile.class, "is-directory(path)", BLN, STR),
  /** XQuery function */
  FLISFILE(FNFile.class, "is-file(path)", BLN, STR),
  /** XQuery function */
  FLLASTMOD(FNFile.class, "last-modified(path)", DTM, STR),
  /** XQuery function */
  FLSIZE(FNFile.class, "size(path)", ITR, STR),
  /** XQuery function */
  FLBASENAME(FNFile.class, "base-name(path[,suffix])", STR, 1, STR, STR),
  /** XQuery function */
  FLDIRNAME(FNFile.class, "dir-name(path)", STR, STR),
  /** XQuery function */
  FLPATHNATIVE(FNFile.class, "path-to-native(path)", STR, STR),
  /** XQuery function */
  FLPATHTOURI(FNFile.class, "path-to-uri(path)", URI, STR),
  /** XQuery function */
  FLRESOLVEPATH(FNFile.class, "resolve-path(path)", STR, STR),
  /** XQuery function */
  FLLIST(FNFile.class, "list(path[,recursive[,pattern]])", STR_ZM, 1, STR, BLN,
      STR),
  /** XQuery function */
  FLCREATEDIR(FNFile.class, "create-directory(path)", EMP, STR),
  /** XQuery function */
  FLDELETE(FNFile.class, "delete(path)", EMP, STR),
  /** XQuery function */
  FLREADTEXT(FNFile.class, "read-text(path[,encoding])", STR, 1, STR, STR),
  /** XQuery function */
  FLREADLINES(FNFile.class, "read-text-lines(path[,encoding])",
      STR_ZM, 1, STR, STR),
  /** XQuery function */
  FLREADBIN(FNFile.class, "read-binary(path)", RAW, STR),
  /** XQuery function */
  FLWRITE(FNFile.class, "write(path,data[,params])", EMP, 2, STR, ITEM_ZM, NOD),
  /** XQuery function */
  FLWRITEBIN(FNFile.class, "write-binary(path,item)", EMP, STR, ITEM_ZM),
  /** XQuery function */
  FLAPPEND(FNFile.class, "append(path,data[,params])", EMP, 2,
      STR, ITEM_ZM, NOD),
  /** XQuery function */
  FLAPPENDBIN(FNFile.class, "append-binary(path,item)", EMP, STR, ITEM_ZM),
  /** XQuery function */
  FLCOPY(FNFile.class, "copy(source,target)", EMP, STR, STR),
  /** XQuery function */
  FLMOVE(FNFile.class, "move(source,target)", EMP, STR, STR),

  /* FNSql functions. */

  /** XQuery function */
  SQLINIT(FNSql.class, "init(class)", EMP, 1, STR),
  /** XQuery function */
  SQLCONNECT(FNSql.class, "connect(url[,user[,pass[,options]]]]])",
      ITR, 1, STR, STR, STR, NOD_ZO),
  /** XQuery function */
  SQLPREPARE(FNSql.class, "prepare(id,statement)", ITR, ITR, STR),
  /** XQuery function */
  SQLEXECUTE(FNSql.class, "execute(id[,item])", ELM_ZM, 1, ITR, ITEM_ZO),
  /** XQuery function */
  SQLCLOSE(FNSql.class, "close(id)", EMP, ITR),
  /** XQuery function */
  SQLCOMMIT(FNSql.class, "commit(id)", EMP, ITR),
  /** XQuery function */
  SQLROLLBACK(FNSql.class, "rollback(id)", EMP, ITR),

  /* FNFt functions. */

  /** Database function: searches the full-text index. */
  FTSEARCH(FNFt.class, "search(node,string)", NOD_ZM, NOD, STR),
  /** Database function: counts the hits of a full-text request. */
  FTCOUNT(FNFt.class, "count(nodes)", ITR, NOD_ZM),
  /** Database function: marks the hits of a full-text request. */
  FTMARK(FNFt.class, "mark(nodes[,tag])", NOD_ZM, 1, NOD_ZM, STR),
  /** Database function: extracts full-text results. */
  FTEXTRACT(FNFt.class, "extract(nodes[,tag[,length]])", NOD_ZM, 1,
      ITEM_ZM, STR, ITR),
  /** Database function: returns the full-text score. */
  FTSCORE(FNFt.class, "score(items)", DBL_ZM, ITEM_ZM),

  /* FNHof functions. */

  /** XQuery function. */
  HOFSORTWITH(FNHof.class, "sort-with(lt-fun,seq)", ITEM_ZM,
      FuncType.get(new SeqType[] { ITEM, ITEM }, BLN).seq(), ITEM_ZM),
  /** XQuery function. */
  HOFID(FNHof.class, "id(expr)", ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  HOFCONST(FNHof.class, "const(return,ignore)", ITEM_ZM, ITEM_ZM, ITEM_ZM),
  /** XQuery function. */
  HOFUNTIL(FNHof.class, "until(pred,func,start)", ITEM_ZM,
      FuncType.get(new SeqType[] { ITEM_ZM }, BLN).seq(),
      FuncType.get(new SeqType[] { ITEM_ZM }, ITEM_ZM).seq(), ITEM_ZM),
  /** XQuery function. */
  HOFFOLDLEFT(FNHof.class, "fold-left1(function,non-empty-seq)", ITEM_ZM,
      FuncType.get(new SeqType[] { ITEM_ZM, ITEM }, ITEM_ZM).seq(), ITEM_OM),
  /** XQuery Function. */
  HOFITERATE(FNHof.class, "iterate(fun, seq)", ITEM_ZM,
      FuncType.arity(1).seq(), ITEM_ZM),

  /* FNCrypto functions (EXPath Cryptographic module). */

  /** Create message authentication code (HMAC). */
  CRYPHMAC(FNCrypto.class, "hmac(string,string,string[,string])", STR, 3, STR,
      STR, STR, STR_ZO),
  /** Encrypt message. */
  CRYPENCRYPT(FNCrypto.class, "encrypt(string, string, string, string)", STR,
      STR, STR, STR, STR),
  /** Decrypt message. */
  CRYPDECRYPT(FNCrypto.class, "decrypt(string, string, string, string)", STR,
      STR, STR, STR, STR),
  /** Generate signature. */
  CRYPGENSIG(FNCrypto.class,
      "generate-signature" +
      "(node,string,string,string,string,string[,item][,item])",
      NOD, 6, NOD, STR, STR, STR, STR, STR, ITEM_ZO, ITEM_ZO),
  /** Validate signature. */
  CRYPVALSIG(FNCrypto.class, "validate-signature(node)", BLN, NOD),

  /* FNHttp functions (EXPath). */

  /** XQuery function */
  HTTPSENDREQUEST(FNHttp.class, "send-request(request[,href,[bodies]])",
      ITEM_ZM, 1, NOD, STR_ZO, ITEM_ZM),

  /* FNJson functions. */

  /** JSON function: convert JSON to XML. */
  JSONPARSE(FNJson.class, "parse(string)", NOD, STR),
  /** JSON function: convert JSON to XML. */
  JSONPARSEML(FNJson.class, "parse-ml(string)", NOD, STR),
  /** JSON function: convert XML to JSON. */
  JSONSER(FNJson.class, "serialize(node)", STR, NOD),
  /** JSON function: convert XML to JsonML. */
  JSONSERML(FNJson.class, "serialize-ml(node)", STR, NOD),

  /* FNSent functions. */

  /** Sentiment function: returns a text sentiment. */
  SENTPOL(FNSent.class, "polarity(string,uri)", DBL, STR, STR),
  /** Sentiment function: returns a normed polarity value. */
  SENTNORM(FNSent.class, "normed-polarity(string,uri)", DBL, STR, STR),

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
  /** Utility function: calculates the MD5 hash of the given string. */
  MD5(FNUtil.class, "md5(str)", HEX, STR),
  /** Utility function: calculates the SHA1 hash of the given string. */
  SHA1(FNUtil.class, "sha1(str)", HEX, STR),
  /** Utility function: calculates the CRC32 hash of the given string. */
  CRC32(FNUtil.class, "crc32(str)", HEX, STR),
  /** Utility function: gets the bytes from the given data data. */
  TO_BYTES(FNUtil.class, "to-bytes(item)", BYT_ZM, ITEM),
  /** Utility function: converts the specified bytes to a string. */
  TO_STRING(FNUtil.class, "to-string(item[,encoding])", STR, 1, ITEM, STR),
  /** Utility function: returns a random unique id. */
  UUID(FNUtil.class, "uuid()", STR),

  /* FNXslt functions. */

  /** XSLT function: performs an XSLT transformation. */
  TRANSFORM(FNXslt.class, "transform(input,stylesheet[,params])",
      NOD, 2, ITEM, ITEM, NOD_ZO),

  /* FNZip functions (EXPath). */

  /** XQuery function */
  ZIPBIN(FNZip.class, "binary-entry(path,entry)", B64, STR, STR),
  /** XQuery function */
  ZIPTEXT(FNZip.class, "text-entry(path,entry[,encoding])", STR, 2, STR, STR,
      STR),
  /** XQuery function */
  ZIPHTML(FNZip.class, "html-entry(path,entry)", NOD, STR, STR),
  /** XQuery function */
  ZIPXML(FNZip.class, "xml-entry(path,entry)", NOD, STR, STR),
  /** XQuery function */
  ZIPENTRIES(FNZip.class, "entries(path)", ELM, STR),
  /** XQuery function */
  ZIPFILE(FNZip.class, "zip-file(zip)", EMP, ELM),
  /** XQuery function */
  ZIPUPDATE(FNZip.class, "update-entries(zip,output)", EMP, ELM, STR);

  /**
   * Mapping between function classes and namespace URIs.
   * If no mapping exists, {@link #FNURI} will be assumed as default mapping.
   */
  private static final HashMap<Class<? extends FuncCall>, byte[]> URIS =
    new HashMap<Class<? extends FuncCall>, byte[]>();

  // initialization of class/uri mappings and statically known modules
  static {
    // W3 functions
    URIS.put(FNMap.class,  MAPURI);
    URIS.put(FNMath.class, MATHURI);
    // EXPath functions
    URIS.put(FNCrypto.class, CRYPTOURI);
    URIS.put(FNFile.class, FILEURI);
    URIS.put(FNHttp.class, HTTPURI);
    URIS.put(FNZip.class,  ZIPURI);
    // internal functions
    URIS.put(FNDb.class,   DBURI);
    URIS.put(FNFt.class,   FTURI);
    URIS.put(FNHof.class,  HOFURI);
    URIS.put(FNJson.class, JSONURI);
    URIS.put(FNSent.class, SENTURI);
    URIS.put(FNSql.class, SQLURI);
    URIS.put(FNUtil.class, UTILURI);
    URIS.put(FNXslt.class, XSLTURI);
  }

  /** Function classes. */
  final Class<? extends FuncCall> func;
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
  private Function(final Class<? extends FuncCall> fun,
      final String dsc, final SeqType r, final SeqType... typ) {
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
  private Function(final Class<? extends FuncCall> fun,
      final String dsc, final SeqType r, final int m, final SeqType... typ) {

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
  public FuncCall get(final InputInfo ii, final Expr... arg) {
    return (FuncCall) Reflect.get(Reflect.find(
        func, InputInfo.class, Function.class, Expr[].class), ii, this, arg);
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
  public FuncType type(final int arity) {
    final SeqType[] arg = new SeqType[arity];
    if(arity != 0 && max == Integer.MAX_VALUE) {
      System.arraycopy(args, 0, arg, 0, args.length);
      final SeqType var = args[args.length - 1];
      for(int i = args.length; i < arg.length; i++) arg[i] = var;
    } else {
      System.arraycopy(args, 0, arg, 0, arity);
    }
    return FuncType.get(arg, ret);
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
  public String args(final Object... arg) {
    final TokenBuilder tb = new TokenBuilder();
    for(final Object a : arg) {
      if(tb.size() != 0) tb.add(',');
      final String s = a.toString();
      if(s.matches("^([\\w\\d-_:\\.]*\\(|<|\"|\\$| ).*") ||
          a instanceof Integer) {
        tb.add(s);
      } else if(a instanceof Boolean) {
        tb.add(s + "()");
      } else {
        tb.add("\"" + s.replaceAll("\"", "\"\"") + "\"");
      }
    }
    return toString().replaceAll("\\(.*", "(") + tb + ")";
  }

  @Override
  public final String toString() {
    final byte[] pre = NSGlobal.prefix(uri());
    return new TokenBuilder(pre).add(':').add(desc).toString();
  }
}
