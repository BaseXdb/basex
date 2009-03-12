package org.basex.gui.view.map;

import java.util.ArrayList;

/**
 * Squarified Layout Algorithm.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Joerg Hauser
 */
public class SquarifiedAlgo extends MapAlgo {

  @Override
  public ArrayList<MapRect> calcMap(final MapRect r, final MapList l, 
      final double[] weights, final int ns, final int ne, final int level) {
    if(level <= 4) {
      l.sort();
    }
    
    
    ArrayList<MapRect> rects = new ArrayList<MapRect>();
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
        weight += l.weights[ni];
        height = weight / sumweight * hh;
        
        ArrayList<MapRect> tmp = new ArrayList<MapRect>();

        double x = xx;
        for(int i = start; i <= ni; i++) {
          double w = i == ni ? xx + ww - x : l.weights[i] / weight * ww;
          tmp.add(new MapRect((int) x, (int) yy, (int) w, (int) height,
              l.list[i], level));
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
          sumweight -= weight - l.weights[ni];
          weight = 0;
          // sometimes there has to be one rectangles to fill the left space
          if(ne == ni + 1) {
            row.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh,
                l.list[ni], level));
            break;
          }
        } else {
          row = tmp;
          ni++;
        }
      } else {
        weight += l.weights[ni];
        width = weight / sumweight * ww;
        
        ArrayList<MapRect> tmp = new ArrayList<MapRect>();

        double y = yy;
        for(int i = start; i <= ni; i++) {
          double h = i == ni ? yy + hh - y : l.weights[i] / weight * hh;
          tmp.add(new MapRect((int) xx, (int) y, (int) width, (int) h,
              l.list[i], level));
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
          sumweight -= weight - l.weights[ni];
          weight = 0;
          // sometimes there has to be one rectangles to fill the left space
          if(ne == ni + 1) {
            row.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh,
                l.list[ni], level));
            break;
          }
        } else {
          row = tmp;
          ni++;
        }
      }
    }
    
    for(MapRect rect : row) rect.h = (int) hh;
    // adding last row
    rects.addAll(row);
    
    return rects;
  }
  
  @Override
  public String getType() {
    return "SquarifiedLayout";
  }
}
