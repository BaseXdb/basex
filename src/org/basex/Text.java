package org.basex;

import static org.basex.core.Lang.*;
import static org.basex.util.Token.*;
import org.basex.core.proc.Create;
import org.basex.core.proc.Drop;
import org.basex.core.proc.Fs;
import org.basex.core.proc.Info;
import org.basex.core.proc.Insert;
import org.basex.core.proc.PFinder;
import org.basex.core.proc.Set;

/**
 * This class organizes textual information for the project classes.
 * All texts are externalized and internationalized.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Andreas Weiler
 */
public interface Text {

  // FREQUENTLY USED CHARACTERS ===============================================

  /** New Line. */
  String NL = org.basex.core.Prop.NL;
  /** New Line. */
  String COL = ":";
  /** New Line. */
  String COLS = ": ";
  /** Dots. */
  String DOT = ".";
  /** Dots. */
  String DOTS = "...";

  /** Project name. */
  String NAME = "BaseX";
  /** URL. */
  String URL = "http://www.basex.org";
  /** Code version. */
  String VERSION = "4.03";
  /** Company Info. */
  String COMPANY = "DBIS, University of Konstanz";

  /** BaseX GUI title. */
  String TITLE = NAME + " " + VERSION;
  /** BaseX Text. */
  String CONSOLEINFO = TITLE + "; " + COMPANY;

  // CONSOLE INFO =============================================================

  /** BaseX Text. */
  String CONSOLE = TITLE + " [%]" + NL + lang("help_intro") + NL;

  /** Good-Bye Information. */
  String[] CLIENTBYE = {
      lang("bye1"), lang("bye2"), lang("bye3"), lang("bye4")
  };

  /** BaseX Console. */
  String LOCALMODE = lang("cs_local");
  /** BaseX Console. */
  String CLIENTMODE = lang("cs_client");
  /** BaseX Prompt. */
  String INPUT = "> ";
  /** BaseX Prompt. */
  String INPUTCONT = DOTS + " ";

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
  /** Slot error. */
  String SERVERFULL = lang("srv_toomany");
  /** Connection error. */
  String SERVERERR = lang("srv_connect");
  /** Port error. */
  String SERVERPORT = lang("srv_port");

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

  /** Database separator. */
  String DUMMYDATABASE0 = "Database Commands:";

  /** Command help. */
  String CREATE0 = "[" + Create.XML + "|" + Create.FS + "|" + Create.INDEX +
    "] [...]";
  /** Command help. */
  String CREATE1 = "Create database or index.";
  /** Command help. */
  String CREATE2 = "Create database from XML or filesystem or create " +
    "index:" + NL + NL +
    "- " + Create.XML + " [file]: " + NL +
    "  create database for the XML [file]" + NL +
    "- " + Create.FS + " [database] [path]: " + NL +
    "  create [database] for the file [path]" + NL +
    "- " + Create.INDEX + " [" + Create.TXT + "|" + Create.ATV +
    "|" + Create.WRD + "|" + Create.FTX + "]: " + NL +
    "  create the specified index";
  /** Command help. */
  String OPEN0 = "[database]";
  /** Command help. */
  String OPEN1 = "Open [database].";
  /** Command help. */
  String OPEN2 = "Open the specified [database].";
  /** Command help. */
  String INFO0 = "[" + Info.DB + "|" + Info.IDX + "|" + Info.TBL + "]?";
  /** Command help. */
  String INFO1 = "Show information on current database.";
  /** Command help. */
  String INFO2 = "Show information on the currently opened structure:" + NL +
    "- no argument: show global information" + NL +
    "- " + Info.DB + ": show database information" + NL +
    "- " + Info.IDX + ": show index information" + NL +
    "- " + Info.TBL + " [start end]|[query]: show XML table";
  /** Command help. */
  String CHECK0 = "[database]";
  /** Command help. */
  String CHECK1 = "Open [database] if it exists, or create new database.";
  /** Command help. */
  String CHECK2 = "Check if the specified [database] is in memory. " + NL +
    "If not, the database file is opened. If there is no such database," + NL +
    "the argument is interpreted as a filename, and a new" + NL +
    "database is created.";
  /** Command help. */
  String CLOSE0 = "";
  /** Command help. */
  String CLOSE1 = "Close current database.";
  /** Command help. */
  String CLOSE2 = "Close the current database and reset main memory.";
  /** Command help. */
  String LIST0 = "";
  /** Command help. */
  String LIST1 = "List available databases.";
  /** Command help. */
  String LIST2 = "List all available databases.";
  /** Command help. */
  String DROP0 = "[" + Drop.DB + "|" + Drop.INDEX + "] [...]";
  /** Command help. */
  String DROP1 = "Drop database or index.";
  /** Command help. */
  String DROP2 = "The following arguments can be specified:" + NL +
    "- " + Drop.DB + " [name]: " + NL + "  drop the database [name]" + NL +
    "- " + Create.INDEX + " [" + Create.TXT + "|" + Create.ATV + "|" +
    Create.WRD + "|" + Create.FTX + "]:" + NL + "  drop the specified index";
  /** Command help. */
  String OPTIMIZE0 = "";
  /** Command help. */
  String OPTIMIZE1 = "Optimize the database.";
  /** Command help. */
  String OPTIMIZE2 = "Optimize the current database structures.";
  /** Command help. */
  String EXPORT0 = "[file]";
  /** Command help. */
  String EXPORT1 = "Export database as XML.";
  /** Command help. */
  String EXPORT2 = "Export the current context set to an XML [file].";

