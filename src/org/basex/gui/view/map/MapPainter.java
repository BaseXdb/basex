package org.basex.gui.view.map;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import org.basex.data.Nodes;
import org.basex.gui.GUIConstants;

/**
 * Provides an interface for data specific TreeMap visualizations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
abstract class MapPainter {
  /** Graphics reference. */
  MapView view;

  /**
   * Constructor.
   * @param m map reference.
   */
  MapPainter(final MapView m) {
    view = m;
  }

  /**
   * Returns next color mark.
   * @param rects rectangle array
   * @param ri current position
   * @return next color mark
   */
  final Color color(final ArrayList<MapRect> rects, final int ri) {
    // find marked node
    final Nodes marked = view.gui.context.marked();
    final int p = -marked.find(rects.get(ri).pre) - 1;
    if(p < 0) return GUIConstants.colormark1;
    else return null;
    // mark ancestor of invisible node
    // [JH] does not mark ancestors if MapNodes have been sorted
    // better find parent to mark
//    final Data data = view.gui.context.data();
//    if(ViewData.parent(data, p)) return GUIConstants.colormark2;
//    return p < marked.size() && ri + 1 < rects.size() && 
//      marked.sorted[p] < rects.get(ri + 1).pre ? GUIConstants.colormark2 : 
//    null;
  }
  
  /**
   * Paints node contents.
   * @param g graphics reference
   * @param r rectangle array
   */
  abstract void drawRectangles(final Graphics g, final ArrayList<MapRect> r);

  /**
   * Reacts on a mouse over/mouse click on the focused area.
   * @param click mouse click (false: mouse move)
   * @param rect current rectangle
   * @param mx mouse x
   * @param my mouse y
   * @return true if area is mouse sensitive
   */
  @SuppressWarnings("unused")
  boolean mouse(final MapRect rect, final int mx, final int my,
      final boolean click) {
    return false;
  }

  /**
   * Initializes the painter.
   * @param rects rectangle array
   */
  @SuppressWarnings("unused")
  void init(final ArrayList<MapRect> rects) { }

  /**
   * Resets the painter.
   */
  void reset() { }

  /**
   * Closes the painter.
   */
  void close() { }
}
