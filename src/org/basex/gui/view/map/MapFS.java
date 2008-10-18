package org.basex.gui.view.map;

import static org.basex.build.BuildText.*;
import static org.basex.Text.*;
import static org.basex.util.Token.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.basex.BaseX;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.fs.DataFS;
import org.basex.gui.GUI;
import org.basex.gui.GUIFS;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewData;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * Paint filesystem specific TreeMap rectangles.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class MapFS extends MapPainter {
  /** Image offset. */
  private static final int PICOFFSET = 2;
  /** Image cache. */
  private static MapImages images;
  /** Data FS reference. */
  private static DataFS fs;
  /** Flag for error message. */
  private boolean error;

  /**
   * Constructor.
   * @param m map reference.
   * @param f fs reference
   */
  MapFS(final MapView m, final DataFS f) {
    super(m);
    fs = f;
    if(images == null) images = new MapImages(m);
  }

  @Override
  void drawRectangles(final Graphics g, final ArrayList<MapRect> rects) {
    final Data data = GUI.context.data();
    final MapRect l = view.layout;
    final int ww = view.getWidth();
    final int hh = view.getHeight();
    final int min = Math.max(GUIProp.fontsize, 16);

    mpos = 0;
    final int rs = rects.size();
    for(int ri = 0; ri < rs; ri++) {
      // get rectangle information
      final MapRect r = rects.get(ri);
      final int pre = r.p;

      // level 1: next context node, set marker pointer to 0
      final int lvl = r.l;
      if(lvl == 0) mpos = 0;

      final boolean isImage = GUIFS.mime(fs.name(r.p)) == GUIFS.Type.IMAGE;
      final boolean full = r.w == ww && r.h == hh;

      // draw rectangle
      Color color = nextMark(rects, pre, ri, rs);
      final boolean mark = color != null;

      if(full && isImage) color = Color.black;
      else if(full || color == null) color = GUIConstants.COLORS[lvl];
      g.setColor(color);

      if(r.w < l.x + l.w || r.h < l.y + l.h || GUIProp.maplayout < 2 ||
          ViewData.isLeaf(data, pre)) {
        g.fillRect(r.x, r.y, r.w, r.h);
      } else {
        // painting only border for non-leaf nodes..
        g.fillRect(r.x, r.y, l.x, r.h);
        g.fillRect(r.x, r.y, r.w, l.y);
        g.fillRect(r.x + r.w - l.w, r.y, l.w, r.h);
        g.fillRect(r.x, r.y + r.h - l.h, r.w, l.h);
      }

      if(!full) {
        color = mark ? GUIConstants.colormark3 : GUIConstants.COLORS[lvl + 2];
        g.setColor(color);
        g.drawRect(r.x, r.y, r.w, r.h);
        color = mark ? GUIConstants.colormark4 :
          GUIConstants.COLORS[Math.max(0, lvl - 2)];
        g.setColor(color);
        g.drawLine(r.x + r.w, r.y, r.x + r.w, r.y + r.h);
        g.drawLine(r.x, r.y + r.h, r.x + r.w, r.y + r.h);
      }

      // skip drawing of string when left space is too small
      if(r.w < min || r.h < min) continue;

      final MapRect cr = r.clone();
      if(drawRectangle(g, cr, mark)) {
        cr.x += 4;
        cr.w -= 8;

        final TokenBuilder tb = new TokenBuilder();
        int k = data.kind(pre);
        final int s = pre + data.size(pre, k);
        int p = pre + data.attSize(pre, k);
        boolean elem = false;
        while(p != s) {
          k = data.kind(p);
          if(k == Data.ELEM) {
            if(elem) tb.add('\n');
            tb.add(data.tag(p));
            tb.add(": ");
            elem = true;
          } else if(k == Data.TEXT) {
            tb.add(data.text(p));
            tb.add("\n");
            elem = false;
          }
          p += data.attSize(p, k);
        }
        g.setFont(GUIConstants.mfont);
        BaseXLayout.drawText(g, cr, tb.finish());
      }
    }
  }

  /**
   * Paint single rectangle.
   * @param g graphics reference
   * @param rect rectangle
   * @param mark selection flag
   * @return meta data flag
   */
  boolean drawRectangle(final Graphics g, final MapRect rect,
      final boolean mark) {

    final Context context = GUI.context;
    final Data data = context.data();
    final int o = GUIProp.fontsize;
    final int pre = rect.p;
    final int kind = data.kind(pre);
    final boolean tag = kind == Data.ELEM || kind == Data.DOC;
    final boolean file = fs.isFile(pre);
    final boolean dir = !file && fs.isDir(pre);

    // show full path in top rectangle
    final Nodes current = context.current();
    final byte[] name = kind == Data.DOC ? ViewData.content(data, pre, true) :
      current.size == 1 && pre != 0 && !file && pre == current.nodes[0] ?
          ViewData.path(data, pre) : ViewData.tag(data, pre);

    // image display
    final boolean isImage = GUIFS.mime(name) == GUIFS.Type.IMAGE;
    if(isImage) {
      final Image image = images.get(pre);
      if(image != null) {
        final int ww = rect.w - (PICOFFSET << 1);
        final int hh = rect.h - (PICOFFSET << 1);
        float iw = image.getWidth(view);
        float ih = image.getHeight(view);
        final float min = Math.min(ww / iw, hh / ih);
        if(min < 1) {
          iw *= min;
          ih *= min;
        }
        rect.x += PICOFFSET;
        rect.y += PICOFFSET;
        g.drawImage(image, rect.x + ((ww - (int) iw) >> 1),
            rect.y + ((hh - (int) ih) >> 1), (int) iw, (int) ih, view);
        return false;
      }
    }

    final boolean full = !isImage && rect.w >= GUIProp.fontsize * 12 &&
      rect.h >= GUIProp.fontsize * 8 || rect.w == view.getWidth() &&
      rect.h == view.getHeight();

    final int fullsize = full && file ? 1 : 0;
    final int off = (16 << fullsize) + fullsize * 8;

    final byte[] text = tag ? name : data.text(pre);
    g.setFont(tag ? fullsize == 1 ? GUIConstants.lfont : GUIConstants.font :
      GUIConstants.mfont);

    final Image img = file ? GUIFS.images(name, fullsize) : null;
    final int fh = g.getFontMetrics().getHeight();

    if(fullsize == 0) {
      // Rectangle display
      if(img == null && !file) {
        g.setColor(Color.black);
        BaseXLayout.chopString(g, text, rect.x + 2, rect.y, rect.w);
      } else {
        final int x = rect.x;
        int w = rect.w;
        if(img != null) {
          rect.x += off;
          rect.w -= off;
        }

        final int h = BaseXLayout.calcHeight(g, rect, text);
        if(img != null) {
          if(!mark) {
            g.setColor(GUIConstants.COLORS[rect.l + 1]);
            g.fillRect(x + 1, rect.y + 1, w - 2, h - GUIProp.fontsize);
          }
          g.drawImage(img, x, rect.y + 2, view);
        }

        g.setColor(Color.black);
        BaseXLayout.drawText(g, rect, text);
        if(h == GUIProp.fontsize && img != null) {
          final long size = toLong(fs.size(pre));
          final byte[] info = token(Performance.formatSize(size, false));
          w = BaseXLayout.width(g, info);
          if(BaseXLayout.width(g, text) < rect.w - w - 10) {
            final int ox = rect.x;
            rect.x += rect.w - w - 2;
            BaseXLayout.chopString(g, info, rect.x, rect.y, rect.w);
            rect.x = ox;
          }
        }
        rect.y += h;
        rect.h -= h;
        rect.x -= o;
        rect.w += o;
      }
      rect.x += 3;
      rect.w -= 3;
    } else {
      if(tag) {
        if(GUIFS.mime(name) == GUIFS.Type.IMAGE) return false;

        // Fullscreen Mode
        g.setColor(GUIConstants.COLORS[rect.l + 2]);
        g.fillRect(rect.x + 2, rect.y + 2, rect.w - 5, fh + 12);
        g.drawImage(img, rect.x + 6, rect.y + 6, view);
        rect.y += 18;
        rect.h -= 18;
        g.setColor(Color.black);
        final int w = BaseXLayout.chopString(g, text, rect.x + off + 3,
            rect.y, rect.w - off - 3);

        final long size = toLong(fs.size(pre));
        final String info = GUIFS.desc(text, dir) + ", " +
          Performance.formatSize(size, true);

        rect.w -= 10;
        final int sw = BaseXLayout.width(g, info);
        if(w + sw + 40 < rect.w) {
          g.setColor(GUIConstants.COLORS[rect.l + 10]);
          BaseXLayout.chopString(g, token(info),
              rect.x + rect.w - sw, rect.y, rect.w);
        }
        rect.x += 10;
        rect.y += 30;
        rect.h -= 38;
      } else {
        rect.x += 12;
        rect.y += 12;
        rect.w -= 24;
        rect.h -= 24;
        g.setColor(GUIConstants.COLORS[16]);
        BaseXLayout.drawText(g, rect, data.text(pre));
      }
    }

    if(!file || rect.w < (o << 1) || rect.h < (o << 1)) return false;

    g.setColor(GUIConstants.COLORS[16]);

    rect.y += o >> 1;
    rect.h -= o >> 1;

    // prepare content display; read in first bytes
    long s = 0;
    final byte[] path = ViewData.path(data, pre);
    byte[] fileBuf = null;
    try {
      boolean binary = GUIFS.mime(name) == GUIFS.Type.IMAGE;

      if(!binary) {
        // approximate number of bytes that will be displayed
        s = rect.h * rect.w / o * 4 / GUIConstants.mfwidth['A'];

        // minimize buffer size
        final File f = new File(string(path));
        s = Math.min(s, f.length());
        
        // read file contents
        final FileInputStream fis = new FileInputStream(f);
        fileBuf = new byte[(int) s];
        fis.read(fileBuf);
        fis.close();

        // check if file contains mainly ASCII characters
        int n = 0;
        for(final byte b : fileBuf) if(b >= ' ' || ws(b)) n++;
        binary = (n << 3) + n < (s << 3);
      }

      // set binary string for images and binary files
      if(binary) {
        fileBuf = MAPBINARY;
        s = fileBuf.length;
      }
    } catch(final IOException ex) {
      if(!error) BaseX.debug(FILEERR, path);
      BaseX.debug(ex);
      error = true;
      return true;
    }

    // draw file contents or binary information
    g.setFont(GUIConstants.mfont);
    BaseXLayout.drawText(g, rect, fileBuf, (int) s, true);
    return false;
  }

  @Override
  boolean highlight(final MapRect r, final int mx, final int my,
      final boolean click) {

    final boolean active = r.w >= 16 && r.h >= 16 && my - r.y < 16 &&
      mx - r.x < 16 && fs.isFile(r.p) &&
      GUIFS.mime(fs.name(r.p)) != GUIFS.Type.IMAGE;

    if(active && click) fs.launch(View.focused);
    return active;
  }

  @Override
  void init(final ArrayList<MapRect> rects) {
    final int off = Math.max(GUIProp.fontsize, 16);
    for(final MapRect r : rects) {
      if(r.w > off && r.h > off && fs.isFile(r.p)) {
        final byte[] name = fs.name(r.p);
        if(r.type == -1) r.type = GUIFS.type(name);
        if(GUIFS.mime(name) == GUIFS.Type.IMAGE) {
          final int o = PICOFFSET << 1;
          images.add(r.p, r.w - o, r.h - o);
        }
      }
    }
    images.load();
  }

  @Override
  void reset() {
    images.stop();
  }

  @Override
  void close() {
    images.reset();
  }
}
