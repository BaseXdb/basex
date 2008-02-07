package org.basex.query.pf;

import static org.basex.util.Token.*;

/**
 * This class assembles texts which are used in the Pathfinder expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
interface PFT {
  
  // PATHFINDER ARGUMENTS =====================================================
  
  /** Pathfinder arguments for XML output. */
  String PFARGS = "-AXls16 -f+";
  /** Pathfinder arguments for dot output. */
  String PFDOTARGS = "-ADls16";
  
  // XPATH STEPS ==============================================================
  
  /** XPath query. */
  byte[] XPNODES = token("logical_query_plan/node");
  /** XPath query. */
  byte[] XPEDGE = token("edge/@to");
  /** XPath query. */
  byte[] XPCOL = token("content/column");
  /** XPath query. */
  byte[] XPVAL = token("value/text()");
  /** XPath query. */
  byte[] XPTYPE = token("value/@type");
  /** XPath query. */
  byte[] XPCTYPE = token("content/type/@name");
  /** XPath query. */
  byte[] XPNEW = token("content/column[@new = 'true']");
  /** XPath query. */
  byte[] XPOLD = token("content/column[@new = 'false']");
  /** XPath query. */
  byte[] XPITEM = token("content/column[@function = 'item']");
  /** XPath query. */
  byte[] XPITER = token("content/column[@function = 'iter']");
  /** XPath query. */
  byte[] XPPART = token("content/column[@function ='partition']");
  /** XPath query. */
  byte[] XPPOS = token("content/column[@position]");
  /** XPath query. */
  byte[] XPPOS1 = token("content/column[@position ='1']");
  /** XPath query. */
  byte[] XPPOS2 = token("content/column[@position ='2']");
  /** XPath query. */
  byte[] XPKIND = token("content/kind/@name");
  /** XPath query. */
  byte[] XPSTEP = token("content/step");

  // ATTRIBUTES ===============================================================
  
  /** Attribute name. */
  byte[] ID = token("id");
  /** Attribute name. */
  byte[] KIND = token("kind");
  /** Attribute name. */
  byte[] NAME = token("name");
  /** Attribute name. */
  byte[] OLDNAME = token("old_name");
  /** Attribute name. */
  byte[] TYPE = token("type");
  /** Attribute name. */
  byte[] AXIS = token("axis");
  /** Item column. */
  byte[] ITEM = token("item");

  // OPERATORS ================================================================

  /** Pathfinder operator. */
  byte[] ALL = token("all");
  /** Pathfinder operator. */
  byte[] AND = token("and");
  /** Pathfinder operator. */
  byte[] ATR = token("attribute_construction");
  /** Pathfinder operator. */
  byte[] AVG = token("avg");
  /** Pathfinder operator. */
  byte[] ACH = token("attach");
  /** Pathfinder operator. */
  byte[] CST = token("cast");
  /** Pathfinder operator. */
  byte[] CNT = token("count");
  /** Pathfinder operator. */
  byte[] CRS = token("cross");
  /** Pathfinder operator. */
  byte[] DIF = token("difference");
  /** Pathfinder operator. */
  byte[] DOC = token("fn:doc");
  /** Pathfinder operator. */
  byte[] DOA = token("documentnode_construction");
  /** Pathfinder operator. */
  byte[] DST = token("distinct");
  /** Pathfinder operator. */
  byte[] ELM = token("element_construction");
  /** Pathfinder operator. */
  byte[] EQU = token("eq");
  /** Pathfinder operator. */
  byte[] EQJ = token("eqjoin");
  /** Pathfinder operator. */
  byte[] ERR = token("error");
  /** Pathfinder operator. */
  byte[] FRE = token("EMPTY_FRAG");
  /** Pathfinder operator. */
  byte[] FRU = token("FRAG_UNION");
  /** Pathfinder operator. */
  byte[] FRG = token("FRAG");
  /** Pathfinder operator. */
  byte[] FUN = token("fun");
  /** Pathfinder operator. */
  byte[] GRT = token("gt");
  /** Pathfinder operator. */
  byte[] ISC = token("intersect");
  /** Pathfinder operator. */
  byte[] MAX = token("max");
  /** Pathfinder operator. */
  byte[] MRG = token("#pf:merge-adjacent-text-nodes");
  /** Pathfinder operator. */
  byte[] MIN = token("min");
  /** Pathfinder operator. */
  byte[] NOT = token("not");
  /** Pathfinder operator. */
  byte[] NUM = token("number");
  /** Pathfinder operator. */
  byte[] ORR = token("or");
  /** Pathfinder operator. */
  byte[] PRJ = token("project");
  /** Pathfinder operator. */
  byte[] ROO = token("ROOTS");
  /** Pathfinder operator. */
  byte[] RON = token("rownum");
  /** Pathfinder operator. */
  byte[] SCJ = token("XPath step");
  /** Pathfinder operator. */
  byte[] SEL = token("select");
  /** Pathfinder operator. */
  byte[] SMJ = token("semijoin");
  /** Pathfinder operator. */
  byte[] SEQ = token("seqty1");
  /** Pathfinder operator. */
  byte[] SER = token("serialize");
  /** Pathfinder operator... correct? */
  byte[] STV = token("#pf:string-value");
  /** Pathfinder operator. */
  byte[] STJ = token("fn:string-join");
  /** Pathfinder operator. */
  byte[] SUM = token("sum");
  /** Pathfinder operator. */
  byte[] TAB = token("table");
  /** Pathfinder operator. */
  byte[] TAG = token("element_tagname");
  /** Pathfinder operator. */
  byte[] TAS = token("type assertion");
  /** Pathfinder operator. */
  byte[] TBE = token("empty_tbl");
  /** Pathfinder operator. */
  byte[] TXT = token("textnode_construction");
  /** Pathfinder operator. */
  byte[] TRP = token("trace map");
  /** Pathfinder operator. */
  byte[] TRM = token("trace msg");
  /** Pathfinder operator. */
  byte[] TRC = token("trace");
  /** Pathfinder operator. */
  byte[] TYP = token("type");
  /** Pathfinder operator. */
  byte[] UNI = token("union");

  // FUNCTIONS ================================================================

  /** Addition function. */
  String FPLUS = "add";
  /** Subtraction function. */
  String FMINUS = "subtract";
  /** Multiplication function. */
  String FMULT = "multiply";
  /** Division function. */
  String FDIV = "divide";
  /** Module function. */
  String FMOD = "modulo";
  /** Abs function. */
  String FABS = "fn:abs";
  /** Concat function. */
  String FCONC = "fn:concat";
  /** Contains function. */
  String FCONT = "fn:contains";
  /** Ceiling function. */
  String FCEIL = "fn:ceiling";
  /** Floor function. */
  String FFLOOR = "fn:floor";
  /** Round function. */
  String FROUND = "fn:round";
  /** Number function. */
  String FNUMB = "fn:number";
  
  // DATA TYPES ===============================================================

  /** Integer data type. */
  int INT = 1;
  /** Pre data. */
  int PRE = 2;
  /** Double data. */
  int DBL = 4;
  /** Boolean data. */
  int BLN = 8;
  /** String data. */
  int STR = 16;

  /** Data type. */
  byte[] TINT = token("int");
  /** Data type. */
  byte[] TNAT = token("nat");
  /** Data type. */
  byte[] TDBL = token("dbl");
  /** Data type. */
  byte[] TDEC = token("dec");
  /** Data type. */
  byte[] TBLN = token("bool");
  /** Data type. */
  byte[] TSTR = token("str");
  /** Data type. */
  byte[] TNODE = token("node");
  /** Data type. */
  byte[] TQNAME = token("qname");
  /** Data type. */
  byte[] TUA = token("uA");

  // AXES =====================================================================

  /** Axis step. */
  byte[] AANCORSELF = token("anc-or-self");
  /** Axis step. */
  byte[] AANCESTOR = token("ancestor");
  /** Axis step. */
  byte[] AATTRIBUTE = token("attribute");
  /** Axis step. */
  byte[] ACHILD = token("child");
  /** Axis step. */
  byte[] ADESCORSELF = token("desc-or-self");
  /** Axis step. */
  byte[] ADESCENDANT = token("descendant");
  /** Axis step. */
  byte[] AFOLSIBLING = token("fol-sibling");
  /** Axis step. */
  byte[] AFOLLOWING = token("following");
  /** Axis step. */
  byte[] APARENT = token("parent");
  /** Axis step. */
  byte[] APRECSIBLING = token("prec-sibling");
  /** Axis step. */
  byte[] APRECEDING = token("preceding");
  /** Axis step. */
  byte[] ASELF = token("self");

  // NODE TESTS ===============================================================

  /** Axis step. */
  byte[] KNODE = token("node");
  /** Axis step. */
  byte[] KATTR = token("attribute");
  /** Axis step. */
  byte[] KELEM = token("element");
  /** Axis step. */
  byte[] KTEXT = token("text");
  /** Axis step. */
  byte[] KCOMM = token("comment");
  /** Axis step. */
  byte[] KPI = token("processing-instruction");

  // PATHFINDER ERRORS ========================================================

  /** Pathfinder error. */
  String PFERROR = "Sorry, query is not supported yet.";

  /** Pathfinder error. */
  String PFIMPL = " not implemented yet.";
  /** Pathfinder error. */
  String PFFUN = "Function %()";
  /** Pathfinder error. */
  String PFFUNC = "()" + PFIMPL;
  /** Pathfinder error. */
  String PFCONS = "This node construction is";
  /** Pathfinder error. */
  String PFDOC = "Doc node construction";
  /** Pathfinder error. */
  String PFAXIS = "Axis '%'";
  /** Pathfinder error. */
  String PFTEST = "Node test '%'";
  /** Pathfinder error. */
  String PFTYPE = "Data type '%'";
  /** Pathfinder error. */
  String PFSIMPLE = "'%'";
  /** Pathfinder error. */
  String PFPARSE = "pathfinder ";
  /** Pathfinder error. */
  String PF404 = "error=2";
  /** Pathfinder error. */
  String PFPATH = "Pathfinder not found; please check path in .basex";
  /** Pathfinder error. */
  String PFDOTTY = "Dotty not found; please check path in .basex";
  /** Pathfinder error. */
  String PFEQJ = "EQJoin for several rows";
  /** Pathfinder error. */
  String PFDB = "Database '%' not found; create new database.";
  
  // DEBUGGING ================================================================

  /** Debugging info. */
  String DBGSEND  = "Sending query : %";
  /** Debugging info. */
  String DBGINPUT = "Getting input : %";
  /** Debugging info. */
  String DBGTABLE = "Creating table: %\n";
  /** Debugging info. */
  String DBGDAG = "Show DAG...";
}
