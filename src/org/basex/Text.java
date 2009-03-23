package org.basex;

import static org.basex.core.Commands.*;
import static org.basex.core.Lang.*;
import static org.basex.util.Token.*;
import org.basex.core.Lang;

/**
 * This class contains internationalized text strings, which are used
 * throughout the project. If this class is called first, the Strings
 * are initialized by the {@link Lang} class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Andreas Weiler
 */
public interface Text {

  // FREQUENTLY USED CHARACTERS ===============================================

  /** New Line. */
  String NL = org.basex.core.Prop.NL;
  /** Colon. */
  String COL = ":";
  /** Colon/space. */
  String COLS = ": ";
  /** Dot. */
  String DOT = ".";
  /** Dots. */
  String DOTS = "...";
  /** List. */
  String LI = "- ";
  /** Space. */
  String S = " ";
  /** On flag. */
  String ON = "ON";
  /** Off flag. */
  String OFF = "OFF";
  /** All flag. */
  String ALL = "ALL";

  /** Project name. */
  String NAME = "BaseX";
  /** Project namespace. */
  String NAMESPACE = NAME.toLowerCase();
  /** URL. */
  String URL = "http://www." + NAMESPACE + ".org";
  /** Mail. */
  String MAIL = "info@" + NAMESPACE + ".org";
  /** Code version. */
  String VERSION = "5.5";
  /** Company Info. */
  String COMPANY = "DBIS, University of Konstanz";
  /** Version Information. */
  String VERSINFO = BaseX.info(lang("version"), VERSION);

  /** BaseX title and version. */
  String TITLE = NAME + S + VERSION;
  /** BaseX Text. */
  String CONSOLEINFO = TITLE + "; " + COMPANY;

  // CONSOLE INFO =============================================================

  /** Console Text. */
  String CONSOLE = TITLE + " [%]" + NL + lang("help_intro") + NL;

  /** Good-Bye Information. */
  String[] CLIENTBYE = {
      lang("bye1"), lang("bye2"), lang("bye3"), lang("bye4")
  };

  /** Local (Standalone) Mode. */
  String LOCALMODE = lang("cs_local");
  /** Client Mode. */
  String CLIENTMODE = lang("cs_client");
  /** Continued prompt. */
  String INPUTCONT = DOTS + S;

  /** Start Information. */
  String CLIENTINFO = CONSOLEINFO + NL +
    "Usage: BaseXClient [options] [query]" + NL +
    "  -d         debug mode" + NL +
    "  -o [file]  specify output file" + NL +
    "  -p<port>   specify server port" + NL +
    "  -q<cmd>    send BaseX commands" + NL +
    "  -s<name>   specify server name" + NL +
    "  -v/V       show (all) process info" + NL +
    "  -x         print result as xml" + NL +
    "  -z         skip query output";

  /** Start Information. */
  String LOCALINFO = CONSOLEINFO + NL +
    "Usage: BaseX [options] [query]" + NL +
    "  [query]    specify query file" + NL +
    "  -c         chop whitespaces" + NL +
    "  -d         debug mode" + NL +
    "  -e         skip entity parsing" + NL +
    "  -o [file]  specify output file" + NL +
    "  -q<cmd>    send BaseX commands" + NL +
    "  -v/V       show (all) process info" + NL +
    "  -x         print result as xml" + NL +
    "  -z         skip query output";

  // SERVER ===================================================================

  /** Server Start. */
  String SERVERSTART = NL + TITLE + " Server " + lang("srv_start");
  /** Server Stop. */
  String SERVERSTOPPED = "Server " + lang("srv_stop");
  /** Server started. */
  String SERVERBIND = lang("srv_bind");
  /** Server timeout. */
  String SERVERTIME = lang("srv_timeout");
  /** Connection error. */
  String SERVERERR = lang("srv_connect");
  /** Port error. */
  String SERVERPORT = lang("srv_port") + COLS;

  /** Start Information. */
  String SERVERINFO = CONSOLEINFO + NL +
    "Usage: java BaseXServer [options]" + NL +
    " stop     stop server" + NL +
    " -d       debug mode" + NL +
    " -h       show this help" + NL +
    " -p<port> specify server port" + NL +
    " -v       verbose mode";

  // WEB SERVER ===============================================================

  /** Server Start. */
  String WSERVERSTART = NL + TITLE + " WebServer " + lang("srv_start");
  /** Server Stop. */
  String WSERVERSTOPPED = "WebServer " + lang("srv_stop");

  /** Start Information. */
  String WSERVERINFO = CONSOLEINFO + NL +
    "Usage: java BaseXWebServer [options]" + NL +
    " stop     stop server" + NL +
    " -c       cache XQuery requests" + NL +
    " -d       debug mode" + NL +
    " -h       show this help" + NL +
    " -p<port> specify server port" + NL +
    " -v       verbose mode";

  /* COMMANDS =================================================================
   *
   * The command strings must equal the command definitions in {@link Commands}.
   * The help texts are called via reflection to avoid a too early
   * initialization of the {@link Lang} class.
   */

  /** Help String. */
  String NOHELP = lang("ch_nohelp");

  /** Database separator. */
  String DUMMYDATABASE0 = lang("ch_dummydatabase0");

