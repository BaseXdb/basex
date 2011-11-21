package org.basex.core;

import static org.basex.core.Lang.*;
import static org.basex.util.Token.*;

import java.util.Locale;

import org.basex.core.Commands.CmdCreate;
import org.basex.core.Commands.CmdDrop;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.Commands.CmdInfo;
import org.basex.core.Commands.CmdPerm;
import org.basex.core.Commands.CmdRepo;
import org.basex.core.Commands.CmdSet;
import org.basex.core.Commands.CmdShow;

/**
 * This class contains internationalized text strings, which are used
 * throughout the project. If this class is called first, the Strings
 * are initialized by the {@link org.basex.core.Lang} class.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public interface Text {

  // FREQUENTLY USED CHARACTERS ===============================================

  /** New line. */
  String NL = Prop.NL;
  /** Project name. */
  String NAME = Prop.NAME;
  /** Code version (must contain major, minor and optional patch number). */
  String VERSION = Prop.VERSION;

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

  /** Project namespace. */
  String NAMELC = NAME.toLowerCase(Locale.ENGLISH);
  /** URL. */
  String URL = "http://www." + NAMELC + ".org";
  /** URL of the community page. */
  String COMMUNITY_URL = URL + "/community";
  /** URL of the documentation. */
  String DOC_URL = "http://docs." + NAMELC + ".org";
  /** URL of the update page. */
  String UPDATE_URL = URL + "/products/download/all-downloads/";
  /** Version URL. */
  String VERSION_URL = "http://files." + NAMELC + ".org/version.txt";
  /** Mail. */
  String MAIL = NAMELC + "-talk@mailman.uni-konstanz.de";
  /** Company info. */
  String COMPANY = NAME + " Team";
  /** Title and version. */
  String TITLE = NAME + ' ' + VERSION;
  /** Default language. */
  String LANGUAGE = "English";

  // CONSOLE INFO =============================================================

  /** Console text. */
  String CONSOLE = TITLE + " [%]" + NL;
  /** Console text. */
  String CONSOLE2 = lang("help_intro", "help") + NL;
  /** Version information. */
  String VERSINFO = lang("version");

  /** Goodbye information. */
  String[] CLIENTBYE = {
      lang("bye1"), lang("bye2"), lang("bye3"), lang("bye4")
  };

  /** Local (standalone) mode. */
  String LOCALMODE = "Standalone";
  /** Start information. */
  String LOCALINFO =
    " [-bcdioqsuvVwxz] [file]" + NL +
    "  [file]      Execute XQuery file" + NL +
    "  -b<pars>    Bind external XQuery variables" + NL +
    "  -c<cmd>     Execute database command(s)" + NL +
    "  -d          Activate debugging mode" + NL +
    "  -i<input>   Open initial file or database" + NL +
    "  -o<file>    Write output to file" + NL +
    "  -q<expr>    Execute XQuery expression" + NL +
    "  -s<pars>    Set serialization parameter(s)" + NL +
    "  -u          Write updates back to original files" + NL +
    "  -v/V        Show (all) process info" + NL +
    "  -w          Preserve whitespaces from input files" + NL +
    "  -x          Show query execution plan" + NL +
    "  -z          Skip output of results";

  /** Client mode. */
  String CLIENTMODE = "Client";
  /** Client start information. */
  String CLIENTINFO =
    " [-bcdinopPqsUvVwxz] [file]" + NL +
    "  [file]      Execute XQuery file" + NL +
    "  -b<pars>    Bind external XQuery variables" + NL +
    "  -c<cmd>     Execute database command(s)" + NL +
    "  -d          Activate debugging mode" + NL +
    "  -i<input>   Open initial file or database" + NL +
    "  -n<name>    Set server (host) name" + NL +
    "  -o<file>    Write output to file" + NL +
    "  -p<num>     Set server port" + NL +
    "  -P<pass>    Specify user password" + NL +
    "  -q<expr>    Execute XQuery expression" + NL +
    "  -s<pars>    Set serialization parameter(s)" + NL +
    "  -U<name>    Specify user name" + NL +
    "  -v/V        Show (all) process info" + NL +
    "  -w          Preserve whitespaces from input files" + NL +
    "  -x          Show query execution plan" + NL +
    "  -z          Skip output of results";

  /** Server mode. */
  String SERVERMODE = "Server";
  /** Server start information. */
  String SERVERINFO =
    " [-cdeipSz] [stop]" + NL +
    "  stop      Stop running server" + NL +
    "  -c<cmd>   Execute initial database command(s)" + NL +
    "  -d        Activate debugging mode" + NL +
    "  -e<num>   Set event port" + NL +
    "  -i        Enter interactive mode" + NL +
    "  -p<num>   Set server port" + NL +
    "  -S        Start as service" + NL +
    "  -z        Suppress logging";

  /** GUI mode. */
  String GUIMODE = "GUI";
  /** GUI start information. */
  String GUIINFO =
    " [file]" + NL +
    "  [file]  Open specified XML or XQuery file";

  /** HTTP information. */
  String HTTPINFO =
    " [-dehlnpPRUWz] [stop]" + NL +
    "  stop       Stop running server" + NL +
    "  -c         Start in client mode" + NL +
    "  -d         Activate debugging mode" + NL +
    "  -e<num>    Set event port" + NL +
    "  -h<num>    Set port of HTTP server" + NL +
    "  -l         Start in local mode" + NL +
    "  -n<name>   Set host name of database server" + NL +
    "  -p<num>    Set port of database server" + NL +
    "  -P<pass>   Specify user password" + NL +
    "  -R         Deactivate REST service" + NL +
    "  -s         Specify port to stop HTTP server" + NL +
    "  -S         Start as service" + NL +
    "  -U<name>   Specify user name" + NL +
    "  -W         Deactivate WebDAV service" + NL +
    "  -z         Suppress logging";

  /** Bug info. */
  String BUGINFO = "Potential bug? Improper use? Your feedback is welcome:";

  // SERVER ===================================================================

  /** Server was started. */
  String SERVERSTART = lang("srv_start");
  /** Server was stopped. */
  String SERVERSTOPPED = lang("srv_stop");
  /** Server is running or permission was denied. */
  String SERVERBIND = lang("srv_bind");
  /** Same ports specified. */
  String SERVERPORTS = lang("srv_ports");
  /** Unknown host. */
  String SERVERUNKNOWN = lang("srv_unknown");
  /** Timeout exceeded. */
  String SERVERTIMEOUT = lang("srv_timeout");
  /** Connection error. */
  String SERVERERROR = lang("srv_connect");
  /** Access denied. */
  String SERVERDENIED = lang("srv_denied");
  /** User name. */
  String SERVERUSER = lang("srv_user");
  /** Password. */
  String SERVERPW = lang("srv_pw");

  /** Localhost. */
  String LOCALHOST = "localhost";

  // COMMANDS =================================================================

  /** Command keyword. */
  String ALL = "ALL";
  /** Command keyword. */
  String TO = "TO";
  /** Command keyword. */
  String ON = "ON";
  /** Command keyword. */
  String OFF = "OFF";
  /** Keyword. */
  String OK = "OK";

  /** Yes flag. */
  String YES = "yes";
  /** No flag. */
  String NO = "no";
  /** True flag. */
  String TRUE = "true";
  /** False flag. */
  String FALSE = "false";

  /** Command help. */
  String C_QUERY = "query";
  /** Command help. */
  String C_PATH = "path";
  /** Command help. */
  String C_INPUT = "input";
  /** Command help. */
  String C_NAME = "name";
  /** Command help. */
  String C_PW = "password";
  /** Command help. */
  String C_PKGPATH = "path";
  /** Command help. */
  String C_PKGNAME = "name";
  /** Command help. */
  String C_PKGDIR = "dir";

  /** Missing help. */
  String NOHELP = lang("ch_nohelp");

  /** Command help. */
  String[] HELPCREATE = {
    "[" + CmdCreate.BACKUP + "|" + CmdCreate.DATABASE + "|" + CmdCreate.EVENT +
    "|" + CmdCreate.INDEX + "|" + CmdCreate.USER + "] [...]",
    lang("ch_create1"),
    lang("ch_create2") + NL +
    LI + CmdDrop.BACKUP + " [" + C_NAME + "]:" + NL +
      "  " + lang("ch_create4", C_NAME) + NL +
    LI + CmdCreate.DATABASE + " [" + C_NAME + "] ([" + C_INPUT + "]):"  + NL +
      "  " + lang("ch_create3", C_NAME, C_INPUT) + NL +
    LI + CmdCreate.EVENT + " [" + C_NAME + "]: " + NL +
      "  " + lang("ch_create9") + NL +
    LI + CmdCreate.INDEX + " [" + CmdIndex.TEXT + "|" + CmdIndex.ATTRIBUTE +
      "|" + CmdIndex.FULLTEXT + "|" + CmdIndex.PATH + "]:" + NL +
      "  " + lang("ch_create5") + NL +
    LI + CmdCreate.USER + " [" + C_NAME + "] ([" + C_PW + "]):" + NL +
      "  " + lang("ch_create8")
  };

  /** Command help. */
  String[] HELPCOPY = {
    "[" + C_NAME + "] [new" + C_NAME + "]", lang("ch_copy1"), lang("ch_copy2")
  };

  /** Command help. */
  String[] HELPCHECK = {
    "[" + C_INPUT + "]", lang("ch_check1"), lang("ch_check2")
  };
  /** Command help. */
  String[] HELPADD = {
    "(" + TO + " [" + C_PATH + "]) [" + C_INPUT + "]",
    lang("ch_add1"), lang("ch_add2", C_INPUT, C_PATH)
  };
  /** Command help. */
  String[] HELPSTORE = {
    "(" + TO + " [" + C_PATH + "]) [" + C_INPUT + "]",
    lang("ch_store1"), lang("ch_store2", C_PATH)
  };
  /** Command help. */
  String[] HELPRETRIEVE = {
    "[" + C_PATH + "]", lang("ch_retrieve1"), lang("ch_retrieve2", C_PATH)
  };
  /** Command help. */
  String[] HELPDELETE = {
    "[" + C_PATH  + "]", lang("ch_delete1"), lang("ch_delete2")
  };
  /** Command help. */
  String[] HELPOPEN = {
    "[" + C_PATH + "]", lang("ch_open1"), lang("ch_open2", C_PATH)
  };
  /** Command help. */
  String[] HELPINFO = {
    "([" + CmdInfo.DATABASE + "|" + CmdInfo.INDEX + "|" +
    CmdInfo.STORAGE + "])",
    lang("ch_info1"),
    lang("ch_info21") + NL +
    LI + lang("ch_info22") + NL +
    LI + CmdInfo.DATABASE + ": " + lang("ch_info23") + NL +
    LI + CmdInfo.INDEX + ": " + lang("ch_info24") + NL +
    LI + CmdInfo.STORAGE + " [start end] | [" + C_QUERY + "]: " +
    lang("ch_info25")
  };
  /** Command help. */
  String[] HELPCLOSE = {
    "", lang("ch_close1"), lang("ch_close2")
  };
  /** Command help. */
  String[] HELPLIST = {
    "([" + C_PATH + "])", lang("ch_list1"), lang("ch_list2", C_PATH)
  };
  /** Command help. */
  String[] HELPDROP = {
    "[" + CmdDrop.BACKUP + "|" + CmdDrop.DATABASE + "|" + "|" + CmdDrop.EVENT +
      CmdDrop.INDEX + "|" + CmdDrop.USER + "] [...]",
    lang("ch_drop1"),
    lang("ch_drop2") + NL +
    LI + CmdDrop.BACKUP + " [" + C_NAME + "]:" + NL +
      "  " + lang("ch_drop24", C_NAME) + NL +
    LI + CmdDrop.DATABASE + " [" + C_NAME + "]:" + NL +
      "  " + lang("ch_drop21") + NL +
    LI + CmdDrop.EVENT + " [" + C_NAME + "]:" + NL +
      "  " + lang("ch_drop25", C_NAME) + NL +
    LI + CmdDrop.INDEX + " [" + CmdIndex.PATH + "|" + CmdIndex.TEXT + "|" +
      CmdIndex.ATTRIBUTE + "|" + CmdIndex.FULLTEXT + "]:" + NL +
      "  " + lang("ch_drop22") + NL +
    LI + CmdDrop.USER + " [" + C_NAME + "] (" + ON + " [database]): " + NL +
      "  " + lang("ch_drop23")
  };
  /** Command help. */
  String[] HELPEXPORT = {
    "[" + C_PATH + "]", lang("ch_export1"), lang("ch_export2", C_PATH)
  };
  /** Command help. */
  String[] HELPOPTIMIZE = {
    "(" + ALL + ")", lang("ch_optimize1"), lang("ch_optimize2", ALL)
  };

  /** Command help. */
  String[] HELPXQUERY = {
    "[" + C_QUERY + "]", lang("ch_xquery1"), lang("ch_xquery2")
  };
  /** Command help. */
  String[] HELPFIND = {
    "[keywords]", lang("ch_find1"), lang("ch_find2")
  };
  /** Command help. */
  String[] HELPFLUSH = {
    "", lang("ch_flush1"), lang("ch_flush2")
  };
  /** Command help. */
  String[] HELPRUN = {
    "[" + C_PATH + "]", lang("ch_run1"), lang("ch_run2", C_PATH)
  };
  /** Command help. */
  String[] HELPCS = {
    "[" + C_QUERY + "]", lang("ch_cs1"), lang("ch_cs2")
  };
  /** Command help. */
  String[] HELPKILL = {
    "[" + C_NAME + "]", lang("ch_kill1"), lang("ch_kill2")
  };
  /** Command help. */
  String[] HELPRENAME = {
    "[" + C_PATH  + "] [newpath]", lang("ch_rename1"), lang("ch_rename2")
  };
  /** Command help. */
  String[] HELPREPLACE = {
    "[" + C_PATH  + "] [" + C_INPUT + "]",
    lang("ch_replace1"), lang("ch_replace2")
  };
  /** Command help. */
  String[] HELPRESTORE = {
    "[" + C_NAME + "-(date)]", lang("ch_restore1"), lang("ch_restore2")
  };
  /** Command help. */
  String[] HELPSHOW = {
    "[" + CmdShow.BACKUPS + "|" + CmdShow.DATABASES + "|" + CmdShow.EVENTS +
    "|" + CmdShow.SESSIONS + "|" + CmdShow.USERS + "]",
    lang("ch_show1"),
    lang("ch_show21") + NL +
    LI + CmdShow.DATABASES + ": " + lang("ch_show22") + NL +
    LI + CmdShow.EVENTS + ": " + lang("ch_show26") + NL +
    LI + CmdShow.SESSIONS + ": " + lang("ch_show23") + NL +
    LI + CmdShow.USERS + " (" + ON + " [database]): " + lang("ch_show24") + NL +
    LI + CmdShow.BACKUPS + ": " + lang("ch_show25")
  };
  /** Command help. */
  String[] HELPGRANT = {
    "[" + CmdPerm.NONE + "|" + CmdPerm.READ + "|" + CmdPerm.WRITE + "|" +
    CmdPerm.CREATE + "|" + CmdPerm.ADMIN + "] (" + ON + " [database]) " + TO +
    " [user]",
    lang("ch_grant1"),
    lang("ch_grant2")
  };
  /** Command help. */
  String[] HELPALTER = {
    "[" + CmdCreate.DATABASE + "|" + CmdCreate.USER + "] [...]",
    lang("ch_alter1"),
    lang("ch_alter2") + NL  +
    LI + CmdCreate.DATABASE + " [" + C_NAME + "] [newname]" + NL +
    "  " + lang("ch_alterdb") + NL +
    LI + CmdCreate.USER  + " [" + C_NAME + "] ([" + C_PW + "]):" + NL +
    "  " + lang("ch_alterpw")
  };
  /** Command help. */
  String[] HELPSET = {
    "[option] ([value])",
    lang("ch_set1", "info"),
    lang("ch_set2", "option", "value") + NL +
    LI + CmdSet.QUERYINFO + COLS + lang("ch_set21") + NL +
    LI + CmdSet.DEBUG     + COLS + lang("ch_set22") + NL +
    LI + CmdSet.SERIALIZE + COLS + lang("ch_set23") + NL +
    LI + CmdSet.CHOP      + COLS + lang("ch_set26") + NL +
    LI + CmdSet.TEXTINDEX + COLS + lang("ch_set28") + NL +
    LI + CmdSet.ATTRINDEX + COLS + lang("ch_set29") + NL +
    LI + CmdSet.FTINDEX   + COLS + lang("ch_set31") + NL +
    LI + CmdSet.PATHINDEX + COLS + lang("ch_set32")
  };
  /** Command help. */
  String[] HELPGET = {
      "[option]", lang("ch_get1", "info"), lang("ch_get2", "option")
  };
  /** Command help. */
  String[] HELPPASSWORD = {
    "([" + C_PW + "])", lang("ch_password1"), lang("ch_password2")
  };
  /** Command help. */
  String[] HELPREPO = {
      "[" + CmdRepo.DELETE + "|" + CmdRepo.INSTALL + "|" + CmdRepo.LIST + "]",
      lang("ch_repo1"),
      lang("ch_repo2") + NL +
      LI + CmdRepo.DELETE + " [" + C_PKGNAME + "|" + C_PKGDIR + "]:" +  NL +
      "  " + lang("ch_repo3", C_PKGNAME, C_PKGDIR) + NL +
      LI + CmdRepo.INSTALL + " [" + C_PKGPATH + "]:" + NL +
      "  " + lang("ch_repo4", C_PKGPATH) + NL +
      LI + CmdRepo.LIST + ":" + NL +
      "  " + lang("ch_repo5")
  };
  /** Command help. */
  String[] HELPHELP = {
    "([command])", lang("ch_help1", NAME), lang("ch_help2", "command")
  };
  /** Command help. */
  String[] HELPEXIT = {
    "", lang("ch_exit1", NAME), lang("ch_exit2", NAME)
  };

  // COMMAND INFOS ============================================================

  /** Command timing information. */
  String PROCTIME = lang("proc_time") + ": %";
  /** Command syntax information. */
  String PROCSYNTAX = lang("proc_syntax") + ": %";
  /** Command execution error. */
  String PROCERR = lang("proc_err") + COL + NL + "%";

  /** No database error. */
  String PROCNODB = lang("proc_nodb");
  /** Main memory error. */
  String PROCMM = lang("proc_mm");
  /** Out of memory error. */
  String PROCMEM = lang("proc_mem");
  /** Out of memory error due to database creation. */
  String PROCMEMCREATE = lang("proc_memcreate");
  /** Progress exception. */
  String PROGERR = "Interrupted" + DOT;

  /** Unknown command error. */
  String CMDNO = lang("cmd_no");
  /** Unknown command error. */
  String CMDUNKNOWN = lang("cmd_unknown");
  /** Unknown command error. */
  String CMDWHICH = CMDUNKNOWN + "; " + lang("help_short", "help") + DOT;
  /** Unknown command error. */
  String CMDSIMILAR = CMDUNKNOWN + "; " + lang("cmd_similar");
  /** Try "help [...]" to get.. */
  String CMDHELP = lang("help_long", "help") + NL;

  // CREATE COMMAND ===========================================================

  /** Create database information. */
  String PROGCREATE = lang("pc_create");
  /** Create index information. */
  String PROGINDEX = lang("pc_index");
  /** Database update. */
  String DBUPDATE = lang("pc_update");
  /** Index update. */
  String INDUPDATE = lang("pc_indupdate");
  /** Builder error. */
  String CANCELCREATE = lang("pc_cancel");
  /** Create database information. */
  String NODESPARSED = " " + lang("pc_parse");
  /** Scanner position. */
  String SCANPOS = lang("pc_pos");

  /** Finish database creation. */
  String DBFINISH = lang("pc_finish") + DOTS;
  /** Create text index. */
  String INDEXTXT = lang("pc_indextxt") + DOTS;
  /** Create attribute index. */
  String INDEXATT = lang("pc_indexatt") + DOTS;
  /** Create full-text index. */
  String INDEXFTX = lang("pc_indexftx") + DOTS;

  /** Database created. */
  String DBCREATED = lang("pc_created");
  /** Path added. */
  String PATHADDED = lang("pc_added");
  /** Path deleted. */
  String PATHDELETED = lang("pc_deleted");
  /** Path renamed. */
  String PATHRENAMED = lang("pc_renamed");
  /** Path replaced. */
  String PATHREPLACED = lang("pc_replaced");
  /** Parse error. */
  String PARSEERR = lang("pc_err");

  /** File not found. */
  String FILEWHICH = lang("pc_filenf");
  /** Path not found. */
  String PATHWHICH = lang("pc_pathnf");
  /** Skipped corrupt files. */
  String SKIPCORRUPT = lang("pc_skipped");
  /** Info on skipped corrupt files. */
  String SKIPINFO = lang("pc_skipinfo");
  /** Missing database name. */
  String DBWHICH = lang("pc_dbnf");
  /** No tokenizer found. */
  String NOTOK = lang("pc_notok");
  /** No stemmer found. */
  String NOSTEM = lang("pc_nostem");
  /** Points to a directory. */
  String DIRERR = lang("pc_direrr");

  // DATABASE COMMANDS ========================================================

  /** Database not found. */
  String DBNOTFOUND = lang("db_no");
  /** Name invalid. */
  String NAMEINVALID = lang("db_invalid");
  /** Database locked. */
  String DBLOCKED = lang("db_locked");
  /** Database closed. */
  String DBCLOSED = lang("db_closed");
  /** Database not closed. */
  String DBCLOSEERR = lang("db_closeerr");
  /** Database dropped. */
  String DBDROPPED = lang("db_dropped");
  /** Database not dropped. */
  String DBNOTDROPPED = lang("db_notdropped");
  /** Database not dropped. */
  String DBDROPERROR = lang("db_droperror");
  /** Database altered. */
  String DBALTERED = lang("db_altered");
  /** Database not dropped. */
  String DBNOTALTERED = lang("db_notaltered");
  /** Database flushed. */
  String DBFLUSHED = lang("db_flushed");
  /** Backup of database created. */
  String DBBACKUP = lang("db_backup");
  /** Backup of database not created. */
  String DBNOBACKUP = lang("db_nobackup");
  /** Copy of database created. */
  String DBCOPY = lang("db_copy");
  /** Copy of database not created. */
  String DBNOCOPY = lang("db_nocopy");
  /** Database restored. */
  String DBRESTORE = lang("db_restore");
  /** Database not restored. */
  String DBNORESTORE = lang("db_norestore");
  /** Database not opened. */
  String DBOPENERR = lang("db_notopened");
  /** Database opened. */
  String DBOPENED = lang("db_opened");
  /** Database exported. */
  String DBEXPORTED = lang("db_exported");
  /** Database not deleted. */
  String DBNOTDELETED = lang("db_notdeleted");
  /** Database exists already. */
  String DBEXIST = lang("db_exists");
  /** Database was dropped. */
  String DBBACKDROP = lang("db_backdrop");
  /** Backup was not found. */
  String DBBACKNF = lang("db_backnf");
  /** Database optimized. */
  String DBOPTIMIZED = lang("db_optimized");
  /** File not stored. */
  String DBNOTSTORED = lang("db_notstored");

  /** Index created. */
  String INDCREATED = lang("in_created");
  /** Index dropped. */
  String INDDROP = lang("in_dropped");
  /** Index not dropped. */
  String INDDROPERROR = lang("in_notdropped");
  /** Index not available. */
  String INDNOTAVL = lang("in_notavl");

  // DATABASE/INDEX INFORMATION ===============================================

  /** Index info. */
  String INDEXSTRUC = LI + "Structure: ";
  /** Index info. */
  String SIZEDISK = LI + "Size: ";
  /** Index info. */
  String IDXENTRIES = LI + "Entries: ";

  /** Index info. */
  String TRIESTRUC = "Trie";
  /** Index info. */
  String HASHSTRUC = "Hash";
  /** Index info. */
  String FUZZYSTRUC = "Fuzzy";
  /** Index info. */
  String TREESTRUC = "Binary tree";

  // XQUERY COMMAND ===========================================================

  /** Query info: query. */
  String QUERYQU = lang("qu_query") + COL + " ";
  /** Query info: optimizing. */
  String QUERYCOMP = lang("qu_comp") + COL;
  /** Query info: evaluating. */
  String QUERYEVAL = lang("qu_eval") + COL;
  /** Query info: querying. */
  String QUERYTIME = lang("qu_time") + COL;
  /** Query info: result . */
  String QUERYRESULT = lang("qu_result") + COLS;
  /** Query info: plan. */
  String QUERYPLAN = lang("qu_plan") + COLS;
  /** Query info: compiler. */
  String QUERYSEP = LI;

  /** Query info: query. */
  String QUERYSTRING = lang("qu_tabquery") + COLS;
  /** Query info: compiling. */
  String QUERYPARSE = lang("qu_tabpars") + COLS;
  /** Query info: compiling. */
  String QUERYCOMPILE = lang("qu_tabcomp") + COLS;
  /** Query info: evaluating. */
  String QUERYEVALUATE = lang("qu_tabeval") + COLS;
  /** Query info: time for printing. */
  String QUERYPRINT = lang("qu_tabprint") + COLS;
  /** Query info: total time. */
  String QUERYTOTAL = lang("qu_tabtotal") + COLS;
  /** Query hits. */
  String QUERYHITS = lang("qu_tabhits") + COLS;
  /** Query info: updated data. */
  String QUERYUPDATED = lang("qu_tabupdated") + COLS;
  /** Query info: printed data. */
  String QUERYPRINTED = lang("qu_tabprinted") + COLS;
  /** Query hits. */
  String HITS = lang("qu_hits");
  /** Insert query info. */
  String QUERYNODESERR = lang("qu_nodeserr");
  /** Query executed. */
  String QUERYEXEC = lang("qu_exec");

  /** Stopped info. */
  String STOPPED = lang("qu_stopped");
  /** Line info. */
  String LINEINFO = lang("qu_line");
  /** Column info. */
  String COLINFO = lang("qu_col");
  /** File info. */
  String FILEINFO = lang("qu_file");

  /** Query hits. */
  String VALHIT = "Item";
  /** Query hits. */
  String VALHITS = "Items";

  // USER COMMANDS ============================================================

  /** User name. */
  String[] USERHEAD = { "Username", "Read", "Write", "Create", "Admin" };
  /** Default admin user and password. */
  String ADMIN = "admin";

  // ADMIN COMMANDS ==========================================================

  /** Show databases. */
  String SRVDATABASES = lang("ad_databases");
  /** Show sessions. */
  String SRVSESSIONS = lang("ad_sessions");
  /** Show events. */
  String EVENTS = lang("ad_events");
  /** Show packages. */
  String PACKAGES = lang("ad_packages");
  /** Permission needed. */
  String PERMNO = lang("ad_permno");
  /** Invalid permissions. */
  String PERMINV = lang("ad_perminv");
  /** Permission granted. */
  String GRANT = lang("ad_grant");
  /** Permission granted on database. */
  String GRANTON = lang("ad_granton");
  /** User not found. */
  String USERNO = lang("ad_userno");
  /** User dropped. */
  String USERDROP = lang("ad_userdrop");
  /** User dropped from database. */
  String USERDROPON = lang("ad_userdropon");
  /** User is logged in. */
  String USERLOG = lang("ad_userlog");
  /** User added. */
  String USERCREATE = lang("ad_usercreate");
  /** Password changed. */
  String USERALTER = lang("ad_useralter");
  /** User unknown. */
  String USERKNOWN = lang("ad_userknown");
  /** Password is no valid MD5 hash. */
  String USERMD5 = lang("ad_usermd5");
  /** Admin user. */
  String USERADMIN = lang("ad_admin");
  /** Killed sessions. */
  String USERKILL = lang("ad_kill");
  /** User kills itself. */
  String USERKILLSELF = lang("ad_killself");
  /** Event dropped. */
  String EVENTDROP = lang("ad_eventdrop");
  /** Event added. */
  String EVENTCREATE = lang("ad_eventcreate");
  /** Event not found. */
  String EVENTNO = lang("ad_eventno");
  /** Already watching the event. */
  String EVENTALR = lang("ad_eventalr");
  /** Nothing to unwatch. */
  String EVENTNOUW = lang("ad_eventnouw");
  /** Event already exists. */
  String EVENTKNOWN = lang("ad_eventknown");
  /** Watch Event. */
  String EVENTWAT = lang("ad_eventatt");
  /** Unwatch Event. */
  String EVENTUNWAT = lang("ad_eventdet");
  /** Package deleted. */
  String REPODEL = lang("ad_repodelete");
  /** Package installed. */
  String REPOINST = lang("ad_repoinstall");

  // GENERAL COMMANDS =========================================================

  /** Invalid key. */
  String SETWHICH = lang("gc_setwhich") + DOT;
  /** Unknown command error. */
  String SETSIMILAR = lang("gc_setwhich") + "; " + lang("cmd_similar");
  /** Invalid value. */
  String SETVAL = lang("gc_setval");

  // INFO STRINGS =============================================================

  /** Waiting information. */
  String INFOWAIT = lang("info_wait") + DOTS;
  /** Index information. */
  String INFOINDEX = lang("info_index");
  /** Optimize information. */
  String INFOOPT = lang("info_opt") + DOTS;
  /** Statistics information. */
  String INFOSTATS = lang("info_stats") + DOTS;

  /** Info on source document. */
  String INFODBNAME = lang("info_dbname");
  /** Info on database size. */
  String INFODBSIZE = lang("info_dbsize");
  /** Info on storage type. */
  String INFOTYPE = lang("info_type");
  /** Info on path. */
  String INFOPATH = lang("info_path");
  /** Info on database time stamp. */
  String INFOTIME = lang("info_time");
  /** Info on number of documents. */
  String INFONRES = lang("info_nres");
  /** Info on document size. */
  String INFODOCSIZE = lang("info_docsize");
  /** Document encoding. */
  String INFOENCODING = lang("info_encoding");
  /** Info on number of nodes. */
  String INFONODES = lang("info_nodes");

  /** Info on used main memory. */
  String INFOMEM = lang("info_mem");

  /** No document opened. */
  String INFONODB = lang("info_nodb") + DOT;
  /** Info on database. */
  String INFODBERR = lang("info_dberror");

  /** Error info. */
  String INFOBROSERERR = lang("info_browsererror");

  /** Info on query info. */
 String INFOQUERY = lang("info_query");
  /** Info on debug mode. */
  String INFODEBUG = lang("info_debug");
  /** Info on result serialization. */
  String INFOSERIALIZE = lang("info_serialize");
  /** Info on whitespace chopping. */
  String INFOCHOP = lang("info_chop");
  /** Info on text indexing. */
  String INFOTEXTINDEX = lang("info_txtindex");
  /** Info on attribute indexing. */
  String INFOATTRINDEX = lang("info_atvindex");
  /** Info on full-text indexing. */
  String INFOFTINDEX = lang("info_ftindex");
  /** Info on path summary. */
  String INFOPATHINDEX = lang("info_pathindex");
  /** Info on up-to-date. */
  String INFOUPTODATE = lang("info_uptodate");
  /** Info on database path. */
  String INFODBPATH = lang("info_dbpath");

  /** Info on tags. */
  String INFOTAGS = lang("info_tags");
  /** Info on attributes. */
  String INFOATTS = lang("info_atts");
  /** Info on namespaces. */
  String INFONS = lang("info_ns");
  /** Info on wildcard indexing. */
  String INFOWCINDEX = lang("info_wcindex");
  /** Info on index. */
  String INFOOUTOFDATED = lang("info_outofdate");

  /** Info on database. */
  String INFODB = lang("info_db");
  /** Info on input resource. */
  String INFORESOURCE = lang("info_resource");
  /** Database info. */
  String INFOGENERAL = lang("info_general");
  /** Database info. */
  String RESULTCHOP = " (" + lang("info_resultchop") + ")";
  /** Option flag. */
  String INFOON = lang("info_on");
  /** Option flag. */
  String INFOOFF = lang("info_off");
  /** Error info. */
  String INFOERROR = lang("info_error") + COLS;
  /** Error info. */
  String INFOENTRIES = "(" + lang("info_entries") + ")";

  // MENU ENTRIES =============================================================

  /** Menu entry. */
  String MENUDB = lang("m_db");
  /** Menu entry. */
  String MENUQUERY = lang("m_query");
  /** Menu entry. */
  String MENUVIEW = lang("m_view");
  /** Menu entry. */
  String MENUNODES = lang("m_nodes");
  /** Menu entry. */
  String MENUOPTIONS = lang("m_options");
  /** Menu entry. */
  String MENUHELP = lang("m_help");

  // GUI COMMANDS =============================================================

  /** Command info. */
  String GUIABOUT = lang("c_about", NAME);
  /** Command info. */
  String GUIABOUTTT = lang("c_abouttt");
  /** Command info. */
  String GUICLOSE = lang("c_close");
  /** Command info. */
  String GUICLOSETT = lang("c_closett");
  /** Command info. */
  String GUICOLOR = lang("c_color");
  /** Command info. */
  String GUICOLORTT = lang("c_colortt");
  /** Command info. */
  String GUICUT = lang("c_cut");
  /** Command info. */
  String GUICOPY = lang("c_copy");
  /** Command info. */
  String GUIALL = lang("c_all");
  /** Command info. */
  String GUICPPATH = lang("c_cppath");
  /** Command info. */
  String GUICOPYTT = lang("c_copytt");
  /** Command info. */
  String GUICPPATHTT = lang("c_cppathtt");
  /** Command info. */
  String GUICREATE = lang("c_create");
  /** Command info. */
  String GUICREATETT = lang("c_creatett");
  /** Command info. */
  String GUIDELETE = lang("c_delete");
  /** Command info. */
  String GUIDELETETT = lang("c_deletett");
  /** Command info. */
  String GUIMANAGE = lang("c_manage");
  /** Command info. */
  String GUIMANAGETT = lang("c_managett");
  /** Command info. */
  String GUIEDIT = lang("c_edit");
  /** Command info. */
  String GUIEDITTT = lang("c_edittt");
  /** Command info. */
  String GUIEXIT = lang("c_exit");
  /** Command info. */
  String GUIEXITTT = lang("c_exittt");
  /** Command info. */
  String GUIEXPORT = lang("c_export");
  /** Command info. */
  String GUIEXPORTTT = lang("c_exporttt");
  /** Command info. */
  String GUIFILTER = lang("c_filter");
  /** Command info. */
  String GUIFILTERTT = lang("c_filtertt");
  /** Command info. */
  String GUIFONTS = lang("c_fonts") + DOTS;
  /** Command info. */
  String GUIFONTSTT = lang("c_fontstt");
  /** Command info. */
  String GUIFULL = lang("c_full");
  /** Command info. */
  String GUIFULLTT = lang("c_fulltt");
  /** Command info. */
  String GUIGOBACK = lang("c_goback");
  /** Command info. */
  String GUIGOFORWARD = lang("c_goforward");
  /** Command info. */
  String GUIGOUP = lang("c_goup");
  /** Command info. */
  String GUIGOUPTT = lang("c_gouptt");
  /** Command info. */
  String GUIPROPS = lang("c_props");
  /** Command info. */
  String GUIPROPSTT = lang("c_propstt");
  /** Command info. */
  String GUIADD = lang("c_add");
  /** Command info. */
  String GUIADDTT = lang("c_addtt");
  /** Command info. */
  String GUIDROP = lang("c_drop");
  /** Command info. */
  String GUIDROPTT = lang("c_droptt");
  /** Command info. */
  String GUIINSERT = lang("c_insert");
  /** Command info. */
  String GUIINSERTTT = lang("c_inserttt");
  /** Command info. */
  String GUIMAPLAYOUT = lang("c_maplayout") + DOTS;
  /** Command info. */
  String GUIMAPLAYOUTTT = lang("c_maplayouttt");
  /** Command info. */
  String GUITREEOPTIONS = lang("c_treeoptions") + DOTS;
  /** Command info. */
  String GUITREEOPTIONSTT = lang("c_treeoptionstt");
  /** Command info. */
  String GUIOPEN = lang("c_open");
  /** Command info. */
  String GUIOPENTT = lang("c_opentt");
  /** Command info. */
  String GUIPASTE = lang("c_paste");
  /** Command info. */
  String GUIPASTETT = lang("c_pastett");
  /** Command info. */
  String GUIPREFS = lang("c_prefs");
  /** Command info. */
  String GUIPREFSTT = lang("c_prefstt");
  /** Command info. */
  String GUIREDO = lang("c_redo");
  /** Command info. */
  String GUIROOT = lang("c_root");
  /** Command info. */
  String GUIROOTTT = lang("c_roottt");
  /** Command info. */
  String GUIRTEXEC = lang("c_rtexec");
  /** Command info. */
  String GUIRTEXECTT = lang("c_rtexectt");
  /** Command info. */
  String GUIRTFILTER = lang("c_rtfilter");
  /** Command info. */
  String GUIRTFILTERTT = lang("c_rtfiltertt");
  /** Command info. */
  String GUISERVER = lang("c_server");
  /** Command info. */
  String GUISERVERTT = lang("c_servertt");
  /** Command info. */
  String GUISHOWBUTTONS = lang("c_showbuttons");
  /** Command info. */
  String GUISHOWBUTTONSTT = lang("c_showbuttonstt");
  /** Command info. */
  String GUISHOWEXPLORE = lang("c_showexplore");
  /** Command info. */
  String GUISHOWEXPLORETT = lang("c_showexplorett");
  /** Command info. */
  String GUISHOWFOLDER = lang("c_showfolder");
  /** Command info. */
  String GUISHOWFOLDERTT = lang("c_showfoldertt");
  /** Command info. */
  String GUISHOWHELP = lang("c_showhelp");
  /** Command info. */
  String GUISHOWHELPTT = lang("c_showhelptt");
  /** Command info. */
  String GUISHOWCOMMUNITY = lang("c_community");
  /** Command info. */
  String GUISHOWCOMMUNITYTT = lang("c_communitytt");
  /** Command info. */
  String GUISHOWDOC = lang("c_doc");
  /** Command info. */
  String GUISHOWDOCTT = lang("c_doctt");
  /** Command info. */
  String GUISHOWUPDATES = lang("c_updates");
  /** Command info. */
  String GUISHOWUPDATESTT = lang("c_updatestt");
  /** Command info. */
  String GUISHOWINFO = lang("c_showinfo");
  /** Command info. */
  String GUISHOWINFOTT = lang("c_showinfott");
  /** Command info. */
  String GUISHOWINPUT = lang("c_showinput");
  /** Command info. */
  String GUISHOWINPUTTT = lang("c_showinputtt");
  /** Command info. */
  String GUISHOWMAP = lang("c_showmap");
  /** Command info. */
  String GUISHOWMAPTT = lang("c_showmaptt");
  /** Command info. */
  String GUISHOWMENU = lang("c_showmenu");
  /** Command info. */
  String GUISHOWMENUTT = lang("c_showmenutt");
  /** Command info. */
  String GUISHOWPLOT = lang("c_showplot");
  /** Command info. */
  String GUISHOWPLOTTT = lang("c_showplottt");
  /** Command info. */
  String GUISHOWSTATUS = lang("c_showstatus");
  /** Command info. */
  String GUISHOWSTATUSTT = lang("c_showstatustt");
  /** Command info. */
  String GUISHOWTABLE = lang("c_showtable");
  /** Command info. */
  String GUISHOWTABLETT = lang("c_showtablett");
  /** Command info. */
  String GUISHOWTEXT = lang("c_showtext");
  /** Command info. */
  String GUISHOWTEXTTT = lang("c_showtexttt");
  /** Command info. */
  String GUISHOWTREE = lang("c_showtree");
  /** Command info. */
  String GUISHOWTREETT = lang("c_showtreett");
  /** Command info. */
  String GUISHOWXQUERY = lang("c_showxquery");
  /** Command info. */
  String GUISHOWXQUERYTT = lang("c_showxquerytt");
  /** Command info. */
  String GUIUNDO = lang("c_undo");
  /** Command info. */
  String GUIXQNEW = lang("c_xqnew");
  /** Command info. */
  String GUIXQNEWTT = lang("c_xqnewtt");
  /** Command info. */
  String GUIXQOPEN = lang("c_xqopen");
  /** Command info. */
  String GUIXQOPENTT = lang("c_xqopentt");
  /** Help string. */
  String GUIXQCLOSE = lang("c_xqclose");
  /** Help string. */
  String GUIXQCLOSETT = lang("c_xqclosett");
  /** Command info. */
  String GUISAVE = lang("c_save");
  /** Command info. */
  String GUISAVETT = lang("c_savett");
  /** Command info. */
  String GUISAVEAS = lang("c_saveas");

  // BUTTONS ==================================================================

  /** Search mode. */
  String BUTTONSEARCH = lang("b_search");
  /** Command mode. */
  String BUTTONCMD = lang("b_cmd");
  /** XQuery mode. */
  String BUTTONXQUERY = lang("b_xquery");
  /** Button text for confirming actions. */
  String BUTTONOK = "  " + lang("b_ok") + "  ";
  /** Button text for choosing actions. */
  String BUTTONYES = "  " + lang("b_yes") + "  ";
  /** Button text for choosing actions. */
  String BUTTONNO = "  " + lang("b_no") + "  ";
  /** Button text for optimization. */
  String BUTTONOPT = lang("b_opt") + DOTS;
  /** Button text for renaming databases. */
  String BUTTONRENAME = lang("b_rename") + DOTS;
  /** Button text for creating backup of databases. */
  String BUTTONBACKUP = lang("b_backup");
  /** Button text for restoring databases. */
  String BUTTONRESTORE = lang("b_restore");
  /** Button text for copying databases. */
  String BUTTONCOPY = lang("b_copy");
  /** Button text for opening files. */
  String BUTTONOPEN = lang("b_open");
  /** Button text for canceling actions. */
  String BUTTONCANCEL = lang("b_cancel");
  /** Button text for deleting files. */
  String BUTTONDROP = lang("b_drop") + DOTS;
  /** Button text for browsing files/directories. */
  String BUTTONBROWSE = lang("b_browse") + DOTS;
  /** Button text for creating things. */
  String BUTTONCREATE = lang("b_create");
  /** Button text for alter password. */
  String BUTTONALTER = lang("b_alter") + DOTS;
  /** Button for starting the server. */
  String BUTTONSTART = lang("b_start");
  /** Button for starting the server. */
  String BUTTONSTOP = lang("b_stop");
  /** Button for connecting. */
  String BUTTONCONNECT = lang("b_connect");
  /** Button for disconnecting. */
  String BUTTONDISCONNECT = lang("b_disconnect");
  /** Button for refreshing. */
  String BUTTONREFRESH = lang("b_refresh");
  /** Button for deleting. */
  String BUTTONDELETE = lang("b_delete");
  /** Button for deleting all. */
  String BUTTONDELALL = lang("b_delall");
  /** Button for adding. */
  String BUTTONADD = lang("b_add");
  /** Button for resetting options. */
  String BUTTONRESET = lang("b_reset");

  // VISUALIZATIONS ===========================================================

  /** Help string. */
  String NODATA = lang("no_data") + DOT;
  /** Help string. */
  String NOSPACE = lang("no_space");
  /** Query info title. */
  String INFOTIT = lang("info_title");
  /** Query title. */
  String EXPLORETIT = lang("explore_title");
  /** Help title. */
  String HELPTIT = lang("help_title");
  /** Text title. */
  String TEXTTIT = lang("text_title");
  /** Editor title. */
  String EDITORTIT = lang("editor_title");
  /** Editor: new file. */
  String EDITORFILE = lang("editor_file");

  /** Plot visualization. */
  String PLOTLOG = "log";

  // DIALOG WINDOWS ===========================================================

  /** Open dialog - No database. */
  String DIALOGINFO = lang("d_info");
  /** Error info. */
  String DIALOGERR = lang("info_error");
  /** Dialog title for choosing a directory. */
  String DIALOGFC = lang("d_fctitle");

  /** Dialog title for choosing a file. */
  String CREATETITLE = lang("dc_choose");
  /** Use Catalog file Checkbox. */
  String USECATFILE = lang("dc_usecat");
  /** Use Catalog file not found on CP. */
  String USECATHLP = lang("dc_usecathlp");
  /** Use Catalog file not found on CP. */
  String USECATHLP2 = lang("dc_usecathlp2");

  /** Database creation filter. */
  String CREATEPATTERN = lang("dc_pattern");
  /** Name of database. */
  String CREATENAME = lang("dc_name") + COLS;
  /** Name of database copy. */
  String CREATENAMEC = lang("dc_namec") + COLS;
  /** Target path. */
  String CREATETARGET = lang("dc_target") + COLS;

  /** XML file description. */
  String CREATEXMLDESC = "XML Documents";
  /** HTML file description. */
  String CREATEHTMLDESC = "HTML Documents";
  /** CSV file description. */
  String CREATECSVDESC = "Comma-Separated Values";
  /** TXT file description. */
  String CREATETXTDESC = "Plain text";
  /** ZIP file description. */
  String CREATEZIPDESC = "ZIP Archives";
  /** GZ file description. */
  String CREATEGZDESC = "GZIP Archives";
  /** XQuery file extensions description. */
  String CREATEXQEXDESC = "XQuery Files";

  /** Dialog title for database options. */
  String CREATEADVTITLE = lang("dc_advtitle");
  /** Whitespaces information. */
  String CREATECHOP = lang("dc_chop");
  /** DTD information. */
  String CREATEDTD = lang("dc_dtd");
  /** Internal parser. */
  String CREATEINTPARSE = lang("dc_intparse");
  /** Parse archives. */
  String CREATEARCHIVES = lang("dc_archives");
  /** Skip corrupt files. */
  String CREATECORRUPT = lang("dc_corrupt");
  /** SAX parsing information. */
  String CREATEFORMAT = lang("dc_createformat") + COLS;

  /** Full-text index information. */
  String CREATEWC = lang("dc_wcindex");
  /** Full-text index information. */
  String CREATEST = lang("dc_ftstem");
  /** Full-text index information. */
  String CREATELN = lang("dc_ftlang");
  /** Full-text index information. */
  String CREATECS = lang("dc_ftcs");
  /** Full-text index information. */
  String CREATEDC = lang("dc_ftdc");
  /** Full-text index using stopword list. */
  String CREATESW = lang("dc_ftsw");
  /** Full-text scoring type. */
  String CREATESCT = lang("dc_ftsct");
  /** Full-text, document-based. */
  String CREATESCT1 = lang("dc_ftsct1");
  /** Full-text, document-based. */
  String CREATESCT2 = lang("dc_ftsct2");

  /** Whitespaces information. */
  String CHOPPINGINFO = lang("dc_chopinfo") + DOT;
  /** Internal parser information. */
  String INTPARSEINFO = lang("dc_intparseinfo") + DOT;
  /** Input format information. */
  String FORMATINFO = lang("dc_formatinfo") + DOT;
  /** CSV header information. */
  String HEADERINFO = lang("dc_headerinfo") + DOT;
  /** CSV Database format information. */
  String FORMINFO = lang("dc_forminfo") + COL;
  /** CSV Separator information. */
  String SEPARATORINFO = lang("dc_separatorinfo") + COL;
  /** TEXT Lines information. */
  String LINESINFO = lang("dc_linesinfo") + DOT;

  /** Path summary information. */
  String PATHINDEXINFO = lang("dc_pathinfo") + DOT;
  /** Text index information. */
  String TXTINDEXINFO = lang("dc_txtinfo") + DOT;
  /** Attribute value index information. */
  String ATTINDEXINFO = lang("dc_attinfo") + DOT;
  /** Full-text index information. */
  String FTINDEXINFO = lang("dc_ftxinfo") + DOT;
  /** Full-text index information. */
  String WCINDEXINFO = lang("dc_wcinfo") + DOT;
  /** Full-text index information. */
  String FTSTEMINFO = lang("dc_ftsteminfo") + DOT;
  /** Full-text index information. */
  String FTLANGINFO = lang("dc_ftlanginfo") + DOT;
  /** Full-text index information. */
  String FTCSINFO = lang("dc_ftcsinfo") + DOT;
  /** Full-text index information. */
  String FTDCINFO = lang("dc_ftdcinfo") + DOT;
  /** Full-text index information. */
  String FTSCINFO = lang("dc_ftscinfo") + DOT;
  /** Full-text index information. */
  String FTSWINFO = lang("dc_ftswinfo") + DOT;

  /** General info. */
  String GENERALINFO = lang("dc_general");
  /** General info. */
  String PARSEINFO = lang("dc_parse");
  /** Indexing info. */
  String NAMESINFO = lang("dc_names");
  /** Indexing info. */
  String INDEXINFO = lang("dc_index");
  /** Indexing info. */
  String FTINFO = lang("dc_ft");

  /** Dialog title for opening a database. */
  String OPENTITLE = lang("do_title");
  /** Dialog title for opening a large database. */
  String OPENLARGE = lang("do_large") + NL + " ";
  /** Dialog asking if a new database should be be created. */
  String NODBQUESTION = INFONODB + NL + lang("do_nodbquestion") + NL + " ";
  /** Dialog for downloading a new version. */
  String GUICHECKVER = lang("do_checkver");

  /** File dialog error. */
  String NOTOPENED = lang("c_notopened");
  /** File dialog error. */
  String NOTSAVED = lang("c_notsaved");
  /** File dialog replace information. */
  String FILEREPLACE = lang("c_replace");
  /** Dir dialog replace information. */
  String DIRREPLACE = lang("c_dirreplace");

  /** Server. */
  String LOCALSERVER = lang("ds_localserver");
  /** Users. */
  String CONNECT = lang("ds_connect");
  /** Users. */
  String USERS = lang("ds_users");
  /** Host. */
  String HOST = lang("ds_host");
  /** PORT. */
  String PORT = lang("ds_port");
  /** Local. */
  String LOCALPORT = lang("ds_localport");
  /** Create user. */
  String CREATEU = lang("ds_createu");
  /** Global permissions. */
  String GLOBPERM = lang("ds_globperm") + COLS;
  /** Local permissions. */
  String LOCPERM = lang("ds_locperm") + COLS;
  /** Question for dropping user. */
  String DRQUESTION = lang("ds_drquestion") + NL + lang("dd_sure");
  /** Question for revoking right from logged in user. */
  String DBREVOKE = lang("ds_dbrevoke") + NL + lang("dd_sure");
  /** Alter password. */
  String ALTERPW = lang("ds_alterpw");
  /** Invalid. */
  String INVALID = lang("ds_invalid") + DOT;
  /** Login. */
  String ADMINLOGIN = lang("ds_adlogin");
  /** Databases. */
  String DATABASES = lang("ds_databases");
  /** Backups. */
  String BACKUPS = lang("ds_backups");
  /** Sessions. */
  String SESSIONS = lang("ds_sessions");
  /** Logs. */
  String LOCALLOGS = lang("ds_locallogs");
  /** Connected. */
  String CONNECTED = lang("ds_connected");
  /** Disconnected. */
  String DISCONNECTED = lang("ds_disconnected");
  /** Server information. */
  String SERVERINFO1 = lang("ds_info1");
  /** Server information. */
  String SERVERINFO2 = lang("ds_info2");

  /** Dialog title for renaming a database. */
  String RENAMETITLE = lang("dr_title");
  /** Dialog title for dropping documents. */
  String DROPTITLE = lang("dr_title2");
  /** Dialog title for copying a database. */
  String COPYTITLE = lang("dr_title3");
  /** Info for overwriting a database. */
  String RENAMEOVER = lang("dr_over") + DOT;
  /** Info for creating an empty database. */
  String EMPTYDATABASE = lang("dr_empty") + DOT;
  /** % documents will be deleted. */
  String DELETEPATH = lang("dr_delete") + DOT;

  /** Dialog title for managing databases. */
  String MANAGETITLE = lang("dd_title");
  /** Dialog title for dropping a database. */
  String DROPCONF = lang("dd_question") + NL + lang("dd_sure");
  /** Availble databases. */
  String AVAILABLE = lang("dd_available") + COL;
  /** Database only available as backup. */
  String ONLYBACKUP = lang("dd_onlybac");

  /** Dialog title for import options. */
  String PREFSTITLE = lang("dp_title");
  /** Database path. */
  String DATABASEPATH = lang("dp_dbpath");
  /** Interactions. */
  String PREFINTER = lang("dp_inter");
  /** Look and feel. */
  String PREFLF = lang("dp_lf") + " (" + lang("dp_restart") + ")";
  /** Focus. */
  String PREFFOCUS = lang("dp_focus");
  /** Simple file dialog. */
  String SIMPLEFILE = lang("dp_simplefd");
  /** Name display flag. */
  String PREFNAME = lang("dp_names");
  /** Language preference. */
  String PREFLANG = lang("dp_lang") + " (" + lang("dp_restart") + ")";

  /** Dialog title for deleting nodes. */
  String DELETECONF = lang("dx_question");
  /** Dialog title for closing XQuery file. */
  String XQUERYCONF = lang("dq_question");

  /** Dialog title for exporting nodes. */
  String OUTDIR = lang("dx_outdir");
  /** Dialog title for exporting nodes. */
  String OVERFILE = lang("dx_overfile");
  /** Dialog title for exporting nodes. */
  String OUTINDENT = lang("dx_indent");

  /** Dialog title for inserting new data. */
  String INSERTTITLE = lang("dn_title");
  /** Dialog title for updating document data. */
  String EDITTITLE = lang("de_title");
  /** Insert name. */
  String EDITNAME = lang("de_name");
  /** Insert value. */
  String EDITVALUE = lang("de_value");
  /** Dialog title for updating text. */
  String EDITTEXT = lang("de_text");
  /** Dialog title for updating text. */
  String[] EDITKIND = { lang("de_kind1"), lang("de_kind2"), lang("de_kind3"),
      lang("de_kind4"), lang("de_kind5"), lang("de_kind6")
  };

  /** Dialog title for choosing a font. */
  String FONTTITLE = lang("df_title");
  /** Predefined font types. */
  String[] FONTTYPES = { lang("df_type1"), lang("df_type2"), lang("df_type3") };

  /** Dialog title for treemap color schema. */
  String SCHEMATITLE = lang("dy_title");
  /** Color schema information. */
  String SCHEMARED = lang("dy_red");
  /** Color schema information. */
  String SCHEMAGREEN = lang("dy_green");
  /** Color schema information. */
  String SCHEMABLUE = lang("dy_blue");

  /** Dialog title for treemap design. */
  String MAPLAYOUTTITLE = lang("dm_title");
  /** Show attributes. */
  String MAPATT = lang("dm_atts");
  /** Predefined number of layouts. */
  String[] MAPOFFSET = {
    lang("dm_choice1"), lang("dm_choice2"), lang("dm_choice3"),
    lang("dm_choice4"), lang("dm_choice5")
  };

  /** Dialog title for tree view options. */
  String TREEOPTIONSTITLE = lang("dt_title");
  /** Slim rectangles to text length. */
  String TREESLIM = lang("dt_slim");
  /** Show attributes. */
  String TREEATT = lang("dt_atts");

  /** Predefined number of layouts. */
  String[] MAPALG = {
    "Split Layout", "Strip Layout", "Squarified Layout",
    "Slice&Dice Layout", "Binary Layout"
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
  String ABOUTTITLE = lang("da_title", NAME);
  /** Copyright info. */
  String COPYRIGHT = "\u00A9 2005-11 " + COMPANY;
  /** License info. */
  String LICENSE = lang("da_license");
  /** Developer info. */
  String DEVELOPER = lang("da_dev") + ": Christian Gr\u00FCn";
  /** Contributors info. */
  String CONTRIBUTE1 = lang("da_cont1") +
  ": Michael Seiferle, Alexander Holupirek,";
  /** Developer names. */
  String CONTRIBUTE2 = "Dimitar Popov, Rositsa Shadura, Lukas Kircher,";
  /** Developer names. */
  String CONTRIBUTE3 = "Leo W\u00F6rteler, Andreas Weiler " + lang("da_cont2");
  /** Translation. */
  String TRANSLATION = lang("da_translation");

  // HELP TEXTS ===============================================================

  /** Help string. */
  byte[] HELPGO = token(lang("h_go"));
  /** Help string. */
  byte[] HELPSTOP = token(lang("h_stop"));
  /** Help string. */
  byte[] HELPHIST = token(lang("h_hist"));
  /** Help string. */
  byte[] HELPSAVE = token(lang("h_save"));
  /** Help string. */
  byte[] HELPRECENT = token(lang("h_recent"));
  /** Help dialog. */
  byte[] HELPCMD = token(lang("h_cmd"));
  /** Help dialog. */
  byte[] HELPSEARCHXML = token(lang("h_searchxml"));
  /** Help dialog. */
  byte[] HELPXPATH = token(lang("h_xpath"));
  /** Help string. */
  byte[] HELPMAP = token(lang("h_map"));
  /** Help string. */
  byte[] HELPTREE = token(lang("h_tree"));
  /** Help string. */
  byte[] HELPPLOT = token(lang("h_plot"));
  /** Help string. */
  byte[] HELPFOLDER = token(lang("h_folder"));
  /** Help string. */
  byte[] HELPTABLE = token(lang("h_table"));
  /** Help string. */
  byte[] HELPTEXT = token(lang("h_text"));
  /** Help string. */
  byte[] HELPINFOO = token(lang("h_info"));
  /** Help string. */
  byte[] HELPEXPLORE = token(lang("h_explore"));
  /** Help string. */
  byte[] HELPXQUERYY = token(lang("h_xquery"));
  /** Help string. */
  byte[] HELPMOVER = token(lang("h_mover"));

  /** Dummy string to check if all language strings have been assigned. */
  String DUMMY = lang(null);
}
