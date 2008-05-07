package org.basex.gui;

import org.basex.core.Prop;

/**
 * This class contains properties which are used in the GUI. They are
 * initially read from and finally written to disk, except for the properties
 * following the {@link #SKIP} flag. 
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  /** Flag for anti-aliasing results in GUI mode. */
  public static boolean fontalias = false;

  /** Red GUI color factor. */
  public static int colorred = 20;
  /** Green GUI color factor. */
  public static int colorgreen = 16;
  /** Blue GUI color factor. */
  public static int colorblue = 8;

  // MAIN WINDOW OPTIONS ======================================================

  /** GUI height. */
  public static int[] guisize = { 1000, 744 };
  /** GUI position. */
  public static int[] guiloc = { 10, 10 };
  /** Search GUI position. */
  public static int[] searchloc = { 10, 10 };
  /** Search GUI width. */
  public static int searchwidth = 200;
  /** Flag for maximized GUI window. */
  public static boolean maxstate = false;

  /** Flag for displaying buttons in the GUI window. */
  public static boolean showbuttons = true;
  /** Flag for displaying the text field in the GUI window. */
  public static boolean showinput = true;
  /** Flag for displaying the status bar in the GUI window. */
  public static boolean showstatus = true;

  /** GUI Layout. */
  public static String layoutclosed = GUIConstants.LAYOUTCLOSED;
  /** GUI Layout. */
  public static String layoutopened = GUIConstants.LAYOUTOPENED;

  /** Flag for activated info view. */
  public static boolean showinfo = false;
  /** Flag for activated map view. */
  public static boolean showmap = true;
  /** Flag for activated table view. */
  public static boolean showtable = false;
  /** Flag for activated result view. */
  public static boolean showtext = true;
  /** Flag for activated tree view. */
  public static boolean showtree = false;
  /** Flag for activated search view. */
  public static boolean showquery = false;
  /** Flag for activated help view. */
  public static boolean showhelp = true;
  /** Flag for dissolving name attributes. */
  public static boolean shownames = true;
  /** Flag for showing the simple file dialog. */
  public static boolean simplefd = false;

  // LAYOUT & INPUT OPTIONS ===================================================

  /** Current input mode in global text field (search, XPath, command). */
  public static int searchmode = 0;
  /** Focus follows mouse. */
  public static boolean mousefocus = false;
  /** Flag for realtime context switch. */
  public static boolean filterrt = false;
  /** Flag for realtime context switch. */
  public static boolean execrt = true;

  /** Flag for using a simple map layout. */
  public static boolean mapsimple = false;
  /** Show attributes in treemap. */
  public static boolean mapatts = false;
  /** Treemap Layout. */
  public static int maplayout = 4;
  /** Treemap Proportion. */
  public static int mapprop = 4;
  
  /** Dialog location. */
  public static int[] maplayoutloc = { 100, 100 };
  /** Dialog location. */
   public static int[] fontsloc = { 100, 100 };
   /** Dialog location. */
   public static int[] colorsloc = { 100, 100 };

  /** Path for creating new XML Documents. */
  public static String createpath = Prop.WORK;
  /** Path for importing the file system. */
  public static String fspath = Prop.WORK;
  /** Name of the filesystem database. */
  public static String importfsname = "Filesystem";
  /** Flag for importing complete file system hierarchy. */
  public static boolean fsall = false;

  /** Last entered BaseX commands. */
  public static String[] commands = new String[0];
  /** Last entered BaseX commands. */
  public static String[] search = new String[0];
  /** Last entered XPath queries. */
  public static String[] xpath = new String[0];

  // CONFIG OPTIONS ===========================================================

  /** Following options are not saved/read; don't remove this flag. */
  public static final boolean SKIP = true;

  /** Flag for displaying the menu in the GUI window. */
  public static boolean showmenu = true;
  /** Flag for activated real tree view. */
  public static boolean showreal = false;

  /** Flag for activated result view after starting. */
  public static boolean showstarttext = false;
  /** Flag for activated help view after starting.  */
  public static boolean showstarthelp = false;
  /** Fullscreen flag. */
  static boolean fullscreen = false;
  
  /** Default path to the BaseX configuration file. */
  private static String cfg = Prop.HOME + "/.basexwin";

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
