package org.basex.gui.view.map;

/**
 * Squarified layout algorithm.
 *
 * @author BaseX Team, BSD License
 * @author Joerg Hauser
 */
final class SquarifiedAlgo extends MapAlgo {
  @Override
  MapRects calcMap(final MapRect r, final MapList ml, final int ns, final int ne) {
    ml.sort();

    final MapRects rects = new MapRects();
    int ni = ns;
    // running start holding first element of current row
    int start = ns;

    // setting initial proportions
    double xx = r.x;
    double yy = r.y;
    double ww = r.w;
    double hh = r.h;

    MapRects row = new MapRects();
    double weight = 0;
    double sumweight = 1;
    double rowratio = Double.MAX_VALUE;

    while(ni <= ne && xx + ww <= r.x + r.w && yy + hh <= r.y + r.h) {
      // horizontal rows are laid out if the remaining space is higher than wide
      final boolean horiz = ww < hh;
      final double pos = horiz ? xx : yy, len = horiz ? ww : hh;
      weight += ml.weight[ni];
      final int size = Math.max(1, (int) (weight / sumweight * (horiz ? hh : ww)));

      final MapRects tmp = new MapRects();
      double p = pos;
      for(int i = start; i <= ni; i++) {
        final int l = Math.max(1, (int) (i == ni ? pos + len - p : ml.weight[i] / weight * len));
        if(p + l > pos + len) break;
        tmp.add(horiz ? new MapRect((int) p, (int) yy, l, size, ml.get(i), r.level) :
          new MapRect((int) xx, (int) p, size, l, ml.get(i), r.level));
        p += l;
      }
      final double tmpratio = lineRatio(tmp);

      // if ar has increased discard tmp and add row
      if(tmpratio > rowratio) {
        // add rects of row to solution
        rects.add(row);
        rowratio = Double.MAX_VALUE;
        // preparing next line
        if(horiz) {
          hh -= row.get(0).h;
          yy += row.get(0).h;
        } else {
          ww -= row.get(0).w;
          xx += row.get(0).w;
        }
        row.reset();
        start = ni;
        sumweight -= weight - ml.weight[ni];
        weight = 0;
        // sometimes there has to be one rectangle to fill the left space
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

    for(final MapRect rect : row) rect.h = (int) hh;
    // adding last row
    rects.add(row);

    return rects;
  }
}
