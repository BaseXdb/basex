package org.basex.gui.view.map;

import org.basex.gui.GUIProp;

/**
 * StripLayout Algorithm.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Joerg Hauser
 */
final class StripAlgo extends MapAlgo{
  @Override
  MapRects calcMap(final MapRect r, final MapList ml,
      final int ns, final int ne) {
    // stores all calculated rectangles
    final MapRects rects = new MapRects();

    // node iterator
    int ni = ns;
    // first node of current row
    int start = ns;

    // setting initial proportions
    double xx = r.x;
    double yy = r.y;
    double ww = r.w;
    double hh = r.h;

    MapRects row = new MapRects();
    int height = 0;
    int width = 0;
    double weight = 0;
    double sumweight = 1;
    double tmpratio;
    double rowratio = Double.MAX_VALUE;

    if(GUIProp.striphor) {
      while(ni <= ne && xx + ww <= r.x + r.w && yy + hh <= r.y + r.h) {
        weight += ml.weight[ni];
        height = (int) (weight / sumweight * hh);
        height = height > 0 ? height : 1;

        final MapRects tmp = new MapRects();

        double x = xx;
        for(int i = start; i <= ni; i++) {
          int w = i == ni ? (int) (xx + ww - x) :
            (int) (ml.weight[i] / weight * ww);
          w = w > 0 ? w : 1;

          if(yy + height <= yy + hh)
            tmp.add(new MapRect((int) x, (int) yy, w, height, ml.get(i),
                r.level));
          else break;
          x += w;
        }
        tmpratio = lineRatio(tmp);

        // if ar has increased discard tmp and add row
        if(tmpratio > rowratio) {
          // add rects of row to solution
          rects.add(row);
          rowratio = Double.MAX_VALUE;
          // preparing next line
          hh -= row.get(0).h;
          yy += row.get(0).h;
          tmp.reset();
          row.reset();
          start = ni;
          sumweight -= weight - ml.weight[ni];
          weight = 0;
          // sometimes there has to be one rectangles to fill the left space
          if(ne == ni) {
            row.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh,
                ml.get(ni), r.level));
            break;
          }
        } else {
          row = tmp;
          rowratio = tmpratio;
          ni++;
        }
      }
      // adding last row
      for(final MapRect rect : row) rect.h = (int) hh;
      rects.add(row);

      return rects;
    }

    while(ni <= ne && xx + ww <= r.x + r.w && yy + hh <= r.y + r.h) {
      weight += ml.weight[ni];
      width = (int) (weight / sumweight * ww);
      width = width > 0 ? width : 1;

      final MapRects tmp = new MapRects();

      double y = yy;
      for(int i = start; i <= ni; i++) {
        int h = i == ni ? (int) (yy + hh - y) :
          (int) (ml.weight[i] / weight * hh);
        h = h > 0 ? h : 1;

        if(yy + height <= yy + hh)
          tmp.add(new MapRect((int) xx, (int) y, width, h, ml.get(i),
              r.level));
        else break;
        y += h;
      }
      tmpratio = lineRatio(tmp);

      // if ar has increased discard tmp and add row
      if(tmpratio > rowratio) {
        // add rects of row to solution
        rects.add(row);
        rowratio = Double.MAX_VALUE;
        // preparing next line
        ww -= row.get(0).w;
        xx += row.get(0).w;
        tmp.reset();
        row.reset();
        start = ni;
        sumweight -= weight - ml.weight[ni];
        weight = 0;
        // sometimes there has to be one rectangles to fill the left space
        if(ne == ni) {
          row.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh,
              ml.get(ni), r.level));
          break;
        }
      } else {
        row = tmp;
        rowratio = tmpratio;
        ni++;
      }
    }
    // adding last row
    for(final MapRect rect : row) rect.w = (int) ww;
    rects.add(row);
    return rects;
  }

  @Override
  String getName() {
    return "Strip";
  }
}