  /** Command help. */
  String CREATE0 = "[" + CmdCreate.DB + "|" + CmdCreate.FS + "|" +
    CmdCreate.INDEX + "] [...]";
  /** Command help. */
  String CREATE1 = lang("ch_create1");
  /** Command help. */
  String CREATE2 = lang("ch_create2") + NL + NL +
    LI + CmdCreate.DB + lang("ch_file") + NL + lang("ch_create3") + NL +
    LI + CmdCreate.FS + lang("ch_create4") + NL + lang("ch_create5") + NL +
    LI + CmdCreate.INDEX + " [" + CmdIndex.TEXT + "|" + CmdIndex.ATTRIBUTE +
    "|" + CmdIndex.FULLTEXT + "]: " + NL + lang("ch_create6");
  /** Command help. */
  String OPEN0 = lang("ch_open0");
  /** Command help. */
  String OPEN1 = lang("ch_open1");
  /** Command help. */
  String OPEN2 = lang("ch_open2");
  /** Command help. */
  String INFO0 = "[" + CmdInfo.DB + "|" + CmdInfo.INDEX + "|" +
    CmdInfo.TABLE + "]?";
  /** Command help. */
  String INFO1 = lang("ch_info1");
  /** Command help. */
  String INFO2 = lang("ch_info21") + NL + lang("ch_info22") + NL +
    LI + CmdInfo.DB + lang("ch_info23") + NL +
    LI + CmdInfo.INDEX + lang("ch_info24") + NL +
    LI + CmdInfo.TABLE + lang("ch_info25");
  /** Command help. */
  String CHECK0 = lang("ch_check0");
  /** Command help. */
  String CHECK1 = lang("ch_check1");
  /** Command help. */
  String CHECK2 = lang("ch_check2");
  /** Command help. */
  String CLOSE0 = "";
  /** Command help. */
  String CLOSE1 = lang("ch_close1");
  /** Command help. */
  String CLOSE2 = lang("ch_close2");
  /** Command help. */
  String LIST0 = "";
  /** Command help. */
  String LIST1 = lang("ch_list1");
  /** Command help. */
  String LIST2 = lang("ch_list2");
  /** Command help. */
  String DROP0 = "[" + CmdDrop.DB + "|" + CmdDrop.INDEX + "] [...]";
  /** Command help. */
  String DROP1 = lang("ch_drop1");
  /** Command help. */
  String DROP2 = lang("ch_drop2") + NL +
    LI + CmdDrop.DB + lang("ch_drop21") + NL + lang("ch_drop22") + NL +
    LI + CmdDrop.INDEX + " [" + CmdIndex.TEXT + "|" + CmdIndex.ATTRIBUTE + "|" +
  CmdIndex.FULLTEXT + "]:" + NL + lang("ch_drop23");
  /** Command help. */
  String OPTIMIZE0 = "";
  /** Command help. */
  String OPTIMIZE1 = lang("ch_optimize1");
  /** Command help. */
  String OPTIMIZE2 = lang("ch_optimize2");
  /** Command help. */
  String EXPORT0 = lang("ch_export0");
  /** Command help. */
  String EXPORT1 = lang("ch_export1");
  /** Command help. */
  String EXPORT2 = lang("ch_export2");

  /** Command help. */
  String DUMMYQUERY0 = lang("ch_dummyquery0");

  /** Command help. */
  String XPATH0 = lang("ch_xpath0");
  /** Command help. */
  String XPATH1 = lang("ch_xpath1");
  /** Command help. */
  String XPATH2 = lang("ch_xpath2");
  /** Command help. */
  String XQUERYMV0 = lang("ch_xquerymv0");
  /** Command help. */
  String XQUERYMV1 = lang("ch_xquerymv1");
  /** Command help. */
  String XQUERYMV2 = lang("ch_xquerymv2");
  /** Command Help. */
  String XQUERY0 = lang("ch_xquery0");
  /** Command Help. */
  String XQUERY1 = lang("ch_xquery1");
  /** Command Help. */
  String XQUERY2 = lang("ch_xquery2");
  /** Command Help. */
  String FIND0 = lang("ch_find0");
  /** Command Help. */
  String FIND1 = lang("ch_find1");
  /** Command Help. */
  String FIND2 = lang("ch_find2");
  /** Command Help. */
  String CS0 = lang("ch_cs0");
  /** Command Help. */
  String CS1 = lang("ch_cs1");
  /** Command Help. */
  String CS2 = lang("ch_cs2");
  /** Command Help. */
  String BASH0 = "";
  /** Command Help. */
  String BASH1 = lang("ch_bash1");
  /** Command Help. */
  String BASH2 = lang("ch_bash2");

  /** Command Help. */
  String DUMMYUPDATE0 = lang("ch_dummyupdate0");

  /** Command Help. */
  String COPY0 = lang("ch_copy0");
  /** Command Help. */
  String COPY1 = lang("ch_copy1");
  /** Command Help. */
  String COPY2 = lang("ch_copy2");
  /** Command Help. */
  String DELETE0 = lang("ch_delete0");
  /** Command Help. */
  String DELETE1 = lang("ch_delete1");
  /** Command Help. */
  String DELETE2 = lang("ch_delete2");

  /** Command Help. */
  String INSERT0 = "[" + CmdUpdate.FRAGMENT + "|" + CmdUpdate.ELEMENT + "|" +
    CmdUpdate.ATTRIBUTE + "|" + CmdUpdate.TEXT + "|" + CmdUpdate.COMMENT + "|" +
    CmdUpdate.PI + "] [...]";
  /** Command Help. */
  String INSERT1 = lang("ch_insert1");
  /** Command Help. */
  String INSERT2 = lang("ch_insert2") + NL + NL +
    LI + CmdUpdate.ELEMENT   + S + lang("ch_insert21") + NL +
    LI + CmdUpdate.TEXT      + S + lang("ch_insert22") + NL +
    LI + CmdUpdate.ATTRIBUTE + S + lang("ch_insert23") + NL +
    LI + CmdUpdate.COMMENT   + S + lang("ch_insert24") + NL +
    LI + CmdUpdate.PI        + S + lang("ch_insert25") + NL +
    LI + CmdUpdate.FRAGMENT  + S + lang("ch_insert26");
  /** Command Help. */
  String UPDATE0 = "[" + CmdUpdate.ELEMENT + "|" + CmdUpdate.ATTRIBUTE + "|" +
    CmdUpdate.TEXT + "|" + CmdUpdate.COMMENT + "|" + CmdUpdate.PI + "] [...]";
  /** Command Help. */
  String UPDATE1 = lang("ch_update1");
  /** Command Help. */
  String UPDATE2 = lang("ch_update2") + NL + NL +
    LI + CmdUpdate.ELEMENT   + S + lang("ch_update21") + NL +
    LI + CmdUpdate.TEXT      + S + lang("ch_update22") + NL +
    LI + CmdUpdate.ATTRIBUTE + S + lang("ch_update23") + NL +
    LI + CmdUpdate.COMMENT   + S + lang("ch_update24") + NL +
    LI + CmdUpdate.PI        + S + lang("ch_update25");

  /** Command Help. */
  String DUMMYGENERAL0 = lang("ch_dummygeneral0");

  /** Command Help. */
  String SET0 = lang("ch_set0");
  /** Command Help. */
  String SET1 = lang("ch_set1");
  /** Command Help. */
  String SET2 = lang("ch_set2") + NL +
    LI + CmdSet.INFO      + lang("ch_set21") + NL +
    LI + CmdSet.DEBUG     + lang("ch_set22") + NL +
    LI + CmdSet.SERIALIZE + lang("ch_set23") + NL +
    LI + CmdSet.XMLOUTPUT + lang("ch_set24") + NL +
    LI + CmdSet.MAINMEM   + lang("ch_set25") + NL +
    LI + CmdSet.CHOP      + lang("ch_set26") + NL +
    LI + CmdSet.ENTITY    + lang("ch_set27") + NL +
    LI + CmdSet.TEXTINDEX + lang("ch_set28") + NL +
    LI + CmdSet.ATTRINDEX + lang("ch_set29") + NL +
    LI + CmdSet.FTINDEX   + lang("ch_set31");

