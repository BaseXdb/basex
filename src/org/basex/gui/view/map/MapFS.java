package org.basex.gui.view.map;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.GUIFS;
import org.basex.gui.GUIProp;
import org.basex.gui.dialog.Dialog;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.ViewData;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;
import org.deepfs.fs.DeepFS;
import org.deepfs.fsml.FileType;
import org.deepfs.fsml.MetaElem;
import org.deepfs.fsml.MimeType;

/**
 * Paint filesystem specific TreeMap rectangles.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class MapFS extends MapPainter {
  /** Image offset. */
  private static final int PICOFFSET = 2;
  /** Image cache. */
  private static MapFSImages images;
  /** Data FS reference. */
  private final DeepFS fs;

  /**
   * Constructor.
   * @param m map reference
   * @param pr gui properties
   * @param f fs reference
   */
  MapFS(final MapView m, final DeepFS f, final GUIProp pr) {
    super(m, pr);
    fs = f;
    if(images == null) images = new MapFSImages(m);
  }

  @Override
  void drawRectangles(final Graphics g, final MapRects rects,
      final float scale) {
    final Data data = view.gui.context.data;
    final MapRect l = view.layout.layout;
    l.x = (int) scale * l.x; l.y = (int) scale * l.y;
    l.w = (int) scale * l.w; l.h = (int) scale * l.h;
    final int ww = view.getWidth();
    final int hh = view.getHeight();
    final int min = Math.max(prop.num(GUIProp.FONTSIZE), 16);

    final int off = prop.num(GUIProp.MAPOFFSETS);
    final int fsz = prop.num(GUIProp.FONTSIZE);
    final int rs = rects.size;
    for(int ri = 0; ri < rs; ri++) {
      // get rectangle information
      final MapRect r = rects.get(ri);
      final int pre = r.pre;

      // level 1: next context node, set marker pointer to 0
      final int lvl = r.level;

      final boolean isImage = GUIFS.mime(fs.name(r.pre)) == GUIFS.Type.IMAGE;
      final boolean full = r.w == ww && r.h == hh;
      Color col = color(rects, ri);
      final boolean mark = col != null;

      if(full && isImage) col = Color.black;
      else if(full || col == null) col = COLORS[lvl];
      g.setColor(col);

      if(r.w < l.x + l.w || r.h < l.y + l.h || off < 2 ||
          ViewData.isLeaf(prop, data, pre)) {
        g.fillRect(r.x, r.y, r.w, r.h);
      } else {
        // painting only border for non-leaf nodes..
        g.fillRect(r.x, r.y, l.x, r.h);
        g.fillRect(r.x, r.y, r.w, l.y);
        g.fillRect(r.x + r.w - l.w, r.y, l.w, r.h);
        g.fillRect(r.x, r.y + r.h - l.h, r.w, l.h);
      }

      if(!full) {
        col = mark ? colormark3 : COLORS[lvl + 2];
        g.setColor(col);
        g.drawRect(r.x, r.y, r.w, r.h);
        col = mark ? colormark4 : COLORS[Math.max(0, lvl - 2)];
        g.setColor(col);
        g.drawLine(r.x + r.w, r.y, r.x + r.w, r.y + r.h);
        g.drawLine(r.x, r.y + r.h, r.x + r.w, r.y + r.h);
      }

      // skip drawing of string when left space is too small
      if(r.w < min || r.h < min) continue;

      if(drawRectangle(g, r.copy())) {
        r.x += 4;
        r.w -= 8;

        final TokenBuilder tb = new TokenBuilder();
        int k = data.kind(pre);
        final int s = pre + data.size(pre, k);
        int p = pre + data.attSize(pre, k);
        boolean elem = false;
        while(p != s) {
          k = data.kind(p);
          if(k == Data.ELEM) {
            if(elem) tb.add('\n');
            tb.add(data.name(p, k));
            tb.add(": ");
            elem = true;
          } else if(k == Data.TEXT) {
            tb.add(data.text(p, true));
            tb.add('\n');
            elem = false;
          }
          p += data.attSize(p, k);
        }
        r.x -= 4;
        r.w += 8;
        g.setFont(mfont);
        MapRenderer.drawText(g, r, tb.finish(), fsz);
      }
    }
  }

  @Override
  boolean drawRectangle(final Graphics g, final MapRect rect) {
    final Context context = view.gui.context;
    final Data data = context.data;
    final int fsz = prop.num(GUIProp.FONTSIZE);
    final int pre = rect.pre;
    final int kind = data.kind(pre);
    final boolean tag = kind == Data.ELEM || kind == Data.DOC;
    final boolean file = fs.isFile(pre);

    // show full path in top rectangle
    final Nodes current = context.current;
    final byte[] name = kind == Data.DOC ? ViewData.content(data, pre, true) :
      current.size() == 1 && pre != 0 && !file && pre == current.nodes[0] ?
          ViewData.path(data, pre) : ViewData.tag(prop, data, pre);

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
        g.drawImage(image, rect.x + PICOFFSET + (ww - (int) iw >> 1), rect.y +
            PICOFFSET + (hh - (int) ih >> 1), (int) iw, (int) ih, view);
        return false;
      }
    }

    final boolean full = !isImage && rect.w >= prop.num(GUIProp.FONTSIZE) * 12
      && rect.h >= prop.num(GUIProp.FONTSIZE) * 8 || rect.w == view.getWidth()
      && rect.h == view.getHeight();

    final int fullsize = full && file && prop.is(GUIProp.MAPFS) ? 1 : 0;
    final int off = (16 << fullsize) + fullsize * 8;

    final byte[] text = tag ? name : data.text(pre, true);
    g.setFont(tag ? fullsize == 1 ? lfont : font : mfont);

    // determine icon size
    final Image icon = file ? GUIFS.images(name, fullsize) : null;
    final int fh = g.getFontMetrics().getHeight();

    if(fullsize == 0) {
      // Rectangle display
      if(icon != null) {
        g.drawImage(icon, rect.x, rect.y + 2, view);
        rect.x += off;
        rect.w -= off;
      }
      g.setColor(Color.black);
      BaseXLayout.chopString(g, text, rect.x + 2, rect.y, rect.w - 2, fsz);
      rect.y += fsz;
      rect.h -= fsz;

      if(icon != null) {
        rect.x -= off;
        rect.w += off;
      }
      rect.x += 3;
      rect.w -= 3;
    } else {
      // paint bigger header
      if(tag) {
        if(GUIFS.mime(name) == GUIFS.Type.IMAGE) return false;

        // Fullscreen Mode
        g.setColor(COLORS[rect.level + 2]);
        g.fillRect(rect.x + 2, rect.y + 2, rect.w - 5, fh + 12);
        g.drawImage(icon, rect.x + 6, rect.y + 6, view);
        rect.y += 18;
        rect.h -= 18;
        g.setColor(Color.black);
        final int w = BaseXLayout.chopString(g, text, rect.x + off + 3,
            rect.y, rect.w - off - 3, fsz);

        final long size = toLong(fs.size(pre));
        
        String type = "";
        String format = "";
        final int nodeSize = data.size(pre, Data.ELEM) + pre;
        final byte[] typeElem = MetaElem.TYPE.tok();
        final byte[] formatElem = MetaElem.FORMAT.tok();
        for(int node = pre; node < nodeSize; ++node) {
          final int k = data.kind(node);
          if(k != Data.ELEM) continue;
          final byte[] nodeName = data.name(node, k);
          if(eq(nodeName, typeElem))
            type = string(ViewData.content(data, node + 1, false));
          else if(eq(nodeName, formatElem))
            format = string(ViewData.content(data, node + 1, false));
          if(!type.isEmpty() && !format.isEmpty()) break;
        }
        if(type.isEmpty()) type = FileType.UNKNOWN_TYPE.toString();
        if(format.isEmpty()) format = MimeType.UNKNOWN.toString();
        
        final String info = type + " (" + format + ")" + ", "
            + Performance.format(size, true); 

        rect.w -= 10;
        final int sw = BaseXLayout.width(g, info);
        if(w + sw + 48 < rect.w) {
          g.setColor(COLORS[rect.level + 10]);
          BaseXLayout.chopString(g, token(info),
              rect.x + rect.w - sw, rect.y, rect.w, fsz);
        }
        rect.x += 10;
        rect.y += 30;
        rect.w -= 10;
        rect.h -= 38;
      } else {
        rect.x += 12;
        rect.y += 12;
        rect.w -= 24;
        rect.h -= 24;
        g.setColor(Color.black);
        MapRenderer.drawText(g, rect, data.text(pre, true),
            prop.num(GUIProp.FONTSIZE));
      }
    }
    if(!file || rect.w < fsz << 1 || rect.h < fsz << 1) return false;

    rect.y += fsz >> 1;
    rect.h -= fsz >> 1;

    rect.pos = null;
    // prepare content display
    byte[] fileBuf = EMPTY;
    if(file) {
      if(!(GUIFS.mime(name) == GUIFS.Type.IMAGE)) {
        final byte[] textElem = DeepFS.TEXT_CONTENT;
        final int size = data.size(pre, Data.ELEM) + pre;
        out: for(int node = pre; node < size; ++node) {
          final byte[] nodeName = data.name(node, data.kind(node));
          if(eq(nodeName, textElem)) {
            int textNodeSize = data.size(node, data.kind(node));
            while(--textNodeSize > 0) {
              if(data.kind(++node) == Data.TEXT) {
                rect.pos = view.gui.context.marked.ftpos.get(node);
                fileBuf = ViewData.content(data, node, false);
                break out;
              }
            }
          }
        }
      }

      // set binary string for images and binary files
      if(fileBuf.length == 0) fileBuf = MAPBINARY;
    }

    g.setColor(Color.black);
    g.setFont(mfont);

    try {
      // check if text fits in rectangle
      rect.thumb = MapRenderer.calcHeight(g, rect, fileBuf, fsz) >= rect.h;
      if(rect.thumb) {
        MapRenderer.drawThumbnails(g, rect, fileBuf, fsz);
      } else {
        MapRenderer.drawText(g, rect, fileBuf, fsz);
      }
    } catch(final Exception ex) {
      // ignore errors for binary files which have been interpreted as texts
    }
    return false;
  }

  @Override
  boolean mouse(final MapRect r, final int mx, final int my,
      final boolean click) {

    final boolean active = r.w >= 16 && r.h >= 16 && my - r.y < 16 &&
      mx - r.x < 16 && fs.isFile(r.pre) &&
      GUIFS.mime(fs.name(r.pre)) != GUIFS.Type.IMAGE;

    if(active && click) {
      try {
        fs.launch(view.gui.context.focused);
      } catch (final IOException ex) {
        Dialog.warn(view.gui, NODEFAULTAPP);;
      }
    }
    return active;
  }

  @Override
  void init(final MapRects rects) {
    final int off = Math.max(prop.num(GUIProp.FONTSIZE), 16);
    for(final MapRect r : rects) {
      if(r.w > off && r.h > off && fs.isFile(r.pre)) {
        final byte[] name = fs.name(r.pre);
        if(r.type == -1) r.type = GUIFS.type(name);
        if(GUIFS.mime(name) == GUIFS.Type.IMAGE) {
          final int o = PICOFFSET << 1;
          images.add(r.pre, r.w - o, r.h - o);
        }
      }
    }
    images.load();
  }

  @Override
  void reset() {
    images.reset();
  }

  @Override
  void close() {
    images.close();
  }
}
