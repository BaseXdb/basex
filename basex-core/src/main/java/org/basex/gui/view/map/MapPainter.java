package org.basex.gui.view.map;

import java.awt.*;

import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.view.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.seq.*;

/**
 * Adds default paint operations to the tree map.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class MapPainter {
  /** Graphics reference. */
  private final MapView view;
  /** GUI options. */
  private final GUIOptions gopts;

  /**
   * Constructor.
   * @param view map reference
   * @param gopts gui options
   */
  MapPainter(final MapView view, final GUIOptions gopts) {
    this.view = view;
    this.gopts = gopts;
  }

  /**
   * Returns color mark.
   * @param rects rectangles to be drawn
   * @param ri current position
   * @param data data reference
   * @return next color mark or {@code null}
   */
  private Color color(final MapRects rects, final int ri, final Data data) {
    // find marked node
    final DBNodes marked = view.gui.context.marked;
    if(marked != null) {
      final int pre = rects.get(ri).pre;
      final int m = -marked.find(pre) - 1;
      if(m >= 0) {
        // mark ancestor of invisible node
        final int r = rects.find(pre);
        if(m < marked.size()) {
          // find pre value of next rectangle
          final int e;
          if(r + 1 < rects.size) {
            e = rects.sorted[r + 1].pre;
          } else {
            // rectangle is last one: get rectangle size via database
            final int spre = rects.sorted[r].pre;
            e = data.size(spre, data.kind(spre));
          }
          // mark rectangle if pre value is its descendant
          return marked.sorted(m) < e ? GUIConstants.colormark2 : null;
        }
        return null;
      }
    }
    // no mark found
    return GUIConstants.colormark1;
  }

  /**
   * Draws the specified rectangles.
   * @param g graphics reference
   * @param rects rectangles to be drawn
   */
  void drawRectangles(final Graphics g, final MapRects rects) {
    // some additions to set up borders
    final MapRenderer renderer = new MapRenderer(g);
    final MapRect l = view.layout.layout;
    final int ww = view.getWidth(), hh = view.getHeight();

    final Data data = view.gui.context.data();
    final FTPosData ftpos = view.gui.context.marked.ftpos();

    final int o = gopts.get(GUIOptions.MAPOFFSETS);
    final int rs = rects.size;
    for(int ri = 0; ri < rs; ++ri) {
      // get rectangle information
      final MapRect rect = rects.get(ri);
      final int pre = rect.pre;

      // level 1: next context node, set marker pointer to 0
      final int lvl = rect.level;
      final boolean full = rect.w == ww && rect.h == hh;
      Color col = color(rects, ri, data);
      final boolean mark = col != null;

      rect.pos = ftpos != null ? ftpos.get(data, pre) : null;
      g.setColor(mark ? col : GUIConstants.color(lvl));

      if(rect.w < l.x + l.w || rect.h < l.y + l.h || o < 2 || ViewData.leaf(gopts, data, pre)) {
        g.fillRect(rect.x, rect.y, rect.w, rect.h);
      } else {
        // painting only border for non-leaf nodes..
        g.fillRect(rect.x, rect.y, l.x, rect.h);
        g.fillRect(rect.x, rect.y, rect.w, l.y);
        g.fillRect(rect.x + rect.w - l.w, rect.y, l.w, rect.h);
        g.fillRect(rect.x, rect.y + rect.h - l.h, rect.w, l.h);
      }

      if(!full) {
        col = mark ? GUIConstants.colormark3 : GUIConstants.color(lvl + 2);
        g.setColor(col);
        g.drawRect(rect.x, rect.y, rect.w, rect.h);
        col = mark ? GUIConstants.colormark4 : GUIConstants.color(Math.max(0, lvl - 2));
        g.setColor(col);
        g.drawLine(rect.x + rect.w, rect.y, rect.x + rect.w, rect.y + rect.h);
        g.drawLine(rect.x, rect.y + rect.h, rect.x + rect.w, rect.y + rect.h);
      }

      // skip drawing of string if there is no space
      if(rect.w <= 3 || rect.h < GUIConstants.fontSize) continue;

      rect.x += 3;
      rect.w -= 3;

      final int kind = data.kind(pre);
      if(kind == Data.ELEM || kind == Data.DOC) {
        g.setColor(GUIConstants.TEXT);
        g.setFont(GUIConstants.font);
        renderer.chopText(ViewData.namedText(gopts, data, pre), rect.x, rect.y, rect.w);
      } else {
        g.setColor(GUIConstants.color((rect.level << 1) + 8));
        g.setFont(GUIConstants.mfont);
        final byte[] text = ViewData.text(data, pre);
        rect.thumb = renderer.calcHeight(rect, text) >= rect.h;
        if(rect.thumb) {
          renderer.drawThumbnails(rect, text);
        } else {
          renderer.drawText(rect, text);
        }
      }
      rect.x -= 3;
      rect.w += 3;
    }
  }

  /**
   * Returns textual contents for a rectangle.
   * @param data data reference
   * @param mr map rectangle
   * @return byte[] content
   */
  static byte[] text(final Data data, final MapRect mr) {
    return ViewData.text(data, mr.pre);
  }
}