  /** Command Help. */
  String HELP0 = "[command]?";
  /** Command Help. */
  String HELP1 = lang("ch_help1") + S + NAME + lang("ch_help11");
  /** Command Help. */
  String HELP2 = lang("ch_help2");
  /** Command Help. */
  String EXIT0 = "";
  /** Command Help. */
  String EXIT1 = lang("ch_exit1") + S + NAME + DOT;
  /** Command Help. */
  String EXIT2 = EXIT1;
  /** Command Help. */
  String QUIT0 = "";
  /** Command Help. */
  String QUIT1 = EXIT1;
  /** Command Help. */
  String QUIT2 = EXIT2;

  // STARTER WINDOW ===========================================================

  /** Waiting info. */
  String WAIT1 = lang("launch") + S + TITLE;
  /** Waiting info. */
  String WAIT2 = DOTS + lang("wait") + DOTS;

  // PROCESS INFOS ============================================================

  /** Process time. */
  String PROCTIME = lang("proc_time") + ": %";
  /** No Document Warning. */
  String PROCSYNTAX = lang("proc_syntax") + ": %";
  /** Command Execution Error. */
  String PROCERR = lang("proc_err") + COL + NL + "%";
  /** No Database Error. */
  String PROCNODB = lang("proc_nodb");
  /** No Filesystem Error. */
  String PROCNOFS = lang("proc_nofs");
  /** Main Memory Error. */
  String PROCMM = lang("proc_mm");
  /** Out of Memory Error. */
  String PROCOUTMEM = lang("proc_outmem");
  /** Implementation Error. */
  String PROCIMPL = "Not Implemented.";
  /** Implementation Bug. */
  String PROCBUG = "Implementation Bug?";
  /** Implementation Bug. */
  String PROCBUG2 = NL + "You are invited to send this to " + MAIL;

  /** Unknown Command error. */
  String CMDNO = lang("cmd_no");
  /** Unknown Command error. */
  String CMDUNKNOWN = lang("cmd_unknown");
  /** Unknown Command error. */
  String CMDWHICH = CMDUNKNOWN + "; " + lang("help_short") + DOT;
  /** Unknown Command error. */
  String CMDSIMILAR = CMDUNKNOWN + "; " + lang("cmd_similar");
  /** Database Closed. */
  String CMDHELP = lang("help_long");

  /** Database Closed. */
  String PINGINFO = lang("cmd_ping") + NL;

  // CREATE COMMAND ===========================================================

  /** Create Database information. */
  String PROGCREATE = lang("pc_create");
  /** Create Database information. */
  String PROGINDEX = lang("pc_index");
  /** Database Update. */
  String DBUPDATE = lang("pc_update");
  /** Index Update. */
  String INDUPDATE = lang("pc_indupdate");
  /** Builder error. */
  String CANCELCREATE = lang("pc_cancel");
  /** Builder error. */
  String LIMITRANGE = lang("pc_range");
  /** Builder error. */
  String LIMITTAGS = lang("pc_tags");
  /** Builder error. */
  String LIMITATTS = lang("pc_atts");
  /** Create Database information. */
  String NODESPARSED = S + lang("pc_parse");
  /** Scanner Position. */
  String SCANPOS = lang("pc_pos");

  /** Create Database information. */
  String INDEXTXT = lang("pc_indextxt") + DOTS;
  /** Create Database information. */
  String INDEXATT = lang("pc_indexatt") + DOTS;
  /** Create Database information. */
  String INDEXFTX = lang("pc_indexftx") + DOTS;

  /** Database created. */
  String DBCREATED = lang("pc_created");

  /** Create information. */
  String CREATETABLE = lang("pc_tbl");
  /** Parse error. */
  String CREATEERR = lang("pc_err");

  /** File not found. */
  String FILEWHICH = lang("pc_filenf");
  /** Path not found. */
  String PATHWHICH = lang("pc_pathnf");
  /** Missing database name. */
  String DBWHICH = lang("pc_dbnf");

  // DATABASE COMMANDS ========================================================

  /** Database not found. */
  String DBNOTFOUND = lang("db_no");
  /** Database is open. */
  String DBINMEM = lang("db_open");
  /** Database closed. */
  String DBCLOSED = lang("db_closed");
  /** Database not closed. */
  String DBCLOSEERR = lang("db_closeerr");
  /** Database Dropped. */
  String DBDROPPED = lang("db_dropped");
  /** Database not dropped. */
  String DBNOTDROPPED = lang("db_notdropped");
  /** Database not opened. */
  String DBOPENERR = lang("db_notopened");
  /** Database opened. */
  String DBOPENED = lang("db_opened");
  /** Database exported. */
  String DBEXPORTED = lang("db_exported");

  /** Database Optimized. */
  String DBOPTIMIZED = lang("db_optimized");
  /** Database Optimization. */
  String DBOPT1 = "Statistics rebuilt in %." + NL;
  /** Database Optimization. */
  String DBOPTERR1 = "Could not write statistics...";

  /** Index created. */
  String DBINDEXED = lang("in_created");
  /** Index error. */
  String DBINDEXERR = lang("in_err");
  /** Index dropped. */
  String DBDROP = lang("in_dropped");
  /** Index not dropped. */
  String DBDROPERR = lang("in_notdropped");

  // DATABASE/INDEX INFORMATION ===============================================

  /** Index info. */
  String TRIE = LI + "Compressed Trie";
  /** Index info. */
  String NAMINDEX = LI + "Hash Index";
  /** Index info. */
  String FUZZY = LI + "Fuzzy Index";
  /** Index info. */
  String TXTINDEX = LI + "Tree Index";
  /** Index info. */
  String SIZEDISK = LI + "Size on Disk: ";
  /** Index info. */
  String IDXENTRIES = LI + "Entries: ";

  // XPATH COMMAND ============================================================

  /** Query Info: Optimizing. */
  String QUERYPARS = lang("qu_pars") + COL;
  /** Query Info: Optimizing. */
  String QUERYCOMP = lang("qu_comp") + COL;
  /** Query Info: Evaluating. */
  String QUERYEVAL = lang("qu_eval") + COL;
  /** Query Info: Querying. */
  String QUERYTIME = lang("qu_time") + COL;
  /** Query Info: Result . */
  String QUERYRESULT = lang("qu_result") + COLS;
  /** Query Info: Plan. */
  String QUERYPLAN = lang("qu_plan") + COLS;
  /** Query Info: Compiler. */
  String QUERYSEP = LI;
  /** Query Info: Plan. */
  String PLANXML = "plan.xml";
  /** Query Info: Plan. */
  String PLANDOT = "plan.dot";

