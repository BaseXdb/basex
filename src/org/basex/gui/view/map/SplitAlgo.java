package org.basex.gui.view.map;

/**
 * SplitLayout Algorithm.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Joerg Hauser
 */
public class SplitAlgo extends MapAlgo {

  @Override
  public MapRects calcMap(final MapRect r, final MapList ml, 
      final int ns, final int ne, final int l) {
    return calcMap(r, ml, ns, ne, l, 1);
  }

  /**
   * Uses recursive SplitLayout algorithm to divide rectangles on one level.
   * 
   * @param r parent rectangle
   * @param ml children array
   * @param ns start array position
   * @param ne end array position
   * @param l indicates level which is calculated
   * @param sumweight weight of this recursion level
   * @return ArrayList containing rectangles
   */
  private MapRects calcMap(final MapRect r, final MapList ml, 
      final int ns, final int ne, final int l, final double sumweight) {

    if(ne - ns == 0) {
      final MapRects rects = new MapRects();
      rects.add(new MapRect(r, ml.list[ns], l));
      return rects;
    } else {
      final MapRects rects = new MapRects();
      double weight;
      int ni = ns - 1;
  
      // increment pivot until left rectangle contains more or equal
      // than half the weight or leave with just setting it to ne - 1
      weight = 0;
// [JH] only difference to BinaryLayout????
//      ni = ns + ((ne - ns) / 2);
//      System.out.println(ns + ";" + ne + ":" + ni);
//      for(int i = ns; i <= ni; i++) {
//        weight += ml.weight[i];
//      }
      
      for(; ni < ne;) {
        weight += ml.weight[++ni];
        if(weight >= sumweight / 2 || ni == ne - 1) break;
      }
      
      int xx = r.x;
      int yy = r.y;
      int ww = !(r.w > r.h) ? r.w : (int) (r.w * 1 / sumweight * weight);
      int hh = r.w > r.h ? r.h : (int) (r.h * 1 / sumweight * weight);
      // paint both rectangles if enough space is left
      if(ww > 0 && hh > 0 && weight > 0) rects.add(calcMap(
          new MapRect(xx, yy, ww, hh, 0, r.level), ml, ns, ni, l, weight));
      if(r.w > r.h) {
        xx += ww;
        ww = r.w - ww;
      } else {
        yy += hh;
        hh = r.h - hh;
      }

      if(ww > 0 && hh > 0 && sumweight - weight > 0 && ni + 1 <= ne)
          rects.add(calcMap(new MapRect(xx, yy, ww, hh, 0, r.level), 
          ml, ni + 1, ne, l, sumweight - weight));
      
      return rects;
    }
  }
}
