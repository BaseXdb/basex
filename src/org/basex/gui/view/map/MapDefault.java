package org.basex.gui.view.map;

import java.awt.Color;
import java.awt.Graphics;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.ViewData;

/**
 * Adds default paint operations to TreeMap.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class MapDefault extends MapPainter {
  /**
   * Constructor.
   * @param m map reference.
   */
  MapDefault(final MapView m) {
    super(m);
  }

  @Override
  void drawRectangles(final Graphics g, final MapRects rects) {
    final MapRect l = view.layout;
    final int ww = view.getWidth();
    final int hh = view.getWidth();
    final Data data = GUI.context.data();

    mpos = 0;
    final int rs = rects.size;
    for(int ri = 0; ri < rs; ri++) {
      // get rectangle information
      final MapRect r = rects.get(ri);
      final int pre = r.p;
      
      // level 1: next context node, set marker pointer to 0
      final int lvl = r.l;
      if(lvl == 0) mpos = 0;

      final boolean full = r.w == ww && r.h == hh;

      // draw rectangle
      Color color = nextMark(rects, pre, ri, rs);
      final boolean mark = color != null;

      g.setColor(mark ? color : GUIConstants.COLORS[lvl]);
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
      if(r.w < GUIProp.fontsize || r.h < GUIProp.fontsize) continue;

      r.thumb = drawRectangle(g, r.clone());
    }
  }

  /**
   * Draws a single rectangle.
   * @param g graphics reference
   * @param rect rectangle
   * @return if the current rectangle is shown as thumbnail
   */
  boolean drawRectangle(final Graphics g, final MapRect rect) {
    rect.x += 3;
    rect.w -= 3;
    final int pre = rect.p;
    final Context context = GUI.context;
    final Data data = context.data();
    final Nodes current = context.current();
    final int kind = data.kind(pre);

    if(kind == Data.ELEM || kind == Data.DOC) {
      // show full path in top rectangle
      final byte[] name = current.size == 1 && pre != 0 &&
        pre == current.pre[0] ? ViewData.path(data, pre) :
          ViewData.tag(data, pre);

      g.setColor(Color.black);
      g.setFont(GUIConstants.font);
      BaseXLayout.chopString(g, name, rect.x, rect.y, rect.w);
    } else {
      g.setColor(GUIConstants.COLORS[Math.min(255, rect.l * 2 + 8)]);
      g.setFont(GUIConstants.mfont);
      final byte[] text = ViewData.content(data, pre, false);
      
      final int p = BaseXLayout.centerPos(g, text, rect.w);
      if(p != -1) {
        rect.x += p;
        rect.y += (rect.h - GUIProp.fontsize) / 2 - 1;
        BaseXLayout.drawText(g, rect, text);
      } else {
        if(BaseXLayout.calcHeight(g, rect, text) < rect.h) {
          BaseXLayout.drawText(g, rect, text);
        } else {
          BaseXLayout.drawThumbnails(g, rect, text);
          return true;
        }
      }
    }
    return false;
  }

  @Override
  boolean highlight(final MapRect rect, final int mx, final int my,
      final boolean click) {
    return false;
  }

  @Override
  void init(final MapRects rects) {
  }

  @Override
  void reset() {
  }

  @Override
  void close() {
  }
}