  /** Command help. */
  String DUMMYQUERY0 = "Query Commands:";

  /** Command help. */
  String XPATH0 = "[query]";
  /** Command help. */
  String XPATH1 = "Evaluate XPath query.";
  /** Command help. */
  String XPATH2 = "Evaluate the specified XPath [query] and print the result.";
  /** Command help. */
  String XPATHMV0 = "[hits] [subhits] [\"query\"]";
  /** Command help. */
  String XPATHMV1 = "Evaluate MedioVis query.";
  /** Command help. */
  String XPATHMV2 = "Evaluate the specified XPath [query] and limit the " + NL +
    "number of top elements to [hits] and the number of " + NL +
    "subordinate elements to [subhits].";
  /** Command Help. */
  String XQUERY0 = "[query]";
  /** Command Help. */
  String XQUERY1 = "Evaluate XQuery [query].";
  /** Command Help. */
  String XQUERY2 =
    "Evaluate an XQuery and show its result. No other commands" + NL +
    "are allowed in the same line. In interactive mode, if no" + NL +
    "query is specified, all following lines are added to the" + NL +
    "query until an empty line is typed in.";
  /** Command Help. */
  String FIND0 = "[query]";
  /** Command Help. */
  String FIND1 = "Evaluate a keyword query.";
  /** Command Help. */
  String FIND2 = "The following modifiers can be used:" + NL +
    " =  : look for exact text nodes" + NL +
    " @= : look for exact attributes" + NL +
    " @  : look for attributes";
  /** Command Help. */
  String PF0 = "[" + PFinder.PIPE + "]? [\"query\"]";
  /** Command Help. */
  String PF1 = "Evaluate XQuery [query] via Pathfinder.";
  /** Command Help. */
  String PF2 = "Evaluate an XQuery via Pathfinder and show its result." + NL +
    "If [" + PFinder.PIPE + "] is set, the pipelined version is applied.";
  /** Command Help. */
  String XMARK0 = "[nr]";
  /** Command Help. */
  String XMARK1 = "Launch XMark query";
  /** Command Help. */
  String XMARK2 = "Launch internal implementation of XMark query [nr] (1-20).";
  /** Command Help. */
  String CD0 = "[query]";
  /** Command Help. */
  String CD1 = "Evaluate XPath query and set result as new context set.";
  /** Command Help. */
  String CD2 = "Evaluate the specified XPath [query] and set the result " + NL +
    "as newcontext set.";
  /** Command Help. */
  String FS0 = "[" + Fs.DU + "|" + Fs.LS + "|" + Fs.PWD + "]";
  /** Command Help. */
  String FS1 = "Perform a filesystem command.";
  /** Command Help. */
  String FS2 = "Perform a filesystem command:" + NL +
    "- " + Fs.DU    + " [path]?: Calculate directory usage" + NL +
    "- " + Fs.LS    + " [path]?: List directory" + NL +
    "- " + Fs.TOUCH + " [file]: Change access and modification times" + NL +
    "- " + Fs.RM + " [option] [file]: Remove a file" + NL +
    "- " + Fs.PWD   + ": Print the current working directory";

  /** Command Help. */
  String DUMMYUPDATE0 = "Update Commands:";

  /** Command Help. */
  String COPY0 = "[pos] [\"source\"] [\"target\"]";
  /** Command Help. */
  String COPY1 = "Copy database nodes.";
  /** Command Help. */
  String COPY2 = "Evaluate [source] and copy the result to [target]" + NL +
  "at the specified child [pos] (0=last child).";
  /** Command Help. */
  String DELETE0 = "[\"target\"]";
  /** Command Help. */
  String DELETE1 = "Delete database nodes.";
  /** Command Help. */
  String DELETE2 = "Delete nodes satifying the specified [target] query.";

  /** Command Help. */
  String INSERT0 = "[" + Insert.FRG + "|" + Insert.ELEM + "|" + Insert.ATT +
    "|" + Insert.TXT + "|" + Insert.COM + "|" + Insert.PI + "] [...]";
  /** Command Help. */
  String INSERT1 = "Insert database nodes.";
  /** Command Help. */
  String INSERT2 = "Insert a fragment or specific node at the specified" + NL +
    "child [pos] (0=last child) of the given [target] query:" + NL + NL +
    "- " + Insert.ELEM + " [name] [pos] [target]: Insert [name]" + NL +
    "- " + Insert.TXT + " [text] [pos] [target]: Insert [text]" + NL +
    "- " + Insert.ATT + " [name] [value] [target]: Insert [name] and " +
    "[value]" + NL +
    "- " + Insert.COM + " [text] [pos] [target]: Insert [text]" + NL +
    "- " + Insert.PI + " [name] [value] [pos] [target]: Insert [name] and " +
    "[value]" + NL +
    "- " + Insert.FRG + " [frag] [pos] [target]: Insert [frag]";
  /** Command Help. */
  String UPDATE0 = "[" + Insert.ELEM + "|" + Insert.ATT + "|" + Insert.TXT +
    "|" + Insert.COM + "|" + Insert.PI + "] [...]";
  /** Command Help. */
  String UPDATE1 = "Update database nodes.";
  /** Command Help. */
  String UPDATE2 = "Update nodes satisfying the specified [target] query." +
    NL + NL +
    "- " + Insert.ELEM + " [name] [target]: Update with [name]" + NL +
    "- " + Insert.TXT + " [text] [target]: Update with [text]" + NL +
    "- " + Insert.ATT + " [name] [value] [target]: " +
    "Update with [name] and [value]" + NL +
    "- " + Insert.COM + " [text] [target]: Update with [text]" + NL +
    "- " + Insert.PI + " [name] [value] [target]: " +
    "Update with [name] and [value]";

