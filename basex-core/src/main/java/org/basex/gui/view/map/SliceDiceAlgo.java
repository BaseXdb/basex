package org.basex.gui.view.map;

/**
 * Slice-and-Dice layout algorithm.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Joerg Hauser
 */
final class SliceDiceAlgo extends MapAlgo {
  @Override
  MapRects calcMap(final MapRect r, final MapList ml, final int ns, final int ne) {
    // setting initial proportions
    double xx = r.x;
    double yy = r.y;
    double ww = 0;
    double hh = 0;

    int tx = -1;
    int ty = -1;
    int th = -1;
    int tw = -1;

    final MapRects rects = new MapRects();
    // calculate map for each rectangle on this level
    final int is = ml.size();
    for(int i = 0; i < is; ++i) {
      if((r.level & 1) == 0) {
        yy += hh;
        hh = ml.weight[i] * r.h;
        ww = r.w;
      } else {
        xx += ww;
        ww = ml.weight[i] * r.w;
        hh = r.h;
      }

      if(ww > 0 && hh > 0 && (tx != (int) xx || ty != (int) yy ||
          th != (int) hh || tw != (int) ww))
        rects.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh, ml.get(i), r.level));
      tx = (int) xx;
      ty = (int) yy;
      th = (int) hh;
      tw = (int) ww;
    }
    return rects;
  }
}
