package org.basex.gui.view.map;

import java.util.ArrayList;

/**
 * SplitLayout Algorithm.
 * @author joggele
 *
 */
public class SplitAlgo extends MapAlgo {

  @Override
  public ArrayList<MapRect> calcMap(final MapRect r, final MapList l, 
      final double[] w, final int ns, final int ne, final int level) {
    return calcMap(r, l, w, ns, ne, level, 1);
  }

  /**
   * Uses recursive SplitLayout algorithm.
   * @param r parent rectangle
   * @param l children array
   * @param w weights array
   * @param ns start array position
   * @param ne end array position
   * @param level indicates level which is calculated
   * @param sumweight weight of this recursion level
   * @return ArrayList holding layedout rectangles
   */
  public ArrayList<MapRect> calcMap(final MapRect r, final MapList l, 
      final double[] w, final int ns, final int ne, final int level,
      final double sumweight) {
    ArrayList<MapRect> rects = new ArrayList<MapRect>();
    double weight = 0;
    int ni = ns;

    // increment pivot until left rectangle contains more or equal
    // than half the weight or leave with just setting it to ne - 1
    weight = 0;
    for(; ni < ne - 1; ni++)  {
      if(weight >= sumweight / 2) break;
      weight += w[ni];
    }
    
    int xx = r.x;
    int yy = r.y;
    int ww = !(r.w > r.h) ? r.w : (int) (r.w * 1 / sumweight * weight);
    int hh = r.w > r.h ? r.h : (int) (r.h * 1 / sumweight * weight);

    // paint both rectangles if enough space is left
    if(ww > 0 && hh > 0) rects.addAll(calcMap(
        new MapRect(xx, yy, ww, hh, 0, r.level), 
        l, w, ns, ni, level, weight));
    if(r.w > r.h) {
      xx += ww;
      ww = r.w - ww;
    } else {
      yy += hh;
      hh = r.h - hh;
    }
    if(ww > 0 && hh > 0) rects.addAll(calcMap(
        new MapRect(xx, yy, ww, hh, 0, r.level), 
        l, w, ni, ne, level, sumweight - weight));
    
    return new ArrayList<MapRect>();
  }
  
  @Override
  public String getType() {
    return "StripLayout";
  }
}
