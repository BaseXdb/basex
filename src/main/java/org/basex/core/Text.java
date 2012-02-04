package org.basex.core;

import org.basex.core.Commands.*;
import static org.basex.core.Lang.lang;

import java.util.Locale;

/**
 * This class contains internationalized text strings, which are used
 * throughout the project. If this class is called first, the Strings
 * are initialized by the {@link org.basex.core.Lang} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public interface Text {

  // FREQUENTLY USED CHARACTERS ===============================================

  /** New line. */
  String NL = Prop.NL;
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
  String NAMELC = Prop.NAME.toLowerCase(Locale.ENGLISH);
  /** URL. */
  String URL = "http://" + NAMELC + ".org";
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
  String COMPANY = Prop.NAME + " Team";
  /** Title and version. */
  String TITLE = Prop.NAME + ' ' + VERSION;

  // CONSOLE INFO =============================================================

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
  String BUGINFO = "Improper use? Potential bug? Your feedback is welcome:";
  /** Console text. */
  String CONSOLE = TITLE + " [%]" + NL;

  /** Console text. */
  String TRY_MORE_X = lang("try_more_%", "help") + NL;
  /** Version information. */
  String VERSINFO = lang("version");

  /** Goodbye information. */
  String[] BYE = { lang("bye1"), lang("bye2"), lang("bye3"), lang("bye4") };

  // SERVER ===================================================================

  /** Server was started. */
  String SRV_STARTED = lang("srv_started");
  /** Server was stopped. */
  String SRV_STOPPED = lang("srv_stopped");
  /** Server is running or permission was denied. */
  String SRV_RUNNING = lang("srv_running");
  /** Ports was specified twice. */
  String PORT_TWICE_X = lang("port_twice_%");
  /** Unknown host. */
  String UNKNOWN_HOST = lang("unknown_host");
  /** Timeout exceeded. */
  String TIMEOUT_EXCEEDED = lang("timeout_exceeded");
  /** Connection error. */
  String CONNECTION_ERROR = lang("connection_error");
  /** Access denied. */
  String ACCESS_DENIED = lang("access_denied");
  /** User name. */
  String USERNAME = lang("username");
  /** Password. */
  String PASSWORD = lang("password");

  /** Localhost. */
  String LOCALHOST = "localhost";
  /** User name. */
  String[] USERHEAD = { "Username", "Read", "Write", "Create", "Admin" };
  /** Default admin user and password. */
  String ADMIN = "admin";

  /** Option flag. */
  String INFOON = lang("ON");
  /** Option flag. */
  String INFOOFF = lang("OFF");

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

  /** No help available. */
  String NOHELP = lang("no_help");

  /** Command help. */
  String[] HELPCREATE = {
    "[" + CmdCreate.BACKUP + "|" + CmdCreate.DATABASE + "|" + CmdCreate.EVENT +
    "|" + CmdCreate.INDEX + "|" + CmdCreate.USER + "] [...]",
    lang("c_create1"),
    lang("c_create2") + NL +
    LI + CmdDrop.BACKUP + " [" + C_NAME + "]:" + NL +
      "  " + lang("c_create4", C_NAME) + NL +
    LI + CmdCreate.DATABASE + " [" + C_NAME + "] ([" + C_INPUT + "]):"  + NL +
      "  " + lang("c_create3", C_NAME, C_INPUT) + NL +
    LI + CmdCreate.EVENT + " [" + C_NAME + "]: " + NL +
      "  " + lang("c_create9") + NL +
    LI + CmdCreate.INDEX + " [" + CmdIndex.TEXT + "|" + CmdIndex.ATTRIBUTE +
      "|" + CmdIndex.FULLTEXT + "|" + CmdIndex.PATH + "]:" + NL +
      "  " + lang("c_create5") + NL +
    LI + CmdCreate.USER + " [" + C_NAME + "] ([" + C_PW + "]):" + NL +
      "  " + lang("c_create8")
  };

  /** Command help. */
  String[] HELPCOPY = {
    "[" + C_NAME + "] [new" + C_NAME + "]", lang("c_copy1"), lang("c_copy2")
  };

  /** Command help. */
  String[] HELPCHECK = {
    "[" + C_INPUT + "]", lang("c_check1"), lang("c_check2")
  };
  /** Command help. */
  String[] HELPADD = {
    "(" + TO + " [" + C_PATH + "]) [" + C_INPUT + "]",
    lang("c_add1"), lang("c_add2", C_INPUT, C_PATH)
  };
  /** Command help. */
  String[] HELPSTORE = {
    "(" + TO + " [" + C_PATH + "]) [" + C_INPUT + "]",
    lang("c_store1"), lang("c_store2", C_PATH)
  };
  /** Command help. */
  String[] HELPRETRIEVE = {
    "[" + C_PATH + "]", lang("c_retrieve1"), lang("c_retrieve2", C_PATH)
  };
  /** Command help. */
  String[] HELPDELETE = {
    "[" + C_PATH  + "]", lang("c_delete1"), lang("c_delete2")
  };
  /** Command help. */
  String[] HELPOPEN = {
    "[" + C_PATH + "]", lang("c_open1"), lang("c_open2", C_PATH)
  };
  /** Command help. */
  String[] HELPINFO = {
    "([" + CmdInfo.DATABASE + "|" + CmdInfo.INDEX + "|" +
    CmdInfo.STORAGE + "])",
    lang("c_info1"),
    lang("c_info21") + NL +
    LI + lang("c_info22") + NL +
    LI + CmdInfo.DATABASE + ": " + lang("c_info23") + NL +
    LI + CmdInfo.INDEX + ": " + lang("c_info24") + NL +
    LI + CmdInfo.STORAGE + " [start end] | [" + C_QUERY + "]: " +
    lang("c_info25")
  };
  /** Command help. */
  String[] HELPCLOSE = {
    "", lang("c_close1"), lang("c_close2")
  };
  /** Command help. */
  String[] HELPLIST = {
    "([" + C_PATH + "])", lang("c_list1"), lang("c_list2", C_PATH)
  };
  /** Command help. */
  String[] HELPDROP = {
    "[" + CmdDrop.BACKUP + "|" + CmdDrop.DATABASE + "|" + "|" + CmdDrop.EVENT +
      CmdDrop.INDEX + "|" + CmdDrop.USER + "] [...]",
    lang("c_drop1"),
    lang("c_drop2") + NL +
    LI + CmdDrop.BACKUP + " [" + C_NAME + "]:" + NL +
      "  " + lang("c_drop24", C_NAME) + NL +
    LI + CmdDrop.DATABASE + " [" + C_NAME + "]:" + NL +
      "  " + lang("c_drop21") + NL +
    LI + CmdDrop.EVENT + " [" + C_NAME + "]:" + NL +
      "  " + lang("c_drop25", C_NAME) + NL +
    LI + CmdDrop.INDEX + " [" + CmdIndex.PATH + "|" + CmdIndex.TEXT + "|" +
      CmdIndex.ATTRIBUTE + "|" + CmdIndex.FULLTEXT + "]:" + NL +
      "  " + lang("c_drop22") + NL +
    LI + CmdDrop.USER + " [" + C_NAME + "] (" + ON + " [database]): " + NL +
      "  " + lang("c_drop23")
  };
  /** Command help. */
  String[] HELPEXPORT = {
    "[" + C_PATH + "]", lang("c_export1"), lang("c_export2", C_PATH)
  };
  /** Command help. */
  String[] HELPOPTIMIZE = {
    "(" + ALL + ")", lang("c_optimize1"), lang("c_optimize2", ALL)
  };

  /** Command help. */
  String[] HELPXQUERY = {
    "[" + C_QUERY + "]", lang("c_xquery1"), lang("c_xquery2")
  };
  /** Command help. */
  String[] HELPFIND = {
    "[keywords]", lang("c_find1"), lang("c_find2")
  };
  /** Command help. */
  String[] HELPFLUSH = {
    "", lang("c_flush1"), lang("c_flush2")
  };
  /** Command help. */
  String[] HELPRUN = {
    "[" + C_PATH + "]", lang("c_run1"), lang("c_run2", C_PATH)
  };
  /** Command help. */
  String[] HELPCS = {
    "[" + C_QUERY + "]", lang("c_cs1"), lang("c_cs2")
  };
  /** Command help. */
  String[] HELPKILL = {
    "[" + C_NAME + "]", lang("c_kill1"), lang("c_kill2")
  };
  /** Command help. */
  String[] HELPRENAME = {
    "[" + C_PATH  + "] [newpath]", lang("c_rename1"), lang("c_rename2")
  };
  /** Command help. */
  String[] HELPREPLACE = {
    "[" + C_PATH  + "] [" + C_INPUT + "]",
    lang("c_replace1"), lang("c_replace2")
  };
  /** Command help. */
  String[] HELPRESTORE = {
    "[" + C_NAME + "-(date)]", lang("c_restore1"), lang("c_restore2")
  };
  /** Command help. */
  String[] HELPSHOW = {
    "[" + CmdShow.BACKUPS + "|" + CmdShow.DATABASES + "|" + CmdShow.EVENTS +
    "|" + CmdShow.SESSIONS + "|" + CmdShow.USERS + "]",
    lang("c_show1"),
    lang("c_show21") + NL +
    LI + CmdShow.DATABASES + ": " + lang("c_show22") + NL +
    LI + CmdShow.EVENTS + ": " + lang("c_show26") + NL +
    LI + CmdShow.SESSIONS + ": " + lang("c_show23") + NL +
    LI + CmdShow.USERS + " (" + ON + " [database]): " + lang("c_show24") + NL +
    LI + CmdShow.BACKUPS + ": " + lang("c_show25")
  };
  /** Command help. */
  String[] HELPGRANT = {
    "[" + CmdPerm.NONE + "|" + CmdPerm.READ + "|" + CmdPerm.WRITE + "|" +
    CmdPerm.CREATE + "|" + CmdPerm.ADMIN + "] (" + ON + " [database]) " + TO +
    " [user]",
    lang("c_grant1"),
    lang("c_grant2")
  };
  /** Command help. */
  String[] HELPALTER = {
    "[" + CmdCreate.DATABASE + "|" + CmdCreate.USER + "] [...]",
    lang("c_alter1"),
    lang("c_alter2") + NL  +
    LI + CmdCreate.DATABASE + " [" + C_NAME + "] [newname]" + NL +
    "  " + lang("c_alterdb") + NL +
    LI + CmdCreate.USER  + " [" + C_NAME + "] ([" + C_PW + "]):" + NL +
    "  " + lang("c_alterpw")
  };
  /** Command help. */
  String[] HELPSET = {
    "[option] ([value])",
    lang("c_set1", "info"),
    lang("c_set2", "option", "value")
  };
  /** Command help. */
  String[] HELPGET = {
      "[option]", lang("c_get1", "info"), lang("c_get2", "option")
  };
  /** Command help. */
  String[] HELPPASSWORD = {
    "([" + C_PW + "])", lang("c_password1"), lang("c_password2")
  };
  /** Command help. */
  String[] HELPREPO = {
      "[" + CmdRepo.DELETE + "|" + CmdRepo.INSTALL + "|" + CmdRepo.LIST + "]",
      lang("c_repo1"),
      lang("c_repo2") + NL +
      LI + CmdRepo.DELETE + " [" + C_PKGNAME + "|" + C_PKGDIR + "]:" +  NL +
      "  " + lang("c_repo3", C_PKGNAME, C_PKGDIR) + NL +
      LI + CmdRepo.INSTALL + " [" + C_PKGPATH + "]:" + NL +
      "  " + lang("c_repo4", C_PKGPATH) + NL +
      LI + CmdRepo.LIST + ":" + NL +
      "  " + lang("c_repo5")
  };
  /** Command help. */
  String[] HELPHELP = {
    "([command])", lang("c_help1", Prop.NAME), lang("c_help2", "command")
  };
  /** Command help. */
  String[] HELPEXIT = {
    "", lang("c_exit1", Prop.NAME), lang("c_exit2", Prop.NAME)
  };

  // COMMAND INFOS ============================================================

  /** Command timing information. */
  String TIME_NEEDED_X = lang("time_needed") + ": %";
  /** Command syntax information. */
  String SYNTAX_X = lang("syntax") + ": %";
  /** Command execution error. */
  String EXEC_ERROR = lang("exec_error_%") + COL + NL + "%";

  /** No database error. */
  String NO_DB_OPENED = lang("no_db_opened");
  /** Main memory error. */
  String NO_MAINMEM = lang("no_mainmem");
  /** Out of memory error. */
  String OUT_OF_MEM = lang("out_of_mem");
  /** Progress exception. */
  String INTERRUPTED = lang("interrupted");

  /** Unknown command error. */
  String EXPECTING_CMD = lang("expecting_cmd");
  /** Unknown command error. */
  String UNKNOWN_CMD_X = lang("unknown_cmd_%");
  /** Unknown command error. */
  String UNKNOWN_TRY_X = UNKNOWN_CMD_X + ' ' + lang("try_%", "HELP");
  /** Try "help [...]" to get.. */
  String TRY_SPECIFIC_X = lang("try_specific_%", "HELP [...]") + NL;
  /** Unknown command error. */
  String UNKNOWN_SIMILAR_X = UNKNOWN_CMD_X + ' ' + lang("similar_cmd_%");

  // CREATE COMMAND ===========================================================

  /** Create database information. */
  String CREATING_DB = lang("creating_db");
  /** Create index information. */
  String CREATING_INDEXES = lang("creating_indexes");
  /** Possible corruption. */
  String DB_CORRUPT = lang("db_corrupt");
  /** Builder error. */
  String CREATION_CANCELED = lang("creation_canceled");
  /** Create database information. */
  String NODES_PARSED_X = " \"%\" (" + lang("nodes_parsed_%") + ")";
  /** Scanner position. */
  String SCANPOS_X_X = "\"%\" (" + lang("line") + " %)";

  /** Finish database creation. */
  String FINISHING_D = lang("finishing") + DOTS;
  /** Create text index. */
  String INDEX_TEXT_D = lang("index_text") + DOTS;
  /** Create attribute index. */
  String INDEX_ATTRIBUTES_D = lang("index_attributes") + DOTS;
  /** Create full-text index. */
  String INDEX_FULLTEXT_D = lang("index_fulltext") + DOTS;

  /** Database created. */
  String DB_CREATED_X_X = lang("db_created_%_%");
  /** Path added. */
  String PATH_ADDED_X_X = lang("path_added_%_%");
  /** Path deleted. */
  String DOCS_DELETED_X_X = lang("docs_deleted_%_%");
  /** Path renamed. */
  String DOCS_RENAMED_X_X = lang("docs_renamed_%_%");
  /** Path replaced. */
  String DOCS_REPLACED_X_X = lang("docs_replaced_%_%");
  /** Parse error. */
  String NOT_PARSED_X = lang("not_parsed_%");

  /** File not found. */
  String FILE_NOT_FOUND_X = lang("file_not_found_%");
  /** Path not found. */
  String FILE_NOT_FOUND = lang("file_not_found");
  /** Skipped corrupt files. */
  String SKIPPED = lang("skipped");
  /** Info on skipped corrupt files. */
  String MORE_SKIPPED_X = lang("more_skipped_%");
  /** Missing database name. */
  String ENTER_DB_NAME = lang("enter_db_name");
  /** No tokenizer found. */
  String NO_TOKENIZER_X = lang("no_tokenizer_%");
  /** No stemmer found. */
  String NO_STEMMER_X = lang("no_stemmer_%");
  /** Points to a directory. */
  String NO_DIR_ALLOWED_X = lang("no_dir_allowed_%");

  // DATABASE COMMANDS ========================================================

  /** Database not found. */
  String DB_NOT_FOUND_X = lang("db_not_found_%");
  /** Name invalid. */
  String NAME_INVALID_X = lang("name_invalid_%");
  /** Database pinned. */
  String DB_PINNED_X = lang("db_pinned_%");
  /** Database updated. */
  String DB_UPDATED_X = lang("db_updated_%");
  /** Database closed. */
  String DB_CLOSED_X = lang("db_closed_%");
  /** Database not closed. */
  String DB_NOT_CLOSED_X = lang("db_not_closed_%");
  /** Database optimized. */
  String DB_OPTIMIZED_X = lang("db_optimized_%");
  /** Database dropped. */
  String DB_DROPPED_X = lang("db_dropped_%");
  /** Database not dropped. */
  String NO_DB_DROPPED = lang("no_db_dropped");
  /** Database not dropped. */
  String DB_NOT_DROPPED_X = lang("db_not_dropped_%");
  /** Database altered. */
  String DB_RENAMED_X = lang("db_renamed_%");
  /** Database not dropped. */
  String DB_NOT_RENAMED_X = lang("db_not_renamed_%");
  /** Database flushed. */
  String DB_FLUSHED_X = lang("db_flushed_%");
  /** Backup of database created. */
  String DB_BACKUP_X = lang("db_backup_%");
  /** Backup of database not created. */
  String DB_NOT_BACKUP_X = lang("db_not_backup_%");
  /** Copy of database created. */
  String DB_COPIED_X = lang("db_copied_%");
  /** Copy of database not created. */
  String DB_NOT_COPIED_X = lang("db_not_copied_%");
  /** Database restored. */
  String DB_RESTORED_X = lang("db_restored_%");
  /** Database not restored. */
  String DB_NOT_RESTORED_X = lang("db_not_restored_%");
  /** Database opened. */
  String DB_OPENED_X = lang("db_opened_%");
  /** Database not opened. */
  String DB_NOT_OPENED_X = lang("db_not_opened_%");
  /** Database exported. */
  String DB_EXPORTED_X = lang("db_exported_%");
  /** Database not deleted. */
  String FILE_NOT_DELETED_X = lang("file_not_deleted_%");
  /** Database exists already. */
  String DB_EXISTS_X = lang("db_exists_%");
  /** Database was dropped. */
  String BACKUP_DROPPED_X = lang("backup_dropped_%");
  /** Backup was not found. */
  String BACKUP_NOT_FOUND_X = lang("backup_not_found_%");
  /** File not stored. */
  String FILE_NOT_STORED_X = lang("file_not_stored_%");

  /** Index created. */
  String INDEX_CREATED_X_X = lang("index_created_%_%");
  /** Index dropped. */
  String INDEX_DROPPED_X_X = lang("index_dropped_%_%");
  /** Index not dropped. */
  String INDEX_NOT_DROPPED_X = lang("index_not_dropped_%");
  /** Index not available. */
  String NOT_AVAILABLE = lang("not_available");

  // DATABASE/INDEX INFORMATION ===============================================

  /** Index info. */
  String LI_STRUCTURE = LI + "Structure: ";
  /** Index info. */
  String LI_SIZE = LI + "Size: ";
  /** Index info. */
  String LI_ENTRIES = LI + "Entries: ";

  /** Index info. */
  String TRIE = "Trie";
  /** Index info. */
  String HASH = "Hash";
  /** Index info. */
  String FUZZY = "Fuzzy";
  /** Index info. */
  String SORTED_LIST = "Sorted List";

  // XQUERY COMMAND ===========================================================

  /** Query info: query. */
  String QUERY_C = lang("query") + COL;
  /** Query info: compiling. */
  String COMPILING_C = lang("compiling") + COL;
  /** Query info: evaluating. */
  String EVALUATING_C = lang("evaluating") + COL;
  /** Query info: querying. */
  String TIMING_C = lang("timing") + COL;
  /** Query info: result . */
  String RESULT_C = lang("result") + COLS;
  /** Query info: plan. */
  String QUERY_PLAN_C = lang("query_plan") + COLS;
  /** Query info: separator. */
  String QUERYSEP = LI;

  /** "Query: ". */
  String QUERY_CC = lang("query") + COLS;
  /** "Parsing: ". */
  String PARSING_CC = lang("parsing") + COLS;
  /** "Compiling: ". */
  String COMPILING_CC = lang("compiling") + COLS;
  /** "Evaluating: ". */
  String EVALUATING_CC = lang("evaluating") + COLS;
  /** "Printing: ". */
  String PRINTING_CC = lang("printing") + COLS;
  /** "Total time: ". */
  String TOTAL_TIME_CC = lang("total_time") + COLS;
  /** "Hit(s): ". */
  String HITS_X_CC = lang("hit_s") + COLS;
  /** "Updated: ". */
  String UPDATED_CC = lang("updated") + COLS;
  /** "Printed: ". */
  String PRINTED_CC = lang("printed") + COLS;
  /** "Results: %". */
  String RESULTS_X = lang("results_%");
  /** "Query must yield database nodes.". */
  String NO_DB_NODES = lang("no_db_nodes");
  /** "Query executed in %". */
  String QUERY_EXECUTED_X = lang("query_executed_%");

  /** Stopped info. */
  String STOPPED_AT = lang("stopped_at");
  /** Line info. */
  String LINE_X = lang("line_%");
  /** Column info. */
  String COLUMN_X = lang("column_%");
  /** File info. */
  String IN_FILE_X = lang("in_file_%");

  /** Query hits. */
  String ITEM = "Item";
  /** Query hits. */
  String ITEMS = "Items";

  // ADMIN COMMANDS ==========================================================

  /** Show databases. */
  String OPENED_DB_X = lang("opened_db_%");
  /** Show sessions. */
  String SESSIONS_X = lang("sessions_%");
  /** Show events. */
  String EVENTS_X = lang("events_%");
  /** Show packages. */
  String PACKAGES_X = lang("packages_%");
  /** Permission needed. */
  String PERM_NEEDED_X = lang("perm_needed_%");
  /** Invalid permissions. */
  String PERM_UNKNOWN_X = lang("perm_unknown_%");
  /** Permission granted. */
  String GRANTED_X_X = lang("granted_%_%");
  /** Permission granted on database. */
  String GRANTED_ON_X_X_X = lang("granted_%_%_%");
  /** Unknown user. */
  String UNKNOWN_USER_X = lang("unknown_user_%");
  /** User dropped. */
  String USER_DROPPED_X = lang("user_dropped_%");
  /** User dropped from database. */
  String USER_DROPPED_X_X = lang("user_dropped_%_%");
  /** User is logged in. */
  String USER_LOGGED_IN_X = lang("user_logged_in_%");
  /** User added. */
  String USER_CREATED_X = lang("user_created_%");
  /** Password changed. */
  String PW_CHANGED_X = lang("pw_changed_%");
  /** User unknown. */
  String USER_EXISTS_X = lang("user_exists_%");
  /** Password is no valid MD5 hash. */
  String PW_NOT_VALID = lang("pw_not_valid");
  /** Admin user. */
  String ADMIN_STATIC_X = lang("admin_static_%");
  /** Killed sessions. */
  String SESSIONS_KILLED_X = lang("sessions_killed_%");
  /** User kills itself. */
  String KILL_SELF_X = lang("kill_self_%");
  /** Event dropped. */
  String EVENT_DROPPED_X = lang("event_dropped_%");
  /** Event added. */
  String EVENT_CREATED_X = lang("event_created_%");
  /** Event not found. */
  String EVENT_UNKNOWN_X = lang("event_unknown_%");
  /** Already watching the event. */
  String EVENT_WATCHED_X = lang("event_watched_%");
  /** Nothing to unwatch. */
  String EVENT_NOT_WATCHED_X = lang("event_not_watched_%");
  /** Event already exists. */
  String EVENT_EXISTS_X = lang("event_exists_%");
  /** Watch Event. */
  String WATCHING_EVENT_X = lang("watching_event_%");
  /** Unwatch Event. */
  String UNWATCHING_EVENT_X = lang("unwatching_event_%");
  /** Package deleted. */
  String PKG_DELETED_X = lang("pkg_deleted_%");
  /** Package installed. */
  String PKG_INSTALLED_X = lang("pkg_installed_%");

  // GENERAL COMMANDS =========================================================

  /** Invalid key. */
  String UNKNOWN_OPTION_X = lang("unknown_option_%");
  /** Unknown command error. */
  String UNKNOWN_OPT_SIMILAR_X =
      lang("unknown_option_%") + ' ' + lang("similar_cmd_%");
  /** Invalid value. */
  String INVALID_VALUE_X_X = lang("invalid_value_%_%");

  // INFO STRINGS =============================================================

  /** Waiting information. */
  String PLEASE_WAIT_D = lang("please_wait") + DOTS;
  /** Optimize information. */
  String OPTIMIZING_DB_D = lang("optimizing_db") + DOTS;
  /** Statistics information. */
  String CREATE_STATS_D = lang("create_stats") + DOTS;

  /** "Name". */
  String NAME = lang("name");
  /** "Size". */
  String SIZE = lang("size");
  /** "Type". */
  String TYPE = lang("type");
  /** "Input path". */
  String INPUT_PATH = lang("input_path");
  /** "Timestamp". */
  String TIMESTAMP = lang("timestamp");
  /** "Resources". */
  String RESOURCES = lang("resources");
  /** "Documents". */
  String DOCUMENTS = lang("documents");
  /** "Binaries". */
  String BINARIES = lang("binaries");
  /** "Input Size". */
  String INPUT_SIZE = lang("input_size");
  /** "Encoding". */
  String ENCODING = lang("encoding");

  /** Info on whitespace chopping. */
  String WS_CHOPPING = lang("ws_chopping");
  /** Info on text indexing. */
  String TEXT_INDEX = lang("text_index");
  /** Info on attribute indexing. */
  String ATTRIBUTE_INDEX = lang("attribute_index");
  /** Info on full-text indexing. */
  String FULLTEXT_INDEX = lang("fulltext_index");
  /** Info on path summary. */
  String PATH_INDEX = lang("path_index");
  /** Info on up-to-date. */
  String UP_TO_DATE = lang("up_to_date");

  /** Info on tags. */
  String ELEMENTS = lang("elements");
  /** Info on attributes. */
  String ATTRIBUTES = lang("attributes");
  /** Info on namespaces. */
  String NAMESPACES = lang("namespaces");
  /** Info on wildcard indexing. */
  String WILDCARDS = lang("wildcards");
  /** Info on index. */
  String OUT_OF_DATE = lang("out_of_date");

  /** "Database Properties". */
  String DB_PROPS = lang("db_props");
  /** "Resource Properties". */
  String RESOURCE_PROPS = lang("resource_props");
  /** "General Information". */
  String GENERAL_INFO = lang("general_info");
  /** "Main Options". */
  String MAIN_OPTIONS = lang("main_options");

  /** "(chopped)". */
  String CHOPPED = " (" + lang("chopped") + ")";
  /** "(% entries)". */
  String ENTRIES = "(" + lang("entries_%") + ")";
  /** "Directory". */
  String DIRECTORY = lang("directory");
  /** "Error". */
  String ERROR = lang("error");
  /** "Error:". */
  String ERROR_C = ERROR + COLS;

  // MENU ENTRIES =============================================================

  /** "Database". */
  String DATABASE = lang("database");
  /** "Editor". */
  String EDITOR = lang("editor");
  /** "View". */
  String VIEW = lang("view");
  /** "Nodes". */
  String NODES = lang("nodes");
  /** "Options". */
  String OPTIONS = lang("options");
  /** "Help". */
  String HELP = lang("help");

  // GUI COMMANDS =============================================================

  /** Command info. */
  String ABOUT = lang("about_%", Prop.NAME);
  /** Command info. */
  String CLOSE = lang("close");
  /** Command info. */
  String COLORS = lang("colors");
  /** Command info. */
  String CUT = lang("cut");
  /** Command info. */
  String COPY = lang("copy");
  /** Command info. */
  String SELECT_ALL = lang("select_all");
  /** Command info. */
  String COPY_PATH = lang("copy_path");
  /** Command info. */
  String NEW = lang("new");
  /** Command info. */
  String DELETE = lang("delete");
  /** Command info. */
  String OPEN_MANAGE = lang("open_manage");
  /** Command info. */
  String EDIT = lang("edit");
  /** Command info. */
  String EXIT = lang("exit");
  /** Command info. */
  String EXPORT_XML = lang("export_xml");
  /** Command info. */
  String FILTER_SELECTED = lang("filter_selected");
  /** Command info. */
  String FULLSCREEN = lang("fullscreen");
  /** Command info. */
  String GO_BACK = lang("go_back");
  /** Command info. */
  String GO_FORWARD = lang("go_forward");
  /** Command info. */
  String GO_UP = lang("go_up");
  /** Command info. */
  String PROPERTIES = lang("properties");
  /** Command info. */
  String PASTE = lang("paste");
  /** Command info. */
  String PREFERENCES = lang("preferences");
  /** Command info. */
  String REDO = lang("redo");
  /** Command info. */
  String GO_HOME = lang("go_home");
  /** Command info. */
  String RT_EXECUCTION = lang("rt_execution");
  /** Command info. */
  String RT_FILTERING = lang("rt_filtering");
  /** Command info. */
  String BUTTONS = lang("buttons");
  /** Command info. */
  String EXPLORER = lang("explorer");
  /** Command info. */
  String FOLDER = lang("folder");
  /** Command info. */
  String COMMUNITY = lang("community");
  /** Command info. */
  String CHECK_FOR_UPDATES = lang("check_for_updates");
  /** Command info. */
  String QUERY_INFO = lang("query_info");
  /** Command info. */
  String INPUT_BAR = lang("input_bar");
  /** Command info. */
  String MAP = lang("map");
  /** Command info. */
  String PLOT = lang("plot");
  /** Command info. */
  String STATUS_BAR = lang("status_bar");
  /** Command info. */
  String TABLE = lang("table");
  /** Command info. */
  String TEXT = lang("text");
  /** Command info. */
  String TREE = lang("tree");
  /** Command info. */
  String UNDO = lang("undo");
  /** Command info. */
  String OPEN = lang("open");
  /** Command info. */
  String SAVE = lang("save");
  /** Command info. */
  String SAVE_AS = lang("save_as");

  /** Command info. */
  String FONTS_D = lang("fonts") + DOTS;
  /** Command info. */
  String MAP_LAYOUT_D = lang("map_layout") + DOTS;
  /** Command info. */
  String TREE_OPTIONS_D = lang("tree_options") + DOTS;

  // BUTTONS ==================================================================

  /** Button text for confirming actions. */
  String B_OK = "  " + lang("ok") + "  ";
  /** Button text for choosing actions. */
  String B_YES = "  " + lang("yes") + "  ";
  /** Button text for choosing actions. */
  String B_NO = "  " + lang("no") + "  ";

  /** Search mode. */
  String SEARCH = lang("search");
  /** Command mode. */
  String COMMAND = lang("command");
  /** XQuery mode. */
  String XQUERY = lang("xquery");
  /** Button text for creating backup of databases. */
  String BACKUP = lang("backup");
  /** Button text for canceling actions. */
  String CANCEL = lang("cancel");

  /** Button text for optimization. */
  String OPTIMIZE_D = lang("optimize") + DOTS;
  /** Button text for renaming databases. */
  String RENAME_D = lang("rename") + DOTS;
  /** Button text for restoring databases. */
  String RESTORE_D = lang("restore") + DOTS;
  /** Button text for copying databases. */
  String COPY_D = lang("copy") + DOTS;
  /** Button text for deleting files. */
  String DROP_D = lang("drop") + DOTS;
  /** Button text for browsing files/directories. */
  String BROWSE_D = lang("browse") + DOTS;

  /** Button text for creating things. */
  String CREATE = lang("create");
  /** Button for starting the server. */
  String START = lang("start");
  /** Button for starting the server. */
  String STOP = lang("stop");
  /** Button for connecting. */
  String CONNECT = lang("connect");
  /** Button for disconnecting. */
  String DISCONNECT = lang("disconnect");
  /** Button for refreshing. */
  String REFRESH = lang("refresh");
  /** Button for deleting all. */
  String DELETE_ALL = lang("delete_all");
  /** Button for adding. */
  String ADD = lang("add");
  /** Button for resetting options. */
  String RESET = lang("reset");
  /** Clear button. */
  String CLEAR = lang("clear");
  /** Filter button. */
  String FILTER = lang("filter");

  // VISUALIZATIONS ===========================================================

  /** "No data available.". */
  String NO_DATA = lang("no_data");
  /** "Not enough space". */
  String NO_SPACE = lang("no_space");
  /** "file". */
  String FILE = lang("file");

  /** "log". */
  String PLOTLOG = "log";

  // DIALOG WINDOWS ===========================================================

  /** Open dialog - No database. */
  String INFORMATION = lang("information");
  /** Dialog title for choosing a directory. */
  String CHOOSE_DIR = lang("choose_dir");

  /** Dialog title for choosing a file. */
  String FILE_OR_DIR = lang("file_or_dir");
  /** Use Catalog file Checkbox. */
  String USE_CATALOG_FILE = lang("use_catalog_file");
  /** Use Catalog file not found on CP. */
  String HELP1_USE_CATALOG = lang("help1_use_catalog");
  /** Use Catalog file not found on CP. */
  String HELP2_USE_CATALOG = lang("help2_use_catalog");

  /** Database creation filter. */
  String FILE_PATTERNS = lang("file_patterns");
  /** Name of database. */
  String NAME_OF_DB = lang("name_of_db");
  /** Name of database copy. */
  String NAME_OF_DB_COPY = lang("name_of_db_copy");
  /** Target path. */
  String TARGET_PATH = lang("target_path");

  /** XML file description. */
  String XML_DOCUMENTS = "XML Documents";
  /** HTML file description. */
  String HTML_DOCUMENTS = "HTML Documents";
  /** CSV file description. */
  String CSV_DOCUMENTS = "Comma-Separated Values";
  /** TXT file description. */
  String PLAIN_TEXT = "Plain text";
  /** ZIP file description. */
  String ZIP_ARCHIVES = "ZIP Archives";
  /** GZ file description. */
  String GZIP_ARCHIVES = "GZIP Archives";
  /** XQuery file extensions description. */
  String XQUERY_FILES = "XQuery Files";

  /** Dialog title for database options. */
  String CREATE_DATABASE = lang("create_database");
  /** Whitespaces information. */
  String CHOP_WS = lang("chop_ws");
  /** DTD information. */
  String PARSE_DTDS = lang("parse_dtds");
  /** Internal parser. */
  String INT_PARSER = lang("int_parser");
  /** Parse files inside archives. */
  String PARSE_ARCHIVES = lang("parse_archives");
  /** Add remaining files as raw files. */
  String ADD_RAW_FILES = lang("add_raw_files");
  /** Skip corrupt files. */
  String SKIP_CORRUPT_FILES = lang("skip_corrupt_files");
  /** SAX parsing information. */
  String INPUT_FORMAT = lang("input_format") + COLS;

  /** Full-text index information. */
  String SUPPORT_WILDCARDS = lang("support_wildcards");
  /** Full-text index information. */
  String STEMMING = lang("stemming");
  /** Full-text index information. */
  String LANGUAGE = lang("language");
  /** Full-text index information. */
  String CASE_SENSITIVITY = lang("case_sensitivity");
  /** Full-text index information. */
  String DIACRITICS = lang("diacritics");
  /** Full-text index using stopword list. */
  String STOPWORD_LIST = lang("stopword_list");
  /** Full-text scoring type. */
  String TFIDF_SCORING = lang("tfidf_scoring");
  /** Full-text, document-based. */
  String TEXT_NODES = lang("text_nodes");

  /** TEXT Lines information. */
  String SPLIT_INPUT_LINES = lang("split_input_lines");
  /** CSV header information. */
  String FIRST_LINE_HEADER = lang("first_line_header");
  /** CSV Database format information. */
  String XML_FORMAT = lang("xml_format") + COL;
  /** CSV Separator information. */
  String SEPARATOR = lang("separator") + COL;

  /** General info. */
  String GENERAL = lang("general");
  /** General info. */
  String PARSING = lang("parsing");
  /** Name indexes. */
  String NAMES = lang("names");
  /** Value indexes. */
  String INDEXES = lang("indexes");
  /** Full-text index. */
  String FULLTEXT = lang("fulltext");

  /** Dialog title for opening a database. */
  String OPEN_DB = lang("open_db");
  /** Dialog asking if a new database should be be created. */
  String NEW_DB_QUESTION = lang("no_db_found") + NL + lang("new_db_question");

  /** File dialog error. */
  String FILE_NOT_OPENED = lang("file_not_opened");
  /** File dialog error. */
  String FILE_NOT_SAVED = lang("file_not_saved");
  /** File dialog replace information. */
  String FILE_EXISTS_X = lang("file_exists_%");
  /** Dir dialog replace information. */
  String FILES_REPLACE_X = lang("files_replace_%");

  /** Users. */
  String USERS = lang("users");
  /** Confirmation . */
  String ARE_YOU_SURE = lang("are_you_sure");

  /** Alter password. */
  String ALTER_PW = lang("alter_pw");
  /** Invalid. */
  String INVALID_X = lang("invalid_%");
  /** Databases. */
  String DATABASES = lang("databases");
  /** Backups. */
  String BACKUPS = lang("backups");

  /** Dialog title for renaming a database. */
  String RENAME_DB = lang("rename_db");

  /** Dialog title for copying a database. */
  String COPY_DB = lang("copy_db");
  /** Info for overwriting a database. */
  String OVERWRITE_DB = lang("overwrite_db");
  /** Info for creating an empty database. */
  String EMPTY_DB = lang("empty_db");

  /** Dialog title for managing databases. */
  String MANAGE_DB = lang("manage_db");
  /** Dialog text for dropping a database. */
  String DROPPING_DB_X = lang("dropping_db_%") + NL + ARE_YOU_SURE;
  /** Database only available as backup. */
  String ONLY_BACKUP = lang("only_backup");
  /** Dialog text for dropping a backup. */
  String DROP_BACKUPS_X = lang("drop_backups_%") + NL + ARE_YOU_SURE;
  /** Dialog text for overwriting a backup. */
  String OVERWRITE_DB_QUESTION = OVERWRITE_DB + NL + ARE_YOU_SURE;

  /** Dialog title for deleting nodes. */
  String DELETE_NODES = lang("delete_nodes");
  /** Dialog title for closing XQuery file. */
  String CLOSE_FILE_X = lang("close_file_%");

  /** Dialog title for exporting nodes. */
  String OUTPUT_DIR = lang("output_dir");
  /** Dialog title for exporting nodes. */
  String DIR_NOT_EMPTY = lang("dir_not_empty");
  /** Dialog title for exporting nodes. */
  String INDENT_WITH_WS = lang("indent_with_ws");

  /** Database path. */
  String DATABASE_PATH = lang("database_path");
  /** Interactions. */
  String GUI_INTERACTIONS = lang("gui_interactions");
  /** Look and feel. */
  String JAVA_LF = lang("java_lf") + " (" + lang("requires_restart") + ")";
  /** Focus. */
  String RT_FOCUS = lang("rt_focus");
  /** Simple file dialog. */
  String SIMPLE_FILE_CHOOSER = lang("simple_file_chooser");
  /** Name display flag. */
  String SHOW_NAME_ATTS = lang("show_name_atts");
  /** Language preference. */
  String LANGUAGE_RESTART = lang("language") +
      " (" + lang("requires_restart") + ")";

  /** Dialog title for inserting new data. */
  String INSERT_NEW_DATA = lang("insert_new_data");
  /** Dialog title for updating document data. */
  String EDIT_DATA = lang("edit_data");
  /** Insert value. */
  String VALUE = lang("value");
  /** Dialog title for updating text. */
  String EDIT_X = lang("edit_%");
  /** Dialog title for updating text. */
  String[] NODE_KINDS = { lang("document"), lang("element"), lang("text"),
      lang("attribute"), lang("comment"), lang("pi")
  };

  /** Dialog title for choosing a font. */
  String CHOOSE_FONT = lang("choose_font");
  /** Predefined font types. */
  String[] FONT_TYPES = { lang("standard"), lang("bold"), lang("italics") };

  /** Dialog title for treemap color schema. */
  String COLOR_SCHEMA = lang("color_schema");
  /** Color schema information. */
  String RED = lang("red");
  /** Color schema information. */
  String GREEN = lang("green");
  /** Color schema information. */
  String BLUE = lang("blue");

  /** Dialog title for treemap design. */
  String MAP_LAYOUT = lang("map_layout");
  /** Show attributes. */
  String SHOW_ATTS = lang("show_atts");
  /** Predefined number of layouts. */
  String[] MAP_CHOICES = {
    lang("map_choice1"), lang("map_choice2"), lang("map_choice3"),
    lang("map_choice4"), lang("map_choice5")
  };

  /** Predefined number of layouts. */
  String[] MAP_LAYOUTS = {
    "Split Layout", "Strip Layout", "Squarified Layout",
    "Slice&Dice Layout", "Binary Layout"
  };

  /** Map layout-algorithm. */
  String OFFSETS = lang("offsets");
  /** Size depending on... */
  String RATIO = lang("ratio");
  /** Size depending on... */
  String CHILDREN_TEXT_LEN = lang("children_text_len");
  /** Size depending on... */
  String NUMBER_CHILDREN = lang("number_children");
  /** Size depending on... */
  String TEXT_LENGTH = lang("text_length");

  /** Dialog title for tree view options. */
  String TREE_OPTIONS = lang("tree_options");
  /** Slim rectangles to text length. */
  String ADJUST_NODES = lang("adjust_nodes");

  /** Info on used main memory. */
  String USED_MEM = lang("used_mem");
  /** Memory information. */
  String TOTAL_MEM_C = lang("total_mem") + COLS;
  /** Memory information. */
  String RESERVED_MEM_C = lang("reserved_mem") + COLS;
  /** Memory information. */
  String MEMUSED_C = USED_MEM + COLS;

  /** Copyright info. */
  String COPYRIGHT = "\u00A9 2005-12 " + COMPANY;
  /** License info. */
  String LICENSE = lang("license");
  /** Developer info. */
  String CHIEF_ARCHITECT = lang("chief_architect") + ": Christian Gr\u00FCn";
  /** Contributors info. */
  String TEAM1 = lang("team") +
      ": Michael Seiferle, Alexander Holupirek,";
  /** Developer names. */
  String TEAM2 = "Dimitar Popov, Rositsa Shadura, Lukas Kircher,";
  /** Developer names. */
  String TEAM3 = "Leo W\u00F6rteler, Andreas Weiler " +
      lang("and_others");
  /** Translation. */
  String TRANSLATION = lang("translation");

  // HELP TEXTS ===============================================================

  /** Memory help. */
  String H_USED_MEM = lang("h_used_mem");
  /** Out of memory error due to database creation. */
  String HELP_OUT_OF_MEM = NL + lang("h_out_of_mem");

  /** Help string. */
  String H_EXECUTE_QUERY = lang("h_execute_query");
  /** Help string. */
  String H_STOP_PROCESS = lang("h_stop_process");
  /** Help string. */
  String H_SHOW_HISTORY = lang("h_show_history");
  /** Help string. */
  String H_SAVE_RESULT = lang("h_save_result");
  /** Help string. */
  String H_RECENTLY_OPEN = lang("h_recently_open");

  /** Command info. */
  String H_ABOUT = lang("h_about");
  /** Command info. */
  String H_CLOSE = lang("h_close");
  /** Command info. */
  String H_COLORS = lang("h_colors");
  /** Command info. */
  String H_COPY = lang("h_copy");
  /** Command info. */
  String H_CPPATH = lang("h_copy_path");
  /** Command info. */
  String H_NEW = lang("h_new");
  /** Command info. */
  String H_DELETE = lang("h_delete");
  /** Command info. */
  String H_OPEN_MANAGE = lang("h_open_manage");

  /** Command info. */
  String H_EDIT = lang("h_edit");
  /** Command info. */
  String H_EXIT = lang("h_exit");
  /** Command info. */
  String H_EXPORT_XML = lang("h_export_xml");
  /** Command info. */
  String H_FILTER_SELECTED = lang("h_filter_selected");
  /** Command info. */
  String H_FONTS = lang("h_fonts");
  /** Command info. */
  String H_FULLSCREEN = lang("h_fullscreen");
  /** Command info. */
  String H_GO_UP = lang("h_go_up");
  /** Command info. */
  String H_PROPERTIES = lang("h_properties");
  /** Command info. */
  String H_NEW_NODE = lang("h_new_node");
  /** Command info. */
  String H_MAP_LAYOUT = lang("h_map_layout");
  /** Command info. */
  String H_TREE_OPTIONS = lang("h_tree_options");
  /** Command info. */
  String H_PASTE = lang("h_paste");
  /** Command info. */
  String H_PREFERENCES = lang("h_preferences");
  /** Command info. */
  String H_GO_HOME = lang("h_go_home");
  /** Command info. */
  String H_RT_EXECUTION = lang("h_rt_execution");
  /** Command info. */
  String H_RT_FILTERING = lang("h_rt_filtering");
  /** Command info. */
  String H_BUTTONS = lang("h_buttons");
  /** Command info. */
  String H_EXPLORER = lang("h_explorer");
  /** Command info. */
  String H_FOLDER = lang("h_folder");
  /** Command info. */
  String H_HELP = lang("h_help");
  /** Command info. */
  String H_COMMUNITY = lang("h_community");
  /** Command info. */
  String H_UPDATES = lang("h_updates");
  /** Command info. */
  String H_QUERY_INFO = lang("h_query_info");
  /** Command info. */
  String H_INPUT_BAR = lang("h_input_bar");
  /** Command info. */
  String H_MAP = lang("h_map");
  /** Command info. */
  String H_PLOT = lang("h_plot");
  /** Command info. */
  String H_STATUS_BAR = lang("h_status_bar");
  /** Command info. */
  String H_TABLE = lang("h_table");
  /** Command info. */
  String H_TEXT = lang("h_text");
  /** Command info. */
  String H_TREE = lang("h_tree");
  /** Command info. */
  String H_EDITOR = lang("h_editor");
  /** Command info. */
  String H_NEW_FILE = lang("h_new_file");
  /** Command info. */
  String H_OPEN_FILE = lang("h_open_file");
  /** Help string. */
  String H_CLOSE_FILE = lang("h_close_file");
  /** Command info. */
  String H_SAVE = lang("h_save");

  /** Whitespaces information. */
  String H_CHOP_WS = lang("h_chop_ws");
  /** Internal parser information. */
  String H_INT_PARSER = lang("h_int_parser");
  /** Input format information. */
  String H_INPUT_FORMAT = lang("h_input_format");

  /** Path summary information. */
  String H_PATH_INDEX = lang("h_path_index");
  /** Text index information. */
  String H_TEXT_INDEX = lang("h_text_index");
  /** Attribute value index information. */
  String H_ATTR_INDEX = lang("h_attr_index");
  /** Full-text index information. */
  String H_FULLTEXT_INDEX = lang("h_fulltext_index");
  /** Full-text index information. */
  String H_WILDCARD = lang("h_wildcards");
  /** Full-text index information. */
  String H_STEMMING = lang("h_stemming");
  /** Full-text index information. */
  String H_LANGUAGE = lang("h_languauge");
  /** Full-text index information. */
  String H_CASE = lang("h_case");
  /** Full-text index information. */
  String H_DIACRITICS = lang("h_diacritics");
  /** Full-text index information. */
  String H_SCORING = lang("h_scoring");
  /** Full-text index information. */
  String H_STOPWORDS = lang("h_stopwords");

  /** "Failed to open a browser". */
  String H_BROWSER_ERROR_X = lang("h_browser_error_%");
  /** Database update. */
  String H_DB_FORMAT = lang("h_db_format");
  /** Index update. */
  String H_INDEX_FORMAT = lang("h_index_format");
  /** Dialog title for opening a large database. */
  String H_LARGE_DB = lang("h_large_db") + NL + " ";
  /** Dialog for downloading a new version. */
  String H_NEW_VERSION = lang("h_new_version");

  // SERVER TEXTS =============================================================

  /** Server. */
  String S_LOCALSERVER = lang("s_localserver");
  /** Users. */
  String S_CONNECT = lang("s_connect");
  /** Host. */
  String S_HOST = lang("s_host");
  /** PORT. */
  String S_PORT = lang("s_port");
  /** Local. */
  String S_LOCALPORT = lang("s_localport");
  /** Create user. */
  String S_CREATEU = lang("s_createu");
  /** Global permissions. */
  String S_GLOBPERM = lang("s_globperm") + COLS;
  /** Local permissions. */
  String S_LOCPERM = lang("s_locperm") + COLS;
  /** Question for dropping user. */
  String S_DRQUESTION = lang("s_drquestion") + NL + ARE_YOU_SURE;
  /** Question for revoking right from logged in user. */
  String S_DBREVOKE = lang("s_dbrevoke") + NL + ARE_YOU_SURE;
  /** Login. */
  String S_ADLOGIN = lang("s_adlogin");
  /** Connected. */
  String S_CONNECTED = lang("s_connected");
  /** Disconnected. */
  String S_DISCONNECTED = lang("s_disconnected");
  /** Server information. */
  String S_INFO1 = lang("s_info1");
  /** Server information. */
  String S_INFO2 = lang("s_info2");
  /** Sessions. */
  String S_SESSIONS = lang("s_sessions");
  /** Logs. */
  String S_LOCALLOGS = lang("s_locallogs");
  /** Button text for altering password. */
  String S_ALTER = lang("s_alter") + DOTS;
  /** Command info. */
  String S_SERVER_ADMIN = lang("s_server_admin");
  /** Command info. */
  String S_H_SERVER_ADMIN = lang("s_h_server_admin");

  /** Dummy string to check if all language strings have been assigned. */
  String DUMMY = lang(null);
}
