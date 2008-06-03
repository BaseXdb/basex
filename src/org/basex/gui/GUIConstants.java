package org.basex.gui;

import static org.basex.gui.GUICommands.*;
import static org.basex.Text.*;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import org.basex.Text;

/**
 * GUI Constants used in different views.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class GUIConstants {
  /* NAME OF VIEWS ============================================================
   *
   * All views have unique names, which are defined below.
   * The following steps are necessary to add a new view
   * (the implementation of the existing views might help you):
   *
   * - define a new name for your view (no spaces; e.g. "Flup")
   * - add a string for your view, as shown below
   * - add the string in the LAYOUTOPENED below
   * - add a new ViewPanel instance for your view in the GUI class constructor
   *
   * Add some more code to allow switching on/off your view:
   *
   * - add a boolean visibility flag in the GUIProp class (e.g. "showflup")
   * - add a corresponding line in the ViewPanel.setVisibility() method
   * - add a corresponding command in the GUICommands class and in
   *   MENUITEMS below
   */
  
  /** Internal name of the Map View. */
  public static final String MAPVIEW = "Map";
  /** Internal name of the Tree View. */
  public static final String TREEVIEW = "Tree";
  /** Internal name of the Text View. */
  public static final String TEXTVIEW = "Text";
  /** Internal name of the Table View. */
  public static final String TABLEVIEW = "Table";
  /** Internal name of the Info View. */
  public static final String INFOVIEW = "Query Info";
  /** Internal name of the Search View. */
  public static final String QUERYVIEW = "Query";
  /** Internal name of the Help View. */
  public static final String HELPVIEW = "Help";
  /** Internal name of the Real Tree View. */
  public static final String REALVIEW = "Real";
  /** Internal name of the Real Tree View. */
  public static final String XPATHVIEW = "XPath";
  
   /**
   * Default GUI Layout. The layout is formatted as follows:
   * The character 'H' or 'V' adds a new horizontal or vertical level,
   * and a level is closed again with the '-' character. All views are
   * separated with spaces, and all views must be specified in this layout.
   * This layout is displayed as soon as a database is opened.
   */
  public static final String LAYOUTOPENED = "H V " + QUERYVIEW + " " + TREEVIEW
      + " " + TEXTVIEW + " " + HELPVIEW + " - V " + MAPVIEW + " " + TABLEVIEW
      + " " + INFOVIEW + " " + REALVIEW + " " + XPATHVIEW + " - -";

  /** This layout is shown when no database is opened. */
  public static final String LAYOUTCLOSED = "V " + TEXTVIEW + " " + HELPVIEW
      + " -";

  // TOOLBAR ==================================================================

  /** Toolbar entries, containing the button commands. */
  public static final GUICommand[] TOOLBAR = {
    GOBACK, GOUP, GOFORWARD, ROOT, null, CREATE, OPEN, null,
      SHOWSEARCH, SHOWINFO, null, SHOWTEXT, SHOWMAP, SHOWTREE, SHOWTABLE, null,
      SHOWHELP, INFO
  };

  /** Toolbar entries, containing the xquery button commands. */
  public static final GUICommand[] TOOLXQ = {
    XQOPEN, XQSAVE
  };

  // MENUBARS =================================================================

  /** Top menu entries. */
  public static final String[] MENUBAR = { 
      Text.MENUFILE, Text.MENUEDIT, Text.MENUVIEW, Text.MENUOPTIONS,
      Text.MENUHELP };

  /** Two-dimensional Menu entries, containing the menu item commands. */
  public static final Object[][] MENUITEMS = { {
    MENUDB, CREATE, OPEN, DROP, CLOSE, null,
    MENUXQ, XQOPEN, XQSAVE, null,
    IMPORTFS, EXPORT, null, EXIT
  }, {
    COPY, PASTE, DELETE, INSERT, EDIT, null,
    SHOWSEARCH, SHOWINFO, null, SELECT, FILTER
  }, {
    MENUMAIN, SHOWMENU, SHOWBUTTONS, SHOWINPUT, SHOWSTATUS, null,
    MENUVIEWS, SHOWTEXT, SHOWMAP, SHOWTREE, SHOWTABLE, null, FULL
  }, {
    MENUINTER, INPUTMODE, RTEXEC, RTFILTER, null,
    MENULAYOUT, COLOR, FONTS, MAPLAYOUT, null, PREFS
  }, {
    SHOWHELP, INFO, null, ABOUT
  }};

  /** Context menu entries. */
  public static final GUICommand[] POPUP = {
      GOBACK, null, COPY, PASTE, DELETE, INSERT, EDIT, null, SELECT, FILTER
  };

  // IMAGES ===================================================================

  /** Name of application logo. */
  public static final String IMGICON = "icon";
  /** Name of application logo. */
  public static final String IMGBASEX = "basex";
  /** Name of application logo. */
  public static final String IMGERROR = "error";
  /** Name of application logo. */
  public static final String IMGLOGO = "logo";

  // CURSORS ==================================================================
  
  /** Arrow cursor. */
  public static final Cursor CURSORARROW = new Cursor(Cursor.DEFAULT_CURSOR);
  /** Hand cursor. */
  public static final Cursor CURSORHAND = new Cursor(Cursor.HAND_CURSOR);
  /** Wait cursor. */
  public static final Cursor CURSORWAIT = new Cursor(Cursor.WAIT_CURSOR);
  /** Left/Right arrow cursor. */
  public static final Cursor CURSORMOVEH = new Cursor(Cursor.E_RESIZE_CURSOR);
  /** Move cursor. */
  public static final Cursor CURSORMOVEV = new Cursor(Cursor.N_RESIZE_CURSOR);
  /** Text cursor. */
  public static final Cursor CURSORTEXT = new Cursor(Cursor.TEXT_CURSOR);

  /** Background fill options. */
  public static enum FILL {
    /** Opaque fill mode.  */ PLAIN,
    /** Transparent mode.  */ NONE,
    /** Upward gradient.   */ UP,
    /** Downward gradient. */ DOWN
  };

  // COLORS ===================================================================

  /** Error color. */
  public static final Color COLORERROR = new Color(208, 0, 0);
  /** Error color. */
  public static final Color COLORMARK = new Color(255, 200, 180);
  /** Error color. */
  public static final Color COLORQUOTE = new Color(0, 0, 192);
  /** Button color. */
  public static final Color COLORBUTTON = Color.gray;

  /** Bright GUI color. */
  public static Color color1;
  /** Second bright GUI color. */
  public static Color color2;
  /** Middle color. */
  public static Color color3;
  /** Middle color. */
  public static Color color4;
  /** Middle color. */
  public static Color color5;
  /** Dark color. */
  public static Color color6;

  /** Mark color. */
  public static Color colormark1;
  /** Second mark color. */
  public static Color colormark2;
  /** Third mark color. */
  public static Color colormark3;
  /** Fourth mark color. */
  public static Color colormark4;
  /** Fifth mark color. */
  public static Color colormark5;
  
  /** Thumbnail colors. **/
  public static Color[] thumbnailcolor;

  /** Cached treemap colors. */
  public static final Color[] COLORS = new Color[256];

  // FONTS ====================================================================

  /** Large font. */
  public static Font lfont;
  /** Character large character widths. */
  public static int[] lwidth;
  /** Font. */
  public static Font font;
  /** Character widths. */
  public static int[] fwidth;
  /** Monospace font. */
  public static Font mfont;
  /** Monospace character widths. */
  public static int[] mfwidth;
  /** Bold Font. */
  public static Font bfont;
  /** Bold character widths. */
  public static int[] bwidth;

  /**
   * Preventing class instantiation.
   */
  private GUIConstants() { }

  /**
   * Initializes colors.
   */
  public static void init() {
    final int r = GUIProp.colorred;
    final int g = GUIProp.colorgreen;
    final int b = GUIProp.colorblue;

    // calculate color c:
    // c = (255 - expectedColor) * 10 / factor (= GUIRED/BLUE/GUIProps.GREEN)
    color1 = new Color(col(r,   8), col(g,   7), col(b,   6));
    color2 = new Color(col(r,  24), col(g,  25), col(b,  40));
    color3 = new Color(col(r,  32), col(g,  32), col(b,  44));
    color4 = new Color(col(r,  48), col(g,  50), col(b,  40));
    color5 = new Color(col(r,  56), col(g,  60), col(b,  80));
    color6 = new Color(col(r, 140), col(g, 140), col(b, 140));
    colormark1 = new Color(col(r, 16), col(g, 120), col(b, 240));
    colormark2 = new Color(col(r, 16), col(g,  80), col(b, 160));
    colormark3 = new Color(col(r, 32), col(g, 160), col(b, 320));
    colormark4 = new Color(col(r,  1), col(g,  40), col(b,  80));
    colormark5 = new Color(col(r, 20), col(g,  48), col(b,  62));

    // create color array
    for(int l = 1; l < 257; l++) {
      COLORS[l - 1] = new Color(
        Math.max(255 - l * GUIProp.colorred, 0),
        Math.max(255 - l * GUIProp.colorgreen, 0),
        Math.max(255 - l * GUIProp.colorblue, 0));
    }
    
    // create thumbnail colors
    thumbnailcolor = new Color[] {
        new Color(224, 64, 64), new Color(0, 224, 0), new Color(255, 128, 0), 
        new Color(224, 0, 224), new Color(0, 192, 192), new Color(96, 0, 224), 
        new Color(64, 64, 255), new Color(224, 0, 96), new Color(128, 128, 128),
        new Color(240, 240, 0)
    };
  }

  /**
   * Converts color value with specified factor.
   * @param c color
   * @param f factor
   * @return converted color value
   */
  private static int col(final int c, final int f) {
    return Math.max(0, 255 - (c * f / 10));
  }

  /**
   * Initializes fonts.
   */
  public static void initFonts() {
    final Container comp = new Container();
    font  = new Font(GUIProp.font, GUIProp.fonttype, GUIProp.fontsize);
    mfont = new Font(GUIProp.monofont, GUIProp.fonttype, GUIProp.fontsize);
    bfont = new Font(GUIProp.font, Font.BOLD, GUIProp.fontsize);
    lfont = new Font(GUIProp.font, GUIProp.fonttype, GUIProp.fontsize + 8);
    fwidth  = comp.getFontMetrics(font).getWidths();
    lwidth  = comp.getFontMetrics(lfont).getWidths();
    mfwidth = comp.getFontMetrics(mfont).getWidths();
    bwidth  = comp.getFontMetrics(bfont).getWidths();
  }

  /**
   * Returns the character widths for the current font.
   * @param f font reference
   * @return character widths
   */
  public static int[] fontWidths(final Font f) {
    if(f == font) return fwidth;
    if(f == mfont) return mfwidth;
    if(f == bfont) return bwidth;
    if(f == lfont) return lwidth;
    return new Container().getFontMetrics(f).getWidths();
  }
}
