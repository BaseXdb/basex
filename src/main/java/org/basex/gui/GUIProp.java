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
 * @author BaseX Team 2005-11, ISC License
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
  public static final Object[] COLORBLUE = { "COLORBLUE", 7 };

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
  public static final Object[] VIEWS = { "VIEWS", GUIConstants.VIEWS };

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
  public static final Object[] SHOWXQUERY = { "SHOWXQUERY", true };
  /** Flag for activated tree view. */
  public static final Object[] SHOWTREE = { "SHOWTREE", false };

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
  /** Flag for realtime context filtering. */
  public static final Object[] FILTERRT = { "FILTERRT", false };
  /** Flag for realtime query execution. */
  public static final Object[] EXECRT = { "EXECRT", false };

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

  /** Path for creating new databases. */
  public static final Object[] CREATEPATH = { "CREATEPATH", Prop.HOME };
  /** Path for creating new XML Documents. */
  public static final Object[] SAVEPATH = { "SAVEPATH", Prop.HOME };
  /** Path for XQuery files. */
  public static final Object[] XQPATH = { "XQPATH", Prop.HOME };
  /** Path for stopwords. */
  public static final Object[] STOPPATH = { "STOPPATH", Prop.HOME };
  /** Name of new database. */
  public static final Object[] CREATENAME = { "CREATENAME", "" };

  /** Path to mapped file hierarchy. */
  public static final Object[] FSPATH = { "FSPATH", Prop.HOME };
  /** Name of the DeepFS database. */
  public static final Object[] FSNAME = { "FSNAME", "DeepFS" };
  /** Name of the mountpoint. */
  public static final Object[] FSMOUNT = { "FSMOUNT", "/mnt/deepfs" };
  /** Flag indicating the mapping of the complete disk. */
  public static final Object[] FSALL = { "FSALL", false };

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

  /** Server user. */
  public static final Object[] SERVERUSER = { "SERVERUSER", "admin" };

  /** Shows real file contents in the treemap. */
  public static final Object[] MAPFS = { "MAPFS", true };

  /**
   * Constructor.
   */
  public GUIProp() {
    super("gui");
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
      set(XQPATH, file.dir());
      sl.add(path);
    }
    final String[] qu = strings(QUERIES);
    for(int q = 0; q < qu.length && q < 9; ++q) {
      final String f = qu[q];
      if(!f.equalsIgnoreCase(path) && IO.get(f).exists()) sl.add(f);
    }
    set(QUERIES, sl.toArray());
  }
}