  /** Command Help. */
  String DUMMYGENERAL0 = "General Commands:";

  /** Command Help. */
  String SET0 = "[option] [val]?";
  /** Command Help. */
  String SET1 = "Sets global options (current values: try 'info').";
  /** Command Help. */
  String SET2 = "Available [option]s with [val=on|off]:" + NL + NL +
    "- " + Set.INFO + " [all?]: Show (all) process info" + NL +
    "- " + Set.DEBUG + ": Show debug info" + NL +
    "- " + Set.SERIALIZE + ": Serialize query results" + NL +
    "- " + Set.XMLOUTPUT + ": Serialize results as XML" + NL +
    "- " + Set.MAINMEM + ": Use main memory mode" + NL +
    "- " + Set.CHOP + ": Chop XML whitespaces" + NL +
    "- " + Set.ENTITY + ": Parse XML entities" + NL +
    "- " + Set.TXTINDEX + ": Index text nodes" + NL +
    "- " + Set.ATTRINDEX + ": Index attributes" + NL +
    "- " + Set.WORDINDEX + ": Index document words" + NL +
    "- " + Set.FTINDEX + ": Index full text" + NL + NL +
    "Additional options:" + NL + NL +
    "- " + Set.DBPATH + " [path]: Set new database path" + NL +
    "- " + Set.RUNS + " [nr]: Set number of query runs";

  /** Command Help. */
  String HELP0 = "[command]?";
  /** Command Help. */
  String HELP1 = "Get help on " + NAME + " commands.";
  /** Command Help. */
  String HELP2 = "If [command] is specified, information on the specific" + NL +
    "command is printed; otherwise, all commands are listed." + NL +
    "If 'all' is specified, hidden commands are included.";
  /** Command Help. */
  String PING0 = "";
  /** Command Help. */
  String PING1 = "Test database connectivity.";
  /** Command Help. */
  String PING2 = "If the connection works, an info string is returned.";
  /** Command Help. */
  String PROMPT1 = "Get prompt message.";
  /** Command Help. */
  String PROMPT2 = "Get prompt message, representing the current context node.";
  /** Command Help. */
  String EXIT0 = "";
  /** Command Help. */
  String EXIT1 = "Exit " + NAME + DOT;
  /** Command Help. */
  String EXIT2 = "Exit " + NAME + DOT;
  /** Command Help. */
  String QUIT0 = "";
  /** Command Help. */
  String QUIT1 = EXIT1;
  /** Command Help. */
  String QUIT2 = EXIT2;

  // STARTER WINDOW ===========================================================

  /** Waiting info. */
  String WAIT1 = lang("launch") + " " + TITLE;
  /** Waiting info. */
  String WAIT2 = "..." + lang("wait") + "...";

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
  String GUIABOUT = lang("c_about") + " " + NAME + DOTS;
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
  String GUICOPY = lang("c_copy");
  /** Command Info. */
  String GUICOPYTT = lang("c_copytt");
  /** Command Info. */
  String GUICREATE = lang("c_create") + DOTS;
  /** Command Info. */
  String GUICREATETT = lang("c_creatett");
  /** Command Info. */
  String GUIDELETE = lang("c_delete") + DOTS;
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
  String GUIINPUTMODE = lang("c_inputmode");
  /** Command Info. */
  String GUIINPUTMODETT = lang("c_inputmodett");
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
  String GUIROOT = lang("c_root");
  /** Command Info. */
  String GUIROOTTT = lang("c_roottt");
  /** Command Info. */
  String GUIRTFILTER = lang("c_rtfilter");
  /** Command Info. */
  String GUIRTFILTERTT = lang("c_rtfiltertt");
  /** Command Info. */
  String GUISELECT = lang("c_select");
  /** Command Info. */
  String GUISELECTTT = lang("c_selecttt");
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
  String GUISHOWTABLETT = lang("c_showtablett");
  /** Command Info. */
  String GUISHOWTEXT = lang("c_showtext");
  /** Command Info. */
  String GUISHOWTEXTTT = lang("c_showtexttt");
  /** Command Info. */
  String GUISHOWTREE = lang("c_showtree");
  /** Command Info. */
  String GUISHOWTREETT = lang("c_showtreett");
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
  String BUTTONXPATH = lang("b_xpath");
  /** Button text for confirming actions. */
  String BUTTONOK = lang("b_ok");
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
  /** Interactive Filtering. */
  String BUTTONFILTER = lang("b_filter");
  /** XQuery copy Button. */
  String BUTTONTOXPATH = ">> XQuery";
  /** Text field mode - Search Modes. */
  String[] SEARCHMODE = { "XQuery", lang("b_simple") };

  // STATUS BAR ===============================================================

  /** Path information. */
  String STATUSPATH = lang("s_path") + COLS;
  /** Info text for calculating map. */
  String STATUSMAP1 = lang("s_calcmap") + COLS;
  /** Info text for painting map. */
  String STATUSMAP2 = ", " + lang("s_paintmap") + COLS;
  /** Wait text. */
  String STATUSWAIT = lang("wait") + DOTS;
  /** Default message. */
  String STATUSOK = lang("s_ok") + ". ";

