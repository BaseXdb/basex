package org.basex.gui;

import static org.basex.gui.GUIMenuCmd.*;

import java.awt.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.*;
import org.basex.gui.view.map.*;
import org.basex.util.*;

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
 *    in the {@link GUIOptions} class {@link GUIOptions#SHOWMAP})</li>
 *  <li> add strings for the menu text.</li>
 *  <li> optionally add localized translations in the .lang files
 *    (e.g. {@code c_showmap} and {@code c_showmaptt})</li>
 *  <li> add a corresponding command in the {@link GUIMenuCmd} class
 *   (e.g. {@link GUIMenuCmd#C_SHOWMAP})and add a reference in the
 *   {@link #MENUITEMS} menu structure</li>
 * </ul>
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class GUIConstants {

  // DUMMY OBJECTS ============================================================

  /** Dummy textfield. */
  public static final JTextField TEXTFIELD = new JTextField();
  /** Dummy label, used for size calculations. */
  public static final JLabel LABEL = new JLabel();

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
  static final GUIMenuCmd[] TOOLBAR = {
    C_CREATE, C_OPEN_MANAGE, C_INFO, C_CLOSE, null,
    C_GOHOME, C_GOBACK, C_GOUP, C_GOFORWARD, null,
    C_SHOWEDITOR, C_SHOWRESULT, C_SHOWINFO, null, C_SHOWMAP, C_SHOWTREE,
    C_SHOWFOLDER, C_SHOWPLOT, C_SHOWTABLE, C_SHOWEXPLORE
  };

  // MENUBARS =================================================================

  /** Top menu entries. */
  static final String[] MENUBAR = {
    Text.DATABASE, Text.EDITOR, Text.VIEW, Text.VISUALIZATION, Text.OPTIONS, Text.HELP
  };

  /**
   * Two-dimensional menu entries, containing the menu item commands.
   * {@link GUIPopupCmd#SEPARATOR} references serve as menu separators.
   */
  static final GUICommand[][] MENUITEMS = { {
    C_CREATE, C_OPEN_MANAGE, SEPARATOR,
    C_INFO, C_EXPORT, C_CLOSE, SEPARATOR,
    Prop.MAC ? null : C_EXIT
  }, {
    C_EDITNEW, C_EDITOPEN, C_EDITREOPEN, C_EDITSAVE, C_EDITSAVEAS, C_EDITCLOSE,
    SEPARATOR, C_FORMAT, C_COMMENT, C_SORT, SEPARATOR, C_NEXTERROR, C_JUMPFILE
  }, {
    C_SHOWEDITOR, C_SHOWPROJECT, C_FILESEARCH, SEPARATOR,
    C_SHOWRESULT, C_SHOWINFO, SEPARATOR, C_SHOWBUTTONS, C_SHOWINPUT, C_SHOWSTATUS,
    GUIMacOSX.nativeFullscreen() ? null : C_FULL
  }, {
    C_SHOWMAP, C_SHOWTREE, C_SHOWFOLDER, C_SHOWPLOT, C_SHOWTABLE,
    C_SHOWEXPLORE,
  }, {
    C_RTEXEC, C_RTFILTER, SEPARATOR,
    C_COLOR, C_FONTS, Prop.MAC ? null : SEPARATOR,
    C_PACKAGES, Prop.MAC ? null : C_PREFS
  }, {
    C_HELP, Prop.MAC ? null : SEPARATOR,
    C_COMMUNITY, C_UPDATES, Prop.MAC ? null : SEPARATOR,
    Prop.MAC ? null : C_ABOUT
  }};

  /** Context menu entries. */
  public static final GUIMenuCmd[] POPUP = {
    C_GOBACK, C_FILTER, null, C_COPY, C_PASTE, C_DELETE, C_INSERT, C_EDIT, null, C_COPYPATH
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
      small = BaseXImages.icon(s);
      large = UIManager.getIcon("OptionPane." + l + "Icon");
    }
  }

  // COLORS ===================================================================

  /** Background color. */
  public static final Color BACK = new Color(TEXTFIELD.getBackground().getRGB());
  /** Text color. */
  public static final Color TEXT = new Color(TEXTFIELD.getForeground().getRGB());
  /** Panel color. */
  public static final Color PANEL = new Color(LABEL.getBackground().getRGB());

  /** Dark theme. */
  public static final boolean INVERT = BACK.getRed() + BACK.getGreen() + BACK.getBlue() < 384;

  /** Color: red. */
  public static final Color RED = color(208, 0, 0);
  /** Color: light red. */
  public static final Color LRED = color(255, 216, 216);
  /** Color: green. */
  public static final Color GREEN = color(0, 160, 0);
  /** Color: blue. */
  public static final Color BLUE = color(0, 64, 192);
  /** Color: cyan. */
  public static final Color CYAN = color(0, 160, 160);
  /** Color: purple. */
  public static final Color PURPLE = color(160, 0, 160);

  /** Cell color. */
  public static Color lgray;
  /** Button color. */
  public static Color gray;
  /** Background color. */
  public static Color dgray;

  /** Cached color gradient. */
  private static final Color[] COLORS = new Color[100];

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
  /** Cursor background color. */
  public static Color color3A;
  /** Mark color, custom alpha value. */
  public static Color colormark1A;
  /** Second mark color, custom alpha value. */
  public static Color colormark2A;

  // FONTS ====================================================================

  /** Standard font size (unscaled). */
  public static final int FONTSIZE = 13;

  /** Standard line height. */
  public static final int HEIGHT;
  /** Standard scale factor (>= 1). */
  public static final double SCALE;
  /** Adapted scale factor. */
  public static final double ASCALE;

  /** Large font. */
  public static Font lfont;
  /** Font. */
  public static Font font;
  /** Bold Font. */
  public static Font bfont;
  /** Monospace font. */
  public static Font mfont;
  /** Default monospace font. */
  public static Font dmfont;
  /** Current font size. */
  public static int fontSize;

  /** Default monospace font widths. */
  private static int[] dwidth;
  /** Character large character widths. */
  private static int[] lwidth;
  /** Character widths. */
  private static int[] fwidth;
  /** Bold character widths. */
  private static int[] bwidth;
  /** Monospace character widths. */
  private static int[] mfwidth;

  // KEYS =====================================================================

  /** No modifier. */
  public static final int NO_MOD = 0;
  /** Shift key. */
  public static final int SHIFT = Event.SHIFT_MASK;
  /** Alt key. */
  public static final int ALT = Event.ALT_MASK;
  /** Control key. */
  public static final int CTRL = Event.CTRL_MASK;
  /** Shortcut key (CTRL/META). */
  public static final int META = Prop.MAC ? Event.META_MASK : Event.CTRL_MASK;

  static {
    HEIGHT = LABEL.getFontMetrics(LABEL.getFont()).getHeight();
    SCALE = Math.max(1, (HEIGHT - 16) / 10d + 1);
    ASCALE = 1 + (SCALE - 1) / 2;
  }

  /** Private constructor, preventing class instantiation. */
  private GUIConstants() { }

  /**
   * Initializes colors.
   * @param opts gui options
   */
  public static void init(final GUIOptions opts) {
    lgray = color(224, 224, 224);
    gray = color(160, 160, 160);
    dgray = color(64, 64, 64);

    // create color array
    final int r = opts.get(GUIOptions.COLORRED);
    final int g = opts.get(GUIOptions.COLORGREEN);
    final int b = opts.get(GUIOptions.COLORBLUE);
    final int cl = COLORS.length;
    for(int c = 1; c < cl + 1; ++c) {
      COLORS[c - 1] = color(Math.max(255 - c * r, 0),
        Math.max(255 - c * g, 0), Math.max(255 - c * b, 0));
    }

    color1 = color(darker(r, 24), darker(g, 25), darker(b, 40));
    color2 = color(darker(r, 32), darker(g, 32), darker(b, 44));
    color3 = color(darker(r, 48), darker(g, 50), darker(b, 40));
    color4 = color(darker(r, 140), darker(g, 100), darker(b, 70));
    colormark1 = color(darker(r, 16), darker(g, 120), darker(b, 240));
    colormark2 = color(darker(r, 16), darker(g, 80), darker(b, 160));
    colormark3 = color(darker(r, 32), darker(g, 160), darker(b, 320));
    colormark4 = color(darker(r, 1), darker(g, 40), darker(b, 80));

    final Color col = COLORS[16];
    color1A = color(darker(r, 110), darker(g, 150), darker(b, 160), 100);
    color2A = color(col.getRed(), col.getGreen(), col.getBlue(), 50);
    color3A = color(col.getRed(), col.getGreen(), col.getBlue(), 30);
    colormark1A = color(darker(r, 32), darker(g, 160), darker(b, 320), 100);
    colormark2A = color(darker(r, 12), darker(g, 60), darker(b, 120), 100);

    final String name = opts.get(GUIOptions.FONT);
    final int type = opts.get(GUIOptions.FONTTYPE);

    fontSize = (int) (opts.get(GUIOptions.FONTSIZE) * SCALE);
    font  = new Font(name, type, fontSize);
    mfont = new Font(opts.get(GUIOptions.MONOFONT), type, fontSize);
    bfont = new Font(name, Font.BOLD, fontSize);
    lfont = new Font(name, type, (int) (FONTSIZE * ASCALE * 1.5 + fontSize / 2.0));
    dmfont = new Font(opts.get(GUIOptions.MONOFONT), 0, (int) (HEIGHT * 0.8));

    dwidth  = LABEL.getFontMetrics(dmfont).getWidths();
    fwidth  = LABEL.getFontMetrics(font).getWidths();
    lwidth  = LABEL.getFontMetrics(lfont).getWidths();
    mfwidth = LABEL.getFontMetrics(mfont).getWidths();
    bwidth  = LABEL.getFontMetrics(bfont).getWidths();
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
    if(f == dmfont) return dwidth;
    return LABEL.getFontMetrics(f).getWidths();
  }

  /**
   * Combines the color value with specified factor and returns a new value.
   * @param c color
   * @param f factor
   * @return converted color value
   */
  private static int darker(final int c, final int f) {
    return Math.max(0, 255 - c * f / 10);
  }

  /**
   * Returns the specified color with the specified RGB values, or its inverted version
   * if {@link #INVERT} is true.
   * @param r red component
   * @param g green component
   * @param b blue component
   * @return converted color
   */
  private static Color color(final int r, final int g, final int b) {
    return INVERT ? new Color(255 - r, 255 - g, 255 - b) : new Color(r, g, b);
  }

  /**
   * Returns the specified color with the specified RGB and alpha values, or its inverted version
   * if {@link #INVERT} is true.
   * @param r red component
   * @param g green component
   * @param b blue component
   * @param a alpha component
   * @return converted color
   */
  private static Color color(final int r, final int g, final int b, final int a) {
    return INVERT ? new Color(255 - r, 255 - g, 255 - b, 255 - a) : new Color(r, g, b, a);
  }
}
