package org.basex.gui.view.map;

import java.awt.Color;
import java.awt.Graphics;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.view.ViewData;

/**
 * Provides an interface for data specific TreeMap visualizations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class MapPainter {
  /** Graphics reference. */
  final MapView view;
  /** Window properties. */
  final GUIProp prop;

  /**
   * Constructor.
   * @param m map reference
   * @param pr gui properties
   */
  MapPainter(final MapView m, final GUIProp pr) {
    view = m;
    prop = pr;
  }

  /**
   * Returns next color mark.
   * @param rects rectangle array
   * @param ri current position
   * @return next color mark
   */
  final Color color(final MapRects rects, final int ri) {
    // find marked node
    final Nodes marked = view.gui.context.marked;
    final int p = marked == null ? -1 : -marked.find(rects.get(ri).pre) - 1;
    if(p < 0) return GUIConstants.colormark1;

    // mark ancestor of invisible node;
    final int i = rects.find(rects.get(ri));
    return p < marked.size() && i + 1 < rects.size && marked.sorted[p] <
      rects.sorted[i + 1].pre ? GUIConstants.colormark2 : null;
  }

  /**
   * Draws the specified rectangles.
   * @param g graphics reference
   * @param r rectangle array
   * @param scale scale boarders using this factor
   */
  abstract void drawRectangles(final Graphics g, final MapRects r,
      final float scale);

  /**
   * Draws a single rectangle.
   * @param g graphics reference
   * @param rect rectangle to be drawn
   * @return if space is left in the paint area
   */
  abstract boolean drawRectangle(final Graphics g, final MapRect rect);

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
  void init(final MapRects rects) { }

  /**
   * Returns the content for the specified pre value.
   * @param data data reference
   * @param mr map rectangle
   * @return byte[] content
   */
  byte[] content(final Data data, final MapRect mr) {
    return ViewData.content(data, mr.pre, false);
  }

  /**
   * Resets the painter.
   */
  void reset() { }

  /**
   * Closes the painter.
   */
  void close() { }
}