  // VISUALIZATION ============================================================

  /** Welcome Information. */
  String WELCOME = lang("welcome1");
  /** Welcome Text. */
  String WELCOMETEXT = lang("welcome2");
  /** Text find information. */
  String RESULTFIND = lang("text_find") + COLS;
  /** Help String. */
  String NOTABLE = lang("table_no") + DOT;
  /** Interactive Filtering. */
  String CMDFILTERRT = lang("query_rt");
  /** Binary file. */
  byte[] MAPBINARY = token(lang("map_binary"));

  // DIALOG WINDOWS ===========================================================

  /** Open Dialog - No database. */
  String DIALOGINFO = lang("d_info");
  /** Dialog title for choosing a directory. */
  String DIALOGFC = lang("d_fctitle");

  /** Dialog title for creating a database. */
  String CREATETITLE = lang("dc_title");
  /** XML File Description. */
  String CREATEXMLDESC = lang("dc_xmldesc") + " (*.xml)";
  /** XQ File Description. */
  String CREATEXQDESC = lang("dc_xqdesc") + " (*.xq)";
  /** Dialog Title for Database Options. */
  String CREATEADVTITLE = lang("dc_advtitle");
  /** Database Options. */
  String CREATEADVLABEL = lang("dc_advlabel") + COL;
  /** Whitespaces information. */
  String CREATECHOP = lang("dc_chop");
  /** Entities information. */
  String CREATEENTITIES = lang("dc_entities");
  /** Value Index information. */
  String CREATETXTINDEX = lang("dc_txtindex");
  /** Value Index information. */
  String CREATEATTINDEX = lang("dc_attindex");
  /** Word Index information. */
  String CREATEWORDINDEX = lang("dc_wordindex");
  /** Word Index information. */
  String CREATEFTINDEX = lang("dc_ftindex");
  /** Main-Memory Mode. */
  String CREATEMAINMEM = lang("dc_mainmem");

  /** Whitespaces information. */
  String CHOPPINGINFO = lang("dc_chopinfo") + DOT;
  /** Entities information. */
  String ENTITIESINFO = lang("dc_entitiesinfo") + " (&...;).";
  /** Value Index information. */
  String TXTINDEXINFO =  lang("dc_txtinfo") + DOT;
  /** Value Index information. */
  String ATTINDEXINFO = lang("dc_attinfo") + DOT;
  /** Fulltext Index information. */
  String WORDINDEXINFO = lang("dc_wrdinfo") + DOT;
  /** Fulltext Index information. */
  String FTINDEXINFO = lang("dc_ftxinfo") + DOT;
  /** Main-Memory Mode. */
  String MMEMINFO =  lang("dc_mminfo") + DOT;

  /** Dialog title for opening a database. */
  String OPENTITLE = lang("do_title");
  /** Open Dialog - No database information. */
  String OPENNODBINFO = lang("do_nodb") + DOT;
  /** Dialog title for deleting nodes. */
  String NODBQUESTION = OPENNODBINFO + DOT + NL +
    lang("do_nodbquestion") + NL + " ";

  /** Dialog title for creating a database. */
  String XQOPENTITLE = lang("dq_open");
  /** Dialog title for creating a database. */
  String XQSAVETITLE = lang("dq_save");
  /** Dialog title for creating a database. */
  String XQOPERROR = lang("dq_notopened");
  /** Dialog title for creating a database. */
  String XQSAVERROR = lang("dq_notsaved");

  /** Dialog Title for Import Options. */
  String IMPORTFSTITLE = lang("dfs_title");
  /** Import Options. */
  String IMPORTSAVE = lang("dfs_save") + COL;
  /** Import Options. */
  String IMPORTDIR = lang("dfs_dir") + COL;
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

  /** Dialog title for dropping a database. */
  String DROPTITLE = lang("dd_title");
  /** Dialog title for dropping a database. */
  String DROPCONF = lang("dd_question") + NL + " ";

  /** Dialog Title for Import Options. */
  String PREFSTITLE = lang("dp_title");
  /** Database Path. */
  String DATABASEPATH = lang("dp_dbpath");
  /** Interactions. */
  String PREFINTER = lang("dp_inter");
  /** Focus. */
  String PREFFOCUS = lang("dp_focus");
  /** Simple File Dialog. */
  String SIMPLEFD = lang("dp_simplefd");
  /** Name Attributes. */
  String PREFNAMES = lang("dp_names");
  /** Name Attributes. */
  String PREFLANG = lang("dp_lang");
  /** Name Attributes. */
  String PREFLANGNEW = lang("dp_langnew");

  /** Dialog title for deleting nodes. */
  String DELETETITLE = lang("dx_title");
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
  /** Dialog Title for updating text. */
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
  /** Simple Map Layout. */
  String MAPATTS = lang("dm_atts");
  /** Layout information. */
  String MAPPROP = lang("dm_prop") + COL;
  /** Predefined number of layouts. */
  String[] MAPLAYOUTCHOICE = {
      lang("dm_choice1"), lang("dm_choice2"), lang("dm_choice3"),
      lang("dm_choice4"), lang("dm_choice5"), lang("dm_choice6")
  };

  /** Title of Information Dialog. */
  String INFOTITLE = lang("di_title");
  /** Index information. */
  String INFOINDEX = lang("di_index");
  /** Index information. */
  String INFOBUILD = lang("di_build") + DOTS;

