package org.basex.gui.view.map;

import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.view.*;

/**
 * Defines shared things of TreeMap layout algorithms.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Joerg Hauser
 */
final class MapLayout {
  /** List of rectangles. */
  final MapRects rectangles = new MapRects();
  /** Font size. */
  private final int off;
  /** Data reference. */
  private final Data data;
  /** Map algorithm to use in this layout. */
  private final MapAlgo algo;
  /** Text lengths. */
  private final int[] textLen;
  /** GUI options. */
  private final GUIOptions gopts;

  /** Layout rectangle. */
  final MapRect layout;

  /**
   * Constructor.
   * @param d data reference to use in this layout
   * @param tl text lengths array
   * @param opts gui options
   */
  MapLayout(final Data d, final int[] tl, final GUIOptions opts) {
    data = d;
    textLen = tl;
    gopts = opts;
    off = GUIConstants.fontSize + 4;

    switch(gopts.get(GUIOptions.MAPOFFSETS)) {
      // no title, small border
      case 1 :
        layout = new MapRect(0, 2, 0, 2); break;
      // title, no border
      case 2 :
        layout = new MapRect(0, off, 0, off); break;
      // title, border
      case 3 :
        layout = new MapRect(2, off - 1, 4, off + 1); break;
      // title, large border
      case 4 :
        layout = new MapRect(off >> 2, off, off >> 1, off + (off >> 2)); break;
      // no title, no border
      default:
        layout = new MapRect(0, 0, 0, 0); break;
    }

    switch(gopts.get(GUIOptions.MAPALGO)) {
      // select method to construct this treemap
      // may should be placed in makeMap to define different method for
      // different levels
      case 1 : algo = new StripAlgo(); break;
      case 2 : algo = new SquarifiedAlgo(); break;
      case 3 : algo = new SliceDiceAlgo(); break;
      case 4 : algo = new BinaryAlgo(); break;
      default: algo = new SplitAlgo(); break;
    }
  }

  /**
   * Returns all children of the specified node.
   * @param par parent node
   * @return children
   */
  private MapList children(final int par) {
    final MapList list = new MapList();
    final int last = par + ViewData.size(data, par);
    final boolean atts = gopts.get(GUIOptions.MAPATTS);
    int p = par + (atts ? 1 : data.attSize(par, data.kind(par)));
    while(p < last) {
      list.add(p);
      p += ViewData.size(data, p);
    }
    return list;
  }

  /**
   * Recursively splits rectangles.
   * @param r parent rectangle
   * @param l children array
   * @param ns start array position
   * @param ne end array position
   */
  void makeMap(final MapRect r, final MapList l, final int ns, final int ne) {
    if(ne - ns == 0) {
      // one rectangle left, add it and go deeper
      r.pre = l.get(ns);
      putRect(r);
    } else {
      int nn = 0;
      if(r.level == 0) {
        final int is = l.size();
        for(int i = 0; i < is; ++i) nn += ViewData.size(data, l.get(i));
      } else {
        nn = l.get(ne) - l.get(ns) + ViewData.size(data, l.get(ne));
      }
      l.initWeights(textLen, nn, data, gopts.get(GUIOptions.MAPWEIGHT));

      // call recursion for next deeper levels
      final MapRects rects = algo.calcMap(r, l, ns, ne);
      for(final MapRect rect : rects) {
        if(rect.x + rect.w <= r.x + r.w && rect.y + rect.h <= r.y + r.h)
          putRect(rect);
      }
    }
  }

  /**
   * One rectangle left, add it and continue with its children.
   * @param r parent rectangle
   */
  private void putRect(final MapRect r) {
    // position, with and height calculated using sizes of former level
    final int x = r.x + layout.x;
    final int y = r.y + layout.y;
    final int w = r.w - layout.w;
    final int h = r.h - layout.h;

    // skip too small rectangles and meta data in file systems
    if(w < off && h < off || w <= 2 || h <= 2) {
      rectangles.add(r);
      return;
    }

    rectangles.add(r);
    final MapList ch = children(r.pre);
    final int cs = ch.size();
    if(cs != 0) {
      makeMap(new MapRect(x, y, w, h, r.pre, r.level + 1), ch, 0, cs - 1);
    }
  }
}
