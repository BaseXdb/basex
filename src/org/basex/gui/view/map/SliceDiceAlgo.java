package org.basex.gui.view.map;

import java.util.ArrayList;

/**
 * Slice-and-Dice Layout Algorithm.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Joerg Hauser
 */
public class SliceDiceAlgo extends MapAlgo {
  @Override
  public ArrayList<MapRect> calcMap(final MapRect r, final MapList ml, 
      final int ns, final int ne, final int l) {
    
    // setting initial proportions
    double xx = r.x;
    double yy = r.y;
    double ww = 0;
    double hh = 0;

    int tmpx = -1;
    int tmpy = -1;
    int tmph = -1;
    int tmpw = -1;
    
    final ArrayList<MapRect> rects = new ArrayList<MapRect>();
    // calculate map for each rectangle on this level
    for(int i = 0; i < ml.size; i++) {      
      if((l & 1) == 0) {
//      if(r.w < r.h) {
        yy += hh;
        hh = ml.weight[i] * r.h;
        ww = r.w;
      } else {
        xx += ww;
        ww = ml.weight[i] * r.w;
        hh = r.h;
      }
      
      if(ww > 0 && hh > 0 && (tmpx != (int) xx || tmpy != (int) yy ||
          tmph != (int) hh || tmpw != (int) ww)) 
        rects.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh, 
            ml.list[i], r.level));
      tmpx = (int) xx;
      tmpy = (int) yy;
      tmph = (int) hh;
      tmpw = (int) ww;
    }
    return rects;
  }

  @Override
  String getName() {
    return "Slice and Dice Layout";
  }
}
