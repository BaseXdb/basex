package org.basex.gui;

import org.basex.core.Prop;

/**
 * This class contains properties which are used in the GUI. They are
 * initially read from and finally written to disk, except for the properties
 * following the {@link #SKIP} flag.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class GUIProp {
  // GLOBAL WINDOW OPTIONS ====================================================

  /** Default GUI Font. */
  public static String font = "SansSerif";
  /** Default GUI Monospace Font. */
  public static String monofont = "Monospaced";
  /** Font type = plain, bold, italics). */
  public static int fonttype = 0;
  /** Font size. */
  public static int fontsize = 12;
  /** Anti-aliasing mode. */
  public static int fontalias = 0;

  /** Red GUI color factor. */
  public static int colorred = 18;
  /** Green GUI color factor. */
  public static int colorgreen = 16;
  /** Blue GUI color factor. */
  public static int colorblue = 8;

  // MAIN WINDOW OPTIONS ======================================================

  /** GUI height. */
  public static int[] guisize = { 1004, 748 };
  /** GUI position. */
  public static int[] guiloc = { 10, 10 };
  /** Flag for maximized GUI window. */
  public static boolean maxstate = false;

  /** Flag for displaying buttons in the GUI window. */
  public static boolean showbuttons = true;
  /** Flag for displaying the text field in the GUI window. */
  public static boolean showinput = true;
  /** Flag for displaying the status bar in the GUI window. */
  public static boolean showstatus = true;

  /** GUI Layout. */
  public static String layoutclosed = GUIConstants.LAYOUTCLOSE;
  /** GUI Layout. */
  public static String layoutopened = GUIConstants.LAYOUTOPEN;

  /** Flag for activated info view. */
  public static boolean showinfo = false;
  /** Flag for activated map view. */
  public static boolean showmap = true;
  /** Flag for activated table view. */
  public static boolean showtable = false;
  /** Flag for activated result view. */
  public static boolean showtext = true;
  /** Flag for activated tree view. */
  public static boolean showfolder = false;
  /** Flag for activated search view. */
  public static boolean showquery = true;
  /** Flag for activated plot view. */
  public static boolean showplot = false;

  /** Flag for Java look and feel. */
  public static boolean javalook = false;
  /** Flag for dissolving name attributes. */
  public static boolean shownames = true;
  /** Focus follows mouse. */
  public static boolean mousefocus = false;
  /** Flag for showing the simple file dialog. */
  public static boolean simplefd = false;

  // LAYOUT & INPUT OPTIONS ===================================================

  /** Current input mode in global text field (Search, XQuery, Command). */
  public static int searchmode = 0;
  /** Flag for realtime context switch. */
  public static boolean filterrt = false;
  /** Flag for realtime context switch. */
  public static boolean execrt = true;
  /** Maximum text size to be displayed. */
  public static int maxtext = 1 << 21;

  /** Show attributes in treemap. */
  public static boolean mapatts = false;
  /** Treemap Offsets. */
  public static int mapoffsets = 3;
  /** Map algorithm. */
  public static int mapalgo = 0;
  /** Strip direction horizontal? */
  public static boolean striphor = false;
  /** number of child <-> size weighting in (0;100). */
  public static int mapweight = 0;
  /** divide rectangles uniformly on each level. */
  public static boolean mapsimple = false;

  /** Dot sizes in plot. */
  public static int plotdots = 0;
  /** Logarithmic plot. */
  public static boolean plotxlog;
  /** Logarithmic plot. */
  public static boolean plotylog;

  /** Dialog location. */
  public static int[] maplayoutloc = { 790, 520 };
  /** Dialog location. */
  public static int[] fontsloc = { 10, 530 };
  /** Dialog location. */
  public static int[] colorsloc = { 530, 620 };
  /** Dialog location. */
  public static int[] helploc = { 690, 484 };
  /** Dialog size. */
  public static int[] helpsize = { 300, 250 };

  /** Path for creating new XML Documents. */
  public static String createpath = Prop.WORK;
  /** Path for XQuery files. */
  public static String xqpath = Prop.WORK;
  /** Path for importing the file system. */
  public static String guifsimportpath = Prop.WORK;

  /** Name of the filesystem database. */
  public static String guifsdbname = "Filesystem";
  /** Name of the mountpoint. */
  public static String guimountpoint = Prop.TMP + "deepfs";
  /** Name of the backingroot. */
  public static String guibackingroot = Prop.TMP +  "backingstore_deepfs";
  /** Flag for importing complete file system hierarchy. */
  public static boolean fsall = false;

  /** Last command inputs. */
  public static String[] commands = new String[0];
  /** Last keyword inputs. */
  public static String[] search = new String[0];
  /** Last XQuery inputs. */
  public static String[] xquery = new String[0];

  // CONFIG OPTIONS ===========================================================

  /** Following options are not saved/read; don't remove this flag. */
  public static final boolean SKIP = true;

  /** Flag for displaying the menu in the GUI window. */
  public static boolean showmenu = true;
  /** Flag for activated help view. */
  public static boolean showhelp = false;

  /** Flag for activated result view after starting. */
  public static boolean showstarttext = false;
  /** Fullscreen flag. */
  public static boolean fullscreen = false;

  /** Flag for computing additional map infos. */
  public static boolean mapinfo = true;
  /** Flag for skipping tim intensive treemap infos. */
  public static boolean perfinfo = true;
  /** Shows real file contents in the treemap. */
  public static boolean mapfs = true;
  /** Choice of interacting with TreeeMap. */
  public static boolean mapinteraction = false;
  /** Distort map in mouse context. */
  public static boolean mapdist = false;
  /** Size of thumb focus in Mapview fraction of Mapview size. */
  public static int mapthumbsize = 4;
  /** Scaling to use in the Map oneclickfocus. */
  public static int lensscale = 2;
  /** Alpha value of the zoombox. */
  public static int zoomboxalpha = 100;
  /** Width of the fisheyeview. */
  public static int fishw = 300;
  /** Height of the fisheyeview. */
  public static int fishh = 200;
  /** Show file contents in TreeMap. */
  public static boolean filecont = true;

  /** Flag for activated tree view. */
  public static boolean showtree = false;

  /** Default path to the BaseX configuration file. */
  private static String cfg = Prop.HOME + ".basexwin";

  /** Private constructor, preventing class instantiation. */
  private GUIProp() { }

  /**
   * Reads in the GUI configuration file and initializes the properties.
   * The file is located in the user's home directory.
   */
  public static void read() {
    Prop.read(cfg, GUIProp.class.getFields());
  }

  /**
   * Writes the configuration file.
   */
  public static void write() {
    Prop.write(cfg, GUIProp.class.getFields());
  }
}
