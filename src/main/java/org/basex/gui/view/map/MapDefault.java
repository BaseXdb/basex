package org.basex.gui.view.map;

import java.awt.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.*;

/**
 * Adds default paint operations to TreeMap.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class MapDefault extends MapPainter {
  /**
   * Constructor.
   * @param m map reference
   * @param pr gui properties
   */
  MapDefault(final MapView m, final GUIProp pr) {
    super(m, pr);
  }

  @Override
  void drawRectangles(final Graphics g, final MapRects rects,
      final float scale) {
    // some additions to set up borders
    final MapRect l = view.layout.layout;
    l.x = (int) scale * l.x; l.y = (int) scale * l.y;
    l.w = (int) scale * l.w; l.h = (int) scale * l.h;
    final int ww = view.getWidth();
    final int hh = view.getWidth();
    final Data data = view.gui.context.data();

    final int off = prop.num(GUIProp.MAPOFFSETS);
    final int rs = rects.size;
    for(int ri = 0; ri < rs; ++ri) {
      // get rectangle information
      final MapRect r = rects.get(ri);
      final int pre = r.pre;

      // level 1: next context node, set marker pointer to 0
      final int lvl = r.level;

      final boolean full = r.w == ww && r.h == hh;
      Color col = color(rects, ri);
      final boolean mark = col != null;

      r.pos = view.gui.context.marked.ftpos != null ?
          view.gui.context.marked.ftpos.get(data, pre) : null;
      g.setColor(mark ? col : GUIConstants.color(lvl));

      if(r.w < l.x + l.w || r.h < l.y + l.h || off < 2 ||
          ViewData.leaf(prop, data, pre)) {
        g.fillRect(r.x, r.y, r.w, r.h);
      } else {
        // painting only border for non-leaf nodes..
        g.fillRect(r.x, r.y, l.x, r.h);
        g.fillRect(r.x, r.y, r.w, l.y);
        g.fillRect(r.x + r.w - l.w, r.y, l.w, r.h);
        g.fillRect(r.x, r.y + r.h - l.h, r.w, l.h);
      }

      if(!full) {
        col = mark ? GUIConstants.colormark3 : GUIConstants.color(lvl + 2);
        g.setColor(col);
        g.drawRect(r.x, r.y, r.w, r.h);
        col = mark ? GUIConstants.colormark4 :
          GUIConstants.color(Math.max(0, lvl - 2));
        g.setColor(col);
        g.drawLine(r.x + r.w, r.y, r.x + r.w, r.y + r.h);
        g.drawLine(r.x, r.y + r.h, r.x + r.w, r.y + r.h);
      }

      // skip drawing of string if there is no space
      if(r.w > 3 && r.h >= prop.num(GUIProp.FONTSIZE)) drawRectangle(g, r);
    }
  }

  /**
   * Draws a single rectangle.
   *
   * @param g graphics reference
   * @param rect rectangle to be drawn
   */
  private void drawRectangle(final Graphics g, final MapRect rect) {
    rect.x += 3;
    rect.w -= 3;

    final int pre = rect.pre;
    final Context context = view.gui.context;
    final Data data = context.data();
    final Nodes current = context.current();
    final int kind = data.kind(pre);
    final int fsz = prop.num(GUIProp.FONTSIZE);

    if(kind == Data.ELEM || kind == Data.DOC) {
      // show full path in top rectangle
      final byte[] name = kind == Data.DOC ? ViewData.content(data, pre, true) :
        current.size() == 1 && pre != 0 && pre == current.list[0] ?
            ViewData.path(data, pre) : ViewData.tag(prop, data, pre);

      g.setColor(Color.black);
      g.setFont(GUIConstants.font);
      BaseXLayout.chopString(g, name, rect.x, rect.y, rect.w, fsz);
    } else {
      g.setColor(GUIConstants.color(rect.level * 2 + 8));
      g.setFont(GUIConstants.mfont);
      final byte[] text = ViewData.content(data, pre, false);

      rect.thumb = MapRenderer.calcHeight(g, rect, text, fsz) >= rect.h;
      if(rect.thumb) {
        MapRenderer.drawThumbnails(g, rect, text, fsz);
      } else {
        MapRenderer.drawText(g, rect, text, fsz);
      }
    }
    rect.x -= 3;
    rect.w += 3;
  }
}
