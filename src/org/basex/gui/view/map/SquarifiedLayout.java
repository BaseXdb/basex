package org.basex.gui.view.map;

import java.util.ArrayList;
import org.basex.data.Data;
//import org.basex.gui.view.ViewData;
import org.basex.gui.view.ViewRect;
import org.basex.util.IntList;

/**
 * Uses a Squarified Algorithm to divide Rectangles.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Joerg Hauser
 */
public final class SquarifiedLayout extends MapLayout {

  @Override
  void calcMap(final Data data, final ViewRect r,
      final ArrayList<ViewRect> mainRects, final IntList l,
      final int ns, final int ne, final int level) {
    // one rectangle left...
    if(ne - ns == 1) {
      putRect(data, r, mainRects, l, ns, level);
    // subdivide list
    } else {
      // number of nodes used to calculate rect size
      int nn = l.list[ne] - l.list[ns];
      int ni = ns;
      // running start holding first element of current row
      int start = ns;

      // determine direction
      final boolean v = (r.w > r.h) ? false : true;

      // setting initial proportions
      double xx = r.x;
      double yy = r.y;
      double ww = r.w;
      double hh = r.h;

      if(v) {
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
            if(lineRatio(tmp) > lineRatio(row)) {
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
      } else {
        ArrayList<ViewRect> row = new ArrayList<ViewRect>();
        double width = 0;
        while(ni < ne) {
          // height of current strip
          /*if(ni != ne) {*/
            width = (l.list[ni + 1] - l.list[start]) * ww / nn;
            ArrayList<ViewRect> tmp = new ArrayList<ViewRect>();
            // create temporary row including current rectangle
            double y = yy;
            for(int i = start; i <= ni; i++) {
              double h = (l.list[i + 1] - l.list[i]) * hh /
                (l.list[ni + 1] - l.list[start]);
              tmp.add(new ViewRect((int) xx, (int) y, (int) width, (int) h,
                  l.list[i], level));
              y += h;
            }

            // if ar has increased discard tmp and add row
            if(lineRatio(tmp) > lineRatio(row)) {
              // add rects of row using recursion
              for(int i = 0; i < row.size(); i++) {
                IntList newl = new IntList(1);
                newl.add(row.get(i).pre);
                calcMap(data, row.get(i), mainRects, newl, 0, 1, level);
              }
              // preparing for new line to lay out
              ww -= row.get(0).w;
              xx += row.get(0).w;
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

  @Override
  String getType() {
    return "SquarifiedLayout";
  }
}