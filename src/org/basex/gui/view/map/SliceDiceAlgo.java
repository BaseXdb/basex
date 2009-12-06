package org.basex.gui.view.map;

/**
 * Slice-and-Dice layout algorithm.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Joerg Hauser
 */
final class SliceDiceAlgo extends MapAlgo {
  @Override
  MapRects calcMap(final MapRect r, final MapList ml,
      final int ns, final int ne) {

    // setting initial proportions
    double xx = r.x;
    double yy = r.y;
    double ww = 0;
    double hh = 0;

    int tmpx = -1;
    int tmpy = -1;
    int tmph = -1;
    int tmpw = -1;

    final MapRects rects = new MapRects();
    // calculate map for each rectangle on this level
    final int is = ml.size();
    for(int i = 0; i < is; i++) {
      if((r.level & 1) == 0) {
        yy += hh;
        hh = ml.weight[i] * r.h;
        ww = r.w;
      } else {
        xx += ww;
        ww = ml.weight[i] * r.w;
        hh = r.h;
      }

      if(ww > 0 && hh > 0 && (tmpx != (int) xx || tmpy != (int) yy ||
          tmph != (int) hh || tmpw != (int) ww))
        rects.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh,
            ml.get(i), r.level));
      tmpx = (int) xx;
      tmpy = (int) yy;
      tmph = (int) hh;
      tmpw = (int) ww;
    }
    return rects;
  }
}
