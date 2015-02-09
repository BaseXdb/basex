package org.basex.gui.view.map;

/**
 * Squarified layout algorithm.
 *
 * @author BaseX Team 2005-15, BSD License
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
      final double tmpratio;
      if(ww < hh) {
        weight += ml.weight[ni];
        int height = (int) (weight / sumweight * hh);
        height = height > 0 ? height : 1;

        final MapRects tmp = new MapRects();
        double x = xx;
        for(int i = start; i <= ni; ++i) {
          int w = i == ni ? (int) (xx + ww - x) :
            (int) (ml.weight[i] / weight * ww);
          w = w > 0 ? w : 1;
          if(x + w <= xx + ww)
            tmp.add(new MapRect((int) x, (int) yy, w, height, ml.get(i), r.level));
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
            row.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh, ml.get(ni), r.level));
            break;
          }
        } else {
          row = tmp;
          rowratio = tmpratio;
          ++ni;
        }
      } else {
        weight += ml.weight[ni];
        int width = (int) (weight / sumweight * ww);
        width = width > 0 ? width : 1;
        final MapRects tmp = new MapRects();

        double y = yy;
        for(int i = start; i <= ni; ++i) {
          int h = i == ni ? (int) (yy + hh - y) :
            (int) (ml.weight[i] / weight * hh);
          h = h > 0 ? h : 1;
          if(y + h <= yy + hh)
            tmp.add(new MapRect((int) xx, (int) y, width, h, ml.get(i), r.level));
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
            row.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh, ml.get(ni), r.level));
            break;
          }
        } else {
          row = tmp;
          rowratio = tmpratio;
          ++ni;
        }
      }
    }

    for(final MapRect rect : row) rect.h = (int) hh;
    // adding last row
    rects.add(row);

    return rects;
  }
}
