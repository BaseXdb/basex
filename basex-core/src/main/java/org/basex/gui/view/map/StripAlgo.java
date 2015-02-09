package org.basex.gui.view.map;

/**
 * StripLayout algorithm.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Joerg Hauser
 */
final class StripAlgo extends MapAlgo {
  @Override
  MapRects calcMap(final MapRect r, final MapList ml, final int ns, final int ne) {
    // stores all calculated rectangles
    final MapRects rects = new MapRects();

    // node iterator
    int ni = ns;
    // first node of current row
    int start = ns;

    // setting initial proportions
    final double yy = r.y;
    final double hh = r.h;
    double xx = r.x;
    double ww = r.w;

    MapRects row = new MapRects();
    double weight = 0;
    double sumweight = 1;
    double rowratio = Double.MAX_VALUE;

    while(ni <= ne && xx + ww <= r.x + r.w && yy + hh <= r.y + r.h) {
      weight += ml.weight[ni];
      int width = (int) (weight / sumweight * ww);
      width = width > 0 ? width : 1;

      final MapRects tmp = new MapRects();

      double y = yy;
      for(int i = start; i <= ni; ++i) {
        int h = i == ni ? (int) (yy + hh - y) :
          (int) (ml.weight[i] / weight * hh);
        h = h > 0 ? h : 1;

        if(yy <= yy + hh)
          tmp.add(new MapRect((int) xx, (int) y, width, h, ml.get(i), r.level));
        else break;
        y += h;
      }
      final double tmpratio = lineRatio(tmp);

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
          row.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh, ml.get(ni), r.level));
          break;
        }
      } else {
        row = tmp;
        rowratio = tmpratio;
        ++ni;
      }
    }
    // adding last row
    for(final MapRect rect : row) rect.w = (int) ww;
    rects.add(row);
    return rects;
  }
}
