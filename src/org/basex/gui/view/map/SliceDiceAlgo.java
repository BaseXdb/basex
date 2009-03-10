package org.basex.gui.view.map;

import java.util.ArrayList;

/**
 * SplitLayout Algorithm.
 * @author joggele
 *
 */
public class SliceDiceAlgo extends MapAlgo{
  @Override
  public ArrayList<MapRect>  calcMap(final MapRect r, final MapList l, 
      final double[] weights, final int ns, final int ne, final int level) {
    
    // setting initial proportions
    double xx = r.x;
    double yy = r.y;
    double ww = 0;
    double hh = 0;

    ArrayList<MapRect> rects = new ArrayList<MapRect>();
    // calculate map for each rectangle on this level
    for(int i = 0; i < l.size - 1; i++) {      
      if((level % 2) == 0) {
        yy += hh;
        hh = l.weights[i] * r.h;
        ww = r.w;
      } else {
        xx += ww;
        ww = l.weights[i] * r.w;
        hh = r.h;
      }
      
      if(ww > 0 && hh > 0) rects.add(new MapRect((int) xx, (int) yy, (int) ww, 
          (int) hh, l.list[i], r.level));
    }
    return rects;
  }

  @Override
  String getType() {
    return "SliceAndDice Layout";
  }
}
