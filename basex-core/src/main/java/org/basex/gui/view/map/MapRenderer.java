package org.basex.gui.view.map;

import static org.basex.gui.GUIConstants.*;
import static org.basex.util.FTToken.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.layout.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;

/**
 * This class assembles utility methods for painting rectangle contents.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
final class MapRenderer {
  /** Color for each tooltip token. */
  private static BoolList ttcol;
  /** Index of tooltip token to underline. */
  private static int ul;

  /** Private constructor. */
  private MapRenderer() { }

  /**
   * Calculates the height of the specified text.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @param fs font size
   * @return last height that was occupied
   */
  static int calcHeight(final Graphics g, final MapRect r, final byte[] s, final int fs) {
    return drawText(g, r, s, false, fs);
  }

  /**
   * Draws a text.
   *
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @param fs font size
   */
  static void drawText(final Graphics g, final MapRect r, final byte[] s, final int fs) {
    drawText(g, r, s, true, fs);
  }

  /**
   * Draws a text.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @param draw draw text (otherwise: just calculate space)
   * @param fs font size
   * @return height of the text
   */
  private static int drawText(final Graphics g, final MapRect r, final byte[] s, final boolean draw,
      final int fs) {

    // limit string to given space
    final int[] cw = fontWidths(g.getFont());
    final int fh = (int) (1.2 * fs);
    final Color textc = g.getColor();

    int xx = r.x;
    int yy = r.y + fh;
    final int ww = r.w;

    // get index on first pre value
    int ll = 0;
    final FTLexer lex = new FTLexer().original().init(s);
    while(lex.hasNext()) {
      final FTSpan span = lex.next();
      byte[] tok = span.text;
      int wl = 0;

      final int tl = tok.length;
      for(int t = 0; t < tl; t += cl(tok, t)) {
        wl += BaseXLayout.width(g, cw, cp(tok, t));
      }

      if(ll + wl >= ww) {
        xx = r.x;
        if(ll != 0) yy += fh;
        if(yy + fh > r.y + r.h) {
          // text to high, skip drawing
          if(draw) g.drawString(Text.DOTS, xx + ll, yy);
          return r.h;
        }

        ll = 0;

        if(draw && wl >= ww) {
          // single word is too long for the rectangle
          int twl = 2 * BaseXLayout.width(g, cw, '.');
          if(twl >= ww) return Integer.MAX_VALUE;

          int n = 0;
          for(; n < tl; n += cl(tok, n)) {
            final int l = BaseXLayout.width(g, cw, cp(tok, n));
            if(twl + l >= ww) break;
            twl += l;
          }
          tok = Arrays.copyOf(tok, n + 2);
          tok[n] = '.';
          tok[n + 1] = '.';
        }
      }

      if(draw) {
        // color each full-text hit
        g.setColor(r.pos != null && r.pos.contains(span.pos) && !span.del ? GREEN : textc);
        g.drawString(string(tok), xx + ll, yy);
      }
      ll += wl;
      if(lex.paragraph()) {
        // new paragraph
        ll = 0;
        yy += fh;
        if(yy + fh > r.y + r.h) {
          // text to high, skip drawing
          if(draw) g.drawString(Text.DOTS, xx + ll, yy);
          return r.h;
        }
      }
    }
    return yy - r.y;
  }

  /**
   * Draws a text using thumbnail visualization.
   * Calculates the needed space and chooses an abstraction level.
   * Token/Sentence/Paragraphs
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @param fs font size
   */
  static void drawThumbnails(final Graphics g, final MapRect r, final byte[] s, final int fs) {
    // thumbnail width and height, empty line height
    final double ffmax = 0.25, ffhmax = 0.5, flhmax = 0.3;
    double ff = ffmax, ffh = ffhmax, flh = flhmax;
    byte lhmi = (byte) Math.max(3, ffh * fs), fhmi = (byte) Math.max(6, (flh + ffh) * fs);

    int h = r.h;
    r.thumbf = ff * fs;
    r.thumbal = 0;

    final int[][] data = new FTLexer().init(s).info();
    boolean l = false;
    final double flhmin = 0.168, ffhmin = 0.28, ffmin = 0.14;
    double flhtmin = flhmin, ffhtmax = ffhmax, ffhtmin = ffhmin, fftmax = ffmax, fftmin = ffmin;
    double bflh = flhmax, bffh = ffhmax, bff = ffmax, flhtmax = flhmax;
    while(r.thumbal < 2) {
      // find parameter setting for the available space
      ff = round(fftmax, fftmin);
      r.thumbf = ff * fs;
      ffh = round(ffhtmax, ffhtmin);
      r.thumbfh = (byte) Math.max(1, ffh * fs);
      flh = round(flhtmax, flhtmin);
      r.thumblh = (byte) Math.max(1, (flh + ffh) * fs);
      r.thumbsw = r.thumbf;

      switch(r.thumbal) {
        case 0:
          h = drawToken(g, r, data, false);
          break;
        case 1: case 2:
          h = drawSentence(g, r, data, false, r.h);
          break;
      }

      if(h >= r.h || le(ffmax, ff) || le(ffhmax, ffh) || le(flhmax, flh)) {
        if(l) {
          // use last setup to draw
          r.thumbf = bff * fs;
          r.thumbfh = (byte) Math.max(1, bffh * fs);
          r.thumblh = (byte) Math.max(1, (bflh + bffh) * fs);
          r.thumbsw = r.thumbf;
          switch(r.thumbal) {
            case 0:
              drawToken(g, r, data, true);
              return;
            case 1: case 2:
              drawSentence(g, r, data, true, r.h);
              return;
          }
        }
        if(le(ff, ffmin) || le(ffh, ffhmin) || le(flh, flhmin)) {
          // change abstraction level
          r.thumbal++;
          fhmi = r.thumbfh;
          lhmi = r.thumblh;
          fftmin = ffmin;
          fftmax = ffmax;
          ffhtmin = ffhmin;
          ffhtmax = ffhmax;
          flhtmin = flhmin;
          flhtmax = flhmax;
        } else {
          // shrink size
          fftmax = ff;
          ffhtmax = ffh;
          flhtmax = flh;
        }
      } else {
        l = true;
        // backup and try to enlarge
        bff = ff;
        bffh = ffh;
        bflh = flh;
        fftmin = ff;
        ffhtmin = ffh;
        flhtmin = flh;
      }
    }

    // calculate parameter setting
    // total number of bytes
    final double sum = data[3].length + data[4].length;
    // number of lines printable
    final double nl = (r.h - 3.0) / lhmi;
    // factor for the width of a thumbnail
    final double fnew = (nl * (r.w - 3) - data[4].length) / sum;
    r.thumbf = fnew;
    r.thumbfh = fhmi;
    r.thumblh = lhmi;
    r.thumbsw = Math.max(1, fnew);
    drawSentence(g, r, data, true, r.h);
  }

  /**
   * Checks if the first is smaller than the second value, ignoring a
   * small difference.
   * @param a double 1
   * @param b double 2
   * @return true if a < b
   */
  private static boolean le(final double a, final double b) {
    return a < b || a / b < 1.05;
  }

  /**
   * Returns the rounded average of the specified values.
   * @param a first double
   * @param b second double
   * @return rounded double
   */
  private static double round(final double a, final double b) {
    final double v = (a + b) / 2;
    final double d = v * 100000;
    final int i = (int) d;
    final double r = d - i >= 0.5 ? i + 1 : i;
    return r / 100000;
  }

  /**
   * Draws a text using thumbnail visualization, that represents a sentence
   * through a thumbnail. Sentences are separated through black thumbnails.
   * @param g graphics reference
   * @param r rectangle
   * @param data full-text to be drawn
   * @param draw boolean for drawing (used for calculating the height)
   * @param mh maximum height
   * @return height
   */
  private static int drawSentence(final Graphics g, final MapRect r, final int[][] data,
      final boolean draw, final int mh) {

    final boolean sen = r.thumbal == 1;
    final FTPos ftp = r.pos;
    final int xx = r.x;
    final int ww = r.w;
    int yy = r.y + 3;

    final Color textc = color(r.level + 4);
    g.setColor(textc);
    int count = -1;
    int pp = 0, sl = 0, pl = 0;
    int psl = 0, ppl = 0;
    double error = 0;

    int i = 0;
    int ll = 0;
    final int[] data0 = data[0], data1 = data[1], data2 = data[2];
    final int dl0 = data0.length, dl1 = data1.length, dl2 = data2.length;
    while(i < dl0) {
      g.setColor(textc);

      int ct = 0;
      int wl = 0;  // word and line length
      while(i < dl0 && ppl < dl2 && data2[ppl] > pl &&
        (psl < dl1 && data1[psl] > sl || psl >= dl1)) {
        sl += data0[i];
        pl += data0[i];
        final int lastl = (int) (data0[i] * r.thumbf);
        error += data0[i] * r.thumbf - lastl;
        if(error >= 1) {
          wl += (int) error;
          error -= (int) error;
        }
        wl += lastl;

        if(ftp != null && ftp.contains(count)) {
          ++ct;
          ++pp;
        }
        ++count;
        if(i < dl0) ++i;
        else break;
      }

      if(ct == 0) {
        while(ll + wl >= ww) {
          if(draw) g.fillRect(xx + ll, yy, ww - ll, r.thumbfh);
          wl -= ww - ll;
          ll = 0;
          yy += r.thumblh;
          if(yy + r.thumblh >= r.y + mh) {
            // height to big
            return r.h;
          }
        }
        if(draw) g.fillRect(xx + ll, yy, wl, r.thumbfh);
        ll += wl;
      } else {
        int cttmp = 0;
        int wltmp = wl / ct;
        while(cttmp < ct) {
          if(pp - ct + cttmp < ftp.size()) g.setColor(GREEN);

          while(ll + wltmp >= ww) {
            if(draw) g.fillRect(xx + ll, yy, ww - ll, r.thumbfh);
            wltmp -= ww - ll;
            ll = 0;
            yy += r.thumblh;
            // skip rest if no space is left
            if(yy + r.thumblh >= r.y + mh) return r.h;
          }
          if(draw) g.fillRect(xx + ll, yy, wltmp, r.thumbfh);
          ll += wltmp;
          wltmp = wl / ct + (cttmp == ct - 2 ? wl - wl / ct * ct : 0);
          ++cttmp;
        }
      }

      // new sentence
      if(psl < dl1 && data1[psl] == sl) {
        if(ll + r.thumbsw >= ww) {
          yy += r.thumblh;
          ll = 0;
          // skip rest if no space is left
          if(yy + r.thumblh >= r.y + mh) return r.h;
        }

        if(draw) {
          g.setColor(TEXT);
          g.fillRect(xx + ll, yy, (int) r.thumbsw, r.thumbfh);
          g.setColor(textc);
        }
        ll += r.thumbsw;
        sl = 0;
        ++psl;
      }

      // new paragraph
      if(ppl < dl2 && data2[ppl] == pl) {
        pl = 0;
        ++ppl;
        if(sen) {
          yy += r.thumblh;
          ll = 0;
          // skip rest if no space is left
          if(yy + r.thumblh >= r.y + mh) return r.h;
        }
      }
    }
    return yy - r.y + r.thumbfh;
  }

  /**
   * Draws a text using thumbnail visualization, that represents a token
   * through a thumbnail.
   * @param g graphics reference
   * @param r rectangle
   * @param data full-text to be drawn
   * @param draw boolean for drawing (used for calculating the height)
   * @return heights
   */
  private static int drawToken(final Graphics g, final MapRect r, final int[][] data,
      final boolean draw) {

    final double xx = r.x;
    final double ww = r.w;
    final FTPos ftp = r.pos;

    int yy = r.y + 3;
    double ll = 0; // line length
    double e = 0;

    final Color textc = color(r.level + 4);
    int count = 0;
    int sl = 0, pl = 0;
    int psl = 0, ppl = 0;
    final int[] data0 = data[0], data1 = data[1], data2 = data[2];
    final int dl1 = data1.length, dl2 = data2.length;
    for(final int ad0 : data0) {
      int wl = (int) (ad0 * r.thumbf); // word length
      e += ad0 * r.thumbf - wl;

      if(e >= 1) {
        wl += (int) e;
        e -= (int) e;
      }
      sl += ad0;
      pl += ad0;
      // check if rectangle fits in line - don't split token and dot
      if(ll + wl + r.thumbsw * (psl < dl1 && sl == data1[psl] ? 1 : 0) >= ww) {
        yy += r.thumblh;
        ll = 0;
        if(wl >= ww) return r.h + 3;
      }

      if(draw) {
        // draw word
        g.setColor(ftp != null && ftp.contains(count) ? GREEN : textc);
        g.fillRect((int) (xx + ll), yy, wl, r.thumbfh);
      }

      ll += wl;
      ++count;

      if(psl < dl1 && sl == data1[psl]) {
        // new sentence, draw dot
        if(draw) {
          g.setColor(TEXT);
          g.fillRect((int) (xx + ll), yy, (int) r.thumbsw, r.thumbfh);
          g.setColor(textc);
        }
        ll += r.thumbsw;
        ++psl;
        sl = 0;
      }

      ll += r.thumbf;
      if(ppl < dl2 && pl == data2[ppl]) {
        // new paragraph
        yy += r.thumblh;
        ll = 0;
        ++ppl;
        pl = 0;
      }
    }
    return yy - r.y + 3;
  }

  /**
   * Checks if cursor is inside the rectangle.
   * @param rx int x-value
   * @param ry int y-value
   * @param rw double width
   * @param rh int height
   * @param xx int x-value of the cursor
   * @param yy int y-value of the cursor
   * @return boolean
   */
  private static boolean inRect(final double rx, final int ry, final double rw, final int rh,
      final int xx, final int yy) {
    return xx >= rx && xx <= rx + rw && yy >= ry && yy <= ry + rh;
  }

  /**
   * Calculates a the tooltip text for the thumbnail visualization.
   * @param r rectangle
   * @param data full-text to be drawn
   * @param x mouseX
   * @param y mouseY
   * @param w width of map view
   * @param g Graphics
   * @return token list
   */
  static TokenList calculateToolTip(final MapRect r, final int[][] data, final int x, final int y,
      final int w, final Graphics g) {

    // rectangle is empty - don't need a tooltip
    if(r.thumbf == 0) return null;

    final boolean sen = r.thumbal < 2;
    final boolean ds = r.thumbal < 1;

    final FTPos ftp = r.pos;
    final int ww = r.w;
    int yy = r.y + 3;

    ul = -1;
    final TokenList tl = new TokenList();
    ttcol = new BoolList();
    int cc = 0;
    int sl = 0;
    int pl = 0;
    int ppl = 0;
    int psl = 0;
    double error = 0;
    double ll = 0; // line length
    final int[] data0 = data[0], data1 = data[1], data2 = data[2], data3 = data[3], data4 = data[4];
    final int dl0 = data0.length, dl1 = data1.length, dl2 = data2.length, dl3 = data3.length;
    for(int i = 0; i < dl0; ++i) {
      double wl = data0[i] * r.thumbf;
      // sum up error, caused by int cast
      error += data0[i] * r.thumbf - wl;
      if(error >= 1) {
        // adjust word length
        wl += error;
        error -= (int) error;
      }

      pl += data0[i];
      sl += data0[i];
      cc += data0[i];

      // find hovered thumbnail and corresponding text
      boolean ir = false;
      if(ll + wl + (ds && psl < dl1 && data1[psl] == sl ? r.thumbsw : 0) >= ww) {
        if(ds) {
          // do not split token
          yy += r.thumblh;
          ir = inRect(r.x, yy, wl, r.thumbfh, x, y);
          ll = wl + (psl < dl1 && data1[psl] == sl ?
              r.thumbsw : r.thumbf);
        } else {
          // split token to safe space
          yy += r.thumblh;
          wl -= ww - ll;
          ir = inRect(r.x, yy, wl, r.thumbfh, x, y);
          ll = wl +
          (psl < dl1 && data1[psl] == sl ? r.thumbsw :  r.thumbf);
        }
      } else {
        ir |= inRect(r.x + ll, yy, wl, r.thumbfh, x, y);
        ll += wl + (ds ? r.thumbf : 0);
      }

      if(ir) {
        // go back and forward through the text, centralize the hovered token
        final int si = i;
        final int[] cw = fontWidths(g.getFont());
        final int sp = BaseXLayout.width(g, cw, ' ');
        final int sd = BaseXLayout.width(g, cw, '.');
        // go some tokens backwards form current token
        final int bpsl = data1[psl] == sl ? psl + 1 : psl;
        final int bsl = data1[psl] == sl ? 0 : sl;
        ll = sd * 2 + sp;
        int l;
        int p = cc >= data0[i] ? cc - data0[i] : 0;
        boolean apm;

        while(p > -1 && i > -1) {
          // append punctuation mark
          apm = psl < dl1 && data1[psl] == sl;
          final byte[] tok = new byte[data0[i] + (apm ? 1 : 0)];
          final int ts = tok.length;
          for(int k = 0; k < ts - (apm ? 1 : 0); ++k) tok[k] = (byte) data3[p + k];

          if(apm) {
            tok[ts - 1] = (byte) data4[psl];
            ++sl;
          }
          sl -= ts;

          if(sl == 0) {
            --psl;
            if(psl == -1) psl = dl1;
            else sl = data1[psl];
          }

          l = 0;
          for(int n = 0; n < ts; n += cl(tok, n))
            l += BaseXLayout.width(g, cw, cp(tok, n));
          if(si > i && ll + l + sp >= w / 2d) break;
          ll += l + sp;

          tl.add(tok);
          // find token color
          ttcol.add(ftp != null && ftp.contains(i));
          if(i == 0) break;
          p -= data0[i - 1];
          --i;
        }
        if(i > 0) {
          tl.add(new byte[] { '.', '.' });
          ttcol.add(false);
        }

        i = si + 1;
        p = cc;
        // invert tokens
        ul = tl.size() - 1;
        final byte[][] toks = tl.next();
        final boolean[] tc = ttcol.next();
        final int tsl = toks.length;
        for(int j = tsl - 1; j >= 0; j--) {
          tl.add(toks[j]);
          ttcol.add(tc[j]);
        }
        ll = 0;

        sl = bsl;
        psl = bpsl;
        // process tokens after current token
        while(p < dl3 && i < dl0) {
          apm = false;
          if(psl < dl1 && data1[psl] == sl + data0[i]) {
            apm = true;
            sl = 0;
            ++psl;
          }
          final byte[] tok = new byte[data0[i] + (apm ? 1 : 0)];
          final int ts = tok.length;
          l = 0;

          for(int k = 0; k < ts - (apm ? 1 : 0); ++k) {
            tok[k] = (byte) data3[p + k];
          }

          if(apm) tok[ts - 1] = (byte) data4[psl - 1];
          sl += apm ? sl : ts;

          for(int n = 0; n < ts; n += cl(tok, n))
            l += BaseXLayout.width(g, cw, cp(tok, n));
          if(ll + l + sp + 2 * sd >= w / 2d) break;
          ll += l + sp;

          tl.add(tok);

          ttcol.add(ftp != null && ftp.contains(i));
          p += ts - (apm ? 1 : 0);
          ++i;
        }

        if(i < dl0) {
          tl.add(new byte[] { '.', '.' });
          ttcol.add(false);
        }
        return tl;
      }

      // new sentence
      if(ds && psl < dl1 && data1[psl] == sl) {
        if(ll + r.thumbsw >= ww) {
          yy += r.thumblh;
          ll -= ww;
        }

        ll += r.thumbsw;
        sl = 0;
        ++psl;
      }

      // new paragraph
      if(ppl < dl2 && data2[ppl] == pl) {
        pl = 0;
        ++ppl;
        if(sen) {
          yy += r.thumblh;
          ll = 0;
        }
      }
    }
    return tl;
  }

  /**
   * Draws pre-calculated tooltip.
   * @param g graphics reference
   * @param x x-value
   * @param y y-value
   * @param mr view rectangle
   * @param tl token list
   * @param fs font size
   */
  static void drawToolTip(final Graphics g, final int x, final int y, final MapRect mr,
      final TokenList tl, final int fs) {

    if(tl == null || tl.isEmpty()) return;
    final int[] cw = fontWidths(g.getFont());
    final int sw = BaseXLayout.width(g, cw, ' ');
    int wl = 0;
    int nl = 1;
    int wi = mr.w / 2;
    final IntList len = new IntList();
    final int ts = tl.size();
    for(int i = 0; i < ts; i++) {
      int l = 0;
      final byte[] tok = tl.get(i);
      final int ns = tok.length;
      for(int n = 0; n < ns; n += cl(tok, n)) {
        l += BaseXLayout.width(g, cw, cp(tok, n));
      }
      if(wl + l + sw < wi) {
        wl += l + sw;
      } else {
        ++nl;
        if(l > wi) wi = l;
        wl = l + sw;
      }
      len.add(l);
    }

    final int ww = nl == 1 && wl < wi ? wl : wi;
    // find optimal position for the tooltip
    final int xx = x + 10 + ww >= mr.x + mr.w ? mr.x + mr.w - ww - 2 : x + 10;
    int yy = y + 28 + fs * nl + 4 < mr.y + mr.h ? y + 28 :
      y - mr.y - 4 > fs * nl ? y - fs * nl : mr.y + mr.h - 4 - fs * nl;

    g.setColor(color(10));
    g.drawRect(xx - 3, yy - fs - 1, ww + 3, fs * nl + 7);
    g.setColor(color(0));
    g.fillRect(xx - 2, yy - fs, ww + 2, fs * nl + 6);
    g.setColor(color(20));
    wl = 0;
    final int is = tl.size();
    for(int i = 0; i < is; ++i) {
      final int l = len.get(i);
      if(wl + l + sw >= wi) {
        yy += fs + 1;
        wl = 0;
      }
      final boolean pm = !valid(tl.get(i)[tl.get(i).length - 1]);
      if(ttcol.get(i)) g.setColor(GREEN);
      g.drawString(string(tl.get(i)), xx + wl, yy);
      if(i == ul) {
        g.drawLine(xx + wl, yy + 1, xx + wl + (pm ? l - sw : l), yy + 1);
      }
      g.setColor(color(24));
      wl += l + sw;
    }
  }
}