  /** Title of Memory Dialog. */
  String MEMTITLE = lang("dz_title");
  /** Memory information. */
  String MEMTOTAL = lang("dz_total") + COLS;
  /** Memory information. */
  String MEMRESERVED = lang("dz_reserved") + COLS;
  /** Memory information. */
  String MEMUSED = lang("dz_used") + COLS;
  /** Memory help. */
  String MEMHELP = lang("dz_help");

  /** About text. */
  String ABOUTTITLE = lang("da_title") + " " + NAME;
  /** BaseX Copyright. */
  String COPYRIGHT = "© 2005-08 " + COMPANY;
  /** BaseX License. */
  String LICENSE = lang("da_license");
  /** BaseX Developer Info. */
  String DEVELOPER = lang("da_dev") + ": Christian Grün";
  /** BaseX Contributors Info. */
  String CONTRIBUTE1 = lang("da_cont1") +
    ": Alexander Holupirek, Tim Petrowsky,";
  /** BaseX Developer Names. */
  String CONTRIBUTE2 = "Sebastian Gath, Lukas Kircher " + lang("da_cont2");

  // PROCESS INFOS ============================================================

  /** Process time. */
  String PROCTIME = lang("proc_time") + ": %";
  /** No Document Warning. */
  String PROCSYNTAX = lang("proc_syntax") + ": %";
  /** Command Execution Error. */
  String PROCERR = lang("proc_err") + COL + NL + "%";
  /** No Database Error. */
  String PROCNODB = lang("proc_nodb");
  /** Update Error. */
  String PROCMM = lang("proc_mm");

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
  /** XMark Error. */
  String XMARKWHICH = lang("cmd_xmark");
  
  // CREATE COMMAND ===========================================================

  /** Create Database information. */
  String PROGCREATE = lang("pc_create");
  /** Create Database information. */
  String PROGINDEX = lang("pc_index");
  /** Database Update. */
  String DBUPDATE = lang("pc_update");
  /** Builder error. */
  String CANCELCREATE = lang("pc_cancel");
  /** Create Database information. */
  String NODESPARSED = " " + lang("pc_parse") + DOTS;
  /** Scanner Position. */
  String SCANPOS = lang("pc_pos");

  /** Create Database information. */
  String INDEXTXT = lang("pc_indextxt") + DOTS;
  /** Create Database information. */
  String INDEXATT = lang("pc_indexatt") + DOTS;
  /** Create Database information. */
  String INDEXWRD = lang("pc_indexwrd") + DOTS;
  /** Create Database information. */
  String INDEXFTX = lang("pc_indexftx") + DOTS;

  /** Database created. */
  String DBCREATED = lang("pc_created");

  /** Create information. */
  String CREATETABLE = lang("pc_tbl");
  /** Create information. */
  String CREATEATTR = lang("pc_att");
  /** Create information. */
  String CREATETEXT = lang("pc_txt");
  /** Create information. */
  String CREATEWORD = lang("pc_wrd");
  /** Create information. */
  String CREATEFT = lang("pc_ftx");
  /** Parse error. */
  String CREATEERR = lang("pc_err");

  /** File not found. */
  String FILEWHICH = lang("pc_notfound");

  // DATABASE COMMANDS ========================================================

  /** Database not found. */
  String DBNOTFOUND = lang("db_no");
  /** Database is open. */
  String DBINMEM = lang("db_open");
  /** No Document in memory Warning. */
  String DBEMPTY = lang("db_nodb");
  /** Database closed. */
  String DBCLOSED = lang("db_closed");
  /** Database not closed. */
  String DBCLOSEERR = lang("db_closeerr");
  /** Database Optimized. */
  String DBOPTIMIZED = lang("db_optimized");
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

  /** Database Opened. */
  String DBINDEXED = lang("in_created");
  /** Database Opened. */
  String DBINDEXERR = lang("in_err");
  /** Database Opened. */
  String DBDROP = lang("in_dropped");
  /** Database Opened. */
  String DBDROPERR = lang("in_notdropped");

  // DATABASE/INDEX INFORMATION ===============================================

  /** Data info. */
  String TAGINDEX = "TAG NAMES:";
  /** Data info. */
  String ATTINDEX = "ATTRIBUTE NAMES:";
  /** Data info. */
  String TEXTINDEX = "TEXTS:";
  /** Data info. */
  String VALUEINDEX = "ATTRIBUTE VALUES:";
  /** Data info. */
  String WORDINDEX = "WORDS:";
  /** Data info. */
  String FTINDEX = "FULLTEXT:";
  /** Index info. */
  String TRIE = "- Compressed Trie";
  /** Index info. */
  String DISKHASH = "- Disk-Based Hash";
  /** Index info. */
  String HASHBUCKETS = "- Buckets: ";
  /** Index info. */
  String SIZEDISK = "- Size on Disk: ";

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
  String QUERYPLAN = lang("qu_plan");
  /** Query Info: Compiler. */
  String QUERYSEP = "- ";
  /** Query Info: Plan. */
  String PLANXML = "plan.xml";
  /** Query Info: Plan. */
  String PLANDOT = "plan.dot";
  /** Query Info: Plan. */
  String RESULTDOT = "result.dot";

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
  String MS = " " + lang("qu_ms");

  /** Query Hits. */
  String VALHIT = "Item";
  /** Query Hits. */
  String VALHITS = "Items";

