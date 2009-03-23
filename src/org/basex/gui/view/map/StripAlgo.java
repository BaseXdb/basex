package org.basex.gui.view.map;

/**
 * StripLayout Algorithm.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Joerg Hauser
 */
public class StripAlgo extends MapAlgo{
  @Override
  public MapRects calcMap(final MapRect r, final MapList ml, 
      final int ns, final int ne, final int l) {
    // stores all calculated rectangles
    final MapRects rects = new MapRects();
    
    // node iterator
    int ni = ns;
    // first node of current row
    int start = ns;

    // setting initial proportions
    final double xx = r.x;
    double yy = r.y;
    final double ww = r.w;
    double hh = r.h;

    MapRects row = new MapRects();
    int height = 0;
    double weight = 0;
    double sumweight = 1;
    double tmpratio = 0;
    double rowratio = Double.MAX_VALUE;
    
    
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
        
//        if(x + w <= xx + ww)
        if(yy + height <= yy + hh)
          tmp.add(new MapRect((int) x, (int) yy, w, height, ml.list[i], l));
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
              ml.list[ni], l));
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
}
