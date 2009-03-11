package org.basex.gui.view.map;

import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;
import java.awt.Color;
import java.awt.Graphics;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXLayout;
import org.basex.index.FTTokenizer;
import org.basex.util.IntList;
import org.basex.util.TokenList;

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
  static int calcHeight(final Graphics g, final MapRect r, final byte[] s) {
    return drawText(g, r, s, s.length, false);
  }

  /**
   * Draws a text.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @return last height that was occupied
   */
  static int drawText(final Graphics g, final MapRect r, final byte[] s) {
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
  static int drawText(final Graphics g, final MapRect r,
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
  private static int drawTextNew(final Graphics g, final MapRect r,
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
    int ls = 0;
    while(ftt.more()) {
      if (ls < ftt.sent) {
        ls++;
        final int w = BaseXLayout.width(g, cw, (byte) ftt.lastpm);
        if (xx + ll +  w > ww) {
          xx = r.x;
          yy += fh;
          ll = 0;
        }
        if (draw) {
          g.drawString(new String(new byte[] {(byte) ftt.lastpm}),
              xx + ll - (xx > we ? we : 0), yy);
        }
//        ll += w;
      }

      if (cp < ftt.para) {
        cp = ftt.para;
        xx = r.x;
        yy += fh;
        ll = 0;
        if (yy >= r.y + r.h)
          return yy - r.y;
      }

      byte[] tok = ftt.get();
      int wl = 0;

      for(int n = 0; n < tok.length; n += cl(tok[n]))
        wl += BaseXLayout.width(g, cw, cp(tok, n));
      if (ll + wl + we >= ww) {
        xx = r.x;
        yy += fh;
        ll = 0;
        if (yy >= r.y + r.h)
          return yy - r.y;
        if(draw && wl + we >= ww) {
          // single word is to long for the rectangle
          int twl = 0;
          twl = 2 * BaseXLayout.width(g, cw, '.');
          if (we + twl < ww) {
            int l, n = 0;
            for(; n < tok.length; n += cl(tok[n])) {
              l = BaseXLayout.width(g, cw, cp(tok, n));
              if (twl + l + we >= ww) break;
              twl += l;
            }
            final byte[] ntok = new byte[n + 2];
            System.arraycopy(tok, 0, ntok, 0, n);
            ntok[n] = '.';
            ntok[n + 1] = '.';
            tok = ntok;
            wl = twl;
          } else {
            tok = new byte[0];
            wl = 0;
          }
        }
      }
      if(draw) {
        if (r.pos != null && pp < r.pos.length && count == r.pos[pp]) {
          g.setColor(getFTColor(r.poi[pp], r.acol));
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
  static void drawThumbnails(final Graphics g, final MapRect r,
      final byte[] s) {

    // thumbnail width
    final double ffmax = 0.25;
    final double ffmin = 0.14;
    // thumbnail height
    final double ffhmax = 0.5;
    // empty line height
    final double flhmax = 0.3; //0.8;
    // space width
    //double sw;

    double ff = ffmax, ffh = ffhmax, flh = flhmax;
    byte lhmi = (byte) Math.max(3, ffh * GUIProp.fontsize);
    byte fhmi = (byte) Math.max(6, (flh + ffh) * GUIProp.fontsize);

    int h = r.h;
    final double fac = Math.exp(Math.log(s.length) * 0.97) / s.length;
    r.thumbf = ff * GUIProp.fontsize;
    r.thumbal = 0;

    FTTokenizer ftt = new FTTokenizer(s);
    final int[][] data = ftt.getInfo();


    while(r.thumbal < 4) {
      ff *= fac; //0.97;
      r.thumbf = ff * GUIProp.fontsize;
      ffh *= fac;
      r.thumbfh = (byte) Math.max(1, ffh * GUIProp.fontsize);
      flh *= fac * fac;
      r.thumblh = (byte) Math.max(1, (flh + ffh) * GUIProp.fontsize);
      //sw = f; //Math.max(f * 0.5, 1.5);
      switch(r.thumbal) {
        case 0:
          h = drawThumbnailsToken(g, r, data, false, 0, 0);
          break;
        case 1:
          h = drawThumbnailsSentence(g, r, data, true, r.thumbf, false);
          break;
        case 2:
          h = drawThumbnailsSentence(g, r, data, false, r.thumbf, false);
          break;
      }

      if (ff < ffmin) {
        // chance al
        r.thumbal++;
        fhmi = r.thumbfh;
        lhmi = r.thumblh;
        ff = ffmax;
        ffh = ffhmax;
        flh = flhmax;
      } else if (h < r.h) {
        // thumbnail fits in rec
        r.thumbal = r.thumbal;
        switch(r.thumbal) {
          case 0:
            drawThumbnailsToken(g, r, data, true, 0, 0);
            return;
          case 1:
            drawThumbnailsSentence(g, r, data, true, r.thumbf, true);
            return;
          case 2:
            drawThumbnailsSentence(g, r, data, false, r.thumbf, true);
            return;
        }
      } else r.thumbal++;
    }

    int sum = data[2][0];
    for (int i = 1; i < data[2].length; i++) sum += data[2][i];
    int nl = (r.h - 6) / lhmi + 1;
    double fnew = (double) (nl * r.w) / (double) sum;
    r.thumbf = fnew;
    r.thumbfh = fhmi;
    r.thumblh = lhmi;
    h = drawThumbnailsSentence(g, r, data, false, Math.max(1.5, fnew), false);
    if (h < r.h)
    drawThumbnailsSentence(g, r, data, false, Math.max(1.5, fnew), true);
  }

  /** Color for each tooltip token.  */
  static IntList ttcol;
  /** Tooltip tokens. */
  static TokenList tl;
  /** Index of tooltip token to underline. */
  private static int ul;

  /**
   * Draws a text using thumbnail visualization.
   * @param g graphics reference
   * @param r rectangle
   * @param data fulltext to be drawn
   * @param x x-value of the cursor
   * @param y y-value of the cursor
   * @param draw boolean for drawing (used for calculating the higth)
   * @return heights
   */
  private static int drawThumbnailsToken(final Graphics g, final MapRect r,
      final int[][] data, final boolean draw, final int x, final int y) {
    final double xx = r.x;
    final double ww = r.w;
    final int ys = r.y + 3;
    int yy = ys;

    int wl = 0; // word length
    int ll = 0; // line length

    final Color textc = draw ? COLORS[r.level + 4] : null;
    int count = 0;
    int pp = 0;
    int sl = 0, pl = 0;
    int psl = 0, ppl = 0;
    cchars = 0;
    ttcol = new IntList();
    tl = new TokenList();
    int ml = 0;
    for (int i = 0; i < data[0].length; i++) {
      wl = (int) (data[0][i] * r.thumbf);
      sl += data[0][i];
      pl += data[0][i];
      cchars += data[0][i];
      // check if rectangle fits in line - don't split token and dot
      if (ll + wl + r.thumbf * ((psl < data[1].length
          && pl == data[1][psl]) ? 1 : 0) >= ww) {
        yy += r.thumblh;
        ll = 0;
        ml = 0;
        if (wl >= ww) return r.h + 3;
      }

      if (draw) {
        // draw word
        if (r.pos != null && pp < r.pos.length && count == r.pos[pp]) {
          g.setColor(getFTColor(r.poi[pp], r.acol));
          pp++;
        } else
          g.setColor(textc);
        g.fillRect((int) (xx + ll), yy, wl, r.thumbfh);
      }

      // check if cursor is inside the rect
      if (x > 0 && y > 0
          && (inRect((int) (xx + ll) + 1, yy, wl, r.thumbfh, x, y) || ml > 0)) {
        ul = -1;
        ml = ml == 0 ? getTooltipLength((int) ww) : ml;
        final byte[] tok = new byte[data[0][i] + 2 < ml ? data[0][i] : ml];
        int k = 0;
        for (; k < tok.length - (data[0][i] + 2 < ml ? 0 : 2); k++)
          tok[k] = (byte) data[3][cchars - data[0][i] + k];

        if (r.pos != null) {
          while(pp < r.pos.length && count > r.pos[pp]) pp++;
          if (pp < r.pos.length && count == r.pos[pp]) {
            ttcol.add(r.poi[pp]);
            pp++;
          } else {
            ttcol.add(-1);
          }
        } else ttcol.add(-1);

        if (k < tok.length) {
          tok[k] = '.';
          tok[k + 1] = '.';
          tl.add(tok);
          return yy - r.y - 3;
        }
        tl.add(tok);
        ml -= tok.length + 1;
      }

      ll += wl;
      count++;

      if (psl < data[1].length && sl == data[1][psl]) {
        // new sentence, draw dot
        if (draw) {
          g.setColor(Color.black);
          g.fillRect((int) (xx + ll), yy, (int) r.thumbf, r.thumbfh);
          g.setColor(textc);
        }
        ll += r.thumbf;
        psl++;
        sl = 0;
        ml = 0;
      }

      ll += r.thumbf;
      wl = 0;


      if (ppl < data[2].length && pl == data[2][ppl]) {
        // new paragraph
        yy += r.thumblh;
        ll = 0;
        ppl++;
        pl = 0;
      }
    }
    return yy - r.y + 3;
  }

  /**
   * Get tooltip length.
   * @param ww int width
   * @return tooltip length
   */
  private static int getTooltipLength(final int ww) {
    return (ww / GUIProp.fontsize) + 2;
  }

  /**
   * Checks if cursor is inside the rect.
   * @param rx int x-value of the rect
   * @param ry int y-value of the rect
   * @param rw int width of the rect
   * @param rh int height of the rect
   * @param xx int x-value of the cursor
   * @param yy int y-value of the cursor
   * @return boolean
   */
  private static boolean inRect(final int rx, final int ry,
      final int rw, final int rh, final int xx, final int yy) {
    return xx >= rx && xx <= rx + rw  && yy >= ry && yy <= ry + rh;
  }

  /**
   * Draws a tooltip.
   * @param g graphics reference
   * @param x x-value
   * @param y y-value
   * @param rx viewrect x-value
   * @param ry viewrect y-value
   * @param rh viewrect h-value
   * @param rw viewrect width
   * @param acol ftand color array
   */
  static void drawToolTip(final Graphics g, final int x, final int y,
      final int rx, final int ry, final int rh, final int rw,
      final byte[][] acol) {
    if (tl != null && tl.size > 0) {
      final int[] cw = fontWidths(g.getFont());
      final int sw = BaseXLayout.width(g, cw, ' ');
      int wl = 0;
      int l;
      int nl = 1;
      int wi = rw / 2;
      IntList len = new IntList();
      for (int i = 0; i < tl.size; i++) {
        l = 0;
        for(int n = 0; n < tl.list[i].length; n += cl(tl.list[i][n])) {
          l += BaseXLayout.width(g, cw, cp(tl.list[i], n));
        }
        if (wl + l + sw < wi) wl += l + sw;
        else {
          nl++;
          if (l > wi) wi = l;
          wl = l + sw;
        }
        len.add(l);
      }

      int xx = x + 10 + wi  >= rw ? rx + rw - wi : x + 10;
      int yy = y + 28 + GUIProp.fontsize * nl + 4 < ry + rh ?
          y + 28 : y  - GUIProp.fontsize * nl;

      g.setColor(Color.white);
      if (nl == 1 && wl < wi)
        g.fillRect(xx - 1, yy - GUIProp.fontsize - 1, wl + 1,
            GUIProp.fontsize * nl + 8);
      else g.fillRect(xx - 1, yy - GUIProp.fontsize - 1, wi + 1,
          GUIProp.fontsize * nl + 8);
      g.setColor(Color.black);
      wl = 0;
      for (int i = 0; i < tl.size; i++) {
        l = len.list[i];
        if (wl + l + sw >= wi) {
          yy += GUIProp.fontsize + 1;
          wl = 0;
        }
        final boolean pm =
          !Character.isLetterOrDigit(tl.list[i][tl.list[i].length - 1]);

        if (ttcol.list[i] > -1) {
          g.setColor(getFTColor(ttcol.list[i], acol));
          g.drawString(new String(tl.list[i]), xx + wl, yy);
          if (ul > -1 && i == ul)
            g.drawLine(xx + wl, yy, xx + wl + l, yy);
          g.setColor(Color.black);
          wl += l;
        } else {
          g.drawString(new String(tl.list[i]), xx + wl, yy);
          if (ul > -1 && i == ul)
            g.drawLine(xx + wl, yy, xx + wl + (pm ? l - sw : l), yy);
          wl += l;
        }
        wl += sw;
      }
    }
  }

  /**
   * Draws a text using thumbnail visualization.
   * @param r rectangle
   * @param data fulltext to be drawn
   * @param sen flag for sentence or paragraph
   * @param sw length of a space
   * @param x mouseX
   * @param y mouseY
   * @param w width of map view
   * @param g Graphics
   * @param ds boolean flag for drawing space between tokens
   */
  static void drawThumbnailsToolTip(final MapRect r, final int[][] data,
      final boolean sen, final double sw, final int x, final int y,
      final int w, final Graphics g, final boolean ds) {

    final double ww = r.w;
    int yy = r.y + 3;

    int wl = 0; // word length
    int ll = 0; // line length
    ul = -1;
    int psl = 0, ppl = 0, pl = 0, sl = 0, cc = 0;
    int pp = 0;
    tl = new TokenList();
    ttcol = new IntList();
    boolean ir;
    for (int i = 0; i < data[0].length; i++) {
      ir = false;
      wl = (int) (data[0][i] * r.thumbf);
      pl += data[0][i];
      sl += data[0][i];
      cc += data[0][i];

      if (ll + wl + (ds ? r.thumbf : 0) >= ww) {
        if (ds) {
          // draw token in new line
          yy += r.thumblh;
          ll = 0;
          ir = inRect(r.x, yy, wl, r.thumbfh, x, y);
          ll = wl + (ds ? +(int) r.thumbf : 0);
        } else {
          ir = inRect(r.x + ll, yy, wl - ll, r.thumbfh, x, y);
          ll = wl - (int) (ww - ll) + (ds ? +(int) r.thumbf : 0);
          yy += r.thumblh;
        }
        ir |= inRect(r.x, yy, ll, r.thumbfh, x, y);
      } else {
        ir |= inRect(r.x + ll, yy, wl, r.thumbfh, x, y);
        ll += wl + (ds ? +r.thumbf : 0);
      }

       if (ir) {
        final int si = i;
        final int[] cw = fontWidths(g.getFont());
        final int sp = BaseXLayout.width(g, cw, ' ');
        final int sd = BaseXLayout.width(g, cw, '.');
        // go some tokens backwards form current token
        while(r.pos != null && pp < r.pos.length && i > r.pos[pp]) pp++;
        final int pps = pp;
        final int bpsl = data[1][psl] == sl ? psl + 1 : psl;
        final int bsl = data[1][psl] == sl ? 0 : sl;
        ll = sd * 2 + sp;
        int l = 0;
        byte[] tok;
        int p = cc >= data[0][i] ? cc - data[0][i] : 0;
        boolean apm = false;
        while (p > -1 && i > -1) {
          // append punctuation mark
          apm = psl < data[1].length && data[1][psl] == sl;
          tok = new byte[data[0][i] + (apm ? 1 : 0)];
          for (int k = 0; k < tok.length - (apm ? 1 : 0); k++) {
            tok[k] = (byte) data[3][p + k];
          }

          if (apm) {
            tok[tok.length - 1] = (byte) data[4][psl];
            sl += 1;
          }
          sl -= tok.length;

          if (sl == 0) {
            psl--;
            if (psl == -1) psl = data[1].length;
            else sl = data[1][psl];
          }

          l = 0;
          for(int n = 0; n < tok.length; n += cl(tok[n]))
            l += BaseXLayout.width(g, cw, cp(tok, n));
          if (si > i && ll + l + sp >= w / 2) break;
          ll += l + sp;

          tl.add(tok);
          // find token color
          if (r.pos != null) {
            while(pp < r.pos.length && pp > -1 && i < r.pos[pp]) pp--;
            if (pp < r.pos.length &&  pp > -1  && i == r.pos[pp]) {
              ttcol.add(r.poi[pp]);
              pp++;
            } else ttcol.add(-1);
          } else ttcol.add(-1);

          if (i == 0) break;
          p -= data[0][i - 1];
          i--;
        }

        tl.add(new byte[]{'.', '.'});
        ttcol.add(-1);

        i = si + 1;
        p = cc;
        // invert tokens
        final byte[][] toks = tl.finish();
        final int[] tc = ttcol.finish();
        tl = new TokenList();
        ttcol = new IntList();
        for (int j = toks.length - 1; j > -1; j--) {
          tl.add(toks[j]);
          ttcol.add(tc[j]);
        }
        ul = tl.size - 1;
        ll = 0;

        pp = pps;
        sl = bsl;
        psl = bpsl;
        // process tokens after current token
        while (p < data[3].length && i < data[0].length) {
          apm = false;
          if (psl < data[1].length && data[1][psl] == sl + data[0][i]) {
            apm = true;
            sl = 0;
            psl++;
          }
          tok = new byte[data[0][i] + (apm ? 1 : 0)];
          l = 0;

          for (int k = 0; k < tok.length - (apm ? 1 : 0); k++) {
            tok[k] = (byte) data[3][p + k];
          }

          if (apm) {
            tok[tok.length - 1] = (byte) data[4][psl - 1];
//            sl += 1;
          }
          sl += apm ? sl : tok.length;

          for(int n = 0; n < tok.length; n += cl(tok[n]))
            l += BaseXLayout.width(g, cw, cp(tok, n));
          if (ll + l + sp + 2 * sd >= w / 2) break;
          ll += l + sp;

          tl.add(tok);

          pp = 0;
          if (r.pos != null) {
            while(pp < r.pos.length && i > r.pos[pp]) pp++;
            if (pp < r.pos.length && i == r.pos[pp]) {
              ttcol.add(r.poi[pp]);
              pp++;
            } else ttcol.add(-1);
          } else ttcol.add(-1);
          p += tok.length - (apm ? 1 : 0);
          i++;
        }
        tl.add(new byte[]{'.', '.'});
        ttcol.add(-1);
        return;
      }

      // new sentence
      if (psl < data[1].length && data[1][psl] == sl) {
        if (ll + sw >= ww) {
          yy += r.thumblh;
          ll = 0;
        }

        ll += sw;
        sl = 0;
        psl++;
      }

      // new paragraph
      if (ppl < data[2].length && data[2][ppl] == pl) {
        pl = 0;
        ppl++;
        if (sen) {
          yy += r.thumblh;
          wl = 0;
          ll = 0;
        }
      }
    }
  }

  /**
   * Draws a text using thumbnail visualization.
   * @param g graphics reference
   * @param r rectangle
   * @param data fulltext to be drawn
   * @param sen flag for sentence or paragraph
   * @param sw length of a space
   * @param draw boolean for drawing (used for calculating the height)
   * @return height
   */
  private static int drawThumbnailsSentence(final Graphics g,
      final MapRect r, final int[][] data, final boolean sen,
      final double sw, final boolean draw) {
    final double xx = r.x;
    final double ww = r.w;
    final int ys = r.y + 3;
    int yy = ys;

    int wl = 0; // word length
    int ll = 0; // line length

    final Color textc = COLORS[r.level + 4];
    g.setColor(textc);
    int lastl = 0;
    int count = -1;
    int pp = 0;
    int sl = 0, pl = 0;
    int psl = 0, ppl = 0;

    int i = 0;
    tl = new TokenList();
    while (i < data[0].length) {
      final int io = i;
      while (ll + wl < ww && i < data[0].length &&
          ppl < data[2].length && data[2][ppl] > pl &&
          (psl < data[1].length && data[1][psl] > sl ||
          psl >= data[1].length) &&
          (r.pos == null || (pp < r.pos.length && count < r.pos[pp])
              || pp == r.pos.length)) {
        sl += data[0][i];
        pl += data[0][i];
        lastl = (int) (data[0][i] * r.thumbf);
        wl += lastl;
        count++;
        if (i < data[0].length) i++;
        else break;
      }
      if (io == i) i++;

      // doesn't fit in line
      if (ll + wl >= ww) {
        final int fp = (int) (ww - ll);
        if (fp <= r.thumbf) {
          yy += r.thumblh;
          ll = 0;
        } else {
          final int sp = wl - fp;
          // draw first part of the sentence
          if (draw) {
            g.setColor(textc);
            g.fillRect((int) (xx + ll), yy, fp, r.thumbfh);
          }
          yy += r.thumblh;
          ll = 0;
          wl = sp;
        }
      }

      // color thumbnail because of fulltext hint
      if (r.pos != null && pp < r.pos.length && count == r.pos[pp]) {
        if (lastl > 0) {
          if (draw) g.fillRect((int) (xx + ll), yy, wl - lastl, r.thumbfh);
          ll += wl - lastl;
          wl = lastl;
        }
        if (draw) g.setColor(getFTColor(r.poi[pp], r.acol));
        pp++;
      }

      if (wl + ll < ww) {
        if (draw) {
          g.fillRect((int) (xx + ll), yy, wl, r.thumbfh); //fh);
          g.setColor(textc);
        }
        ll += wl;
        wl = 0;
      }

      // new sentence
      if (psl < data[1].length && data[1][psl] == sl) {
        if (ll + sw >= ww) {
          yy += r.thumblh; //lh;
          ll = 0;
        }

        if (draw) {
          g.setColor(Color.black);
          g.fillRect((int) (xx + ll), yy, (int) sw, r.thumbfh); //sw, fh);
          g.setColor(textc);
        }
        ll += sw;
        sl = 0;
        psl++;
      }

      // new paragraph
      if (ppl < data[2].length && data[2][ppl] == pl) {
        pl = 0;
        ppl++;
        if (sen) {
          yy += r.thumblh;
          wl = 0;
          ll = 0;
        }
      }
    }
    return yy - r.y + r.thumbfh;
  }
}
