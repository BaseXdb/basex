package org.basex.gui.view.map;

/**
 * Split and binary layout algorithm.
 *
 * @author BaseX Team, BSD License
 * @author Joerg Hauser
 */
final class SplitAlgo extends MapAlgo {
  /** Binary layout: split the list in the middle instead of at half of the weight. */
  private final boolean binary;

  /**
   * Constructor.
   * @param binary binary layout
   */
  SplitAlgo(final boolean binary) {
    this.binary = binary;
  }

  @Override
  MapRects calcMap(final MapRect r, final MapList ml, final int ns, final int ne) {
    return calcMap(r, ml, ns, ne, 1);
  }

  /**
   * Uses recursive SplitLayout algorithm to divide rectangles on one level.
   * @param r parent rectangle
   * @param ml children array
   * @param ns start array position
   * @param ne end array position
   * @param sw weight of this recursion level
   * @return rectangles
   */
  private MapRects calcMap(final MapRect r, final MapList ml, final int ns, final int ne,
      final double sw) {

    final MapRects rects = new MapRects();
    if(ne - ns == 0) {
      rects.add(new MapRect(r, ml.get(ns)));
      return rects;
    }

    double w = 0;
    int ni;
    if(binary) {
      // setting middle of the list and calc weights
      ni = ns + (ne - ns) / 2;
      for(int i = ns; i <= ni; i++) w += ml.weight[i];
    } else {
      // increment pivot until left rectangle contains more or equal
      // than half the weight or leave with just setting it to ne - 1
      ni = ns - 1;
      while(ni < ne) {
        w += ml.weight[++ni];
        if(w >= sw / 2 || ni == ne - 1) break;
      }
    }

    int xx = r.x;
    int yy = r.y;
    int ww = r.w > r.h ? (int) (r.w / sw * w) : r.w;
    int hh = r.w > r.h ? r.h : (int) (r.h / sw * w);
    // paint both rectangles if enough space is left
    if(ww > 0 && hh > 0 && w > 0) rects.add(calcMap(
        new MapRect(xx, yy, ww, hh, 0, r.level), ml, ns, ni, w));
    if(r.w > r.h) {
      xx += ww;
      ww = r.w - ww;
    } else {
      yy += hh;
      hh = r.h - hh;
    }

    if(ww > 0 && hh > 0 && sw - w > 0 && ni + 1 <= ne)
      rects.add(calcMap(new MapRect(xx, yy, ww, hh, 0, r.level), ml, ni + 1, ne, sw - w));

    return rects;
  }
}
