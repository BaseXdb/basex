package org.basex.gui.view.map;

import java.util.ArrayList;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.view.ViewRect;
import org.basex.gui.view.ViewData;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * Uses a SplitLayout Algorithm to divide Rectangles. First algo used in BaseX.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Joerg Hauser
 */
public class SplitLayout extends MapLayout {

  @Override
  void calcMap(final ViewRect r, final ArrayList<ViewRect> mainRects,
      final IntList l, final int ns, final int ne, final int level) {
    // one rectangle left.. continue with this child
    if(ne - ns == 1) {
      // calculate rectangle sizes
      final ViewRect t = new ViewRect(r.x, r.y, r.w, r.h, l.list[ns], r.level);
      mainRects.add(t);

      // position, with and height calculated using sizes of former level
      final int x = t.x + layout.x;
      final int y = t.y + layout.y;
      final int w = t.w - layout.w;
      final int h = t.h - layout.h;

      // skip too small rectangles and leaf nodes (= meta data in deepfs)
      if((w >= o || h >= o) && w > 0 && h > 0 && 
          !ViewData.isLeaf(GUI.context.data(), t.pre)) {
        final IntList ch = children(t.pre);
        if(ch.size != 0) calcMap(new ViewRect(x, y, w, h, l.list[ns],
            r.level + 1), mainRects, ch, 0, ch.size - 1, level + 1);
      }
    } else {
      long nn, ln; 
      int ni;
      // number of nodes used to calculate space
      nn = ne - ns;
      // nn / 2, pretends to be the middle of the handled list
      // except if starting point in the list is not at position 0
      ln = nn >> 1;
      // pivot with integrated list start
      ni = (int) (ns + ln);    
        // consider number of descendants to calculate split point
      if(!GUIProp.mapsimple && level != 0) {
        // calculating real number of nodes of this recursion
        nn = l.list[ne] - l.list[ns];
        
        // let pivot be the first element of the list
        ni = ns;
        
        final Data data = GUI.context.data();
        if(data.fs != null && GUIProp.mapaggr) {
          // parents size
          int par = data.parent(l.list[ns], Data.ELEM);
          long parsize = Token.toLong(data.attValue(data.sizeID, par));
          // temporary to sum up the child sizes
          long sum = 0;
          
          // increment pivot until left rectangle contains more or equal
          // than the half descendants or if left node is greater than half of 
          // all descendants leave with just setting it to ne - 1
          for(; ni < ne - 1; ni++)  {
            // use file sizes to calculate breakpoint
            if(sum >= parsize / 2) break;
            sum += Token.toLong(data.attValue(data.sizeID, l.list[ni]));
          }
          nn = parsize;
          ln = sum;
        } else {
          // increment pivot until left rectangle contains more or equal
          // than the half descendants or if left node is greater than half of 
          // all descendants leave with just setting it to ne - 1
          for(; ni < ne - 1; ni++)  {
            if(l.list[ni] - l.list[ns] >= (nn >> 1)) break;
          }
          ln = l.list[ni] - l.list[ns];
        }
      }

      // determine rectangle orientation (horizontal/vertical)
      // mapprop contains prefered alignment influence
      final int p = GUIProp.mapprop;
      final boolean v = p > 4 ? r.w > r.h * (p + 4) / 8 : 
        r.w * (13 - p) / 8 > r.h;
      
      int xx = r.x;
      int yy = r.y;

      int ww = !v ? r.w : (int) (r.w * ln / nn);
      int hh = v ? r.h : (int) (r.h * ln / nn);
      
      // paint both rectangles if enough space is left
      if(ww > 0 && hh > 0) calcMap(new ViewRect(xx, yy, ww, hh, 0, r.level),
          mainRects, l, ns, ni, level);
      if(v) {
        xx += ww;
        ww = r.w - ww;
      } else {
        yy += hh;
        hh = r.h - hh;
      }
      if(ww > 0 && hh > 0) calcMap(new ViewRect(xx, yy, ww, hh, 0, r.level),
          mainRects, l, ni, ne, level);
    }
  }
}
