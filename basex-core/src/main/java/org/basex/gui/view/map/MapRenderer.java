package org.basex.gui.view.map;

import static org.basex.gui.GUIConstants.*;
import static org.basex.util.FTToken.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.util.*;

import org.basex.core.*;
import org.basex.gui.layout.*;
import org.basex.query.util.ft.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;

/**
 * This class assembles utility methods for painting rectangle contents.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
final class MapRenderer {
  /** Graphics reference. */
  private final Graphics g;

  /** Color for each tooltip token. */
  private BoolList ttcol;
  /** Index of tooltip token to underline. */
  private int ul;

  /**
   * Private constructor.
   * @param g graphics reference
   */
  MapRenderer(final Graphics g) {
    this.g = g;
  }

  /**
   * Calculates the height of the specified text.
   * @param rect rectangle
   * @param text text to be drawn
   * @return last height that was occupied
   */
  int calcHeight(final MapRect rect, final byte[] text) {
    return drawText(rect, text, false);
  }

  /**
   * Draws a text.
   * @param rect rectangle
   * @param text text to be drawn
   */
  void drawText(final MapRect rect, final byte[] text) {
    drawText(rect, text, true);
  }

  /**
   * Draws the specified string. Chops the last character if space is not enough space.
   * @param text text to be drawn
   * @param x x coordinate
   * @param y y coordinate
   * @param w width
   */
  void chopText(final byte[] text, final int x, final int y, final int w) {
    BaseXLayout.chopString(g, text, x, y, w, fontSize);
  }

  /**
   * Draws a text.
   * @param rect rectangle
   * @param text text to be drawn
   * @param draw draw text (otherwise: just calculate space)
   * @return height of the text
   */
  private int drawText(final MapRect rect, final byte[] text, final boolean draw) {
    // limit string to given space
    final int fh = (int) (1.2 * fontSize);
    final Color textc = g.getColor();

    int xx = rect.x;
    int yy = rect.y + fh;
    final int ww = rect.w;

    // get index on first pre value
    int ll = 0;
    final FTLexer lexer = new FTLexer().original().init(text);
    while(lexer.hasNext()) {
      final FTSpan span = lexer.next();
      byte[] token = span.text;
      int wl = 0;

      final int tl = token.length;
      for(int t = 0; t < tl; t += cl(token, t)) {
        wl += BaseXLayout.width(g, cp(token, t));
      }

      if(ll + wl >= ww) {
        xx = rect.x;
        if(ll != 0) yy += fh;
        if(yy + fh > rect.y + rect.h) {
          // text to high, skip drawing
          if(draw) g.drawString(Text.DOTS, xx + ll, yy);
          return rect.h;
        }

        ll = 0;

        if(draw && wl >= ww) {
          // single word is too long for the rectangle
          int twl = 2 * BaseXLayout.width(g, '.');
          if(twl >= ww) return Integer.MAX_VALUE;

          int n = 0;
          for(; n < tl; n += cl(token, n)) {
            final int l = BaseXLayout.width(g, cp(token, n));
            if(twl + l >= ww) break;
            twl += l;
          }
          token = Arrays.copyOf(token, n + 2);
          token[n] = '.';
          token[n + 1] = '.';
        }
      }

      if(draw) {
        // color each full-text hit
        g.setColor(rect.pos != null && rect.pos.contains(span.pos) && !span.del ? GREEN : textc);
        g.drawString(string(token), xx + ll, yy);
      }
      ll += wl;
      if(lexer.paragraph()) {
        // new paragraph
        ll = 0;
        yy += fh;
        if(yy + fh > rect.y + rect.h) {
          // text to high, skip drawing
          if(draw) g.drawString(Text.DOTS, xx + ll, yy);
          return rect.h;
        }
      }
    }
    return yy - rect.y;
  }

  /**
   * Draws a text using thumbnail visualization.
   * Calculates the needed space and chooses an abstraction level.
   * Token/Sentence/Paragraphs
   * @param rect rectangle
   * @param text text to be drawn
   */
  void drawThumbnails(final MapRect rect, final byte[] text) {
    // thumbnail width and height, empty line height
    final double ffmax = 0.25, ffhmax = 0.5, flhmax = 0.3;
    double ff = ffmax, ffh = ffhmax, flh = flhmax;
    byte lhmi = (byte) Math.max(3, ffh * fontSize);
    byte fhmi = (byte) Math.max(6, (flh + ffh) * fontSize);

    int h = rect.h;
    rect.thumbf = ff * fontSize;
    rect.thumbal = 0;

    final int[][] data = new FTLexer().init(text).info();
    boolean l = false;
    final double flhmin = 0.168, ffhmin = 0.28, ffmin = 0.14;
    double flhtmin = flhmin, ffhtmax = ffhmax, ffhtmin = ffhmin, fftmax = ffmax, fftmin = ffmin;
    double bflh = flhmax, bffh = ffhmax, bff = ffmax, flhtmax = flhmax;
    while(rect.thumbal < 2) {
      // find parameter setting for the available space
      ff = round(fftmax, fftmin);
      rect.thumbf = ff * fontSize;
      ffh = round(ffhtmax, ffhtmin);
      rect.thumbfh = (byte) Math.max(1, ffh * fontSize);
      flh = round(flhtmax, flhtmin);
      rect.thumblh = (byte) Math.max(1, (flh + ffh) * fontSize);
      rect.thumbsw = rect.thumbf;

      switch(rect.thumbal) {
        case 0:
          h = drawToken(rect, data, false);
          break;
        case 1:
          h = drawSentence(rect, data, false, rect.h);
          break;
      }

      if(h >= rect.h || le(ffmax, ff) || le(ffhmax, ffh) || le(flhmax, flh)) {
        if(l) {
          // use last setup to draw
          rect.thumbf = bff * fontSize;
          rect.thumbfh = (byte) Math.max(1, bffh * fontSize);
          rect.thumblh = (byte) Math.max(1, (bflh + bffh) * fontSize);
          rect.thumbsw = rect.thumbf;
          switch(rect.thumbal) {
            case 0:
              drawToken(rect, data, true);
              return;
            case 1: case 2:
              drawSentence(rect, data, true, rect.h);
              return;
          }
        }
        if(le(ff, ffmin) || le(ffh, ffhmin) || le(flh, flhmin)) {
          // change abstraction level
          rect.thumbal++;
          fhmi = rect.thumbfh;
          lhmi = rect.thumblh;
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
    final double nl = (rect.h - 3.0) / lhmi;
    // factor for the width of a thumbnail
    final double fnew = (nl * (rect.w - 3) - data[4].length) / sum;
    rect.thumbf = fnew;
    rect.thumbfh = fhmi;
    rect.thumblh = lhmi;
    rect.thumbsw = Math.max(1, fnew);
    drawSentence(rect, data, true, rect.h);
  }

  /**
   * Draws a text using thumbnail visualization, that represents a sentence
   * through a thumbnail. Sentences are separated through black thumbnails.
   * @param rect rectangle
   * @param data full-text to be drawn
   * @param draw boolean for drawing (used for calculating the height)
   * @param mh maximum height
   * @return height
   */
  private int drawSentence(final MapRect rect, final int[][] data, final boolean draw,
      final int mh) {

    final boolean sen = rect.thumbal == 1;
    final FTPos ftp = rect.pos;
    final int xx = rect.x;
    final int ww = rect.w;
    int yy = rect.y + 3;

    final Color textc = color(rect.level + 4);
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
      while(i < dl0 && ppl < dl2 && data2[ppl] > pl && (psl >= dl1 || data1[psl] > sl)) {
        sl += data0[i];
        pl += data0[i];
        final int lastl = (int) (data0[i] * rect.thumbf);
        error += data0[i] * rect.thumbf - lastl;
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
        ++i;
      }

      if(ct == 0) {
        while(ll + wl >= ww) {
          if(draw) g.fillRect(xx + ll, yy, ww - ll, rect.thumbfh);
          wl -= ww - ll;
          ll = 0;
          yy += rect.thumblh;
          if(yy + rect.thumblh >= rect.y + mh) {
            // height to big
            return rect.h;
          }
        }
        if(draw) g.fillRect(xx + ll, yy, wl, rect.thumbfh);
        ll += wl;
      } else {
        int cttmp = 0;
        int wltmp = wl / ct;
        while(cttmp < ct) {
          if(pp - ct + cttmp < ftp.size()) g.setColor(GREEN);

          while(ll + wltmp >= ww) {
            if(draw) g.fillRect(xx + ll, yy, ww - ll, rect.thumbfh);
            wltmp -= ww - ll;
            ll = 0;
            yy += rect.thumblh;
            // skip rest if no space is left
            if(yy + rect.thumblh >= rect.y + mh) return rect.h;
          }
          if(draw) g.fillRect(xx + ll, yy, wltmp, rect.thumbfh);
          ll += wltmp;
          wltmp = wl / ct + (cttmp == ct - 2 ? wl - wl / ct * ct : 0);
          ++cttmp;
        }
      }

      // new sentence
      if(psl < dl1 && data1[psl] == sl) {
        if(ll + rect.thumbsw >= ww) {
          yy += rect.thumblh;
          ll = 0;
          // skip rest if no space is left
          if(yy + rect.thumblh >= rect.y + mh) return rect.h;
        }

        if(draw) {
          g.setColor(TEXT);
          g.fillRect(xx + ll, yy, (int) rect.thumbsw, rect.thumbfh);
          g.setColor(textc);
        }
        ll += rect.thumbsw;
        sl = 0;
        ++psl;
      }

      // new paragraph
      if(ppl < dl2 && data2[ppl] == pl) {
        pl = 0;
        ++ppl;
        if(sen) {
          yy += rect.thumblh;
          ll = 0;
          // skip rest if no space is left
          if(yy + rect.thumblh >= rect.y + mh) return rect.h;
        }
      }
    }
    return yy - rect.y + rect.thumbfh;
  }

  /**
   * Draws a text using thumbnail visualization, that represents a token
   * through a thumbnail.
   * @param rect rectangle
   * @param data full-text to be drawn
   * @param draw boolean for drawing (used for calculating the height)
   * @return heights
   */
  private int drawToken(final MapRect rect, final int[][] data, final boolean draw) {
    final double xx = rect.x;
    final double ww = rect.w;
    final FTPos ftp = rect.pos;

    int yy = rect.y + 3;
    double ll = 0; // line length
    double e = 0;

    final Color textc = color(rect.level + 4);
    int count = 0;
    int sl = 0, pl = 0;
    int psl = 0, ppl = 0;
    final int[] data0 = data[0], data1 = data[1], data2 = data[2];
    final int dl1 = data1.length, dl2 = data2.length;
    for(final int ad0 : data0) {
      int wl = (int) (ad0 * rect.thumbf); // word length
      e += ad0 * rect.thumbf - wl;

      if(e >= 1) {
        wl += (int) e;
        e -= (int) e;
      }
      sl += ad0;
      pl += ad0;
      // check if rectangle fits in line - don't split token and dot
      if(ll + wl + rect.thumbsw * (psl < dl1 && sl == data1[psl] ? 1 : 0) >= ww) {
        yy += rect.thumblh;
        ll = 0;
        if(wl >= ww) return rect.h + 3;
      }

      if(draw) {
        // draw word
        g.setColor(ftp != null && ftp.contains(count) ? GREEN : textc);
        g.fillRect((int) (xx + ll), yy, wl, rect.thumbfh);
      }

      ll += wl;
      ++count;

      if(psl < dl1 && sl == data1[psl]) {
        // new sentence, draw dot
        if(draw) {
          g.setColor(TEXT);
          g.fillRect((int) (xx + ll), yy, (int) rect.thumbsw, rect.thumbfh);
          g.setColor(textc);
        }
        ll += rect.thumbsw;
        ++psl;
        sl = 0;
      }

      ll += rect.thumbf;
      if(ppl < dl2 && pl == data2[ppl]) {
        // new paragraph
        yy += rect.thumblh;
        ll = 0;
        ++ppl;
        pl = 0;
      }
    }
    return yy - rect.y + 3;
  }

  /**
   * Calculates a the tooltip text for the thumbnail visualization.
   * @param rect rectangle
   * @param data full-text to be drawn
   * @param x mouseX
   * @param y mouseY
   * @param w width of map view
   * @return token list or {@code null}
   */
  TokenList computeToolTip(final MapRect rect, final int[][] data, final int x, final int y,
      final int w) {

    // rectangle is empty - don't need a tooltip
    if(rect.thumbf == 0) return null;

    final boolean sen = rect.thumbal < 2;
    final boolean ds = rect.thumbal < 1;

    final FTPos ftp = rect.pos;
    final int ww = rect.w;
    int yy = rect.y + 3;

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
      double wl = data0[i] * rect.thumbf;
      // sum up error, caused by int cast
      error += data0[i] * rect.thumbf - wl;
      if(error >= 1) {
        // adjust word length
        wl += error;
        error -= (int) error;
      }

      pl += data0[i];
      sl += data0[i];
      cc += data0[i];

      // find hovered thumbnail and corresponding text
      final boolean ir;
      if(ll + wl + (ds && psl < dl1 && data1[psl] == sl ? rect.thumbsw : 0) >= ww) {
        // split token to safe space
        if(!ds) wl -= ww - ll;
        yy += rect.thumblh;
        ir = inRect(rect.x, yy, wl, rect.thumbfh, x, y);
        ll = wl + (psl < dl1 && data1[psl] == sl ? rect.thumbsw : rect.thumbf);
      } else {
        ir = inRect(rect.x + ll, yy, wl, rect.thumbfh, x, y);
        ll += wl + (ds ? rect.thumbf : 0);
      }

      if(ir) {
        // go back and forward through the text, centralize the hovered token
        final int si = i;
        final int sp = BaseXLayout.width(g, ' ');
        final int sd = BaseXLayout.width(g, '.');
        // go some tokens backwards form current token
        final int bpsl = data1[psl] == sl ? psl + 1 : psl;
        final int bsl = data1[psl] == sl ? 0 : sl;
        ll = (sd << 1) + sp;
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
          for(int n = 0; n < ts; n += cl(tok, n)) l += BaseXLayout.width(g, cp(tok, n));
          if(si > i && ll + l + sp >= w / 2.0d) break;
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

          for(int n = 0; n < ts; n += cl(tok, n)) l += BaseXLayout.width(g, cp(tok, n));
          if(ll + l + sp + 2 * sd >= w / 2.0d) break;
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
        if(ll + rect.thumbsw >= ww) {
          yy += rect.thumblh;
          ll -= ww;
        }

        ll += rect.thumbsw;
        sl = 0;
        ++psl;
      }

      // new paragraph
      if(ppl < dl2 && data2[ppl] == pl) {
        pl = 0;
        ++ppl;
        if(sen) {
          yy += rect.thumblh;
          ll = 0;
        }
      }
    }
    return tl;
  }

  /**
   * Draws pre-calculated tooltip.
   * @param rect view rectangle
   * @param x x value
   * @param y y value
   * @param tl token list
   */
  void drawToolTip(final MapRect rect, final int x, final int y, final TokenList tl) {
    if(tl == null || tl.isEmpty()) return;
    final int sw = BaseXLayout.width(g, ' ');
    int wl = 0;
    int nl = 1;
    int wi = rect.w / 2;
    final IntList len = new IntList();
    final int ts = tl.size();
    for(int i = 0; i < ts; i++) {
      int l = 0;
      final byte[] tok = tl.get(i);
      final int ns = tok.length;
      for(int n = 0; n < ns; n += cl(tok, n)) {
        l += BaseXLayout.width(g, cp(tok, n));
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
    final int xx = x + 10 + ww >= rect.x + rect.w ? rect.x + rect.w - ww - 2 : x + 10;
    int yy = y + 28 + fontSize * nl + 4 < rect.y + rect.h ? y + 28 :
      y - rect.y - 4 > fontSize * nl ? y - fontSize * nl : rect.y + rect.h - 4 - fontSize * nl;

    g.setColor(color(10));
    g.drawRect(xx - 3, yy - fontSize - 1, ww + 3, fontSize * nl + 7);
    g.setColor(color(0));
    g.fillRect(xx - 2, yy - fontSize, ww + 2, fontSize * nl + 6);
    g.setColor(color(20));
    wl = 0;
    final int is = tl.size();
    for(int i = 0; i < is; ++i) {
      final int l = len.get(i);
      if(wl + l + sw >= wi) {
        yy += fontSize + 1;
        wl = 0;
      }
      final boolean pm = !lod(tl.get(i)[tl.get(i).length - 1]);
      if(ttcol.get(i)) g.setColor(GREEN);
      g.drawString(string(tl.get(i)), xx + wl, yy);
      if(i == ul) {
        g.drawLine(xx + wl, yy + 1, xx + wl + (pm ? l - sw : l), yy + 1);
      }
      g.setColor(color(24));
      wl += l + sw;
    }
  }

  /**
   * Checks if cursor is inside the rectangle.
   * @param rx x value
   * @param ry y value
   * @param rw width
   * @param rh height
   * @param xx x value of the cursor
   * @param yy y value of the cursor
   * @return result of check
   */
  private static boolean inRect(final double rx, final int ry, final double rw, final int rh,
      final int xx, final int yy) {
    return xx >= rx && xx <= rx + rw && yy >= ry && yy <= ry + rh;
  }

  /**
   * Checks if the first is smaller than the second value, ignoring a small difference.
   * @param a double 1
   * @param b double 2
   * @return result of check
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
}
