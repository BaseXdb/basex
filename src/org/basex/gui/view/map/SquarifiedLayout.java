package org.basex.gui.view.map;

import java.util.ArrayList;
import org.basex.data.Data;

/**
 * Uses a Squarified Algorithm to divide Rectangles.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Joerg Hauser
 */
public final class SquarifiedLayout extends MapLayout {
  
  @Override
  void calcMap(final Data data, final MapRect r,
      final ArrayList<MapRect> mainRects, final MapList l,
      final int ns, final int ne, final int level) {
    // one rectangle left...
    if(ne - ns <= 1) {
      putRect(data, r, mainRects, l, ns, level);
    } else {
   // some more nodes have to be positioned on the first level
      if(level == 0) {
        splitUniformly(data, r, mainRects, l, ns, ne, level, r.w > r.h);
      } else {
        // init number of childs ans sizes
        l.initChilds(data);
        int nn = 0;
        // number of nodes on this level
        for(int i = 0; i <= ne; i++) {
          nn += l.nrchilds[i];
        }
        long parsize = data.fs != null ? addSizes(l, ns, ne, data) : 0;
        int ni = ns;
        // init weights of nodes and sort
        l.initWeights(parsize, nn, data);
        l.sort();
        // running start holding first element of current row
        int start = ns;
  
        // determine direction
        final boolean v = r.w < r.h;
  
        // setting initial proportions
        double xx = r.x;
        double yy = r.y;
        double ww = r.w;
        double hh = r.h;
  
        if(v) {
          ArrayList<MapRect> row = new ArrayList<MapRect>();
          double height = 0;
          while(ni < ne) {
            // height of current strip
            long size = 0;
            for(int i = start; i <= ni; i++) {
              size += l.sizes[i];
            }
            int childs = 0;
            for(int i = start; i <= ni; i++) {
              childs += l.nrchilds[i];
            }
            double weight = calcWeight(size, childs, parsize, nn, data);
            height = weight * hh;
            
            ArrayList<MapRect> tmp = new ArrayList<MapRect>();
            // create temporary row including current rectangle
            double x = xx;
            for(int i = start; i <= ni; i++) {
              double w = i == ni ? xx + ww - x : 
                calcWeight(l.sizes[i], l.nrchilds[i], size, childs, data) * ww;
              tmp.add(new MapRect((int) x, (int) yy, (int) w, (int) height,
                  l.list[i], level));
              x += (int) w;
            }
  
            // if ar has increased discard tmp and add row
            if(lineRatio(tmp) > lineRatio(row)) {
              // add rects of row using recursion
              for(int i = 0; i < row.size(); i++) {
                MapList newl = new MapList(1);
                newl.add(row.get(i).pre);
                calcMap(data, row.get(i), mainRects, newl, 0, 1, level);
              }
              // preparing for new line to lay out
              hh -= row.get(0).h;
              yy += row.get(0).h;
              tmp.clear();
              row.clear();
              start = ni;
              nn = 0;
              for(int i = start; i <= ne; i++) {
                nn += l.nrchilds[i];
              }
              parsize =  data.fs != null ? addSizes(l, start, ne, data) : 0;
              // sometimes there has to be one rectangles to fill the left space
              if(ne == ni + 1) {
                row.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh,
                    l.list[ni], level));
                break;
              }
            }
            row = tmp;
            ni++;
          }
  
          // adding remaining rectangles
          for(int i = 0; i < row.size(); i++) {
            MapList newl = new MapList(1);
            newl.add(row.get(i).pre);
            calcMap(data, row.get(i), mainRects, newl, 0, 1, level);
          }
        } else {
          ArrayList<MapRect> row = new ArrayList<MapRect>();
          double width = 0;
          while(ni < ne) {
            // height of current strip
            long size = 0;
            for(int i = start; i <= ni; i++) {
              size += l.sizes[i];
            }
            int childs = 0;
            for(int i = start; i <= ni; i++) {
              childs += l.nrchilds[i];
            }
            double weight = calcWeight(size, childs, parsize, nn, data);
            width = weight * ww;
            
            ArrayList<MapRect> tmp = new ArrayList<MapRect>();
            // create temporary row including current rectangle
            double y = yy;
            for(int i = start; i <= ni; i++) {
              double h = i == ni ? yy + hh - y : 
                calcWeight(l.sizes[i], l.nrchilds[i], size, childs, data) * hh;
              tmp.add(new MapRect((int) xx, (int) y, (int) width, (int) h,
                  l.list[i], level));
              y += (int) h;
            }
  
            // if ar has increased discard tmp and add row
            if(lineRatio(tmp) > lineRatio(row)) {
              // add rects of row using recursion
              for(int i = 0; i < row.size(); i++) {
                MapList newl = new MapList(1);
                newl.add(row.get(i).pre);
                calcMap(data, row.get(i), mainRects, newl, 0, 1, level);
              }
              // preparing for new line to lay out
              ww -= row.get(0).w;
              xx += row.get(0).w;
              tmp.clear();
              row.clear();
              start = ni;
              nn = 0;
              for(int i = start; i <= ne; i++) {
                nn += l.nrchilds[i];
              }
              parsize =  data.fs != null ? addSizes(l, start, ne, data) : 0;
              // sometimes there has to be one rectangles to fill the left space
              if(ne == ni + 1) {
                row.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh,
                    l.list[ni], level));
                break;
              }
            }
            row = tmp;
            ni++;
          }
  
          // adding remaining rectangles
          for(int i = 0; i < row.size(); i++) {
            MapList newl = new MapList(1);
            newl.add(row.get(i).pre);
            calcMap(data, row.get(i), mainRects, newl, 0, 1, level);
          }
        }
      }
    }
  }

  @Override
  String getType() {
    return "SquarifiedLayout";
  } 
}