  // UPDATE COMMANDS ==========================================================

  /** Insert query info. */
  String QUERYNODESERR = "Query must yield a nodeset.";

  /** Deletion info. */
  String DELETEINFO = "% node(s) deleted in %.";
  /** Deletion error. */
  String DELETEROOT = "Cannot delete document node.";
  /** Insert query info. */
  String COPYROOT = "Cannot insert after root node.";

  /** Copy query info. */
  String COPYINFO = "% node(s) copied in %.";
  /** Insert query info. */
  String INSERTINFO = "% node(s) inserted in %.";
  /** Update query info. */
  String UPDATEINFO = "% node(s) updated in %.";
  /** Update node info. */
  String UPDATENODE = "Node to be updated must be a %.";
  /** Update element info. */
  String UPDATEELEM = "Node to be updated must be an element.";
  /** Update attribute info. */
  String UPDATEATTR = "Node to be updated must be an attribute.";
  /** Insert query info. */
  String COPYTAGS = "Target nodes must be elements.";
  /** Insert query info. */
  String INSERTTEXT = "Cannot insert text node after/before another text node.";
  /** Update element info. */
  String TAGINVALID = "\"%\" is not a valid tag name.";
  /** Update element info. */
  String ATTINVALID = "\"%\" is not a valid attribute name.";
  /** Duplicate attribute info. */
  String ATTDUPL = "Attribute \"%\" is already defined.";
  /** Invalid position. */
  String POSINVALID = "Invalid position value \"%\".";
  /** Update PI info. */
  String PIINVALID = "PI name \"%\" is invalid.";
  /** Update text info. */
  String TXTINVALID = "Text nodes cannot be empty.";

  // INFO STRINGS =============================================================

  /** Info on source document. */
  String INFODBNAME = "Database Name: ";
  /** Info on document size. */
  String INFODOCSIZE = "XML Doc.Size : ";
  /** Info on database size. */
  String INFODBSIZE = "Database Size: ";
  /** Info on source document. */
  String INFODOC = "Source Path  : ";
  /** Info on document path. */
  String INFOPATH = "Path: ";
  /** Info on database time stamp. */
  String INFOTIME = "Time Stamp   : ";
  /** Info on used main memory. */
  String INFOMEM = "Used Main Memory: ";
  /** Info on database table size. */
  String INFONODES = "Table Nodes  : ";
  /** Maximum tree height. */
  String INFOHEIGHT = "Tree Height  : ";
  /** Document encoding. */
  String INFOENCODING = "Encoding     : ";
  /** Info on execution times. */
  String INFORUNS = "Number of runs";

  /** Info on database path. */
  String INFODBPATH = "Database Path: ";
  /** Info on database. */
  String INFONODB = "No database found.";
  /** Info on database. */
  String INFODBLIST = " Databases found.";
  /** Info on database. */
  String INFODBERR = "could not be opened.";

  /** Info on database path. */
  String INFONEWPATH = "New Database Path: ";
  /** Info on database path. */
  String INFOPATHERR = "Database Path not found: ";
  /** Info on Query Verbosity. */
  String INFOINFO = "Process Info ";
  /** Info on Query Verbosity. */
  String INFOALL = "(All) ";
  /** Info on Debug Mode. */
  String INFODEBUG = "Debug Mode ";
  /** Info on Whitespace Chopping. */
  String INFOCHOP = "Whitespace Chopping ";
  /** Info on Entity Parsing. */
  String INFOENTITIES = "Entity Parsing ";
  /** Info on result serialization. */
  String INFOSERIALIZE = "Serialize ";
  /** Info on well formed XML serialization. */
  String INFOXMLOUTPUT = "XML Output ";
  /** Info on Main Memory mode. */
  String INFOMAINMEM = "Main Memory Mode ";
  /** Info on Text Indexing. */
  String INFOTXTINDEX = "Text Indexing ";
  /** Info on Attribute Indexing. */
  String INFOATVINDEX = "Attribute Indexing ";
  /** Info on Word Indexing. */
  String INFOWORDINDEX = "Word Indexing ";
  /** Info on Fulltext Indexing. */
  String INFOFTINDEX = "Fulltext Indexing ";

  /** Info on Document Creation. */
  String INFODB = "Database Information:";
  /** Info on Document Creation. */
  String INFOCREATE = "Document Creation:";
  /** Info on Index Creation. */
  String INFOINDEXES = "Indexing:";
  /** Database Info. */
  String INFOGENERAL = "General Information:";
  /** Database Info. */
  String RESULTCHOP = "\n...output was chopped.";
  
  // HELP TEXTS ===============================================================

