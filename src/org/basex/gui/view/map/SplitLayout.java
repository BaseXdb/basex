package org.basex.gui.view.map;

import java.util.ArrayList;
import org.basex.data.Data;
import org.basex.gui.GUIProp;
import org.basex.gui.view.ViewRect;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * Uses a SplitLayout Algorithm to divide Rectangles. First algo used in BaseX.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Joerg Hauser
 */
public final class SplitLayout extends MapLayout {

  @Override
  void calcMap(final Data data, final ViewRect r,
      final ArrayList<ViewRect> mainRects, final IntList l,
      final int ns, final int ne, final int level) {

    // one rectangle left.. continue with this child
    if(ne - ns <= 1) {
      putRect(data, r, mainRects, l, ns, level);
    } else {
      double weight = 0;
      int ni = ns;

      /*/ debugging...
      for(int i = 0; i < l.size; i++) {
        int v = l.list[ns];
        System.out.println(i + ": " + v + ", Kind: " + data.kind(v));
      }*/
      
      // parents size
      int par = data.parent(l.list[ns], data.kind(l.list[ns]));
      long parsize = data.fs != null ?
          Token.toLong(data.attValue(data.sizeID, par)) : 0;
      int parchilds = l.list[ne] - l.list[ni];
      if(parsize == 0) parsize = l.list[ne] - l.list[ns];
      weight = 0;

      // increment pivot until left rectangle contains more or equal
      // than half the weight or leave with just setting it to ne - 1
      for(; ni < ne - 1; ni++)  {
        long size = data.fs != null ? 
            Token.toLong(data.attValue(data.sizeID, l.list[ni])) : 0;
        int childs = l.list[ni + 1] - l.list[ni];
        if(weight >= 0.5) break;
        weight += calcWeight(size, childs, parsize, parchilds, data);
      }

      // determine rectangle orientation (horizontal/vertical)
      // mapprop contains preferred alignment influence
      // [JH] alignment should always be centered....
      final int p = GUIProp.mapprop;
      final boolean v = p > 4 ? r.w > r.h * (p + 4) / 8 :
        r.w * (13 - p) / 8 > r.h;

      int xx = r.x;
      int yy = r.y;

      // needs to be replaced by something like this
      int ww = !v ? r.w : (int) (r.w * weight);
      int hh = v ? r.h : (int) (r.h * weight);

      // paint both rectangles if enough space is left
      if(ww > 0 && hh > 0) calcMap(data, new ViewRect(xx, yy, ww, hh, 0,
          r.level), mainRects, l, ns, ni, level);
      if(v) {
        xx += ww;
        ww = r.w - ww;
      } else {
        yy += hh;
        hh = r.h - hh;
      }
      if(ww > 0 && hh > 0) calcMap(data, new ViewRect(xx, yy, ww, hh, 0,
          r.level), mainRects, l, ni, ne, level);
    }
  }

  @Override
  String getType() {
    return "SplitLayout";
  }
}