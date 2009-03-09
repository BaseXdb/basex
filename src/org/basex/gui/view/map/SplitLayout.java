package org.basex.gui.view.map;

import java.util.ArrayList;
import org.basex.data.Data;
import org.basex.util.Token;

/**
 * Uses a SplitLayout Algorithm to divide Rectangles. First algo used in BaseX.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Joerg Hauser
 */
public final class SplitLayout extends MapLayout {

  @Override
  void calcMap(final Data d, final MapRect r,
      final ArrayList<MapRect> mainRects, final MapList l,
      final int ns, final int ne, final int level) {

    // one rectangle left.. continue with this child
    if(ne - ns == 1) {
      putRect(d, r, mainRects, l, ns, level);
    } else {
      final boolean v = r.w > r.h;
      // some more nodes have to be positioned on the first level
      if(level == 0) {
        splitUniformly(d, r, mainRects, l, ns, ne, level, v);
      } else {
        double weight = 0;
        int ni = ns;

        // sometimes it is not the whole size of the parent node. simply sum
        // the sizes of the nodes in the list
        long parsize = d.fs != null ? addSizes(l, ni, ne, d) : 0;
        int parchildren = l.list[ne] - l.list[ni];
        if(parsize == 0) parsize = l.list[ne] - l.list[ns];
        weight = 0;

        // increment pivot until left rectangle contains more or equal
        // than half the weight or leave with just setting it to ne - 1
        for(; ni < ne - 1; ni++)  {
          long size = d.fs != null ? 
              Token.toLong(d.attValue(d.sizeID, l.list[ni])) : 0;
          int children = l.list[ni + 1] - l.list[ni];
          if(weight >= 0.5) break;
          weight += calcWeight(size, children, parsize, parchildren, d);
        }
        
        int xx = r.x;
        int yy = r.y;

        // needs to be replaced by something like this
        int ww = !v ? r.w : (int) (r.w * weight);
        int hh = v ? r.h : (int) (r.h * weight);

        // paint both rectangles if enough space is left
        if(ww > 0 && hh > 0) calcMap(d, new MapRect(xx, yy, ww, hh, 0,
            r.level), mainRects, l, ns, ni, level);
        if(v) {
          xx += ww;
          ww = r.w - ww;
        } else {
          yy += hh;
          hh = r.h - hh;
        }
        if(ww > 0 && hh > 0) calcMap(d, new MapRect(xx, yy, ww, hh, 0,
            r.level), mainRects, l, ni, ne, level);
      }
    }
  }

  @Override
  String getType() {
    return "SplitLayout";
  }
}