  /** Help String. */
  byte[] HELPFILTER = token(lang("h_filter"));
  /** Help String. */
  byte[] HELPQUERYRT = token(lang("h_queryrt"));
  /** Help String. */
  byte[] HELPFILTERRT = token(lang("h_filterrt"));
  /** Help String. */
  byte[] HELPTOXQUERY =
    token("The current XPath query is copied to the XQuery search panel.");
  /** Help String. */
  byte[] HELPTOXPATH =
    token("By modifying the search widgets, XPath queries are internally " +
        "built and executed. You can display and change the current query " +
        "by pressing this button.");
  /** Help String. */
  byte[] HELPEXEC =
    token("The entered commands will be executed by pressing this button.");
  /** Help String. */
  byte[] HELPSTOP =
    token("Press this button to stop the database build process.");
  /** Help String. */
  byte[] HELPCANCEL = token("Press this button to close the dialog and " +
      "return to the main window.");
  /** Help String. */
  byte[] HELPMODE = token("Click on the button to switch between the " +
      "search, XPath and command mode.\nThis button is only active if a " +
      "database has been opened.");
  /** Help String. */
  byte[] HELPOK =
    token("Press this button to start the process.");
  /** Help String. */
  byte[] HELPRENAMEDB =
    token("Press this button to rename the chosen database.");
  /** Help String. */
  byte[] HELPOPENDB =
    token("Press this button to open the chosen database.");
  /** Help string. */
  byte[] HELPOPENINFO = token("This panel displays some information on " +
      "the currently selected database.");
  /** Help String. */
  byte[] HELPFALIAS =
    token("All texts can be anti-aliased by choosing this checkbox.");
  /** Help String. */
  byte[] HELPBROWSE =
    token("You can use the file chooser to choose a directory.");
  /** Help String. */
  byte[] HELPFSPATH =
    token("Use this text field to enter or modify the directory to be parsed.");
  /** Help String. */
  byte[] HELPDBPATH =
    token("Here you can modify the current database path.");
  /** Help String. */
  byte[] HELPLANG =
    token("Here you can switch to another language. Note that BaseX must " +
        "be restarted to activate the chosen language.");
  /** Help String. */
  byte[] HELPNAMES =
    token("This option checks if tags contain a 'name' attribute. " +
      "If the attribute exists, the value of the attribute is " +
      "shown instead of the tag. You can't change this option " +
      "if the current database has no 'name' attributes");
  /** Help String. */
  byte[] HELPFSNAME =
    token("Enter the name of the new database in this text field.");
  /** Help String. */
  byte[] HELPFSALL =
    token("If this checkbox is chosen, the whole file system will be parsed.");
  /** Help String. */
  byte[] HELPMETA =
    token("By choosing this checkbox, meta-data will be extracted from " +
      "numerous file types. Meta-data includes e.g. the dimensions of " +
      "images, ID3 tags of MP3 files or contents of email files.");
  /** Help String. */
  byte[] HELPSTAT =
    token("If this checkbox is chosen, 'stat' information will be extracted " +
      "from files and directories. This includes e.g. the owner rights, " +
      "time stamps or the unique node IDs of files and directories.");
  /** Help String. */
  byte[] HELPCONT =
    token("The textual contents of files can be included in the database by " +
      "choosing this checkbox. If the contents are included, they can " +
      "be searched via XPath or XQuery.");
  /** Help String. */
  byte[] HELPFSMAX =
    token("Here you can choose a size limit for the included text files.");
  /** Help String. */
  byte[] HELPMAPSIMPLE =
    token("If this checkbox is turned on, a simpler algorithm will be " +
      "used to calculate the TreeMap.");
  /** Help String. */
  byte[] HELPMAPATTS =
    token("If this checkbox is turned on, attributes are shown inside " +
        "the TreeMap.");
  /** Help String. */
  byte[] HELPMAPLAYOUT =
    token("Here you can modify the layout algorithm which is used in the " +
      "Map visualization. The changes can be immediately seen if a " +
      "database is opened.");
  /** Help String. */
  byte[] HELPMAPALIGN =
    token("Here you can modify the alignment of the rectangles in the " +
      "Map visualization. The changes can be immediately seen if a " +
      "database is opened.");
  /** Help String. */
  byte[] HELPDROP = token("Choose the button to drop the currently " +
      "chosen database");
  /** Help String. */
  byte[] HELPDROPDB = token("Choose the database you want to drop.");
  /** Help String. */
  byte[] HELPFOCUS =
    token("If this checkbox is chosen, input components are focused " +
      "as soon as the cursor is moved over them.");
  /** Help String. */
  byte[] HELPSIMPLEFD =
    token("If you have troubles using the default file chooser, you can " +
      "switch to a simple, but faster one.");
  /** Help String. */
  byte[] HELPOPEN = token("Choose the database you want to open.");
  /** Help String. */
  byte[] HELPFONT =
    token("Here you can choose the fonts which are used in the views.");
  /** Help string. */
  byte[] HELPMMEM = token("In main-memory mode, queries will be executed " +
      "50-80% faster. As a drawback, main-memory consumption increases, " +
      "and updates on the database are disabled.");
  /** Help string. */
  byte[] HELPCOLORS = token("Move the sliders to change the colors of " +
      "the visual interface.");
  /** Help Dialog. */
  byte[] HELPCMD = token("Here you can enter BaseX commands. " +
    "Try 'help' to get a survey on all available commands.\n\n" +
    "You can retrieve previous inputs with the cursor keys UP and DOWN.");
  /** Help Dialog. */
  byte[] HELPSEARCHXML = token("Just type something in the text field to " +
    "search in your XML document. The following syntax is supported:\n\n" +
    "...     Find tags and texts\n" +
    "=...    Find exact text nodes\n" +
    "@ ...   Find attributes and attribute values\n" +
    "@=      Find exact attribute values\n" +
    "/...    Evaluate input as XPath\n\n" +
    "You can enter BaseX commands by preceding your input with an " +
    "exclamation mark.");
  /** Help Dialog. */
  byte[] HELPSEARCHFS = token("Just type something in the text field to " +
    "search in your filesystem. The following syntax is supported:\n\n" +
    "...      Find any files or directories\n" +
    "= ...    Find exact names\n" +
    "~ ...    Find approximate names\n" +
    ". ..     Find file extensions\n" +
    "< ...    Find files smaller than the specified size\n" +
    "> ...    Find files larger than the specified size\n" +
    "...=...  Find metadata (e.g. 'Album=Beatles')\n\n" +
    "You can enter BaseX commands by preceding your input with an " +
    "exclamation mark.");
  /** Help Dialog. */
  byte[] HELPXPATH = token(
      "The XPath mode allows the input of simple XPath requests. " +
      "XPath is often processed faster than XQuery. " +
      "The XPath language is based on a tree representation " +
      "of the XML document, and provides the ability to navigate " +
      "around the tree, selecting nodes by a variety of criteria.");
  /** Help String. */
  String HELPHIST = "\n\nThe text area offers a history function: " +
    "You can store the current input with Ctrl+ENTER and retrieve " +
    "previous inputs with Ctrl+UP/DOWN.";
  /** Help String. */
  String HELPMODEFILT = "If real-time filtering is deactivated, press " +
    "Ctrl+ENTER to filter the query results.";