  /** Query Info: Query. */
  String QUERYSTRING = lang("qu_tabquery") + COLS;
  /** Query Info: Compiling. */
  String QUERYPARSE = lang("qu_tabpars") + COLS;
  /** Query Info: Compiling. */
  String QUERYCOMPILE = lang("qu_tabcomp") + COLS;
  /** Query Info: Evaluating. */
  String QUERYEVALUATE = lang("qu_tabeval") + COLS;
  /** Query Info: Finishing. */
  String QUERYFINISH = lang("qu_tabfinish") + COLS;
  /** Query Info: Time for printing. */
  String QUERYPRINT = lang("qu_tabprint") + COLS;
  /** Query Info: Total Time. */
  String QUERYTOTAL = lang("qu_tabtotal") + COLS;
  /** Query Hits. */
  String QUERYHITS = lang("qu_tabhits") + COLS;
  /** Query Info: Printed Data. */
  String QUERYPRINTED = lang("qu_tabprinted") + COLS;
  /** Query Info: Memory. */
  String QUERYMEM = lang("qu_tabmem") + ": %";
  /** Query Hits. */
  String HITS = lang("qu_hits");
  /** Milliseconds. */
  String MS = S + lang("qu_ms");

  /** Query Hits. */
  String VALHIT = "Item";
  /** Query Hits. */
  String VALHITS = "Items";

  // UPDATE COMMANDS ==========================================================

  /** Insert query info. */
  String QUERYNODESERR = lang("uc_querynodeserr");

  /** Deletion info. */
  String DELETEINFO = lang("uc_deleteinfo");
  /** Deletion error. */
  String DELETEROOT = lang("uc_deleteroot");
  /** Insert query info. */
  String COPYROOT = lang("uc_copyroot");

  /** Copy query info. */
  String COPYINFO = lang("uc_copyinfo");
  /** Insert query info. */
  String INSERTINFO = lang("uc_insertinfo");
  /** Update query info. */
  String UPDATEINFO = lang("uc_updateinfo");
  /** Update node info. */
  String UPDATENODE = lang("uc_updatenode");
  /** Insert query info. */
  String COPYTAGS = lang("uc_copytags");
  /** Insert query info. */
  String INSERTTEXT = lang("uc_inserttext");
  /** Update element info. */
  String ATTINVALID = lang("uc_attinvalid");
  /** Duplicate attribute info. */
  String ATTDUPL = lang("uc_attdupl");
  /** Invalid position. */
  String POSINVALID = lang("uc_posinvalid");
  /** Update PI info. */
  String NAMEINVALID = lang("uc_invalid");
  /** Update text info. */
  String TXTINVALID = lang("uc_txtinvalid");
  /** Update namespaces error. */
  String UPDATENS = lang("uc_namespaces");

  // INFO STRINGS =============================================================

  /** Process information. */
  String INFOWAIT = lang("wait");
  /** Title of Information Dialog. */
  String INFOTITLE = lang("info_head");
  /** Index information. */
  String INFOINDEX = lang("info_index");
  /** Index information. */
  String INFOBUILD = lang("info_build") + DOTS;
  /** Optimize information. */
  String INFOOPT = lang("info_opt") + DOTS;
  /** Optimize information. */
  String INFOOPTIM = lang("info_optim");
  /** Statistics information. */
  String INFOSTATS = lang("info_stats") + DOTS;

  /** Info on source document. */
  String INFODBNAME = lang("info_dbname");
  /** Info on database size. */
  String INFODBSIZE = lang("info_dbsize");
  /** Info on source document. */
  String INFODOC = lang("info_doc");
  /** Info on database time stamp. */
  String INFOTIME = lang("info_time");
  /** Info on number of documents. */
  String INFONDOCS = lang("info_ndocs");
  /** Info on document size. */
  String INFODOCSIZE = lang("info_docsize");
  /** Document encoding. */
  String INFOENCODING = lang("info_encoding");
  /** Info on database table size. */
  String INFONODES = lang("info_nodes");
  /** Maximum tree height. */
  String INFOHEIGHT = lang("info_height");

  /** Info on used main memory. */
  String INFOMEM = lang("info_mem");

  /** Info on database path. */
  String INFODBPATH = lang("info_dbpath");
  /** No Document opened. */
  String INFONODB = lang("info_nodb");
  /** Info on database. */
  String INFODBLIST = lang("info_dblist");
  /** Info on database. */
  String INFODBERR = lang("info_dberror");

  /** Info on Query Verbosity. */
  String INFOINFO = lang("info_info");
  /** Info on Query Verbosity. */
  String INFOALL = lang("info_all");
  /** Info on Debug Mode. */
  String INFODEBUG = lang("info_debug");
  /** Info on Whitespace Chopping. */
  String INFOCHOP = lang("info_chop");
  /** Info on Entity Parsing. */
  String INFOENTITY = lang("info_entities");
  /** Info on result serialization. */
  String INFOSERIALIZE = lang("info_serialize");
  /** Info on well formed XML serialization. */
  String INFOXMLOUTPUT = lang("info_xmloutput");
  /** Info on Main Memory mode. */
  String INFOMAINMEM = lang("info_mainmem");
  /** Info on Tag Index. */
  String INFOTAGINDEX = lang("info_tagindex");
  /** Info on Attribut value Index. */
  String INFOATNINDEX = lang("info_atnindex");
  /** Info on Text Indexing. */
  String INFOTEXTINDEX = lang("info_txtindex");
  /** Info on Attribute Indexing. */
  String INFOATTRINDEX = lang("info_atvindex");
  /** Info on Fulltext Indexing. */
  String INFOFTINDEX = lang("info_ftindex");
  /** Info on Fuzzy Indexing. */
  String INFOFZINDEX = lang("info_fzindex");
  /** Info on Index. */
  String INFOOUTOFDATED = lang("info_outofdated");

  /** Info on Document Creation. */
  String INFODB = lang("info_db");
  /** Info on Document Creation. */
  String INFOCREATE = lang("info_create");
  /** Database Info. */
  String INFOGENERAL = lang("info_general");
  /** Database Info. */
  String RESULTCHOP = lang("info_resultchop");
  /** Option flag. */
  String INFOON = lang("info_on");
  /** Option flag. */
  String INFOOFF = lang("info_off");
  /** Error info. */
  String INFOERROR = lang("info_error") + COLS;

  // MENU ENTRIES =============================================================

  /** Menu entry. */
  String MENUFILE = lang("m_file");
  /** Menu entry. */
  String MENUEDIT = lang("m_edit");
  /** Menu entry. */
  String MENUVIEW = lang("m_view");
  /** Menu entry. */
  String MENUOPTIONS = lang("m_options");
  /** Menu entry. */
  String MENUHELP = lang("m_help");

