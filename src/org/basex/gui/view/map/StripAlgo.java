package org.basex.gui.view.map;

import java.util.ArrayList;

/**
 * SplitLayout Algorithm.
 * @author joggele
 *
 */
public class StripAlgo extends MapAlgo{
  @Override
  public ArrayList<MapRect> calcMap(final MapRect r, final MapList l, 
      final double[] weights, final int ns, final int ne, final int level) {
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
    double weight = 0;
    
    while(ni < ne) {
      weight += l.weights[ni];
      height = weight * hh;
      
      ArrayList<MapRect> tmp = new ArrayList<MapRect>();
      // create temporary row including current rectangle
      double x = xx;
      for(int i = start; i <= ni; i++) {
        double w = i == ni ? xx + ww - x : l.weights[i] / weight * ww;
        tmp.add(new MapRect((int) x, (int) yy, (int) w, (int) height,
            l.list[i], level));
        x += (int) w;
      }
      double tmpratio = lineRatio(tmp);
      // if ar has increased discard tmp and add row
      if(tmpratio > lineRatio(row)) {
        // preparing for new line to lay out
        hh -= row.get(0).h;
        yy += row.get(0).h;
        // add rects of row using recursion
        rects.addAll(row);
        tmp.clear();
        row.clear();
        ni--;
        start = ni;
        weight = 0;
      }

      // sometimes there has to be one rectangles to fill the left space
      if(ne == ni + 1) {
        rects.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh,
            l.list[ni], level));
        break;
      }
      row = tmp;
      ni++;
    }
    return rects;
  }

  @Override
  String getType() {
    return "StripLayout Layout";
  }
}
