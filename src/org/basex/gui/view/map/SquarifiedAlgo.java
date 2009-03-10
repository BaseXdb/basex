package org.basex.gui.view.map;

import java.util.ArrayList;

/**
 * SplitLayout Algorithm.
 * @author joggele
 *
 */
public class SquarifiedAlgo extends MapAlgo {

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
    
    while(ni < ne) {
      double weight = 0;
      double ratio = Double.MAX_VALUE;
      if(ww < hh) {
        ArrayList<MapRect> row = new ArrayList<MapRect>();
        double height = 0;
  
        weight += l.weights[ni];
        height = weight * hh;
        
        ArrayList<MapRect> tmp = new ArrayList<MapRect>();
        // create temporary row including current rectangle
        double x = xx;
        for(int i = start; i <= ni; i++) {
          // sometimes left space has to be used
          double w = i == ni ? xx + ww - x : l.weights[i] * ww;
          tmp.add(new MapRect((int) x, (int) yy, (int) w, (int) height,
              l.list[i], level));
          x += (int) w;
        }
        double tmpratio = lineRatio(tmp);
        // if ar has increased discard tmp and add row
        if(tmpratio > ratio) {
          // add row to rects
          rects.addAll(row);
          // preparing for new line to lay out
          hh -= row.get(0).h;
          yy += row.get(0).h;
          tmp.clear();
          row.clear();
          start = ni;
          
          // sometimes there has to be one rectangles to fill the left space
          if(ne == ni + 1) {
            rects.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh,
                l.list[ni], level));
            break;
          }
        }
        ratio = tmpratio;
        row = tmp;
        ni++;
      } else {
        ArrayList<MapRect> row = new ArrayList<MapRect>();
        double width = 0;
        weight += l.weights[ni];
        width = weight * ww;
        
        ArrayList<MapRect> tmp = new ArrayList<MapRect>();
        // create temporary row including current rectangle
        double y = yy;
        for(int i = start; i <= ni; i++) {
          // sometimes left space has to be used
          double h = i == ni ? yy + hh - y : l.weights[i] * hh;
          tmp.add(new MapRect((int) xx, (int) y, (int) width, (int) hh,
              l.list[i], level));
          y += (int) h;
        }
        double tmpratio = lineRatio(tmp);
        // if ar has increased discard tmp and add row
        if(tmpratio > ratio) {
          // add row to rects
          rects.addAll(row);
          // preparing for new line to lay out
          yy -= row.get(0).w;
          xx += row.get(0).w;
          tmp.clear();
          row.clear();
          start = ni;
          
          // sometimes there has to be one rectangles to fill the left space
          if(ne == ni + 1) {
            rects.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh,
                l.list[ni], level));
            break;
          }
        }
        ratio = tmpratio;
        row = tmp;
        ni++;
      }
    }
    return rects;
  }
  
  @Override
  public String getType() {
    return "SplitLayout";
  }
}
