package org.basex.gui.view.map;

import java.util.ArrayList;
import org.basex.data.Data;
import org.basex.gui.view.ViewData;
import org.basex.gui.view.ViewRect;
import org.basex.util.IntList;

/**
 * Uses a StripLayout Algorithm to divide Rectangles.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Joerg Hauser
 */
public final class StripLayout extends MapLayout {

  @Override
  void calcMap(final Data data, final ViewRect r,
      final ArrayList<ViewRect> mainRects, final IntList l,
      final int ns, final int ne, final int level) {

    // one rectangle left.. continue with this child
    if(ne - ns <= 1) {
      // calculate rectangle sizes
      final ViewRect t = new ViewRect(r.x, r.y, r.w, r.h, l.list[ns], r.level);

      // position, with and height are calculated using split sizes of
      // former recursion level
      final int x = t.x + layout.x;
      final int y = t.y + layout.y;
      final int w = t.w - layout.w;
      final int h = t.h - layout.h;

      // adding rectangle to list of rects
      mainRects.add(t);

      // skip too small rectangles and leaf nodes (= meta data in deepfs)
      if(w > 0 && h > 0 && !ViewData.isLeaf(data, t.pre)) {
        final IntList ch = children(data, t.pre);
        // scan children
        if(ch.size >= 0) calcMap(data, new ViewRect(x, y, w, h, l.list[ns],
            t.level + 1), mainRects, ch, 0, ch.size - 1, level + 1);
      }
    } else {
      // number of nodes used to calculate rect size
      int nn = l.list[ne] - l.list[ns];
      int ni = ns;
      // running start holding first element of current row
      int start = ns;

      // setting initial proportions
      double xx = r.x;
      double yy = r.y;
      double ww = r.w;
      double hh = r.h;

      ArrayList<ViewRect> row = new ArrayList<ViewRect>();
      double height = 0;
      while(ni < ne) {
        // height of current strip
        /*if(ni != ne) {*/
          height = (l.list[ni + 1] - l.list[start]) * hh / nn;
          ArrayList<ViewRect> tmp = new ArrayList<ViewRect>();
          // create temporary row including current rectangle
          double x = xx;
          for(int i = start; i <= ni; i++) {
            double w = (l.list[i + 1] - l.list[i]) * ww /
              (l.list[ni + 1] - l.list[start]);
            tmp.add(new ViewRect((int) x, (int) yy, (int) w, (int) height,
                l.list[i], level));
            x += w;
          }

          // if ar has increased discard tmp and add row
          if(allRatio(tmp) > allRatio(row)) {
            // add rects of row using recursion
            for(int i = 0; i < row.size(); i++) {
              IntList newl = new IntList(1);
              newl.add(row.get(i).pre);
              calcMap(data, row.get(i), mainRects, newl, 0, 1, level);
            }
            // preparing for new line to lay out
            hh -= row.get(0).h;
            yy += row.get(0).h;
            tmp.clear();
            row.clear();
            start = ni;
            nn = l.list[ne] - l.list[start];
            // sometimes there has to be one rectangles to fill the left space
            if(ne == ni + 1) {
              row.add(new ViewRect((int) xx, (int) yy, (int) ww, (int) hh,
                  l.list[ni], level));
              break;
            }
          }
          row = tmp;
          ni++;
      }

      // adding remaining rectangles
      for(int i = 0; i < row.size(); i++) {
        IntList newl = new IntList(1);
        newl.add(row.get(i).pre);
        calcMap(data, row.get(i), mainRects, newl, 0, 1, level);
      }
    }
  }
}