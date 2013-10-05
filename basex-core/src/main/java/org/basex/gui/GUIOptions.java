package org.basex.gui;

import java.awt.*;

import org.basex.core.*;
import org.basex.util.*;

/**
 * This class contains options which are used in the GUI.
 * They are also stored in the project's home directory.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class GUIOptions extends AOptions {
  // DATABASE & PROGRAM PATHS =================================================

  /** Comment: written to options file. */
  public static final Option C_PATHS = new Option("Paths");

  /** Path to database input. */
  public static final Option INPUTPATH = new Option("INPUTPATH", Prop.HOME);
  /** Path for additional material. */
  public static final Option DATAPATH = new Option("DATAPATH", Prop.HOME);
  /** Path to working directory. */
  public static final Option WORKPATH = new Option("WORKPATH", Prop.HOME);
  /** Last editor files. */
  public static final Option EDITOR = new Option("EDITOR", new String[0]);
  /** Input paths. */
  public static final Option INPUTS = new Option("INPUTS", new String[0]);

  /** Comment: written to options file. */
  public static final Option C_LAYOUT = new Option("Layout");

  /** Default GUI Font. */
  public static final Option FONT = new Option("FONT", Font.SANS_SERIF);
  /** Default GUI Monospace Font. */
  public static final Option MONOFONT = new Option("MONOFONT", Font.MONOSPACED);
  /** Font TYPE = plain, bold, italics). */
  public static final Option FONTTYPE = new Option("FONTTYPE", 0);
  /** Font size. */
  public static final Option FONTSIZE = new Option("FONTSIZE", 13);

  /** Red GUI color factor. */
  public static final Option COLORRED = new Option("COLORRED", 13);
  /** Green GUI color factor. */
  public static final Option COLORGREEN = new Option("COLORGREEN", 11);
  /** Blue GUI color factor. */
  public static final Option COLORBLUE = new Option("COLORBLUE", 6);
  /** Paint gradients as background. */
  public static final Option GRADIENT = new Option("GRADIENT", true);

  /** Comment: written to options file. */
  public static final Option C_WINDOWS = new Option("Windows");

  /** Last updated version. */
  public static final Option UPDATEVERSION = new Option("UPDATEVERSION",
    Prop.VERSION.replaceAll(" .*", ""));

  /** GUI Layout. */
  public static final Option VIEWS = new Option("VIEWS", GUIConstants.VIEWS);

  /** GUI height. */
  public static final Option GUISIZE = new Option("GUISIZE", new int[] { 1004, 748 });
  /** GUI position. */
  public static final Option GUILOC = new Option("GUILOC", new int[] { 10, 10 });
  /** Flag for maximized GUI window. */
  public static final Option MAXSTATE = new Option("MAXSTATE", false);

  /** Flag for displaying buttons in the GUI window. */
  public static final Option SHOWBUTTONS = new Option("SHOWBUTTONS", true);
  /** Flag for displaying the text field in the GUI window. */
  public static final Option SHOWINPUT = new Option("SHOWINPUT", true);
  /** Flag for displaying the status bar in the GUI window. */
  public static final Option SHOWSTATUS = new Option("SHOWSTATUS", true);

  /** Flag for activated info view. */
  public static final Option SHOWINFO = new Option("SHOWINFO", true);
  /** Flag for activated map view. */
  public static final Option SHOWMAP = new Option("SHOWMAP", true);
  /** Flag for activated table view. */
  public static final Option SHOWTABLE = new Option("SHOWTABLE", false);
  /** Flag for activated result view. */
  public static final Option SHOWTEXT = new Option("SHOWTEXT", true);
  /** Flag for activated tree view. */
  public static final Option SHOWFOLDER = new Option("SHOWFOLDER", false);
  /** Flag for activated query view. */
  public static final Option SHOWEXPLORE = new Option("SHOWEXPLORE", false);
  /** Flag for activated plot view. */
  public static final Option SHOWPLOT = new Option("SHOWPLOT", false);
  /** Flag for activated xquery view. */
  public static final Option SHOWEDITOR = new Option("SHOWEDITOR", true);
  /** Flag for activated tree view. */
  public static final Option SHOWTREE = new Option("SHOWTREE", false);

  /** Dialog location. */
  public static final Option MAPLAYOUTLOC = new Option("MAPLAYOUTLOC", new int[] { 790, 520 });
  /** Dialog location. */
  public static final Option FONTSLOC = new Option("FONTSLOC", new int[] { 10, 530 });
  /** Dialog location. */
  public static final Option COLORSLOC = new Option("COLORSLOC", new int[] { 530, 620 });

  /** Flag for Java look and feel. */
  public static final Option JAVALOOK = new Option("JAVALOOK", false);
  /** Flag for dissolving name attributes. */
  public static final Option SHOWNAME = new Option("SHOWNAME", true);
  /** Focus follows mouse. */
  public static final Option MOUSEFOCUS = new Option("MOUSEFOCUS", false);
  /** Flag for showing the simple file dialog. */
  public static final Option SIMPLEFD = new Option("SIMPLEFD", false);

  /** Current input mode in global text field (Search, XQuery, Command). */
  public static final Option SEARCHMODE = new Option("SEARCHMODE", 0);
  /** Flag for realtime context filtering. */
  public static final Option FILTERRT = new Option("FILTERRT", false);
  /** Flag for realtime query execution. */
  public static final Option EXECRT = new Option("EXECRT", false);

  /** Name of new database. */
  public static final Option DBNAME = new Option("CREATENAME", "");
  /** Last insertion type. */
  public static final Option LASTINSERT = new Option("LASTINSERT", 1);

  /** Comment: written to options file. */
  public static final Option C_SERVER = new Option("Server Dialog");

  /** Server: host, used for connecting new clients. */
  public static final Option S_HOST = new Option("S_HOST", Text.LOCALHOST);
  /** Server: port, used for connecting new clients. */
  public static final Option S_PORT = new Option("S_PORT", 1984);
  /** Server: port, used for binding the server. */
  public static final Option S_SERVERPORT = new Option("S_SERVERPORT", 1984);
  /** Server: port, used for sending events. */
  public static final Option S_EVENTPORT = new Option("S_EVENTPORT", 1985);
  /** Default user. */
  public static final Option S_USER = new Option("S_USER", "");
  /** Default password. */
  public static final Option S_PASSWORD = new Option("S_PASSWORD", "");

  /** Comment: written to options file. */
  public static final Option C_VISUALIZATIONS = new Option("Visualizations");

  /** Show attributes in treemap. */
  public static final Option MAPATTS = new Option("MAPATTS", false);
  /** Treemap Offsets. */
  public static final Option MAPOFFSETS = new Option("MAPOFFSETS", 3);
  /** Map algorithm. */
  public static final Option MAPALGO = new Option("MAPALGO", 0);
  /** number of children <-> size weight in (0;100). */
  public static final Option MAPWEIGHT = new Option("MAPWEIGHT", 0);

  /** Slim rectangles to text length. */
  public static final Option TREESLIMS = new Option("TREESLIM", true);
  /** Show attributes in treeview. */
  public static final Option TREEATTS = new Option("TREEATTS", false);

  /** Dot sizes in plot. */
  public static final Option PLOTDOTS = new Option("PLOTDOTS", 0);
  /** Logarithmic plot. */
  public static final Option PLOTXLOG = new Option("PLOTXLOG", false);
  /** Logarithmic plot. */
  public static final Option PLOTYLOG = new Option("PLOTYLOG", false);

  /** Maximum text size to be displayed. */
  public static final Option MAXTEXT = new Option("MAXTEXT", 1 << 21);
  /** Maximum number of hits to be displayed (-1: return all hits; default: 250K). */
  public static final Option MAXHITS = new Option("MAXHITS", 250000);

  /** Comment: written to options file. */
  public static final Option C_SEARCH = new Option("Search");

  /** Search text. */
  public static final Option SR_SEARCH = new Option("SR_SEARCH", "");
  /** Replace text. */
  public static final Option SR_REPLACE = new Option("SR_REPLACE", "");
  /** Regular expressions. */
  public static final Option SR_REGEX = new Option("SR_REGEX", false);
  /** Match case. */
  public static final Option SR_CASE = new Option("SR_CASE", false);
  /** Whole word. */
  public static final Option SR_WORD = new Option("SR_WORD", false);
  /** Multi-line mode. */
  public static final Option SR_MULTI = new Option("SR_MULTI", false);
  /** Last searched strings. */
  public static final Option SEARCHED = new Option("SEARCHED", new String[0]);
  /** Last replaced strings. */
  public static final Option REPLACED = new Option("REPLACED", new String[0]);

  /** Comment: written to options file. */
  public static final Option C_HISTORY = new Option("History");

  /** Last command inputs. */
  public static final Option COMMANDS = new Option("COMMANDS", new String[0]);
  /** Last keyword inputs. */
  public static final Option SEARCH = new Option("SEARCH", new String[0]);
  /** Last XQuery inputs. */
  public static final Option XQUERY = new Option("XQUERY", new String[0]);

  /**
   * Constructor.
   */
  public GUIOptions() {
    super("gui");
    // reset realtime operations
    set(GUIOptions.FILTERRT, false);
    set(GUIOptions.EXECRT, false);
    Prop.gui = true;
  }
}