  /** Menu label. */
  String MENUDB = lang("m_db") + COL;
  /** Menu label. */
  String MENUXQ = lang("m_xquery") + COL;
  /** Menu label. */
  String MENUMAIN = lang("m_main") + COL;
  /** Menu label. */
  String MENUVIEWS = lang("m_views") + COL;
  /** Menu label. */
  String MENUINTER = lang("m_inter") + COL;
  /** Menu label. */
  String MENULAYOUT = lang("m_layout") + COL;

  // GUI COMMANDS =============================================================

  /** Command Info. */
  String GUIABOUT = BaseX.info(lang("c_about") + DOTS, NAME);
  /** Command Info. */
  String GUIABOUTTT = lang("c_abouttt");
  /** Command Info. */
  String GUICLOSE = lang("c_close");
  /** Command Info. */
  String GUICLOSETT = lang("c_closett");
  /** Command Info. */
  String GUICOLOR = lang("c_color") + DOTS;
  /** Command Info. */
  String GUICOLORTT = lang("c_colortt");
  /** Command Info. */
  String GUICUT = lang("c_cut");
  /** Command Info. */
  String GUICOPY = lang("c_copy");
  /** Command Info. */
  String GUICPPATH = lang("c_cppath");
  /** Command Info. */
  String GUICOPYTT = lang("c_copytt");
  /** Command Info. */
  String GUICPPATHTT = lang("c_cppathtt");
  /** Command Info. */
  String GUICREATE = lang("c_create") + DOTS;
  /** Command Info. */
  String GUICREATETT = lang("c_creatett");
  /** Command Info. */
  String GUIDELETE = lang("c_delete") + DOTS;
  /** Command Info. */
  String GUIDEL = lang("c_delete");
  /** Command Info. */
  String GUIDELETETT = lang("c_deletett");
  /** Command Info. */
  String GUIDROP = lang("c_drop") + DOTS;
  /** Command Info. */
  String GUIDROPTT = lang("c_droptt");
  /** Command Info. */
  String GUIEDIT = lang("c_edit") + DOTS;
  /** Command Info. */
  String GUIEDITTT = lang("c_edittt");
  /** Command Info. */
  String GUIEXIT = lang("c_exit");
  /** Command Info. */
  String GUIEXITTT = lang("c_exittt");
  /** Command Info. */
  String GUIEXPORT = lang("c_export") + DOTS;
  /** Command Info. */
  String GUIEXPORTTT = lang("c_exporttt");
  /** Command Info. */
  String GUIFILTER = lang("c_filter");
  /** Command Info. */
  String GUIFILTERTT = lang("c_filtertt");
  /** Command Info. */
  String GUIFONTS = lang("c_fonts");
  /** Command Info. */
  String GUIFONTSTT = lang("c_fontstt");
  /** Command Info. */
  String GUIFULL = lang("c_full");
  /** Command Info. */
  String GUIFULLTT = lang("c_fulltt");
  /** Command Info. */
  String GUIGOBACK = lang("c_goback");
  /** Command Info. */
  String GUIGOBACKTT = lang("c_gobacktt");
  /** Command Info. */
  String GUIGOFORWARD = lang("c_goforward");
  /** Command Info. */
  String GUIGOFORWARDTT = lang("c_goforwardtt");
  /** Command Info. */
  String GUIGOUP = lang("c_goup");
  /** Command Info. */
  String GUIGOUPTT = lang("c_gouptt");
  /** Command Info. */
  String GUIIMPORTFS = lang("c_importfs") + DOTS;
  /** Command Info. */
  String GUIIMPORTFSTT = lang("c_importfstt");
  /** Command Info. */
  String GUIINFO = lang("c_info");
  /** Command Info. */
  String GUIINFOTT = lang("c_infott");
  /** Command Info. */
  String GUIINSERT = lang("c_insert") + DOTS;
  /** Command Info. */
  String GUIINSERTTT = lang("c_inserttt");
  /** Command Info. */
  String GUIMAPLAYOUT = lang("c_maplayout");
  /** Command Info. */
  String GUIMAPLAYOUTTT = lang("c_maplayouttt");
  /** Command Info. */
  String GUIOPEN = lang("c_open") + DOTS;
  /** Command Info. */
  String GUIOPENTT = lang("c_opentt");
  /** Command Info. */
  String GUIPASTE = lang("c_paste");
  /** Command Info. */
  String GUIPASTETT = lang("c_pastett");
  /** Command Info. */
  String GUIPREFS = lang("c_prefs") + DOTS;
  /** Command Info. */
  String GUIPREFSTT = lang("c_prefstt");
  /** Command Info. */
  String GUIREDO = lang("c_redo");
  /** Command Info. */
  String GUIROOT = lang("c_root");
  /** Command Info. */
  String GUIROOTTT = lang("c_roottt");
  /** Command Info. */
  String GUIRTEXEC = lang("c_rtexec");
  /** Command Info. */
  String GUIRTEXECTT = lang("c_rtexectt");
  /** Command Info. */
  String GUIRTFILTER = lang("c_rtfilter");
  /** Command Info. */
  String GUIRTFILTERTT = lang("c_rtfiltertt");
  /** Command Info. */
  String GUISHOWBUTTONS = lang("c_showbuttons");
  /** Command Info. */
  String GUISHOWBUTTONSTT = lang("c_showbuttonstt");
  /** Command Info. */
  String GUISHOWHELP = lang("c_showhelp");
  /** Command Info. */
  String GUISHOWHELPTT = lang("c_showhelptt");
  /** Command Info. */
  String GUISHOWINFO = lang("c_showinfo");
  /** Command Info. */
  String GUISHOWINFOTT = lang("c_showinfott");
  /** Command Info. */
  String GUISHOWINPUT = lang("c_showinput");
  /** Command Info. */
  String GUISHOWINPUTTT = lang("c_showinputtt");
  /** Command Info. */
  String GUISHOWMAP = lang("c_showmap");
  /** Command Info. */
  String GUISHOWMAPTT = lang("c_showmaptt");
  /** Command Info. */
  String GUISHOWMENU = lang("c_showmenu");
  /** Command Info. */
  String GUISHOWMENUTT = lang("c_showmenutt");
  /** Command Info. */
  String GUISHOWSEARCH = lang("c_showsearch");
  /** Command Info. */
  String GUISHOWSEARCHTT = lang("c_showsearchtt");
  /** Command Info. */
  String GUISHOWSTATUS = lang("c_showstatus");
  /** Command Info. */
  String GUISHOWSTATUSTT = lang("c_showstatustt");
  /** Command Info. */
  String GUISHOWTABLE = lang("c_showtable");
  /** Command Info. */
  String GUISHOWPLOT = lang("c_showplot");
  /** Command Info. */
  String GUISHOWTABLETT = lang("c_showtablett");
  /** Command Info. */
  String GUISHOWPLOTTT = lang("c_showplottt");
  /** Command Info. */
  String GUISHOWTEXT = lang("c_showtext");
  /** Command Info. */
  String GUISHOWTEXTTT = lang("c_showtexttt");
  /** Command Info. */
  String GUISHOWFOLDER = lang("c_showfolder");
  /** Command Info. */
  String GUISHOWFOLDERTT = lang("c_showfoldertt");
  /** Command Info. */
  String GUIUNDO = lang("c_undo");
  /** Command Info. */
  String GUIXQOPEN = lang("c_xqopen") + DOTS;
  /** Command Info. */
  String GUIXQOPENTT = lang("c_xqopentt");
  /** Command Info. */
  String GUIXQSAVE = lang("c_xqsave") + DOTS;
  /** Command Info. */
  String GUIXQSAVETT = lang("c_xqsavett");

