package org.basex.gui;

import static org.basex.util.Prop.*;

import java.awt.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * This class contains options which are used in the GUI.
 * They are also stored in the project's home directory.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class GUIOptions extends Options {
  // DATABASE & PROGRAM PATHS =================================================

  /** Comment: written to options file. */
  public static final Comment C_PATHS = new Comment("Paths");

  /** Path to database input. */
  public static final StringOption INPUTPATH = new StringOption("INPUTPATH", HOME);
  /** Path for additional material. */
  public static final StringOption DATAPATH = new StringOption("DATAPATH", HOME);
  /** Path to working directory. */
  public static final StringOption WORKPATH = new StringOption("WORKPATH", HOME);
  /** Path to database project. */
  public static final StringOption PROJECTPATH = new StringOption("PROJECTPATH", "");
  /** Last editor files. */
  public static final StringsOption EDITOR = new StringsOption("EDITOR");
  /** Input paths. */
  public static final StringsOption INPUTS = new StringsOption("INPUTS");
  /** Open editor files. */
  public static final StringsOption OPEN = new StringsOption("OPEN");

  /** Comment: written to options file. */
  public static final Comment C_LAYOUT = new Comment("Layout");

  /** Default GUI Font. */
  public static final StringOption FONT = new StringOption("FONT", Font.SANS_SERIF);
  /** Default GUI Monospace Font. */
  public static final StringOption MONOFONT = new StringOption("MONOFONT",
      Prop.WIN ? "Consolas" : Font.MONOSPACED);
  /** Font TYPE = plain, bold, italics). */
  public static final NumberOption FONTTYPE = new NumberOption("FONTTYPE", 0);
  /** Font size. */
  public static final NumberOption FONTSIZE = new NumberOption("FONTSIZE", GUIConstants.FONTSIZE);

  /** Red GUI color factor. */
  public static final NumberOption COLORRED = new NumberOption("COLORRED", 15);
  /** Green GUI color factor. */
  public static final NumberOption COLORGREEN = new NumberOption("COLORGREEN", 11);
  /** Blue GUI color factor. */
  public static final NumberOption COLORBLUE = new NumberOption("COLORBLUE", 6);
  /** Paint gradients as background. */
  public static final BooleanOption GRADIENT = new BooleanOption("GRADIENT", true);

  /** Comment: written to options file. */
  public static final Comment C_WINDOWS = new Comment("Windows");

  /** Last updated version. */
  public static final StringOption UPDATEVERSION = new StringOption("UPDATEVERSION",
    Prop.VERSION.replaceAll(" .*", ""));

  /** GUI Layout. */
  public static final StringOption VIEWS = new StringOption("VIEWS", GUIConstants.VIEWS);

  /** GUI height. */
  public static final NumbersOption GUISIZE = new NumbersOption("GUISIZE", 1004, 748);
  /** GUI position. */
  public static final NumbersOption GUILOC = new NumbersOption("GUILOC", 10, 10);
  /** Flag for maximized GUI window. */
  public static final BooleanOption MAXSTATE = new BooleanOption("MAXSTATE", false);

  /** Flag for displaying buttons in the GUI window. */
  public static final BooleanOption SHOWBUTTONS = new BooleanOption("SHOWBUTTONS", true);
  /** Flag for displaying the text field in the GUI window. */
  public static final BooleanOption SHOWINPUT = new BooleanOption("SHOWINPUT", true);
  /** Flag for displaying the status bar in the GUI window. */
  public static final BooleanOption SHOWSTATUS = new BooleanOption("SHOWSTATUS", true);

  /** Flag for activated info view. */
  public static final BooleanOption SHOWINFO = new BooleanOption("SHOWINFO", true);
  /** Flag for activated map view. */
  public static final BooleanOption SHOWMAP = new BooleanOption("SHOWMAP", true);
  /** Flag for activated table view. */
  public static final BooleanOption SHOWTABLE = new BooleanOption("SHOWTABLE", false);
  /** Flag for activated result view. */
  public static final BooleanOption SHOWTEXT = new BooleanOption("SHOWTEXT", true);
  /** Flag for activated tree view. */
  public static final BooleanOption SHOWFOLDER = new BooleanOption("SHOWFOLDER", false);
  /** Flag for activated query view. */
  public static final BooleanOption SHOWEXPLORE = new BooleanOption("SHOWEXPLORE", false);
  /** Flag for activated plot view. */
  public static final BooleanOption SHOWPLOT = new BooleanOption("SHOWPLOT", false);
  /** Flag for activated xquery view. */
  public static final BooleanOption SHOWEDITOR = new BooleanOption("SHOWEDITOR", true);
  /** Flag for activated tree view. */
  public static final BooleanOption SHOWTREE = new BooleanOption("SHOWTREE", false);
  /** Flag for activated project structure. */
  public static final BooleanOption SHOWPROJECT = new BooleanOption("SHOWPROJECT", true);

  /** Dialog location. */
  public static final NumbersOption FONTSLOC = new NumbersOption("FONTSLOC", 10, 530);
  /** Dialog location. */
  public static final NumbersOption COLORSLOC = new NumbersOption("COLORSLOC", 530, 620);

  /** Preferences tab. */
  public static final NumberOption PREFTAB = new NumberOption("PREFTAB", 0);
  /** Flag for Java look and feel. */
  public static final StringOption LOOKANDFEEL = new StringOption("LOOKANDFEEL", "");
  /** Flag for dissolving name attributes. */
  public static final BooleanOption SHOWNAME = new BooleanOption("SHOWNAME", true);
  /** Focus follows mouse. */
  public static final BooleanOption MOUSEFOCUS = new BooleanOption("MOUSEFOCUS", false);
  /** Flag for showing the simple file dialog. */
  public static final BooleanOption SIMPLEFD = new BooleanOption("SIMPLEFD", false);

  /** Sort ascending. */
  public static final BooleanOption ASCSORT = new BooleanOption("ASCSORT", true);
  /** Case sensitive sorting. */
  public static final BooleanOption CASESORT = new BooleanOption("CASESORT", true);
  /** Merge duplicate lines. */
  public static final BooleanOption MERGEDUPL = new BooleanOption("MERGEDUPL", false);

  /** Show line margin. */
  public static final BooleanOption SHOWMARGIN = new BooleanOption("SHOWMARGIN", true);
  /** Line margin. */
  public static final NumberOption MARGIN = new NumberOption("MARGIN", 80);
  /** Insert tabs as spaces. */
  public static final BooleanOption TABSPACES = new BooleanOption("TABSPACES", true);
  /** Indentation. */
  public static final NumberOption INDENT = new NumberOption("INDENT", 2);
  /** Show invisible characters. */
  public static final BooleanOption SHOWINVISIBLE = new BooleanOption("SHOWINVISIBLE", true);
  /** Show newlines. */
  public static final BooleanOption SHOWNL = new BooleanOption("SHOWNL", false);
  /** Show line numbers. */
  public static final BooleanOption SHOWLINES = new BooleanOption("SHOWLINES", true);
  /** Mark current line. */
  public static final BooleanOption MARKLINE = new BooleanOption("MARKLINE", true);
  /** Save before executing file. */
  public static final BooleanOption SAVERUN = new BooleanOption("SAVERUN", false);
  /** Automatically add characters. */
  public static final BooleanOption AUTO = new BooleanOption("AUTO", true);
  /** Default file filter. */
  public static final StringOption FILES = new StringOption("FILES", "*.xml, *.xq*");

  /** Current input mode in global text field (Search, XQuery, Command). */
  public static final NumberOption SEARCHMODE = new NumberOption("SEARCHMODE", 0);
  /** Flag for realtime context filtering. */
  public static final BooleanOption FILTERRT = new BooleanOption("FILTERRT", false);
  /** Flag for realtime query execution. */
  public static final BooleanOption EXECRT = new BooleanOption("EXECRT", false);

  /** Name of new database. */
  public static final StringOption DBNAME = new StringOption("DBNAME", "");
  /** Last insertion type. */
  public static final NumberOption LASTINSERT = new NumberOption("LASTINSERT", 1);

  /** Comment: written to options file. */
  public static final Comment C_SERVER = new Comment("Server Dialog");

  /** Server: host, used for connecting new clients. */
  public static final StringOption S_HOST = new StringOption("S_HOST", Text.S_LOCALHOST);
  /** Server: port, used for connecting new clients. */
  public static final NumberOption S_PORT = new NumberOption("S_PORT", 1984);
  /** Server: port, used for binding the server. */
  public static final NumberOption S_SERVERPORT = new NumberOption("S_SERVERPORT", 1984);
  /** Server: port, used for sending events. */
  public static final NumberOption S_EVENTPORT = new NumberOption("S_EVENTPORT", 1985);
  /** Default user. */
  public static final StringOption S_USER = new StringOption("S_USER", "");
  /** Default password. */
  public static final StringOption S_PASSWORD = new StringOption("S_PASSWORD", "");

  /** Comment: written to options file. */
  public static final Comment C_VISUALIZATIONS = new Comment("Visualizations");

  /** Show attributes in treemap. */
  public static final BooleanOption MAPATTS = new BooleanOption("MAPATTS", false);
  /** Treemap Offsets. */
  public static final NumberOption MAPOFFSETS = new NumberOption("MAPOFFSETS", 3);
  /** Map algorithm. */
  public static final NumberOption MAPALGO = new NumberOption("MAPALGO", 0);
  /** number of children <-> size weight in (0;100). */
  public static final NumberOption MAPWEIGHT = new NumberOption("MAPWEIGHT", 0);

  /** Slim rectangles to text length. */
  public static final BooleanOption TREESLIMS = new BooleanOption("TREESLIM", true);
  /** Show attributes in treeview. */
  public static final BooleanOption TREEATTS = new BooleanOption("TREEATTS", false);

  /** Dot sizes in plot. */
  public static final NumberOption PLOTDOTS = new NumberOption("PLOTDOTS", 0);
  /** Logarithmic plot. */
  public static final BooleanOption PLOTXLOG = new BooleanOption("PLOTXLOG", false);
  /** Logarithmic plot. */
  public static final BooleanOption PLOTYLOG = new BooleanOption("PLOTYLOG", false);

  /** Maximum text size to be displayed. */
  public static final NumberOption MAXTEXT = new NumberOption("MAXTEXT", 1 << 21);
  /** Maximum number of hits to be displayed (-1: return all hits; default: 250K). */
  public static final NumberOption MAXHITS = new NumberOption("MAXHITS", 250000);

  /** Comment: written to options file. */
  public static final Comment C_SEARCH = new Comment("Search");

  /** Search text. */
  public static final StringOption SR_SEARCH = new StringOption("SR_SEARCH", "");
  /** Replace text. */
  public static final StringOption SR_REPLACE = new StringOption("SR_REPLACE", "");
  /** Regular expressions. */
  public static final BooleanOption SR_REGEX = new BooleanOption("SR_REGEX", false);
  /** Match case. */
  public static final BooleanOption SR_CASE = new BooleanOption("SR_CASE", false);
  /** Whole word. */
  public static final BooleanOption SR_WORD = new BooleanOption("SR_WORD", false);
  /** Multi-line mode. */
  public static final BooleanOption SR_MULTI = new BooleanOption("SR_MULTI", false);
  /** Last searched strings. */
  public static final StringsOption SEARCHED = new StringsOption("SEARCHED");
  /** Last replaced strings. */
  public static final StringsOption REPLACED = new StringsOption("REPLACED");

  /** Comment: written to options file. */
  public static final Comment C_HISTORY = new Comment("History");

  /** Last command inputs. */
  public static final StringsOption COMMANDS = new StringsOption("COMMANDS");
  /** Last keyword inputs. */
  public static final StringsOption SEARCH = new StringsOption("SEARCH");
  /** Last XQuery inputs. */
  public static final StringsOption XQUERY = new StringsOption("XQUERY");

  /**
   * Constructor.
   */
  public GUIOptions() {
    super(new IOFile(HOME + IO.BASEXSUFFIX + "gui"));
    // reset realtime operations
    set(FILTERRT, false);
    set(EXECRT, false);
    gui = true;
  }
}
