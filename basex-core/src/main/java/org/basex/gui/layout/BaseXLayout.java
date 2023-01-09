package org.basex.gui.layout;

import static org.basex.gui.GUIConstants.*;
import static org.basex.gui.layout.BaseXKeys.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.function.*;

import javax.swing.*;
import javax.swing.border.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.dialog.*;
import org.basex.gui.listener.*;
import org.basex.gui.text.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * This class provides static layout and paint helper methods which are used all over
 * the GUI.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class BaseXLayout {
  /** Desktop hints. */
  private static final Map<?, ?> HINTS = (Map<?, ?>)
    Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
  /** Flag for adding rendering hints. */
  private static boolean hints = true;

  /** Shortcut string for meta key (deprecated since Java 9). */
  @SuppressWarnings("deprecation")
  private static final String META = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ==
      InputEvent.CTRL_MASK ? "ctrl" : "meta";

  /** Key listener for global shortcuts. */
  private static KeyListener keys;

  /** Private constructor. */
  private BaseXLayout() { }

  /**
   * If enabled, focuses the specified component.
   * @param comp component to be focused
   */
  static void focus(final Component comp) {
    final GUI gui = gui(comp);
    if(gui == null) return;
    if(gui.gopts.get(GUIOptions.MOUSEFOCUS) && comp.isEnabled()) comp.requestFocusInWindow();
  }

  /**
   * Set desktop hints (not supported by all platforms).
   * @param g graphics reference
   */
  public static void hints(final Graphics g) {
    if(HINTS != null && hints) {
      try {
        ((Graphics2D) g).addRenderingHints(HINTS);
      } catch(final Exception ex) {
        Util.stack(ex);
        hints = false;
      }
    }
  }

  /**
   * Activates graphics anti-aliasing.
   * @param g graphics reference
   * @return graphics reference
   */
  public static Graphics2D antiAlias(final Graphics g) {
    final Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    return g2;
  }

  /**
   * Returns the ancestor GUI reference of the specified component.
   * @param comp component
   * @return reference to the main window or {@code null}
   */
  private static GUI gui(final Component comp) {
    Component c = comp;
    do {
      if(c instanceof GUI) return (GUI) c;
      c = c.getParent();
    } while(c != null);
    return null;
  }

  /**
   * Sets the scaled component width, adopting the original component height.
   * @param comp component
   * @param w width
   */
  public static void setWidth(final Component comp, final int w) {
    comp.setPreferredSize(new Dimension(w, comp.getPreferredSize().height));
  }

  /**
   * Sets the scaled component height, adopting the original component width.
   * @param comp component
   * @param h height
   */
  public static void setHeight(final Component comp, final int h) {
    comp.setPreferredSize(new Dimension(comp.getPreferredSize().width, h));
  }

  /**
   * Returns a border with the specified insets.
   * @param t top distance
   * @param l left distance
   * @param b bottom distance
   * @param r right distance
   * @return border
   */
  public static EmptyBorder border(final int t, final int l, final int b, final int r) {
    return new EmptyBorder(t, l, b, r);
  }

  /**
   * Adds drag and drop functionality.
   * @param comp component
   * @param dnd drag and drop handler
   */
  public static void addDrop(final JComponent comp, final DropHandler dnd) {
    comp.setDropTarget(new DropTarget(comp, DnDConstants.ACTION_COPY_OR_MOVE, null, true, null) {
      @Override
      public synchronized void drop(final DropTargetDropEvent dtde) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        for(final Object clip : fromClipboard(dtde::getTransferable)) {
          dnd.drop(clip);
        }
        comp.requestFocusInWindow();
      }
    });
  }

  /**
   * Returns a keystroke for the specified string.
   * @param cmd command
   * @return keystroke or {@code null}
   */
  public static KeyStroke keyStroke(final GUICommand cmd) {
    final Object sc = cmd.shortcuts();
    if(sc == null) return null;

    final String scut;
    if(sc instanceof BaseXKeys[]) {
      final BaseXKeys[] scs = (BaseXKeys[]) sc;
      if(scs.length == 0) return null;
      scut = scs[0].shortCut();
    } else {
      scut = Util.info(sc, META);
    }
    final KeyStroke ks = KeyStroke.getKeyStroke(scut);
    if(ks == null) Util.errln("Could not assign shortcut: " + sc + " / " + scut);
    return ks;
  }

  /**
   * Sets a mnemomic for the specified button.
   * @param b button
   * @param mnem mnemonics that have already been assigned
   */
  public static void setMnemonic(final AbstractButton b, final StringBuilder mnem) {
    // do not set mnemonics for Mac! Alt+key used for special characters.
    if(Prop.MAC) return;

    // find and assign unused mnemomic
    final String label = b.getText();
    final int ll = label.length();
    for(int l = 0; l < ll; l++) {
      final char ch = Character.toLowerCase(label.charAt(l));
      if(!letter(ch) || mnem.indexOf(Character.toString(ch)) != -1) continue;
      b.setMnemonic(ch);
      mnem.append(ch);
      break;
    }
  }

  /**
   * Returns the clipboard contents.
   * @param clipboard clipboard supplier (optional)
   * @return clipboard contents
   */
  @SuppressWarnings("unchecked")
  public static ArrayList<Object> fromClipboard(final Supplier<Transferable> clipboard) {
    // try multiple times (system clipboard may be blocked by another process)
    final ArrayList<Object> list = new ArrayList<>();
    final int n = 10;
    for(int i = 1; i <= n; i++) {
      try {
        final Transferable tr = clipboard != null ? clipboard.get() :
          Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if(tr != null) {
          if(tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            list.addAll((List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor));
          } else if(tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            list.add(tr.getTransferData(DataFlavor.stringFlavor));
          } else {
            final StringBuilder sb = new StringBuilder("Data flavors not supported:\n");
            for(final DataFlavor df : tr.getTransferDataFlavors()) {
              sb.append("- ").append(df).append('\n');
            }
            Util.debug(sb);
          }
          break;
        }
        Util.debug("Clipboard has no contents.");
        break;
      } catch(final Exception ex) {
        Util.stack(ex);
        Performance.sleep(i * 200L);
      }
    }
    return list;
  }

  /**
   * Copies the specified string to the clipboard.
   * @param text text
   */
  public static void toClipboard(final String text) {
    // try multiple times (system clipboard may be blocked by another process)
    final int n = 10;
    for(int i = 1; i <= n; i++) {
      try {
        final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        clip.setContents(new StringSelection(text), null);
        return;
      } catch(final Exception ex) {
        Util.stack(ex);
        if(i == n) {
          Util.stack(ex);
        } else {
          Util.debug(ex);
          Performance.sleep(i * 200L);
        }
      }
    }
  }

  /**
   * Copies the specified file path to the clipboard.
   * @param path path
   */
  public static void copyPath(final String path) {
    try {
      toClipboard(Paths.get(path).toRealPath().toString());
    } catch(final Exception ex) {
      Util.stack(ex);
    }
  }

  /**
   * Drag and drop handler.
   * @author BaseX Team 2005-23, BSD License
   * @author Christian Gruen
   */
  public interface DropHandler {
    /**
     * Drops a file.
     * @param object object to be dropped
     */
    void drop(Object object);
  }

  /**
   * Adds default listeners to the specified component.
   * @param comp component
   * @param win parent window
   */
  public static void addInteraction(final Component comp, final BaseXWindow win) {
    comp.addMouseListener((MouseEnteredListener) e -> focus(comp));

    // check if component is a dialog
    final BaseXDialog dialog = win.dialog();
    if(dialog == null) {
      // no: window is main window
      comp.addKeyListener(globalShortcuts(win.gui()));
    } else {
      // yes: add default keys
      comp.addKeyListener((KeyPressedListener) e -> {
        final BaseXCombo combo = comp instanceof BaseXCombo ? (BaseXCombo) comp : null;
        if(combo != null && combo.isPopupVisible()) return;

        // do not key close dialog if button or editor is focused
        if(ENTER.is(e) && !(comp instanceof BaseXButton || comp instanceof TextPanel)) {
          dialog.close();
        } else if(ESCAPE.is(e)) {
          // do not cancel dialog if search bar is opened
          boolean close = true;
          if(comp instanceof TextPanel) {
            final SearchBar bar = ((TextPanel) comp).getSearch();
            close = bar == null || !bar.deactivate(true);
          }
          if(close) dialog.cancel();
        }
      });
    }
  }

  /**
   * Returns or creates a new key listener for global shortcuts.
   * @param gui gui reference
   * @return key listener
   */
  private static KeyListener globalShortcuts(final GUI gui) {
    if(keys == null) keys = (KeyPressedListener) e -> {
      // browse back/forward
      if(gui.context.data() != null) {
        if(GOBACK.is(e)) {
          GUIMenuCmd.C_GO_BACK.execute(gui);
        } else if(GOFORWARD.is(e)) {
          GUIMenuCmd.C_GO_FORWARD.execute(gui);
        } else if(GOUP.is(e)) {
          GUIMenuCmd.C_GO_UP.execute(gui);
        } else if(GOHOME.is(e)) {
          GUIMenuCmd.C_GO_HOME.execute(gui);
        } else if(FILTER.is(e)) {
          GUIMenuCmd.C_FILTER_NODES.execute(gui);
        }
      }

      // focus input bar
      if(FOCUSINPUT.is(e)) gui.input.requestFocusInWindow();
      // focus editor
      else if(FOCUSEDITOR.is(e)) gui.editor.focusEditor();
      // focus text
      else if(FOCUSTEXT.is(e)) gui.text.focusText();

      // change font size
      final int fs = gui.gopts.get(GUIOptions.FONTSIZE);
      long nfs = fs;
      if(INCFONT1.is(e) || INCFONT2.is(e)) {
        nfs = Math.max(fs + 1, Math.round(fs * 1.05));
      } else if(DECFONT.is(e)) {
        nfs = Math.min(fs - 1, Math.round(fs / 1.05));
      } else if(NORMFONT.is(e)) {
        nfs = (long) (fontSize() * 1.5);
      }
      if(fs != nfs) {
        gui.gopts.set(GUIOptions.FONTSIZE, (int) Math.max(1, Math.min(128, nfs)));
        gui.updateLayout();
      }
    };
    return keys;
  }

  /**
   * Adds human-readable shortcuts to the specified string.
   * @param string tooltip string
   * @param sc shortcut (can be {@code null})
   * @return tooltip
   */
  public static String addShortcut(final String string, final String sc) {
    if(sc == null) return string;
    final StringBuilder sb = new StringBuilder();
    for(final String s : sc.split(" ")) {
      String t = "%".equals(s) ? Prop.MAC ? "meta" : "control" : s;
      if(t.length() != 1) t = Toolkit.getProperty("AWT." + t.toLowerCase(Locale.ENGLISH), t);
      sb.append('+').append(t);
    }
    return string + " (" + sb.substring(1) + ')';
  }

  /**
   * Returns the value of the specified pre value and attribute.
   * @param val value to be evaluated
   * @return value as string
   */
  public static String value(final double val) {
    return string(chopNumber(token(val)));
  }

  /**
   * Draws a colored cell.
   * @param g graphics reference
   * @param xs horizontal start position
   * @param xe horizontal end position
   * @param ys vertical start position
   * @param ye vertical end position
   * @param focus highlighting flag
   */
  public static void drawCell(final Graphics g, final int xs, final int xe, final int ys,
      final int ye, final boolean focus) {

    g.setColor(gray);
    g.drawRect(xs, ys, xe - xs - 1, ye - ys - 1);
    g.setColor(BACK);
    g.drawRect(xs + 1, ys + 1, xe - xs - 3, ye - ys - 3);
    g.setColor(focus ? lgray : BACK);
    g.fillRect(xs + 1, ys + 1, xe - xs - 2, ye - ys - 2);
  }

  /**
   * Draws a centered string to the panel.
   * @param g graphics reference
   * @param string string to be drawn
   * @param w panel width
   * @param y vertical position
   */
  public static void drawCenter(final Graphics g, final String string, final int w, final int y) {
    g.drawString(string, (w - width(g, string)) / 2, y);
  }

  /**
   * Draws a visualization tooltip.
   * @param g graphics reference
   * @param tt tooltip label
   * @param x horizontal position
   * @param y vertical position
   * @param w width
   * @param c color depth
   */
  public static void drawTooltip(final Graphics g, final String tt, final int x, final int y,
      final int w, final int c) {
    final int tw = width(g, tt);
    final int th = g.getFontMetrics().getHeight();
    final int xx = Math.min(w - tw - 8, x);
    g.setColor(color(c));
    g.fillRect(xx - 1, y - th, tw + 4, th);
    g.setColor(BACK);
    g.drawString(tt, xx, y - 4);
  }

  /**
   * Returns the width of the specified text.
   * @param g graphics reference
   * @param s string to be evaluated
   * @return string width
   */
  public static int width(final Graphics g, final String s) {
    return g.getFontMetrics().stringWidth(s);
  }

  /**
   * Draws the specified string and chops the last characters if there is not enough space.
   * @param g graphics reference
   * @param string string
   * @param x x coordinate
   * @param y y coordinate
   * @param w width
   * @param fs font size
   */
  public static void chopString(final Graphics g, final byte[] string, final int x, final int y,
      final int w, final int fs) {

    // too little space: skip rendering
    if(w < 10) return;
    int j = string.length;
    try {
      for(int k = 0, l = 0, fw = 0; k < j; k += l) {
        final int ww = width(g, cp(string, k));
        if(fw + ww >= w - 4) {
          j = Math.max(1, k - l);
          if(k > 1) fw -= width(g, cp(string, k - 1));
          g.drawString("..", x + fw, y + fs);
          break;
        }
        fw += ww;
        l = cl(string, k);
      }
    } catch(final Exception ex) {
      Util.debug(ex);
    }
    g.drawString(string(string, 0, j), x, y + fs);
  }

  /**
   * Returns the width of the specified string.
   * Cached font widths are used to speed up calculation.
   * @param g graphics reference
   * @param string string
   * @return string width
   */
  public static int width(final Graphics g, final byte[] string) {
    int fw = 0;
    try {
      // ignore faulty character sets
      final int l = string.length;
      for(int k = 0; k < l; k += cl(string, k)) fw += width(g, cp(string, k));
    } catch(final Exception ex) {
      Util.debug(ex);
    }
    return fw;
  }

  /**
   * Returns the character width of the specified character.
   * @param g graphics reference
   * @param cp code point
   * @return character width
   */
  public static int width(final Graphics g, final int cp) {
    return g.getFontMetrics().charWidth(cp);
  }

  /**
   * Resizes a font.
   * @param comp component
   * @param factor resize factor
   */
  public static void resizeFont(final JComponent comp, final float factor) {
    final Font f = comp.getFont();
    comp.setFont(f.deriveFont(f.getSize() * factor));
  }

  /**
   * Uses a bold font for the specified component.
   * @param comp component
   */
  public static void boldFont(final JComponent comp) {
    comp.setFont(comp.getFont().deriveFont(Font.BOLD));
  }

  /**
   * Returns the file name and a reverse representation of the file path.
   * @param file file
   * @return path
   */
  public static String reversePath(final IOFile file) {
    final StringBuilder sb = new StringBuilder();
    final String[] names = file.file().getParent().split("[/\\\\]");
    for(int n = names.length - 1; n >= 0; n--) {
      sb.append(names[n]);
      if(n > 0) sb.append('/');
    }
    return sb.toString();
  }

  /**
   * Returns a string representation for the specified file.
   * @param file file
   * @param full full path
   * @return string
   */
  public static String info(final IOFile file, final boolean full) {
    final StringBuilder sb = new StringBuilder();
    if(file != null) {
      sb.append(full ? file.path() : file.name());
      if(file.exists()) sb.append(" (").append(Performance.format(file.length())).append(')');
    }
    return sb.toString();
  }

  /**
   * Returns a string representation of the number of results.
   * @param count number of results (ignored if {@code -1})
   * @param bytes number of bytes (ignored if {@code -1})
   * @param gui GUI reference
   * @return result string
   */
  public static String results(final long count, final long bytes, final GUI gui) {
    final GUIOptions gopts = gui.gopts;
    final BiFunction<Long, Integer, String> more = (num, max) -> num >= max ? "\u2265" : "";
    final StringBuilder sb = new StringBuilder();
    if(count >= 0) {
      final String num = new DecimalFormat("#,###,###").format(count);
      final String text = more.apply(count, gopts.get(GUIOptions.MAXRESULTS)) + num;
      sb.append(Util.info(count == 1 ? Text.RESULT_X : Text.RESULTS_X, text));
      if(bytes >= 0) {
        final int maxtext = gopts.get(GUIOptions.MAXTEXT);
        sb.append(", ").append(more.apply(bytes, maxtext)).append(Performance.format(bytes));
      }
    }
    return sb.length() > 0 ? sb.toString() : " ";
  }

  /**
   * Assigns macOS-specific interface GUI properties.
   * @param gui GUI reference
   */
  public static void initMac(final GUI gui) {
    try {
      // show menu in the screen menu instead of inside the application window
      System.setProperty("apple.laf.useScreenMenuBar", "true");

      final Desktop desktop = Desktop.getDesktop();
      desktop.setAboutHandler(e -> DialogAbout.show(gui));
      desktop.setPreferencesHandler(e -> DialogPrefs.show(gui));
      desktop.setQuitHandler((e, r) -> {
        if(gui.quit()) r.performQuit();
        else r.cancelQuit();
      });

      final Taskbar taskbar = Taskbar.getTaskbar();
      taskbar.setIconImage(BaseXImages.get("logo_large"));

    } catch(final Exception ex) {
      Util.errln("Failed to initialize native Mac OS X interface:");
      Util.stack(ex);
    }
  }
}