  /** Help String. */
  byte[] HELPMAP =
    token("This visualization is called TreeMap. All nodes of the XML " +
        "document are represented as rectangles, filling the complete " +
        "area. Nodes can be highlighted by single clicks and filtered " +
        "by double clicks. A popup menu will be shown by right-clicking " +
        "on an element");
  /** Help String. */
  byte[] HELPTREE =
    token("This visualization displays all XML nodes in a usual tree view. " +
        "Nodes can be expanded or closed by clicks on the arrows. " +
        "Although they can be highlighted by single clicks and zoomed in " +
        "by double clicks. This is an efficient way to explore the " +
        "XML document. A popup menu will be shown by right-clicking " +
        "on an element.");
  /** Help String. */
  byte[] HELPTABLE =
    token("This visualization displays XML nodes in a table. " +
        "If you click on a text, all nodes with the same text are " +
        "highlighted. By double-clicking on a text, the query results " +
        "are filtered. A popup menu will be shown by right-clicking on an " +
        "element\n\n" +
        "Note that many XML documents are pretty hierarchic, so this view " +
        "works only for highly regular documents.");
  /** Help String. */
  byte[] HELPTEXT =
    token("This panel displays query results and other textual output.\n" +
        "\nKey Shortcuts: " +
        "Press '-' and '+' to change the font size of all panels. " +
        "Type in any text into the find popup to launch an incremental " +
        "search for it. It's an easy way to find special text points.");
  /** Help String. */
  byte[] HELPINFO =
    token("This panel displays information on the compilation and " +
        "evaluation of an XPath or XQuery request, including some " +
        "simple profiling");

  /** Help String. */
  byte[][] HELPSEARCH = {
      token("The XQuery input mode offers the full range of query " +
         "features that is offered by the XQuery language. " +
         "XQuery uses XPath expression syntax to address specific " +
         "parts of an XML document. It supplements this with a SQL-like " +
         "FLWOR expression for performing joins. A FLWOR expression is " +
         "constructed from the five clauses after which it is named: " +
         "FOR, LET, WHERE, ORDER BY, RETURN."),
      token("The simple search mode allows simple queries in the " +
        "XML document.")
  };
  /** Help String. */
  byte[] HELPQUERYMODE =
    token("In this text area, you can enter any XQuery 1.0 query. " +
        "The input is evaluated after each key click.\n\n" +
        "The following example would show all book titles " +
        " with the topic 'BaseX':\n\n" +
        "  for $a IN //book\n" +
        "  where $a/topic = \"BaseX\"\n" +
        "  return $a/title" + HELPHIST);
  /** Help String. */
  byte[] HELPSEARCHCAT =
    token("In this combo box, all tags and attribute names of the current " +
        "XML document are listed. If an entry is chosen, the " +
        "query results are marked in the visualizations. If the chosen entry " +
        "contains textual values, an additional widget pops up, which " +
        "allows a further specification of the query.");
  /** Help String. */
  byte[] HELPCAT =
    token("This combo box lists all values of the current category. If you " +
        "choose one of the values, the query is further refined.");
  /** Help String. */
  byte[] HELPDS =
    token("Drag the left and right arrow of this double slider to restrict " +
        "the values of the current query category. If you have changed " +
        "the range, you can also click inside the slider to move it " +
        "left and right.");
  /** Help String. */
  byte[] HELPCATINPUT =
    token("Here you can further limit your query by entering substrings. " +
        "To find exact matches and speedup search, you can enclose your " +
        "input with quotes (\"...\").");
  /** Help String. */
  byte[] HELPMOVER =
    token("You can change the position of a view by dragging the bar on " +
        "top of all view panels to any other place inside the main window.\n" +
        "If you move a view outside the main window, it will be closed.");
  /** Help String. */
  byte[] HELPTABLEHEAD =
    token("You can sort the table by  on the corresponding " +
        "column in the table header. Moreover, you can directly query " +
        "inside the table by entering something into the text fields.");
  /** Help String. */
  byte[] HELPMEM =
    token("This box displays the reserved memory of BaseX. " +
        "By clicking on the box, you can enforce garbage collection.\n\n" +
        "The display turns red if free memory is low. If this happens " +
        "regularly, just launch BaseX with the Java flag -Xmx... " +
        "('java -X' gives you more information).");

  /** Dummy string to check if all language strings have been assigned. */
  String CHECK = lang(null);
}
