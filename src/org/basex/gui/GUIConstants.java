package org.basex.gui;

import static org.basex.gui.GUICommands.*;
import static org.basex.Text.*;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.UIManager;
import org.basex.Text;
import org.basex.io.IO;

/**
 * GUI Constants used in different views.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class GUIConstants {
  /* NAME OF VIEWS ============================================================
   *
   * All views have unique names, which are defined below.
   * The following steps are necessary to add a new view
   * (the implementation of the existing views might help you):
   *
   * - define a unique name for your view in lower case (e.g. "flup")
   * - add a string for your view, as shown below
   * - add the string in the LAYOUTOPEN string below
   * - add a new ViewPanel instance for your view in the GUI class
   *
   * Add some more code to allow switching on/off your view:
   *
   * - add a boolean visibility flag with the view name included
   *   in the GUIProp class (e.g. "showflup")
   * - add a corresponding command in the GUICommands class and in
   *   MENUITEMS below
   */
  
  /** Internal name of the Map View. */
  public static final String MAPVIEW = "map";
  /** Internal name of the Tree View. */
  public static final String FOLDERVIEW = "folder";
  /** Internal name of the Text View. */
  public static final String TEXTVIEW = "text";
  /** Internal name of the Table View. */
  public static final String TABLEVIEW = "table";
  /** Internal name of the Info View. */
  public static final String INFOVIEW = "info";
  /** Internal name of the Search View. */
  public static final String QUERYVIEW = "query";
  /** Internal name of the Help View. */
  public static final String HELPVIEW = "help";
  /** Internal name of the Plot View. */
  public static final String PLOTVIEW = "plot";
  
   /**
   * Default GUI Layout. The layout is formatted as follows:
   * The character 'H' or 'V' adds a new horizontal or vertical level,
   * and a level is closed again with the '-' character. All views are
   * separated with spaces, and all views must be specified in this layout.
   * This layout is displayed as soon as a database is opened.
   */
  public static final String LAYOUTOPEN = "H V " + QUERYVIEW + " " +
    TEXTVIEW + " - V " + MAPVIEW + " " + TABLEVIEW + " " +
    INFOVIEW + " " + PLOTVIEW + " " + FOLDERVIEW + " - -";

  /** This layout is shown when no database is opened. */
  public static final String LAYOUTCLOSE = "V " + TEXTVIEW + " -";

  // TOOLBAR ==================================================================

  /** Toolbar entries, containing the button commands. */
  public static final GUICommand[] TOOLBAR = {
    GOBACK, GOUP, GOFORWARD, ROOT, null, CREATE, OPEN, null,
      SHOWSEARCH, SHOWINFO, null, SHOWTEXT, SHOWMAP, SHOWFOLDER,
      SHOWTABLE, SHOWPLOT, null, SHOWHELP, INFO
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
    SHOWSEARCH, SHOWINFO, null, COPYPATH, FILTER
  }, {
    MENUMAIN, SHOWMENU, SHOWBUTTONS, SHOWINPUT, SHOWSTATUS, null,
    MENUVIEWS, SHOWTEXT, SHOWMAP, SHOWFOLDER, SHOWTABLE, SHOWPLOT, null, FULL
  }, {
    MENUINTER, RTEXEC, RTFILTER, null,
    MENULAYOUT, COLOR, FONTS, MAPLAYOUT,
    null, PREFS
  }, {
    SHOWHELP, INFO, null, ABOUT
  }};

  /** Context menu entries. */
  public static final GUICommand[] POPUP = {
    GOBACK, FILTER, null, COPY, PASTE, DELETE, INSERT, EDIT, null, COPYPATH
  };

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
  public static enum Fill {
    /** Opaque fill mode.  */ PLAIN,
    /** Transparent mode.  */ NONE,
    /** Upward gradient.   */ UP,
    /** Downward gradient. */ DOWN
  };

  /** Anti-alias modes. */
  public static final String[] FONTALIAS = {
    "OFF", "ON", "GASP", "LCD_HRGB", "LCD_HBGR", "LCD_VRGB", "LCD_VBGR"
  };

  // COLORS ===================================================================

  /** Error color. */
  public static final Color COLORERROR = new Color(208, 0, 0);
  /** Error highlight color. */
  public static final Color COLORERRHIGH = new Color(255, 200, 180);

  /** Cell color. */
  public static final Color COLORCELL = new Color(224, 224, 224);
  /** Button color. */
  public static final Color COLORBUTTON = new Color(160, 160, 160);
  /** Background color. */
  public static final Color COLORDARK = new Color(64, 64, 64);

  /** Fulltext color. */
  public static final Color COLORFT = new Color(0, 224, 0);
  
  /* Colors of full-text hits.
  private static final Color[] COLORFT = new Color[] {
    new Color(224, 64, 64), new Color(0, 224, 0), new Color(255, 128, 0), 
    new Color(224, 0, 224), new Color(0, 192, 192), new Color(96, 0, 224), 
    new Color(64, 64, 255), new Color(224, 0, 96), new Color(128, 128, 128),
    new Color(240, 240, 0)
  };

  /* Colors of full-text hits.
  private static final Color[] COLORFT = new Color[] {
    new Color(0, 0, 0), new Color(255, 0, 0), new Color(0, 255, 0), 
    new Color(0, 0, 255), new Color(255, 255, 0), new Color(255, 0, 255), 
    new Color(0, 255, 255), new Color(192, 192, 192)
  };*/
  
  /** Transparent background color. */
  public static Color back;
  /** Transparent frame color. */
  public static Color frame;

  /** GUI color. */
  public static Color color;
  /** Bright GUI color. */
  public static Color color1;
  /** Second bright GUI color. */
  public static Color color2;
  /** Middle color. */
  public static Color color3;
  /** Middle color. */
  public static Color color4;
  /** Dark color. */
  public static Color color6;
  
  /** Mark color, custom alpha value. */
  public static Color colormarkA;
  /** Second mark color, custom alpha value. */
  public static Color colormark2A;
  /** Mark color. */
  public static Color colormark1;
  /** Second mark color. */
  public static Color colormark2;
  /** Third mark color. */
  public static Color colormark3;
  /** Fourth mark color. */
  public static Color colormark4;
  
  /** Cached treemap colors. */
  public static final Color[] COLORS = new Color[IO.MAXHEIGHT];

  // FONTS ====================================================================

  /** Default monospace font. */
  public static Font dfont;
  /** Default monospace font widths. */
  public static int[] dwidth;
  
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
    color = new Color(col(r, 110), col(g, 150), col(b, 160), 100);
    color1 = new Color(col(r, 8), col(g, 7), col(b, 6));
    color2 = new Color(col(r, 24), col(g, 25), col(b, 40));
    color3 = new Color(col(r, 32), col(g, 32), col(b, 44));
    color4 = new Color(col(r, 48), col(g, 50), col(b, 40));
    color6 = new Color(col(r, 140), col(g, 100), col(b, 70));
    
    colormarkA = new Color(col(r, 32), col(g, 160), col(b, 320), 100);
    colormark2A = new Color(col(r, 16), col(g, 80), col(b, 160), 100);
    colormark1 = new Color(col(r, 16), col(g, 120), col(b, 240));
    colormark2 = new Color(col(r, 16), col(g, 80), col(b, 160));
    colormark3 = new Color(col(r, 32), col(g, 160), col(b, 320));
    colormark4 = new Color(col(r, 1), col(g, 40), col(b, 80));

    // create color array
    for(int l = 1; l < 257; l++) {
      COLORS[l - 1] = new Color(
        Math.max(255 - l * GUIProp.colorred, 0),
        Math.max(255 - l * GUIProp.colorgreen, 0),
        Math.max(255 - l * GUIProp.colorblue, 0));
    }
    final Color c = COLORS[16];
    back = new Color(c.getRed(), c.getGreen(), c.getBlue(),  40);
    frame = new Color(c.getRed(), c.getGreen(), c.getBlue(),  100);
    
    initFonts();
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
    if(dfont == null) {
      dfont = new Font(GUIProp.monofont, 0,
          UIManager.getFont("TextArea.font").getSize() - 1);
      dwidth  = new Container().getFontMetrics(dfont).getWidths();
    }
    if(f == font) return fwidth;
    if(f == mfont) return mfwidth;
    if(f == bfont) return bwidth;
    if(f == lfont) return lwidth;
    if(f == dfont) return dwidth;
    return new Container().getFontMetrics(f).getWidths();
  }
}
