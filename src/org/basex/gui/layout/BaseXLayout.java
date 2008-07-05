package org.basex.gui.layout;

import static org.basex.Text.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.dialog.Dialog;
import org.basex.gui.view.View;
import org.basex.gui.view.map.MapRect;
import org.basex.util.Token;

/**
 * This class assembles layout and paint methods which are frequently
 * used in the GUI.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXLayout {
  /** Private constructor. */
  private BaseXLayout() { }

  /**
   * Creates a OK and CANCEL button.
   * @param dialog reference to the component, reacting on button clicks.
   * @return button list
   */
  public static BaseXBack okCancel(final Dialog dialog) {
    return newButtons(dialog, true,
        new String[] { BUTTONOK, BUTTONCANCEL },
        new byte[][] { HELPOK, HELPCANCEL });
  }

  /**
   * Creates a new button list.
   * @param dialog reference to the component, reacting on button clicks.
   * @param hor horizontal alignment
   * @param texts button names
   * @param help help texts
   * @return button list
   */
  public static BaseXBack newButtons(final Dialog dialog,
      final boolean hor, final String[] texts, final byte[][] help) {

    // horizontal/vertical layout
    final BaseXBack panel = new BaseXBack();
    if(hor) {
      panel.setBorder(12, 0, 0, 0);
      panel.setLayout(new TableLayout(1, texts.length, 8, 0));
    } else {
      panel.setBorder(0, 0, 0, 0);
      panel.setLayout(new GridLayout(texts.length, 1, 0, 3));
    }
    for(int i = 0; i < texts.length; i++) {
      panel.add(new BaseXButton(texts[i], help[i], dialog));
    }

    final BaseXBack buttons = new BaseXBack();
    buttons.setLayout(new BorderLayout());
    buttons.add(panel, hor ? BorderLayout.EAST : BorderLayout.NORTH);
    return buttons;
  }

  /**
   * Enables/disables the OK button in the specified panel.
   * @param panel button panel
   * @param enabled enabled/disabled
   */
  public static void enableOK(final JComponent panel, final boolean enabled) {
    final Component[] jc = panel.getComponents();
    for(final Component c : jc) {
      if(!(c instanceof JComponent)) {
        continue;
      } else if(!(c instanceof BaseXButton)) {
        enableOK((JComponent) c, enabled);
      } else {
        final BaseXButton b = (BaseXButton) c;
        final String text = b.getText();
        if(!text.equals(BUTTONCANCEL)) enable(b, enabled);
      }
    }
  }

  /**
   * Sets the component width, adopting the original component height.
   * @param comp component
   * @param w width
   */
  public static void setWidth(final Container comp, final int w) {
    comp.setPreferredSize(new Dimension(w, comp.getPreferredSize().height));
  }

  /**
   * Sets the component height, adopting the original component width.
   * @param comp component
   * @param h height
   */
  public static void setHeight(final Container comp, final int h) {
    comp.setPreferredSize(new Dimension(comp.getPreferredSize().width, h));
  }

  /**
   * Sets the component size.
   * @param comp component
   * @param w width
   * @param h height
   */
  public static void setSize(final Container comp, final int w, final int h) {
    comp.setPreferredSize(new Dimension(w, h));
  }

  /**
   * Adds default notifiers for ENTER and ESCAPE to the specified component.
   * @param c the component that receives default notifications.
   * @param l the parent dialog
   */
  public static void addDefaultKeys(final Container c, final Dialog l) {
    // no dialog listener specified..
    if(l == null) return;
    // add default keys
    c.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        // process key events
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
          if(!(e.getSource() instanceof BaseXButton)) l.close();
        } else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          l.cancel();
        }
      }
    });
  }

  /**
   * Adds a help notifier to the specified component.
   * @param comp component
   * @param hlp help text
   */
  public static void addHelp(final Component comp, final byte[] hlp) {
    comp.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(final MouseEvent e) {
        GUI.get().focus(comp, hlp);
      }
    });
  }

  /**
   * Enables or disables the specified component.
   * @param comp component
   * @param enable boolean flag
   */
  public static void enable(final Container comp, final boolean enable) {
    if(comp.isEnabled() != enable) comp.setEnabled(enable);
  }

  /**
   * Selects or de-selects the specified component.
   * @param but component
   * @param select selection flag
   */
  public static void select(final AbstractButton but, final boolean select) {
    if(but.isSelected() != select) but.setSelected(select);
  }

  /**
   * Fills the specified area with a color gradient.
   * @param gg graphics reference
   * @param mode mode for filling the component
   * @param xs horizontal start position
   * @param ys vertical start position
   * @param xe horizontal end position
   * @param ye vertical end position
   */
  public static void fill(final Graphics gg, final FILL mode,
      final int xs, final int ys, final int xe, final int ye) {

    final Color col1 = mode == FILL.DOWN ? color1 : color2;
    final Color col2 = mode == FILL.DOWN ? color2 : color1;

    final int w = xe - xs;
    final int h = ye - ys;
    final int r = col1.getRed();
    final int g = col1.getGreen();
    final int b = col1.getBlue();
    final float rf = (col2.getRed() - r) / (float) h;
    final float gf = (col2.getGreen() - g) / (float) h;
    final float bf = (col2.getBlue() - b) / (float) h;

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
   * Draws a shadowed rectangle.
   * @param g graphics reference
   * @param active active flag
   * @param xs horizontal start position
   * @param ys vertical start position
   * @param xe horizontal end position
   * @param ye vertical end position
   */
  public static void rect(final Graphics g, final boolean active, final int xs,
      final int ys, final int xe, final int ye) {

    final Color col1 = active ? COLORBUTTON : Color.white;
    final Color col2 = active ? Color.white : COLORBUTTON;
    g.setColor(col1);
    g.drawLine(xs, ys, xe, ys);
    g.drawLine(xs, ys, xs, ye);
    g.setColor(col2);
    g.drawLine(xe, ys + 1, xe, ye);
    g.drawLine(xs + 1, ye, xe, ye);
  }

  /**
   * Draw the header of the scrollbar.
   * @param g graphics reference
   * @param xs horizontal start position
   * @param xe horizontal end position
   * @param ys vertical start position
   * @param ye vertical end position
   * @param high highlighting flag
   */
  public static void drawCell(final Graphics g, final int xs,
      final int xe, final int ys, final int ye, final boolean high) {

    g.setColor(high ? color6 : color5);
    g.fillRect(xs, ys, xe - xs, ye - ys);
    g.setColor(Color.white);
    g.fillRect(xs + 1, ys + 1, xe - xs - 2, ye - ys  - 2);
    fill(g, FILL.UP, xs + 2, ys + 2, xe - 2, ye - 2);
  }

  /**
   * Prints the specified text on the graphics panel.
   * @param g graphics reference
   * @param t text to be printed
   * @param p printing position
   */
  public static void textBox(final Graphics g, final String t, final Point p) {
    g.setFont(new Font(GUIProp.font, 0, GUIProp.fontsize));
    final FontMetrics fm = g.getFontMetrics();
    final int w = fm.stringWidth(t);
    final int h = fm.getHeight();
    final int x = p.x;
    final int y = p.y;

    g.setColor(GUIConstants.color6);
    g.fillRect(x - 3, y - h + 1, w + 9, h + 3);
    g.setColor(Color.white);
    g.fillRect(x - 4, y - h, w + 8, h + 2);
    g.setColor(GUIConstants.color6);
    g.drawRect(x - 4, y - h, w + 8, h + 2);
    g.drawString(t, x, y - 2);
  }

  /**
   * Enables/Disables anti-aliasing.
   * @param g graphics reference
   */
  public static void antiAlias(final Graphics g) {
    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        // JAVA 1.6 needed...
        //GUIProp.fontalias ? RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB :
        GUIProp.fontalias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON :
          RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
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
   * Draws a right aligned string to the panel.
   * @param g graphics reference
   * @param text text to be painted
   * @param w panel width
   * @param y vertical position
   */
  public static void drawRight(final Graphics g, final String text,
      final int w, final int y) {
    g.drawString(text, w - width(g, text), y);
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
   * @return width of printed string
   */
  public static int chopString(final Graphics g, final byte[] s,
      final int x, final int y, final int w) {

    if(w < 12) return w;
    final int[] cw = fontWidths(g.getFont());

    int j = s.length;
    int fw = 0;
    int l = 0;
    for(int k = 0; k < j; k += l) {
      final int ww = width(g, cw, cp(s, k));
      if(fw + ww >= w - 4) {
        j = Math.max(1, k - l);
        if(k > 1) fw -= width(g, cw, cp(s, k - 1));
        g.drawString("..", x + fw, y + GUIProp.fontsize);
        break;
      }
      fw += ww;
      l = cl(s[k]);
    }
    g.drawString(string(s, 0, j), x, y + GUIProp.fontsize);
    return fw;
  }

  /**
   * Calculates if the string fits in one line; if yes, returns the
   * horizontal start position. Otherwise, returns -1.
   * @param g graphics reference
   * @param s string to be checked
   * @param ww maximum width
   * @return result of check
   */
  public static int centerPos(final Graphics g, final byte[] s, final int ww) {
    final int[] cw = fontWidths(g.getFont());
    int sw = 0;
    final int j = s.length;
    for(int k = 0; k < j; k += cl(s[k])) {
      sw += width(g, cw, cp(s, k));
      if(sw >= ww) return -1;
    }
    return (ww - sw - 2) >> 1;
  }

  /**
   * Calculates the width of a string.
   * @param g graphics reference
   * @param s string to be checked
   * @return result of check
   */
  public static int calcWidth(final Graphics g, final byte[] s) {
    final int[] cw = fontWidths(g.getFont());
    int sw = 0;
    final int j = s.length;
    for(int k = 0; k < j; k += cl(s[k])) {
      sw += width(g, cw, cp(s, k));
    }
    return sw;
  }

  /**
   * Calculates the height of the specified text.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @return last height that was occupied
   */
  public static int calcHeight(final Graphics g, final MapRect r,
      final byte[] s) {
    return drawText(g, r, s, s.length, false);
  }

  /**
   * Draws a text.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @return last height that was occupied
   */
  public static int drawText(final Graphics g, final MapRect r,
      final byte[] s) {

    return drawText(g, r, s, s.length, true);
  }

  /**
   * Draws a text.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @param m length of text
   * @param draw draw text (otherwise: just calculate space)
   * @return last height that was occupied
   */
  public static int drawText(final Graphics g, final MapRect r,
      final byte[] s, final int m, final boolean draw) {

    // limit string to given space
    final int[] cw = fontWidths(g.getFont());
    final int fh = (int) (1.2 * GUIProp.fontsize);
    final int ws = cw[' '];
    int i = 0;
    int j = m;
    int xx = r.x;
    int yy = r.y + fh;
    int ww = r.w;
    final Color textc = g.getColor();
    boolean c = false;

    // get index on first pre value
    final int[][] ft = View.ftPos;
    //int k = ft != null && ft.length != 0 ? Array.firstIndexOf(r.p, ft[0]) :-1;
    int k = -1; // ft highlighting currently disabled...
    
    do {
      int sw = 0;
      int l = i;
      for(int n = i; n < j; n += cl(s[n])) {
        if(draw && k > -1 && k < ft[0].length &&
            i == ft[1][k] && r.p == ft[0][k]) {
           k++;
           c = true;
        }
        sw += width(g, cw, cp(s, n));
        if(sw >= ww) {
          j = Math.max(i + 1, l);
          ww = r.w;
          break;
        }
        if(s[n] == '\n') {
          j = n + 1;
          ww = r.w;
          break;
        }
        if(Token.ws(s[n])) {
          j = n + 1;
          ww -= sw;
          break;
        }
        l = n;
      }
      // draw string
      if(draw) {
        // draw word
        if(c && k > -1)  {
          g.setColor(View.ftPoi != null ?
            GUIConstants.thumbnailcolor[View.ftPoi[k]] :
              GUIConstants.COLORERROR);
          c = ww == r.w;
        } else {
          g.setColor(textc);
        }
        g.drawString(string(s, i, j - i), xx, yy);
      }
      if(ww < ws) ww = r.w;
      if(ww == r.w) {
        xx = r.x;
        yy += fh;
      } else {
        xx += sw;
      }
      i = j;
      j = m;
    } while(i < j && yy - (draw ? 0 : GUIProp.fontsize) < r.y + r.h);
    
    return yy - r.y;
  }

  /**
   * Draws a text using thumbnail visualization.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   */
  public static void drawThumbnails(final Graphics g, final MapRect r,
      final byte[] s) {

    final double xx = r.x;
    final double ww = r.w;
    final double f = 0.25 * GUIProp.fontsize;
    final int fh = (int) Math.max(1, 0.5 * GUIProp.fontsize);
    final int lh = (int) Math.max(1, 0.8 * GUIProp.fontsize);
    final double ys = r.y + 3;
    double yy = ys;

    int wl = 0; // word length
    int ll = 0; // line length

    // get index on first pre value
    final int[][] ft = View.ftPos;
    //int k = ft != null && ft.length != 0 ? Array.firstIndexOf(r.p, ft[0]) :-1;
    int k = -1; // ft highlighting currently disabled...

    final Color textc = g.getColor();
    boolean c = false;

    // including i == s.length in loop to draw last string...
    for(int i = 0; i <= s.length; i++) {
      if(k > -1 && k < ft[0].length && i == ft[1][k] && r.p == ft[0][k]) {
         k++;
         c = true;
      }

      if(i == s.length || Token.ws(s[i])) {
        // check if rectangle fits in line and if it's not the first word
        if(f * (ll + wl) > ww && ll != 0) {
          yy += lh;
          ll = 0;
        }
        if(yy + lh >= r.y + r.h) return;

        // draw word
        if(c && k > -1)  {
          g.setColor(View.ftPoi != null ?
            GUIConstants.thumbnailcolor[View.ftPoi[k]] :
              GUIConstants.COLORERROR);
          c = false;
        } else {
          g.setColor(textc);
        }

        final int xw = (int) Math.min(ww - f * ll - 4, f * wl);
        g.fillRect((int) (xx + f * ll), (int) yy, xw, fh);
        ll += wl;

        if (i < s.length && s[i] == '\n' || ll + 1 >= ww) {
          yy += lh;
          ll = 0;
        } else {
          ll++;
        }
        wl = 0;
      } else {
        // add to current word length
        wl += cl(s[i]);
      }
    }
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
    for(int k = 0; k < l; k += cl(s[k])) fw += width(g, cw, cp(s, k));
    return fw;
  }

  /**
   * Returns the character width of the specified character.
   * @param g graphics reference
   * @param cw character array
   * @param c character
   * @return character width
   */
  private static int width(final Graphics g, final int[] cw, final int c) {
    return c > 255 ? g.getFontMetrics().charWidth(c) : cw[c];
  }
}