  // BUTTONS ==================================================================

  /** New Line String. */
  String BUTTONSEARCH = lang("b_search");
  /** Input field. */
  String BUTTONCMD = lang("b_cmd");
  /** Input field. */
  String BUTTONXQUERY = lang("b_xquery");
  /** Button text for confirming actions. */
  String BUTTONOK = lang("b_ok");
  /** Button text for confirming actions. */
  String BUTTONOPT = lang("b_opt");
  /** Button text for opening files. */
  String BUTTONRENAME = lang("b_rename");
  /** Button text for opening files. */
  String BUTTONOPEN = lang("b_open");
  /** Button text for canceling actions. */
  String BUTTONCANCEL = lang("b_cancel");
  /** Button text for deleting files. */
  String BUTTONDROP = lang("b_drop") + DOTS;
  /** Button text for browsing files/directories. */
  String BUTTONBROWSE = lang("b_browse") + DOTS;
  /** XQuery copy Button. */
  String BUTTONTOXQUERY = ">> XQuery";
  /** Text field mode - Search Modes. */
  String[] SEARCHMODE = { "XQuery", lang("b_simple") };

  // STATUS BAR ===============================================================

  /** Default message. */
  String STATUSOK = lang("s_ok") + ". ";

  // VISUALIZATIONS ===========================================================

  /** Text find information. */
  String RESULTFIND = lang("text_find") + COLS;
  /** Help String. */
  String NOTABLE = lang("no_table") + DOT;
  /** Help String. */
  String NOSPACE = lang("no_space");
  /** Binary file. */
  byte[] MAPBINARY = token(lang("map_binary"));
  /** Query Info title. */
  String INFOTIT = lang("info_title");
  /** No Query info. */
  String INFONO = lang("info_no");
  /** Query title. */
  String QUERYTIT = lang("query_title");
  /** Help title. */
  String HELPTIT = lang("help_title");
  /** Help title. */
  String TEXTTIT = lang("text_title");
  /** Plot visualization. */
  String PLOTLOG = "log";

  // DIALOG WINDOWS ===========================================================

  /** Open Dialog - No database. */
  String DIALOGINFO = lang("d_info");
  /** Dialog title for choosing a directory. */
  String DIALOGFC = lang("d_fctitle");

  /** Dialog title for choosing a file. */
  String CREATETITLE = lang("dc_title");
  /** Database creation filter. */
  String CREATEFILTER = lang("dc_filter");
  /** Dialog title for creating a database. */
  String CREATENAME = lang("dc_name") + COLS;
  /** XML File Description. */
  String CREATEXMLDESC = lang("dc_xmldesc") + " (*.xml)";
  /** ZIP File Description. */
  String CREATEZIPDESC = lang("dc_zipdesc") + " (*.zip)";
  /** GZ File Description. */
  String CREATEGZDESC = lang("dc_gzdesc") + " (*.gz)";
  /** XQ File Description. */
  String CREATEXQDESC = lang("dc_xqdesc") + " (*.xq)";
  /** Dialog title for creating a database. */
  String CREATEDOC = lang("dc_doc") + COL;
  /** Dialog title for creating a database. */
  String CREATECOLL = lang("dc_coll") + COL;
  /** Dialog Title for Database Options. */
  String CREATEADVTITLE = lang("dc_advtitle");
  /** Whitespaces information. */
  String CREATECHOP = lang("dc_chop");
  /** Entities information. */
  String CREATEENTITIES = lang("dc_entities");
  /** Entities information. */
  String CREATEDTD = lang("dc_dtd");
  /** SAX parsing information. */
  String CREATEINTPARSE = lang("dc_intparse");
  /** Main-Memory Mode. */
  String CREATEMAINMEM = lang("dc_mainmem");

  /** Word Index information. */
  String CREATEFZ = lang("dc_fzindex");
  /** Word Index information. */
  String CREATESTEM = lang("dc_ftstem");
  /** Word Index information. */
  String CREATECS = lang("dc_ftcs");
  /** Word Index information. */
  String CREATEDC = lang("dc_ftdc");

  /** Whitespaces information. */
  String CHOPPINGINFO = lang("dc_chopinfo");
  /** Whitespaces information. */
  String INTPARSEINFO = lang("dc_intparseinfo");
  /** Entities information. */
  String ENTITIESINFO = lang("dc_entitiesinfo");
  /** Entities information. */
  String DTDINFO = lang("dc_dtdinfo");
  /** Main-Memory Mode. */
  String MMEMINFO =  lang("dc_mminfo");

  /** Value Index information. */
  String TXTINDEXINFO =  lang("dc_txtinfo");
  /** Value Index information. */
  String ATTINDEXINFO = lang("dc_attinfo");
  /** Fulltext Index information. */
  String FTINDEXINFO = lang("dc_ftxinfo");
  /** Fulltext Index information. */
  String FZINDEXINFO = lang("dc_fzinfo");
  /** Fulltext Index information. */
  String FTSTEMINFO = lang("dc_ftsteminfo");
  /** Fulltext Index information. */
  String FTCSINFO = lang("dc_ftcsinfo");
  /** Fulltext Index information. */
  String FTDCINFO = lang("dc_ftdcinfo");

  /** General info. */
  String GENERALINFO =  lang("dc_general");
  /** General info. */
  String PARSEINFO =  lang("dc_parse");
  /** Indexing info. */
  String NAMESINFO =  lang("dc_names");
  /** Indexing info. */
  String INDEXINFO =  lang("dc_index");
  /** General info. */
  String METAINFO =  lang("dc_meta");
  /** Indexing info. */
  String FTINFO =  lang("dc_ft");

  /** Dialog title for opening a database. */
  String OPENTITLE = lang("do_title");
  /** Dialog title for deleting nodes. */
  String NODBQUESTION = INFONODB + DOT + NL + lang("do_nodbquestion") + NL + S;

