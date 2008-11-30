package org.basex.gui.view.map;

import java.util.ArrayList;

import org.basex.build.fs.FSParser;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.view.ViewData;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * Uses simpleslice and ice Layout to split rectangles.
 * 
 * @author joggele
 *
 */
public class SliceDiceLayout extends MapLayout {

  @Override
  void calcMap(final MapRect r, final ArrayList<MapRect> mainRects, 
      final IntList l, final int ns, final int ne, final int level) {
    final Data data = GUI.context.data();

    // one rectangle left.. continue with this child
    if(ne - ns == 1) {
      
      // calculate rectangle sizes
      final MapRect t = new MapRect(r.x, r.y, r.w, r.h, l.list[ns], r.l);

      // position, with and height are calculated using split sizes of 
      //  former recursion level
      final int x = t.x + layout.x;
      final int y = t.y + layout.y;
      final int w = t.w - layout.w;
      final int h = t.h - layout.h;
      mainRects.add(t);
      // skip too small rectangles and leaf nodes (= meta data in deepfs)
      if(w > 0 && h > 0 && !ViewData.isLeaf(GUI.context.data(), t.p)) {
        final IntList ch = children(t.p);
        if(ch.size >= 0) calcMap(new MapRect(x, y, w, h, 
            l.list[ns], t.l + 1), mainRects, ch, 0, ch.size - 1, level + 1);
      }
    } else {      
      // number of nodes used to calculate rect size
      int nn = ne - ns;
      
      long parsize = 1;
      
      // determine direction
//      final boolean v = (level % 2) == 0 ? true : false;
      final boolean v = (r.w > r.h) ? false : true;
      
      // setting initial proportions
      double xx = r.x;
      double yy = r.y;
      double ww, hh;
      
      if(data.fs != null && GUIProp.mapaggr) {
        int par = data.parent(l.list[ns], Data.ELEM);
        parsize = Token.toLong(data.attValue(par + FSParser.SIZEOFFSET));
        hh = 0;
        ww = 0;
      } else {
        if(v) {
          ww = r.w;
          hh = (double) r.h / nn;
        } else {
          ww = (double) r.w / nn;
          hh = r.h;
        }
      }
      
      // calculate map for each rectangel on this level
      for(int i = 0; i < l.size - 1; i++) {
        int[] liste = new int[1];
        liste[0] = l.list[i];
        
        // draw map taking sizes into account
        if(data.fs != null && GUIProp.mapaggr) {
          if(v) {
            yy += hh;
            hh = (double) Token.toLong(
                data.attValue(data.sizeID, l.list[i])) * r.h / parsize;
            ww = r.w;
          } else {
            xx += ww;
            ww = (double) Token.toLong(
                data.attValue(data.sizeID, l.list[i])) * r.w / parsize;
            hh = r.h;
          }
          if(ww > 0 && hh > 0) calcMap(
            new MapRect((int) xx, (int) yy, (int) ww, (int) hh, 0, r.l), 
            mainRects, new IntList(liste), 0, 1, level);
        } else {
          if(ww > 0 && hh > 0) {
            if(v) {
              calcMap(
                  new MapRect((int) xx, (int) yy, (int) ww, (int) hh, 0, r.l), 
                  mainRects, new IntList(liste), 0, 1, level);
              yy += hh;
            } else {
              calcMap(
                  new MapRect((int) xx, (int) yy, (int) ww, (int) hh, 0, r.l),
                  mainRects, new IntList(liste), 0, 1, level);
              xx += ww;
            }
          }  
        }
      }
    }
  }
}
