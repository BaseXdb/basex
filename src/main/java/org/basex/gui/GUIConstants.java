package org.basex.gui;

import static org.basex.gui.GUICommand.*;
import static org.basex.gui.GUICommands.*;

import java.awt.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.*;
import org.basex.gui.view.map.*;

/**
 * GUI Constants used in different views.
 *
 * To add a new view, please proceed as follows:<br/>
 * <br/>
 * All views have unique names, which are defined below.
 * The following steps are necessary to add a new view
 * (the implementation of the existing views might help you):
 *
 * <ul>
 *  <li> define a unique name for your view (e.g. {@code map})</li>
 *  <li> add a string for your view, as shown below</li>
 *  <li> add the string in the {@link #VIEWS} string below</li>
 *  <li> create your view implementation in a new sub package
 *    (e.g. {@link MapView}).
 *  <li> add a new {@link View} instance in the {@link GUI} constructor.</li>
 * </ul>
 *
 * Add some more code to allow switching on/off your view:
 *
 * <ul>
 *  <li> add a boolean visibility flag with the view name included
 *    in the {@link GUIProp} class {@link GUIProp#SHOWMAP})</li>
 *  <li> add strings for the menu text and command description in the
 *    {@link Text} class (e.g. {@link Text#MAP} an
 *    {@link Text#H_MAP}).
 *  <li> optionally add localized translations in the .lang files
 *    (e.g. {@code c_showmap} and {@code c_showmaptt})
 *  <li> add a corresponding command in the {@link GUICommands} class
 *   (e.g. {@link GUICommands#C_SHOWMAP})and add a reference in the
 *   {@link #MENUITEMS} menu structure</li>
 * </ul>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class GUIConstants {
  // VIEW NAMES ===============================================================

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
  /** Internal name of the Explore View. */
  public static final String EXPLOREVIEW = "explore";
  /** Internal name of the Plot View. */
  public static final String PLOTVIEW = "plot";
  /** Internal name of the Tree View. */
  public static final String TREEVIEW = "tree";
  /** Internal name of the Editor View. */
  public static final String EDITORVIEW = "editor";

  /**
   * Default GUI Layout. The layout is formatted as follows:
   * The character 'H' or 'V' adds a new horizontal or vertical level,
   * and a level is closed again with the '-' character. All views are
   * separated with spaces, and all views must be specified in this layout.
   * This layout is displayed as soon as a database is opened.
   */
  public static final String VIEWS = "V H " + EDITORVIEW + ' ' + FOLDERVIEW +
    ' ' + MAPVIEW + ' ' + PLOTVIEW + ' ' + " - H " + TEXTVIEW + ' ' + INFOVIEW +
    ' ' + TABLEVIEW + ' ' + TREEVIEW + ' ' + EXPLOREVIEW + " - -";

  // TOOLBAR ==================================================================

  /** Toolbar entries, containing the button commands. */
  static final GUICommands[] TOOLBAR = {
    C_CREATE, C_OPEN_MANAGE, C_INFO, C_CLOSE, null,
    C_GOHOME, C_GOBACK, C_GOUP, C_GOFORWARD, null,
    C_SHOWEDITOR, C_SHOWINFO, null, C_SHOWTEXT, C_SHOWMAP, C_SHOWTREE,
    C_SHOWFOLDER, C_SHOWPLOT, C_SHOWTABLE, C_SHOWEXPLORE
  };

  // MENUBARS =================================================================

  /** Top menu entries. */
  static final String[] MENUBAR = {
    Text.DATABASE, Text.EDITOR, Text.VIEW, Text.NODES, Text.OPTIONS, Text.HELP
  };

  /**
   * Two-dimensional menu entries, containing the menu item commands.
   * {@link #EMPTY} references serve as menu separators.
   */
  static final GUICommand[][] MENUITEMS = { {
    C_CREATE, C_OPEN_MANAGE, EMPTY,
    C_INFO, C_EXPORT, C_CLOSE, EMPTY,
    C_SERVER, Prop.MAC ? null : EMPTY,
    Prop.MAC ? null : C_EXIT
  }, {
    C_EDITNEW, C_EDITOPEN, C_EDITSAVE, C_EDITSAVEAS, C_EDITCLOSE,
    EMPTY, C_SHOWEDITOR, C_SHOWINFO
  }, {
    C_SHOWBUTTONS, C_SHOWINPUT, C_SHOWSTATUS, EMPTY,
    C_SHOWTEXT, C_SHOWMAP, C_SHOWTREE, C_SHOWFOLDER, C_SHOWPLOT, C_SHOWTABLE,
    C_SHOWEXPLORE, EMPTY, C_FULL
  }, {
    C_COPY, C_PASTE, C_DELETE, C_INSERT, C_EDIT, EMPTY,
    C_COPYPATH, C_FILTER
  }, {
    C_RTEXEC, C_RTFILTER, EMPTY,
    C_COLOR, C_FONTS, C_MAPLAYOUT, C_TREEOPTIONS, Prop.MAC ? null : EMPTY,
    C_PACKAGES, Prop.MAC ? null : C_PREFS
  }, {
    C_HELP, Prop.MAC ? null : EMPTY,
    C_COMMUNITY, C_UPDATES, Prop.MAC ? null : EMPTY,
    Prop.MAC ? null : C_ABOUT
  }};

  /** Context menu entries. */
  public static final GUICommands[] POPUP = {
    C_GOBACK, C_FILTER, null, C_COPY, C_PASTE, C_DELETE, C_INSERT, C_EDIT, null,
    C_COPYPATH
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
  /** Move cursor. */
  public static final Cursor CURSORMOVE = new Cursor(Cursor.MOVE_CURSOR);

  /** Icon type. */
  public enum Msg {
    /** Warning message. */
    WARN("warn", "warning"),
    /** Error message. */
    ERROR("error", "error"),
    /** Success message. */
    SUCCESS("ok", "information"),
    /** Question message. */
    QUESTION("warn", "question"),
    /** Yes/no/cancel message. */
    YESNOCANCEL("warn", "question");

    /** Small icon. */
    public final Icon small;
    /** Large icon. */
    public final Icon large;

    /**
     * Constructor.
     * @param s small icon
     * @param l large icon
     */
    Msg(final String s, final String l) {
      small = BaseXLayout.icon(s);
      large = UIManager.getIcon("OptionPane." + l + "Icon");
    }
  }

  /** Background fill options. */
  public enum Fill {
    /** Opaque fill mode.  */ PLAIN,
    /** Transparent mode.  */ NONE,
    /** Downward gradient. */ GRADIENT
  }

  // COLORS ===================================================================

  /** Cell color. */
  public static final Color LGRAY = new Color(224, 224, 224);
  /** Button color. */
  public static final Color GRAY = new Color(160, 160, 160);
  /** Background color. */
  public static final Color DGRAY = new Color(64, 64, 64);

  /** Bright GUI color. */
  public static final Color WHITE = Color.white;
  /** Color for control characters. */
  public static final Color RED = new Color(208, 0, 0);
  /** Color for highlighting errors. */
  public static final Color LRED = new Color(255, 208, 200);
  /** Color for highlighting full-text hits. */
  public static final Color GREEN = new Color(0, 176, 0);
  /** Color for highlighting quotes. */
  public static final Color BLUE = new Color(0, 64, 192);
  /** Color for control characters. */
  public static final Color PINK = new Color(160, 0, 160);

  /** Second bright GUI color. */
  public static Color color1;
  /** Middle color. */
  public static Color color2;
  /** Middle color. */
  public static Color color3;
  /** Dark color. */
  public static Color color4;

  /** Mark color. */
  public static Color colormark1;
  /** Second mark color. */
  public static Color colormark2;
  /** Third mark color. */
  public static Color colormark3;
  /** Fourth mark color. */
  public static Color colormark4;

  /** Alpha color. */
  public static Color color1A;
  /** Transparent background color. */
  public static Color color2A;
  /** Transparent frame color. */
  public static Color color3A;
  /** Mark color, custom alpha value. */
  public static Color colormark1A;
  /** Second mark color, custom alpha value. */
  public static Color colormark2A;

  /** Cached color gradient. */
  private static final Color[] COLORS = new Color[100];

  // FONTS ====================================================================

  /** Default monospace font. */
  public static Font dfont;
  /** Large font. */
  public static Font lfont;
  /** Font. */
  public static Font font;
  /** Bold Font. */
  public static Font bfont;
  /** Monospace font. */
  public static Font mfont;

  /** Default monospace font widths. */
  private static int[] dwidth;
  /** Character large character widths. */
  private static int[] lwidth;
  /** Character widths. */
  private static int[] fwidth;
  /** Bold character widths. */
  private static int[] bwidth;
  /** Monospace character widths. */
  public static int[] mfwidth;

  // KEYS =====================================================================

  /** Shift key. */
  public static final int SHF = Event.SHIFT_MASK;
  /** Alt key. */
  public static final int ALT = Event.ALT_MASK;
  /** Control key. */
  public static final int CTRL = Event.CTRL_MASK;
  /** Shortcut key (CTRL/META). */
  public static final int SC = Prop.MAC ? Event.META_MASK : Event.CTRL_MASK;

  /** Private constructor, preventing class instantiation. */
  private GUIConstants() { }

  /**
   * Initializes colors.
   * @param prop gui properties
   */
  public static void init(final GUIProp prop) {
    final int r = prop.num(GUIProp.COLORRED);
    final int g = prop.num(GUIProp.COLORGREEN);
    final int b = prop.num(GUIProp.COLORBLUE);

    // calculate color c:
    // c = (255 - expectedColor) * 10 / factor (= GUIRED/BLUE/GREEN)
    color1 = new Color(col(r, 24), col(g, 25), col(b, 40));
    color2 = new Color(col(r, 32), col(g, 32), col(b, 44));
    color3 = new Color(col(r, 48), col(g, 50), col(b, 40));
    color4 = new Color(col(r, 140), col(g, 100), col(b, 70));
    color1A = new Color(col(r, 110), col(g, 150), col(b, 160), 100);

    colormark1A = new Color(col(r, 32), col(g, 160), col(b, 320), 100);
    colormark2A = new Color(col(r, 16), col(g, 80), col(b, 160), 100);
    colormark1 = new Color(col(r, 16), col(g, 120), col(b, 240));
    colormark2 = new Color(col(r, 16), col(g, 80), col(b, 160));
    colormark3 = new Color(col(r, 32), col(g, 160), col(b, 320));
    colormark4 = new Color(col(r, 1), col(g, 40), col(b, 80));

    // create color array
    for(int l = 1; l < COLORS.length + 1; ++l) {
      COLORS[l - 1] = new Color(Math.max(255 - l * r, 0),
        Math.max(255 - l * g, 0), Math.max(255 - l * b, 0));
    }
    final Color c = COLORS[16];
    color2A = new Color(c.getRed(), c.getGreen(), c.getBlue(), 40);
    color3A = new Color(c.getRed(), c.getGreen(), c.getBlue(), 100);

    final String f = prop.get(GUIProp.FONT);
    final int type = prop.num(GUIProp.FONTTYPE);
    final int size = prop.num(GUIProp.FONTSIZE);
    font  = new Font(f, type, size);
    mfont = new Font(prop.get(GUIProp.MONOFONT), type, size);
    bfont = new Font(f, Font.BOLD, size);
    lfont = new Font(f, type, size + 10);
    dfont = new Font(prop.get(GUIProp.MONOFONT), 0,
        UIManager.getFont("TextArea.font").getSize() - 1);

    final Container comp = new Container();
    dwidth  = comp.getFontMetrics(dfont).getWidths();
    fwidth  = comp.getFontMetrics(font).getWidths();
    lwidth  = comp.getFontMetrics(lfont).getWidths();
    mfwidth = comp.getFontMetrics(mfont).getWidths();
    bwidth  = comp.getFontMetrics(bfont).getWidths();
  }

  /**
   * Returns the specified color from the color gradient.
   * @param i color index
   * @return color
   */
  public static Color color(final int i) {
    return COLORS[Math.min(COLORS.length - 1, i)];
  }

  /**
   * Returns the character widths for the current font.
   * @param f font reference
   * @return character widths
   */
  public static int[] fontWidths(final Font f) {
    if(f == font)  return fwidth;
    if(f == mfont) return mfwidth;
    if(f == bfont) return bwidth;
    if(f == lfont) return lwidth;
    if(f == dfont) return dwidth;
    return new Container().getFontMetrics(f).getWidths();
  }

  /**
   * Converts color value with specified factor.
   * @param c color
   * @param f factor
   * @return converted color value
   */
  private static int col(final int c, final int f) {
    return Math.max(0, 255 - c * f / 10);
  }
}
