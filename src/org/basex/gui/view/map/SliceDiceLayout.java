package org.basex.gui.view.map;

import java.util.ArrayList;
import org.basex.data.Data;
import org.basex.util.Token;

/**
 * Uses simple slice and dice layout to split rectangles.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Joerg Hauser
 */
public final class SliceDiceLayout extends MapLayout {

  @Override
  void calcMap(final Data d, final MapRect r,
      final ArrayList<MapRect> mainRects, final MapList l,
      final int ns, final int ne, final int level) {

    // one rectangle left.. continue with this child
    if(ne - ns == 1) {
      putRect(d, r, mainRects, l, ns, level);
    } else {
      // determine direction
      final boolean v = (level % 2) == 0 ? true : false;
//      final boolean v = r.w < r.h;
      
      // some more nodes have to be positioned on the first level
      if(level == 0) {
        splitUniformly(d, r, mainRects, l, ns, ne, level, v);
      } else {  
        int par = d.parent(l.list[ns], d.kind(l.list[ns]));;
        long parsize = d.fs != null ? 
            Token.toLong(d.attValue(d.sizeID, par)) : 0;
        int parchilds = l.list[ne] - l.list[ns];
  
        // setting initial proportions
        double xx = r.x;
        double yy = r.y;
        double ww = 0;
        double hh = 0;
  
        // calculate map for each rectangel on this level
        for(int i = 0; i < l.size - 1; i++) {
          // draw map taking sizes into account
          long size = d.fs != null ? 
              Token.toLong(d.attValue(d.sizeID, l.list[i])) : 0;
          int childs = l.list[i + 1] - l.list[i];
          double weight = calcWeight(size, childs, parsize, parchilds, d);
          
          if(v) {
            yy += hh;
            hh = weight * r.h;
            ww = r.w;
          } else {
            xx += ww;
            ww = weight * r.w;
            hh = r.h;
          }
          
          int[] liste = new int[1];
          liste[0] = l.list[i];
          if(ww > 0 && hh > 0) calcMap(d,
            new MapRect((int) xx, (int) yy, (int) ww, (int) hh, 0, r.level),
            mainRects, new MapList(liste), 0, 1, level);
        }
      }
    }
  }

  @Override
  String getType() {
    return "SliceAndDice Layout";
  }
}