  /** File Dialog title. */
  String XQOPENTITLE = lang("dq_open");
  /** File Dialog title. */
  String XQSAVETITLE = lang("dq_save");
  /** File Dialog error. */
  String XQOPERROR = lang("dq_notopened");
  /** File Dialog error. */
  String XQSAVERROR = lang("dq_notsaved");
  /** File Dialog replace information. */
  String FILEREPLACE = lang("dq_replace");

  /** Dialog title for exporting XML. */
  String EXPORTTITLE = lang("d_export");

  /** Progress text for FS import. */
  String IMPORTPROG = "Parse filesystem...";
  /** Dialog Title for Import Options. */
  String IMPORTFSTITLE = lang("dfs_title");
  /** Import Options. */
  String IMPORTALL = lang("dfs_all");
  /** Import Options. */
  String IMPORTALLINFO = lang("dfs_allinfo") + COL;
  /** Import Options. */
  String IMPORTFSTEXT = lang("dfs_text") + COL;
  /** Import Options. */
  String IMPORTFSTEXT1 = lang("dfs_text1") + COL;
  /** Import Options. */
  String IMPORTFSTEXT2 = lang("dfs_text2") + COL;
  /** Import Options. */
  String IMPORTFSTYPES = lang("dfs_types") + DOTS;
  /** Import Options. */
  String IMPORTFSMAXINFO = lang("dfs_maxinfo") + DOT;
  /** Import Options. */
  String IMPORTCONT = lang("dfs_cont");
  /** Import Options. */
  String IMPORTMETA = lang("dfs_meta") + " (MP3, JPG, TIF, PNG, GIF, ...)";
  /** Import Options. */
  String IMPORTFTINDEX = lang("dfs_ftindex");
  /** Import Options. */
  String[] IMPORTFSMAX = {
      "Max. 1KB", "Max. 10KB", "Max. 100KB", "Max. 1MB", "Max. 10MB"
  };
  /** Import Options. */
  int[] IMPORTFSMAXSIZE = { 1024, 10240, 102400, 1048576, 10485760 };

  /** Dialog title for renaming a database. */
  String RENAMETITLE = lang("dr_title");
  /** Info for renaming a database. */
  String RENAMEINVALID = lang("dr_invalid");
  /** Info for renaming a database. */
  String RENAMEEXISTS = lang("dr_exists");
  /** Info for overwrite a database. */
  String RENAMEOVER = lang("dr_over");
  /** Info for overwrite a database and deletion of backing store. */
  String RENAMEOVERBACKING = lang("dr_overbacking");

  /** Dialog title for dropping a database. */
  String DROPTITLE = lang("dd_title");
  /** Dialog title for dropping a database. */
  String DROPCONF = lang("dd_question") + NL + S;

  /** Dialog Title for Import Options. */
  String PREFSTITLE = lang("dp_title");
  /** Database Path. */
  String DATABASEPATH = lang("dp_dbpath");
  /** Interactions. */
  String PREFINTER = lang("dp_inter");
  /** Look and Feel. */
  String PREFLF = lang("dp_lf");
  /** Focus. */
  String PREFFOCUS = lang("dp_focus");
  /** Simple File Dialog. */
  String SIMPLEFD = lang("dp_simplefd");
  /** Name display flag. */
  String PREFNAMES = lang("dp_names");
  /** Language preference. */
  String PREFLANG = lang("dp_lang");

  /** Dialog title for deleting nodes. */
  String DELETECONF = lang("dx_question");

  /** Dialog Title for inserting new data. */
  String INSERTTITLE = lang("dn_title");
  /** Insert Name. */
  String INSERTNAME = lang("dn_name") + COL;
  /** Insert Value. */
  String INSERTVALUE = lang("dn_value") + COL;

  /** Dialog Title for updating document data. */
  String EDITTITLE = lang("de_title");
  /** Dialog Title for updating text. */
  String EDITTEXT = lang("de_text");
  /** Dialog Title for updating text. */
  String[] EDITKIND = { lang("de_kind1"), lang("de_kind2"), lang("de_kind3"),
      lang("de_kind4"), lang("de_kind5"), lang("de_kind6")
  };

  /** Update texts. */
  String[] KINDS = { "root", "element", "text", "attribute", "comment", "pi" };

  /** Dialog title for choosing a font. */
  String FONTTITLE = lang("df_title");
  /** Anti-Aliasing information. */
  String FAALIAS = lang("df_aalias");
  /** Predefined font types. */
  String[] FONTTYPES = { lang("df_type1"), lang("df_type2"), lang("df_type3") };
  /** Predefined font sizes. */
  String[] FTSZ = { "8", "10", "12", "14", "16", "18", "20", "22", "24", "32" };

  /** Dialog title for treemap color schema. */
  String SCHEMATITLE = lang("ds_title");
  /** Color schema information. */
  String SCHEMARED = lang("ds_red");
  /** Color schema information. */
  String SCHEMAGREEN = lang("ds_green");
  /** Color schema information. */
  String SCHEMABLUE = lang("ds_blue");

  /** Dialog title for treemap design. */
  String MAPLAYOUTTITLE = lang("dm_title");
  /** Simple Map Layout. */
  String MAPSIMPLE = lang("dm_simple");
  /** Show Atts.  */
  String MAPATTS = lang("dm_atts");
  /** Predefined number of layouts. */
  String[] MAPOFFSETS = {
    lang("dm_choice1"), lang("dm_choice2"), lang("dm_choice3"),
    lang("dm_choice4"), lang("dm_choice5")
  };
  /** Predefined number of layouts. */
  String[] MAPALGO = {
    "Split Layout", "Strip Layout", "Squarified Layout", "Slice&Dice Layout"
  };

  /** Map layout-algorithm. */
  String MAPOFF = lang("dm_offset") + COL;

  /** Size depending on... */
  String MAPSIZE = lang("dm_size");
  /** Size depending on... */
  String MAPBOTH = lang("dm_size_both");
  /** Size depending on... */
  String MAPCHILDREN = lang("dm_size_children");
  /** Size depending on... */
  String MAPFSSIZE = lang("dm_size_fssize");
  /** Size depending on... */
  String MAPTEXTSIZE = lang("dm_size_textsize");

  /** Memory information. */
  String MEMTOTAL = lang("dz_total") + COLS;
  /** Memory information. */
  String MEMRESERVED = lang("dz_reserved") + COLS;
  /** Memory information. */
  String MEMUSED = lang("dz_used") + COLS;
  /** Memory help. */
  String MEMHELP = lang("dz_help");

