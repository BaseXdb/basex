package org.basex.gui;

import java.awt.Font;

import org.basex.core.AProp;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.util.StringList;

/**
 * This class contains properties which are used in the GUI. They are
 * initially read from and finally written to disk, except for the properties
 * following the {@link #SKIP} flag.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class GUIProp extends AProp {
  // The following properties will be saved to disk:

  // DATABASE & PROGRAM PATHS =================================================

  /** Default GUI Font. */
  public static final Object[] FONT = { "FONT", Font.SANS_SERIF };
  /** Default GUI Monospace Font. */
  public static final Object[] MONOFONT = { "MONOFONT", Font.MONOSPACED };
  /** Font TYPE = plain, bold, italics). */
  public static final Object[] FONTTYPE = { "FONTTYPE", 0 };
  /** Font size. */
  public static final Object[] FONTSIZE = { "FONTSIZE", 12 };

  /** Red GUI color factor. */
  public static final Object[] COLORRED = { "COLORRED", 18 };
  /** Green GUI color factor. */
  public static final Object[] COLORGREEN = { "COLORGREEN", 16 };
  /** Blue GUI color factor. */
  public static final Object[] COLORBLUE = { "COLORBLUE", 8 };

  // MAIN WINDOW OPTIONS ======================================================

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

  /** GUI Layout. */
  public static final Object[] LAYOUTCLOSED =
    { "LAYOUTCLOSED", GUIConstants.LAYOUTCLOSE };
  /** GUI Layout. */
  public static final Object[] LAYOUTOPENED =
    { "LAYOUTOPENED", GUIConstants.LAYOUTOPEN };

  /** Flag for activated info view. */
  public static final Object[] SHOWINFO = { "SHOWINFO", false };
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
  public static final Object[] SHOWXQUERY = { "SHOWXQUERY", true };

  /** Flag for activated text view after starting. */
  public static final Object[] SHOWSTARTTEXT = { "SHOWSTARTTEXT", false };
  /** Flag for activated xquery view after starting. */
  public static final Object[] SHOWSTARTXQUERY = { "SHOWSTARTXQUERY", false };
  /** Flag for activated info view after starting. */
  public static final Object[] SHOWSTARTINFO = { "SHOWSTARTINFO", false };

  /** Flag for Java look and feel. */
  public static final Object[] JAVALOOK = { "JAVALOOK", false };
  /** Flag for dissolving name attributes. */
  public static final Object[] SHOWNAME = { "SHOWNAME", true };
  /** Focus follows mouse. */
  public static final Object[] MOUSEFOCUS = { "MOUSEFOCUS", false };
  /** Flag for showing the simple file dialog. */
  public static final Object[] SIMPLEFD = { "SIMPLEFD", false };

  // LAYOUT & INPUT OPTIONS ===================================================

  /** Current input mode in global text field (Search, XQuery, Command). */
  public static final Object[] SEARCHMODE = { "SEARCHMODE", 0 };
  /** Flag for realtime context switch. */
  public static final Object[] FILTERRT = { "FILTERRT", false };
  /** Flag for realtime context switch. */
  public static final Object[] EXECRT = { "EXECRT", true };

  /** Show attributes in treemap. */
  public static final Object[] MAPATTS = { "MAPATTS", false };
  /** Treemap Offsets. */
  public static final Object[] MAPOFFSETS = { "MAPOFFSETS", 3 };
  /** Map algorithm. */
  public static final Object[] MAPALGO = { "MAPALGO", 0 };
  /** number of children <-> size weight in (0;100). */
  public static final Object[] MAPWEIGHT = { "MAPWEIGHT", 0 };

  /** Dot sizes in plot. */
  public static final Object[] PLOTDOTS = { "PLOTDOTS", 0 };
  /** Logarithmic plot. */
  public static final Object[] PLOTXLOG = { "PLOTXLOG", false };
  /** Logarithmic plot. */
  public static final Object[] PLOTYLOG = { "PLOTYLOG", false };

  /** Dialog location. */
  public static final Object[] MAPLAYOUTLOC =
    { "MAPLAYOUTLOC", new int[] { 790, 520 } };
  /** Dialog location. */
  public static final Object[] FONTSLOC =
    { "FONTSLOC", new int[] { 10, 530 } };
  /** Dialog location. */
  public static final Object[] COLORSLOC =
    { "COLORSLOC", new int[] { 530, 620 } };
  /** Dialog location. */
  public static final Object[] HELPLOC =
    { "HELPLOC", new int[] { 690, 484 } };
  /** Dialog size. */
  public static final Object[] HELPSIZE =
    { "HELPSIZE", new int[] { 300, 250 } };

  /** Path for creating new XML Documents. */
  public static final Object[] CREATEPATH = { "CREATEPATH", Prop.WORK };
  /** Path for XQuery files. */
  public static final Object[] XQPATH = { "XQPATH", Prop.WORK };

  /** Path to mapped file hierarchy. */
  public static final Object[] FSBACKING = { "FSBACKING", Prop.HOME };
  /** Name of the DeepFS database. */
  public static final Object[] FSDBNAME = { "FSDBNAME", "DeepFS_Database" };
  /** Name of the mountpoint. */
  public static final Object[] FSMOUNT = { "FSMOUNT", "/mnt/deepfs" };
  /** Flag indicating the mapping of the complete disk. */
  public static final Object[] FSALL = { "FSALL", false };
  /** Flag write through in Desktop Query Engine. */
  public static final Object[] FSWTHROUGH = { "FSWTHROUGH", false };

  /** Last command inputs. */
  public static final Object[] COMMANDS = { "COMMANDS", new String[0] };
  /** Last keyword inputs. */
  public static final Object[] SEARCH = { "SEARCH", new String[0] };
  /** Last XQuery inputs. */
  public static final Object[] XQUERY = { "XQUERY", new String[0] };
  /** Last XQuery files. */
  public static final Object[] QUERIES = { "QUERIES", new String[0] };

  // CONFIG OPTIONS ===========================================================

  /** The following options are not saved to disk; don't remove this flag. */
  public static final Object[] SKIP = { "SKIP", true };

  /** Flag for displaying the menu in the GUI window. */
  public static final Object[] SHOWMENU = { "SHOWMENU", true };
  /** Flag for activated help view. */
  public static final Object[] SHOWHELP = { "SHOWHELP", false };
  /** Flag for activated tree view. */
  public static final Object[] SHOWTREE = { "SHOWTREE", false };

  /** Server user. */
  public static final Object[] SERVERUSER = { "SERVERUSER", "admin" };
  /** Server password. */
  public static final Object[] SERVERPASS = { "SERVERPASS", "" };

  /** Shows real file contents in the treemap. */
  public static final Object[] MAPFS = { "MAPFS", true };
  /** Choice of interacting with TreeeMap. */
  public static final Object[] MAPINTERACTION = { "MAPINTERACTION", false };
  /** Distort map in mouse context. */
  public static final Object[] MAPDIST = { "MAPDIST", false };
  /** Size of thumb focus in map view fraction of total size. */
  public static final Object[] MAPTHUMBSIZE = { "MAPTHUMBSIZE", 4 };
  /** Scaling to use in the Map One-click-focus. */
  public static final Object[] LENSSCALE = { "LENSSCALE", 2 };
  /** Alpha value of the zoom box. */
  public static final Object[] ZOOMBOXALPHA = { "ZOOMBOXALPHA", 100 };
  /** Width of the fisheye view. */
  public static final Object[] FISHW = { "FISHW", 300 };
  /** Height of the fisheye view. */
  public static final Object[] FISHH = { "FISHH", 200 };
  /** Show file contents in TreeMap. */
  public static final Object[] FILECONT = { "FILECONT", true };

  /**
   * Constructor.
   */
  public GUIProp() {
    super("win");
    files(null);
  }

  /**
   * Refreshes the list of recent query files.
   * @param file new file
   */
  public void files(final IO file) {
    final StringList sl = new StringList();

    String path = null;
    if(file != null) {
      path = file.path();
      set(XQPATH, file.getDir());
      Prop.xquery = file;
      sl.add(path);
    }
    final String[] qu = strings(QUERIES);
    for(int q = 0; q < qu.length && q < 9; q++) {
      final String f = qu[q];
      if(!f.equals(path) && IO.get(f).exists()) sl.add(f);
    }
    set(QUERIES, sl.finish());
  }
}
