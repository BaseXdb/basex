package org.basex.gui.layout;

import static org.basex.gui.GUIConstants.*;
import static org.basex.gui.layout.BaseXKeys.*;
import static org.basex.util.Token.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.swing.ImageIcon;
import org.basex.core.Main;
import org.basex.gui.GUI;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.dialog.Dialog;
import org.basex.util.Performance;

/**
 * This class assembles layout and paint methods which are frequently
 * used in the GUI.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class BaseXLayout {
  /** Cached images. */
  private static final HashMap<String, ImageIcon> IMAGES =
    new HashMap<String, ImageIcon>();
  /** Date Format. */
  private static final SimpleDateFormat DATE =
    new SimpleDateFormat("dd.MM.yyyy HH:mm");

  /** Private constructor. */
  private BaseXLayout() { }

  /**
   * Sets the help text for the specified component.
   * @param cont input container
   * @param help help text
   */
  public static void focus(final Component cont, final byte[] help) {
    final GUI gui = gui(cont);
    if(gui == null) return;
    if(gui.prop.is(GUIProp.MOUSEFOCUS)) cont.requestFocusInWindow();
    if(gui.fullscreen) return;
    if(help != null && gui.prop.is(GUIProp.SHOWHELP)) gui.help.setText(help);
  }

  /**
   * Returns the class reference of the specified container.
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
   * Adds default interactions to the specified component.
   * @param comp component
   * @param hlp help text
   * @param win parent window
   */
  public static void addInteraction(final Component comp, final byte[] hlp,
        final Window win) {

    comp.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(final MouseEvent e) {
        focus(comp, hlp);
      }
    });

    final boolean dialog = win instanceof Dialog;
    final GUI gui = dialog ? ((Dialog) win).gui : win instanceof GUI ?
        (GUI) win : null;

    if(dialog) {
      // add default keys
      final Dialog d = (Dialog) win;
      comp.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(final KeyEvent e) {
          if(e.getSource() instanceof BaseXCombo) {
            if(((BaseXCombo) e.getSource()).isPopupVisible()) return;
          }
          // process key events
          if(pressed(ENTER, e)) {
            final Object s = e.getSource();
            if(!(s instanceof BaseXButton || s instanceof BaseXText)) d.close();
          } else if(pressed(ESCAPE, e)) {
            d.cancel();
          }
        }
      });
    }
    if(gui == null) return;

    // add default keys
    comp.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        // browse back/forward
        if(gui.context.data != null) {
          if(pressed(GOBACK, e)) {
            GUICommands.GOBACK.execute(gui);
          } else if(pressed(GOFORWARD, e)) {
            GUICommands.GOFORWARD.execute(gui);
          } else if(pressed(GOUP, e)) {
            GUICommands.GOUP.execute(gui);
          } else if(pressed(GOHOME, e)) {
            GUICommands.GOHOME.execute(gui);
          }
        }

        final int fs = gui.prop.num(GUIProp.FONTSIZE);
        int nfs = fs;
        if(pressed(INCFONT1, e) || pressed(INCFONT2, e)) {
          nfs = fs + 1;
        } else if(pressed(DECFONT, e)) {
          nfs = Math.max(1, fs - 1);
        } else if(pressed(NORMFONT, e)) {
          nfs = 12;
        }
        if(fs != nfs) {
          gui.prop.set(GUIProp.FONTSIZE, nfs);
          GUIConstants.initFonts(gui.prop);
          gui.notify.layout();
        }
      }
    });
  }

  /**
   * Returns the specified image as icon.
   * @param name name of icon
   * @return icon
   */
  public static ImageIcon icon(final String name) {
    ImageIcon img = IMAGES.get(name);
    if(img != null) return img;
    img = new ImageIcon(image(name));
    IMAGES.put(name, img);
    return img;
  }

  /**
   * Returns the specified image.
   * @param name name of image
   * @return image
   */
  public static Image image(final String name) {
    return Toolkit.getDefaultToolkit().getImage(imageURL(name));
  }

  /**
   * Returns the image url.
   * @param name name of image
   * @return url
   */
  public static URL imageURL(final String name) {
    final String path = "/img/" + name + ".png";
    final URL url = GUI.class.getResource(path);
    if(url == null) Main.errln("Not found: " + path);
    return url;
  }

  /**
   * Returns the value of the specified pre value and attribute.
   * @param val value to be evaluated
   * @param size size flag
   * @param date date flag
   * @return value as string
   */
  public static String value(final double val, final boolean size,
      final boolean date) {

    if(size) return Performance.format((long) val, true);
    if(date) return DATE.format(new Date((long) val));
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
  public static void fill(final Graphics gg, final Color c1,
      final Color c2, final int xs, final int ys, final int xe, final int ye) {

    final int w = xe - xs;
    final int h = ye - ys;
    final int r = c1.getRed();
    final int g = c1.getGreen();
    final int b = c1.getBlue();
    final float rf = (c2.getRed() - r) / (float) h;
    final float gf = (c2.getGreen() - g) / (float) h;
    final float bf = (c2.getBlue() - b) / (float) h;

    int nr = 0, ng = 0, nb = 0;
    int cr = 0, cg = 0, cb = 0;
    int hh = 0;
    for(int y = 0; y < h; y++) {
      nr = r + (int) (rf * y);
      ng = g + (int) (gf * y);
      nb = b + (int) (bf * y);
      if(nr != cr || ng != cg || nb != cb) {
        gg.setColor(new Color(nr, ng, nb));
        gg.fillRect(xs, ys + y - hh, w, hh);
        hh = 0;
      }
      cr = nr;
      cg = ng;
      cb = nb;
      hh++;
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
  public static void drawCell(final Graphics g, final int xs,
      final int xe, final int ys, final int ye, final boolean focus) {

    g.setColor(COLORBUTTON);
    g.drawRect(xs, ys, xe - xs - 1, ye - ys - 1);
    g.setColor(Color.white);
    g.drawRect(xs + 1, ys + 1, xe - xs - 3, ye - ys - 3);

    fill(g, focus ? COLORCELL : Color.white, COLORCELL,
        xs + 2, ys + 2, xe - 1, ye - 1);
  }

  /**
   * Draws a centered string to the panel.
   * @param g graphics reference
   * @param text text to be painted
   * @param w panel width
   * @param y vertical position
   */
  public static void drawCenter(final Graphics g, final String text,
      final int w, final int y) {
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
  public static void drawTooltip(final Graphics g, final String tt,
      final int x, final int y, final int w, final int c) {
    final int tw = width(g, tt);
    final int th = g.getFontMetrics().getHeight();
    final int xx = Math.min(w - tw - 8, x);
    g.setColor(GUIConstants.COLORS[c]);
    g.fillRect(xx - 1, y - th, tw + 4, th);
    g.setColor(GUIConstants.color1);
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
   * @param g graphics reference
   * @param s text
   * @param x x coordinate
   * @param y y coordinate
   * @param w width
   * @param fs font size
   * @return width of printed string
   */
  public static int chopString(final Graphics g, final byte[] s,
      final int x, final int y, final int w, final int fs) {

    if(w < 12) return w;
    final int[] cw = fontWidths(g.getFont());

    int j = s.length;
    int fw = 0;
    int l = 0;
    try {
      for(int k = 0; k < j; k += l) {
        final int ww = width(g, cw, cp(s, k));
        if(fw + ww >= w - 4) {
          j = Math.max(1, k - l);
          if(k > 1) fw -= width(g, cw, cp(s, k - 1));
          g.drawString("..", x + fw, y + fs);
          break;
        }
        fw += ww;
        l = cl(s[k]);
      }
    } catch(final Exception ex) {
      Main.debug(ex);
    }
    g.drawString(string(s, 0, j), x, y + fs);
    return fw;
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
      for(int k = 0; k < l; k += cl(s[k])) fw += width(g, cw, cp(s, k));
    } catch(final Exception ex) {
      Main.debug(ex);
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
    return c > 255 ? g.getFontMetrics().charWidth(c) : cw[c];
  }
}
