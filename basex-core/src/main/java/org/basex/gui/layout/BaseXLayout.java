package org.basex.gui.layout;

import static org.basex.gui.GUIConstants.*;
import static org.basex.gui.layout.BaseXKeys.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.gui.text.*;
import org.basex.util.*;

/**
 * This class provides static layout and paint helper methods which are used all over
 * the GUI.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BaseXLayout {
  /** Desktop hints. */
  private static final Map<?, ?> HINTS = (Map<?, ?>)
    Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
  /** Flag for adding rendering hints. */
  private static boolean hints = true;

  /** Shortcut string for meta key. */
  private static final String META = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ==
      Event.META_MASK ? "meta" : "ctrl";
  /** Key listener for global shortcuts. */
  private static KeyAdapter keys;

  /** Private constructor. */
  private BaseXLayout() { }

  /**
   * Sets the help text for the specified component.
   * @param cont input container
   */
  static void focus(final Component cont) {
    final GUI gui = gui(cont);
    if(gui == null) return;
    if(gui.gopts.get(GUIOptions.MOUSEFOCUS) && cont.isEnabled()) cont.requestFocusInWindow();
  }

  /**
   * Set desktop hints (not supported by all platforms).
   * @param g graphics reference
   */
  public static void hints(final Graphics g) {
    if(hints) {
      try {
        ((Graphics2D) g).addRenderingHints(HINTS);
      } catch(final Exception ex) {
        Util.debug(ex);
        hints = false;
      }
    }
  }

  /**
   * Returns the GUI reference of the specified container.
   * @param cont input container
   * @return gui
   */
  private static GUI gui(final Component cont) {
    final Container c = cont.getParent();
    return c == null || c instanceof GUI ? (GUI) c : gui(c);
  }

  /**
   * Sets the component width, adopting the original component height.
   * @param comp component
   * @param w width
   */
  public static void setWidth(final Component comp, final int w) {
    comp.setPreferredSize(new Dimension(w, comp.getPreferredSize().height));
  }

  /**
   * Sets the component height, adopting the original component width.
   * @param comp component
   * @param h height
   */
  public static void setHeight(final Component comp, final int h) {
    comp.setPreferredSize(new Dimension(comp.getPreferredSize().width, h));
  }

  /**
   * Sets the component size.
   * @param comp component
   * @param w width
   * @param h height
   */
  static void setSize(final Component comp, final int w, final int h) {
    comp.setPreferredSize(new Dimension(w, h));
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
        final Transferable tr = dtde.getTransferable();
        for(final Object o : contents(tr)) dnd.drop(o);
        comp.requestFocusInWindow();
      }
    });
  }

  /**
   * Returns a keystroke for the specified string.
   * @param cmd command
   * @return keystroke
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
    for(int l = 0; l < label.length(); l++) {
      final char ch = Character.toLowerCase(label.charAt(l));
      if(!letter(ch) || mnem.indexOf(Character.toString(ch)) != -1)
        continue;
      b.setMnemonic(ch);
      mnem.append(ch);
      break;
    }
  }

  /**
   * Returns the contents of the specified transferable.
   * @param tr transferable
   * @return contents
   */
  @SuppressWarnings("unchecked")
  public static ArrayList<Object> contents(final Transferable tr) {
    final ArrayList<Object> list = new ArrayList<Object>();
    try {
      if(tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        for(final File fl : (List<File>)
            tr.getTransferData(DataFlavor.javaFileListFlavor)) list.add(fl);
      } else if(tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        list.add(tr.getTransferData(DataFlavor.stringFlavor));
      }
    } catch(final Exception ex) {
      Util.stack(ex);
    }
    return list;
  }

  /**
   * Copies the specified string to the clipboard.
   * @param text text
   */
  public static void copy(final String text) {
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
  }

  /**
   * Drag and drop handler.
   * @author BaseX Team 2005-14, BSD License
   * @author Christian Gruen
   */
  public interface DropHandler {
    /**
     * Drops a file.
     * @param obj object to be dropped
     */
    void drop(final Object obj);
  }

  /**
   * Adds default interactions to the specified component.
   * @param comp component
   * @param win parent window
   */
  public static void addInteraction(final Component comp, final Window win) {
    comp.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(final MouseEvent e) {
        focus(comp);
      }
    });

    if(win instanceof BaseXDialog) {
      // add default keys
      final BaseXDialog d = (BaseXDialog) win;
      comp.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(final KeyEvent e) {
          final Object s = e.getSource();
          if(s instanceof BaseXCombo && ((JComboBox) s).isPopupVisible()) return;

          // do not key close dialog of button or editor is focused
          if(ENTER.is(e) && !(s instanceof BaseXButton || s instanceof TextPanel)) {
            d.close();
          } else if(ESCAPE.is(e)) {
            // do not cancel dialog if search bar is opened
            boolean close = true;
            if(s instanceof TextPanel) {
              final SearchBar bar = ((TextPanel) s).getSearch();
              close = bar == null || !bar.deactivate(true);
            }
            if(close) d.cancel();
          }
        }
      });
      return;
    }

    if(win instanceof GUI) {
      comp.addKeyListener(globalShortcuts((GUI) win));
    } else {
      throw Util.notExpected("Reference to main window expected.");
    }
  }

  /**
   * Returns or creates a new key listener for global shortcuts.
   * @param gui gui reference
   * @return key listener
   */
  private static KeyAdapter globalShortcuts(final GUI gui) {
    if(keys == null) {
      keys = new KeyAdapter() {
        @Override
        public void keyPressed(final KeyEvent e) {
          // browse back/forward
          if(gui.context.data() != null) {
            if(GOBACK.is(e)) {
              GUIMenuCmd.C_GOBACK.execute(gui);
            } else if(GOFORWARD.is(e)) {
              GUIMenuCmd.C_GOFORWARD.execute(gui);
            } else if(GOUP.is(e)) {
              GUIMenuCmd.C_GOUP.execute(gui);
            } else if(GOHOME.is(e)) {
              GUIMenuCmd.C_GOHOME.execute(gui);
            }
          }

          // jump to input bar
          if(INPUTBAR.is(e)) gui.input.requestFocusInWindow();

          // change font size
          final int fs = gui.gopts.get(GUIOptions.FONTSIZE);
          int nfs = fs;
          if(INCFONT1.is(e) || INCFONT2.is(e)) {
            nfs = fs + 1;
          } else if(DECFONT.is(e)) {
            nfs = Math.max(1, fs - 1);
          } else if(NORMFONT.is(e)) {
            nfs = 13;
          }
          if(fs != nfs) {
            gui.gopts.set(GUIOptions.FONTSIZE, nfs);
            gui.updateLayout();
          }
        }
      };
    }
    return keys;
  }

  /**
   * Adds human readable shortcuts to the specified string.
   * @param str text of tooltip
   * @param sc shortcut
   * @return tooltip
   */
  public static String addShortcut(final String str, final String sc) {
    if(sc == null || str == null) return str;
    final StringBuilder sb = new StringBuilder();
    for(final String s : sc.split(" ")) {
      String t = "%".equals(s) ? Prop.MAC ? "meta" : "control" : s;
      if(t.length() != 1) t = Toolkit.getProperty("AWT." + t.toLowerCase(), t);
      sb.append('+').append(t);
    }
    return str + " (" + sb.substring(1) + ')';
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
   * Fills the specified area with a color gradient.
   * @param gg graphics reference
   * @param c1 first color
   * @param c2 second color
   * @param xs horizontal start position
   * @param ys vertical start position
   * @param xe horizontal end position
   * @param ye vertical end position
   */
  public static void fill(final Graphics gg, final Color c1, final Color c2, final int xs,
      final int ys, final int xe, final int ye) {

    final int w = xe - xs, h = ye - ys;
    final int r = c1.getRed(), g = c1.getGreen(), b = c1.getBlue();
    final float rf = (c2.getRed() - r) / (float) h;
    final float gf = (c2.getGreen() - g) / (float) h;
    final float bf = (c2.getBlue() - b) / (float) h;

    int hh = 0;
    for(int y = 0, cr = 0, cg = 0, cb = 0; y < h; ++y) {
      final int nr = r + (int) (rf * y);
      final int ng = g + (int) (gf * y);
      final int nb = b + (int) (bf * y);
      if(nr != cr || ng != cg || nb != cb) {
        gg.setColor(new Color(nr, ng, nb));
        gg.fillRect(xs, ys + y - hh, w, hh);
        hh = 0;
      }
      cr = nr;
      cg = ng;
      cb = nb;
      ++hh;
    }
    gg.fillRect(xs, ys + h - hh, w, hh);
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

    g.setColor(GRAY);
    g.drawRect(xs, ys, xe - xs - 1, ye - ys - 1);
    g.setColor(Color.white);
    g.drawRect(xs + 1, ys + 1, xe - xs - 3, ye - ys - 3);

    fill(g, focus ? LGRAY : Color.white, LGRAY, xs + 2, ys + 2, xe - 1, ye - 1);
  }

  /**
   * Draws a centered string to the panel.
   * @param g graphics reference
   * @param text text to be painted
   * @param w panel width
   * @param y vertical position
   */
  public static void drawCenter(final Graphics g, final String text, final int w, final int y) {
    g.drawString(text, (w - width(g, text)) / 2, y);
  }

  /**
   * Draws a visualization tooltip.
   * @param g graphics reference
   * @param tt tooltip label
   * @param x horizontal position
   * @param y vertical position
   * @param w width
   * @param c color color depth
   */
  public static void drawTooltip(final Graphics g, final String tt, final int x, final int y,
      final int w, final int c) {
    final int tw = width(g, tt);
    final int th = g.getFontMetrics().getHeight();
    final int xx = Math.min(w - tw - 8, x);
    g.setColor(color(c));
    g.fillRect(xx - 1, y - th, tw + 4, th);
    g.setColor(WHITE);
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
   * Draws the specified string.
   *
   * @param g graphics reference
   * @param s text
   * @param x x coordinate
   * @param y y coordinate
   * @param w width
   * @param fs font size
   */
  public static void chopString(final Graphics g, final byte[] s, final int x, final int y,
      final int w, final int fs) {

    if(w < 12) return;
    final int[] cw = fontWidths(g.getFont());

    int j = s.length;
    try {
      int l = 0;
      int fw = 0;
      for(int k = 0; k < j; k += l) {
        final int ww = width(g, cw, cp(s, k));
        if(fw + ww >= w - 4) {
          j = Math.max(1, k - l);
          if(k > 1) fw -= width(g, cw, cp(s, k - 1));
          g.drawString("..", x + fw, y + fs);
          break;
        }
        fw += ww;
        l = cl(s, k);
      }
    } catch(final Exception ex) {
      Util.debug(ex);
    }
    g.drawString(string(s, 0, j), x, y + fs);
  }

  /**
   * Returns the width of the specified text.
   * Cached font widths are used to speed up calculation.
   * @param g graphics reference
   * @param s string to be evaluated
   * @return string width
   */
  public static int width(final Graphics g, final byte[] s) {
    final int[] cw = fontWidths(g.getFont());
    final int l = s.length;
    int fw = 0;
    try {
      // ignore faulty character sets
      for(int k = 0; k < l; k += cl(s, k)) fw += width(g, cw, cp(s, k));
    } catch(final Exception ex) {
      Util.debug(ex);
    }
    return fw;
  }

  /**
   * Returns the character width of the specified character.
   * @param g graphics reference
   * @param cw array with character widths
   * @param c character
   * @return character width
   */
  public static int width(final Graphics g, final int[] cw, final int c) {
    return c >= cw.length ? g.getFontMetrics().charWidth(c) : cw[c];
  }
}
