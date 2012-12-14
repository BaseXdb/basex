package org.basex.gui;

import java.awt.*;

import org.basex.core.*;

/**
 * This class contains properties which are used in the GUI.
 * They are also stored in the project's home directory.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class GUIProp extends AProp {
  // DATABASE & PROGRAM PATHS =================================================

  /** Comment: written to property file. */
  public static final Object[] C_PATHS = { "Paths" };

  /** Path to database input. */
  public static final Object[] INPUTPATH = { "INPUTPATH", Prop.HOME };
  /** Path for additional material. */
  public static final Object[] DATAPATH = { "DATAPATH", Prop.HOME };
  /** Path to working directory. */
  public static final Object[] WORKPATH = { "WORKPATH", Prop.HOME };
  /** Last editor files. */
  public static final Object[] EDITOR = { "EDITOR", new String[0] };
  /** Input paths. */
  public static final Object[] INPUTS = { "INPUTS", new String[0] };

  /** Comment: written to property file. */
  public static final Object[] C_LAYOUT = { "Layout" };

  /** Default GUI Font. */
  public static final Object[] FONT = { "FONT", Font.SANS_SERIF };
  /** Default GUI Monospace Font. */
  public static final Object[] MONOFONT = { "MONOFONT", Font.MONOSPACED };
  /** Font TYPE = plain, bold, italics). */
  public static final Object[] FONTTYPE = { "FONTTYPE", 0 };
  /** Font size. */
  public static final Object[] FONTSIZE = { "FONTSIZE", 13 };

  /** Red GUI color factor. */
  public static final Object[] COLORRED = { "COLORRED", 20 };
  /** Green GUI color factor. */
  public static final Object[] COLORGREEN = { "COLORGREEN", 17 };
  /** Blue GUI color factor. */
  public static final Object[] COLORBLUE = { "COLORBLUE", 7 };
  /** Paint gradients as background. */
  public static final Object[] GRADIENT = { "GRADIENT", true };

  /** Comment: written to property file. */
  public static final Object[] C_WINDOWS = { "Windows" };

  /** Last updated version. */
  public static final Object[] UPDATEVERSION = { "UPDATEVERSION",
    Prop.VERSION.replaceAll(" .*", "") };

  /** GUI Layout. */
  public static final Object[] VIEWS = { "VIEWS", GUIConstants.VIEWS };

  /** GUI height. */
  public static final Object[] GUISIZE = { "GUISIZE", new int[] { 1004, 748 } };
  /** GUI position. */
  public static final Object[] GUILOC = { "GUILOC", new int[] { 10, 10 } };
  /** Flag for maximized GUI window. */
  public static final Object[] MAXSTATE = { "MAXSTATE", false };

  /** Flag for displaying buttons in the GUI window. */
  public static final Object[] SHOWBUTTONS = { "SHOWBUTTONS", true };
  /** Flag for displaying the text field in the GUI window. */
  public static final Object[] SHOWINPUT = { "SHOWINPUT", true };
  /** Flag for displaying the status bar in the GUI window. */
  public static final Object[] SHOWSTATUS = { "SHOWSTATUS", true };

  /** Flag for activated info view. */
  public static final Object[] SHOWINFO = { "SHOWINFO", true };
  /** Flag for activated map view. */
  public static final Object[] SHOWMAP = { "SHOWMAP", true };
  /** Flag for activated table view. */
  public static final Object[] SHOWTABLE = { "SHOWTABLE", false };
  /** Flag for activated result view. */
  public static final Object[] SHOWTEXT = { "SHOWTEXT", true };
  /** Flag for activated tree view. */
  public static final Object[] SHOWFOLDER = { "SHOWFOLDER", false };
  /** Flag for activated query view. */
  public static final Object[] SHOWEXPLORE = { "SHOWEXPLORE", false };
  /** Flag for activated plot view. */
  public static final Object[] SHOWPLOT = { "SHOWPLOT", false };
  /** Flag for activated xquery view. */
  public static final Object[] SHOWEDITOR = { "SHOWEDITOR", true };
  /** Flag for activated tree view. */
  public static final Object[] SHOWTREE = { "SHOWTREE", false };

  /** Dialog location. */
  public static final Object[] MAPLAYOUTLOC = { "MAPLAYOUTLOC", new int[] { 790, 520 } };
  /** Dialog location. */
  public static final Object[] FONTSLOC = { "FONTSLOC", new int[] { 10, 530 } };
  /** Dialog location. */
  public static final Object[] COLORSLOC = { "COLORSLOC", new int[] { 530, 620 } };
  /** Dialog location. */
  public static final Object[] REPLACELOC = { "REPLACELOC", new int[] { 530, 230 } };

  /** Flag for Java look and feel. */
  public static final Object[] JAVALOOK = { "JAVALOOK", false };
  /** Flag for dissolving name attributes. */
  public static final Object[] SHOWNAME = { "SHOWNAME", true };
  /** Focus follows mouse. */
  public static final Object[] MOUSEFOCUS = { "MOUSEFOCUS", false };
  /** Flag for showing the simple file dialog. */
  public static final Object[] SIMPLEFD = { "SIMPLEFD", false };

  /** Current input mode in global text field (Search, XQuery, Command). */
  public static final Object[] SEARCHMODE = { "SEARCHMODE", 0 };
  /** Flag for realtime context filtering. */
  public static final Object[] FILTERRT = { "FILTERRT", false };
  /** Flag for realtime query execution. */
  public static final Object[] EXECRT = { "EXECRT", false };

  /** Name of new database. */
  public static final Object[] DBNAME = { "CREATENAME", "" };
  /** Last insertion type. */
  public static final Object[] LASTINSERT = { "LASTINSERT", 1 };

  /** Comment: written to property file. */
  public static final Object[] C_SERVER = { "Server Dialog" };

  /** Server: host, used for connecting new clients. */
  public static final Object[] S_HOST = { "S_HOST", Text.LOCALHOST };
  /** Server: port, used for connecting new clients. */
  public static final Object[] S_PORT = { "S_PORT", 1984 };
  /** Server: port, used for binding the server. */
  public static final Object[] S_SERVERPORT = { "S_SERVERPORT", 1984 };
  /** Server: port, used for sending events. */
  public static final Object[] S_EVENTPORT = { "S_EVENTPORT", 1985 };
  /** Default user. */
  public static final Object[] S_USER = { "S_USER", "" };
  /** Default password. */
  public static final Object[] S_PASSWORD = { "S_PASSWORD", "" };

  /** Comment: written to property file. */
  public static final Object[] C_VISUALIZATIONS = { "Visualizations" };

  /** Show attributes in treemap. */
  public static final Object[] MAPATTS = { "MAPATTS", false };
  /** Treemap Offsets. */
  public static final Object[] MAPOFFSETS = { "MAPOFFSETS", 3 };
  /** Map algorithm. */
  public static final Object[] MAPALGO = { "MAPALGO", 0 };
  /** number of children <-> size weight in (0;100). */
  public static final Object[] MAPWEIGHT = { "MAPWEIGHT", 0 };

  /** Slim rectangles to text length. */
  public static final Object[] TREESLIMS = { "TREESLIM", true};
  /** Show attributes in treeview. */
  public static final Object[] TREEATTS = { "TREEATTS", false};

  /** Dot sizes in plot. */
  public static final Object[] PLOTDOTS = { "PLOTDOTS", 0 };
  /** Logarithmic plot. */
  public static final Object[] PLOTXLOG = { "PLOTXLOG", false };
  /** Logarithmic plot. */
  public static final Object[] PLOTYLOG = { "PLOTYLOG", false };

  /** Maximum text size to be displayed. */
  public static final Object[] MAXTEXT = { "MAXTEXT", 1 << 21 };
  /** Maximum number of hits to be displayed (-1: return all hits; default: 250K). */
  public static final Object[] MAXHITS = { "MAXHITS", 250000 };

  /** Comment: written to property file. */
  public static final Object[] C_SEARCH = { "Search" };

  /** Search text. */
  public static final Object[] SR_SEARCH = { "SR_SEARCH", "" };
  /** Replace text. */
  public static final Object[] SR_REPLACE = { "SR_REPLACE", "" };
  /** Regular expressions. */
  public static final Object[] SR_REGEX = { "SR_REGEX", false };
  /** Match case. */
  public static final Object[] SR_CASE = { "SR_CASE", false };
  /** Whole word. */
  public static final Object[] SR_WORD = { "SR_WORD", false };
  /** Multi-line mode. */
  public static final Object[] SR_MULTI = { "SR_MULTI", false };
  /** Last searched strings. */
  public static final Object[] SEARCHED = { "SEARCHED", new String[0] };
  /** Last replaced strings. */
  public static final Object[] REPLACED = { "REPLACED", new String[0] };

  /** Comment: written to property file. */
  public static final Object[] C_HISTORY = { "History" };

  /** Last command inputs. */
  public static final Object[] COMMANDS = { "COMMANDS", new String[0] };
  /** Last keyword inputs. */
  public static final Object[] SEARCH = { "SEARCH", new String[0] };
  /** Last XQuery inputs. */
  public static final Object[] XQUERY = { "XQUERY", new String[0] };

  /**
   * Constructor.
   */
  public GUIProp() {
    super("gui");
    // reset realtime operations
    set(GUIProp.FILTERRT, false);
    set(GUIProp.EXECRT, false);
    Prop.gui = true;
  }
}
