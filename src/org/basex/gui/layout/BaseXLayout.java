package org.basex.gui.layout;

import static org.basex.Text.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import org.basex.BaseX;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.dialog.Dialog;
import org.basex.gui.view.ViewRect;
import org.basex.index.FTTokenizer;
import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.Performance;

/**
 * This class assembles layout and paint methods which are frequently
 * used in the GUI.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXLayout {
  /** Date Format. */
  public static final SimpleDateFormat DATE =
    new SimpleDateFormat("dd.MM.yyyy hh:mm");
  /** Private constructor. */
  private BaseXLayout() { }
  /** Count number of digit/char bytes. */
  private static int cchars = 0;

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
   * Enables/disables a button in the specified panel.
   * @param panel button panel
   * @param label button label
   * @param enabled enabled/disabled
   */
  public static void enableOK(final JComponent panel, final String label,
      final boolean enabled) {
    final Component[] jc = panel.getComponents();
    for(final Component c : jc) {
      if(!(c instanceof JComponent)) {
        continue;
      } else if(!(c instanceof BaseXButton)) {
        enableOK((JComponent) c, label, enabled);
      } else {
        final BaseXButton b = (BaseXButton) c;
        if(b.getText().equals(label)) enable(b, enabled);
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
   * Returns the value of the specified pre value and attribute.
   * @param val value to be evaluated
   * @param size size flag
   * @param date date flag
   * @return value as string
   */
  public static String value(final double val, final boolean size,
      final boolean date) {

    if(size) return Performance.format((long) val, true);
    if(date) return DATE.format(new Date((long) val * 60000));
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
   * Draw the header of the scrollbar.
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
   * Enables/Disables anti-aliasing.
   * @param g graphics reference
   */
  public static void antiAlias(final Graphics g) {
    Object hint = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;

    if(GUIProp.fontalias) {
      // Check out Java 1.6 rendering; if not available, use default rendering
      try {
        final Class<?> rh = RenderingHints.class;
        hint = rh.getField("VALUE_TEXT_ANTIALIAS_" + GUIProp.fontaa).get(null);
      } catch(final Exception e) {
        hint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
      }
    }
    final Graphics2D gg = (Graphics2D) g;
    gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, hint);
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
    final int tw = BaseXLayout.width(g, tt);
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
   * @return width of printed string
   */
  public static int chopString(final Graphics g, final byte[] s,
      final int x, final int y, final int w) {

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
          g.drawString("..", x + fw, y + GUIProp.fontsize);
          break;
        }
        fw += ww;
        l = cl(s[k]);
      }
    } catch(final Exception ex) {
      BaseX.debug(ex);
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
   * Calculates the height of the specified text.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @return last height that was occupied
   */
  public static int calcHeight(final Graphics g, final ViewRect r,
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
  public static int drawText(final Graphics g, final ViewRect r,
      final byte[] s) {
    return drawTextNew(g, r, s, s.length, true);
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
  public static int drawText(final Graphics g, final ViewRect r,
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

    // get index on first pre value
    int count = 0;
    int pp = 0;
    
    do {
      int sw = 0;
      int l = i;
      for(int n = i; n < j; n += cl(s[n])) {
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
        if(ws(s[n])) {
          j = n + 1;
          ww -= sw;
          break;
        }
        l = n;
      }
      // draw string
      if(draw) {
          if (r.pos != null && pp < r.pos.length && count == r.pos[pp]) {
            pp++;
            g.setColor(COLORERROR);
          } else 
          g.setColor(textc);
        g.drawString(string(s, i, j - i), xx, yy);
        count++;
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
   * Draws a text.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @param m length of text
   * @param draw draw text (otherwise: just calculate space)
   * @return last height that was occupied
   */
  public static int drawTextNew(final Graphics g, final ViewRect r,
      final byte[] s, final int m, final boolean draw) {

    // limit string to given space
    final int[] cw = fontWidths(g.getFont());
    final int fh = (int) (1.2 * GUIProp.fontsize);
    int xx = r.x;
    int yy = r.y + fh;
    int ww = r.w;
    final Color textc = g.getColor();
    byte[] tmp;
    if (s.length > m) {
      tmp = new byte[m];
      System.arraycopy(s, 0, tmp, 0, m);
    } else tmp = s;
    final FTTokenizer ftt = new FTTokenizer(tmp);
    
    // get index on first pre value
    int count = 0;
    int pp = 0;
    
    int ll = 0;
    int cp = 0;
    final int we = width(g, cw, ' ');
    while(ftt.more()) {
      if (cp < ftt.para) {
        cp = ftt.para;
        xx = r.x;
        yy += fh;
        ll = 0;
        if (yy >= r.y + r.h)
          return yy - r.y;        
      }

      final byte[] tok = ftt.get();
      int wl = 0;
      
      for(int n = 0; n < tok.length; n += cl(tok[n])) 
        wl += width(g, cw, cp(tok, n));
//      for (byte b : tok) wl += width(g, cw, cp(s, n));
      if (ll + wl + we >= ww) { 
        xx = r.x;
        yy += fh;
        ll = 0;
        if (yy >= r.y + r.h)
          return yy - r.y;
      }
      if(draw) {
        if (r.pos != null && pp < r.pos.length && count == r.pos[pp]) {
          g.setColor(thumbnailcolor[r.poi[pp]]);
          pp++;          
        } else g.setColor(textc);
        g.drawString(new String(tok), xx + ll, yy);
        count++;
      }
      ll += wl + we;
    }
    
    return yy - r.y;
  }

  /**
   * Draws a text using thumbnail visualization.
   * Calculates the needed space and chooses an abstraction level.
   * Token/Sentence/Paragraphs
   * 
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   */
  public static void drawThumbnails(final Graphics g, final ViewRect r,
      final byte[] s) {
    int al = 0; 
    // thumbnail width
    final double ffmax = 0.25;
    final double ffmin = 0.14;
    // thumbnail height
    final double ffhmax = 0.5;
    // empty line height
    final double flhmax = 0.3; //0.8;
    // space width
    double sw;
    
    double ff = ffmax, ffh = ffhmax, flh = flhmax;
    double fmi = ff * GUIProp.fontsize;
    int lhmi = (int) Math.max(3, ffh * GUIProp.fontsize);
    int fhmi = (int) Math.max(6, (flh + ffh) * GUIProp.fontsize);
    int h = r.h; 
    final double fac = Math.exp(Math.log(s.length) * 0.97) / s.length;
    double f = ff * GUIProp.fontsize;
    int fh, lh;
  
    while (al < 4) {
      ff *= fac; //0.97;
      f = ff * GUIProp.fontsize;
      ffh *= fac;
      fh = (int) Math.max(1, ffh * GUIProp.fontsize);
      flh *= fac;
      lh = (int) Math.max(1, (flh + ffh) * GUIProp.fontsize);
      sw = Math.max(f * 0.5, ffmin * GUIProp.fontsize);
      switch(al) {
        case 0: 
          h = drawThumbnailsToken(g, r, s, f, fh, lh, sw, false);
          break;
        case 1: 
          h = drawThumbnailsSentence(g, r, s, true, f, fh, lh, sw, false); 
          break;
        case 2: 
          h = drawThumbnailsSentence(g, r, s, false, f, fh, lh, sw, false);  
      }
      
      if (ff < ffmin) {
        if ((cchars * ffmin * GUIProp.fontsize  / r.w) * (fh + 1) > r.h) {
          drawThumbnailsSentenceComp(g, r, s, 
              ffmin * GUIProp.fontsize, fh, true);
          return;
        }         

        al++;
        fmi = f;
        fhmi = fh;
        lhmi = lh;
        ff = ffmax;
        ffh = ffhmax;
        flh = flhmax;
      } else if (h < r.h) {
        // thumbnail fits in rec
        switch(al) {
          case 0: 
            drawThumbnailsToken(g, r, s, f, fh, lh, sw, true); 
            return;
          case 1: 
            drawThumbnailsSentence(g, r, s, true, f, fh, lh, sw, true); 
            return;
          case 2: 
            drawThumbnailsSentence(g, r, s, false, f, fh, lh, sw, true); 
            return;
        }
      } else if (al > 2) {
        // reduce space between each line further
        f = ffmin * GUIProp.fontsize;
        while (h >= r.h && flh + ffh >= 1)  {
          flh *= 0.97;
          lhmi = (int) Math.max(1, (flh + ffh) * GUIProp.fontsize);
          h = drawThumbnailsSentence(g, r, s, false, f, fhmi, lhmi, sw, 
              false);
        }
        
        if (h < r.h) {
          if (lhmi < fhmi) lhmi = fhmi;
          drawThumbnailsSentence(g, r, s, false, f, fhmi, lhmi, sw, true);
        } else {
          drawThumbnailsSentenceComp(g, r, s, f, fhmi, true);
        }
        return;
      }
    }
    drawThumbnailsSentence(g, r, s, false, fmi, fhmi, lhmi, 
        Math.max(f * 0.5, ffmin * GUIProp.fontsize), true);
  }
  
  /**
   * Draws a text using thumbnail visualization.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @param f length of a thumbnailtoken
   * @param fh higth of a thumbnailtoken
   * @param lh higth of an empty line
   * @param sw length of a space
   * @param draw boolean for drawing (used for calculating the higth)
   * @return higths 

   */
  public static int drawThumbnailsToken(final Graphics g, final ViewRect r,
      final byte[] s, final double f, final int fh, final int lh, 
      final double sw, final boolean draw) {
    final double xx = r.x;
    final double ww = r.w;
    final int ys = r.y + 3;
    int yy = ys;

    int wl = 0; // word length
    int ll = 0; // line length

    final FTTokenizer ftt = new FTTokenizer(s);
    final Color textc = g.getColor();
    int count = 0;
    int pp = 0;
    int cs = 0;
    int cp = 0;
    int ftts;
    cchars = 0;

    while(ftt.more()) {
      if (cs < ftt.sent) cs = ftt.sent;
      if (cp < ftt.para) {
        yy += lh;
        ll = 0;
        cp = ftt.para;
      } 

      ftts = ftt.s;
      wl = ftt.p - ftts;
      cchars += wl;
      // check if rectangle fits in line
      if (f * (ll + wl) > ww) {
        yy += lh;
        ll = 0;
      } 
      
//      if(yy + lh >= r.y + r.h) return yy + lh - r.y;
      if (draw) {
        // draw word
        if (r.pos != null && pp < r.pos.length && count == r.pos[pp]) {
          g.setColor(thumbnailcolor[r.poi[pp]]);
          pp++;          
        } else 
          g.setColor(textc);
  
        final int xw = (int) Math.min(ww - f * ll - 4, f * wl);
        g.fillRect((int) (xx + f * ll), yy, xw, fh);
      }
      ll += wl;
      count++;
      ll += sw; //f * 0.5;
      wl = 0;
    }
    return yy - r.y;
  }
  
  
  /**
   * Draws a text using thumbnail visualization.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @param sen flag for sentence or paragraphe
   * @param f length of a thumbnailtoken
   * @param fh higth of a thumbnailtoken
   * @param lh higth of an empty line
   * @param sw length of a space
   * @param draw boolean for drawing (used for calculating the higth)
   * @return higths 
   */
  public static int drawThumbnailsSentence(final Graphics g, 
      final ViewRect r, final byte[] s, final boolean sen, 
      final double f, final int fh, final int lh, 
      final double sw, final boolean draw) {
    final double xx = r.x;
    final double ww = r.w;
    final int ys = r.y + 3;
    int yy = ys;

    int wl = 0; // word length
    int ll = 0; // line length

    final FTTokenizer ftt = new FTTokenizer(s);
    final Color textc = sen && fh == lh ? g.getColor() : GUIConstants.COLORS[6];
    int count = -1;
    int pp = 0;
    int cp = 0;
    int cs = 0;
    boolean m = ftt.more();
    int lastl = 0;
    
    while (m) {
      while (ll + wl < ww &&
          (cs == ftt.sent && sen || cp == ftt.para && !sen) && 
          (r.pos == null || (pp < r.pos.length && count < r.pos[pp]) 
              || pp == r.pos.length)) {
        lastl = (int) ((ftt.p - ftt.s) * f);
        wl += lastl;
        
        count++;
        m = ftt.more();
        if (!m) break;        
      }
      
      // doesn't fit in line
      if (ll + wl >= ww) { 
        final int fp = (int) (ww - ll);
        if (fp <= f) {
//          if(yy + lh  + 3 >= r.y + r.h) return yy + lh + 3 - r.y;
          yy += lh;
          ll = 0;
        } else {
          final int sp = wl - fp;
          // draw first part of the sentence
          g.setColor(textc);
          if (draw) g.fillRect((int) (xx + ll), yy, (int) (fp - f), fh);
          ll += fp - f;
          // color last rect of first part of the word black
          if (draw) {
            g.setColor(new Color(0, 0, 0));
            g.fillRect((int) (xx + ll), yy, (int) f, fh);
            g.setColor(textc);
          }
//          if(yy + lh + 3 >= r.y + r.h) return yy + lh + 3 - r.y;
          yy += lh;
          ll = 0;
          // color first rec of second part of the word black
          if (draw) {
            g.setColor(new Color(0, 0, 0));
            g.fillRect((int) xx, yy, (int) f, fh);
            g.setColor(textc);
          }
          wl = sp;
          ll = (int) f;
        } 
      }
      
      // color thumbnail because of fulltext hint
      if (r.pos != null && pp < r.pos.length && count == r.pos[pp]) {
        if (lastl > 0) {
          if (draw) g.fillRect((int) (xx + ll), yy, wl - lastl, fh);
          ll += wl - lastl;
          wl = lastl;
        }        
        if (draw) g.setColor(thumbnailcolor[r.poi[pp]]);
        pp++;
      } 
      
      if (wl + ll < ww) {
        if (draw) {
          g.fillRect((int) (xx + ll), yy, wl, fh);
          g.setColor(textc);
        }
        ll += wl;
        wl = 0;
      }
      
      // begin new line / new sentence
      if (cs < ftt.sent && sen || cp < ftt.para && !sen) {
        // new sentence
        if (sen) {
          ll += sw; //f;
          cs = ftt.sent;
        }
        if (cp < ftt.para) {
          cp = ftt.para;
//          if(yy + lh + 3 >= r.y + r.h) return yy + lh + 3 - r.y;
          yy += lh;
          wl = 0;
          ll = 0;
        }
      }      
    }
    return yy - r.y;
  }

  /**
   * Draws a text using thumbnail visualization. Each sentence within
   * a fulltext hit will be colored. This is a very compact and efficente
   * visualization for large textnodes.
   * 
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @param f length of a thumbnailtoken
   * @param fh higth of a thumbnailtoken
   * @param draw boolean for drawing (used for calculating the higth)
   * @return used high   
   */
  public static int drawThumbnailsSentenceComp(final Graphics g, 
      final ViewRect r, final byte[] s, final double f, final int fh, 
      final boolean draw) {
    final int xx = r.x;
    final int ww = r.w;
    int yy = r.y + 3;
    int wl = 0; // word length
    int ll = 0; // line length

    final FTTokenizer ftt = new FTTokenizer(s);
    final Color textc = GUIConstants.COLORS[7];
    g.setColor(textc);
    int count = -1;
    int pp = 0;
    int cs = 0;
    int tl;
    IntList col;
    IntList tokl;
    int cc;
    boolean m = ftt.more();
    
    while (m) {
      col = new IntList();
      tokl = new IntList();
      tokl.add(0);
      cc = 0;
      while (cs == ftt.sent && 
          (r.pos == null || (pp < r.pos.length && count < r.pos[pp]) 
              || pp == r.pos.length)) {
        tl = (int) ((ftt.p - ftt.s) * f);
        wl += tl; 
        count++;
        if (draw && r.pos != null && pp < r.pos.length 
            && count == r.pos[pp]) {  
          col.add(r.poi[pp++]);
          tokl.add(tl);
          tokl.list[0] += tl;
        }
        m = ftt.more();
        if (!m) break;
      }
      cs++;
      
      int l = 0;
      int[] sizes = getThumbnailLength(tokl, wl);
      while (l < wl) {
        int tw = sizes[cc]; //col.size == 0 ? wl : (wl / col.size) + 1;
        while (ll + tw > ww) {
          if (draw) {
            if (col.size > 0) g.setColor(thumbnailcolor[col.list[cc]]); 
            g.fillRect(xx + ll, yy, ww - ll, fh);
          }
          l += ww - ll;
          tw -= ww - ll;
          ll = 0;
          yy += fh + 1;
          if (yy + 3 >= r.y + r.h) return yy - r.y; 
        }
        if (draw) {
          if (col.size > 0) 
            g.setColor(thumbnailcolor[col.list[cc]]);
          
          g.fillRect(xx + ll, yy, tw, fh);
          g.setColor(textc);
        }
        ll += tw;
        l += tw;
        if (col.size > 0) cc++;
      }
      wl = 0;
    }    
    return yy - r.y;
  }

  /**
   * Calculates the length of a thumbnail.
   * @param il IntList with tokens length
   * @param wl total length of the thumbnail
   * @return int[] length
   */
  private static int[] getThumbnailLength(final IntList il, final int wl) {
    if (il.size == 1) return new int[] {wl};
    int[] i = new int[il.size - 1];
    for (int j = 0; j < i.length; j++) 
      i[j] = (il.list[j + 1] * wl / il.list[0]) + 1;  
    return i;
  }
  

  /**
   * Draws a text token within its context.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   */
  public static void drawTextinContext(final Graphics g, final ViewRect r,
      final byte[] s) {

    // limit string to given space
    final int[] cw = fontWidths(g.getFont());
    final int fh = (int) (1.2 * GUIProp.fontsize);
    int xx = r.x;
    int yy = r.y + fh;
    final Color textc = g.getColor();

    int count = 0;
    int pp = 0;
    int sw = 0;
    IntList poic = new IntList();
    final int wd = width(g, cw, '.');
    final int we = width(g, cw, ' ');
    final String e = new String(" ");
    final String d = new String(".");
    FTTokenizer ftt = new FTTokenizer(s);
    while(ftt.more()) {
      if (r.pos != null && pp < r.pos.length && count == r.pos[pp]) {
        sw += sw == 0 ? we + wd * 2 : we + wd;
        if (sw > r.w) {
          yy += fh;
          xx = r.x;
          sw = 2 * we;
          if (yy >= r.y + r.h) return;
        }
        g.drawString(sw == we + wd * 2 ? d + d + e : d + e, xx, yy);
        xx = r.x + sw;
          
        byte[] tok = new byte[0];
        byte[] t = null;
        int c = 0;
        while(pp + c + 1 < r.poi.length && 
            r.poi[pp + 1] == r.poi[pp + c + 1] && 
            r.pos[pp] == r.pos[pp + c] - c) {
          if(t != null) {
            ftt.more();
            count++;
          }
          t = ftt.get();
          for (byte b : t)
            sw += width(g, cw, b);
          sw += we;
          if (sw > r.w) {
            yy += fh;
            xx = r.x;
            sw = 2 * we;
            if (yy >= r.y + r.h) return;
          }
          tok = Array.add(tok, Array.add(t, new byte[]{' '}));
          c++;          
        }
        sw -= we;
        g.setColor(thumbnailcolor[r.poi[pp]]);
        pp  = pp + c;
        g.drawString(new String(tok), xx, yy);
        g.setColor(textc);
        if(!poic.contains(r.poi[pp])) poic.add(r.poi[pp]);
        xx = r.x + sw;
        int k = 0;
        int ll;
        while (k < 2 && ftt.more()) {
          ll = sw;
          count++;
          if (r.pos != null && pp < r.pos.length && count == r.pos[pp]) {
            g.setColor(thumbnailcolor[r.poi[pp]]);
            pp++;
            if(!poic.contains(r.poi[pp])) poic.add(r.poi[pp]);
          } else g.setColor(textc);
          sw += we;
          if (sw > r.w) {
            yy += fh;
            xx = r.x;
            sw = 2 * we;
            if (yy >= r.y + r.h) return;
          }
          tok = ftt.get();
          for (byte b : tok)
            sw += width(g, cw, b);
          if (sw > r.w) {
            yy += fh;
            xx = r.x;
            sw = 2 * we;
            if (yy >= r.y + r.h) return;
          }
          g.drawString(e + new String(tok), xx, yy);
          g.setColor(textc);
          xx += sw - ll;
          k++;
        }
        sw += we + wd;
        if (sw > r.w) {
          yy += fh;
          xx = r.x;
          sw = 2 * we;
          if (yy >= r.y + r.h) return;
        }
        g.drawString(e + d, xx, yy);
        xx = r.x + sw;
        
        if (r.poi != null && r.poi[0] == poic.size) {
          g.drawString(d, xx, yy);
          yy += fh;
          xx = r.x;
          sw = 0;
          poic = new IntList();
        }
        if (yy >= r.y + r.h) return;
        
      }
      count++;
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
    try {
      // ignore faulty character sets
      for(int k = 0; k < l; k += cl(s[k])) fw += width(g, cw, cp(s, k));
    } catch(final Exception ex) {
      BaseX.debug(ex);
    }
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
