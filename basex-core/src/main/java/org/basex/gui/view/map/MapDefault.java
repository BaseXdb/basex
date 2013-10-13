package org.basex.gui.view.map;

import java.awt.*;

import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.*;

/**
 * Adds default paint operations to TreeMap.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class MapDefault extends MapPainter {
  /**
   * Constructor.
   * @param m map reference
   * @param opts gui options
   */
  MapDefault(final MapView m, final GUIOptions opts) {
    super(m, opts);
  }

  @Override
  void drawRectangles(final Graphics g, final MapRects rects, final float scale) {
    // some additions to set up borders
    final MapRect l = view.layout.layout;
    l.x = (int) scale * l.x; l.y = (int) scale * l.y;
    l.w = (int) scale * l.w; l.h = (int) scale * l.h;
    final int ww = view.getWidth();
    final int hh = view.getWidth();

    final Data data = view.gui.context.data();
    final int fsz = GUIConstants.fontSize;

    final int off = gopts.get(GUIOptions.MAPOFFSETS);
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
          ViewData.leaf(gopts, data, pre)) {
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
      if(r.w <= 3 || r.h < GUIConstants.fontSize) continue;

      r.x += 3;
      r.w -= 3;

      final int kind = data.kind(pre);
      if(kind == Data.ELEM || kind == Data.DOC) {
        g.setColor(Color.black);
        g.setFont(GUIConstants.font);
        BaseXLayout.chopString(g, ViewData.name(gopts, data, pre), r.x, r.y, r.w, fsz);
      } else {
        g.setColor(GUIConstants.color(r.level * 2 + 8));
        g.setFont(GUIConstants.mfont);
        final byte[] text = ViewData.content(data, pre, false);

        r.thumb = MapRenderer.calcHeight(g, r, text, fsz) >= r.h;
        if(r.thumb) {
          MapRenderer.drawThumbnails(g, r, text, fsz);
        } else {
          MapRenderer.drawText(g, r, text, fsz);
        }
      }
      r.x -= 3;
      r.w += 3;
    }
  }
}
