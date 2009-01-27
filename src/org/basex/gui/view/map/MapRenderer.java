package org.basex.gui.view.map;

import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;
import java.awt.Color;
import java.awt.Graphics;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.ViewRect;
import org.basex.index.FTTokenizer;

/**
 * This class assembles utility methods for painting rectangle contents.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class MapRenderer {
  /** Count number of digit/char bytes. */
  private static int cchars = 0;

  /** Private constructor. */
  private MapRenderer() { }

  /**
   * Calculates the height of the specified text.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @return last height that was occupied
   */
  static int calcHeight(final Graphics g, final ViewRect r,
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
  static int drawText(final Graphics g, final ViewRect r,
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
  static int drawText(final Graphics g, final ViewRect r,
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
        sw += BaseXLayout.width(g, cw, cp(s, n));
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
  private static int drawTextNew(final Graphics g, final ViewRect r,
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
    final int we = BaseXLayout.width(g, cw, ' ');
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
        wl += BaseXLayout.width(g, cw, cp(tok, n));
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
  static void drawThumbnails(final Graphics g, final ViewRect r,
      final byte[] s) {
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
    int lhmi = (int) Math.max(3, ffh * GUIProp.fontsize);
    int fhmi = (int) Math.max(6, (flh + ffh) * GUIProp.fontsize);
    int h = r.h;
    final double fac = Math.exp(Math.log(s.length) * 0.97) / s.length;
    double f = ff * GUIProp.fontsize;
    int fh, lh, al = 0;
    FTTokenizer ftt = new FTTokenizer(s);
    final int[][] data = ftt.getInfo();
    
    while(al < 4) {
      ff *= fac; //0.97;
      f = ff * GUIProp.fontsize;
      ffh *= fac;
      fh = (int) Math.max(1, ffh * GUIProp.fontsize);
      flh *= fac * fac;
      lh = (int) Math.max(1, (flh + ffh) * GUIProp.fontsize);
      sw = f; //Math.max(f * 0.5, 1.5);
      switch(al) {
        case 0:
          h = drawThumbnailsToken(g, r, data, f, fh, lh, f, false);
          break;
        case 1:
          h = drawThumbnailsSentence(g, r, data, true, f, fh, lh, sw, false);
          break;
        case 2:
          h = drawThumbnailsSentence(g, r, data, false, f, fh, lh, sw, false);
          break;
      }
      
      if (ff < ffmin) {
        // chance al
        al++;
        fhmi = fh;
        lhmi = lh;
        ff = ffmax;
        ffh = ffhmax;
        flh = flhmax;
      } else if (h < r.h) {
        // thumbnail fits in rec
        switch(al) {
          case 0:
            drawThumbnailsToken(g, r, data, f, fh, lh, f, true);
            return;
          case 1:
            drawThumbnailsSentence(g, r, data, true, f, fh, lh, sw, true);
            return;
          case 2:
            drawThumbnailsSentence(g, r, data, false, f, fh, lh, sw, true);
            return;
        }
      } 
    }
    
    int sum = data[1].length + data[2][0];
    for (int i = 1; i < data[2].length; i++) sum += data[2][i];
    int nl = (r.h - 3) / lhmi;
    double fnew = (double) (nl * r.w) / (double) sum;
    drawThumbnailsSentence(g, r, data, false, fnew, fhmi, lhmi, 
        Math.max(1.5, fnew), true);
    
  }
  
  
  /**
   * Draws a text using thumbnail visualization.
   * @param g graphics reference
   * @param r rectangle
   * @param data fulltext to be drawn
   * @param f length of a thumbnailtoken
   * @param fh higth of a thumbnailtoken
   * @param lh higth of an empty line
   * @param sw length of a space
   * @param draw boolean for drawing (used for calculating the higth)
   * @return higths
   */
  private static int drawThumbnailsToken(final Graphics g, final ViewRect r,
      final int[][] data, final double f, final int fh, final int lh,
      final double sw, final boolean draw) {
    final double xx = r.x;
    final double ww = r.w;
    final int ys = r.y + 3;
    int yy = ys;

    int wl = 0; // word length
    int ll = 0; // line length
    
    final Color textc = draw ? GUIConstants.COLORS[6] : null; //g.getColor();
    int count = 0;
    int pp = 0;
    int sl = 0, pl = 0;
    int psl = 0, ppl = 0;
    cchars = 0;

    for (int i = 0; i < data[0].length; i++) {
      wl = (int) (data[0][i] * f);
      sl += data[0][i];
      pl += data[0][i];
      cchars += data[0][i];
      // check if rectangle fits in line
      if (ll + wl + 
          sw * ((psl < data[1].length && pl == data[1][psl]) ? 1 : 0) > ww) {
        yy += lh;
        ll = 0;
      }

      if (draw) {
        // draw word
        if (r.pos != null && pp < r.pos.length && count == r.pos[pp]) {
          g.setColor(thumbnailcolor[r.poi[pp]]);
          pp++;
        } else
          g.setColor(textc);
          g.fillRect((int) (xx + ll), yy, wl, fh);
      }
      ll += wl;
      count++;

      if (draw && psl < data[1].length && sl == data[1][psl]) {
        // new sentence, draw dot
        g.setColor(Color.black);
        g.fillRect((int) (xx + ll), yy, (int) sw, fh);
        ll += sw;
        g.setColor(textc);
        psl++;
        sl = 0;
      }

      ll += sw;
      wl = 0;

      
      if (ppl < data[2].length && pl == data[2][ppl]) {
        // new paragraph
        yy += lh;
        ll = 0;
        ppl++;
        pl = 0;
      }
    }
    return yy - r.y + 3;
  }

  
  
  /**
   * Draws a text using thumbnail visualization.
   * @param g graphics reference
   * @param r rectangle
   * @param data fulltext to be drawn
   * @param sen flag for sentence or paragraphe
   * @param f length of a thumbnailtoken
   * @param fh higth of a thumbnailtoken
   * @param lh higth of an empty line
   * @param sw length of a space
   * @param draw boolean for drawing (used for calculating the higth)
   * @return higths
   */
  private static int drawThumbnailsSentence(final Graphics g,
      final ViewRect r, final int[][] data, final boolean sen,
      final double f, final int fh, final int lh,
      final double sw, final boolean draw) {
    final double xx = r.x;
    final double ww = r.w;
    final int ys = r.y + 3;
    int yy = ys;
    
    int wl = 0; // word length
    int ll = 0; // line length

    final Color textc = GUIConstants.COLORS[6];
    g.setColor(textc);
    int lastl = 0;
    int count = -1;
    int pp = 0;
    int sl = 0, pl = 0;
    int psl = 0, ppl = 0;
    
    int i = 0;
    while (i < data[0].length) {
      final int io = i;
      while (ll + wl < ww && i < data[0].length && 
          psl < data[1].length && ppl < data[2].length &&
          data[1][psl] > sl && data[2][ppl] > pl &&
          (r.pos == null || (pp < r.pos.length && count < r.pos[pp])
              || pp == r.pos.length)) {
        sl += data[0][i];
        pl += data[0][i];
        lastl = (int) (data[0][i] * f);
        wl += lastl;
        count++;
        if (i < data[0].length) i++;
        else break;
      }
      if (io == i) i++;

      // doesn't fit in line
      if (ll + wl >= ww) {
        final int fp = (int) (ww - ll);
        if (fp <= f) {
          yy += lh;
          ll = 0;
        } else {
          final int sp = wl - fp;
          // draw first part of the sentence
          if (draw) {
            g.setColor(textc);
            g.fillRect((int) (xx + ll), yy, fp, fh);
          }
          yy += lh;
          ll = 0;
          wl = sp;
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

      // new sentence
      if (psl < data[1].length && data[1][psl] == sl) {
        if (ll + sw >= ww) {
          yy += lh;
          ll = 0;
        }

        if (draw) {
          g.setColor(Color.black);
          g.fillRect((int) (xx + ll), yy, (int) sw, fh);
          g.setColor(textc);
        }
        ll += sw; //f;
        sl = 0;
        psl++;
      }

      // new paragraph
      if (ppl < data[2].length && data[2][ppl] == pl) {
        pl = 0;
        ppl++;
        if (sen) {
          yy += lh;
          wl = 0;
          ll = 0;
        }
      }
    }
    
    return yy - r.y + 3;
  }

  /**
   * Draws a text token within its context.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
  static void drawTextinContext(final Graphics g, final ViewRect r,
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
   */
}