  /** About text. */
  String ABOUTTITLE = BaseX.info(lang("da_title"), NAME);
  /** Copyright Info. */
  String COPYRIGHT = "©2005-09 " + COMPANY;
  /** License Info. */
  String LICENSE = lang("da_license");
  /** Developer Info. */
  String DEVELOPER = lang("da_dev") + ": Christian Grün";
  /** Contributors Info. */
  String CONTRIBUTE1 = lang("da_cont1") + ": Sebastian Gath, Lukas Kircher,";
  /** Developer Names. */
  String CONTRIBUTE2 = "Andreas Weiler, Alexander Holupirek " +
    lang("da_cont2");
  /** Developer Names. */
  String CONTACT = lang("da_contact") + COLS + MAIL;

  // HELP TEXTS ===============================================================

  /** Help String. */
  byte[] HELPQUERYRT = token(lang("h_queryrt"));
  /** Help String. */
  byte[] HELPTOXQUERY = token(lang("h_toxquery"));
  /** Help String. */
  byte[] HELPTOXPATH = token(lang("h_toxpath"));
  /** Help String. */
  byte[] HELPFILTER = token(lang("h_filter"));
  /** Help String. */
  byte[] HELPGO = token(lang("h_go"));
  /** Help String. */
  byte[] HELPHIST = token(lang("h_hist"));
  /** Help String. */
  byte[] HELPSTOP = token(lang("h_stop"));
  /** Help String. */
  byte[] HELPCANCEL = token(lang("h_cancel"));
  /** Help String. */
  byte[] HELPMODE = token(lang("h_mode"));
  /** Help String. */
  byte[] HELPOK = token(lang("h_ok"));
  /** Help String. */
  byte[] HELPOPT = token(lang("h_opt"));
  /** Help String. */
  byte[] HELPRENAMEDB = token(lang("h_renamedb"));
  /** Help String. */
  byte[] HELPOPENDB = token(lang("h_opendb"));
  /** Help string. */
  byte[] HELPOPENINFO = token(lang("h_openinfo"));
  /** Help String. */
  byte[] HELPFALIAS = token(lang("h_falias"));
  /** Help String. */
  byte[] HELPBROWSE = token(lang("h_browse"));
  /** Help String. */
  byte[] HELPFSPATH = token(lang("h_fspath"));
  /** Help String. */
  byte[] HELPDBPATH = token(lang("h_dbpath"));
  /** Help String. */
  byte[] HELPLANG = token(lang("h_lang"));
  /** Help String. */
  byte[] HELPNAMES = token(lang("h_names"));
  /** Help String. */
  byte[] HELPFSNAME = token(lang("h_fsname"));
  /** Help String. */
  byte[] HELPFSALL = token(lang("h_fsall"));
  /** Help String. */
  byte[] HELPFSBACKING = token(lang("h_fsbacking"));
  /** Help String. */
  byte[] HELPFSMOUNT = token(lang("h_fsmount"));
  /** Help String. */
  byte[] HELPMETA = token(lang("h_meta"));
  /** Help String. */
  byte[] HELPCONT = token(lang("h_cont"));
  /** Help String. */
  byte[] HELPFSMAX = token(lang("h_fsmax"));
  /** Help String. */
  byte[] HELPMAPATTS = token(lang("h_mapatts"));
  /** Help String. */
  byte[] HELPMAPSIMPLE = token(lang("h_mapsimple"));
  /** Help String. */
  byte[] HELPMAPLAYOUT = token(lang("h_maplayout"));
  /** Help String. */
  byte[] HELPMAPOFF = token(lang("h_mapoff"));
  /** HelpString. */
  byte[] HELPMAPSIZE = token(lang("h_mapsize"));
  /** Help String. */
  byte[] HELPDROP = token(lang("h_drop"));
  /** Help String. */
  byte[] HELPDROPDB = token(lang("h_dropdb"));
  /** Help String. */
  byte[] HELPLF = token(lang("h_lf"));
  /** Help String. */
  byte[] HELPFOCUS = token(lang("h_focus"));
  /** Help String. */
  byte[] HELPSIMPLEFD = token(lang("h_simplefd"));
  /** Help String. */
  byte[] HELPOPEN = token(lang("h_open"));
  /** Help String. */
  byte[] HELPFONT = token(lang("h_font"));
  /** Help string. */
  byte[] HELPMMEM = token(lang("h_mmem"));
  /** Help string. */
  byte[] HELPCOLORS = token(lang("h_colors"));
  /** Help Dialog. */
  byte[] HELPCMD = token(lang("h_cmd"));
  /** Help Dialog. */
  byte[] HELPSEARCHXML = token(lang("h_searchxml"));
  /** Help Dialog. */
  byte[] HELPSEARCHFS = token(lang("h_searchfs"));
  /** Help Dialog. */
  byte[] HELPXPATH = token(lang("h_xpath"));
  /** Help String. */
  String HELPMODEFILT = lang("h_modefilt");
  /** Help String. */
  byte[] HELPMAP = token(lang("h_map"));
  /** Help String. */
  byte[] HELPPLOT = token(lang("h_plot"));
  /** Help String. */
  byte[] HELPPLOTAXISX = token(lang("h_plotaxisx"));
  /** Help String. */
  byte[] HELPPLOTAXISY = token(lang("h_plotaxisy"));
  /** Help String. */
  byte[] HELPPLOTXLOG = token(lang("h_plotxlog"));
  /** Help String. */
  byte[] HELPPLOTYLOG = token(lang("h_plotylog"));
  /** Help String. */
  byte[] HELPPLOTDOTS = token(lang("h_plotdots"));
  /** Help String. */
  byte[] HELPPLOTITEM = token(lang("h_plotitem"));
  /** Help String. */
  byte[] HELPFOLDER = token(lang("h_folder"));
  /** Help String. */
  byte[] HELPTABLE = token(lang("h_table"));
  /** Help String. */
  byte[] HELPTEXT = token(lang("h_text"));
  /** Help String. */
  byte[] HELPINFO = token(lang("h_info"));
  /** Help String. */
  byte[][] HELPSEARCH = { token(lang("h_search1")), token(lang("h_search2")) };
  /** Help String. */
  byte[] HELPQUERYMODE = token(lang("h_querymode"));
  /** Help String. */
  byte[] HELPSEARCHCAT = token(lang("h_searchcat"));
  /** Help String. */
  byte[] HELPCAT = token(lang("h_cat"));
  /** Help String. */
  byte[] HELPDS = token(lang("h_ds"));
  /** Help String. */
  byte[] HELPCATINPUT = token(lang("h_catinput"));
  /** Help String. */
  byte[] HELPMOVER = token(lang("h_mover"));
  /** Help String. */
  byte[] HELPTABLEHEAD = token(lang("h_tablehead"));
  /** Help String. */
  byte[] HELPMEM = token(lang("h_mem"));
  /** Help String. */
  byte[] HELPGUISTATUS = token(lang("h_guistatus"));
  
  /** Dummy string to check if all language strings have been assigned. */
  String DUMMY = lang(null);
}
