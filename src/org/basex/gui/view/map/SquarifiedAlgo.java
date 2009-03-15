package org.basex.gui.view.map;

import java.util.ArrayList;

/**
 * Squarified Layout Algorithm.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Joerg Hauser
 */
public class SquarifiedAlgo extends MapAlgo {

  @Override
  public ArrayList<MapRect> calcMap(final MapRect r, final MapList ml, 
      final double[] weights, final int ns, final int ne, final int l) {

    ml.sort();
    
    final ArrayList<MapRect> rects = new ArrayList<MapRect>();
    int ni = ns;
    // running start holding first element of current row
    int start = ns;

    // setting initial proportions
    double xx = r.x;
    double yy = r.y;
    double ww = r.w;
    double hh = r.h;
    
    ArrayList<MapRect> row = new ArrayList<MapRect>();
    double height = 0;
    double width = 0;
    double weight = 0;
    double sumweight = 1;
    
    while(ni < ne) {
      if(ww < hh) {
        weight += ml.weight[ni];
        height = weight / sumweight * hh;
        
        final ArrayList<MapRect> tmp = new ArrayList<MapRect>();

        double x = xx;
        for(int i = start; i <= ni; i++) {
          final double w = i == ni ? xx + ww - x : ml.weight[i] / weight * ww;
          tmp.add(new MapRect((int) x, (int) yy, (int) w, (int) height,
              ml.list[i], l));
          x += (int) w;
        }

        // if ar has increased discard tmp and add row
        if(lineRatio(tmp) > lineRatio(row)) {
          // add rects of row to solution
          rects.addAll(row);
          // preparing next line
          hh -= row.get(0).h;
          yy += row.get(0).h;
          tmp.clear();
          row.clear();
          start = ni;
          sumweight -= weight - ml.weight[ni];
          weight = 0;
          // sometimes there has to be one rectangles to fill the left space
          if(ne == ni + 1) {
            row.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh,
                ml.list[ni], l));
            break;
          }
        } else {
          row = tmp;
          ni++;
        }
      } else {
        weight += ml.weight[ni];
        width = weight / sumweight * ww;
        final ArrayList<MapRect> tmp = new ArrayList<MapRect>();

        double y = yy;
        for(int i = start; i <= ni; i++) {
          final double h = i == ni ? yy + hh - y : ml.weight[i] / weight * hh;
          tmp.add(new MapRect((int) xx, (int) y, (int) width, (int) h,
              ml.list[i], l));
          y += (int) h;
        }

        // if ar has increased discard tmp and add row
        if(lineRatio(tmp) > lineRatio(row)) {
          // add rects of row to solution
          rects.addAll(row);
          // preparing next line
          ww -= row.get(0).w;
          xx += row.get(0).w;
          tmp.clear();
          row.clear();
          start = ni;
          sumweight -= weight - ml.weight[ni];
          weight = 0;
          // sometimes there has to be one rectangles to fill the left space
          if(ne == ni + 1) {
            row.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh,
                ml.list[ni], l));
            break;
          }
        } else {
          row = tmp;
          ni++;
        }
      }
    }
    
    for(final MapRect rect : row) rect.h = (int) hh;
    // adding last row
    rects.addAll(row);
    
    return rects;
  }

  @Override
  String getName() {
    return "Squarified Layout";
  }
}
