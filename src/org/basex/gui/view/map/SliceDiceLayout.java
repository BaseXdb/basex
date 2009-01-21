package org.basex.gui.view.map;

import java.util.ArrayList;
import org.basex.build.fs.FSParser;
import org.basex.data.Data;
import org.basex.gui.GUIProp;
import org.basex.gui.view.ViewRect;
import org.basex.gui.view.ViewData;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * Uses simple slice and dice layout to split rectangles.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Joerg Hauser
 */
public final class SliceDiceLayout extends MapLayout {

  @Override
  void calcMap(final Data data, final ViewRect r,
      final ArrayList<ViewRect> mainRects, final IntList l,
      final int ns, final int ne, final int level) {

    // one rectangle left.. continue with this child
    if(ne - ns == 1) {

      // calculate rectangle sizes
      final ViewRect t = new ViewRect(r.x, r.y, r.w, r.h, l.list[ns], r.level);

      // position, with and height are calculated using split sizes of
      //  former recursion level
      final int x = t.x + layout.x;
      final int y = t.y + layout.y;
      final int w = t.w - layout.w;
      final int h = t.h - layout.h;
      mainRects.add(t);
      // skip too small rectangles and leaf nodes (= meta data in deepfs)
      if(w > 0 && h > 0 && !ViewData.isLeaf(data, t.pre)) {
        final IntList ch = children(data, t.pre);
        if(ch.size >= 0) calcMap(data, new ViewRect(x, y, w, h,
            l.list[ns], t.level + 1), mainRects, ch, 0, ch.size - 1, level + 1);
      }
    } else {
      // number of nodes used to calculate rect size
      int nn = ne - ns;

      long parsize = 1;
      int par;
      par = data.parent(l.list[ns], Data.ELEM);
      int parchilds;
//      parchilds = children(data, par).size;
      parchilds = l.list[ne] - l.list[ns];
      
      // determine direction
      final boolean v = (level % 2) == 0 ? true : false;
//      final boolean v = (r.w > r.h) ? false : true;

      // setting initial proportions
      double xx = r.x;
      double yy = r.y;
      double ww, hh;

      // use filesize to calculate rect sizes
      if(data.fs != null && GUIProp.mapaggr) {
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
          long size = Token.toLong(data.attValue(data.sizeID, l.list[i]));
          int childs = l.list[i + 1] - l.list[i];
          double weight = calcWeight(size, childs, parsize, parchilds, data);
          
//          if(Double.isNaN(weight)) System.out.println("[" + l.list[i] + "]"
//              + "(" + size + ";" + childs + "/" + parsize + ";" + parchilds + 
//              ")" + weight);
          if(v) {
            yy += hh;
              // old calculation
//            hh = (double) size * r.h / parsize;
            hh = weight * r.h;
            ww = r.w;
          } else {
            xx += ww;
            // old calculation
//            ww = (double) size * r.w / parsize;
            ww = weight * r.w;
            hh = r.h;
          }
          if(ww > 0 && hh > 0) calcMap(data,
            new ViewRect((int) xx, (int) yy, (int) ww, (int) hh, 0, r.level),
            mainRects, new IntList(liste), 0, 1, level);
        } else {
          if(ww > 0 && hh > 0) {
            if(v) {
              calcMap(data, new ViewRect((int) xx, (int) yy, (int) ww, (int) hh,
                0, r.level), mainRects, new IntList(liste), 0, 1, level);
              yy += hh;
            } else {
              calcMap(data, new ViewRect((int) xx, (int) yy, (int) ww, (int) hh,
                0, r.level), mainRects, new IntList(liste), 0, 1, level);
              xx += ww;
            }
          }
        }
      }
    }
  }

  @Override
  String getType() {
    return "SliceAndDice Layout";
  }
}